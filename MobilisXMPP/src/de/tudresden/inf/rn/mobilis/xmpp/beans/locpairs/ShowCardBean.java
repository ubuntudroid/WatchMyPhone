package de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs;


import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

/**
 * Implementation for the bean ShowCardBean.
 * Is sent from the server to all Clients to notify them about what card
 * has been uncovered. It contains the bar code ID and the player ID of 
 * the player that had uncovered the card by scanning an bar code.
 * 
 * Directions
 * Set: Server -> Client
 * Result: Client -> Server
 * 
 * Payloads
 * Set: barCodeId, playerId
 *
 * @author Reik Mueller
 */
public class ShowCardBean extends XMPPBean {
	private static final long serialVersionUID = 1L;
	
	/** The Constant CHILD_ELEMENT. */
	public static final String CHILD_ELEMENT = "query";
    
    /** The Constant NAMESPACE. */
    public static final String NAMESPACE = "mobilislocpairs:iq:showcard";
	
    private String barCodeId = null;
    private String playerId = null;
    private boolean result;
    
    /**
     * standard Constructor (type = SET) *.
     */
    public ShowCardBean(){
    	super();
    	this.type = XMPPBean.TYPE_SET;
    }
	
	/**
	 * Constructor for type=SET.
	 *
	 * @param barCodeId the bar code id
	 * @param playerId the player id
	 */
	public ShowCardBean(String barCodeId, String playerId) {
		super();
		this.barCodeId = barCodeId;
		this.playerId = playerId;
		this.type=XMPPBean.TYPE_SET;
	}
	
	/**
	 * Constructor for empty bean and type=RESULT.
	 *
	 * @param result the result
	 */
	public ShowCardBean(boolean result) {
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
	public ShowCardBean(String errorType, String errorCondition, String errorText) {
		super(errorType, errorCondition, errorText);
	}
	
	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean#clone()
	 */
	@Override
	public ShowCardBean clone() {
		
		ShowCardBean twin = null;
		if(this.type == XMPPBean.TYPE_RESULT){
			twin = new ShowCardBean(this.result);
		} else if (this.type == XMPPBean.TYPE_SET){
			twin = new ShowCardBean(
					this.barCodeId,
					this.playerId);
		}
		twin = (ShowCardBean) cloneBasicAttributes(twin);
		return twin;
	}

	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean#payloadToXML()
	 */
	@Override
	public String payloadToXML() {
		
		StringBuilder sb = new StringBuilder();
		
		switch(this.type){
		case XMPPBean.TYPE_RESULT:
				sb.append("<result>").append(result).append("</result>");
			break;
		case XMPPBean.TYPE_SET:
			if (this.playerId != null)
				sb.append("<playerId>").append(playerId).append("</playerId>");
			if (this.barCodeId != null)
				sb.append("<barCodeId>").append(this.barCodeId).append("</barCodeId>");
			break;
		}
		sb = appendErrorPayload(sb);			
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo#fromXML(org.xmlpull.v1.XmlPullParser)
	 */
	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String childElement = ShowCardBean.CHILD_ELEMENT;
		boolean done = false;
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
				} else if (tagName.equals("playerId")) {
					this.playerId = parser.nextText();
				} else if (tagName.equals("barCodeId")) {
					this.barCodeId = parser.nextText();
				}else
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

	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo#getChildElement()
	 */
	@Override
	public String getChildElement() {
		return ShowCardBean.CHILD_ELEMENT;
	}

	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return ShowCardBean.NAMESPACE;
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
	 * Gets the bar code id.
	 *
	 * @return the bar code id
	 */
	public String getBarCodeId() {
		return barCodeId;
	}

	/**
	 * Sets the bar code id.
	 *
	 * @param barCodeId the new bar code id
	 */
	public void setBarCodeId(String barCodeId) {
		this.barCodeId = barCodeId;
	}	
}
