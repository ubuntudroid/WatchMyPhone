package de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt;

import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.model.OpenGameInfo;

public class OpenGamesBean extends XMPPBean {

	private static final long serialVersionUID = 7187317607918750642L;
	public static final String NAMESPACE = "mobilisxhunt:iq:opengames";
	public static final String CHILD_ELEMENT = "query";

	//SET
	public int AreaId = -1;
	
	//RESULT
	public ArrayList<OpenGameInfo> OpenGames = new ArrayList<OpenGameInfo>();
	
	public OpenGamesBean() {}
	
	//SET
	public OpenGamesBean(int areaId){
		this.AreaId = areaId;
	}
	
	//RESULT
	public OpenGamesBean(ArrayList<OpenGameInfo> openGames){
		this.OpenGames = openGames;
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		boolean done = false;
		OpenGameInfo openGame = new OpenGameInfo();
		
		do {
			switch (parser.getEventType()){
			
				case XmlPullParser.START_TAG:
					String tagName = parser.getName();
					
					if (tagName.equals(CHILD_ELEMENT)){
						parser.next();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_AREA_ID)) {
						this.AreaId = Integer.valueOf(parser.nextText()).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME)) {
						openGame = new OpenGameInfo();
						parser.next();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME_JID)) {
						openGame.GameJid = parser.nextText();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME_NAME)) {
						openGame.GameName = parser.nextText();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME_PASSWORD_REQUIRED)) {
						openGame.PasswordRequired = Boolean.valueOf(parser.nextText()).booleanValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME_WAITINGPLAYERS)) {
						openGame.WatingPlayers = Integer.valueOf(parser.nextText()).intValue();
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
					else if (parser.getName().equals(XHuntElements.CHILD_ELEMENT_GAME)){
						this.OpenGames.add(openGame);
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
		OpenGamesBean clone = this.AreaId > -1
			? new OpenGamesBean(this.AreaId)
			: new OpenGamesBean(this.OpenGames);
			
		return (OpenGamesBean)cloneBasicAttributes(clone);
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();

		if(this.AreaId > -1){
			sb.append("<" + XHuntElements.CHILD_ELEMENT_AREA_ID + ">")
				.append(this.AreaId)
				.append("</" + XHuntElements.CHILD_ELEMENT_AREA_ID + ">");
		}
		else{
			for ( OpenGameInfo openGame : this.OpenGames ){
				sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME + ">");
				
				sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME_JID + ">")
					.append(openGame.GameJid)
					.append("</" + XHuntElements.CHILD_ELEMENT_GAME_JID + ">");
				sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME_NAME + ">")
					.append(openGame.GameName)
					.append("</" + XHuntElements.CHILD_ELEMENT_GAME_NAME + ">");
				sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME_PASSWORD_REQUIRED + ">")
					.append(openGame.PasswordRequired)
					.append("</" + XHuntElements.CHILD_ELEMENT_GAME_PASSWORD_REQUIRED + ">");
				sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME_WAITINGPLAYERS + ">")
					.append(openGame.WatingPlayers)
					.append("</" + XHuntElements.CHILD_ELEMENT_GAME_WAITINGPLAYERS + ">");
				
				sb.append("</" + XHuntElements.CHILD_ELEMENT_GAME + ">");
			}
		}
		
		sb = appendErrorPayload(sb);

		return sb.toString();
	}
}