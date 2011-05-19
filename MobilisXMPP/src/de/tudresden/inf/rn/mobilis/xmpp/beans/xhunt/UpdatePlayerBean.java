package de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.model.PlayerInfo;

public class UpdatePlayerBean extends XMPPBean {

	private static final long serialVersionUID = 750295526785549993L;
	public static final String NAMESPACE = "mobilisxhunt:iq:updateplayer";
	public static final String CHILD_ELEMENT = "query";

	//SET
	public PlayerInfo PlayerInfo;
	public String Info;
	
	public UpdatePlayerBean() {}
	
	//SET
	public UpdatePlayerBean(PlayerInfo playerInfo){
		this.PlayerInfo = playerInfo;
	}
	
	//RESULT
	public UpdatePlayerBean(String info){
		this.Info = info;
	}
	
	//ERROR
	public UpdatePlayerBean(String errorType, String errorCondition, String errorText) {
		super(errorType, errorCondition, errorText);	
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		boolean done = false;
		this.PlayerInfo = new PlayerInfo();
		
		do {
			switch (parser.getEventType()){
			
				case XmlPullParser.START_TAG:
					String tagName = parser.getName();
					
					if (tagName.equals(CHILD_ELEMENT)){
						parser.next();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_PLAYER_JID)) {
						PlayerInfo.Jid = parser.nextText();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_PLAYER_NAME)) {
						PlayerInfo.PlayerName = parser.nextText();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_PLAYER_ISMODERATOR)) {
						PlayerInfo.IsModerator = Boolean.valueOf( parser.nextText() ).booleanValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_PLAYER_ISMRX)) {
						PlayerInfo.IsMrX = Boolean.valueOf( parser.nextText() ).booleanValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_PLAYER_ISREADY)) {
						PlayerInfo.IsReady = Boolean.valueOf( parser.nextText() ).booleanValue();
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
		UpdatePlayerBean clone =  this.PlayerInfo != null
			? new UpdatePlayerBean(this.PlayerInfo)
			: new UpdatePlayerBean(this.Info);
		
		return (UpdatePlayerBean)cloneBasicAttributes(clone);
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();

		if(this.PlayerInfo != null){
			sb.append("<" + XHuntElements.CHILD_ELEMENT_PLAYER_JID + ">")
				.append(PlayerInfo.Jid)
				.append("</" + XHuntElements.CHILD_ELEMENT_PLAYER_JID + ">");	
			sb.append("<" + XHuntElements.CHILD_ELEMENT_PLAYER_NAME + ">")
				.append(PlayerInfo.PlayerName)
				.append("</" + XHuntElements.CHILD_ELEMENT_PLAYER_NAME + ">");	
			sb.append("<" + XHuntElements.CHILD_ELEMENT_PLAYER_ISMODERATOR + ">")
				.append(PlayerInfo.IsModerator)
				.append("</" + XHuntElements.CHILD_ELEMENT_PLAYER_ISMODERATOR + ">");				
			sb.append("<" + XHuntElements.CHILD_ELEMENT_PLAYER_ISMRX + ">")
				.append(PlayerInfo.IsMrX)
				.append("</" + XHuntElements.CHILD_ELEMENT_PLAYER_ISMRX + ">");				
			sb.append("<" + XHuntElements.CHILD_ELEMENT_PLAYER_ISREADY + ">")
				.append(PlayerInfo.IsReady)
				.append("</" + XHuntElements.CHILD_ELEMENT_PLAYER_ISREADY + ">");
		}
		else if(this.Info != null){
			sb.append("<" + XHuntElements.CHILD_ELEMENT_INFO + ">")
				.append(this.Info)
				.append("</" + XHuntElements.CHILD_ELEMENT_INFO + ">");
		}
		
		sb = appendErrorPayload(sb);

		return sb.toString();
	}
}