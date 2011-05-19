package de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

public class PlayerExitBean extends XMPPBean {

	private static final long serialVersionUID = 656154501863411893L;
	public static final String NAMESPACE = "mobilisxhunt:iq:playerexit";
	public static final String CHILD_ELEMENT = "query";

	//SET and RESULT
	public String Jid;
	public boolean isSpectator = false;
	
	public PlayerExitBean() {}
	
	//SET
	public PlayerExitBean(String jid){
		this.Jid = jid;
		this.type=XMPPBean.TYPE_SET;
	}
	public PlayerExitBean(String jid, boolean isSpectator){
		this.Jid = jid;
		this.isSpectator=isSpectator;
		this.type=XMPPBean.TYPE_SET;
	}
	
	//ERROR
	public PlayerExitBean(String errorType, String errorCondition, String errorText) {
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
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_PLAYER_JID)) {
						this.Jid = parser.nextText();
					}
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_PLAYER_ISSPECTATOR)) {
						this.isSpectator = Boolean.parseBoolean( parser.nextText() );
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
		PlayerExitBean clone = new PlayerExitBean(this.Jid);
		
		return (PlayerExitBean)cloneBasicAttributes(clone);
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();

		if (this.type==XMPPBean.TYPE_SET) {
			if (this.Jid != null){
				sb.append("<" + XHuntElements.CHILD_ELEMENT_PLAYER_JID + ">")
					.append(this.Jid)
					.append("</" + XHuntElements.CHILD_ELEMENT_PLAYER_JID + ">");
			}
			sb.append("<" + XHuntElements.CHILD_ELEMENT_PLAYER_ISSPECTATOR + ">")
				.append(this.isSpectator)
				.append("</" + XHuntElements.CHILD_ELEMENT_PLAYER_ISSPECTATOR + ">");
		}
		sb = appendErrorPayload(sb);
		return sb.toString();
	}
}