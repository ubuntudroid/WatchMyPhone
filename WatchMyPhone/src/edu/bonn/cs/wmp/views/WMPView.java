package edu.bonn.cs.wmp.views;

import edu.bonn.cs.wmp.awarenesswidgets.WMPAwarenessWidget;
import edu.bonn.cs.wmp.awarenesswidgets.ContentChange;

/**
 * This interface has to be implemented by all WMPViews.
 * 
 * @author Sven Bendel *
 */
public interface WMPView {
	public void addWidget(WMPAwarenessWidget w);

	public boolean removeWidget(WMPAwarenessWidget w);

	public void notifyExternalWMPWidgetsOfContentChange(ContentChange c);

}
