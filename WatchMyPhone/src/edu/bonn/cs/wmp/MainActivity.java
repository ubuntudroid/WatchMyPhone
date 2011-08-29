package edu.bonn.cs.wmp;

import de.tudresden.inf.rn.mobilis.mxa.services.callbacks.ICollabEditingCallback;
import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import edu.bonn.cs.wmp.application.WMPApplication;

public class MainActivity extends Activity implements WMPConnectionListener {
	private static MainActivity instance;
	private WMPApplication app;

	boolean DEBUG = false;
	private final String TAG = this.getClass().getSimpleName();


	protected void makeAndShowToast(final String text, final int length) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), text, length).show();
			}
		});
	}
	
	public static MainActivity getInstance() {
		return instance;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		app = (WMPApplication) getApplication();
		instance = this;

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
			app.startXMPPPrefs();
			break;
		case R.id.menu_join:
			app.connectToServiceAndServer(this);
			break;
		case R.id.menu_disconnect:
			try {
				app.getCollabEditingService().leaveSession("edit_text_test");
				app.deregisterCollabEditingCallback();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		try {
			if (app.getCollabEditingService().isConnected())
				app.getCollabEditingService().leaveSession("edit_text_test");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onDestroy();
	}

	@Override
	public void onConnected() {
		try {
			app.getCollabEditingService().joinSession("edit_text_test");
		} catch (RemoteException e) {
			Log.e(TAG, "Could not join session edit_text_test.");
			e.printStackTrace();
		}
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}
	
}
