package de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.model.PlayerSnapshotInfo;

public class SnapshotBean extends XMPPBean {

	private static final long serialVersionUID = 4281570531411195035L;
	public static final String NAMESPACE = "mobilisxhunt:iq:snapshot";
	public static final String CHILD_ELEMENT = "query";
	
	public String GameName;
	public int Round = -1;
	public boolean IsRoundStart = false;
	public boolean ShowMrX = false;
	public int StartTimer = -1;
	public HashMap<Integer, Integer> Tickets = new HashMap<Integer, Integer>();
	public ArrayList<PlayerSnapshotInfo> PlayersSnapshot = new ArrayList<PlayerSnapshotInfo>();
	
	public SnapshotBean() {}
	
	//SET
	public SnapshotBean(String gameId, int round, boolean isRoundStart, boolean showMrx,
			int startTimer, HashMap<Integer, Integer> tickets, ArrayList<PlayerSnapshotInfo> playersSnapshot) {
		this.GameName = gameId;
		this.Round = round;
		this.IsRoundStart = isRoundStart;
		this.ShowMrX = showMrx;
		this.StartTimer = startTimer;
		this.Tickets = tickets;
		this.PlayersSnapshot = playersSnapshot;
	}
	
	//ERROR
	public SnapshotBean(String errorType, String errorCondition, String errorText) {
		super(errorType, errorCondition, errorText);	
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		boolean done = false;
		int ticketId = -1;
		int ticketAmount = -1;
		PlayerSnapshotInfo playerSnapshotInfo = new PlayerSnapshotInfo();
		
		do {
			switch (parser.getEventType()){
			
				case XmlPullParser.START_TAG:
					String tagName = parser.getName();
					
					if (tagName.equals(CHILD_ELEMENT)){
						parser.next();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME_NAME)) {
						this.GameName = parser.nextText();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME_ROUND)) {
						this.Round = Integer.valueOf( parser.nextText() ).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME_ISROUNDSTART)) {
						this.IsRoundStart = Boolean.valueOf( parser.nextText() ).booleanValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME_SHOWMRX)) {
						this.ShowMrX = Boolean.valueOf( parser.nextText() ).booleanValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME_STARTTIMER)) {
						this.StartTimer = Integer.valueOf( parser.nextText() ).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_TICKET_ID)) {
						ticketId = Integer.valueOf(parser.nextText()).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_TICKET_AMOUNT)) {
						ticketAmount = Integer.valueOf(parser.nextText()).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_PLAYER_JID)) {
						playerSnapshotInfo.Jid = parser.nextText();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_PLAYER_NAME)) {
						playerSnapshotInfo.PlayerName = parser.nextText();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_PLAYER_ISMODERATOR)) {
						playerSnapshotInfo.IsModerator = Boolean.valueOf( parser.nextText() ).booleanValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_PLAYER_ISMRX)) {
						playerSnapshotInfo.IsMrX = Boolean.valueOf( parser.nextText() ).booleanValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_PLAYER_ISREADY)) {
						playerSnapshotInfo.IsReady = Boolean.valueOf( parser.nextText() ).booleanValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_LOCATION_LAT)) {
						playerSnapshotInfo.Latitude = Integer.valueOf(parser.nextText()).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_LOCATION_LON)) {
						playerSnapshotInfo.Longitude = Integer.valueOf(parser.nextText()).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_STATION_ID)) {
						playerSnapshotInfo.TargetId = Integer.valueOf( parser.nextText() ).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_STATION_ISFINAL)) {
						playerSnapshotInfo.IsTargetFinal = Boolean.valueOf( parser.nextText() ).booleanValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_STATION_ARRIVED)) {
						playerSnapshotInfo.TargetReached = Boolean.valueOf(parser.nextText()).booleanValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_STATION_LASTSTATION)) {
						playerSnapshotInfo.LastStationId = Integer.valueOf( parser.nextText() ).intValue();
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
						this.Tickets.put(ticketId, ticketAmount);
						ticketId = -1;
						ticketAmount = -1;
						parser.next();
					}
					else if (parser.getName().equals(XHuntElements.CHILD_ELEMENT_PLAYER)){
						this.PlayersSnapshot.add(playerSnapshotInfo);
						playerSnapshotInfo = new PlayerSnapshotInfo();
						
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
		SnapshotBean clone = this.Round > 0
		? new SnapshotBean(this.GameName, this.Round, this.IsRoundStart, 
				this.ShowMrX, this.StartTimer, this.Tickets, this.PlayersSnapshot)
		: new SnapshotBean();
		
		return (SnapshotBean)cloneBasicAttributes(clone);
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();
		
		if(this.Round > 0){
			sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME_NAME + ">")
				.append(this.GameName)
				.append("</" + XHuntElements.CHILD_ELEMENT_GAME_NAME + ">");		
			sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME_ROUND + ">")
				.append(this.Round)
				.append("</" + XHuntElements.CHILD_ELEMENT_GAME_ROUND + ">");	
			sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME_ISROUNDSTART + ">")
				.append(this.IsRoundStart)
				.append("</" + XHuntElements.CHILD_ELEMENT_GAME_ISROUNDSTART + ">");
			sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME_SHOWMRX + ">")
				.append(this.ShowMrX)
				.append("</" + XHuntElements.CHILD_ELEMENT_GAME_SHOWMRX + ">");
			sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME_STARTTIMER + ">")
				.append(StartTimer)
				.append("</" + XHuntElements.CHILD_ELEMENT_GAME_STARTTIMER + ">");
			
			for ( Map.Entry<Integer, Integer> elem : this.Tickets.entrySet() ){
				sb.append("<" + XHuntElements.CHILD_ELEMENT_TICKET + ">");
				
				sb.append("<" + XHuntElements.CHILD_ELEMENT_TICKET_ID + ">")
					.append(elem.getKey())
					.append("</" + XHuntElements.CHILD_ELEMENT_TICKET_ID + ">");
				sb.append("<" + XHuntElements.CHILD_ELEMENT_TICKET_AMOUNT + ">")
					.append(elem.getValue())
					.append("</" + XHuntElements.CHILD_ELEMENT_TICKET_AMOUNT + ">");
				
				sb.append("</" + XHuntElements.CHILD_ELEMENT_TICKET + ">");
			}
			
			for(PlayerSnapshotInfo playerSnapshot : PlayersSnapshot){
				sb.append("<" + XHuntElements.CHILD_ELEMENT_PLAYER + ">");
				
				sb.append("<" + XHuntElements.CHILD_ELEMENT_PLAYER_JID + ">")
					.append(playerSnapshot.Jid)
					.append("</" + XHuntElements.CHILD_ELEMENT_PLAYER_JID + ">");	
				sb.append("<" + XHuntElements.CHILD_ELEMENT_PLAYER_NAME + ">")
					.append(playerSnapshot.PlayerName)
					.append("</" + XHuntElements.CHILD_ELEMENT_PLAYER_NAME + ">");	
				sb.append("<" + XHuntElements.CHILD_ELEMENT_PLAYER_ISMODERATOR + ">")
					.append(playerSnapshot.IsModerator)
					.append("</" + XHuntElements.CHILD_ELEMENT_PLAYER_ISMODERATOR + ">");				
				sb.append("<" + XHuntElements.CHILD_ELEMENT_PLAYER_ISMRX + ">")
					.append(playerSnapshot.IsMrX)
					.append("</" + XHuntElements.CHILD_ELEMENT_PLAYER_ISMRX + ">");				
				sb.append("<" + XHuntElements.CHILD_ELEMENT_PLAYER_ISREADY + ">")
					.append(playerSnapshot.IsReady)
					.append("</" + XHuntElements.CHILD_ELEMENT_PLAYER_ISREADY + ">");
				
				sb.append("<" + XHuntElements.CHILD_ELEMENT_LOCATION_LAT + ">")
					.append(playerSnapshot.Latitude)
					.append("</" + XHuntElements.CHILD_ELEMENT_LOCATION_LAT + ">");	
				sb.append("<" + XHuntElements.CHILD_ELEMENT_LOCATION_LON + ">")
					.append(playerSnapshot.Longitude)
					.append("</" + XHuntElements.CHILD_ELEMENT_LOCATION_LON + ">");
				
				sb.append("<" + XHuntElements.CHILD_ELEMENT_STATION_ID + ">")
					.append(playerSnapshot.TargetId)
					.append("</" + XHuntElements.CHILD_ELEMENT_STATION_ID + ">");	
				sb.append("<" + XHuntElements.CHILD_ELEMENT_STATION_ISFINAL + ">")
					.append(playerSnapshot.IsTargetFinal)
					.append("</" + XHuntElements.CHILD_ELEMENT_STATION_ISFINAL + ">");
				sb.append("<" + XHuntElements.CHILD_ELEMENT_STATION_ARRIVED + ">")
					.append(playerSnapshot.TargetReached)
					.append("</" + XHuntElements.CHILD_ELEMENT_STATION_ARRIVED + ">");
				sb.append("<" + XHuntElements.CHILD_ELEMENT_STATION_LASTSTATION + ">")
					.append(playerSnapshot.LastStationId)
					.append("</" + XHuntElements.CHILD_ELEMENT_STATION_LASTSTATION + ">");
				
				sb.append("</" + XHuntElements.CHILD_ELEMENT_PLAYER + ">");
			}
		}

		sb = appendErrorPayload(sb);
		
		return sb.toString();
	}
}