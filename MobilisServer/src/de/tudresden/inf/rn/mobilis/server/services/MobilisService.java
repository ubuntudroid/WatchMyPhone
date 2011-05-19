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
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smackx.NodeInformationProvider;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.jivesoftware.smackx.packet.DiscoverItems.Item;

import de.tudresden.inf.rn.mobilis.server.MobilisManager;
import de.tudresden.inf.rn.mobilis.server.agents.MobilisAgent;
import de.tudresden.inf.rn.mobilis.xmpp.beans.Mobilis;
import de.tudresden.inf.rn.mobilis.xmpp.filter.ServiceFilter;
import de.tudresden.inf.rn.mobilis.xmpp.packet.SettingsIQ;

public abstract class MobilisService implements PacketListener, NodeInformationProvider {

	protected MobilisAgent mAgent = null;
    private Map<String, Map<String, Object>> mUserSettings;
    private Map<String, Object> mDefaultSettings;
    
    public MobilisService() {
		mUserSettings = Collections.synchronizedMap(new HashMap<String, Map<String, Object>>());
		try {
			mDefaultSettings = MobilisManager.getInstance().getSettings("services", getIdent());
		} catch (Exception e) {
			mDefaultSettings = Collections.synchronizedMap(new HashMap<String, Object>());
			MobilisManager.getLogger().warning("Mobilis Service (" + getIdent() + ") could not read configuration, using empty settings instead.");
		}
    }

    public void startup() throws Exception {
		if (mAgent == null) {
			try {
				String agentName = MobilisManager.getInstance().getSettingString("services", getIdent(), "agent");
				mAgent = MobilisManager.getInstance().getAgent(agentName);
			} catch (Exception e) {
				throw e;
			}
		}
		
		try {
			startup(mAgent);
		} catch (Exception e) {
			throw e;
		}
    }

    public void startup(MobilisAgent agent) throws Exception {
		try {
			mAgent = agent;
			
			// packet listener
			mAgent.getConnection().addPacketListener(this, new AndFilter(
			   	new ServiceFilter(getIdent()),
				new PacketTypeFilter(SettingsIQ.class),
				new OrFilter(
					new IQTypeFilter(Type.GET),
			   		new IQTypeFilter(Type.SET)
			   	)
			));
			registerPacketListener();
			
			// logging
			MobilisManager.getLogger().info("Mobilis Service (" + getIdent() + ") started up.");
		} catch (Exception e) {
			throw e;
		}
    }

    public void shutdown() throws Exception {
		// packet listener
		mAgent.getConnection().removePacketListener(this);
		
		// logging
		MobilisManager.getLogger().info("Mobilis Service (" + getIdent() + ") shut down.");
    }
    
    // getter + setter methods
    
    protected void setSettingString(String jid, String name, String value) {
    	synchronized(mUserSettings) {
    		if (!mUserSettings.containsKey(jid)) {
    			mUserSettings.put( jid, Collections.synchronizedMap(new HashMap<String, Object>()) );
    		}
    		Map<String, Object> settings = mUserSettings.get(jid);
   			synchronized(settings) {
   				settings.put( name, value );
   			}
    	}
    }

	protected void setSettingStrings(String jid, String name, Map<String,String> value) {
    	synchronized(mUserSettings) {
    		if (!mUserSettings.containsKey(jid)) {
    			mUserSettings.put( jid, Collections.synchronizedMap(new HashMap<String, Object>()) );
    		}
    		Map<String, Object> settings = mUserSettings.get(jid);
   			synchronized(settings) {
   				settings.put( name, value );
   			}
    	}
    }
    
    protected String getSettingString(String name) {
    	Object value = null;
		synchronized(mDefaultSettings) {
    		if (mDefaultSettings.containsKey(name)) {
    			value = mDefaultSettings.get(name);
    		}
		}
    	return value instanceof String ? (String)value : null;
    }
    
    @SuppressWarnings("unchecked")
	protected Map<String,String> getSettingStrings(String name) {
    	Object value = null;
		synchronized(mDefaultSettings) {
    		if (mDefaultSettings.containsKey(name)) {
    			value = mDefaultSettings.get(name);
    		}
		}
    	return value instanceof Map<?,?> ? (Map<String,String>)value : null;
    }
    
    protected String getSettingString(String jid, String name) {
    	synchronized(mUserSettings) {
    		if (mUserSettings.containsKey(name)) {
    			Object value = null;
    			Map<String, Object> settings = mUserSettings.get(name);
    			synchronized(settings) {
    				if (settings.containsKey(jid)) {
        				value = settings.get(jid);
    				}
    			}
    	    	return (value instanceof String ? (String)value : null);
    	    } else {
        		return getSettingString(name);
        	}
    	}
    }
    
    @SuppressWarnings("unchecked")
	protected Map<String,String> getSettingStrings(String jid, String name) {
    	synchronized(mUserSettings) {
    		if (mUserSettings.containsKey(name)) {
    			Object value = null;
    			Map<String, Object> settings = mUserSettings.get(name);
    			synchronized(settings) {
    				if (settings.containsKey(jid)) {
        				value = settings.get(jid);
    				}
    			}
    	    	return (value instanceof Map<?,?> ? (Map<String,String>)value : null);
    	    } else {
        		return getSettingStrings(name);
        	}
    	}
    }
    
    public DiscoverItems.Item getDiscoverItem() {
    	Item item = new DiscoverItems.Item(mAgent.getConnection().getUser());
		item.setName(getName());
		item.setNode(getNode());
		return item;
    }

	public String getNode() {
		return MobilisManager.discoServicesNode + "/" + getIdent();
	}
	
	public String getName() {
		String result = null;
		if (getSettingString("description") != null) {
			result = getSettingString("description");
		} else if (getSettingString("name") != null) {
			result = getSettingString("name");
		} else {
			result = "Mobilis Service";
		}
		return result;
	}
	
    public MobilisAgent getAgent() {
    	return mAgent;
    }
    
	public String getIdent() {
		return getClass().getSimpleName();
	}
	
	public String getNamespace() {
		return Mobilis.NAMESPACE + "#services/" + getIdent();
	}
	
	/**
	 * Gets the version of the MobilisService. Each MobilisService SHOULD override this method
	 * because otherwise simply "1.0" is returned.
	 * @return the version of the MobilisService if the version is set, otherwise: "1.0"
	 */
	public String getVersion() {
		return "1.0";
	}
    
    // XMPP related functions
    
    @Override
    public List<DiscoverInfo.Identity> getNodeIdentities() {
		List<DiscoverInfo.Identity> identities = new ArrayList<DiscoverInfo.Identity>();
		identities.add(new DiscoverInfo.Identity("component", getName()));
		if (getNodeItems().size() > 0) {
			identities.add(new DiscoverInfo.Identity("hierarchy", "branch"));
		} else {
			identities.add(new DiscoverInfo.Identity("hierarchy", "leaf"));
		}
		return identities;
	}
	
	@Override
	public List<String> getNodeFeatures() {
		List<String> features = new ArrayList<String>();
		features.add(MobilisManager.discoNamespace);
		return features;
	}

	@Override
    public List<DiscoverItems.Item> getNodeItems() {
        List<DiscoverItems.Item> items = new ArrayList<DiscoverItems.Item>();
        return items;
    }
    
    protected abstract void registerPacketListener();
    
    @Override
    public void processPacket(Packet p) {
        if (p instanceof SettingsIQ) {
        	SettingsIQ iq = (SettingsIQ) p;
            String requester = iq.getFrom();
            
            if (iq.getType().equals(Type.SET)) {
            	setSettingString(requester, iq.getName(), iq.getValue());
            	//TODO: send empty result IQ
            } else if (iq.getType().equals(Type.GET)) {
            	String prop = getSettingString(requester, iq.getName());
            	iq.setValue(prop);
            	
            	// send result packet
            	iq.setType(Type.RESULT);
            	iq.setFrom(mAgent.getConnection().getUser());
            	iq.setTo(requester);
            	
            	mAgent.getConnection().sendPacket(iq);
            }
        }
	}
}
