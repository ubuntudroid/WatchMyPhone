package edu.bonn.cs.wmp;

import java.util.HashMap;

import android.view.View;
import edu.bonn.cs.wmp.views.WMPView;

/**
 * All WMPViews have to register themselves at this registry singleton by submitting their View object
 * alongside their R.id. By crawling the HashMap it is possible to gain access to any WMPView
 * just by knowing it's R.id.. Because View-Objects may be really expensive, make sure to remove
 * them from the registry when they are not longer needed ({@link View#onDetachedFromWindow()} may be a good indicator)! Remember, that a WMPView is no Android View object!
 * You have to cast it to the specific WMPView implementation (which is a Android View) to gain access to View specific
 * methods.
 * 
 * @author Sven Bendel
 *
 */
public class WMPViewRegistry {
	
	private static WMPViewRegistry instance;
	private final static String TAG = WMPViewRegistry.class.getSimpleName();

	private HashMap<Integer,WMPView> views = new HashMap<Integer,WMPView>();
	
	public static WMPViewRegistry getInstance() {
		if (instance == null) {
			instance = new WMPViewRegistry();
		}
		return instance;
	}
	
	/**
	 * This method submits a {@link WMPView} to the registry. The WMPView
	 * will remain accessible by it's Android resource ID.
	 * @param view
	 * 				the {@link WMPView} to be submitted to the registry
	 * @param resId
	 * 				the Android resource ID which will be the key of the view in the registry
	 */
	public void addWMPView(WMPView view, int resId) {
		views.put(resId, view);
	}
	
	/**
	 * Convenience method for {@link WMPViewRegistry#addWMPView(WMPView, int)}.
	 * It will automatically gain the Android resource ID from the View.
	 * 
	 * @param view
	 * 				the {@link WMPView} to be submitted to the registry
	 * @throws ClassCastException
	 * 				is thrown if the submitted View is not an instance of {@link WMPView}
	 * @see WMPView
	 */
	public void addWMPView(View view) throws ClassCastException {
		views.put(view.getId(), (WMPView) view);
	}
	
	/**
	 * Removes the View with the specified resource ID from the registry.
	 * 
	 * @param resId
	 * 				the resource ID of the {@link WMPView} to be removed from the registry
	 */
	public void removeWMPView(int resId) {
		views.remove(resId);
	}
	
	/**
	 * Returns the {@link WMPView} object for the given resource ID.
	 * 
	 * @param resId
	 * 				resource ID
	 * @return
	 * 				the {@link WMPView} for the given resource ID 
	 */
	public WMPView findWMPView(int resId) {
		return views.get(resId);
	}
}
