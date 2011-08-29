package edu.bonn.cs.wmp.xmpp.beans;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

public abstract class WMPBean extends XMPPBean {
	
	private static final long serialVersionUID = 1L;
	
	protected int wmpId;
	public static final String CHILD_ELEMENT = "query";
	
	public WMPBean(String errorType, String errorCondition, String errorText) {
		super(errorType, errorCondition, errorText);
	}

	public WMPBean(int wmpId) {
		super();
		this.wmpId = wmpId;
	}
	
	/**
	 * This constructor shouldn't be used any more as the view
	 * has to add it's R.id as wmpId by contract.
	 */
	@Deprecated
	public WMPBean() {
		super();
		this.wmpId = getIdFromRegistry();
	}

	/**
	 *  This method is obsolete, as the view has to add it's
	 *  R.id as wmpId by contract.
	 */
	@Deprecated
	private int getIdFromRegistry() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getWmpId() {
		return wmpId;
	}

	public void setWmpId(int wmpId) {
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
					this.wmpId = Integer.parseInt(parser.nextText());
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
