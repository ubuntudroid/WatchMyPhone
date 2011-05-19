package de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt;

import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.model.LocationInfo;

public class LocationBean extends XMPPBean {

	private static final long serialVersionUID = -4446904605284092860L;
	public static final String NAMESPACE = "mobilisxhunt:iq:location";
	public static final String CHILD_ELEMENT = "query";

	//SET and RESULT
	public ArrayList<LocationInfo> Locations = new ArrayList<LocationInfo>();
	
	public LocationBean() {}
	
	//SET
	public LocationBean(ArrayList<LocationInfo> locationInfos){
		this.Locations = locationInfos;
	}
	
	//RESULT
	public LocationBean(LocationInfo playerLocation){
		this.Locations.clear();
		this.Locations.add(playerLocation);
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		boolean done = false;
		LocationInfo locationInfo = new LocationInfo();
		
		do {
			switch (parser.getEventType()){
			
				case XmlPullParser.START_TAG:
					String tagName = parser.getName();
					
					if (tagName.equals(CHILD_ELEMENT)){
						parser.next();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_LOCATION)) {
						locationInfo = new LocationInfo();
						parser.next();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_PLAYER_JID)) {
						locationInfo.Jid = parser.nextText();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_LOCATION_LAT)) {
						locationInfo.Latitude = Integer.valueOf(parser.nextText()).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_LOCATION_LON)) {
						locationInfo.Longitude = Integer.valueOf(parser.nextText()).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_ERROR)) {
						parser = parseErrorAttributes(parser);
					}
					else
						parser.next();
					break;
				case XmlPullParser.END_TAG:
					if (parser.getName().equals(CHILD_ELEMENT)){
						done = true;
					}
					else if (parser.getName().equals(XHuntElements.CHILD_ELEMENT_LOCATION)) {
						Locations.add(locationInfo);						
						parser.next();
					}
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
	public String getChildElement() {
		return CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

	@Override
	public XMPPBean clone() {
		ArrayList<LocationInfo> infos = new ArrayList<LocationInfo>();
		
		for(LocationInfo info : Locations){
			infos.add(info.copy());
		}
		
		LocationBean clone = new LocationBean(infos);
		
		return (LocationBean)cloneBasicAttributes(clone);
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();

		for(LocationInfo locationInfo : this.Locations){
				sb.append("<" + XHuntElements.CHILD_ELEMENT_LOCATION + ">");
			
				sb.append("<" + XHuntElements.CHILD_ELEMENT_PLAYER_JID + ">")
					.append(locationInfo.Jid)
					.append("</" + XHuntElements.CHILD_ELEMENT_PLAYER_JID + ">");	
				sb.append("<" + XHuntElements.CHILD_ELEMENT_LOCATION_LAT + ">")
					.append(locationInfo.Latitude)
					.append("</" + XHuntElements.CHILD_ELEMENT_LOCATION_LAT + ">");	
				sb.append("<" + XHuntElements.CHILD_ELEMENT_LOCATION_LON + ">")
					.append(locationInfo.Longitude)
					.append("</" + XHuntElements.CHILD_ELEMENT_LOCATION_LON + ">");
				
				sb.append("</" + XHuntElements.CHILD_ELEMENT_LOCATION + ">");
		}
		
		sb = appendErrorPayload(sb);

		return sb.toString();
	}
}