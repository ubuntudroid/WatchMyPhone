package edu.bonn.cs.wmp.xmpp.beans;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

public abstract class WMPBean extends XMPPBean {
	
	private static final long serialVersionUID = 1L;
	
	protected String wmpId;
	public static final String CHILD_ELEMENT = "query";
	
	public WMPBean(String errorType, String errorCondition, String errorText) {
		super(errorType, errorCondition, errorText);
	}

	public WMPBean(String wmpId) {
		super();
		this.wmpId = wmpId;
	}

	public WMPBean() {
		super();
		this.wmpId = getIdFromRegistry();
	}

	private String getIdFromRegistry() {
		// TODO Auto-generated method stub
		return "";
	}

	public String getWmpId() {
		return wmpId;
	}

	public void setWmpId(String wmpId) {
		this.wmpId = wmpId;
	}
	
	StringBuilder appendErrorPayloadAndWMPId(StringBuilder sb){
		sb.append("<wmpId>").append(this.wmpId).append("</wmpId>");
		sb = appendErrorPayload(sb);
		return sb;
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String childElement = WMPBean.CHILD_ELEMENT;
		
		boolean done = false;
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
				} else if (tagName.equals("wmpId")) {
					this.wmpId = parser.nextText();
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

}
