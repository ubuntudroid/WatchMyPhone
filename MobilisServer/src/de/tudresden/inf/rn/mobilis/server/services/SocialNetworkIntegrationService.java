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

import java.lang.reflect.Constructor;
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
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverItems;

import de.tudresden.inf.rn.mobilis.server.MobilisManager;
import de.tudresden.inf.rn.mobilis.server.services.integration.MobilisIntegrationService;
import de.tudresden.inf.rn.mobilis.xmpp.packet.BuddylistIQ;

/**
* The purpose of SocialNetworkIntegrationService is to manage friend lists for 
* all XMPP users using AndroidBuddy.
*
* @author Christopher
*/
public class SocialNetworkIntegrationService extends MobilisService {
	
    private final Map<String, MobilisIntegrationService> mIntegrationServices = Collections.synchronizedMap(new HashMap<String, MobilisIntegrationService>());
	
    public final static String UNINITIALIZED = "uninitialized";
    public final static String INITIALIZED = "initialized";
	public final static String STARTED = "started";
	public final static String STOPPED = "stopped";
    
    private String mStatus = UNINITIALIZED;
    
	public SocialNetworkIntegrationService() {
		super();
	}
	
	//returns all the buddies jids for a jid
	public Collection<String> getBuddies(String jid) {
		Collection<String> buddies = new HashSet<String>();
		
		synchronized(mIntegrationServices) {
			for (String iskey : mIntegrationServices.keySet()) {
				try {
					MobilisIntegrationService service = mIntegrationServices.get(iskey);
					Collection<String> buds = service.getBuddies(jid);
					if (buds != null) {
						buddies.addAll(buds);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
				}
			}
		}
		
		return buddies;
    }
	
	//returns all the trackable buddies jids for a jid
	public Collection<String> getTrackableBuddies(String jid) {
		Collection<String> buddies = new HashSet<String>();
		
		synchronized(mIntegrationServices) {
			for (String iskey : mIntegrationServices.keySet()) {
				try {
					MobilisIntegrationService service = mIntegrationServices.get(iskey);
					if (service.isTrackable()) {
						Collection<String> buds = service.getBuddies(jid);
						if (buds != null) {
							buddies.addAll(buds);
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
				}
			}
		}
		
		return buddies;
    }
	
	//returns all the buddies jids of a jid from a certain network 
	public Collection<String> getBuddies(String jid, String network) {
		synchronized(mIntegrationServices) {
			MobilisIntegrationService service = mIntegrationServices.get(network);
			return service.getBuddies(jid);
		}
    }

    public MobilisIntegrationService getIntegrationService(String network, String classname) {
		MobilisIntegrationService singleton = null;
		
		String packagename = getClass().getPackage().getName();
		if (!classname.startsWith(packagename)) {
			classname = packagename + ".integration." + classname;
		}

		synchronized(mIntegrationServices) {
			singleton = mIntegrationServices.get(network);
		}

		if (singleton != null) {
			return singleton;
		}

		try {
			Class<?> agentClass = Class.forName(classname);
			Constructor<?> constructor = agentClass.getConstructor( new Class[]{ String.class } );
			singleton = (MobilisIntegrationService) constructor.newInstance( new Object[]{ network } );
		} catch (Exception e) {
			MobilisManager.getLogger().severe("Couldn't instantiate integration service: " + network + " (" + classname + ")");
		}

		synchronized(mIntegrationServices) {
			mIntegrationServices.put(network, singleton);
		}

		return singleton;
	}

	private void setupIntegrationServices() {
		// bootstrap service instances from configuration
		for (String networkIdent : MobilisManager.getInstance().getSettings("networks").keySet()) {
			String className;
			try {
				className = MobilisManager.getInstance().getSettingString("networks", networkIdent, "type");
				getIntegrationService(networkIdent, className);
			} catch (Exception e) {
				MobilisManager.getLogger().warning("Mobilis Integration Service (" + networkIdent + ") setup failed.");
			}
		}
	}
	
	@Override
	public void startup() throws Exception {
		super.startup();
		
		synchronized(mStatus) {
			if (mStatus == UNINITIALIZED) {
				setupIntegrationServices();
				mStatus = INITIALIZED;
			}
		}
		
		ServiceDiscoveryManager sdm = ServiceDiscoveryManager.getInstanceFor(mAgent.getConnection());

		synchronized(mStatus) {
			if ((mStatus == INITIALIZED) || (mStatus == STOPPED)) {
				// startup IntegrationServices
		    	synchronized(mIntegrationServices) {
		    		for (String networkIdent : mIntegrationServices.keySet()) {
		    			MobilisIntegrationService network = mIntegrationServices.get(networkIdent);
		    			try {
		    				network.startup(this);
		    				sdm.setNodeInformationProvider(network.getNode(), network);
		    				//MobilisManager.getLogger().info("Mobilis Integration Service (" + networkIdent + ") started up.");
		    			} catch (Exception e) {
		    				mIntegrationServices.remove(networkIdent);
		    				MobilisManager.getLogger().warning("Mobilis Integration Service (" + networkIdent + ") startup failed.");
		    			}
		    		}
		    	}
		    	mStatus = STARTED;
			} else {
				MobilisManager.getLogger().info("SocialNetworkIntegrationService is already up.");
			}
		}
    }
	
	@Override
	public void shutdown() throws Exception {
    	super.shutdown();
		
		ServiceDiscoveryManager sdm = ServiceDiscoveryManager.getInstanceFor(mAgent.getConnection());

    	synchronized(mStatus) {
        	if (mStatus == STARTED) {
        		// startup IntegrationServices
            	synchronized(mIntegrationServices) {
            		for (String networkIdent : mIntegrationServices.keySet()) {
            			MobilisIntegrationService network = mIntegrationServices.get(networkIdent);
            			try {
            				sdm.removeNodeInformationProvider(network.getNode());
            				network.shutdown();
            				//MobilisManager.getLogger().info("Mobilis Integration Service (" + networkIdent + ") shut down.");
            			} catch (Exception e) {
            				mIntegrationServices.remove(networkIdent);
            				MobilisManager.getLogger().warning("Mobilis Integration Service (" + networkIdent + ") shutdown failed.");
            			}
            		}
            	}
            	mStatus = STOPPED;
        	} else {
    			MobilisManager.getLogger().info("SocialNetworkIntegrationService is already down.");
    		}
    	}
    }
	
    // XMPP related functions
    
	@Override
    public List<String> getNodeFeatures() {
		List<String> features = super.getNodeFeatures();
		features.add(MobilisManager.discoNamespace + "#getbuddylist");
		return features;
    }

	@Override
    public List<DiscoverItems.Item> getNodeItems() {
        List<DiscoverItems.Item> items = super.getNodeItems();
		synchronized(mIntegrationServices) {
			// services offered by this agent
			for (String serviceName : mIntegrationServices.keySet()) {
				try {
					DiscoverItems.Item item = mIntegrationServices.get(serviceName).getDiscoverItem();
					if (item != null) {
						items.add(item);
					}
				} catch (Exception e) {

				}
			}
		}
        return items;
    }

	@Override
	protected void registerPacketListener() {
		mAgent.getConnection().addPacketListener(this, new AndFilter(
			new PacketTypeFilter(BuddylistIQ.class),
			new IQTypeFilter(Type.GET)
		));
	}

	@Override
	public void processPacket(Packet p) {
		super.processPacket(p);
		
		if (p instanceof BuddylistIQ) {
			BuddylistIQ iq = (BuddylistIQ) p;
			String requester = iq.getFrom();
			
			try {
				Collection<String> buddies = getBuddies(requester);
				iq.setBuddies(buddies);
				
				//send result packet
				iq.setType(Type.RESULT);
				
				MobilisManager.getLogger().info(requester + " retrieved buddylist.");
			} catch (Exception e) {
				iq.setType(Type.ERROR);
				
				MobilisManager.getLogger().warning(requester + " retrieval of buddylist failed.");
			}
			
			iq.setFrom(mAgent.getConnection().getUser());
			iq.setTo(requester);
			
			mAgent.getConnection().sendPacket(iq);
		}
	}
}
