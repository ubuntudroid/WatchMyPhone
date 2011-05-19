package de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

public class UsedTicketsBean extends XMPPBean {

	private static final long serialVersionUID = -9118167197458385130L;
	public static final String NAMESPACE = "mobilisxhunt:iq:usedtickets";
	public static final String CHILD_ELEMENT = "query";

	//RESULT
	public HashMap<String, ArrayList<Integer>> UsedTickets = new HashMap<String, ArrayList<Integer>>();
	
	public UsedTicketsBean() {}
	
	//RESULT
	public UsedTicketsBean(HashMap<String, ArrayList<Integer>> usedTickets) {
		this.UsedTickets = usedTickets;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		boolean done = false;
		
		ArrayList<Integer> ticketIds = new ArrayList<Integer>();
		String playerName = "";
		
		do {
			switch (parser.getEventType()){
			
				case XmlPullParser.START_TAG:
					String tagName = parser.getName();
					
					if (tagName.equals(CHILD_ELEMENT)){
						parser.next();
					}					
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_PLAYER_NAME)) {						
						playerName = parser.nextText();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_TICKET_ID)) {
						ticketIds.add(Integer.valueOf( parser.nextText() ).intValue());
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_ERROR)) {
						parser = parseErrorAttributes(parser);
					}
					else
						parser.next();
					break;
				case XmlPullParser.END_TAG:
					if (parser.getName().equals(XHuntElements.CHILD_ELEMENT_PLAYER)){
						this.UsedTickets.put(playerName, (ArrayList<Integer>)ticketIds.clone());
						ticketIds.clear();
						playerName = "";
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
		UsedTicketsBean clone = this.UsedTickets.size() > 0
			? new UsedTicketsBean(this.UsedTickets)
			: new UsedTicketsBean();
			
		return (UsedTicketsBean)cloneBasicAttributes(clone);
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();

		if(this.UsedTickets.size() > 0){
			for(Map.Entry<String, ArrayList<Integer>> elem : this.UsedTickets.entrySet()){
				sb.append("<" + XHuntElements.CHILD_ELEMENT_PLAYER+ ">");
				
				sb.append("<" + XHuntElements.CHILD_ELEMENT_PLAYER_NAME+ ">")
					.append(elem.getKey())
					.append("</" + XHuntElements.CHILD_ELEMENT_PLAYER_NAME + ">");
				
				for(Integer ticketId : elem.getValue()){
					sb.append("<" + XHuntElements.CHILD_ELEMENT_TICKET_ID+ ">")
						.append(ticketId)
						.append("</" + XHuntElements.CHILD_ELEMENT_TICKET_ID + ">");
				}
				
				sb.append("</" + XHuntElements.CHILD_ELEMENT_PLAYER + ">");
			}
		}
		
		sb = appendErrorPayload(sb);

		return sb.toString();
	}
}