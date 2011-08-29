package edu.bonn.cs.wmp.xmpp.beans;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

public class ViewportBean extends WMPBean {

	private static final long serialVersionUID = 1L;
	public static final String NAMESPACE = "wmp:iq:viewport";
	public static final String CHILD_ELEMENT = "query";
	protected float viewportStart;
	protected float viewportEnd;
	
	public float getViewportStart() {
		return viewportStart;
	}

	public void setViewportStart(float viewportStart) {
		this.viewportStart = viewportStart;
	}

	public float getViewportEnd() {
		return viewportEnd;
	}

	public void setViewportEnd(float viewportEnd) {
		this.viewportEnd = viewportEnd;
	}

	/** Constructor for type=SET */
	public ViewportBean(int wmpId) {
		super(wmpId);
		this.type = XMPPBean.TYPE_SET;
	}
	
	/** Constructor for type=ERROR */
	public ViewportBean(String errorType, String errorCondition, String errorText) {
		super(errorType, errorCondition, errorText);
	}
	
	@Override
	public ViewportBean clone() {
		ViewportBean twin = new ViewportBean(wmpId);
		
		twin = (ViewportBean) cloneBasicAttributes(twin);
		return twin;
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("<viewport_start>").append(this.viewportStart).append("</viewport_start>");
		sb.append("<viewport_end>").append(this.viewportEnd).append("</viewport_end>");
//		sb = appendErrorPayload(sb);
		
		sb = appendErrorPayloadAndWMPId(sb);
		return sb.toString();
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		//TODO: age wmpId-handling to WMPBean 
//		super.fromXML(parser);
		
		String childElement = ViewportBean.CHILD_ELEMENT;
		
		boolean done = false;
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
				} else if (tagName.equals("wmpId")) {
					this.wmpId = Integer.parseInt(parser.nextText());
				} else if (tagName.equals("viewport_start")) {
					this.viewportStart = Float.parseFloat(parser.nextText());
				} else if (tagName.equals("viewport_end")) {
					this.viewportEnd = Float.parseFloat(parser.nextText());
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
		return ViewportBean.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		// TODO Auto-generated method stub
		return ViewportBean.NAMESPACE;
	}


}
