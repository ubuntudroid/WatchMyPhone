/*******************************************************************************
 * Copyright (C) 2011 Technische Universität Dresden
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
package de.tudresden.inf.rn.mobilis.android.services;

import java.util.ArrayList;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.packet.MUCUser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import de.tudresden.inf.rn.mobilis.android.util.Const;
import de.tudresden.inf.rn.mobilis.xmpp.packet.GeolocPacketExtension;

public class GroupMemberService implements PacketListener {

	private Roster mRoster;
	private ArrayList<GroupMember> mEntries;
	private BroadcastReceiver ir;

	public void initIntentReceiver() {
	    ir = new IntentReceiver();
	    //Context context = SessionService.getInstance().getContext();
	    //context.registerReceiver(ir, new IntentFilter();
	}

	public void unregisterIntentReceiver() {
	    //SessionService.getInstance().getContext().unregisterReceiver(ir);
	}

	private class IntentReceiver extends BroadcastReceiver {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        // TODO Auto-generated method stub

	    }
	}

	public GroupMemberService() {
		mEntries = new ArrayList<GroupMember>();
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<GroupMember> getGroupMembers(String group) {
		ArrayList<GroupMember> groupMembers = new ArrayList<GroupMember>();
		for (GroupMember gm : mEntries) {
			if (gm.isMemberOf(group))
				groupMembers.add(gm);
		}
		return (ArrayList) java.util.Collections.unmodifiableList(groupMembers);
	}

	private void addGroupMember(GroupMember gm) {
		if (!mEntries.contains(gm))
			mEntries.add(gm);
		else
			mEntries.set(mEntries.indexOf(gm), gm);
	}

	/**
	 * Adds the user with the supplied jid both to the roster (if not yet known)
	 * and to the internal roster representation.
	 * 
	 * @param group
	 * @param jid
	 */
	public void addGroupMember(String group, String jid) {
		try {
			// check if already on roster, if yes just add to group.
			RosterEntry re = mRoster
					.getEntry(StringUtils.parseBareAddress(jid));
			if (re != null) {
				// add group if not yet added
				RosterGroup rg = mRoster.createGroup(group);
				rg.addEntry(re);
			} else {
				// add to roster
				mRoster.createEntry(StringUtils.parseBareAddress(jid),
						StringUtils.parseName(jid),
						new String[] { SessionService.getInstance()
								.getGroupManagementService().getGroupName() });
			}

			GroupMember gm = new GroupMember(jid);
			gm.addMembership(group);
			addGroupMember(gm);
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isMember(String jid) {
		return mEntries.contains(new GroupMember(jid));
	}

	public void updateGroupMember(String jid, Presence.Mode mode) {
		if (isMember(jid)) {
			mEntries.get(mEntries.indexOf(new GroupMember(jid)))
					.setPresenceMode(mode.name());
		}
	}

	public void updateGroupMember(String jid, Location loc) {
		handleLocationCallback(jid, loc);
	}

	/**
	 * Sends a location changed intent to the upper layer, if the supplied jid
	 * belongs to this group.
	 * 
	 * @param jid
	 *            The full jabber id of the user concerned.
	 * @param geoloc
	 *            A Location object with the gained location information.
	 */
	private void handleLocationCallback(String jid, Location geoloc) {
		// if (isMember(jid)) {
		Intent i = new Intent(
				Const.INTENT_PREFIX + "location");
		i.putExtra(Const.INTENT_PREFIX + "location.jid", jid);
		i.putExtra(Const.INTENT_PREFIX + "location.location",
				geoloc);
		SessionService.getInstance().getContext().sendBroadcast(i);
		// }
	}

	/**
	 * Handle XEP-0080 Packet Extension stanzas.
	 */
	@Override
	public void processPacket(Packet packet) {
		if (packet instanceof Presence) {
			Presence p = (Presence) packet;
			if (packet.getExtension("http://jabber.org/protocol/muc#user") != null) {
				MUCUser chatParticipant = (MUCUser) packet
						.getExtension("http://jabber.org/protocol/muc#user");
				String participant = chatParticipant.getItem().getJid();
				String group = StringUtils.parseName(packet.getFrom());
				if (!isMember(participant)) {
					// participant not known yet
					addGroupMember(group, participant);
				} else {
					// participant's mode updated
					updateGroupMember(participant, p.getMode());
				}
			} else {
				// normal roster entry
				// TODO manage this presence also as the user may be added
				// later.
			}
		} else if (packet.getExtension(GeolocPacketExtension.elementName,
				GeolocPacketExtension.namespace) != null) {
			GeolocPacketExtension gpe = (GeolocPacketExtension) packet
					.getExtension(GeolocPacketExtension.elementName,
							GeolocPacketExtension.namespace);
			Location loc = new Location("gps");
			loc.setAltitude(gpe.getAlt());
			loc.setLatitude(gpe.getLat());
			loc.setLongitude(gpe.getLon());
			updateGroupMember(packet.getFrom(), loc);
		}
		// NotificationManager nm = (NotificationManager) SessionService
		// .getInstance().getContext().getSystemService("notification");
		// Notification n = new Notification();
		// n.icon = R.drawable.marker_red;
		// n.tickerText = "tourist added";
		// nm.notify(0, n);
	}

	public void initialize(XMPPConnection conn) {
		ProviderManager pm = ProviderManager.getInstance();
		// FIXME change to a less generalized extension provider
		// GeolocPacketExtensionProvider geoProvider = new
		// GeolocPacketExtensionProvider();
		// pm.addExtensionProvider("event",
		// "http://jabber.org/protocol/pubsub#event", geoProvider);

		// provide custom listener
		// FIXME change to a less generalized filter
		// PacketFilter filter = new AndFilter(new
		// PacketExtensionFilter("event",
		// "http://jabber.org/protocol/pubsub#event"), new NotFilter(new
		// org.jivesoftware.smack.filter.FromContainsFilter("pubsub.")));
		// PacketTypeFilter ptf = new PacketTypeFilter(Presence.class);
		// mRoster = conn.getRoster();
		// conn.addPacketListener(this, new OrFilter(filter, ptf));
	}
}
