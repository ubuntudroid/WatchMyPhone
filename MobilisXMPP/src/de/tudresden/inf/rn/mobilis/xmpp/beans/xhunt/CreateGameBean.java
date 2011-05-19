package de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt;

import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

public class CreateGameBean extends XMPPBean {

	private static final long serialVersionUID = 4934589575574771042L;
	public static final String NAMESPACE = "mobilisxhunt:iq:creategame";
	public static final String CHILD_ELEMENT = "query";
	
	//SET
	public int AreaId = -1;
	public String GameName;
	public String GamePassword;
	public int CountRounds;
	public int MinPlayers;
	public int MaxPlayers;
	public int StartTimer = -1;
	
	//TicketId, countTickets
	public HashMap<Integer, Integer> TicketsMrX = new HashMap<Integer, Integer>();
	public HashMap<Integer, Integer> TicketsAgents = new HashMap<Integer, Integer>();
	
	//RESULT
	public CreateGameBean() {}
	
	//SET
	public CreateGameBean(int areaId, String gameName, String gamePassword, 
			int countRounds, int startTimer, int minPlayers, int maxPlayers,
			HashMap<Integer, Integer> ticketsMrX, HashMap<Integer, Integer> ticketsAgents){
		this.AreaId = areaId;
		this.GameName = gameName;
		this.GamePassword = gamePassword;
		this.CountRounds = countRounds;
		this.StartTimer = startTimer;
		this.MinPlayers = minPlayers;
		this.MaxPlayers = maxPlayers;
		this.TicketsMrX = ticketsMrX;
		this.TicketsAgents = ticketsAgents;
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		boolean done = false;
		
		boolean ticketsMrX = false;
		boolean ticketsAgents = false;
		int ticketId = -1;
		int ticketAmount = -1;
		
		do {
			switch (parser.getEventType()){
			
				case XmlPullParser.START_TAG:
					String tagName = parser.getName();
					
					if (tagName.equals(CHILD_ELEMENT)){
						parser.next();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_AREA_ID)) {
						this.AreaId = Integer.valueOf( parser.nextText() ).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME_NAME)) {
						this.GameName = parser.nextText();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME_PASSWORD)) {
						this.GamePassword = parser.nextText();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME_COUNTROUNDS)) {
						this.CountRounds = Integer.valueOf( parser.nextText() ).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME_STARTTIMER)) {
						this.StartTimer = Integer.valueOf( parser.nextText() ).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME_MINPLAYERS)) {
						this.MinPlayers = Integer.valueOf( parser.nextText() ).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME_MAXPLAYERS)) {
						this.MaxPlayers = Integer.valueOf( parser.nextText() ).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_TICKETS_MRX)) {
						ticketsMrX = true;
						ticketsAgents = false;
						parser.next();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_TICKETS_AGENTS)) {
						ticketsMrX = false;
						ticketsAgents = true;
						parser.next();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_TICKET_ID)) {
						ticketId = Integer.valueOf( parser.nextText() ).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_TICKET_AMOUNT)) {
						ticketAmount = Integer.valueOf( parser.nextText() ).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME_STARTTIMER)) {
						this.StartTimer = Integer.valueOf( parser.nextText() ).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_ERROR)) {
						parser = parseErrorAttributes(parser);
					}
					else
						parser.next();
					break;
				case XmlPullParser.END_TAG:
					if (parser.getName().equals(XHuntElements.CHILD_ELEMENT_TICKETS_MRX)){
						ticketsMrX = false;
						parser.next();
					}
					else if (parser.getName().equals(XHuntElements.CHILD_ELEMENT_TICKETS_AGENTS)){
						ticketsAgents = false;
						parser.next();
					}
					else if (parser.getName().equals(XHuntElements.CHILD_ELEMENT_TICKET_AMOUNT)){
						if(ticketsMrX)
							this.TicketsMrX.put(ticketId, ticketAmount);
						else if(ticketsAgents)
							this.TicketsAgents.put(ticketId, ticketAmount);
						
						ticketId = -1;
						ticketAmount = -1;
						
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
		CreateGameBean clone = this.AreaId > -1 
			? new CreateGameBean ( this.AreaId, this.GameName, this.GamePassword,
						this.CountRounds, this.StartTimer, this.MinPlayers, this.MaxPlayers,
						this.TicketsMrX, this.TicketsAgents)
			: new CreateGameBean ();
		
		return (CreateGameBean)cloneBasicAttributes(clone);
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();

		//SET
		if(this.AreaId > -1){
			sb.append("<" + XHuntElements.CHILD_ELEMENT_AREA_ID + ">")
				.append(this.AreaId)
				.append("</" + XHuntElements.CHILD_ELEMENT_AREA_ID + ">");
			sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME_NAME + ">")
				.append(this.GameName)
				.append("</" + XHuntElements.CHILD_ELEMENT_GAME_NAME + ">");
			sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME_PASSWORD + ">")
				.append(this.GamePassword)
				.append("</" + XHuntElements.CHILD_ELEMENT_GAME_PASSWORD + ">");
			sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME_COUNTROUNDS + ">")
				.append(this.CountRounds)
				.append("</" + XHuntElements.CHILD_ELEMENT_GAME_COUNTROUNDS + ">");
			sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME_STARTTIMER + ">")
				.append(this.StartTimer)
				.append("</" + XHuntElements.CHILD_ELEMENT_GAME_STARTTIMER + ">");
			sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME_MINPLAYERS + ">")
				.append(this.MinPlayers)
				.append("</" + XHuntElements.CHILD_ELEMENT_GAME_MINPLAYERS + ">");
			sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME_MAXPLAYERS + ">")
				.append(this.MaxPlayers)
				.append("</" + XHuntElements.CHILD_ELEMENT_GAME_MAXPLAYERS + ">");
			
			sb.append("<" + XHuntElements.CHILD_ELEMENT_TICKETS_MRX + ">");
			for(Map.Entry<Integer, Integer> elem : this.TicketsMrX.entrySet()){
				sb.append("<" + XHuntElements.CHILD_ELEMENT_TICKET_ID+ ">")
					.append(elem.getKey())
					.append("</" + XHuntElements.CHILD_ELEMENT_TICKET_ID + ">");
				
				sb.append("<" + XHuntElements.CHILD_ELEMENT_TICKET_AMOUNT+ ">")
					.append(elem.getValue())
					.append("</" + XHuntElements.CHILD_ELEMENT_TICKET_AMOUNT + ">");
			}
			sb.append("</" + XHuntElements.CHILD_ELEMENT_TICKETS_MRX + ">");
			
			sb.append("<" + XHuntElements.CHILD_ELEMENT_TICKETS_AGENTS + ">");
			for(Map.Entry<Integer, Integer> elem : this.TicketsAgents.entrySet()){
				sb.append("<" + XHuntElements.CHILD_ELEMENT_TICKET_ID+ ">")
					.append(elem.getKey())
					.append("</" + XHuntElements.CHILD_ELEMENT_TICKET_ID + ">");
				
				sb.append("<" + XHuntElements.CHILD_ELEMENT_TICKET_AMOUNT+ ">")
					.append(elem.getValue())
					.append("</" + XHuntElements.CHILD_ELEMENT_TICKET_AMOUNT + ">");
			}
			sb.append("</" + XHuntElements.CHILD_ELEMENT_TICKETS_AGENTS+ ">");
		}
		
		sb = appendErrorPayload(sb);

		return sb.toString();
	}
}