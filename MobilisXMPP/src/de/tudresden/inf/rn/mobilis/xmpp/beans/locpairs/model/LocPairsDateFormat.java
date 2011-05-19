package de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model;

import java.text.SimpleDateFormat;

/**
 * The Class LocPairsDateFormat. Here the format used in the LocPairs XMPP-Beans
 * and the corresponding IQs is defined and can be maintained.
 * 
 * @author Reik Mueller
 */
public class LocPairsDateFormat {
	private static final SimpleDateFormat format = new SimpleDateFormat(
			"yyyy-MM-dd kk:mm:ss");

	/**
	 * Gets the format.
	 * 
	 * @return the format
	 */
	public static SimpleDateFormat getFormat() {
		return format;
	}

}
