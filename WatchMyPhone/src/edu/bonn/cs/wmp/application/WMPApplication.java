package edu.bonn.cs.wmp.application;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import de.hdm.cefx.awareness.AwarenessEvent;
import de.tudresden.inf.rn.mobilis.media.parcelables.ParcelableAwarenessEvent;
import de.tudresden.inf.rn.mobilis.mxa.ConstMXA;
import de.tudresden.inf.rn.mobilis.mxa.IXMPPService;
import de.tudresden.inf.rn.mobilis.mxa.MXAController;
import de.tudresden.inf.rn.mobilis.mxa.MXAListener;
import de.tudresden.inf.rn.mobilis.mxa.callbacks.IXMPPIQCallback;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPIQ;
import de.tudresden.inf.rn.mobilis.mxa.services.callbacks.ICollabEditingCallback;
import de.tudresden.inf.rn.mobilis.mxa.services.collabedit.ICollabEditingService;
import de.tudresden.inf.rn.mobilis.xmpp.android.Parceller;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.collabedit.AwarenessEventBean;
import edu.bonn.cs.wmp.WMPConnectionListener;
import edu.bonn.cs.wmp.WMPViewRegistry;
import edu.bonn.cs.wmp.awarenesswidgets.ViewportChange;
import edu.bonn.cs.wmp.views.WMPView;
import edu.bonn.cs.wmp.viewupdater.ViewUpdater;
import edu.bonn.cs.wmp.xmpp.beans.ViewportBean;

public class WMPApplication extends Application {

	private static WMPApplication instance;

	private ICollabEditingService collabEditService;
	private IXMPPService xmppService;
	private List<ViewUpdater> viewUpdaters = new ArrayList<ViewUpdater>();
	private ICollabEditingCallback collabEditingCallback;

	private String TAG = this.getClass().getSimpleName();

	private boolean connected;
	
	@Override
	public void onCreate() {
		instance = this;
		super.onCreate();
	}
	
	/**
	 * Opens up the XMPP preferences Activity from MXA.
	 */
	public void startXMPPPrefs() {
		Intent i = new Intent(ConstMXA.INTENT_PREFERENCES);
		this.startActivity(Intent.createChooser(i,
				"MXA not found. Please install."));
	}
	
	/**
	 * Supplies the MXA CollabEditingService object.
	 * @return
	 * 			{@link ICollabEditingService}
	 */
	public ICollabEditingService getCollabEditingService() {
		return collabEditService;
	}
	
	/**
	 * Disconnects from the given Collaborative Editing session.
	 * @param sessionName
	 * 						The name of the session to be disconnected from.
	 */
	public void leaveSession(String sessionName) {
		/*
		 * TODO: as we probably will only be in one session per app we could simply store
		 * the session name and provide this method without any additional parameters.
		 */
		
		try {
			collabEditService.leaveSession(sessionName);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Connects to the given Collaborative Editing session.
	 * @param sessionName
	 * 						The name of the session to connect to.
	 */
	public void joinSession(String sessionName) {
		try {
			collabEditService.joinSession(sessionName);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Tells whether we the CollabEditingService is connected to the Collaborative
	 * Editing Framework's server.
	 * @return <code>true</code> if the CollabEditingService is connected to the server,
	 * 		<code>false</code> otherwise.
	 */
	public boolean isCollabEditingServiceConnected() {
		try {
			return (collabEditService != null && collabEditService.isConnected());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Supplies the current instance of the WMPApplication class.
	 * @return
	 * 			the current instance of WMPApplication
	 */
	public static WMPApplication getInstance() {
		/*
		 * this should never be null, as the application object is the first one to be created and
		 * the last one to be destroyed
		 */
		return instance;
	}
	
	/**
	 * This method handles all necessary steps to connect to the MXA service and the Collaborative
	 * Editing Service. The calling class should implement the {@link WMPConnectionListener} interface
	 * and pass itself as a parameter to get notified when the connection is up and running and when
	 * it is teared down again. Additionally one may use the {@link #isCollabEditingServiceConnected()}
	 * method in combination with the ICollabEditingService object's connection methods to get this information.
	 * @param listener
	 * 			Implementation of the WMPConnectionListener interface, which is notified about
	 * 			connection state changes. 
	 */
	public void connectToServiceAndServer(final WMPConnectionListener listener) {
		Handler connectionHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
					Log.i(TAG, "XMPP connected: " + msg);
					try {
						collabEditService = xmppService.getCollabEditingService();
					} catch (RemoteException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					IXMPPIQCallback viewportCallback = new IXMPPIQCallback.Stub() {
						
						@Override
						public void processIQ(XMPPIQ iq) throws RemoteException {
							XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(iq);
							if (b instanceof ViewportBean) {
								if (b.getType() == XMPPBean.TYPE_SET) {
									ViewportBean vb = (ViewportBean) b;
									int resId = vb.getWmpId();
									String user = vb.getFrom();
									float viewportStart = vb.getViewportStart();
									float viewportEnd = vb.getViewportEnd();
									
									ViewportChange c = new ViewportChange();
									c.user = user;
									c.top = viewportStart;
									c.bottom = viewportEnd;
									
									WMPView view = WMPViewRegistry.getInstance().findWMPView(resId);
									if (view != null) {
										view.notifyExternalWMPWidgetsOfContentChange(c);
									}
								} else if (b.getType() == XMPPBean.TYPE_ERROR) {
									// this is not defined atm!
								}
							}
						}
					};
					
					IXMPPIQCallback awarenessEventCallback = new IXMPPIQCallback.Stub() {
						
						@Override
						public void processIQ(XMPPIQ iq) throws RemoteException {
							XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(iq);
							if (b instanceof AwarenessEventBean) {
								if (b.getType() == XMPPBean.TYPE_SET) {
									AwarenessEventBean ab = (AwarenessEventBean) b;
									AwarenessEvent event = (AwarenessEvent) ab.getEvent();
									notifyViewUpdatersOfAwarenessEvent(event);
								} else if (b.getType() == XMPPBean.TYPE_ERROR) {
									// this is not defined atm!
								}
							}
						}
					};
					collabEditingCallback = new ICollabEditingCallback.Stub() {
						
						@Override
						public void onAwarenessEventReceived(ParcelableAwarenessEvent event)
								throws RemoteException {
							notifyViewUpdatersOfAwarenessEvent(event);
						}
					};
					try {
						xmppService.registerIQCallback(viewportCallback, ViewportBean.CHILD_ELEMENT, ViewportBean.NAMESPACE);
						xmppService.registerIQCallback(awarenessEventCallback, AwarenessEventBean.CHILD_ELEMENT, AwarenessEventBean.NAMESPACE);
						collabEditService.registerCollabEditingCallback(collabEditingCallback);
						
					} catch (RemoteException e) {
						Log.e(TAG, "Couldn't register IQ-callback for ViewportBeans!");
						e.printStackTrace();
					}
					
					// inform attached activity
					if (listener != null)
						listener.onConnected();
					
					// initialize needed CEFX nodes
					for (ViewUpdater v : viewUpdaters) {
						String wmpName = v.getWMPView().getWMPName();
						try {
							collabEditService.createNewTopLevelNode(wmpName);
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
			};

		};
		registerBeanPrototypes();
		final Messenger mConnectMessenger = new Messenger(connectionHandler);
		MXAController mxaController = MXAController.get();
		MXAListener mMXAListener = new MXAListener() {

			@Override
			public void onMXADisconnected() {
				Log.i(TAG, "Disconnected from MXA Remote Service");
				if (listener != null)
					listener.onDisconnected();
				connected = false;
			}

			@Override
			public void onMXAConnected() {
				Log.i(TAG, "Connected to MXA Remote Service");
				Toast.makeText(WMPApplication.getInstance().getApplicationContext(), "Connected to MXA Remote Service", Toast.LENGTH_SHORT);
				xmppService = MXAController.get().getXMPPService();
				try {
					xmppService.connect(mConnectMessenger);
//					if (xmppService.isConnected()) {
//						
//					} else {
//						Toast.makeText(WMPApplication.getInstance().getApplicationContext(), "Couldn't connect to CEFX - restarting...", Toast.LENGTH_SHORT).show();
//						restartApp();
//					}
					connected = true;
				} catch (RemoteException e) {
					Log.e(TAG,
							"MXA Remote service couldn't connect to XMPP Server");
					restartApp();
				}
			}
		};
		mxaController.connectMXA(WMPApplication.getInstance().getApplicationContext(), mMXAListener);
	}
	
	/**
	 * This method notifies all registered {@link ViewUpdater}s about new
	 * {@link AwarenessEvent}s.
	 * @param event
	 * 			AwarenessEvent to be propagated to registered view updaters
	 */
	private void notifyViewUpdatersOfAwarenessEvent(AwarenessEvent event) {
		for (ViewUpdater v : viewUpdaters) {
			if (v.hasInterestIn(event)) {
				v.notifyOfAwarenessEvent(event);
			}
		}
	}
	
	/**
	 * Registers all necessary bean prototypes at the XMPP-{@link Parceller}.
	 */
	protected void registerBeanPrototypes() {
		// TODO: add additionally needed packet types here (Chat, Buddy-List etc.)
		ViewportBean viewportBeanPrototype = new ViewportBean(0);
		Parceller.getInstance().registerXMPPBean(viewportBeanPrototype);
		AwarenessEventBean awarenessEventBeanPrototype = new AwarenessEventBean();
		Parceller.getInstance().registerXMPPBean(awarenessEventBeanPrototype);
	}
	
	/*
	 * TODO: this is just a quick fix for the prototype - remove all references to this method as well
	 * as the method itself as soon as the connection quirks are solved!
	 */
	private void restartApp() {
		Intent i;
		i = WMPApplication
				.getInstance()
				.getBaseContext()
				.getPackageManager()
				.getLaunchIntentForPackage(
						WMPApplication.getInstance().getBaseContext()
								.getPackageName());
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		WMPApplication.getInstance().getApplicationContext()
				.startActivity(i);
	}
	
	/**
	 * Registers the given {@link ViewUpdater}, so that it gets notified on the
	 * arrival of new {@link AwarenessEvent}s.
	 * @param v
	 * 			ViewUpdater to be eventually notified about AwarenessEvents 
	 */
	public void registerViewUpdater(ViewUpdater v) {
		viewUpdaters.add(v);
		if (connected) {
			String wmpName = v.getWMPView().getWMPName();
			try {
				collabEditService.createNewTopLevelNode(wmpName);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void disconnectFromMXA() {
		if (connected) {
			leaveSession("edit_text_test");
			deregisterCollabEditingCallback();
			/*
			 * TODO: add the following line or alternatively 
			 * MXAController.get().disconnect(); when implemented by the Mobilis developers
			 */
//			unbindService(MXAController.get());

//			collabEditService = null;
			connected = false;
		}
		
	}
	
	/**
	 * De-registers the given {@link ViewUpdater}, so that it gets notified on the
	 * arrival of new {@link AwarenessEvent}s.
	 * @param v
	 * 			ViewUpdater which should not be notified about AwarenessEvents
	 * 			any more 
	 */
	public void deregisterViewUpdater(ViewUpdater v) {
		viewUpdaters.remove(v);
	}

	/**
	 * This method disconnects the app from the MXA's CollabEditingService. You should
	 * call this after disconnection from a Collaborative Editing Session if it is obvious,
	 * that the service isn't needed any more in the near future as this will save some
	 * resources.
	 */
	public void deregisterCollabEditingCallback() {
		try {
			collabEditService.deregisterCollabEditingCallback(collabEditingCallback);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isConnectedToMXA() {
		return connected;
	}
}
