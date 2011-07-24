package edu.bonn.cs.wmp.awarenesswidgets;

public interface WMPAwarenessWidget {
	public void onViewContentChange(ContentChange c);

	public boolean isInterestedIn(Class<? extends ContentChange> changeClass);

}
