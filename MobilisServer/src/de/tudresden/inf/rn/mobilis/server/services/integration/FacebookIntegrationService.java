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
package de.tudresden.inf.rn.mobilis.server.services.integration;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.packet.IQ.Type;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.google.code.facebookapi.FacebookXmlRestClient;

import de.tudresden.inf.rn.mobilis.server.MobilisManager;
import de.tudresden.inf.rn.mobilis.xmpp.filter.NetworkFilter;
import de.tudresden.inf.rn.mobilis.xmpp.packet.NetworkIQ;

/**
 * The purpose of FacebookIntegrationService is to sync Facebook friends with
 * the SocialNetworkIntegrationService friends list and also communicate with 
 * clients to get credentials for accessing Facebook on their behalf.
 *
 * @author Christopher
 */
public class FacebookIntegrationService extends MobilisIntegrationService {

	private final String mApiKey;
	private final String mApiSecret;
	
	// per JID - authtoken cache until getSession() is called
	private Map<String, String> mFacebookAuthTokens;
	// per JID
	private Map<String, FacebookXmlRestClient> mFacebookClients;

	public FacebookIntegrationService(String network) {
		super(network);

		mApiKey = getSettingString("apikey");
		mApiSecret = getSettingString("secret");

		mFacebookClients = Collections.synchronizedMap(new HashMap<String, FacebookXmlRestClient>());
		mFacebookAuthTokens = Collections.synchronizedMap(new HashMap<String, String>());
	}

	private FacebookXmlRestClient getFacebookClient(String jid) {
		FacebookXmlRestClient fbClient;
		synchronized(mFacebookClients) {
			if (mFacebookClients.containsKey(jid)) {
				fbClient = mFacebookClients.get(jid);
			} else {
				fbClient = new FacebookXmlRestClient(mApiKey, mApiSecret);
				fbClient.setIsDesktop(true);
				mFacebookClients.put(jid, fbClient);
			}
		}
		return fbClient;
	}

	// make sure to use the same token and secure against hackers
	private String getAuthToken(String jid) throws Exception {
		FacebookXmlRestClient fbClient = getFacebookClient(jid);
		String authtoken = null;
		synchronized(mFacebookAuthTokens) {
			if (mFacebookAuthTokens.containsKey(jid)) {
				authtoken = mFacebookAuthTokens.get(jid);
			} else {
				try {
					authtoken = fbClient.auth_createToken();
					mFacebookAuthTokens.put(jid, authtoken);
					MobilisManager.getLogger().fine("Retrieved authtoken " + authtoken + " from Facebook for JID " + jid);
				} catch (Exception e) {
					MobilisManager.getLogger().warning("Failed to get authtoken from Facebook for user " + jid);
					throw new Exception();
				}
			}
		}
		return authtoken;
	}

	private void getSession(String jid) throws Exception {
		FacebookXmlRestClient fbClient = getFacebookClient(jid);
		synchronized(mFacebookAuthTokens) {
			if (mFacebookAuthTokens.containsKey(jid)) {
				String authtoken = mFacebookAuthTokens.get(jid);
				try {
					String sessionkey = fbClient.auth_getSession(authtoken);
					MobilisManager.getLogger().fine("Retrieved sessionkey " + sessionkey + " from Facebook for JID " + jid + " and authtoken " + authtoken);
					
					// has to be refetched for the next getSession() call.
					mFacebookAuthTokens.remove(jid);
					
					String userId = fbClient.getCacheUserId().toString();
					setIdentity(jid, userId);
					
					MobilisManager.getLogger().finer("Retrieved identity " + userId + " from Facebook for JID " + jid);
				} catch (Exception e) {
					MobilisManager.getLogger().finest("Show RawResponse (" + fbClient.getRawResponse() + ") from Facebook for JID " + jid);
					MobilisManager.getLogger().finest("Show SessionSecret (" + fbClient.getSessionSecret() + ") from Facebook for JID " + jid);
					MobilisManager.getLogger().finest("Show CacheUserId (" + fbClient.getCacheUserId() + ") from Facebook for JID " + jid);
					MobilisManager.getLogger().warning("Failed to get sessionkey from Facebook for user " + jid);
					throw new Exception();
				}
			} else {
				throw new Exception();
			}
		}
	}

	private void syncProfile(String jid) throws Exception {
		FacebookXmlRestClient fbClient = getFacebookClient(jid);
		
		if (fbClient.getCacheSessionKey() == null) {
			getSession(jid);
		}

		HashMap<String, String> profile = new HashMap<String, String>();
		try {
			Document fbXmlProfile = fbClient.profile_getInfo(fbClient.getCacheUserId());
			//fbClient.users_getStandardInfo(userIds, fields)
			//TODO: implement
			throw new Exception();
		} catch (Exception e) {
			MobilisManager.getLogger().finest("Show RawResponse (" + fbClient.getRawResponse() + ") from Facebook for JID " + jid);
			MobilisManager.getLogger().warning("Failed to add profile information from Facebook for user " + jid);
			throw new Exception();
		}
	}

	private void syncBuddylist(String jid) throws Exception {
		FacebookXmlRestClient fbClient = getFacebookClient(jid);
		
		if (fbClient.getCacheSessionKey() == null) {
			getSession(jid);
		}

		Collection<String> buddies = new HashSet<String>();
		try {
			Document fbXmlBuddies = fbClient.friends_get();
			//Document fbXmlBuddies = fbClient.friends_getLists();
			NodeList fbXmlBuddiesNL = fbXmlBuddies.getElementsByTagName("uid");
			for (int i = 0; i < fbXmlBuddiesNL.getLength(); i++) {
				buddies.add(fbXmlBuddiesNL.item(i).getFirstChild().getNodeValue());
			}
			setBuddies(getIdentity(jid), buddies);
			MobilisManager.getLogger().info("Added " + buddies.size() + " Facebook-contacts for " + jid);
		} catch (Exception e) {
			MobilisManager.getLogger().finest("Show RawResponse (" + fbClient.getRawResponse() + ") from Facebook for JID " + jid);
			//MobilisManager.getLogger().finest("Show CacheFriendsList (" + fbClient.getCacheFriendsList() + ") from Facebook for JID " + jid);
			MobilisManager.getLogger().warning("Failed to add contacts from Facebook for user " + jid);
			throw new Exception();
		}
	}

	private void syncGroups(String jid) throws Exception {
		FacebookXmlRestClient fbClient = getFacebookClient(jid);
		
		if (fbClient.getCacheSessionKey() == null) {
			getSession(jid);
		}

		Collection<String> groups = new HashSet<String>();
		try {
			Document fbXmlGroups = fbClient.groups_get(null, null);
			NodeList fbXmlGroupsNL = fbXmlGroups.getElementsByTagName("gid");
			for (int i = 0; i < fbXmlGroupsNL.getLength(); i++) {
				groups.add(fbXmlGroupsNL.item(i).getFirstChild().getNodeValue());
			}
			setGroups(getIdentity(jid), groups);

			MobilisManager.getLogger().fine("Added " + groups.size() + " groups from Facebook for user " + jid);
			MobilisManager.getLogger().finest("FbGroups: " + groups);
		} catch (Exception e) {
			MobilisManager.getLogger().finest("Show RawResponse (" + fbClient.getRawResponse() + ") from Facebook for JID " + jid);
			MobilisManager.getLogger().warning("Failed to add groups from Facebook for user " + jid);
			throw new Exception();
		}
	}

	// XMPP related functions

	@Override
	public List<String> getNodeFeatures() {
		List<String> features = super.getNodeFeatures();
		features.add(MobilisManager.discoNamespace + "#facebook");
		return features;
	}

	@Override
	protected void registerPacketListener() {
		mSNIS.getAgent().getConnection().addPacketListener(this, new AndFilter(
				new NetworkFilter(getIdent()),
				new PacketTypeFilter(NetworkIQ.class),
				new OrFilter(
						new IQTypeFilter(Type.GET),
						new IQTypeFilter(Type.SET)
				)
		));
	}

	@Override
	public void processPacket(Packet p) {
		if (p instanceof NetworkIQ) {
			NetworkIQ iq = (NetworkIQ) p;
			String requester = iq.getFrom();

			if (iq.getType().equals(Type.GET)) {
				Map<String, String> outParams = new HashMap<String, String>();
				if (iq.getAction().equals("getauthtoken")) {
					try {
						String authtoken = getAuthToken(requester);
						outParams.put("authtoken", authtoken);
						outParams.put("apikey", mApiKey);
						iq.setParams(outParams);
						iq.setType(Type.RESULT);
						
						MobilisManager.getLogger().fine("Sent authtoken " + authtoken + " and apikey " + mApiKey + " for Facebook to JID " + requester);
					} catch (Exception e) {
						iq.setType(Type.ERROR);
						iq.setError(new XMPPError(XMPPError.Condition.remote_server_error));
						
						MobilisManager.getLogger().warning("Facebook Error GET getauthtoken " + e);
					}
				}
			} else if (iq.getType().equals(Type.SET)) {
				if (iq.getAction().equals("login")) {
					// TODO Configure here, what has to be done when the user did autentify 
					//   themselves to facebook
					try {
						//syncProfile(requester);
						syncBuddylist(requester);
						//syncGroups(requester);
						iq.setType(IQ.Type.RESULT);
						
						MobilisManager.getLogger().fine("Logged in and synced Facebook buddylist of JID " + requester);
					} catch (Exception e) {
						iq.setType(Type.ERROR);
						iq.setError(new XMPPError(XMPPError.Condition.remote_server_error));
						
						MobilisManager.getLogger().warning("Facebook Error SET login " + e);
					}
				} 
			}
			
			// send result packet
			iq.setFrom(mSNIS.getAgent().getConnection().getUser());
			iq.setTo(requester);
			mSNIS.getAgent().getConnection().sendPacket(iq);
		}
	}
}
