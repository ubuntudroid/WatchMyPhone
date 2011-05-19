package de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt;

import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.model.PlayerInfo;

public class PlayersBean extends XMPPBean {

	private static final long serialVersionUID = -9088297420134409480L;
	public static final String NAMESPACE = "mobilisxhunt:iq:players";
	public static final String CHILD_ELEMENT = "query";

	//SET
	public ArrayList<PlayerInfo> Players = new ArrayList<PlayerInfo>();
	public String Info;
	
	public PlayersBean() {}
	
	//SET
	public PlayersBean(ArrayList<PlayerInfo> players, String info){
		this.Players = players;
		this.Info = info;
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		boolean done = false;
		PlayerInfo playerInfo = new PlayerInfo();
		
		do {
			switch (parser.getEventType()){
			
				case XmlPullParser.START_TAG:
					String tagName = parser.getName();
					
					if (tagName.equals(CHILD_ELEMENT)){
						parser.next();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_PLAYER_JID)) {
						playerInfo.Jid = parser.nextText();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_PLAYER_NAME)) {
						playerInfo.PlayerName = parser.nextText();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_PLAYER_ISMODERATOR)) {
						playerInfo.IsModerator = Boolean.valueOf( parser.nextText() ).booleanValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_PLAYER_ISMRX)) {
						playerInfo.IsMrX = Boolean.valueOf( parser.nextText() ).booleanValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_PLAYER_ISREADY)) {
						playerInfo.IsReady = Boolean.valueOf( parser.nextText() ).booleanValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_INFO)) {
						this.Info = parser.nextText();
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
					else if (parser.getName().equals(XHuntElements.CHILD_ELEMENT_PLAYER)) {
						Players.add(playerInfo);
						playerInfo = new PlayerInfo();
						
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
		PlayersBean clone = this.Players.size() > 0
			? new PlayersBean(this.Players, this.Info)
			: new PlayersBean();
	
		return (PlayersBean)cloneBasicAttributes(clone);
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();

		for(PlayerInfo playerInfo : this.Players){
			sb.append("<" + XHuntElements.CHILD_ELEMENT_PLAYER + ">");
		
			sb.append("<" + XHuntElements.CHILD_ELEMENT_PLAYER_JID + ">")
				.append(playerInfo.Jid)
				.append("</" + XHuntElements.CHILD_ELEMENT_PLAYER_JID + ">");	
			sb.append("<" + XHuntElements.CHILD_ELEMENT_PLAYER_NAME + ">")
				.append(playerInfo.PlayerName)
				.append("</" + XHuntElements.CHILD_ELEMENT_PLAYER_NAME + ">");	
			sb.append("<" + XHuntElements.CHILD_ELEMENT_PLAYER_ISMODERATOR + ">")
				.append(playerInfo.IsModerator)
				.append("</" + XHuntElements.CHILD_ELEMENT_PLAYER_ISMODERATOR + ">");				
			sb.append("<" + XHuntElements.CHILD_ELEMENT_PLAYER_ISMRX + ">")
				.append(playerInfo.IsMrX)
				.append("</" + XHuntElements.CHILD_ELEMENT_PLAYER_ISMRX + ">");				
			sb.append("<" + XHuntElements.CHILD_ELEMENT_PLAYER_ISREADY + ">")
				.append(playerInfo.IsReady)
				.append("</" + XHuntElements.CHILD_ELEMENT_PLAYER_ISREADY + ">");
			
			sb.append("</" + XHuntElements.CHILD_ELEMENT_PLAYER + ">");
		}
		
		if(this.Info != null){
			sb.append("<" + XHuntElements.CHILD_ELEMENT_INFO + ">")
				.append(this.Info)
				.append("</" + XHuntElements.CHILD_ELEMENT_INFO + ">");
		}

		sb = appendErrorPayload(sb);
		
		return sb.toString();
	}
}