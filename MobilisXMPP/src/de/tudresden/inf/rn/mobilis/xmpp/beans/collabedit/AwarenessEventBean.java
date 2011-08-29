package de.tudresden.inf.rn.mobilis.xmpp.beans.collabedit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.Base64;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

public class AwarenessEventBean extends XMPPBean {

	private static final long serialVersionUID = 1L;
	public static final String NAMESPACE = "wmp:iq:awarenessevent";
	public static final String CHILD_ELEMENT = "query";
	
//	protected String eventDescription;
//	protected String eventType;
//	protected String eventSource;
	protected Object event;
	
//	public String getEventDescription() {
//		return eventDescription;
//	}
//
//	public void setEventDescription(String eventDescription) {
//		this.eventDescription = eventDescription;
//	}
//
//	public String getEventType() {
//		return eventType;
//	}
//
//	public void setEventType(String eventType) {
//		this.eventType = eventType;
//	}
//
//	public String getEventSource() {
//		return eventSource;
//	}
//
//	public void setEventSource(String eventSource) {
//		this.eventSource = eventSource;
//	}

	public Object getEvent() {
		return event;
	}

	public void setEvent(Object event) {
		this.event = event;
	}

	/** Constructor for type=SET */
	public AwarenessEventBean() {
		this.type = XMPPBean.TYPE_SET;
	}

	public AwarenessEventBean(String errorType, String errorCondition,
			String errorText) {
		super(errorType, errorCondition, errorText);
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String childElement = AwarenessEventBean.CHILD_ELEMENT;
		
		boolean done = false;
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
//				} else if (tagName.equals("event_description")) {
//					this.eventDescription = parser.nextText();
//				} else if (tagName.equals("event_type")) {
//					this.eventType = parser.nextText();
//				} else if (tagName.equals("event_source")) {
//					this.eventSource = parser.nextText();
				} else if (tagName.equals("event")) {
					byte[] bytes = Base64.decode(parser.nextText());
					
					// Deserialize from byte array
				    ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
				    this.event = (Object) in.readObject();
				    in.close();
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
		return AwarenessEventBean.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		return AwarenessEventBean.NAMESPACE;
	}

	@Override
	public XMPPBean clone() {
		AwarenessEventBean twin = new AwarenessEventBean();
		
		twin = (AwarenessEventBean) cloneBasicAttributes(twin);
		return twin;
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();
		
//		sb.append("<event_description>").append(this.eventDescription).append("</event_description>");
//		sb.append("<event_type>").append(this.eventType).append("</event_type>");
//		sb.append("<event_source>").append(this.eventSource).append("</event_source>");
		
		// Serialize to a byte array
	    ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
	    ObjectOutputStream out = null;
		try {
	    	out = new ObjectOutputStream(bos); 
	    	out.writeObject(this.event);
			out.close();
			byte[] buf = bos.toByteArray();
			sb.append("<event>").append(Base64.encode(buf)).append("</event>");
		} catch (IOException e) {
			e.printStackTrace();
		}

		sb = appendErrorPayload(sb);
		return sb.toString();
	}

}
