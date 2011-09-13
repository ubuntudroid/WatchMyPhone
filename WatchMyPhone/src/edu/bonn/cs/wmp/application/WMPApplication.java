package edu.bonn.cs.wmp.application;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
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
	
	@Override
	public void onCreate() {
		instance = this;
		super.onCreate();
	}
	
	public void startXMPPPrefs() {
		Intent i = new Intent(ConstMXA.INTENT_PREFERENCES);
		this.startActivity(Intent.createChooser(i,
				"MXA not found. Please install."));
	}
	
	public ICollabEditingService getCollabEditingService() {
		return collabEditService;
	}
	
	public void leaveSession(String sessionName) {
		try {
			collabEditService.leaveSession(sessionName);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void joinSession(String sessionName) {
		try {
			collabEditService.joinSession(sessionName);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isConnected() {
		try {
			return (collabEditService != null && collabEditService.isConnected());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public static WMPApplication getInstance() {
		/*
		 * this should never be null, as the application object is the first one to be created and
		 * the last one to be destroyed
		 */
		return instance;
	}
	
	public void connectToServiceAndServer(final WMPConnectionListener listener) {
		Handler xmppResultHandler = new Handler() {

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
					
					listener.onConnected();
			};

		};
		registerBeanPrototypes();
		final Messenger mConnectMessenger = new Messenger(xmppResultHandler);
		MXAController mxaController = MXAController.get();
		MXAListener mMXAListener = new MXAListener() {

			@Override
			public void onMXADisconnected() {
				Log.i(TAG, "Disconnected from MXA Remote Service");
				listener.onDisconnected();
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
				} catch (RemoteException e) {
					Log.e(TAG,
							"MXA Remote service couldn't connect to XMPP Server");
					restartApp();
				}
			}
		};
		mxaController.connectMXA(WMPApplication.getInstance().getApplicationContext(), mMXAListener);
	}
	
	private void notifyViewUpdatersOfAwarenessEvent(
			AwarenessEvent event) {
		for (ViewUpdater v : viewUpdaters) {
			if (v.hasInterestIn(event)) {
				v.notifyOfAwarenessEvent(event);
			}
		}
	}
	
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
	
	public void registerViewUpdater(ViewUpdater v) {
		viewUpdaters.add(v);
	}

	public void deregisterViewUpdater(ViewUpdater v) {
		viewUpdaters.remove(v);
	}

	public void deregisterCollabEditingCallback() {
		try {
			collabEditService.deregisterCollabEditingCallback(collabEditingCallback);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
