package edu.bonn.cs.wmp.awarenesswidgets;

import edu.bonn.cs.wmp.views.WMPView;

/**
 * Super class for all types of content changes. Content changes
 * are exchanged between {@link WMPView}s and their corresponding
 * {@link WMPAwarenessWidget}s and sometimes - e.g. in case of the {@link ViewportChange}
 * in the form of beans - even between clients. 
 * 
 * @author Sven Bendel
 *
 */
public abstract class ContentChange {
	public String user = USER_ME;
	
	public final static String USER_ME = "me";
}
