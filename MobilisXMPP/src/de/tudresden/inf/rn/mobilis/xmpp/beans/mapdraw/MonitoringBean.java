package de.tudresden.inf.rn.mobilis.xmpp.beans.mapdraw;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

/**
 * @author Robert Lübke
 */
public class MonitoringBean extends XMPPBean {

	private static final long serialVersionUID = 1L;
	public static final String CHILD_ELEMENT = "query";
    public static final String NAMESPACE = "mobilis:iq:monitoring";
	public static final String PING = "ping";
	public static final String START_TIMER = "startTimer";
	private String statusMsg;
	
	/** Constructor for type=SET */
	public MonitoringBean(String statusMsg) {
		super();
		this.statusMsg=statusMsg;
		this.type=XMPPBean.TYPE_SET;
	}
	
	/** Constructor for empty bean and type=RESULT */
	public MonitoringBean() {
		super();
		this.type=XMPPBean.TYPE_RESULT;
	}
	
	/** Constructor for type=ERROR */
	public MonitoringBean(String errorType, String errorCondition, String errorText) {
		super(errorType, errorCondition, errorText);
	}
	
	@Override
	public MonitoringBean clone() {
		MonitoringBean twin = new MonitoringBean(statusMsg);
		
		twin = (MonitoringBean) cloneBasicAttributes(twin);
		return twin;
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();
		
		if (this.statusMsg != null)
			sb.append("<status>").append(this.statusMsg).append("</status>");
				
		sb = appendErrorPayload(sb);			
		return sb.toString();				
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String childElement = MonitoringBean.CHILD_ELEMENT;

		boolean done = false;
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
				} else if (tagName.equals("status")) {
					this.statusMsg = parser.nextText();
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
		return MonitoringBean.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		return MonitoringBean.NAMESPACE;
	}
	
	// Setter & Getter
	public String getStatusMsg() {
		return statusMsg;
	}

	public void setStatusMsg(String statusMsg) {
		this.statusMsg = statusMsg;
	}
	
}
