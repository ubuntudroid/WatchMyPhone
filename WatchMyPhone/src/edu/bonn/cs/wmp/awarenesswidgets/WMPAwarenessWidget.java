package edu.bonn.cs.wmp.awarenesswidgets;

import edu.bonn.cs.wmp.views.WMPView;

/**
 * This interface defines all methods necessary for any {@link WMPAwarenessWidget}.
 * Don't get confused with the class AwarenessWidget get from the CEFX project which 
 * is used as a super-class for view updaters. This class shall be used as a hub
 * between CEFX and WMP whereas the WMPAwarenessWidget implementations get their
 * changes directly from the WMPViews (subject view) which enables them to also get notified about
 * changes not being managed by CEFX like viewports.
 * 
 * For an example implementation take a look at {@link RadarView}.
 * 
 * TODO: find a better name for the AwarenessWidget interface from the CEFX project
 * 
 * @author Sven Bendel
 *
 */
public interface WMPAwarenessWidget {
	/**
	 * This method is called by the subject view of this Awareness Widget when
	 * its content changes.
	 * @param c
	 * 			{@link ContentChange} incorporating informations about the changed
	 * 			state of the subject view.
	 */
	public void onViewContentChange(ContentChange c);

	/**
	 * This method is called by the subject view of this Awareness Widget when
	 * its content changes, but before the {@link ContentChange} is propagated to the
	 * Awareness Widget. We may decide if we want to get informed about this particular
	 * ContentChange by returning <code>true</code> or <code>false</code>.
	 * @param c
	 * 			{@link ContentChange} incorporating informations about the changed
	 * 			state of the subject view.
	 */
	public boolean isInterestedIn(Class<? extends ContentChange> changeClass);
	
	/**
	 * This method allows the manual setting/changing of the
	 * view this Awareness Widget receives it's {@link ContentChange}
	 * objects from. Normally this should be something simple like <code>this.subject = (View) v; invalidate();</code>
	 * @param v
	 * 			The {@link WMPView} which will eventually all ContentChanges
	 * 			to this awareness widget.
	 */
	public void setSubjectView(WMPView v);

}
