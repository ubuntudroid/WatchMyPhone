package de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.model;

public class LocationInfo {
	
	public String Jid;
	public int Latitude = -1;
	public int Longitude = -1;

	public LocationInfo() {}
	
	public LocationInfo(String jid, int lat, int lon) {
		this.Jid = jid;
		this.Latitude = lat;
		this.Longitude = lon;
	}
	
	public LocationInfo copy(){
		return new LocationInfo(Jid, Latitude, Longitude);
	}
	
	public String toString(){
		return "LocationInfo: ["
			+ "Jid=" + Jid
			+ "; Latitude=" + Latitude
			+ "; Longitude=" + Longitude + "]";
	}
}
