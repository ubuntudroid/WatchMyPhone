package de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt;

import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

public class GameDetailsBean extends XMPPBean {

	private static final long serialVersionUID = -7853171279214037394L;
	public static final String NAMESPACE = "mobilisxhunt:iq:gamedetails";
	public static final String CHILD_ELEMENT = "query";
	
	//RESULT
	public String GameName;
	public Boolean RequirePassword = false;
	public int CountRounds = -1;
	public int StartTimer = -1;
	public ArrayList<String> PlayerNames = new ArrayList<String>();
	public boolean isOpen = false;

	//GET
	public GameDetailsBean() {
	}
	
	//RESULT
	public GameDetailsBean(String gameName, boolean requirePassword,
			int countRounds, int startTimer, ArrayList<String> playerNames,
			boolean isOpen) {
		this.GameName = gameName;
		this.RequirePassword = requirePassword;
		this.CountRounds = countRounds;
		this.StartTimer = startTimer;
		this.PlayerNames = playerNames;
		this.isOpen = isOpen;
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
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME_NAME)) {
						this.GameName = parser.nextText();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME_PASSWORD_REQUIRED)) {
						this.RequirePassword = Boolean.valueOf( parser.nextText() ).booleanValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME_COUNTROUNDS)) {
						this.CountRounds = Integer.valueOf( parser.nextText() ).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME_STARTTIMER)) {
						this.StartTimer = Integer.valueOf( parser.nextText() ).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_PLAYER_NAME)) {
						this.PlayerNames.add( parser.nextText() );
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME_ISOPEN)) {
						this.isOpen = Boolean.valueOf( parser.nextText() ).booleanValue();
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
		GameDetailsBean clone = this.GameName == null
			? new GameDetailsBean()
			: new GameDetailsBean(this.GameName, this.RequirePassword,
					this.CountRounds, this.StartTimer, this.PlayerNames,
					this.isOpen);
		
		return (GameDetailsBean)cloneBasicAttributes(clone);
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();

		if(GameName != null){
			sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME_NAME + ">")
				.append(this.GameName)
				.append("</" + XHuntElements.CHILD_ELEMENT_GAME_NAME + ">");
			sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME_PASSWORD_REQUIRED + ">")
				.append(this.RequirePassword)
				.append("</" + XHuntElements.CHILD_ELEMENT_GAME_PASSWORD_REQUIRED + ">");
			sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME_COUNTROUNDS + ">")
				.append(this.CountRounds)
				.append("</" + XHuntElements.CHILD_ELEMENT_GAME_COUNTROUNDS + ">");
			sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME_STARTTIMER + ">")
				.append(this.StartTimer)
				.append("</" + XHuntElements.CHILD_ELEMENT_GAME_STARTTIMER + ">");
			
			for(String player : PlayerNames){
				sb.append("<" + XHuntElements.CHILD_ELEMENT_PLAYER_NAME + ">")
					.append(player)
					.append("</" + XHuntElements.CHILD_ELEMENT_PLAYER_NAME + ">");
			}
			
			sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME_ISOPEN + ">")
				.append(this.isOpen)
				.append("</" + XHuntElements.CHILD_ELEMENT_GAME_ISOPEN + ">");
		}
		
		sb = appendErrorPayload(sb);

		return sb.toString();
	}
}