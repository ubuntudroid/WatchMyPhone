package edu.bonn.cs.wmp.xmpp.beans;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

public class ButtonBean extends WMPBean {

	private static final long serialVersionUID = 1L;
	public static final String NAMESPACE = "wmp:iq:button";
	public static final String CHILD_ELEMENT = "query";
	
	/** Constructor for type=SET */
	public ButtonBean(String wmpId) {
		super(wmpId);
		this.type = XMPPBean.TYPE_SET;
	}
	
	/** Constructor for empty bean and type=RESULT */
	public ButtonBean() {
		super();
		this.type = XMPPBean.TYPE_RESULT;
	}
	
	/** Constructor for type=ERROR */
	public ButtonBean(String errorType, String errorCondition, String errorText) {
		super(errorType, errorCondition, errorText);
	}
	
	@Override
	public XMPPBean clone() {
		ButtonBean twin = new ButtonBean(wmpId);
		
		twin = (ButtonBean) cloneBasicAttributes(twin);
		return twin;
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();
		
//		sb.append("<wmpId>").append(this.wmpId).append("</wmpId>");
//		sb = appendErrorPayload(sb);
		
		sb = appendErrorPayloadAndWMPId(sb);
		return sb.toString();
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		//TODO: age wmpId-handling to WMPBean 
//		super.fromXML(parser);
		
		String childElement = ButtonBean.CHILD_ELEMENT;
		
		boolean done = false;
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
				} else if (tagName.equals("wmpId")) {
					this.wmpId = parser.nextText();
				} else if (tagName.equals("error")) {
					parser = parseErrorAttributes(parser);
				} else
					parser.next();
				break;
			case XmlPullParser.END_TAG:
				if (parser.getName().equals(childElement))
					done = true;
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
		// TODO Auto-generated method stub
		return ButtonBean.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		// TODO Auto-generated method stub
		return ButtonBean.NAMESPACE;
	}


}
