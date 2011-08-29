package edu.bonn.cs.wmp.awarenesswidgets;

/**
 * This interface defines all methods necessary for any WMP awareness widget.
 * Don't get confused with the class AwarenessWidget from the CEFX project which 
 * is used as a super-class for view updaters. This class shall be used as a hub
 * between CEFX and WMP whereas the WMPAwarenessWidget implementations get their
 * changes directly from the WMPViews which enables them to also get notified about
 * changes not being managed by CEFX like viewports.
 * 
 * TODO: find a better name for the AwarenessWidget interface from the CEFX project
 * 
 * @author Sven Bendel
 *
 */
public interface WMPAwarenessWidget {
	public void onViewContentChange(ContentChange c);

	public boolean isInterestedIn(Class<? extends ContentChange> changeClass);

}
