package de.tudresden.inf.rn.mobilis.xmpp.packet;

import java.util.Date;

import org.jivesoftware.smack.packet.IQ;

import de.tudresden.inf.rn.mobilis.xmpp.beans.LocationInfo;

/**
 *
 * @author Christopher, Benjamin
 */
public class LocationIQ extends IQ {
    
    public static final String elementName = "query";
    public static final String namespace = "mobilis:iq:location";
	
	private double altitude;
	private double latitude;
	private double longitude;
	private float speed;
	private String identity;
	private boolean proximity;
	private Date timestamp;
	private boolean alert;
    
    public LocationInfo getLocation() {
    	LocationInfo l = new LocationInfo();
    	l.setAltitude(altitude);
    	l.setLatitude(latitude);
    	l.setLongitude(longitude);
    	l.setSpeed(speed);
    	l.setTimestamp(timestamp);
        return l;
    }

    public void setLocation(LocationInfo location) {
    	this.altitude = location.getAltitude();
    	this.latitude = location.getLatitude();
    	this.longitude = location.getLongitude();
    	this.speed = location.getSpeed();
    	this.timestamp = location.getTimestamp();
    }
    
    public double getAltitude() {
    	return this.altitude;
    }
    
    public void setAltitude(double altitude) {
    	this.altitude = altitude;
    }
    
    public double getLatitude() {
    	return this.latitude;
    }
    
    public void setLatitude(double latitude) {
    	this.latitude = latitude;
    }
    
    public double getLongitude() {
    	return this.longitude;
    }
    
    public void setLongitude(double longitude) {
    	this.longitude = longitude;
    }
    
    public float getSpeed() {
    	return this.speed;
    }
    
    public void setSpeed(float speed) {
    	this.speed = speed;
    }
    
    public Date getTimestamp() {
    	return this.timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
    	this.timestamp = timestamp;
    }
    
    public boolean isProximity() {
    	return this.proximity;
    }

    public void setProximity(boolean proximity) {
    	this.proximity = proximity;
    }

    public String getIdentity() {
    	return this.identity;
    }
    
    public void setIdentity(String identity) {
    	this.identity = identity;
    }
    
    public boolean getAlert() {
    	return this.alert;
    }
    
    public void setAlert(boolean alert) {
    	this.alert = alert;
    }
    
    public LocationIQ() {
        super();
        this.setType(IQ.Type.SET);
    }
    
    @Override
	public String getChildElementXML() {
    	StringBuffer buf = new StringBuffer();
    	buf.append("<" + elementName + " xmlns=\"" + namespace + "\">\n");
    	
    	buf.append("<altitude>").append(this.altitude).append("</altitude>\n");
    	buf.append("<latitude>").append(this.latitude).append("</latitude>\n");
    	buf.append("<longitude>").append(this.longitude).append("</longitude>\n");
    	buf.append("<speed>").append(this.speed).append("</speed>\n");
    	buf.append("<proximity>").append(this.proximity ? "true" : "false").append("</proximity>\n");
    	buf.append("<alert>").append(this.alert ? "true" : "false").append("</alert>\n");
    	
    	if (this.identity != null) 
    		buf.append("<identity>").append(this.identity).append("</identity>\n");
    	if (this.timestamp != null)
    		buf.append("<timestamp>").append(this.timestamp.getTime()).append("</timestamp>\n");
    	    	
    	buf.append("</" + elementName + ">");
        return buf.toString();
    }
}
