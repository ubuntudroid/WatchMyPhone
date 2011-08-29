/**
 * Copyright (C) 2009 Technische Universit�t Dresden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: http://mobilisplatform.sourceforge.net
 */

package de.tudresden.inf.rn.mobilis.mxa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import de.tudresden.inf.rn.mobilis.mxa.ConstMXA.MessageItems;
import de.tudresden.inf.rn.mobilis.mxa.ConstMXA.RosterItems;
import de.tudresden.inf.rn.mobilis.mxa.callbacks.IConnectionCallback;
import de.tudresden.inf.rn.mobilis.mxa.callbacks.IXMPPIQCallback;
import de.tudresden.inf.rn.mobilis.mxa.callbacks.IXMPPMessageCallback;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPIQ;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPMessage;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPPresence;
import de.tudresden.inf.rn.mobilis.mxa.services.collabedit.CollabEditingService;
import de.tudresden.inf.rn.mobilis.mxa.services.collabedit.ICollabEditingService;
import de.tudresden.inf.rn.mobilis.mxa.services.filetransfer.FileTransferService;
import de.tudresden.inf.rn.mobilis.mxa.services.filetransfer.IFileTransferService;
import de.tudresden.inf.rn.mobilis.mxa.services.multiuserchat.IMultiUserChatService;
import de.tudresden.inf.rn.mobilis.mxa.services.multiuserchat.MultiUserChatService;
import de.tudresden.inf.rn.mobilis.mxa.services.pubsub.IPubSubService;
import de.tudresden.inf.rn.mobilis.mxa.services.pubsub.PubSubService;
import de.tudresden.inf.rn.mobilis.mxa.services.servicediscovery.IServiceDiscoveryService;
import de.tudresden.inf.rn.mobilis.mxa.services.servicediscovery.ServiceDiscoveryService;
import de.tudresden.inf.rn.mobilis.mxa.util.FilteredCallbackList;
import de.tudresden.inf.rn.mobilis.mxa.xmpp.IQImpl;
import de.tudresden.inf.rn.mobilis.mxa.xmpp.IQImplFilter;
import de.tudresden.inf.rn.mobilis.mxa.xmpp.IQImplProvider;
import de.tudresden.inf.rn.mobilis.mxa.xmpp.MXAIdentExtension;
import de.tudresden.inf.rn.mobilis.mxa.xmpp.MXAIdentExtensionProvider;
import de.tudresden.inf.rn.mobilis.mxa.xmpp.MXAIdentFilter;

/**
 * @author Istvan Koren
 */
public class XMPPRemoteService extends Service {

	private static final String TAG = "XMPPRemoteService";

	private static final int XMPPSERVICE_STATUS = 1;

	private SharedPreferences mPreferences;
	private XMPPConnection mConn;
	private WriterThread xmppWriteWorker;
	private ReaderThread xmppReadWorker;
	ExecutorService mWriteExecutor;

	// Additional MXA Services
	FileTransferService mFileTransferService;	
	MultiUserChatService mMultiUserChatService;
	PubSubService mPubSubService;
	ServiceDiscoveryService mServiceDiscoveryService;
	CollabEditingService mCollabEditingService;
	
	private XMPPRemoteService instance;

	/*
	 * Remote callback list for message listeners.
	 */
	final FilteredCallbackList<IXMPPMessageCallback> mMsgCallbacks = new FilteredCallbackList<IXMPPMessageCallback>();
	/*
	 * Remote callback list for connection listeners.
	 */
	final RemoteCallbackList<IConnectionCallback> mConnectionCallbacks = new RemoteCallbackList<IConnectionCallback>();

	// Remote callback list for iq listeners.
	final FilteredCallbackList<IXMPPIQCallback> mIQCallbacks = new FilteredCallbackList<IXMPPIQCallback>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// Workaround for ServiceDiscoveryManager: this class has a static
		// initializer which is not called in the right moment. That is why
		// we repeat its code here.
		XMPPConnection
				.addConnectionCreationListener(new ConnectionCreationListener() {
					@Override
					public void connectionCreated(XMPPConnection connection) {
						new ServiceDiscoveryManager(connection);
					}
				});

		// initialize and start worker threads
		xmppWriteWorker = new WriterThread();
		xmppWriteWorker.start();
		xmppReadWorker = new ReaderThread();
		xmppReadWorker.start();

		// read in preferences
		mPreferences = getSharedPreferences(
				"de.tudresden.inf.rn.mobilis.mxa_preferences",
				Context.MODE_PRIVATE);

		// initialize IQ executor
		mWriteExecutor = Executors.newCachedThreadPool();

		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// Clear leftover notification in case this service previously got
		// killed while playing
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(XMPPSERVICE_STATUS);
		instance = this;
	}
	
	/**
	 * This method is just a quick fix to get hold of the current XMPPConnection object. It may
	 * be removed as soon as the WMP-CollaborationService has been properly integrated into Mobilis
	 * and into this service.
	 */
	public XMPPRemoteService getInstance() {
		return instance;
	}

	public XMPPConnection getXMPPConnection() {
		return mConn;
	}
	
	public void setXMPPConnection(XMPPConnection mConn) {
		this.mConn = mConn;
	}

	public ExecutorService getWriteExecutor() {
		return mWriteExecutor;
	}

	/**
	 * The WriterThread is responsible for sending XMPP stanzas to the server.
	 * 
	 * @author koren
	 * 
	 */
	private class WriterThread extends Thread {
		public Handler mHandler;

		public void run() {
			setName("MXA Writer Thread");
			Looper.prepare();

			mHandler = new Handler() {

				public void handleMessage(Message msg) {
					// initialize response Message, as Messages cannot be reused
					// get a Message from the Message pool and copy values of
					// msg
					Message msg2 = Message.obtain(msg);
					switch (msg.what) {
					case ConstMXA.MSG_CONNECT:
						// initialize XMPP Connection
						// check if already connected
						if (mConn != null && mConn.isConnected()) {
							msg2.arg1 = ConstMXA.MSG_STATUS_SUCCESS;
							xmppResults.sendMessage(msg2);
							break;
						}
						// read server preferences
						String host = mPreferences.getString("pref_host", null);
						int port = Integer.parseInt(mPreferences.getString(
								"pref_port", null));
						String serviceName = mPreferences.getString(
								"pref_service", null);
						ConnectionConfiguration config = new ConnectionConfiguration(
								host, port, serviceName);
						mConn = new XMPPConnection(config);
						// read user credentials
						String username = mPreferences.getString(
								"pref_xmpp_user", null);
						String password = mPreferences.getString(
								"pref_xmpp_password", null);
						String resource = mPreferences.getString(
								"pref_resource", null);

						// connect and login to XMPP server
						try {
							mConn.connect();

							ProviderManager pm = ProviderManager.getInstance();
							configureProviderManager(pm);

							// install MXAIdentExtension
							MXAIdentExtensionProvider.install(pm);

							// register PacketListener for all message and
							// presence stanzas
							mConn
									.addPacketListener(
											xmppReadWorker,
											new OrFilter(
													new OrFilter(
															new PacketTypeFilter(
																	org.jivesoftware.smack.packet.Message.class),
															new PacketTypeFilter(
																	Presence.class)),
													new PacketTypeFilter(
															IQ.class)));
							

							// register connection listener
							mConn.addConnectionListener(xmppReadWorker);

							mConn.login(username, password, resource);

							// TODO remove this 
							
							// delete all entries in the RosterProvider
							getContentResolver().delete(
									ConstMXA.RosterItems.CONTENT_URI, "1",
									new String[] {});
							// get Roster from server
							final Roster r = XMPPRemoteService.this.mConn
									.getRoster();
							r.addRosterListener(xmppReadWorker);
							Collection<RosterEntry> rosterEntries = r
									.getEntries();
							List<String> entries = new ArrayList<String>(
									rosterEntries.size());
							for (RosterEntry re : rosterEntries)
								entries.add(re.getUser());
							xmppReadWorker.entriesAdded(entries);

							// Notification
							NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

							Notification status = new Notification(
									R.drawable.stat_notify_chat,
									getString(R.string.sb_txt_text), System
											.currentTimeMillis());
							status
									.setLatestEventInfo(
											XMPPRemoteService.this,
											getString(R.string.sb_txt_title),
											getString(R.string.sb_txt_text),
											PendingIntent
													.getActivity(
															XMPPRemoteService.this,
															0,
															new Intent(
																	ConstMXA.INTENT_SERVICEMONITOR),
															0));
							status.flags |= Notification.FLAG_ONGOING_EVENT;
							status.icon = R.drawable.stat_notify_chat;
							nm.notify(XMPPSERVICE_STATUS, status);

							// initialize services
							mFileTransferService = new FileTransferService(
									XMPPRemoteService.this);
							mMultiUserChatService = new MultiUserChatService(
									XMPPRemoteService.this);
							mPubSubService = new PubSubService(
									XMPPRemoteService.this);
							mServiceDiscoveryService = new ServiceDiscoveryService(
									XMPPRemoteService.this);
							mCollabEditingService = new CollabEditingService(
									XMPPRemoteService.this);
							
							msg2.arg1 = ConstMXA.MSG_STATUS_SUCCESS;
						} catch (XMPPException e) {
							msg2.arg1 = ConstMXA.MSG_STATUS_ERROR;
						}

						// // get roster, some entries may already have been
						// // inserted due to concurrent receiving of Presence
						// // stanzas
						// Log.i(TAG, "reading roster");
						// Roster r = mConn.getRoster();
						// ContentValues valuesRoster;
						// for (RosterEntry re : r.getEntries()) {
						// valuesRoster = new ContentValues();
						// valuesRoster.put(RosterItems.XMPP_ID, re.getUser());
						// valuesRoster.put(RosterItems.NAME, StringUtils
						// .parseName(re.getUser()));
						// valuesRoster.put(RosterItems.PRESENCE_MODE,
						// "offline");
						//
						// getContentResolver().insert(
						// RosterItems.CONTENT_URI, valuesRoster);
						// }

						xmppResults.sendMessage(msg2);
						break;
					case ConstMXA.MSG_DISCONNECT:
						if (mConn == null || !mConn.isConnected()) {
							msg2.arg1 = ConstMXA.MSG_STATUS_SUCCESS;
							xmppResults.sendMessage(msg2);
							break;
						}
						// disconnect() deletes all listeners, use shutdown() to
						// retain
						// listeners
						// TODO only disconnect from XMPP server if all
						// service consumers disconnected before

						NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
						nm.cancel(XMPPSERVICE_STATUS);

						mConn.disconnect();
						msg2.arg1 = ConstMXA.MSG_STATUS_SUCCESS;
						xmppResults.sendMessage(msg2);
						break;
					case ConstMXA.MSG_SEND_IQ:
						Bundle data = msg.getData();
						XMPPIQ payloadIQ = data.getParcelable("PAYLOAD");
						IQImpl iq = new IQImpl(payloadIQ.payload);
						iq.setTo(payloadIQ.to);
						switch (payloadIQ.type) {
						case XMPPIQ.TYPE_GET:
							iq.setType(Type.GET);
							break;
						case XMPPIQ.TYPE_SET:
							iq.setType(Type.SET);
							break;
						case XMPPIQ.TYPE_RESULT:
							iq.setType(Type.RESULT);
							break;
						case XMPPIQ.TYPE_ERROR:
							iq.setType(Type.ERROR);
							break;
						default:
							iq.setType(Type.GET);
						}
						
						//Set the packet-ID of the iq which should be sent:
						iq.setPacketID(payloadIQ.packetID);
						
						// set token
						if ((payloadIQ.namespace != null)
								|| (payloadIQ.token != null)) {
							MXAIdentExtension mie = new MXAIdentExtension(
									payloadIQ.namespace, payloadIQ.token);
							iq.addExtension(mie);
						}

						mConn.sendPacket(iq);

						break;
					case ConstMXA.MSG_SEND_PRESENCE:
						Bundle dataPresence = msg.getData();
						XMPPPresence payloadPresence = dataPresence
								.getParcelable("PAYLOAD");
						Presence presence = new Presence(
								Presence.Type.available);
						presence.setStatus(payloadPresence.status);
						presence.setPriority(payloadPresence.priority);
						switch (payloadPresence.mode) {
						case XMPPPresence.MODE_AVAILABLE:
							presence.setMode(Mode.available);
							break;
						case XMPPPresence.MODE_AWAY:
							presence.setMode(Mode.away);
							break;
						case XMPPPresence.MODE_CHAT:
							presence.setMode(Mode.chat);
							break;
						case XMPPPresence.MODE_DND:
							presence.setMode(Mode.dnd);
							break;
						case XMPPPresence.MODE_XA:
							presence.setMode(Mode.xa);
							break;
						default:
							presence.setMode(Mode.available);
						}

						// send Presence over XMPP
						mConn.sendPacket(presence);

						// send result ack
						msg2.arg1 = ConstMXA.MSG_STATUS_DELIVERED;
						xmppResults.sendMessage(msg2);

						// broadcast new presence
						Intent i = new Intent(ConstMXA.BROADCAST_PRESENCE);
						i.putExtra("STATUS_TEXT", payloadPresence.status);
						sendBroadcast(i);

						break;
					}

				}
			};

			Looper.loop();
		}
	}

	/**
	 * An IQ runner thread sends a GET or SET IQ Message to the XMPP server,
	 * constructs a PacketListener for the result with the specific packet ID
	 * and notifies a handler. This happens for XMPP standard compliance
	 * reasons, as GET or SET IQ Messages require a RESULT or ERROR IQ stanza
	 * from the XMPP partner. Developers not wanting the result/error to be
	 * handled by a PacketCollector should set the result messenger to null.
	 * 
	 * @author Istvan Koren
	 * 
	 */
	private class IQRunner implements Runnable {
		private Message msg;
		private long timeout = 4000;

		/**
		 * Constructs a new IQ runner.
		 * 
		 * @param result
		 *            the handler to be notified upon IQ result
		 * @param iq
		 *            the iq to be sent, must be of type GET or SET, as RESULT
		 *            and ERROR don't expect results.
		 */
		public IQRunner(Message msg) {
			this.msg = msg;
		}

		/**
		 * 
		 */
		@Override
		public void run() {
			Bundle data = msg.getData();
			XMPPIQ iq = data.getParcelable("PAYLOAD");

			IQImpl iqPacket = new IQImpl(iq.payload);
			iqPacket.fromXMPPIQ(iq);

			// create PacketCollector.
			PacketCollector coll = mConn
					.createPacketCollector(new PacketIDFilter(iqPacket
							.getPacketID()));
			iqPacket.setFrom(mConn.getUser());
			mConn.sendPacket(iqPacket);
			// send ack message
			Message msgAck = Message.obtain(msg);
			msgAck.arg1 = ConstMXA.MSG_STATUS_DELIVERED;
			xmppResults.sendMessage(msgAck);

			if (msgAck.getData().getParcelable("MSN_RESULT") != null) {
				Packet resultPacket = coll.nextResult(timeout);
				coll.cancel();
	
				// construct result Message
				Message resultMsg = Message.obtain(msg);
				if (resultPacket == null) {
					// timeout
					resultMsg.arg1 = ConstMXA.MSG_STATUS_ERROR;
				} else {
					// check if any error occurred
					XMPPError err = resultPacket.getError();
					if (err != null) {
						resultMsg.arg1 = ConstMXA.MSG_STATUS_ERROR;
					} else {
						Log.i(TAG, "Success IQ: " + resultPacket.toXML());
						// attach result
	
						resultMsg.arg1 = ConstMXA.MSG_STATUS_SUCCESS;
						if (resultPacket instanceof IQImpl) {
							IQImpl resultIQ = (IQImpl) resultPacket;
							data.putParcelable("PAYLOAD", new XMPPIQ(resultIQ
									.getFrom(), resultIQ.getTo(),
									XMPPIQ.TYPE_RESULT, resultIQ
											.getChildNamespace(), resultIQ
											.getChildElementName(), resultIQ
											.getChildElementXML()));
						}
					}
				}
				// notify result handler
				xmppResults.sendMessage(resultMsg);
			}
		}
	}

	private class MessageRunner implements Runnable {
		private Message msg;

		public MessageRunner(Message msg) {
			this.msg = msg;
		}

		@Override
		public void run() {
			Message msgAck = Message.obtain(msg);
			Bundle dataMsg = msg.getData();
			XMPPMessage payloadMsg = dataMsg.getParcelable("PAYLOAD");
			org.jivesoftware.smack.packet.Message xmppMsg = new org.jivesoftware.smack.packet.Message();
			xmppMsg.setTo(payloadMsg.to);
			if (payloadMsg.type == XMPPMessage.TYPE_CHAT) {
				// send chat message
				xmppMsg.setBody(payloadMsg.body);
				xmppMsg.setType(org.jivesoftware.smack.packet.Message.Type.chat);
				// save to database, update status later
				ContentValues values = new ContentValues();
				Long now = Long.valueOf(System.currentTimeMillis());
				values.put(MessageItems.SENDER, mConn.getUser());
				values.put(MessageItems.RECIPIENT, xmppMsg.getTo());
				if (xmppMsg.getSubject() != null)
					values.put(MessageItems.SUBJECT, xmppMsg.getSubject());
				if (xmppMsg.getBody() != null)
					values.put(MessageItems.BODY, xmppMsg.getBody());
				values.put(MessageItems.DATE_SENT, now);
				values.put(MessageItems.READ, 0);
				values.put(MessageItems.TYPE, "chat");
				values.put(MessageItems.STATUS, "sent");

				Log.i(TAG, "saving chat message");
				getContentResolver().insert(MessageItems.CONTENT_URI, values);

				// send via XMPP
				mConn.sendPacket(xmppMsg);
				msgAck.arg1 = ConstMXA.MSG_STATUS_DELIVERED;
			} else if (payloadMsg.type == XMPPMessage.TYPE_NORMAL) {
				// send data message with mxa-ident
				xmppMsg
						.setType(org.jivesoftware.smack.packet.Message.Type.normal);
				// add MXA-ident extension
				MXAIdentExtension mie = new MXAIdentExtension(
						payloadMsg.namespace, payloadMsg.token);
				xmppMsg.addExtension(mie);
				mConn.sendPacket(xmppMsg);
				msgAck.arg1 = ConstMXA.MSG_STATUS_DELIVERED;
			} else if (payloadMsg.type == XMPPMessage.TYPE_GROUPCHAT) {
				// we cannot handle groupchat at this time
				Log.e(TAG, "we cannot handle groupchat at this time");
				msgAck.arg1 = ConstMXA.MSG_STATUS_ERROR;
			} else {
				msgAck.arg1 = ConstMXA.MSG_STATUS_ERROR;
			}
			// send ack message
			xmppResults.sendMessage(msgAck);
		}

	}

	private Handler xmppResults = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// handle response of worker thread
			Message msg2 = Message.obtain(msg);
			// TODO send to IQ result if result/error, do not send if result
			// messenger null
			try {
				Bundle data = msg.getData();

				if (msg.what == ConstMXA.MSG_CONNECT
						&& msg.arg1 == ConstMXA.MSG_STATUS_SUCCESS) {
					Messenger ack = data.getParcelable("MSN_ACK");
					ack.send(msg2);
				} else if (msg.what == ConstMXA.MSG_DISCONNECT
						&& msg.arg1 == ConstMXA.MSG_STATUS_SUCCESS) {
					Messenger ack = data.getParcelable("MSN_ACK");
					ack.send(msg2);
				} else if (msg.arg1 == ConstMXA.MSG_STATUS_DELIVERED) {
					Messenger ack = data.getParcelable("MSN_ACK");
					ack.send(msg2);
				} else if (msg.arg1 == ConstMXA.MSG_STATUS_SUCCESS) {
					Messenger result = data.getParcelable("MSN_RESULT");
					result.send(msg2);
				}
			} catch (RemoteException e) {
				// Handler doesn't exist anymore, ignore it.
				e.printStackTrace();
			}
		}
	};

	public Handler getXMPPResultsHandler() {
		return xmppResults;
	}

	private class ReaderThread extends Thread implements PacketListener,
			ConnectionListener, RosterListener {

		public void run() {
			setName("MXA Reader Thread");
			Looper.prepare();
			Looper.loop();
		}

		@Override
		public void processPacket(Packet packet) {
			Log.i(TAG, "reading packet");
			if (packet instanceof org.jivesoftware.smack.packet.Message) {
				// ===================================
				// Message packets
				// ===================================
				org.jivesoftware.smack.packet.Message m = (org.jivesoftware.smack.packet.Message) packet;

				// if message is of type normal, notify listeners
				if (m.getType().equals(
						org.jivesoftware.smack.packet.Message.Type.normal)) {
					Log.i(TAG, "--> message type=normal");
					// notify callback listeners
					XMPPMessage xMsg = new XMPPMessage(m.getFrom(), m.getTo(),
							m.getBody(), m.getType().ordinal());

					MXAIdentExtension mie = (MXAIdentExtension) m.getExtension(
							MXAIdentExtension.ELEMENT_NAME,
							MXAIdentExtension.NAMESPACE);
					if (mie != null) {
						String namespaceMsg = mie.getConsumerNamespace();
						String tokenMsg = mie.getToken();
						boolean notifiedMsg = false;

						int i = mMsgCallbacks.beginBroadcast();
						while (i > 0) {
							i--;
							try {
								IXMPPMessageCallback imc = mMsgCallbacks
										.getBroadcastItem(i);
								if (mMsgCallbacks.getFilters(imc).contains(
										new MXAIdentFilter(namespaceMsg,
												tokenMsg))) {
									// found an appropriate filter, notify
									// callback interface
									imc.processMessage(xMsg);
									notifiedMsg = true;
								}
							} catch (RemoteException e) {
								// The RemoteCallbackList will take care of
								// removing the dead object for us.
							}
						}
						mMsgCallbacks.finishBroadcast();

						if (notifiedMsg) {
							// reply with error as no callback listeners
							// available with appropriate filter
							sendXMPPErrorMessage(
									m.getFrom(),
									XMPPError.Condition.feature_not_implemented,
									"No service available for this kind of request.");
						}
					} else {
						// reply with error as we do not yet understand
						// messages of type pubsub etc.
						//TODO: is disabled, because if we play with 2 android clients wich
						// uses mxa, they producing an endless loop of ErrorMessages to each other
						// tested with openfire in local network
/*						sendXMPPErrorMessage(m.getFrom(),
								XMPPError.Condition.feature_not_implemented,
								"No service available for this kind of request.");
*/					}
				} else if (m.getType().equals(
						org.jivesoftware.smack.packet.Message.Type.chat)) {
					Log.i(TAG, "message type=chat");
					// save in database, the content provider takes care of
					// notifying interested parties
					ContentValues values = new ContentValues();
					Long now = Long.valueOf(System.currentTimeMillis());
					values.put(MessageItems.SENDER, m.getFrom());
					values.put(MessageItems.RECIPIENT, m.getTo());
					if (m.getSubject() != null)
						values.put(MessageItems.SUBJECT, m.getSubject());
					if (m.getBody() != null)
						values.put(MessageItems.BODY, m.getBody());
					values.put(MessageItems.DATE_SENT, now);
					values.put(MessageItems.READ, 0);
					values.put(MessageItems.TYPE, "chat");
					values.put(MessageItems.STATUS, "received");

					Log.i(TAG, "saving chat message");
					Uri uri = getContentResolver().insert(
							MessageItems.CONTENT_URI, values);
					Log.i(TAG, "saved chat message to " + uri.toString());
				} else if (m.getType().equals(
						org.jivesoftware.smack.packet.Message.Type.groupchat)) {
					Log.i(TAG, "message type=groupchat");
					
					ContentValues values = new ContentValues();
					Long now = Long.valueOf(System.currentTimeMillis());
					values.put(MessageItems.SENDER, m.getFrom());
					values.put(MessageItems.RECIPIENT, m.getTo());
					if (m.getSubject() != null)
						values.put(MessageItems.SUBJECT, m.getSubject());
					if (m.getBody() != null)
						values.put(MessageItems.BODY, m.getBody());
					values.put(MessageItems.DATE_SENT, now);
					values.put(MessageItems.READ, 0);
					values.put(MessageItems.TYPE, "groupchat");
					values.put(MessageItems.STATUS, "received");

					Uri uri = getContentResolver().insert(
							MessageItems.CONTENT_URI, values);
					Log.i(TAG, "saved groupchat message to " + uri.toString());
				} else {
					Log.i(TAG, "message type=? -->"+m.getType().toString());
					sendXMPPErrorMessage(m.getFrom(),
							XMPPError.Condition.feature_not_implemented,
							"No service available for this kind of request.");
				}

			} else if (packet instanceof IQImpl) {
				// ===================================
				// IQ packets
				// ===================================
				Log.i(TAG, "packet instance of IQImpl");
				final XMPPIQ parcelable = ((IQImpl) packet).toXMPPIQ();
				for (int i = mIQCallbacks.beginBroadcast()-1; i >= 0; i--)
					try {
						IXMPPIQCallback callback = mIQCallbacks
								.getBroadcastItem(i);
						for (PacketFilter filter : mIQCallbacks
								.getFilters(callback))
							if (filter.accept(packet))
								callback.processIQ(parcelable);
					} catch (RemoteException e) {
						//TODO: Details zur Exception ausgeben.
						Log.e(TAG, "RemoteException!");
						e.printStackTrace();
					}
				mIQCallbacks.finishBroadcast();
			} else if (packet instanceof Presence) {
				// ===================================
				// Presence packets
				// ===================================
				// update db
				Log.i(TAG, "received presence");
				Presence pres = (Presence) packet;
				ContentValues values = new ContentValues();
				String user = StringUtils.parseBareAddress(pres.getFrom());
				values.put(RosterItems.XMPP_ID, user);
				if (pres.getType() == Presence.Type.unavailable)
					values.put(RosterItems.PRESENCE_MODE, "offline");
				else if (pres.getMode() == null)
					values.put(RosterItems.PRESENCE_MODE, "online");
				else
					values.put(RosterItems.PRESENCE_MODE, pres.getMode()
							.toString());
				values.put(RosterItems.PRESENCE_STATUS, pres.getStatus());

				Log.i(TAG, "saving presence information");
				Uri uri = getContentResolver().insert(RosterItems.CONTENT_URI,
						values);
				Log.i(TAG, "saved presence to " + uri.toString());
			}
			else {
				Log.e(TAG, "Packet unknown. XML: " + packet.toXML());
				if(packet.getError() != null)
				{
					Log.e(TAG, "ERROR Message: " + packet.getError());
				}
			}
		}

		// ==========================================================
		// Interface methods
		// ==========================================================

		@Override
		public void connectionClosed() {
			notifyConnectionListeners(false);
		}

		@Override
		public void connectionClosedOnError(Exception arg0) {
			notifyConnectionListeners(false);
		}

		@Override
		public void reconnectingIn(int arg0) {
			notifyConnectionListeners(false);
		}

		@Override
		public void reconnectionFailed(Exception arg0) {
			notifyConnectionListeners(false);
		}

		@Override
		public void reconnectionSuccessful() {
			notifyConnectionListeners(true);
		}

		@Override
		public void entriesAdded(Collection<String> entries) {
			final ContentResolver cr = XMPPRemoteService.this
					.getContentResolver();
			ContentValues[] cvs = getFromStrings(entries);
			cr.bulkInsert(ConstMXA.RosterItems.CONTENT_URI, cvs);
		}

		@Override
		public void entriesDeleted(Collection<String> entries) {
			final ContentResolver cr = XMPPRemoteService.this
					.getContentResolver();
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (String e : entries) {
				if (!first)
					sb.append(" or ");
				else
					first = false;
				sb.append(ConstMXA.RosterItems.XMPP_ID + "='" + e + "'");
			}
			cr.delete(ConstMXA.RosterItems.CONTENT_URI, sb.toString(), null);
		}

		@Override
		public void entriesUpdated(Collection<String> entries) {
			final ContentResolver cr = XMPPRemoteService.this
					.getContentResolver();
			ContentValues[] cvs = getFromStrings(entries);
			int i = 0;
			for (String e : entries) {
				cr.update(ConstMXA.RosterItems.CONTENT_URI, cvs[i],
						ConstMXA.RosterItems.XMPP_ID + "='" + e + "'", null);
				i++;
			}
		}

		@Override
		public void presenceChanged(Presence presence) {
			final ContentResolver cr = XMPPRemoteService.this
					.getContentResolver();
			ContentValues cv = this.getFromPresences(presence);
			cr.update(ConstMXA.RosterItems.CONTENT_URI, cv,
					ConstMXA.RosterItems.XMPP_ID + "='" + presence.getFrom()
							+ "'", null);
		}

		// ==========================================================
		// Private methods
		// ==========================================================

		/**
		 * 
		 * @param entries
		 *            The XMPP addresses of the contacts that have been added to
		 *            the roster.
		 */
		public ContentValues[] getFromStrings(Collection<String> entries) {
			final Roster r = XMPPRemoteService.this.mConn.getRoster();
			ContentValues[] cvs = new ContentValues[entries.size()];
			int i = 0;
			for (String e : entries) {
				final RosterEntry re = r.getEntry(e);
				final Presence p = r.getPresence(e);
				cvs[i] = new ContentValues();
				cvs[i].put(ConstMXA.RosterItems.XMPP_ID, e);
				cvs[i].put(ConstMXA.RosterItems.NAME, re.getName());
				cvs[i].put(ConstMXA.RosterItems.PRESENCE_MODE,
						(p.getMode() != null ? p.getMode().name() : null));
				cvs[i].put(ConstMXA.RosterItems.PRESENCE_STATUS, p.getStatus());
				cvs[i].put(ConstMXA.RosterItems.UPDATED_DATE, System
						.currentTimeMillis());
				i++;
			}
			return cvs;
		}

		/**
		 * 
		 * @param presence
		 * @return
		 */
		public ContentValues getFromPresences(Presence presence) {
			ContentValues cv = new ContentValues();
			Log.v(TAG, "getFromPresences --> presence.mode:"+presence.getMode().toString());
			cv.put(ConstMXA.RosterItems.PRESENCE_MODE, presence.getMode()
					.name());
			cv.put(ConstMXA.RosterItems.PRESENCE_STATUS, presence.getStatus());
			cv.put(ConstMXA.RosterItems.UPDATED_DATE, System
					.currentTimeMillis());
			return cv;
		}

		/**
		 * Sends an XMPP Error Message to the recipient.
		 */
		private void sendXMPPErrorMessage(String to,
				XMPPError.Condition condition, String errorText) {
			org.jivesoftware.smack.packet.Message msg = new org.jivesoftware.smack.packet.Message(
					to, org.jivesoftware.smack.packet.Message.Type.error);
			msg.setError(new XMPPError(condition, errorText));
			mConn.sendPacket(msg);
		}

		/**
		 * Notifies all remote connection listeners on connection changes.
		 * 
		 * @param connected
		 *            If XMPP is connected or not.
		 */
		private void notifyConnectionListeners(boolean connected) {
			int i = mConnectionCallbacks.beginBroadcast();
			while (i > 0) {
				i--;
				try {
					mConnectionCallbacks.getBroadcastItem(i)
							.onConnectionChanged(connected);
				} catch (RemoteException e) {
					// The RemoteCallbackList will take care of
					// removing the dead object for us.
				}
			}
			mConnectionCallbacks.finishBroadcast();
		}
	}

	public IXMPPService getXMPPService() {
		return mBinder;
	}
	
	private final IXMPPService.Stub mBinder = new IXMPPService.Stub() {

		@Override
		public void connect(Messenger acknowledgement) throws RemoteException {
			Log.i(TAG, "connect to XMPP server");
			Message msg = new Message();
			msg.what = ConstMXA.MSG_CONNECT;

			// set ack target
			Bundle data = new Bundle();
			data.putParcelable("MSN_ACK", acknowledgement);
			msg.setData(data);

			xmppWriteWorker.mHandler.sendMessage(msg);
		}

		@Override
		public void disconnect(Messenger acknowledgement)
				throws RemoteException {
			Log.i(TAG, "disconnect from XMPP server");
			Message msg = new Message();
			msg.what = ConstMXA.MSG_DISCONNECT;

			// set ack target
			Bundle data = new Bundle();
			data.putParcelable("MSN_ACK", acknowledgement);
			msg.setData(data);

			xmppWriteWorker.mHandler.sendMessage(msg);
		}

		@Override
		public void sendMessage(Messenger acknowledgement, int requestCode,
				XMPPMessage message) throws RemoteException {
			Message msg = Message.obtain();
			// send packet
			msg.what = ConstMXA.MSG_SEND_MESSAGE;
			msg.arg2 = requestCode;
			// create new Bundle and supply the Acknowledgement Messenger
			Bundle data = new Bundle();
			data.putParcelable("MSN_ACK", acknowledgement);
			data.putParcelable("PAYLOAD", message);
			msg.setData(data);

			// send message to a writer thread
			MessageRunner mr = new MessageRunner(msg);
			mWriteExecutor.execute(mr);
		}

		@Override
		public void sendIQ(Messenger acknowledgement, Messenger result,
				int requestCode, XMPPIQ iq) throws RemoteException {
			// if iq type is GET or SET, send iq to ThreadPool for result
			// waiting. if iq type is RESULT or ERROR, send iq to worker thread

			Message msg = new Message();
			msg.what = ConstMXA.MSG_SEND_IQ;
			msg.arg1 = ConstMXA.MSG_STATUS_REQUEST;
			msg.arg2 = requestCode;
			Bundle data = new Bundle();
			data.putParcelable("MSN_ACK", acknowledgement);
			data.putParcelable("MSN_RESULT", result);
			data.putParcelable("PAYLOAD", iq);
			msg.setData(data);

			if (iq.type == XMPPIQ.TYPE_GET || iq.type == XMPPIQ.TYPE_SET) {
				IQRunner iqRunJob = new IQRunner(msg);
				mWriteExecutor.execute(iqRunJob);
			} else {
				// send iq to worker thread
				xmppWriteWorker.mHandler.sendMessage(msg);
			}
		}
		
		@Override
		public void sendIQWithRetransmission(Messenger acknowledgement,
				Messenger result, int requestCode, XMPPIQ iq,
				int retransmissionAttempts) throws RemoteException {
			// TODO RETRANSMISSION
			
		}

		@Override
		public void sendPresence(Messenger acknowledgement, int requestCode,
				XMPPPresence presence) throws RemoteException {
			Message msg = new Message();
			// send packet
			msg.what = ConstMXA.MSG_SEND_PRESENCE;
			msg.arg2 = requestCode;
			// create new Bundle and supply the Acknowledgement Messenger
			Bundle data = new Bundle();
			data.putParcelable("MSN_ACK", acknowledgement);
			data.putParcelable("PAYLOAD", presence);
			msg.setData(data);

			// send message to worker thread
			xmppWriteWorker.mHandler.sendMessage(msg);
		}

		@Override
		public void registerDataMessageCallback(IXMPPMessageCallback cb,
				String namespace, String token) throws RemoteException {
			if (cb != null) {
				MXAIdentFilter mif = new MXAIdentFilter(namespace, token);
				mMsgCallbacks.register(cb, mif);
			}
		}

		@Override
		public void unregisterDataMessageCallback(IXMPPMessageCallback cb,
				String namespace, String token) throws RemoteException {
			if (cb != null) {
				MXAIdentFilter mif = new MXAIdentFilter(namespace, token);
				mMsgCallbacks.unregister(cb, mif);
			}
		}

		@Override
		public String getUsername() throws RemoteException {
			Bundle xmppConnectionParameters = getXMPPConnectionParameters();
			return xmppConnectionParameters.getString("xmpp_user") + "@" +
					xmppConnectionParameters.getString("xmpp_service") + "/" +
					xmppConnectionParameters.getString("xmpp_resource");
		}
		
		@Override
		public boolean isConnected() throws RemoteException {
			if (mConn != null) {
				return mConn.isAuthenticated();
			} else {
				return false;
			}
		}

		@Override
		public void registerConnectionCallback(IConnectionCallback cb)
				throws RemoteException {
			mConnectionCallbacks.register(cb);
		}

		@Override
		public void unregisterConnectionCallback(IConnectionCallback cb)
				throws RemoteException {
			mConnectionCallbacks.unregister(cb);
		}

		@Override
		public IFileTransferService getFileTransferService()
				throws RemoteException {
			IBinder b = mFileTransferService.onBind(null);
			return (IFileTransferService) b;
		}
		
		@Override
		public IMultiUserChatService getMultiUserChatService()
				throws RemoteException {
			IBinder b = mMultiUserChatService.onBind(null);
			return (IMultiUserChatService) b;
		}

		@Override
		public IPubSubService getPubSubService() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public IServiceDiscoveryService getServiceDiscoveryService()
				throws RemoteException {
			IBinder b = mServiceDiscoveryService.onBind(null);
			return (IServiceDiscoveryService) b;
		}
		
		public ICollabEditingService getCollabEditingService() throws RemoteException {
			IBinder b = mCollabEditingService.onBind(null);
			return (ICollabEditingService) b;
		}

		@Override
		public void registerIQCallback(IXMPPIQCallback cb, String elementName,
				String namespace) throws RemoteException {
			if (cb != null) {
				IQImplProvider iqProvider = new IQImplProvider(namespace,
						elementName);
				ProviderManager.getInstance().addIQProvider(elementName,
						namespace, iqProvider);
				IQImplFilter iqFilter = new IQImplFilter(elementName, namespace);
				boolean result = mIQCallbacks.register(cb, iqFilter);
				Log.i(TAG, "registerIQCallback(). elementName="+elementName+" namespace="+namespace+". result="+result);
			}
		}

		@Override
		public void unregisterIQCallback(IXMPPIQCallback cb,
				String elementName, String namespace) throws RemoteException {
			if (cb != null) {
				// TODO remove IQ Provider if all callbacks for this
				// elementName/namespace combination have been unregistered.
				IQImplFilter iqFilter = new IQImplFilter(elementName, namespace);
				mIQCallbacks.unregister(cb, iqFilter);
			}
		}

		@Override
		public Bundle getXMPPConnectionParameters() throws RemoteException {
			// TODO Auto-generated method stub
			
			// read server preferences
			String host = mPreferences.getString("pref_host", null);
			int port = Integer.parseInt(mPreferences.getString(
					"pref_port", null));
			String serviceName = mPreferences.getString(
					"pref_service", null);			
			// read user credentials
			String username = mPreferences.getString(
					"pref_xmpp_user", null);
			String password = mPreferences.getString(
					"pref_xmpp_password", null);
			String resource = mPreferences.getString(
					"pref_resource", null);
						
			if ((host == null) || (serviceName == null) ||
					(username == null) || (password == null) || (resource==null)) {
				return null;
			}
			
			Bundle connectionParams = new Bundle();
			connectionParams.putString("xmpp_host", host);
			connectionParams.putInt("xmpp_port", port);
			connectionParams.putString("xmpp_service", serviceName);
			connectionParams.putString("xmpp_user", username);
			connectionParams.putString("xmpp_password", password);
			connectionParams.putString("xmpp_resource", resource);

			return connectionParams;
		}

	};

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
	}

}
