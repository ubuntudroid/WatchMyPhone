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
package de.tudresden.inf.rn.mobilis.server.agents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.FromMatchesFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.ToContainsFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;

import se.su.it.smack.pubsub.PubSub;
import se.su.it.smack.pubsub.elements.CreateElement;
import de.tudresden.inf.rn.mobilis.server.MobilisManager;
import de.tudresden.inf.rn.mobilis.xmpp.pubsub.elements.ConfigureElement;

/**
 *
 * @author Istvan, Christopher
 */
public class SessionAgent extends MobilisAgent implements PacketListener {

    public SessionAgent(String ident) {
		super(ident);
	}

	public SessionAgent(String username, String password, MobilisAgent refAgentForConfig) {
		super(username, password, refAgentForConfig);
	}

	// member
    private String mGroupName;
    private MultiUserChat mMuc;
    //private static String mGroupPubSubNode;

    public void shutdown() throws XMPPException {
        if (getConnection().isConnected()) {
            mMuc.destroy(getIdent(), null);
            MobilisManager.getLogger().info("Deleting temporary account: " + getConnection().getUser());
            getConnection().getAccountManager().deleteAccount();
        }
        super.shutdown();
    }

    /**
     * Creates a new multi user chat with the given groupname and invites the requester.
     * @param groupName The name of the group (multi user chatroom) to create.
     * @param requesterJid The requester of the new group, will be invited.
     */
    public void initializeGroup(String groupName, String requesterJid) {
        try {
        	mGroupName = groupName;
            //String serviceName = "conference." + mConnection.getServiceName();
        	String serviceName = MobilisManager.getInstance().getSettingString("services", "SessionService", "ChatRoomServer");

            // check if group name is occupied, if so, choose another name
            Collection<HostedRoom> hostedRooms = MultiUserChat.getHostedRooms(getConnection(), serviceName);
            synchronized(mGroupName) {
                boolean inUse;
                int i = 1;
                do {
                    inUse = false;
                    for (HostedRoom hr : hostedRooms) {
                        if (hr.getName().equals(mGroupName)) {
                            inUse = true;
                            mGroupName = groupName + i;
                            i++;
                            break;
                        }
                    }
                } while (inUse);
            }

            // create new muc for group
            String roomName = mGroupName + "@" + serviceName;
            mMuc = new MultiUserChat(getConnection(), roomName);
            mMuc.addParticipantListener(this);
            mMuc.create(getIdent());
            mMuc.sendConfigurationForm(new Form(Form.TYPE_SUBMIT));

            // create pubsub node
            //createGroupNode();

            // add member, send invitation and send node id.
            addMember(requesterJid);
        } catch (XMPPException ex) {
            Logger.getLogger(SessionAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Creates a new PubSub node for the group and gets the newly generated ID.
     */
    public void createGroupNode() {
        String pubsubserver = MobilisManager.getInstance().getSettingString("services", "SessionService", "PubSubServer");
    	
    	PubSub pubSub = new PubSub();
        pubSub.setNamespace("http://jabber.org/protocol/pubsub");
        pubSub.setFrom(getConnection().getUser());
        pubSub.setTo(pubsubserver);
        pubSub.setType(IQ.Type.SET);
        CreateElement ce = new CreateElement();
        pubSub.appendChild(ce);
        // create configure element in order to set type to collection
        ConfigureElement confElem = new ConfigureElement("collection");
        confElem.setParentNode(pubsubserver);
        pubSub.appendChild(confElem);

        //PacketFilter pf = new PacketExtensionFilter("pubsub", "http://jabber.org/protocol/pubsub");
        PacketFilter pf = new AndFilter(new FromMatchesFilter("pubsub.acer666"), new ToContainsFilter("sessionagent")); //TODO: acer.666 ???
        PacketCollector response = getConnection().createPacketCollector(pf);
        getConnection().sendPacket(pubSub);
        
        System.out.println(pubSub.toXML());

        PubSub ps = (PubSub) response.nextResult(SmackConfiguration.getPacketReplyTimeout());
        response.cancel();
        if (ps != null) {
            //CreateElement createElem = (CreateElement) ps.getChildren().get(0);
            //mGroupPubSubNode = createElem.getNode();
        }
    }

    /**
     * Invites the user to the MUC and affiliates it to the pubsub node.
     * @param jid The Jid of whom to add.
     */
    public void addMember(String jid) {
        mMuc.invite(jid, "Enter group.");

//        PubSub ps = new PubSub();
//        ps.setFrom(mConnection.getUser());
//        ps.setTo(SessionCoordinator.mPubSubServer);
//        ps.setCustomNamespace("http://jabber.org/protocol/pubsub#owner");
//        ps.setType(IQ.Type.SET);
//        AffiliationsElement affs = new AffiliationsElement(mGroupPubSubNode);
//        AffiliationElement aff = new AffiliationElement();
//        aff.setJID(StringUtils.parseBareAddress(jid));
//        aff.setAffiliation("publisher");
//        affs.appendChild(aff);
//        ps.appendChild(affs);
//        mConnection.sendPacket(ps);
//        
//        // notify client about affiliation (not done by server implementation)
//        Message notifyMessage = new Message();
//        notifyMessage.setTo(jid);
//        PubSubPacketExtension ppe = new PubSubPacketExtension();
//        AffiliationsElement affsNotfy = new AffiliationsElement(mGroupPubSubNode);
//        AffiliationElement affNotfy = new AffiliationElement();
//        affNotfy.setJID(StringUtils.parseBareAddress(jid));
//        affNotfy.setAffiliation("publisher");
//        affsNotfy.appendChild(affNotfy);
//        ppe.setChild(affsNotfy);
//        notifyMessage.addExtension(ppe);
//        mConnection.sendPacket(notifyMessage);
    }

    /**
     * Returns the room name.
     * @return
     */
    public String getGroupName() {
        return mGroupName;
    }

    /**
     * Returns a list of all group members' Jids.
     * @return ArrayList of all group members' jids.
     */
    public ArrayList<String> getGroupMembers() {
        ArrayList<String> participants = new ArrayList<String>();
        try {
            ArrayList<Occupant> occupants = (ArrayList<Occupant>) mMuc.getParticipants();
            for (Occupant occ : occupants) {
                participants.add(occ.getJid());
            }
        } catch (XMPPException ex) {
            Logger.getLogger(SessionAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return participants;
    }
    
    public void processPacket(Packet p) {
    	// TODO SessionCoordinator.getInstance().getParentView().setGroupMemberValues(mGroupName, getGroupMembers());
//    	MobilisManager.getInstance().getServerView().refreshItem("sessiongroup-"+mGroupName, getGroupMembers());
    }
    
    
}
