package edu.bonn.cs.wmp.application;

import android.app.Application;
import android.content.Intent;
import de.tudresden.inf.rn.mobilis.mxa.ConstMXA;
import edu.bonn.cs.wmp.service.CollabEditingService;

public class WMPApplication extends Application {

	private static WMPApplication instance;

	private CollabEditingService collabEditService;

	private String TAG = this.getClass().getSimpleName();
	
	@Override
	public void onCreate() {
		instance = this;
		super.onCreate();
		// initialize all stuff needed for WMP
		collabEditService = new CollabEditingService(this);
	}
	
	public void startXMPPPrefs() {
		Intent i = new Intent(ConstMXA.INTENT_PREFERENCES);
		this.startActivity(Intent.createChooser(i,
				"MXA not found. Please install."));
	}
	
	public CollabEditingService getCollabEditingService() {
		return collabEditService;
	}
	
	public static WMPApplication getInstance() {
		// this should never be null, as the application object is the first one to be created
		return instance;
	}

}
