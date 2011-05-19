package edu.bonn.cs.wmp.xmpp.beans;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

public class PingBean extends XMPPBean {

	private static final long serialVersionUID = 1L;
	public static final String NAMESPACE = "wmp:iq:ping";
	public static final String CHILD_ELEMENT = "query";
	
	private String message = "";
	private int counter = -1;
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

	/** Constructor for type=SET */
	public PingBean(String message, int counter) {
		super();
		this.message = message;
		this.counter = counter;
		this.type = XMPPBean.TYPE_SET;
	}
	
	/** Constructor for empty bean and type=RESULT */
	public PingBean() {
		super();
		this.type = XMPPBean.TYPE_RESULT;
	}
	
	/** Constructor for type=ERROR */
	public PingBean(String errorType, String errorCondition, String errorText) {
		super(errorType, errorCondition, errorText);
	}
	
	@Override
	public XMPPBean clone() {
		PingBean twin = new PingBean(message, counter);
		
		twin = (PingBean) cloneBasicAttributes(twin);
		return twin;
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("<message>").append(this.message).append("</message>");
		sb.append("<counter>").append(this.counter).append("</counter>");
		
		sb = appendErrorPayload(sb);			
		return sb.toString();
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String childElement = PingBean.CHILD_ELEMENT;
		
		boolean done = false;
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
				} else if (tagName.equals("message")) {
					this.message = parser.nextText();
				} else if (tagName.equals("counter")) {
					this.counter = Integer.valueOf(parser.nextText());
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
		return PingBean.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		// TODO Auto-generated method stub
		return PingBean.NAMESPACE;
	}

}
