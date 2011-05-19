package de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class NetworkFingerPrint. Used by several XMPPBeans of the LocPairs project.
 * 
 * @author Reik Mueller
 * @author Norbert Harder
 */
/**
 * @author Schaf
 *
 */
/**
 * @author Schaf
 * 
 */
public class NetworkFingerPrint {
	
	private GeoPosition position = null;
	private Map<String, Integer> networkFingerPrint = new HashMap<String, Integer>();

	/**
	 * Adds the finger print.
	 * 
	 * @param bssid
	 *            the bssid of the network supply.
	 * @param signalStrength
	 *            the signal strength
	 */
	public void addFingerPrint(String bssid, Integer signalStrength) {
		networkFingerPrint.put(bssid, signalStrength);
	}

	/**
	 * Gets the network finger print.
	 * 
	 * @return the network finger print
	 */
	public Map<String, Integer> getNetworkFingerPrint() {
		return networkFingerPrint;
	}

	/**
	 * Sets the network finger print.
	 * 
	 * @param networkFingerPrint
	 *            the network finger print
	 */
	public void setNetworkFingerPrint(Map<String, Integer> networkFingerPrint) {
		this.networkFingerPrint = networkFingerPrint;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return networkFingerPrint.toString();
	}

	public GeoPosition getPosition() {
		return position;
	}

	public void setPosition(GeoPosition position) {
		this.position = position;
	}
	
}