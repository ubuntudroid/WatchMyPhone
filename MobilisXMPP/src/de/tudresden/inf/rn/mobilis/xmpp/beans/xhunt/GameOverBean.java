package de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

public class GameOverBean extends XMPPBean {

	private static final long serialVersionUID = 2524200135646998968L;
	public static final String NAMESPACE = "mobilisxhunt:iq:gameover";
	public static final String CHILD_ELEMENT = "query";

	//SET and RESULT
	public String Reason;
	
	public GameOverBean() {}
	
	//SET
	public GameOverBean(String reason){
		this.Reason = reason;
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
					else if (tagName.equals(XHuntElements.CHILD_ELEMENT_GAME_OVERREASON)) {
						this.Reason = parser.nextText();
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
		GameOverBean clone = this.Reason != null
			? new GameOverBean(this.Reason)
			: new GameOverBean();
		
		return (GameOverBean)cloneBasicAttributes(clone);
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();

		if(this.Reason != null){
			sb.append("<" + XHuntElements.CHILD_ELEMENT_GAME_OVERREASON + ">")
				.append(this.Reason)
				.append("</" + XHuntElements.CHILD_ELEMENT_GAME_OVERREASON + ">");
		}
		
		sb = appendErrorPayload(sb);

		return sb.toString();
	}
}