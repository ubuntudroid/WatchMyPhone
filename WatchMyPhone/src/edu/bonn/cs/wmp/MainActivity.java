package edu.bonn.cs.wmp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import de.tudresden.inf.rn.mobilis.mxa.MXAController;
import de.tudresden.inf.rn.mobilis.mxa.MXAListener;
import de.tudresden.inf.rn.mobilis.mxa.callbacks.IXMPPIQCallback;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPIQ;
import de.tudresden.inf.rn.mobilis.xmpp.android.Parceller;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import edu.bonn.cs.wmp.service.CollabEditingService;
import edu.bonn.cs.wmp.service.SessionService;
import edu.bonn.cs.wmp.views.WMPButton;
import edu.bonn.cs.wmp.views.WMPEditText;
import edu.bonn.cs.wmp.xmpp.beans.ButtonBean;
import edu.bonn.cs.wmp.xmpp.beans.PingBean;

public class MainActivity extends Activity {
    
	private static MainActivity instance;

	boolean DEBUG = false;
	
	private final static String TAG = "MainActivity";
	
	private MXAListener mMXAListener;
	private MXAController mMXAController;
	private IXMPPService iXMPPService;
	private Messenger mConnectMessenger;
	
	private Button accountInfoBtn;
	
	private WMPButton sendPingBtn;
	private WMPButton testBtn;
	private WMPButton tic0Btn;
	private WMPButton tic1Btn;
	private WMPButton tic2Btn;
	private WMPButton tic3Btn;
	private WMPButton tic4Btn;
	private WMPButton tic5Btn;
	private WMPButton tic6Btn;
	private WMPButton tic7Btn;
	private WMPButton tic8Btn;
	
	private WMPEditText collabText;
	
	private int[][] gameBoard = new int[3][3];
	private int roundCounter = 0;
	
	//TODO: obtain XMPP Service from MXAService 
	private final String xmppService = "sven-ubuntu-big";
	private final String xmppResource = "MXA";
	private final String wmpUser1 = createXMPPAddress("desire");
	private final String wmpUser2 = createXMPPAddress("emu");
	
	private String fromAccount;
	private String toAccount;
	
	// TODO: connecting, disconnecting and send/receiveIQ can be encapsulated in WMPButton or WMPBean -> more transparency
	
	private Handler xmppResultHandler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			if (DEBUG) Toast.makeText(getApplicationContext(), "XMPP connected: " + msg, Toast.LENGTH_LONG).show();
		};
		
	};

	private Handler ackHandler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			if (DEBUG) Toast.makeText(getApplicationContext(), "Message ACKed: " + msg, Toast.LENGTH_SHORT).show();
		};
	};
	
	private Handler btnIQResultHandler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			if (DEBUG) Toast.makeText(getApplicationContext(), "Button IQ result is: " + msg, Toast.LENGTH_SHORT).show();
		};
	};

	private Handler pingIQResultHandler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			if (DEBUG) Toast.makeText(getApplicationContext(), "Ping IQ result is: " + msg, Toast.LENGTH_SHORT).show();
		};
	};
	
	private IXMPPIQCallback pingIQCallback = new IXMPPIQCallback.Stub() {
		
		@Override
		public void processIQ(final XMPPIQ xiq) throws RemoteException {
			XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(xiq);
			
			if (b instanceof PingBean) {
				Log.i(TAG, "Received IQ: " + xiq.payload);
				if (DEBUG) makeAndShowToast("Received IQ: " + xiq.payload, Toast.LENGTH_SHORT);
				PingBean p = (PingBean) b;
				if (p.getType() == XMPPBean.TYPE_SET){
					makeAndShowToast(p.getMessage(), Toast.LENGTH_SHORT);
					if (p.getCounter() != 1) {
						sendPingBack(p);
					} else {
						makeAndShowToast("Ping successful", Toast.LENGTH_SHORT);
					}
				} else if (p.getType() == XMPPBean.TYPE_RESULT){
					// TODO
				} else if (p.getType() == XMPPBean.TYPE_ERROR){
					Log.e(TAG, "PingBean type=ERROR arrived. IQ-Payload:" + xiq.payload);
				}
			}
		}
	};
	
	private IXMPPIQCallback buttonIQCallback = new IXMPPIQCallback.Stub() {
		
		@Override
		public void processIQ(final XMPPIQ xiq) throws RemoteException {
			XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(xiq);
			
			if (b instanceof ButtonBean) {
				Log.i(TAG, "Received IQ: " + xiq.payload);
				if (DEBUG) makeAndShowToast("Received IQ: " + xiq.payload, Toast.LENGTH_SHORT);
				ButtonBean p = (ButtonBean) b;
				if (p.getType() == XMPPBean.TYPE_SET){
					final WMPButton btn = (WMPButton) findViewById(getResources().getIdentifier(p.getWmpId(), "id", "edu.bonn.cs.wmp"));
					btn.setWmpInput(true);
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							btn.performClick();
						}
					});
				} else if (p.getType() == XMPPBean.TYPE_RESULT){
					// TODO
				} else if (p.getType() == XMPPBean.TYPE_ERROR){
					Log.e(TAG, "PingBean type=ERROR arrived. IQ-Payload:" + xiq.payload);
				}
			}
		}
	};
	
	public static MainActivity getInstance() {
		return instance;
    }

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        instance = this;
        
        sendPingBtn = (WMPButton) findViewById(R.id.main_button_send);
        sendPingBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendPingIQ();
			}
		});
        
        testBtn = (WMPButton) findViewById(R.id.main_button_test);
        testBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//TODO: disable button for user while in wmpInput mode
				/*TODO: make wmpInput status handling transparent for developer (i.e. provide method 
				 *onUserClick in WMPButton and handle status in onClick over there)
				 */
				if (!testBtn.isWmpInput()){
					sendButtonTestIQ();
				} else {
					makeAndShowToast("Button clicked", Toast.LENGTH_SHORT);
					testBtn.setWmpInput(false);
				}
			}
		});
        
        accountInfoBtn = (Button) findViewById(R.id.main_button_private_info);
        accountInfoBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
				ab.setMessage("Your account name is " + fromAccount).show();
			}
		});
        
        initializeGameBoard();
        initializeTicButtons();        
        collabText = (WMPEditText) findViewById(R.id.main_edit_text_collab_text);
        
        
//        startXMPPPrefs();
//        registerBeanPrototypes();
//        connectToServiceAndServer();

        SessionService.getInstance().getCollabEditingService().connect();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	makeAndShowToast("Using account " + fromAccount, Toast.LENGTH_SHORT);
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu XML resource.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);

		return true;
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.menu_pref:
    		startXMPPPrefs();
    	case R.id.menu_join:
    		CollabEditingService collabService = SessionService.getInstance().getCollabEditingService();
    		collabService.joinSession("edit_text_test");
//    		collabService.loadDocumentFromServer("edit_text_test.xml");
    		collabText.setCollabEditingService(collabService);
    	}
    	return true;
    }

	private void initializeGameBoard() {
		for (int i = 0; i < 3; i++){
			for (int j = 0; j < 3; j++){
				gameBoard[j][i] = 0;
			}
		}
	}

	private void initializeTicButtons() {
		OnClickListener ticBtnListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				WMPButton ticBtn = (WMPButton) v;
				roundCounter++;
				if (ticBtn.isWmpInput()){
					ticBtn.setWmpInput(false);
				} else {
					sendButtonIQ("main_button_tic_" + (String) ticBtn.getTag());
				}
				if (roundCounter % 2 == 1){ // player 1 turn
					ticBtn.setText("X");
					setMarker(1, Integer.parseInt((String) ticBtn.getTag()));
				} else {					// player 2 turn
					ticBtn.setText("O");
					setMarker(4, Integer.parseInt((String) ticBtn.getTag()));
				}
				checkForRow();
			}
		};
		
		tic0Btn = (WMPButton) findViewById(R.id.main_button_tic_0);
		tic0Btn.setTag("0"); // tags are used to determine the button's position on game board
		tic0Btn.setOnClickListener(ticBtnListener);
		
		tic1Btn = (WMPButton) findViewById(R.id.main_button_tic_1);
		tic1Btn.setTag("1"); // tags are used to determine the button's position on game board
		tic1Btn.setOnClickListener(ticBtnListener);

		tic2Btn = (WMPButton) findViewById(R.id.main_button_tic_2);
		tic2Btn.setTag("2"); // tags are used to determine the button's position on game board
		tic2Btn.setOnClickListener(ticBtnListener);
		
		tic3Btn = (WMPButton) findViewById(R.id.main_button_tic_3);
		tic3Btn.setTag("3"); // tags are used to determine the button's position on game board
		tic3Btn.setOnClickListener(ticBtnListener);
		
		tic4Btn = (WMPButton) findViewById(R.id.main_button_tic_4);
		tic4Btn.setTag("4"); // tags are used to determine the button's position on game board
		tic4Btn.setOnClickListener(ticBtnListener);
		
		tic5Btn = (WMPButton) findViewById(R.id.main_button_tic_5);
		tic5Btn.setTag("5"); // tags are used to determine the button's position on game board
		tic5Btn.setOnClickListener(ticBtnListener);
		
		tic6Btn = (WMPButton) findViewById(R.id.main_button_tic_6);
		tic6Btn.setTag("6"); // tags are used to determine the button's position on game board
		tic6Btn.setOnClickListener(ticBtnListener);
		
		tic7Btn = (WMPButton) findViewById(R.id.main_button_tic_7);
		tic7Btn.setTag("7"); // tags are used to determine the button's position on game board
		tic7Btn.setOnClickListener(ticBtnListener);
		
		tic8Btn = (WMPButton) findViewById(R.id.main_button_tic_8);
		tic8Btn.setTag("8"); // tags are used to determine the button's position on game board
		tic8Btn.setOnClickListener(ticBtnListener);
		
		initializeTicButtonTexts();
	}

	private void initializeTicButtonTexts() {
		tic0Btn.setText("");
		tic1Btn.setText("");
		tic2Btn.setText("");
		tic3Btn.setText("");
		tic4Btn.setText("");
		tic5Btn.setText("");
		tic6Btn.setText("");
		tic7Btn.setText("");
		tic8Btn.setText("");
	}

	protected void setMarker(int playerId, int buttonNumber) {
		gameBoard[buttonNumber % 3][buttonNumber/3] = playerId;
	}

	protected void checkForRow() {
		/* Simply check if one player achieved a row by adding up the entries on the game board for every row, column and the diagonals.
		 * 
		 * This could be optimized by just calculating the row, column and (if necessary) diagonal containing the changed value and just for
		 * the current player.
		 */
		
		// rows and columns
		for (int i = 0; i < 3; i++){
			int sumRow = 0;
			int sumColumn = 0;
			for (int j = 0; j < 3; j++){
				sumRow += gameBoard[j][i];
				sumColumn += gameBoard[i][j];
			}
			if (sumRow == 3 || sumColumn == 3){ // 1+1+1=3 -> player 1 wins
				endGame(1);
				return;
			} else if (sumRow == 12 || sumColumn == 12){ // 4+4+4=12 -> player 2 wins
				endGame(2);
				return;
			}
		}
		
		// diagonals
		int diagonal1 = gameBoard[0][0] + gameBoard[1][1] + gameBoard[2][2];
		int diagonal2 = gameBoard[0][2] + gameBoard[1][1] + gameBoard[2][0];
		if (diagonal1 == 3 || diagonal2 == 3){
			endGame(1);
			return;
		} else if (diagonal1 == 12 || diagonal2 == 12){
			endGame(2);
			return;
		} else if (roundCounter == 9) {
			endGame(3);
		}
		
	}

	private void endGame(int winnerId) {
		if (winnerId != 3){
			makeAndShowToast("Player " + winnerId + " wins!", Toast.LENGTH_SHORT);
		} else {
			makeAndShowToast("Stalemate!", Toast.LENGTH_SHORT);
		}
		initializeTicButtonTexts();
		initializeGameBoard();
		roundCounter = 0;
	}

	protected void makeAndShowToast(final String text, final int length) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), text, length).show();
			}
		});
	}

	protected void sendPingBack(PingBean p) {
		PingBean ping = new PingBean(p.getMessage(), p.getCounter() + 1);
		
		try {
			ping.setFrom(iXMPPService.getUsername());
			ping.setTo(p.getFrom());
			iXMPPService.sendIQ(new Messenger(ackHandler), new Messenger(pingIQResultHandler), 1, Parceller.getInstance().convertXMPPBeanToIQ(ping, true));
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private void registerBeanPrototypes() {
		PingBean pingPrototype = new PingBean();
		Parceller.getInstance().registerXMPPBean(pingPrototype);
		ButtonBean buttonPrototype = new ButtonBean();
		Parceller.getInstance().registerXMPPBean(buttonPrototype);
	}

	private void startXMPPPrefs() {
		Intent i = new Intent(ConstMXA.INTENT_PREFERENCES);
		this.startActivity(Intent.createChooser(i, "MXA not found. Please install."));
	}
	
	private void connectToServiceAndServer() {
		mConnectMessenger = new Messenger(xmppResultHandler);
		mMXAListener = new MXAListener() {
			
			@Override
			public void onMXADisconnected() {
				Toast.makeText(getApplicationContext(), "Disconnected from MXA Remote Service", Toast.LENGTH_SHORT).show();
				Log.i(TAG, "Disconnected from MXA Remote Service");
			}
			
			@Override
			public void onMXAConnected() {
				Toast.makeText(getApplicationContext(), "Connected to MXA Remote Service", Toast.LENGTH_SHORT).show();
				Log.i(TAG, "Connected to MXA Remote Service");
				iXMPPService = mMXAController.getXMPPService();
				try {
					iXMPPService.connect(mConnectMessenger);
					Log.i(TAG, "MXA Remote service successfully connected to XMPP Server");
					registerPingIQCallback();
					registerButtonIQCallback();
					
					// set appropriate to and from fields (TODO: get 'to' field from roster)
					
					/* TODO: throws NPE when trying to obtain username parcel from 
					 * service if MXAManager(!) is not yet running and connected -
					 * maybe we don't have the username at this moment, but have to 
					 * wait a little longer until the service has obtained it from the server?
					 */
					String username = iXMPPService.getUsername();
					
					fromAccount = username;
					if (fromAccount.equals(wmpUser1)){
						toAccount = wmpUser2;
					} else {
						toAccount = wmpUser1;
					}
					makeAndShowToast("Using account " + fromAccount, Toast.LENGTH_SHORT);
					
					sendPingBtn.setEnabled(true);
					testBtn.setEnabled(true);
					enableTicButtons();
				} catch(RemoteException e) {
					Log.e(TAG, "MXA Remote service couldn't connect to XMPP Server");
				}
			}
		};
		mMXAController = MXAController.get();
		mMXAController.connectMXA(getApplicationContext(), mMXAListener);
	}
	
	protected void enableTicButtons() {
		tic0Btn.setEnabled(true);
		tic1Btn.setEnabled(true);
		tic2Btn.setEnabled(true);
		tic3Btn.setEnabled(true);
		tic4Btn.setEnabled(true);
		tic5Btn.setEnabled(true);
		tic6Btn.setEnabled(true);
		tic7Btn.setEnabled(true);
		tic8Btn.setEnabled(true);
	}

	protected void registerButtonIQCallback() {
		try {
			iXMPPService.registerIQCallback(buttonIQCallback, ButtonBean.CHILD_ELEMENT, ButtonBean.NAMESPACE);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void registerPingIQCallback() {
		try {
			iXMPPService.registerIQCallback(pingIQCallback, PingBean.CHILD_ELEMENT, PingBean.NAMESPACE);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private void sendPingIQ() {
		PingBean ping = new PingBean("Test", 0);
		try {
			ping.setFrom(fromAccount);
			ping.setTo(toAccount);
			iXMPPService.sendIQ(new Messenger(ackHandler), new Messenger(pingIQResultHandler), 1, Parceller.getInstance().convertXMPPBeanToIQ(ping, true));
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
//		XMPPIQ iq = new XMPPIQ();
//		try {
//			iq.from = "desire";//Scheint auch so zu funktionieren, ansonsten wie unten
//			iq.to = "x10";//TODO: "x10@sven-laptop/c4d10414" <- Resource fest setzen? Ansonsten direkt von Server (http://127.0.0.1:9090/session-summary.jsp) auslesen
//			iq.type = XMPPIQ.TYPE_GET;
//			iq.element = "query";
//			iq.namespace = "wmp:iq:ping";
//			iq.payload = "<Ping id=\"1\"><message>Test</message></Ping>";
//			iXMPPService.sendIQ(new Messenger(ackHandler), new Messenger(pingIQResultHandler), 1, iq);
//		} catch(RemoteException e){
//			e.printStackTrace();
//		}
	}
	
	private void sendButtonTestIQ() {
		ButtonBean btn = new ButtonBean("main_button_test");
		try {
			btn.setFrom(fromAccount);
			btn.setTo(toAccount);
			iXMPPService.sendIQ(new Messenger(ackHandler), new Messenger(btnIQResultHandler), 1, Parceller.getInstance().convertXMPPBeanToIQ(btn, true));
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private void sendButtonIQ(String button_res_id_string) {
		ButtonBean btn = new ButtonBean(button_res_id_string);
		try {
			btn.setFrom(fromAccount);
			btn.setTo(toAccount);
			iXMPPService.sendIQ(new Messenger(ackHandler), new Messenger(btnIQResultHandler), 1, Parceller.getInstance().convertXMPPBeanToIQ(btn, true));
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private String createXMPPAddress(String username) {
		return username + "@" + xmppService + "/" + xmppResource;
	}
}