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
package de.tudresden.inf.rn.mobilis.mapdraw;

import jabberSrpc.JabberClient;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.provider.BytestreamsProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.IBBProviders;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.search.UserSearch;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import de.tudresden.inf.rn.mobilis.android.services.SessionService;
import de.tudresden.inf.rn.mobilis.xmpp.packet.BuddylistIQ;
import de.tudresden.inf.rn.mobilis.xmpp.packet.LocationIQ;
import de.tudresden.inf.rn.mobilis.xmpp.packet.MonitoringIQ;
import de.tudresden.inf.rn.mobilis.xmpp.packet.NetworkIQ;
import de.tudresden.inf.rn.mobilis.xmpp.packet.SettingsIQ;
import de.tudresden.inf.rn.mobilis.xmpp.provider.BuddylistIQProvider;
import de.tudresden.inf.rn.mobilis.xmpp.provider.LocationIQProvider;
import de.tudresden.inf.rn.mobilis.xmpp.provider.MonitoringIQProvider;
import de.tudresden.inf.rn.mobilis.xmpp.provider.NetworkIQProvider;
import de.tudresden.inf.rn.mobilis.xmpp.provider.SettingsIQProvider;

/**
 * Singleton class to easily access the XMPP service.
 * @author Dirk Hering, Istvan Koren
 */
public class XMPPController {

	private static XMPPController instance;
	private XMPPConnection connection;
	private String mobilisGuideBroker;
	private String mobilisCollabEditingBroker;
	private Context context;
	
	private XMPPController() {
		context = SessionService.getInstance().getContext();
		SharedPreferences preferences = SessionService.getInstance().getPreferences();
		// TODO read out brokers
		ProviderManager pm = ProviderManager.getInstance();
		configureProviderManager(pm);
	};
	
	public static XMPPController getInstance() {
		if (instance == null) {
			instance = new XMPPController();
		}
		return instance;
	}
	
//	public void loginToXMPP() {
//		SessionService.getInstance().getCollabEditingService().connect();
//		
//		//Moved into CollabEditingService
//		XMPPConnection xmpp = JabberClient.getInstance().getJabberConnection();
//		xmpp.addPacketListener(Monitoring.get().getMonitoringIQHandler(), new AndFilter(
//						new IQTypeFilter(IQ.Type.GET),
//						new PacketTypeFilter(MonitoringIQ.class)
//					));
//	}
	
	public void registerPacketListener() {
		XMPPConnection xmpp = JabberClient.getInstance().getJabberConnection();
		xmpp.addPacketListener(Monitoring.get().getMonitoringIQHandler(), new AndFilter(
						new IQTypeFilter(IQ.Type.GET),
						new PacketTypeFilter(MonitoringIQ.class)
					));
	}
	
//	public Bundle readXMPPPreferences() {
//		
//		// prepare preferences for xmpp (can be changed in the running app from the preferences menu entry)
//		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
//			
//		// read server preferences
//		String host = preferences.getString("pref_xmpp_host", null);
//		int port = Integer.parseInt(preferences.getString(
//				"pref_xmpp_port", "5222"));
//		String serviceName = preferences.getString(
//				"pref_xmpp_service", null);
//		// read user credentials
//		String userName = preferences.getString(
//				"pref_xmpp_user", null);
//		String password = preferences.getString(
//				"pref_xmpp_password", null);
//		String resource = preferences.getString(
//				"pref_xmpp_resource", null);
//		
//		if ((host == null) || (serviceName == null) || (userName == null) || (password == null)) {
//			return null;
//		}
//
//		Bundle connectionParams = new Bundle();
//		connectionParams.putString("xmpp_host", host);
//		connectionParams.putInt("xmpp_port", port);
//		connectionParams.putString("xmpp_service", serviceName);
//		connectionParams.putString("xmpp_user", userName);
//		connectionParams.putString("xmpp_password", password);
//		connectionParams.putString("xmpp_resource", resource);
//
//		return connectionParams;
//	}
	
	/**
	 * WORKAROUND for Android only! The necessary configuration files for Smack
	 * library are not included in Android's apk-Package.
	 * 
	 * @param pm
	 *            A ProviderManager instance.
	 */
	private void configureProviderManager(ProviderManager pm) {

		// Private Data Storage
		pm.addIQProvider("query", "jabber:iq:private",
				new PrivateDataManager.PrivateDataIQProvider());

		// Time
		try {
			pm.addIQProvider("query", "jabber:iq:time", Class
					.forName("org.jivesoftware.smackx.packet.Time"));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		// Roster Exchange
		pm.addExtensionProvider("x", "jabber:x:roster",
				new RosterExchangeProvider());

		// Message Events
		pm.addExtensionProvider("x", "jabber:x:event",
				new MessageEventProvider());

		// Chat State
		pm.addExtensionProvider("active",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("composing",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("paused",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("inactive",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("gone",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		// XHTML
		pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",
				new XHTMLExtensionProvider());

		// Group Chat Invitations
		pm.addExtensionProvider("x", "jabber:x:conference",
				new GroupChatInvitation.Provider());

		// Service Discovery # Items
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
				new DiscoverItemsProvider());

		// Service Discovery # Info
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
				new DiscoverInfoProvider());

		// Data Forms
		pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());

		// MUC User
		pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user",
				new MUCUserProvider());

		// MUC Admin
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin",
				new MUCAdminProvider());

		// MUC Owner
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner",
				new MUCOwnerProvider());

		// Delayed Delivery
		pm.addExtensionProvider("x", "jabber:x:delay",
				new DelayInformationProvider());

		// Version
		try {
			pm.addIQProvider("query", "jabber:iq:version", Class
					.forName("org.jivesoftware.smackx.packet.Version"));
		} catch (ClassNotFoundException e) {
			// Not sure what's happening here.
		}

		// VCard
		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());

		// Offline Message Requests
		pm.addIQProvider("offline", "http://jabber.org/protocol/offline",
				new OfflineMessageRequest.Provider());

		// Offline Message Indicator
		pm.addExtensionProvider("offline",
				"http://jabber.org/protocol/offline",
				new OfflineMessageInfo.Provider());

		// Last Activity
		pm
				.addIQProvider("query", "jabber:iq:last",
						new LastActivity.Provider());

		// User Search
		pm
				.addIQProvider("query", "jabber:iq:search",
						new UserSearch.Provider());

		// SharedGroupsInfo
		pm.addIQProvider("sharedgroup",
				"http://www.jivesoftware.org/protocol/sharedgroup",
				new SharedGroupsInfo.Provider());

		// JEP-33: Extended Stanza Addressing
		pm.addExtensionProvider("addresses",
				"http://jabber.org/protocol/address",
				new MultipleAddressesProvider());

		// FileTransfer
		pm.addIQProvider("si", "http://jabber.org/protocol/si",
				new StreamInitiationProvider());
		pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams",
				new BytestreamsProvider());
		pm.addIQProvider("open", "http://jabber.org/protocol/ibb",
				new IBBProviders.Open());
		pm.addIQProvider("close", "http://jabber.org/protocol/ibb",
				new IBBProviders.Close());
		pm.addExtensionProvider("data", "http://jabber.org/protocol/ibb",
				new IBBProviders.Data());

		// Privacy
		pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
		
		// Mobilis IQs
		pm.addIQProvider(LocationIQ.elementName, LocationIQ.namespace, new LocationIQProvider());
		pm.addIQProvider(NetworkIQ.elementName, NetworkIQ.namespace, new NetworkIQProvider());
		pm.addIQProvider(SettingsIQ.elementName, SettingsIQ.namespace, new SettingsIQProvider());
		pm.addIQProvider(BuddylistIQ.elementName, BuddylistIQ.namespace, new BuddylistIQProvider());
		pm.addIQProvider(MonitoringIQ.elementName, MonitoringIQ.namespace, new MonitoringIQProvider());
	}

	public String getMobilisGuideBroker() {
		return mobilisGuideBroker;
	}
}
