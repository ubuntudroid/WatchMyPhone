package edu.bonn.cs.wmp;

import de.tudresden.inf.rn.mobilis.mxa.services.collabedit.ICollabEditingService;
import edu.bonn.cs.wmp.application.WMPApplication;

/**
 * This interface needs to be implemented by everyone who needs to get informed about
 * the MXA-WMP connection status via the {@link WMPApplication} Object.
 * 
 * Note, that there is no need to implement this interface for every activity in your
 * application - just for that one which wants to make the actual call to
 * {@link WMPApplication#connectToServiceAndServer(WMPConnectionListener)}. All other 
 * classes may just get this information by calling {@link WMPApplication#isCollabEditingServiceConnected()}
 * in addition to the connection related methods from the current {@link ICollabEditingService}.
 * However, these classes don't get actively notified about state changes.
 * 
 * An example of how to implement the interface may be found in {@link WMPActivity}.
 * 
 * @author Sven Bendel
 *
 */
public interface WMPConnectionListener {
	/**
	 * This method is called by {@link WMPApplication} when
	 * it has successfully connected to the MXA Service and the
	 * Collaborative Editing Server.  
	 */
	public void onConnected();
	
	/**
	 * This method is called by {@link WMPApplication} when
	 * it was disconnected from the MXA Service and therefore from
	 * the Collaborative Editing Server.  
	 */
	public void onDisconnected();
}
