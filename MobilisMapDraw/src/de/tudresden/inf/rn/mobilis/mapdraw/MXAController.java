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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.widget.Toast;
import de.tudresden.inf.rn.mobilis.android.services.MobilisAndroidService;
import de.tudresden.inf.rn.mobilis.android.services.SessionService;

import de.tudresden.inf.rn.mobilis.mxa.ConstMXA;
import de.tudresden.inf.rn.mobilis.mxa.IXMPPService;
import de.tudresden.inf.rn.mobilis.mxa.callbacks.IXMPPIQCallback;
import de.tudresden.inf.rn.mobilis.mxa.callbacks.IXMPPMessageCallback;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPIQ;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPMessage;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPPresence;


/**
 * Singleton class to easily access the XMPP service.
 * @author Koren, Hering
 */
public class MXAController {

	private static MXAController instance;
	private Context context;
	Messenger mStdAckMessenger;
	Messenger mStdIQMessenger;
	IXMPPService mXMPPService = null;
	
//	// the enum values sent as action to the worker thread
//	public static final int MSG_CONNECT = 0;
//	public static final int MSG_DISCONNECT = 1;
//	public static final int MSG_SEND_MESSAGE = 2;
//	public static final int MSG_SEND_IQ = 3;
//	public static final int MSG_SEND_PRESENCE = 4;
//	// the enum values for status
//	public static final int MSG_STATUS_REQUEST = 0;
//	public static final int MSG_STATUS_SUCCESS = 1;
//	public static final int MSG_STATUS_DELIVERED = 2;
//	public static final int MSG_STATUS_ERROR = 3;
//	// the additional enum values for IQ status
//	public static final int MSG_STATUS_IQ_RESULT = 4;
//	public static final int MSG_STATUS_IQ_ERROR = 5;
//	// the enum values for callbacks
//	public static final int MSG_PRES_RECEIVED = 0;
//	public static final int MSG_MSG_RECEIVED = 1;
//	public static final int MSG_IQ_RECEIVED = 2;
	
	private MXAController() {
		mStdAckMessenger = new Messenger(xmppResultHandler);
		mStdIQMessenger = new Messenger(xmppIQHandler);
		context = SessionService.getInstance().getContext();
	};
	
	public static MXAController get() {
		if (instance == null) {
			instance = new MXAController();
		}
		return instance;
	}
	
	public void connectToXMPPService() {
		// bind to the Remote Service
		Intent i = new Intent(IXMPPService.class.getName());
		// int pid = android.os.Process.myPid();
		// i.putExtra("PROCESSID", pid);
		// Messenger msn = new Messenger(xmppResults);
		// i.putExtra("MESSENGER", msn);
		context.bindService(i, mConnection, Context.BIND_AUTO_CREATE);
	}

	public void loginToXMPP() {
		// call login on service
		try {
			mXMPPService.connect(mStdAckMessenger);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	public void sendTestPresence() {
		XMPPPresence presence = new XMPPPresence(
				XMPPPresence.MODE_AWAY, "out eating", 5);
		try {
			mXMPPService.sendPresence(mStdAckMessenger, 0, presence);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendTestMessage(String toJID) {
		// send a test message
		XMPPMessage message = new XMPPMessage(null, toJID,
				"test message", XMPPMessage.TYPE_CHAT);
		try {
			mXMPPService.sendMessage(mStdAckMessenger, 0, message);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendTestIQ() {
		String payload = "<payload />";
		XMPPIQ iq = new XMPPIQ(null, "emil@mobilisenv/MATLAB",
				XMPPIQ.TYPE_GET, "query", "mobilis:iq:test", payload);
		try {
			mXMPPService.sendIQ(mStdAckMessenger, mStdIQMessenger, 0,
					iq);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void disconnectFromXMPP() {
		// disconnect from xmpp server
		try {
			mXMPPService.disconnect(mStdAckMessenger);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Class for interacting with the main interface of the service.
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. We are communicating with our
			// service through an IDL interface, so get a client-side
			// representation of that from the raw service object.
			mXMPPService = IXMPPService.Stub.asInterface(service);

			// register Message callback
//			try {
//				mXMPPService.registerMessageCallback(mMsgCallback);
//			} catch (RemoteException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

			// inform the user about the established service connection
			Toast.makeText(context, "Service connected",
					Toast.LENGTH_SHORT).show();
			
			// login to XMPP
			loginToXMPP();
			//sendTestMessage("u2@wpc");
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mXMPPService = null;

			// As part of the sample, tell the user what happened.
			Toast.makeText(context, "Service disconnected",
					Toast.LENGTH_SHORT).show();
		}
	};

	/**
	 * Receives the Acknowledgements from the XMPP service.
	 */
	private Handler xmppResultHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// handle response of service
			switch (msg.what) {
			case ConstMXA.MSG_CONNECT:
				// successfully connected?
				String status;
				if (msg.arg1 == ConstMXA.MSG_STATUS_SUCCESS) status = "XMPP is connected";
				else status = "XMPP is NOT connected";
				Toast.makeText(context, status,
						Toast.LENGTH_SHORT).show();
				break;
			case ConstMXA.MSG_DISCONNECT:
				// successfully disconnected
				Toast.makeText(context, "XMPP disconnected",
						Toast.LENGTH_SHORT).show();
				break;
			case ConstMXA.MSG_SEND_MESSAGE:
				// sent message
				Toast.makeText(context, "Sent Message",
						Toast.LENGTH_SHORT).show();
				break;
			case ConstMXA.MSG_SEND_PRESENCE:
				// sent presence
				Toast.makeText(context, "Sent Presence",
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	/**
	 * Receives the IQ results from the XMPP service and incoming IQs from the
	 * XMPP IQ callback.
	 */
	private Handler xmppIQHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case ConstMXA.MSG_IQ_RECEIVED:
				Toast.makeText(context, "received IQ stanza",
						Toast.LENGTH_SHORT).show();
				break;
			case ConstMXA.MSG_SEND_IQ:
				switch (msg.arg1) {
				case ConstMXA.MSG_STATUS_ERROR:
					Toast.makeText(context, "IQ result timeout",
							Toast.LENGTH_SHORT).show();
					break;
				default:
					Toast.makeText(context, "received IQ result",
							Toast.LENGTH_SHORT).show();
				}
				break;
			}
		}
	};

	/**
	 * Receives new Messages from the XMPP message callback.
	 */
	private Handler xmppMessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			XMPPMessage xMsg = msg.getData().getParcelable("PAYLOAD");
			Toast.makeText(context, "Message received: " + xMsg.body,
					Toast.LENGTH_LONG).show();
		}
	};

	/**
	 * This implementation is used to receive Message callbacks from the remote
	 * service.
	 */
//	private IXMPPMessageCallback mMsgCallback = new IXMPPMessageCallback.Stub() {
//
//		/**
//		 * This is called by the remote service regularly to tell us about new
//		 * values. Note that IPC calls are dispatched through a thread pool
//		 * running in each process, so the code executing here will NOT be
//		 * running in our main thread like most other things -- so, to update
//		 * the UI, we need to use a Handler to hop over there.
//		 */
//		@Override
//		public void processMessage(XMPPMessage message) throws RemoteException {
//			Message msg = Message.obtain(xmppMessageHandler, MSG_MSG_RECEIVED);
//			Bundle data = new Bundle();
//			data.putParcelable("PAYLOAD", message);
//			msg.setData(data);
//			msg.sendToTarget();
//		}
//
//	};

	/**
	 * This implementation is used to receive IQ callbacks from the remote
	 * service.
	 */
	private IXMPPIQCallback mIQCallback = new IXMPPIQCallback.Stub() {

		/**
		 * This is called by the remote service regularly to tell us about new
		 * values. Note that IPC calls are dispatched through a thread pool
		 * running in each process, so the code executing here will NOT be
		 * running in our main thread like most other things -- so, to update
		 * the UI, we need to use a Handler to hop over there.
		 */
		@Override
		public void processIQ(XMPPIQ iq) throws RemoteException {
			Message msg = Message.obtain(xmppIQHandler, ConstMXA.MSG_IQ_RECEIVED);
			Bundle data = new Bundle();
			data.putParcelable("PAYLOAD", iq);
			msg.setData(data);
			msg.sendToTarget();
		}

	};
	
	public void setAckMessenger(Messenger mStdAckMessenger) {
		this.mStdAckMessenger = mStdAckMessenger;
	}

	public void setXMPPService(IXMPPService mXMPPService) {
		this.mXMPPService = mXMPPService;
	}

	public void setIQMessenger(Messenger mStdIQMessenger) {
		this.mStdIQMessenger = mStdIQMessenger;
	}
	
	
	public Messenger getAckMessenger() {
		return mStdAckMessenger;
	}

	public Messenger getIqMessenger() {
		return mStdIQMessenger;
	}

	public IXMPPService getXmppService() {
		return mXMPPService;
	}
}
