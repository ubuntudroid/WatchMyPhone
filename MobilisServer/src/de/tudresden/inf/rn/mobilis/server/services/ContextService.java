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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.packet.IQ.Type;

import de.tudresden.inf.rn.mobilis.server.MobilisManager;
import de.tudresden.inf.rn.mobilis.xmpp.beans.LocationInfo;
import de.tudresden.inf.rn.mobilis.xmpp.packet.LocationIQ;

/**
* The purpose of the ContextService is to receive location updates 
* and distribute them to other server-side services.
* 
* @author Christopher
*/
public class ContextService extends MobilisService {
	
    private final Map<String, LocationInfo> mLocations = Collections.synchronizedMap(new HashMap<String, LocationInfo>());
    private final Collection<ContextAwareness> mContextAwareServices = Collections.synchronizedSet(new HashSet<ContextAwareness>());
	
	public ContextService() {
		super();
	}
	
    public void registerContextAwareService(ContextAwareness service) {
    	synchronized(mContextAwareServices) {
    		mContextAwareServices.add(service);
    	}
    }

    public void unregisterContextAwareService(ContextAwareness service) {
    	synchronized(mContextAwareServices) {
    		mContextAwareServices.remove(service);
    	}
    }
    
    // getter/setter functions

    public void setLocation(String jid, LocationInfo location) {
    	synchronized(mLocations) {
    		mLocations.put(jid, location);
    	}
    	
    	synchronized(mContextAwareServices) {
    		for (ContextAwareness service : mContextAwareServices) {
    			service.setLocation(jid, location);
    		}
    	}
    }
    
    public LocationInfo getLocation(String jid) {
    	synchronized(mLocations) {
    		return mLocations.get(jid);
    	}
    }

    public boolean containsLocation(String jid) {
    	synchronized(mLocations) {
    		return mLocations.containsKey(jid);
    	}
    }
    
    // XMPP related functions

	@Override
	protected void registerPacketListener() {
        mAgent.getConnection().addPacketListener(this, new AndFilter(
        	new PacketTypeFilter(LocationIQ.class),
        	new IQTypeFilter(Type.SET)
        ));
	}

	@Override
	public List<String> getNodeFeatures() {
		List<String> features = super.getNodeFeatures();
		features.add(MobilisManager.discoNamespace + "#locationupdate");
		return features;
    }

	@Override
	public void processPacket(Packet p) {
		super.processPacket(p);
		
        if (p instanceof LocationIQ) {
        	LocationIQ iq = (LocationIQ) p;
            String requester = iq.getFrom();
            
            try {
				LocationInfo location = iq.getLocation();
				setLocation(requester, location);
				
				iq.setType(Type.RESULT);
				
				MobilisManager.getLogger().info(requester + " updated location.");
			} catch (Exception e) {
				iq.setType(Type.ERROR);
				iq.setError(new XMPPError(XMPPError.Condition.remote_server_error));
				
				MobilisManager.getLogger().warning(requester + " failed to update location.");
			}
			
			// send result packet
			iq.setFrom(mAgent.getConnection().getUser());
			iq.setTo(requester);
			mAgent.getConnection().sendPacket(iq);
        }
	}
}
