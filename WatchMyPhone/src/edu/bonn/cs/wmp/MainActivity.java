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
	
	private WMPEditText collabText;
	
	public static MainActivity getInstance() {
		return instance;
    }
	
	private Handler xmppResultHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (DEBUG) Toast.makeText(getApplicationContext(), "XMPP connected: " + msg, Toast.LENGTH_LONG).show();
		};

	};

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        instance = this;
        
        collabText = (WMPEditText) findViewById(R.id.main_edit_text_collab_text);
        
//        startXMPPPrefs();
//        registerBeanPrototypes();
//        connectToServiceAndServer();

        SessionService.getInstance().getCollabEditingService().connect();
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
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
//    		collabText.setCollabEditingService(collabService);
    	}
    	return true;
    }


	protected void makeAndShowToast(final String text, final int length) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), text, length).show();
			}
		});
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
				} catch(RemoteException e) {
					Log.e(TAG, "MXA Remote service couldn't connect to XMPP Server");
				}
			}
		};
		mMXAController = MXAController.get();
		mMXAController.connectMXA(getApplicationContext(), mMXAListener);
	}
	
}