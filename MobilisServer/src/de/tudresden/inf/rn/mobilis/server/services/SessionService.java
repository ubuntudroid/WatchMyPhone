/*******************************************************************************
 * Copyright (C) 2010 Technische Universität Dresden
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: http://mobilisplatform.sourceforge.net
 ******************************************************************************/
package de.tudresden.inf.rn.mobilis.server.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.jivesoftware.smackx.packet.DiscoverItems.Item;

import se.su.it.smack.pubsub.PubSub;
import se.su.it.smack.pubsub.elements.CreateElement;
import de.tudresden.inf.rn.mobilis.server.MobilisManager;
import de.tudresden.inf.rn.mobilis.server.agents.SessionAgent;
import de.tudresden.inf.rn.mobilis.xmpp.packet.CreateGroupIQ;
import de.tudresden.inf.rn.mobilis.xmpp.packet.JoinGroupIQ;
import de.tudresden.inf.rn.mobilis.xmpp.packet.QueryGroupsIQ;
import de.tudresden.inf.rn.mobilis.xmpp.pubsub.elements.ConfigureElement;

public class SessionService extends MobilisService {

	private Map<String, SessionAgent> mSessionAgents = Collections.synchronizedMap(new HashMap<String, SessionAgent>());
	private AccountManager mAccountManager;
	
	public SessionService() {
		super();
	}
	
	@Override
	public void startup() throws Exception {
		super.startup();

		// get account manager in order to create session agent accounts later
		mAccountManager = new AccountManager(mAgent.getConnection());

		// Initializes the session coordinator tasks. Ensures that mobilis PubSub root node exists.
		try {
			ServiceDiscoveryManager sdm = ServiceDiscoveryManager.getInstanceFor(this.mAgent.getConnection());
			DiscoverItems discoverItems = sdm.discoverItems(getSettingString("PubSubServer"));
			Iterator<Item> items = discoverItems.getItems();
			boolean hasServiceNode = false;
			while (items.hasNext()) {
				Item i = items.next();
				if (i.getNode().equals(getSettingString("PubSubNode")))
					hasServiceNode = true;
			}
			if (!hasServiceNode) {
				// if root pubsub node doesn't exist, create it
				createRootPubSubNode();
				MobilisManager.getLogger().info("PubSub node (" + getSettingString("PubSubNode") + ") has been created at (" + getSettingString("PubSubServer") + ")");
			}
		} catch (Exception e) {
			MobilisManager.getLogger().warning("Couldn't initialize Session Coordinator PubSub root: " + getSettingString("PubSubNode"));
		}
	}

	@Override
	public void shutdown() throws Exception {
		// disconnecting and deleting the session agents
		if ((mAgent.getConnection() != null) && mAgent.getConnection().isConnected()) {
			synchronized(mSessionAgents) {
				for (String sa : mSessionAgents.keySet()) {
					try {
//						RosterEntry ra = mAgent.getConnection().getRoster().getEntry(sa);
//						mAgent.getConnection().getRoster().removeEntry(ra);
//						mAgent.getConnection().getAccountManager().deleteAccount();
						mSessionAgents.get(sa).shutdown();
					} catch (Exception e) {
						MobilisManager.getLogger().warning("Couldn't shutdown Session Agent: " + sa);
					}
				}
				mSessionAgents.clear();
			}
		}
		
		super.shutdown();
		
		// clear group list : setGroupListView();
		ArrayList<String> groups = new ArrayList<String>();
		synchronized(mSessionAgents) {
			for (String sessAgent : mSessionAgents.keySet()) {
				groups.add(mSessionAgents.get(sessAgent).getGroupName());
			}
		}
//		MobilisManager.getInstance().getServerView().refreshItem("sessiongroups", groups);
	}

	public ArrayList<String> getGroupMembers(String groupName) {
		ArrayList<String> result = null;
		synchronized(mSessionAgents) {
			for (SessionAgent sa : mSessionAgents.values()) {
				if (sa.getGroupName().equals(groupName)) {
					// found group
					result = sa.getGroupMembers();
				}
			}
		}
		return result;
	}

	// XMPP related functions

	/**
	 * Creates the root collection node for publish/subscribe of the service.
	 */
	private void createRootPubSubNode() throws XMPPException {
		PubSub pubSub = new PubSub();
		pubSub.setNamespace("http://jabber.org/protocol/pubsub");
		pubSub.setFrom(mAgent.getConnection().getUser());
		pubSub.setTo(getSettingString("PubSubServer"));
		pubSub.setType(IQ.Type.SET);
		CreateElement ce = new CreateElement(getSettingString("PubSubNode"));
		pubSub.appendChild(ce);
		// create configure element in order to set type to collection
		ConfigureElement confElem = new ConfigureElement("collection");
		pubSub.appendChild(confElem);
		mAgent.getConnection().sendPacket(pubSub);
	}
	
	@Override
	protected void registerPacketListener() {
		mAgent.getConnection().addPacketListener(this, new OrFilter(
				new AndFilter(
						new PacketTypeFilter(QueryGroupsIQ.class), 
						new IQTypeFilter(Type.GET)),
				new OrFilter(
						new AndFilter(
								new PacketTypeFilter(CreateGroupIQ.class),
								new IQTypeFilter(Type.GET)),
						new AndFilter(
								new PacketTypeFilter(JoinGroupIQ.class),
								new IQTypeFilter(Type.GET)))
		));
	}

	@Override
	public List<String> getNodeFeatures() {
		List<String> features = super.getNodeFeatures();
		features.add(MobilisManager.discoNamespace + "#querygroups");
		features.add(MobilisManager.discoNamespace + "#creategroup");
		features.add(MobilisManager.discoNamespace + "#joingroup");
		return features;
    }

	@Override
	public void processPacket(Packet p) {
		super.processPacket(p);
		
		if (p instanceof QueryGroupsIQ) {
			// return result IQ with all active groups
			QueryGroupsIQ qgi = (QueryGroupsIQ) p;
			String requester = qgi.getFrom();

			qgi.setType(Type.RESULT);
			qgi.setFrom(mAgent.getConnection().getUser());
			qgi.setTo(requester);

			synchronized(mSessionAgents) {
				for (SessionAgent sa : mSessionAgents.values()) {
					qgi.addItem(sa.getGroupName(), sa.getJid());
				}
			}

			mAgent.getConnection().sendPacket(qgi);
		} else if (p instanceof CreateGroupIQ) {
			// create a new group
			CreateGroupIQ cgi = (CreateGroupIQ) p;
			String groupName = cgi.getGroup();
			String requester = cgi.getFrom();
			//String userSuffix = "@" + mConnConfig.getServiceName();
			String userSuffix = "@" + mAgent.getSettingString("service");
			
			// create new session agent
			int sessionCounter = 0;
			String sessAgent = null;
			synchronized(mSessionAgents) {
				do {
					sessionCounter++;
					sessAgent = "sessionagent" + sessionCounter;
				} while (mSessionAgents.containsKey(sessAgent + userSuffix));
			}

			boolean done = true;
			
			do {
				
				try {
					String sessPasswd = String.valueOf((new java.security.SecureRandom()).nextInt());
					if (!mAgent.getConnection().getRoster().contains(sessAgent + userSuffix)) {
						mAccountManager.createAccount(sessAgent, sessPasswd);
					}

					SessionAgent sa = new SessionAgent(sessAgent, sessPasswd, mAgent);
					synchronized(mSessionAgents) {
						mSessionAgents.put(sessAgent + userSuffix, sa);
					}
					sa.startup();
					sa.initializeGroup(groupName, requester);

					cgi.setType(Type.RESULT);
					done = true;

				} catch (NumberFormatException e) {
					MobilisManager.getLogger().severe("Couldn't create Session Group: " + groupName);
					cgi.setType(Type.ERROR);
				} catch (XMPPException e) {
					if (e.getMessage().equals("conflict(409)")) {
						// try again if there is an old session coordinator remaining on the server
						sessionCounter++;
						sessAgent = "sessionagent" + sessionCounter;
						done = false;
					} else {
						MobilisManager.getLogger().severe("Couldn't create Session Group: " + groupName);
						cgi.setType(Type.ERROR);
					}
				}
				
			} while(!done);

			// TODO update view : setGroupListView();

			// send result packet
			cgi.setFrom(mAgent.getConnection().getUser());
			cgi.setTo(requester);
			mAgent.getConnection().sendPacket(cgi);
		} else if (p instanceof JoinGroupIQ) {
			JoinGroupIQ jgi = (JoinGroupIQ) p;
			String groupName = jgi.getGroup();
			String requester = jgi.getFrom();
			synchronized(mSessionAgents) {
				for (SessionAgent sa : mSessionAgents.values()) {
					if (sa.getGroupName().equals(groupName)) {
						// found group
						sa.addMember(requester);
						break;
					}
				}
			}
		}
	}
	
	public Map<String, SessionAgent> getSessions() {
		return mSessionAgents;
	}
}
