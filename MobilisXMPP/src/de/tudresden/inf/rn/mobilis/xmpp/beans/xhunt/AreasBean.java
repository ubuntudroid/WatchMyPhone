package de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt;

import java.util.ArrayList;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.model.AreaInfo;

public class AreasBean extends XMPPBean {

	private static final long serialVersionUID = 6083637806223339229L;
	public static final String NAMESPACE = "mobilisxhunt:iq:areas";
	public static final String CHILD_ELEMENT = "query";
	
	//RESULT
	public ArrayList<AreaInfo> Areas = new ArrayList<AreaInfo>();
	
	public AreasBean() {}
	
	//RESULT
	public AreasBean(ArrayList<AreaInfo> areas){
		this.Areas = areas;
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		boolean done = false;
		int ticketId = -1;
		String ticketName = "";
		AreaInfo area = new AreaInfo();
		
		do {
			switch (parser.getEventType()){
			
				case XmlPullParser.START_TAG:
					String tagName = parser.getName();
					
					if (tagName.equals(CHILD_ELEMENT)){
						parser.next();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_AREA)) {
						area = new AreaInfo();
						parser.next();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_AREA_ID)) {
						area.AreaId = Integer.valueOf(parser.nextText()).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_AREA_NAME)) {
						area.AreaName = parser.nextText();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_AREA_DESCRIPTION)) {
						area.AreaDescription = parser.nextText();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_AREA_VERSION)) {
						area.Version = Integer.valueOf(parser.nextText()).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_TICKET_ID)) {
						ticketId = Integer.valueOf(parser.nextText()).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_TICKET_NAME)) {
						ticketName = parser.nextText();
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
					else if (parser.getName().equals(XHuntElements.CHILD_ELEMENT_TICKET)){
						area.Tickettypes.put(ticketId, ticketName);
						ticketId = -1;
						ticketName = "";
						parser.next();
					}
					else if (parser.getName().equals(XHuntElements.CHILD_ELEMENT_AREA)){
						this.Areas.add(area);
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
		AreasBean clone = this.Areas.size() > 0 
			? new AreasBean(this.Areas)
			: new AreasBean();
			
		return (AreasBean)cloneBasicAttributes(clone);
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();

		if(this.Areas.size() > 0){
			for ( AreaInfo area : this.Areas ){
				sb.append("<" + XHuntElements.CHILD_ELEMENT_AREA + ">");
				
				sb.append("<" + XHuntElements.CHILD_ELEMENT_AREA_ID + ">")
					.append(area.AreaId)
					.append("</" + XHuntElements.CHILD_ELEMENT_AREA_ID + ">");
				sb.append("<" + XHuntElements.CHILD_ELEMENT_AREA_NAME + ">")
					.append(area.AreaName)
					.append("</" + XHuntElements.CHILD_ELEMENT_AREA_NAME + ">");
				sb.append("<" + XHuntElements.CHILD_ELEMENT_AREA_DESCRIPTION + ">")
					.append(area.AreaDescription)
					.append("</" + XHuntElements.CHILD_ELEMENT_AREA_DESCRIPTION + ">");
				sb.append("<" + XHuntElements.CHILD_ELEMENT_AREA_VERSION + ">")
					.append(area.Version)
					.append("</" + XHuntElements.CHILD_ELEMENT_AREA_VERSION + ">");

				for ( Map.Entry<Integer, String> elem : area.Tickettypes.entrySet() ){
					sb.append("<" + XHuntElements.CHILD_ELEMENT_TICKET + ">");
					
					sb.append("<" + XHuntElements.CHILD_ELEMENT_TICKET_ID + ">")
						.append(elem.getKey())
						.append("</" + XHuntElements.CHILD_ELEMENT_TICKET_ID + ">");
					sb.append("<" + XHuntElements.CHILD_ELEMENT_TICKET_NAME + ">")
						.append(elem.getValue())
						.append("</" + XHuntElements.CHILD_ELEMENT_TICKET_NAME + ">");
					
					sb.append("</" + XHuntElements.CHILD_ELEMENT_TICKET + ">");
				}
				
				sb.append("</" + XHuntElements.CHILD_ELEMENT_AREA + ">");
			}
		}

		sb = appendErrorPayload(sb);
		
		return sb.toString();
	}
}