package edu.bonn.cs.wmp.awarenesswidgets;

/*
 * TODO: handle removed lines, i.e. changed document size, as this way the RadarViews
 * lineLengths HashMap keeps on increasing forever.
 */

/**
 * This content change type shall be used for propagating line lenght changes from a
 * WMPEditText to a RadarView. The row-index of the array corresponds to the line number.
 * 
 * @author Sven Bendel
 *
 */
public class LineChange extends ContentChange {
	
	public float[] lineLengths;
	
}
