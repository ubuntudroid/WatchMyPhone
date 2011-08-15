package edu.bonn.cs.wmp.awarenesswidgets;

/**
 * This change type is used to tell a RadarView about the new viewport position of a
 * specific user. <code>top</code> is number of the top line of the viewport, while <code>bottom</code> is the
 * bottom line number.
 * @author Sven Bendel
 *
 */

// TODO: make this more generic by using the offset instead of the line number
public class ViewportChange extends ContentChange {
	public int top;
	public int bottom;
}
