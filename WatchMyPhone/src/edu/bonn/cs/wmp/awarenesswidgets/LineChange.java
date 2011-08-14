package edu.bonn.cs.wmp.awarenesswidgets;

import java.util.Map;

/*
 * TODO: handle removed lines, i.e. changed document size, as this way the RadarViews
 * lineLengths HashMap keeps on increasing forever.
 */

/**
 * This content change type shall be used for propagating line lenght changes from a
 * WMPEditText to a RadarView.
 * 
 * The {@link LineChange#line} attribute describes the number of the line whose lenght has 
 * changed. The lenght of the line ({@link LineChange#lineLength}) may be one of three sizes defined by the enum
 * {@link LineLength}.
 * @author Sven Bendel
 *
 */
public class LineChange extends ContentChange {
	/**
	 * Defines the possible values for the <code>lineLenght</code> attribute,
	 * where <code>FULL</code> means a line of max length, and <code>EMPTY</code>
	 * indicates an empty length. It is adviced to use <code>EMPTY</code> just for
	 * really empty lines and <code>FULL</code> just for completely filled (these
	 * are also lines with auto line-breaks) lines. The rest is just <code>HALF</code>.
	 * This abstraction helps saving calculation time, as just really relevant changes
	 * should trigger ContentChange events.
	 * @author Sven Bendel
	 * @see LineChange#lineLength
	 *
	 */
	public enum LineLength {FULL, HALF, EMPTY};
	
	/**
	 * The number of the line whose lenght has changed starting with 0.
	 */
	public int line;
	/**
	 * The new length of the changed line. May be one of the values defined in {@link LineLength}.
	 */
	public LineLength lineLength;
	
	public float[] lineLengths;
	
	/**
	 * The maximum line length of the lines stored in lineLengths. If possible, try to determine
	 * and set this value when filling the lineLengths as this doesn't consume as much cpu time
	 * as if the value has to be calculated afterwards. The method {@link LineChange#getMaxLineLength()}
	 * will return the value of maxLength if it has been set before. If not, it will calculate
	 * the max value. Therefore you should never read this value directly, but call {@link LineChange#getMaxLineLength()}.
	 */
	public float maxLength = -1;
	
	public float getMaxLineLength() {
		if (maxLength == -1) {
			int arraySize = lineLengths.length;
			for (int i = 0; i < arraySize; i++) {
				if (lineLengths[i] > maxLength) {
					maxLength = lineLengths[i];
				}
			}
			return maxLength;
		} else {
			return maxLength;
		}
	}
}
