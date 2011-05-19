package de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt;

import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

public class JoinGameBean extends XMPPBean {

	private static final long serialVersionUID = -1788423101848455230L;
	public static final String NAMESPACE = "mobilisxhunt:iq:joingame";
	public static final String CHILD_ELEMENT = "query";
	
	//SET
	public String GamePassword;
	public String PlayerName;
	public boolean isSpectator = false;
	
	//RESULT
	public String ChatRoom;
	public String ChatPassword;
	public int StartTimer = -1;
	public ArrayList<String> IncomingGameFileNames = new ArrayList<String>();
	
	public JoinGameBean() {}
	
	//SET
	/**
	 * Constructor for a JoinGameBean with type SET.
	 * Used to let a normal player (not a spectator!) join the game.
	 * @param gamePassword the password that is required to join the game
	 * @param playername the name of the person who wants to join the game
	 */
	public JoinGameBean(String gamePassword, String playername){
		this.GamePassword = gamePassword;
		this.PlayerName = playername;
		this.type=XMPPBean.TYPE_SET;
	}
	
	/**
	 * Constructor for a JoinGameBean with type SET.
	 * Used to let a normal player or a spectator join the game.
	 * @param gamePassword the password that is required to join the game
	 * @param playername the name of the person who wants to join the game
	 * @param isSpectator set to true if the joining person does not play,
	 * but only spectates at the game. false otherwise. 
	 */
	public JoinGameBean(String gamePassword, String playername, boolean isSpectator){
		this.GamePassword = gamePassword;
		this.PlayerName = playername;
		this.isSpectator = isSpectator;
		this.type=XMPPBean.TYPE_SET;
	}
	
	//RESULT
	public JoinGameBean(String chatRoom, String chatPassword, int startTimer,
			ArrayList<String> incomingGameFileNames){
		this.ChatRoom = chatRoom;
		this.ChatPassword = chatPassword;
		this.StartTimer = startTimer;
		this.IncomingGameFileNames = incomingGameFileNames;
		this.type=XMPPBean.TYPE_RESULT;
	}
	
	//ERROR
	public JoinGameBean(String errorType, String errorCondition, String errorText) {
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
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME_PASSWORD)) {
						this.GamePassword = parser.nextText();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_PLAYER_NAME)) {
						this.PlayerName = parser.nextText();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_PLAYER_ISSPECTATOR)) {
						this.isSpectator = Boolean.parseBoolean( parser.nextText() );
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_CHAT_ROOM)) {
						this.ChatRoom = parser.nextText();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_CHAT_PASSWORD)) {
						this.ChatPassword = parser.nextText();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME_STARTTIMER)) {
						this.StartTimer = Integer.valueOf( parser.nextText() ).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME_INCOMING_FILENAME)) {
						this.IncomingGameFileNames.add(parser.nextText());
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
		JoinGameBean clone =  this.ChatRoom != null 
			? new JoinGameBean ( ChatRoom, ChatPassword, StartTimer, IncomingGameFileNames )
			: new JoinGameBean ( GamePassword, PlayerName );
		clone.isSpectator = this.isSpectator;
		return (JoinGameBean) cloneBasicAttributes(clone);
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();

		//SET
		if (this.type==XMPPBean.TYPE_SET) {
			if(GamePassword != null)
				sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME_PASSWORD + ">").append(GamePassword)
					.append("</" + XHuntElements.CHILD_ELEMENT_GAME_PASSWORD + ">");
			if(PlayerName != null)
				sb.append("<" + XHuntElements.CHILD_ELEMENT_PLAYER_NAME + ">").append(PlayerName)
					.append("</" + XHuntElements.CHILD_ELEMENT_PLAYER_NAME + ">");
			sb.append("<" + XHuntElements.CHILD_ELEMENT_PLAYER_ISSPECTATOR + ">").append(isSpectator)
				.append("</" + XHuntElements.CHILD_ELEMENT_PLAYER_ISSPECTATOR + ">");
		}
		
		//RESULT
		if(ChatRoom != null){
			sb.append("<" + XHuntElements.CHILD_ELEMENT_CHAT_ROOM + ">")
				.append(ChatRoom)
				.append("</" + XHuntElements.CHILD_ELEMENT_CHAT_ROOM + ">");
			sb.append("<" + XHuntElements.CHILD_ELEMENT_CHAT_PASSWORD + ">")
				.append(ChatPassword)
				.append("</" + XHuntElements.CHILD_ELEMENT_CHAT_PASSWORD + ">");
			sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME_STARTTIMER + ">")
				.append(StartTimer)
				.append("</" + XHuntElements.CHILD_ELEMENT_GAME_STARTTIMER + ">");
			
			sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME_INCOMING_FILES + ">");
			for(String fileName : IncomingGameFileNames){
				sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME_INCOMING_FILENAME + ">")
					.append(fileName)
					.append("</" + XHuntElements.CHILD_ELEMENT_GAME_INCOMING_FILENAME + ">");
			}
			sb.append("</" + XHuntElements.CHILD_ELEMENT_GAME_INCOMING_FILES + ">");
		}
		
		sb = appendErrorPayload(sb);

		return sb.toString();
	}
}