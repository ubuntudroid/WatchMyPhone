/**
 * Copyright (C) 2010 Technische Universität Dresden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: http://mobilisplatform.sourceforge.net
 */

package de.tudresden.inf.rn.mobilis.xmpp.beans.context;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo;

/**
 * Class for exchanging information about the user's
 * location. Based on XEP-0080 - User Location.
 * @author Robert Lübke
 */
public class UserLocationInfo implements XMPPInfo, Cloneable {

	private static final long serialVersionUID = 1L;
	public static final String CHILD_ELEMENT = "geoloc";
	public static final String NAMESPACE = "http://jabber.org/protocol/geoloc";

	private double accuracy;
	private double alt;
	private String area;
	private double bearing;
	private String building;
	private String country;
	private String countrycode;
	private String datum;
	private String description;
	private double error;
	private String floor;
	private double lat;
	private String locality;
	private double lon;
	private String postalcode;
	private String region;
	private String room;
	private double speed;
	private String street;
	private String text;
	private String timestamp;
	private String uri;	
	
	/** Constructor for an empty User Location Item	 */
	public UserLocationInfo() {
		initializeNumbers();
	}
	
	private void initializeNumbers() {
		this.accuracy = Double.MIN_VALUE;
		this.alt = Double.MIN_VALUE;
		this.bearing = Double.MIN_VALUE;
		this.error = Double.MIN_VALUE;
		this.lat = Double.MIN_VALUE;
		this.lon = Double.MIN_VALUE;
		this.speed = Double.MIN_VALUE;	
	}

	@Override
	public UserLocationInfo clone() {
		UserLocationInfo twin = new UserLocationInfo();
		
		twin.accuracy=this.accuracy;
		twin.alt=this.alt;
		twin.area=this.area;
		twin.bearing=this.bearing;
		twin.building=this.building;
		twin.country=this.country;
		twin.countrycode=this.countrycode;
		twin.datum=this.datum;
		twin.description=this.description;
		twin.error=this.error;
		twin.floor=this.floor;
		twin.lat=this.lat;
		twin.locality=this.locality;
		twin.lon=this.lon;		
		twin.postalcode=this.postalcode;
		twin.region=this.region;
		twin.room=this.room;
		twin.speed=this.speed;
		twin.street=this.street;
		twin.text=this.text;
		twin.timestamp=this.timestamp;	
		twin.uri = this.uri;
		
		return twin;
	}
	
	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String childElement = UserLocationInfo.CHILD_ELEMENT;
		
		boolean done = false;

		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
				} else if (tagName.equals("accuracy")) {
					this.accuracy = Double.valueOf(parser.nextText()).doubleValue();
				} else if (tagName.equals("alt")) {
					this.alt = Integer.valueOf(parser.nextText()).intValue();
				} else if (tagName.equals("area")) {
					this.area = parser.nextText();
				} else if (tagName.equals("bearing")) {
					this.bearing = Double.valueOf(parser.nextText()).doubleValue();
				} else if (tagName.equals("building")) {
					this.building = parser.nextText();
				} else if (tagName.equals("country")) {
					this.country = parser.nextText();
				} else if (tagName.equals("countrycode")) {
					this.countrycode = parser.nextText();
				} else if (tagName.equals("datum")) {
					this.datum = parser.nextText();		
				} else if (tagName.equals("description")) {
					this.description = parser.nextText();			
				} else if (tagName.equals("error")) {
					this.error = Double.valueOf(parser.nextText()).doubleValue();
				} else if (tagName.equals("floor")) {
					this.floor = parser.nextText();			
				} else if (tagName.equals("lat")) {
					this.lat = Double.valueOf(parser.nextText()).doubleValue();
				} else if (tagName.equals("locality")) {
					this.locality = parser.nextText();			
				} else if (tagName.equals("lon")) {
					this.lon = Double.valueOf(parser.nextText()).doubleValue();
				} else if (tagName.equals("postalcode")) {
					this.postalcode = parser.nextText();			
				} else if (tagName.equals("region")) {
					this.region = parser.nextText();			
				} else if (tagName.equals("room")) {
					this.room = parser.nextText();			
				} else if (tagName.equals("speed")) {
					this.speed = Double.valueOf(parser.nextText()).doubleValue();
				} else if (tagName.equals("street")) {
					this.street = parser.nextText();			
				} else if (tagName.equals("text")) {
					this.text = parser.nextText();	
				} else if (tagName.equals("timestamp")) {
					this.timestamp = parser.nextText();	
				} else if (tagName.equals("uri")) {
					this.uri = parser.nextText();
				} else
					parser.next();
				break;
			case XmlPullParser.END_TAG:
				if (parser.getName().equals(childElement))
					done = true;
				else
					parser.next();
				break;
			case XmlPullParser.END_DOCUMENT:
				done = true;
				break;
			default:
				parser.next();
			}
		} while (!done);		
		
	}

	@Override
	public String toXML() {
		String childElement = UserLocationInfo.CHILD_ELEMENT;
		String namespace = UserLocationInfo.NAMESPACE;		

		StringBuilder sb = new StringBuilder()
				.append("<").append(childElement)
				.append(" xmlns=\"").append(namespace).append("\">");
		
		if (this.accuracy > Double.MIN_VALUE)
			sb.append("<accuracy>").append(this.accuracy).append("</accuracy>");
		if (this.alt > Double.MIN_VALUE)
			sb.append("<alt>").append(this.alt).append("</alt>");
		if (this.area != null)
			sb.append("<area>").append(this.area).append("</area>");
		if (this.bearing > Double.MIN_VALUE)
			sb.append("<bearing>").append(this.bearing).append("</bearing>");
		if (this.building != null)
			sb.append("<building>").append(this.building).append("</building>");
		if (this.country != null)
			sb.append("<country>").append(this.country).append("</country>");
		if (this.countrycode != null)
			sb.append("<countrycode>").append(this.countrycode).append("</countrycode>");
		if (this.datum != null)
			sb.append("<datum>").append(this.datum).append("</datum>");
		if (this.description != null)
			sb.append("<description>").append(this.description).append("</description>");
		if (this.error > Double.MIN_VALUE)
			sb.append("<error>").append(this.error).append("</error>");
		if (this.floor != null)
			sb.append("<floor>").append(this.floor).append("</floor>");
		if (this.lat > Double.MIN_VALUE)
			sb.append("<lat>").append(this.lat).append("</lat>");
		if (this.locality != null)
			sb.append("<locality>").append(this.locality).append("</locality>");
		if (this.lon > Double.MIN_VALUE)
			sb.append("<lon>").append(this.lon).append("</lon>");
		if (this.postalcode != null)
			sb.append("<postalcode>").append(this.postalcode).append("</postalcode>");
		if (this.region != null)
			sb.append("<region>").append(this.region).append("</region>");
		if (this.room != null)
			sb.append("<room>").append(this.room).append("</room>");
		if (this.speed > Double.MIN_VALUE)
			sb.append("<speed>").append(this.speed).append("</speed>");
		if (this.street != null)
			sb.append("<street>").append(this.street).append("</street>");
		if (this.text != null)
			sb.append("<text>").append(this.text).append("</text>");
		if (this.timestamp != null)
			sb.append("<timestamp>").append(this.timestamp).append("</timestamp>");		
		if (this.uri != null)
			sb.append("<uri>").append(this.uri).append("</uri>");		
		
		sb.append("</").append(childElement).append(">");
		return sb.toString();
	}

	@Override
	public String getChildElement() {
		return UserLocationInfo.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		return UserLocationInfo.NAMESPACE;
	}	
	
	// Getter & Setter
	
	public double getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}

	public double getAlt() {
		return alt;
	}

	public void setAlt(double alt) {
		this.alt = alt;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public double getBearing() {
		return bearing;
	}

	public void setBearing(double bearing) {
		this.bearing = bearing;
	}

	public String getBuilding() {
		return building;
	}

	public void setBuilding(String building) {
		this.building = building;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCountrycode() {
		return countrycode;
	}

	public void setCountrycode(String countrycode) {
		this.countrycode = countrycode;
	}

	public String getDatum() {
		return datum;
	}

	public void setDatum(String datum) {
		this.datum = datum;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getError() {
		return error;
	}

	public void setError(double error) {
		this.error = error;
	}

	public String getFloor() {
		return floor;
	}

	public void setFloor(String floor) {
		this.floor = floor;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public String getLocality() {
		return locality;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public String getPostalcode() {
		return postalcode;
	}

	public void setPostalcode(String postalcode) {
		this.postalcode = postalcode;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
}
