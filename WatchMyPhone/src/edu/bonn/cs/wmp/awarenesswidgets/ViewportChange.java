package edu.bonn.cs.wmp.awarenesswidgets;

/**
 * This change type is used to tell a {@link RadarView} about the new viewport position of a
 * specific user. <code>top</code> is the ratio of the top line of the viewport to the 
 * total number of lines (i.e. # current line/total lines), while <code>bottom</code> is the
 * corresponding bottom line ratio.
 * @author Sven Bendel
 *
 */

public class ViewportChange extends ContentChange {
	public float top;
	public float bottom;
}
