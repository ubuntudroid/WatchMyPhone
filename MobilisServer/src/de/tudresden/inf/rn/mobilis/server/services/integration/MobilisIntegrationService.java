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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smackx.NodeInformationProvider;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.jivesoftware.smackx.packet.DiscoverItems.Item;

import de.tudresden.inf.rn.mobilis.server.MobilisManager;
import de.tudresden.inf.rn.mobilis.server.services.SocialNetworkIntegrationService;
import de.tudresden.inf.rn.mobilis.xmpp.filter.NetworkFilter;
import de.tudresden.inf.rn.mobilis.xmpp.packet.NetworkIQ;

/**
*
* @author Christopher
*/
public abstract class MobilisIntegrationService implements PacketListener, NodeInformationProvider {
	
	private final String mIdentifier;
	
	protected SocialNetworkIntegrationService mSNIS;
	
	protected Map<String, Object> mDefaultSettings;
	
	// map Social Network identity to JabberID
	private Map<String, String> mIdentities;
	
	// Social Network Buddylist
	private Map<String, Collection<String>> mBuddies;
	
	// Social Network Groups
	private Map<String, Collection<String>> mGroups;
	
	// Social Network Profile Information
	private Map<String, Map<String, String>> mProfiles;
	
	public MobilisIntegrationService(String ident) {
		mIdentifier = ident;
		
		mSNIS = (SocialNetworkIntegrationService) MobilisManager.getInstance().getService("SocialNetworkIntegrationService");
		
		try {
			mDefaultSettings = MobilisManager.getInstance().getSettings("networks", ident);
		} catch (Exception e) {
			mDefaultSettings = Collections.synchronizedMap(new HashMap<String, Object>());
		}
		
		mIdentities = Collections.synchronizedMap(new HashMap<String, String>());
		mBuddies = Collections.synchronizedMap(new HashMap<String, Collection<String>>());
		
		mGroups = Collections.synchronizedMap(new HashMap<String, Collection<String>>());
		mProfiles = Collections.synchronizedMap(new HashMap<String, Map<String, String>>());
	}

    public void startup() throws Exception {
		if (mSNIS == null) {
			try {
				mSNIS = (SocialNetworkIntegrationService) MobilisManager.getInstance().getService("SocialNetworkIntegrationService");
			} catch (Exception e) {
				throw e;
			}
		}
		
		try {
			startup(mSNIS);
		} catch (Exception e) {
			throw e;
		}
	}

    public void startup(SocialNetworkIntegrationService snis) throws Exception {
		try {
			mSNIS = snis;
			
			// packet listener
			mSNIS.getAgent().getConnection().addPacketListener(this, new AndFilter(
			   	new NetworkFilter(getIdent()),
				new PacketTypeFilter(NetworkIQ.class),
				new OrFilter(
					new IQTypeFilter(Type.GET),
			   		new IQTypeFilter(Type.SET)
			   	)
			));
			registerPacketListener();
			
			// logging
			MobilisManager.getLogger().info("Mobilis Integration Service (" + getIdent() + ") started up.");
		} catch (Exception e) {
			throw e;
		}
    }
    
    public void shutdown() throws Exception {
		// packet listener
    	mSNIS.getAgent().getConnection().removePacketListener(this);
		
		// logging
		MobilisManager.getLogger().info("Mobilis Integration Service (" + getIdent() + ") shut down.");
    }
    
    protected String getSettingString(String name) {
    	Object value = null;
		synchronized(mDefaultSettings) {
    		if (mDefaultSettings.containsKey(name)) {
    			value = mDefaultSettings.get(name);
    		}
		}
    	return (value instanceof String ? (String)value : null);
    }
    
    @SuppressWarnings("unchecked")
	protected Map<String,String> getSettingStrings(String name) {
    	Object value = null;
		synchronized(mDefaultSettings) {
    		if (mDefaultSettings.containsKey(name)) {
    			value = mDefaultSettings.get(name);
    		}
		}
    	return (value instanceof Map<?,?> ? (Map<String,String>)value : null);
    }
    
    public boolean isTrackable() {
    	if (Boolean.parseBoolean(getSettingString("trackable"))) {
    		return true;
    	} else {
    		return false;
    	}
    }
	
	public void setIdentity(String jid, String ident) {
		synchronized(mIdentities) {
			mIdentities.put(jid, ident);
		}
	}

	public String getIdentity(String jid) {
		synchronized(mIdentities) {
			return mIdentities.get(jid);
		}
	}

	//performs the remapping from identitys to jids
	public String getJid(String identity) {
		String result = null;
		synchronized(mIdentities) {
			if (mIdentities.containsValue(identity)) {
				for (String ident : mIdentities.keySet()) {
					if (identity.equals(mIdentities.get(ident))) {
						result = ident;
					}
				}
			}
		}
//		MobilisManager.getLogger().fine("identity: " + identity );
		//MobilisManager.getLogger().fine("mIdentities: " + mIdentities );
		if (result != null) 
		{
		MobilisManager.getLogger().fine("Mapped: " + identity + " = " + result);
		}
		return result;
	}

	public void setBuddies(String ident, Collection<String> buddies) {
		synchronized(mBuddies) {
			mBuddies.put(ident, buddies);
		}
	}
	
	public void setGroups(String ident, Collection<String> groups) {
		synchronized(mGroups) {
			mGroups.put(ident, groups);
		}
	}
	
	// get a list of JabberIDs
	public Collection<String> getBuddies(String jid) {
		Collection<String> buddies = Collections.synchronizedSet(new HashSet<String>());
		
		String identity = getIdentity(jid);
		synchronized(mBuddies) {
			try {
				Collection<String> buddy = mBuddies.get(identity);
//				MobilisManager.getLogger().fine("Found Buddies: " + buddy);
				if (buddy != null) {
					for (String bud : buddy) {
						String jjid = getJid(bud);
						if (jjid != null) {
							buddies.add(jjid);
						}
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
			}
		}
		return buddies;
	}
    
    public String getIdent() {
    	return mIdentifier;
    }

	public String getName() {
		String result = null;
		if (getSettingString("description") != null) {
			result = getSettingString("description");
		} else if (getSettingString("name") != null) {
			result = getSettingString("name");
		} else {
			result = "Mobilis Integration Service";
		}
		return result;
	}
	
	public String getNode() {
		return MobilisManager.discoServicesNode + "/" + mSNIS.getIdent() + "#networks/" + getIdent();
	}
	
    public DiscoverItems.Item getDiscoverItem() {
    	Item item = new DiscoverItems.Item(mSNIS.getAgent().getConnection().getUser());
		item.setName(getName());
		item.setNode(getNode());
		return item;
    }
    
    // XMPP related functions
    
    protected abstract void registerPacketListener();
    
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
}
