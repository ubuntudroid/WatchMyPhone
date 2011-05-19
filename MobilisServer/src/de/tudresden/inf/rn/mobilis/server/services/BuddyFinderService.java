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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.packet.Packet;

import de.tudresden.inf.rn.mobilis.server.MobilisManager;
import de.tudresden.inf.rn.mobilis.xmpp.beans.LocationInfo;
import de.tudresden.inf.rn.mobilis.xmpp.packet.LocationIQ;

/**
 * The purpose of BuddyFinderService is to handle location updates, 
 * to check for and to handle proximity alerts. Also to keep track of
 * those who want to receive those updates and those to skip. 
 *
 * @author Christopher, Lukas
 */
public class BuddyFinderService extends MobilisService implements ContextAwareness {

	private static final int DEFAULT_RADIUS = 500;
	// data per JID
	private final Map<String, Collection<String>> mAlertDelivered = Collections.synchronizedMap(new HashMap<String, Collection<String>>());

	public BuddyFinderService() {
		super();

		try {
			((ContextService) MobilisManager.getInstance().getService("ContextService")).registerContextAwareService(this);
		} catch (Exception e) {
			MobilisManager.getLogger().severe("BuddyFinderService couldn't register as context aware service with ContextService.");
		}
	}

	// XMPP related functions

	@Override
	public List<String> getNodeFeatures() {
		List<String> features = super.getNodeFeatures();
		features.add(MobilisManager.discoNamespace + "#statusupdate");
		features.add(MobilisManager.discoNamespace + "#proximityradius");
		return features;
	}

	@Override
	protected void registerPacketListener() {
		//mAgent.getConnection().addPacketListener(this, null);
	}

	@Override
	public void processPacket(Packet p) {
		super.processPacket(p);
	}

	private void deliverLocationUpdate(String jid, String buddy, Boolean proximity, Boolean alert) {
		if (isServiceEnabled(jid) && isServiceEnabled(buddy)) {
			LocationIQ iq = new LocationIQ();

			iq.setAlert(alert);
			iq.setIdentity(buddy);
			iq.setProximity(proximity);
			iq.setLocation(getLocation(buddy));

			MobilisManager.getLogger().finest("BFS deliverLocationUpdate: " + iq.toString());
			
			// send result packet
			iq.setFrom(mAgent.getConnection().getUser());
			iq.setTo(jid);

			mAgent.getConnection().sendPacket(iq);
		} else {
			MobilisManager.getLogger().finest("BFS DID NOT deliverLocationUpdate, because: (" + jid + " : " + isServiceEnabled(jid) + ") and (" + buddy + " : " + isServiceEnabled(buddy) + ")");
		}
	}

	// Service access adapters

	public void setLocation(String jid, LocationInfo location) {
		proximityCheck(jid);
	}

	private LocationInfo getLocation(String jid) {
		return ((ContextService) MobilisManager.getInstance().getService("ContextService")).getLocation(jid);
	}

	private boolean containsLocation(String jid) {
		return ((ContextService) MobilisManager.getInstance().getService("ContextService")).containsLocation(jid);
	}

	private Collection<String> getBuddies(String jid) {
		return ((SocialNetworkIntegrationService) MobilisManager.getInstance().getService("SocialNetworkIntegrationService")).getBuddies(jid);
	}
	
	private Collection<String> getTrackableBuddies(String jid) {
		return ((SocialNetworkIntegrationService) MobilisManager.getInstance().getService("SocialNetworkIntegrationService")).getTrackableBuddies(jid);
	}

	// BuddyFinder getters/setters 

	private boolean isServiceEnabled(String jid) {
		return Boolean.parseBoolean(getSettingString(jid, "Status"));
	}

	private void rememberProximityAlert(String jid, String buddy) {
		synchronized(mAlertDelivered) {
			// initialize per-jid-HashMap
			if (! mAlertDelivered.containsKey(jid)) {
				mAlertDelivered.put(jid, new HashSet<String>());
			}
			mAlertDelivered.get(jid).add(buddy);
		}
	}

	private boolean containsProximityAlert(String jid, String buddy) {
		Boolean result = false;
		synchronized(mAlertDelivered) {
			if (mAlertDelivered.containsKey(jid)) {
				result = mAlertDelivered.get(jid).contains(buddy);
			}
		}
		return result;
	}

	private void forgetProximityAlert(String jid, String buddy) {
		synchronized(mAlertDelivered) {
			if (mAlertDelivered.containsKey(jid)) {
				mAlertDelivered.get(jid).remove(buddy);
			}
		}
	}

	// BuddyFinder functionality

	private void proximityCheck(String jid) {
		Collection<String> buddies = getBuddies(jid);

		Iterator<String> li = buddies.iterator();
		while (li.hasNext()) {
			String buddy = li.next();
			if (containsLocation(buddy)) {
				// check if jid is close to buddy, remember and notify
				MobilisManager.getLogger().finer("BFS proximityCheck between: " + jid + " and " + buddy);
				proximityCheck(jid, buddy);

				// check if buddy is close to jid, remember and notify
				MobilisManager.getLogger().finer("BFS proximityCheck between: " + buddy + " and " + jid);
				proximityCheck(buddy, jid);
			}
		}

		//// test case by Benjamin: always send location update
		//deliverLocationUpdate(jid, jid, true, true);
	}
	
	// checks on proximity hit and sets alert on LocationUpdate if first hit
	private void proximityCheck(String jid, String buddy) {
		int radius = 0;
		String radiusString = getSettingString(jid, "Radius");
		if (radiusString == null) {
			radius = BuddyFinderService.DEFAULT_RADIUS;
		} else {
			radius = Integer.parseInt(radiusString);
		}
		if (closeTo(getLocation(jid), getLocation(buddy), radius)) {
			if (! containsProximityAlert(jid, buddy)) {
				deliverLocationUpdate(jid, buddy, true, true);
				rememberProximityAlert(jid, buddy);
			} else {
				deliverLocationUpdate(jid, buddy, true, false);
			}
		} else {
			if (containsProximityAlert(jid, buddy)) {
				deliverLocationUpdate(jid, buddy, false, true);
				forgetProximityAlert(jid, buddy);
			} else {
				if (getTrackableBuddies(jid).contains(buddy)) {
					deliverLocationUpdate(jid, buddy, false, false);
				}
			}
		}
	}

	// computes the distance between two locations and compares with radius
	public boolean closeTo(LocationInfo loc1, LocationInfo loc2, Integer radius) {
		double lat1InRad =loc1.getLatitude()*((2.0 * Math.PI)/360.0);
		double lon1InRad =loc1.getLongitude()*((2.0 * Math.PI)/360.0);
		double lat2InRad =loc2.getLatitude()*((2.0 * Math.PI)/360.0);
		double lon2InRad =loc2.getLongitude()*((2.0 * Math.PI)/360.0);

		double tmp = Math.sin(lat1InRad) * Math.sin(lat2InRad) + Math.cos(lat1InRad)
		* Math.cos(lat2InRad)
		* Math.cos(lon2InRad - lon1InRad);

		tmp = Math.acos(tmp);
		MobilisManager.getLogger().fine("distance by closeto(): " + tmp * 6378388);
		return ((tmp * 6378388)<radius);

	} 
}
