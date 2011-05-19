package de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt;

import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.model.RoundStatusInfo;

public class RoundStatusBean extends XMPPBean {

	private static final long serialVersionUID = 3767389406318981769L;
	public static final String NAMESPACE = "mobilisxhunt:iq:roundstatus";
	public static final String CHILD_ELEMENT = "query";

	//SET
	public int Round = -1;
	public ArrayList<RoundStatusInfo> RoundStatusInfos = new ArrayList<RoundStatusInfo>();
	
	public RoundStatusBean() {}
	
	//SET
	public RoundStatusBean(int round, ArrayList<RoundStatusInfo> roundStatusInfos){
		this.Round = round;
		this.RoundStatusInfos = roundStatusInfos;
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		boolean done = false;
		RoundStatusInfo roundInfo = new RoundStatusInfo();
		
		do {
			switch (parser.getEventType()){
			
				case XmlPullParser.START_TAG:
					String tagName = parser.getName();
					
					if (tagName.equals(CHILD_ELEMENT)){
						parser.next();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME_ROUND)) {
						this.Round = Integer.valueOf( parser.nextText() ).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_PLAYER_JID)) {
						roundInfo.PlayerJid = parser.nextText();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_STATION_ID)) {
						roundInfo.TargetId = Integer.valueOf(parser.nextText()).intValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_STATION_ISFINAL)) {
						roundInfo.IsTargetFinal = Boolean.valueOf(parser.nextText()).booleanValue();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_STATION_ARRIVED)) {
						roundInfo.TargetReached = Boolean.valueOf(parser.nextText()).booleanValue();
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
					else if (parser.getName().equals(XHuntElements.CHILD_ELEMENT_PLAYER)){
						this.RoundStatusInfos.add(roundInfo);
						roundInfo = new RoundStatusInfo();
						
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
		RoundStatusBean clone = this.Round > -1
			? new RoundStatusBean(this.Round, this.RoundStatusInfos)
			: new RoundStatusBean();
			
		return (RoundStatusBean)cloneBasicAttributes(clone);
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();

		if(this.Round > -1){
			sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME_ROUND + ">")
				.append(this.Round)
				.append("</" + XHuntElements.CHILD_ELEMENT_GAME_ROUND + ">");
			
			for ( RoundStatusInfo info : this.RoundStatusInfos ){
				sb.append("<" + XHuntElements.CHILD_ELEMENT_PLAYER + ">");
				
				sb.append("<" + XHuntElements.CHILD_ELEMENT_PLAYER_JID + ">")
					.append(info.PlayerJid)
					.append("</" + XHuntElements.CHILD_ELEMENT_PLAYER_JID + ">");
				sb.append("<" + XHuntElements.CHILD_ELEMENT_STATION_ID + ">")
					.append(info.TargetId)
					.append("</" + XHuntElements.CHILD_ELEMENT_STATION_ID + ">");
				sb.append("<" + XHuntElements.CHILD_ELEMENT_STATION_ISFINAL + ">")
					.append(info.IsTargetFinal)
					.append("</" + XHuntElements.CHILD_ELEMENT_STATION_ISFINAL + ">");
				sb.append("<" + XHuntElements.CHILD_ELEMENT_STATION_ARRIVED + ">")
					.append(info.TargetReached)
					.append("</" + XHuntElements.CHILD_ELEMENT_STATION_ARRIVED + ">");
				
				sb.append("</" + XHuntElements.CHILD_ELEMENT_PLAYER + ">");
			}
		}
		
		sb = appendErrorPayload(sb);

		return sb.toString();
	}
}