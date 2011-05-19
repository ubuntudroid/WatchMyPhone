package de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.GeoPosition;

/**
 * This Bean is responsible for joining a game on the client side
 * Directions: Client -> Server
 * Payloads: (SET) Position, Player ID, Player Name.
 *
 * @author Norbert Harder
 * @author Reik Mueller
 */
public class JoinGameBean extends XMPPBean {

	private static final long serialVersionUID = 1L;
	
	/** The Constant CHILD_ELEMENT. */
	public static final String CHILD_ELEMENT = "query";
    
    /** The Constant NAMESPACE. */
    public static final String NAMESPACE = "mobilislocpairs:iq:joingame";
    
    /** The Constant STATE_READY. */
    public static final boolean STATE_READY = true;
	
	/** The Constant STATE_PENDING. */
	public static final boolean STATE_PENDING = false;
    
    private GeoPosition location;
    private String playerID;
    private String playerName;
    private boolean result;
    
	
	/**
	 * Constructor for type=SET.
	 *
	 * @param location the location
	 * @param playerID the player id
	 * @param playerName the player name
	 */
	public JoinGameBean(GeoPosition location, String playerID, String playerName) {
		super();
		this.location = location;
		this.playerID = playerID;
		this.playerName = playerName;
		this.type=XMPPBean.TYPE_SET;
	}
	
	/**
	 * Constructor for empty bean and type=RESULT.
	 *
	 * @param result the result
	 */
	public JoinGameBean(boolean result) {
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
	public JoinGameBean(String errorType, String errorCondition, String errorText) {
		super(errorType, errorCondition, errorText);
	}
	
	/**
	 * Instantiates a new join game bean.
	 */
	public JoinGameBean() {
		super();
	}
	
	/**
	 * Gets the result bean with test data.
	 *
	 * @return the result bean with test data
	 */
	public static JoinGameBean getResultBeanWithTestData(){
		JoinGameBean result = new JoinGameBean(true);
		return result;
	}
	
	/**
	 * Gets the sets the bean with test data.
	 *
	 * @return the sets the bean with test data
	 */
	public static JoinGameBean getSetBeanWithTestData(){
		return new JoinGameBean(new GeoPosition(1,2,3), "_Alpha_","Alpha");
	}

	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean#clone()
	 */
	@Override
	public JoinGameBean clone() {
		JoinGameBean twin;
		
		if (this.type == XMPPBean.TYPE_SET) twin = new JoinGameBean(location,playerID,playerName);
		else if (this.type == XMPPBean.TYPE_RESULT) twin = new JoinGameBean(result);
		else twin = new JoinGameBean();
		twin = (JoinGameBean) cloneBasicAttributes(twin);
		return twin;
	}


	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean#payloadToXML()
	 */
	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();
		
		if (this.playerID != null)
			sb.append("<playerID>").append(this.playerID).append("</playerID>");
		
		if (this.playerName != null)
			sb.append("<playerName>").append(this.playerName).append("</playerName>");
		
		if (this.location != null)
			sb.append("<location>").append(location.toXML()).append("</location>");
		
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
		String childElement = JoinGameBean.CHILD_ELEMENT;
		boolean done = false;
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
				} else if (tagName.equals("playerID")) {
					playerID = parser.nextText();
					parser.next();
				} else if (tagName.equals("playerName")) {
					playerName = parser.nextText();
					parser.next();
				} else if (tagName.equals("location")) {
					location = new GeoPosition(0,0,0);
					parser.next();
				} else if (tagName.equals("latitude")) {
					location.setLatitude(Double.parseDouble(parser.nextText()));
					parser.next();
				} else if (tagName.equals("longitude")) {
					location.setLongitude(Double.parseDouble(parser.nextText()));
					parser.next();
				} else if (tagName.equals("altitude")) {
					location.setAltitude(Double.parseDouble(parser.nextText()));
					parser.next();
				} else if (tagName.equals("latitude")) {
					location.setLatitude(Double.parseDouble(parser.nextText()));
					parser.next();
				} else if (tagName.equals("result")) {
					this.result = Boolean.valueOf(parser.nextText());
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

	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo#getChildElement()
	 */
	@Override
	public String getChildElement() {
		return JoinGameBean.CHILD_ELEMENT;
	}

	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return JoinGameBean.NAMESPACE;
	
	
	}
	
	/*
	 * Setters & Getters
	 */
	/**
	 * Gets the location.
	 *
	 * @return the location
	 */
	public GeoPosition getLocation() {
		return location;
	}

	/**
	 * Sets the location.
	 *
	 * @param location the new location
	 */
	public void setLocation(GeoPosition location) {
		this.location = location;
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
	 * Gets the player id.
	 *
	 * @return the player id
	 */
	public String getPlayerID() {
		return playerID;
	}

	/**
	 * Sets the player id.
	 *
	 * @param playerID the new player id
	 */
	public void setPlayerID(String playerID) {
		this.playerID = playerID;
	}

	/**
	 * Gets the player name.
	 *
	 * @return the player name
	 */
	public String getPlayerName() {
		return playerName;
	}

	/**
	 * Sets the player name.
	 *
	 * @param playerName the new player name
	 */
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	
}
