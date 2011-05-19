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
import java.util.List;

import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.IQ.Type;

import de.tudresden.inf.rn.mobilis.server.MobilisManager;
import de.tudresden.inf.rn.mobilis.xmpp.filter.NetworkFilter;
import de.tudresden.inf.rn.mobilis.xmpp.packet.BuddylistIQ;

/**
* The purpose of DefaultIntegrationService is to keep a buddylist for a network.
 * 
 * @author Christopher
 */
public class DefaultIntegrationService extends MobilisIntegrationService {

	public DefaultIntegrationService(String network) {
		super(network);
    }
	
	// XMPP related functions
	
	@Override
	protected void registerPacketListener() {
		mSNIS.getAgent().getConnection().addPacketListener(this, new AndFilter(
           	new NetworkFilter(getIdent()),
        	new PacketTypeFilter(BuddylistIQ.class),
        	new IQTypeFilter(Type.SET)
        ));
	}

	@Override
	public List<String> getNodeFeatures() {
		List<String> features = super.getNodeFeatures();
		features.add(MobilisManager.discoNamespace + "#buddylist");
		return features;
    }

	@Override
	public void processPacket(Packet p) {
        if (p instanceof BuddylistIQ) {
        	BuddylistIQ iq = (BuddylistIQ) p;
            String requester = iq.getFrom();
            
            try {
				String identity = iq.getIdentity();
				if (identity != null) {
				    setIdentity(requester, identity);
				}
				
				Collection<String> contacts = iq.getBuddies();
				if (contacts != null) {
				    setBuddies(identity, contacts);
				    MobilisManager.getLogger().info(requester + " added " + contacts.size() + " contacts from " + iq.getNetwork() );
				    MobilisManager.getLogger().finest("Buddies: " + contacts);
				}
				
				iq.setType(Type.RESULT);
				
				MobilisManager.getLogger().info(requester + " retrieved " + getIdent() + " buddylist.");
			} catch (Exception e) {
				iq.setType(Type.ERROR);
				
				MobilisManager.getLogger().warning(requester + " tried to retrieve " + getIdent() + " buddylist and FAILED.");
			}
            
			iq.setFrom(mSNIS.getAgent().getConnection().getUser());
			iq.setTo(requester);
			
			mSNIS.getAgent().getConnection().sendPacket(iq);
        }
	}
}
