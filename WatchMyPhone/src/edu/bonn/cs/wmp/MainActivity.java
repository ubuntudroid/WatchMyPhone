package edu.bonn.cs.wmp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import edu.bonn.cs.wmp.application.WMPApplication;
import edu.bonn.cs.wmp.views.WMPEditText;

public class MainActivity extends Activity {
	private WMPEditText collabText;
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

		collabText = (WMPEditText) findViewById(R.id.main_edit_text_collab_text);

//		SessionService.getInstance().getCollabEditingService().connect();
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
			if (app.getCollabEditingService().isConnected()) {
				app.getCollabEditingService().joinSession("edit_text_test");
			} else {
				Log.e(TAG, "Could not join session edit_text_test as XMPPService was not connected.");
			}
			break;
		case R.id.menu_disconnect:
			app.getCollabEditingService().leaveSession("edit_text_test");
			break;
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		if (app.getCollabEditingService().isConnected())
			app.getCollabEditingService().leaveSession("edit_text_test");
		super.onDestroy();
	}
	
	
}
