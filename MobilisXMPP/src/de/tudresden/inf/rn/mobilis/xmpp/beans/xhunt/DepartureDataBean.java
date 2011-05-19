package de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt;

import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.model.DepartureInfo;

public class DepartureDataBean extends XMPPBean {

	private static final long serialVersionUID = 1070333143502499905L;
	public static final String NAMESPACE = "mobilisxhunt:iq:departure";
	public static final String CHILD_ELEMENT = "query";

	//GET
	public int StationId = -1;
	
	//RESULT
	public ArrayList<DepartureInfo> Departures = new ArrayList<DepartureInfo>();
	
	public DepartureDataBean() {}
	
	//GET
	public DepartureDataBean(int stationId){
		this.StationId = stationId;
	}
	
	//RESULT
	public DepartureDataBean(ArrayList<DepartureInfo> departures){
		this.Departures = departures;
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		boolean done = false;
		
		DepartureInfo departure = new DepartureInfo();
		
		do {
			switch (parser.getEventType()){
			
				case XmlPullParser.START_TAG:
					String tagName = parser.getName();
					
					if (tagName.equals(CHILD_ELEMENT)){
						parser.next();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_STATION_ID)) {
						this.StationId = Integer.valueOf( parser.nextText() ).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_DEPARTURE)) {
						departure = new DepartureInfo();
						parser.next();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_VEHICLE_ID)) {
						departure.VehicleId = Integer.valueOf( parser.nextText() ).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_VEHICLE_NAME)) {
						departure.VehicleName = parser.nextText();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_DEPARTURE_DIRECTION)) {
						departure.Direction = parser.nextText();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_DEPARTURE_TIMELEFT)) {
						departure.TimeLeft = parser.nextText();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_ERROR)) {
						parser = parseErrorAttributes(parser);
					}
					else
						parser.next();
					break;
				case XmlPullParser.END_TAG:
					if (parser.getName().equals(XHuntElements.CHILD_ELEMENT_DEPARTURE)){
						Departures.add(departure);
						parser.next();
					}
					else if (parser.getName().equals(CHILD_ELEMENT)){
						done = true;
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
		DepartureDataBean clone = this.StationId > -1 
			? new DepartureDataBean ( StationId )
			: new DepartureDataBean ( Departures );
		
		return (DepartureDataBean)cloneBasicAttributes(clone);
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();

		//GET
		if(StationId > -1){
			sb.append("<" + XHuntElements.CHILD_ELEMENT_STATION_ID + ">").append(StationId)
				.append("</" + XHuntElements.CHILD_ELEMENT_STATION_ID + ">");
		}
		else{//RESULT
			for(DepartureInfo departure : Departures){
				sb.append("<" + XHuntElements.CHILD_ELEMENT_DEPARTURE + ">");
				
				sb.append("<" + XHuntElements.CHILD_ELEMENT_VEHICLE_ID+ ">")
					.append(departure.VehicleId)
					.append("</" + XHuntElements.CHILD_ELEMENT_VEHICLE_ID + ">");
				sb.append("<" + XHuntElements.CHILD_ELEMENT_VEHICLE_NAME + ">")
					.append(departure.VehicleName)
					.append("</" + XHuntElements.CHILD_ELEMENT_VEHICLE_NAME + ">");
				sb.append("<" + XHuntElements.CHILD_ELEMENT_DEPARTURE_DIRECTION + ">")
					.append(departure.Direction)
					.append("</" + XHuntElements.CHILD_ELEMENT_DEPARTURE_DIRECTION + ">");
				sb.append("<" + XHuntElements.CHILD_ELEMENT_DEPARTURE_TIMELEFT + ">")
					.append(departure.TimeLeft)
					.append("</" + XHuntElements.CHILD_ELEMENT_DEPARTURE_TIMELEFT + ">");
				
				sb.append("</" + XHuntElements.CHILD_ELEMENT_DEPARTURE + ">");
			}
		}
		
		sb = appendErrorPayload(sb);

		return sb.toString();
	}
}