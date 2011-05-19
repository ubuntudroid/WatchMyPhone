package de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model;

/**
 * The Class GeoPosition. Used by several XMPPBeans of the LocPairs project.
 * 
 * @author Reik Mueller
 * @author Norbert Harder
 */
public class GeoPosition {

	public static final String LONGITUDE = "longitude";
	public static final String ALTITUDE = "altitude";
	public static final String LATITUDE = "latitude";
	private double latitude;
	private double longitude;
	private double altitude;

	/**
	 * Instantiates a new geo position.
	 * 
	 * @param latitude
	 *            the latitude
	 * @param longitude
	 *            the longitude
	 * @param altitude
	 *            the altitude
	 */
	public GeoPosition(double latitude, double longitude, double altitude) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
	}

	/**
	 * To xml. Returns the coordinate in xml style.
	 * 
	 * @return the string
	 */
	public String toXML() {
		String result = "<latitude>" + latitude + "</latitude>";
		result = result + "<longitude>" + longitude + "</longitude>";
		result = result + "<altitude>" + altitude + "</altitude>";
		return result;
	}

	@Override
	public String toString() {
		return "GeoPos: Latitude: " + String.valueOf(latitude) + " Longitude: "
				+ String.valueOf(longitude) + " Altitude: "
				+ String.valueOf(altitude);
	}

	/**
	 * Gets the latitude.
	 * 
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * Sets the latitude.
	 * 
	 * @param latitude
	 *            the new latitude
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * Gets the longitude.
	 * 
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * Sets the longitude.
	 * 
	 * @param longitude
	 *            the new longitude
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * Gets the altitude.
	 * 
	 * @return the altitude
	 */
	public double getAltitude() {
		return altitude;
	}

	/**
	 * Sets the altitude.
	 * 
	 * @param altitude
	 *            the new altitude
	 */
	public void setAltitude(double altitude) {
		this.altitude = altitude;

	}

}
