package de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

public class TargetBean extends XMPPBean {

	private static final long serialVersionUID = 4585607706893259802L;
	public static final String NAMESPACE = "mobilisxhunt:iq:target";
	public static final String CHILD_ELEMENT = "query";

	//SET
	public int StationId = -1;
	public int Round = -1;
	public int TicketId = -1;
	public boolean IsFinal = false;
	
	public TargetBean() {}
	
	//SET
	public TargetBean(int stationId, int round,
			int ticketId, boolean isFinal) {
		this.StationId = stationId;
		this.Round = round;
		this.TicketId = ticketId;
		this.IsFinal = isFinal;
	}
	
	//RESULT
	public TargetBean(int ticketId) {
		this.TicketId = ticketId;
	}
	
	//ERROR
	public TargetBean(String errorType, String errorCondition, String errorText) {
		super(errorType, errorCondition, errorText);	
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		boolean done = false;
		
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
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME_ROUND)) {
						this.Round = Integer.valueOf( parser.nextText() ).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_TICKET_ID)) {
						this.TicketId = Integer.valueOf( parser.nextText() ).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_STATION_ISFINAL)) {
						this.IsFinal = Boolean.valueOf( parser.nextText() ).booleanValue();
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
		TargetBean clone = this.StationId > -1
			? new TargetBean(this.StationId, this.Round,
					this.TicketId, this.IsFinal)
			: new TargetBean(this.TicketId);
			
		return (TargetBean)cloneBasicAttributes(clone);
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();

		sb.append("<" + XHuntElements.CHILD_ELEMENT_TICKET_ID + ">")
			.append(this.TicketId)
			.append("</" + XHuntElements.CHILD_ELEMENT_TICKET_ID + ">");
		
		if(this.StationId > -1){
			sb.append("<" + XHuntElements.CHILD_ELEMENT_STATION_ID + ">")
				.append(this.StationId)
				.append("</" + XHuntElements.CHILD_ELEMENT_STATION_ID + ">");		
			sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME_ROUND + ">")
				.append(this.Round)
				.append("</" + XHuntElements.CHILD_ELEMENT_GAME_ROUND + ">");							
			sb.append("<" + XHuntElements.CHILD_ELEMENT_STATION_ISFINAL + ">")
				.append(this.IsFinal)
				.append("</" + XHuntElements.CHILD_ELEMENT_STATION_ISFINAL + ">");
		}
		
		sb = appendErrorPayload(sb);

		return sb.toString();
	}
}