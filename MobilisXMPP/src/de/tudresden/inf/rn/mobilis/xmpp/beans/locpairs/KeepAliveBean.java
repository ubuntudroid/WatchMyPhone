package de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs;


import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.GeoPosition;

/**
 * Implementation for the bean StartRoundBean
 * 
 * The KeepAliveBean is used to tell the server that the client is still playing 
 * and active.
 * 
 * @author Reik Mueller
 */
public class KeepAliveBean extends XMPPBean {
	
	private static final long serialVersionUID = 1L;
	
	/** The Constant CHILD_ELEMENT. */
	public static final String CHILD_ELEMENT = "query";
    
    /** The Constant NAMESPACE. */
    public static final String NAMESPACE = "mobilislocpairs:iq:keepalive";
	
    private String playerId = null;
    private GeoPosition position = null;
    private boolean result;
	
	/**
	 * Constructor for type=SET.
	 *
	 * @param playerId the player id
	 * @param position the position
	 */
	public KeepAliveBean(String playerId, GeoPosition position) {
		super();
		this.position = position;
		this.playerId = playerId;
		this.type=XMPPBean.TYPE_SET;
	}
	
	/**
	 * Constructor for empty bean and type=RESULT.
	 *
	 * @param result the result
	 */
	public KeepAliveBean(boolean result) {
		super();
		this.result = result;
		this.type=XMPPBean.TYPE_RESULT;
	}
	
	/**
	 * Instantiates a new keep alive bean.
	 */
	public KeepAliveBean(){
		super();
	}
	
	/**
	 * Constructor for type=ERROR.
	 *
	 * @param errorType the error type
	 * @param errorCondition the error condition
	 * @param errorText the error text
	 */
	public KeepAliveBean(String errorType, String errorCondition, String errorText) {
		super(errorType, errorCondition, errorText);
	}
	
	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean#clone()
	 */
	@Override
	public KeepAliveBean clone() {
		KeepAliveBean twin = new KeepAliveBean(this.playerId, this.position);
		
		twin = (KeepAliveBean) cloneBasicAttributes(twin);
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
			if(position != null) sb.append(position.toXML());
		
		sb.append("<result>").append(result).append("</result>");
		sb = appendErrorPayload(sb);			
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo#fromXML(org.xmlpull.v1.XmlPullParser)
	 */
	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		GeoPosition pos = new GeoPosition(0, 0, 0);
		String childElement = KeepAliveBean.CHILD_ELEMENT;
		boolean done = false;
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
				} else if (tagName.equals("playerId")) {
					this.playerId = parser.nextText();
				} else if (tagName.equals(GeoPosition.LONGITUDE)) {
					pos.setLongitude(new Double(parser.nextText()));
				} else if (tagName.equals(GeoPosition.LATITUDE)) {
					pos.setLatitude(new Double(parser.nextText()));
				} else if (tagName.equals(GeoPosition.ALTITUDE)) {
					pos.setAltitude(new Double(parser.nextText()));
				} else if (tagName.equals("result")) {
					this.result = Boolean.parseBoolean(parser.nextText());
				} else
					parser.next();
				this.position = pos;
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
		return KeepAliveBean.CHILD_ELEMENT;
	}

	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return KeepAliveBean.NAMESPACE;
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
	 * Checks if is result.
	 *
	 * @return true, if is result
	 */
	public boolean isResult() {
		return result;
	}

	/**
	 * Sets the result.
	 *
	 * @param result the new result
	 */
	public void setResult(boolean result) {
		this.result = result;
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
	
	
	
}
