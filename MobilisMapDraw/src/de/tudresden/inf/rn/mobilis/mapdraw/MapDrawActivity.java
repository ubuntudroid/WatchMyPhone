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

import org.jivesoftware.smack.XMPPConnection;

import jabberSrpc.JabberClient;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

import de.tudresden.inf.rn.mobilis.android.services.SessionService;
import de.tudresden.inf.rn.mobilis.mxa.ConstMXA;

public class MapDrawActivity extends MapActivity {
	
	//private LinearLayout linearLayout;
	private MapView mapView;

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// initiate the SessionService, managing the other services
		SessionService sessionService = SessionService.getInstance();
		// store the context for easy access for every class of this application
		Context context = getApplicationContext();
		sessionService.setContext(context);
		
		// prepare preferences for other services
		// (can be changed in the running app from the preferences menu entry)
		sessionService.setPreferences(PreferenceManager.getDefaultSharedPreferences(context));
		
		// connect to XMPP
//		XMPPController.getInstance();
		
		// construct the UI
		setContentView(R.layout.map);
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		
		// adding map drawing controls and drawing overlay
		// binding MapDrawExtension as a plugin to the central Mobilis map component will be possible
		MapDrawExtension mapDrawExtension = new MapDrawExtension(context, mapView);
		mapDrawExtension.show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// this.menu = menu;
		// Inflate the menu XML resource.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_map, menu);

		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_pref:
			Intent i = new Intent(ConstMXA.INTENT_PREFERENCES);
        	this.startActivity(Intent.createChooser(i, "MXA not found. Please install."));
			break;
		case R.id.menu_connect:
			//XMPPController.getInstance().loginToXMPP();
			SessionService.getInstance().getCollabEditingService().connect();
		}
		return false;
	}
}
