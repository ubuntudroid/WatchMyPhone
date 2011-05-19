/**
 * Copyright (C) 2009 Technische Universität Dresden
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

package de.tudresden.inf.rn.mobilis.mxa.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import de.tudresden.inf.rn.mobilis.mxa.ConstMXA;
import de.tudresden.inf.rn.mobilis.mxa.IXMPPService;
import de.tudresden.inf.rn.mobilis.mxa.R;
import de.tudresden.inf.rn.mobilis.mxa.callbacks.IXMPPIQCallback;
import de.tudresden.inf.rn.mobilis.mxa.callbacks.IXMPPMessageCallback;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPIQ;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPMessage;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPPresence;
import de.tudresden.inf.rn.mobilis.mxa.services.parcelable.FileTransfer;

/**
 * @author Istvan Koren, Benjamin Söllner
 */
public class MainActivity extends Activity {

	// views
	private Button btnServiceConnect;
	private Button btnConnectionStart;
	private Button btnPresenceSet;
	private Button btnMessageSend;
	private Button btnIQSend;
	private Button btnDisconnect;
	private Button btnFileSend;
	private Button btnMonitor;
	private Menu mMenu;
	private Messenger mStdAckMessenger;
	private Messenger mStdIQMessenger;

	// members
	private boolean isServiceConnected = false;
	IXMPPService mXMPPService = null;

	/**
	 * Start the MainActivity. Uses the CLEAR_TOP flag which means that other
	 * stacked activities may be killed in order to get back to MainActivity.
	 */
	public static void actionShowMainActivity(Context context) {
		Intent i = new Intent(context, MainActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(i);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mStdAckMessenger = new Messenger(xmppResultHandler);
		mStdIQMessenger = new Messenger(xmppIQHandler);

		// link views
		btnServiceConnect = (Button) findViewById(R.id.main_btn_service);
		btnServiceConnect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// bind to the Remote Service
				Intent i = new Intent(IXMPPService.class.getName());
				// int pid = android.os.Process.myPid();
				// i.putExtra("PROCESSID", pid);
				// Messenger msn = new Messenger(xmppResults);
				// i.putExtra("MESSENGER", msn);
				startService(i);
				bindService(i, mConnection, 0);
				isServiceConnected = true;
			}
		});
		btnConnectionStart = (Button) findViewById(R.id.main_btn_server);
		btnConnectionStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// call login on service
				try {
					mXMPPService.connect(mStdAckMessenger);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		btnPresenceSet = (Button) findViewById(R.id.main_btn_presence);
		btnPresenceSet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				XMPPPresence presence = new XMPPPresence(
						XMPPPresence.MODE_AWAY, "out eating", 5);
				try {
					mXMPPService.sendPresence(mStdAckMessenger, 0, presence);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		btnMessageSend = (Button) findViewById(R.id.main_btn_message);
		btnMessageSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// send a test message
				XMPPMessage message = new XMPPMessage(null, "emil@mobilisenv",
						"test message", XMPPMessage.TYPE_CHAT);
				try {
					mXMPPService.sendMessage(mStdAckMessenger, 0, message);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		btnIQSend = (Button) findViewById(R.id.main_btn_iq);
		btnIQSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				String payload = "<query xmlns=\"mobilis:iq:test\"/>";
				XMPPIQ iq = new XMPPIQ(null, "emil@mobilisenv/MATLAB",
						XMPPIQ.TYPE_GET, "mobilis", "mobilis:test", payload);
				try {
					mXMPPService.sendIQ(mStdAckMessenger, mStdIQMessenger, 0,
							iq);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		btnFileSend = (Button) findViewById(R.id.main_btn_file);
		btnFileSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String from = "anton@mobilisenv";
				String to = "emil@mobilisenv/MobilisPC";
				String description = "Hello Watson!";
				String path = "/sdcard/transfertest.txt"; // This file has to be
				// present on the
				// file system.
				FileTransfer xmppFile = new FileTransfer(from, to, description,
						path, "", -1, 50);
				try {
					mXMPPService.getFileTransferService().sendFile(
							mStdAckMessenger, 0, xmppFile);
				} catch (RemoteException e) {
					Log
							.e(MainActivity.class.getName(),
									"Unable to request SaaAS to initiate file transfer.");
				}
			}
		});

		btnDisconnect = (Button) findViewById(R.id.main_btn_disconnect);
		btnDisconnect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// disconnect from server
				try {
					mXMPPService.disconnect(mStdAckMessenger);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		btnMonitor = (Button) findViewById(R.id.main_btn_monitor);
		btnMonitor.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// show Service Monitor
				Intent i = new Intent(MainActivity.this, ServiceMonitor.class);
				startActivity(i);
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (isServiceConnected)
			unbindService(mConnection);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mMenu = menu;

		// Inflate the menu XML resource.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_map, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_pref:
			Intent i = new Intent(this, PreferencesClient.class);
			startActivity(i);
			break;
		}
		return false;
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
			try {
				mXMPPService.registerDataMessageCallback(mMsgCallback,
						"namespace", "token");
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// As part of the sample, tell the user what happened.
			Toast.makeText(MainActivity.this, "Service connected",
					Toast.LENGTH_SHORT).show();
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mXMPPService = null;

			// As part of the sample, tell the user what happened.
			Toast.makeText(MainActivity.this, "Service disconnected",
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
				// successfully connected
				Toast.makeText(MainActivity.this, "XMPP connected",
						Toast.LENGTH_SHORT).show();
				break;
			case ConstMXA.MSG_DISCONNECT:
				// successfully disconnected
				Toast.makeText(MainActivity.this, "XMPP disconnected",
						Toast.LENGTH_SHORT).show();
				break;
			case ConstMXA.MSG_SEND_MESSAGE:
				// sent message
				Toast.makeText(MainActivity.this, "Sent Message",
						Toast.LENGTH_SHORT).show();
				break;
			case ConstMXA.MSG_SEND_PRESENCE:
				// sent presence
				Toast.makeText(MainActivity.this, "Sent Presence",
						Toast.LENGTH_SHORT).show();
				break;
			case ConstMXA.MSG_SEND_FILE:
				// file transfer status
				String text = "File Transfer Response";
				if (msg.arg1 == ConstMXA.MSG_STATUS_SUCCESS) {
					Bundle b = msg.getData();
					int blocksTransferred = b.getInt("INT_BLOCKSTRANSFERRED");
					if (blocksTransferred == -1)
						text = "File transfer negotiation in progress.";
					else if (blocksTransferred == 0)
						text = "File transfer negotiated.";
					else
						text = "Transmitted block " + blocksTransferred + ".";
				} else if (msg.arg1 == ConstMXA.MSG_STATUS_DELIVERED) {
					text = "File transferred completely.";
				} else if (msg.arg1 == ConstMXA.MSG_STATUS_ERROR) {
					Bundle b = msg.getData();
					String e = b.getString("STR_ERRORMESSAGE");
					text = "File Transfer error: " + e;
				}
				Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT)
						.show();
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
			case ConstMXA.MSG_SEND_IQ:
				switch (msg.arg1) {
				case ConstMXA.MSG_STATUS_ERROR:
					Toast.makeText(MainActivity.this, "IQ result timeout",
							Toast.LENGTH_SHORT).show();
					break;
				default:
					Toast.makeText(MainActivity.this, "received IQ result",
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
			Toast.makeText(MainActivity.this, "Message received: " + xMsg.body,
					Toast.LENGTH_LONG).show();
		}
	};

	/**
	 * This implementation is used to receive Message callbacks from the remote
	 * service.
	 */
	private IXMPPMessageCallback mMsgCallback = new IXMPPMessageCallback.Stub() {

		/**
		 * This is called by the remote service regularly to tell us about new
		 * values. Note that IPC calls are dispatched through a thread pool
		 * running in each process, so the code executing here will NOT be
		 * running in our main thread like most other things -- so, to update
		 * the UI, we need to use a Handler to hop over there.
		 */
		@Override
		public void processMessage(XMPPMessage message) throws RemoteException {
			Message msg = Message.obtain(xmppMessageHandler,
					ConstMXA.MSG_MSG_RECEIVED);
			Bundle data = new Bundle();
			data.putParcelable("PAYLOAD", message);
			msg.setData(data);
			msg.sendToTarget();
		}

	};

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
			Message msg = Message.obtain(xmppIQHandler,
					ConstMXA.MSG_IQ_RECEIVED);
			Bundle data = new Bundle();
			data.putParcelable("PAYLOAD", iq);
			msg.setData(data);
			msg.sendToTarget();
		}

	};
}
