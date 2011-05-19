package de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs;


import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.GeoPosition;

/**
 * Implementation for the GoThereBean. 
 * The GoThereBean is used to tell an other player where to go.
 *  
 * 
 * @author Reik Mueller
 */
public class GoThereBean extends XMPPBean {

	private static final long serialVersionUID = 1L;
	
	/** The Constant CHILD_ELEMENT. */
	public static final String CHILD_ELEMENT = "query";
    
    /** The Constant NAMESPACE. */
    public static final String NAMESPACE = "mobilislocpairs:iq:gothere";
	
    private String playerId = null;
    private GeoPosition position = new GeoPosition(0, 0, 0);
    private boolean result;
	
	/**
	 * Constructor for type=SET.
	 *
	 * @param playerId the player id
	 * @param position the position
	 */
	public GoThereBean(String playerId, GeoPosition position) {
		super();
		this.playerId = playerId;
		this.position = position;
		this.type=XMPPBean.TYPE_SET;
	}
	
	/**
	 * Constructor for empty bean and type=RESULT.
	 *
	 * @param result the result
	 */
	public GoThereBean(boolean result) {
		super();
		this.result = result;
		this.type=XMPPBean.TYPE_RESULT;
	}
	
	/**
	 * Constructor for type=ERROR.
	 *
	 * @param errorType the error type
	 * @param errorCondition the error condition
	 * @param errorText the error text
	 */
	public GoThereBean(String errorType, String errorCondition, String errorText) {
		super(errorType, errorCondition, errorText);
	}
	
	/**
	 * Instantiates a new go there bean.
	 */
	public GoThereBean() {
		
		super();
	}

	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean#clone()
	 */
	@Override
	public GoThereBean clone() {
		GoThereBean twin = new GoThereBean(
		this.playerId, 
		this.position);
		
		twin = (GoThereBean) cloneBasicAttributes(twin);
		return twin;
	}

	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean#payloadToXML()
	 */
	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();
		
		if (this.playerId != null)
			sb.append("<playerId>").append(playerId).append("</playerId>");
		if (this.position != null)
			sb.append("<longitude>").append(Double.toString(this.position.getLongitude())).append("</longitude>").
			append("<latitude>").append(Double.toString(this.position.getLatitude())).append("</latitude>");
		
		if (this.type == XMPPBean.TYPE_RESULT)
			sb.append("<result>").append(result).append("</result>");
		
		sb = appendErrorPayload(sb);			
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo#fromXML(org.xmlpull.v1.XmlPullParser)
	 */
	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String childElement = GoThereBean.CHILD_ELEMENT;
		boolean done = false;
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
				} else if (tagName.equals("playerId")) {
					this.playerId = parser.nextText();
				} else if (tagName.equals("longitude")) {
					this.position.setLongitude(Double.valueOf(parser.nextText()));
				}else if (tagName.equals("latitude")) {
					this.position.setLatitude(Double.valueOf(parser.nextText()));
				}else if (tagName.equals("result")) {
					this.result = Boolean.valueOf(parser.nextText());
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
	
	
	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo#getChildElement()
	 */
	@Override
	public String getChildElement() {
		return GoThereBean.CHILD_ELEMENT;
	}

	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return GoThereBean.NAMESPACE;
	}


	// Setter & Getter
	
	/**
	 * Gets the player id.
	 *
	 * @return the player id
	 */
	public String getPlayerId() {
		return playerId;
	}

	/**
	 * Sets the player id.
	 *
	 * @param playerId the new player id
	 */
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	/**
	 * Gets the position.
	 *
	 * @return the position
	 */
	public GeoPosition getPosition() {
		return position;
	}

	/**
	 * Sets the position.
	 *
	 * @param position the new position
	 */
	public void setPosition(GeoPosition position) {
		this.position = position;
	}

	/**
	 * Sets the result.
	 *
	 * @param result the new result
	 */
	public void setResult(boolean result) {
		this.result = result;
	}
	
}