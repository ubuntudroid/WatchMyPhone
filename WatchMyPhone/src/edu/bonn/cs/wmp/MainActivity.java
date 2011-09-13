package edu.bonn.cs.wmp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import edu.bonn.cs.wmp.WMPConnectionListener;
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
			app.leaveSession("edit_text_test");
			app.deregisterCollabEditingCallback();
			break;
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		if (app.isConnected())
			app.leaveSession("edit_text_test");
		super.onDestroy();
	}

	@Override
	public void onConnected() {
		app.joinSession("edit_text_test");
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}
	
}
