package de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs;

import java.util.ArrayList;
import java.util.Collection;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

/**
 * The GameInformationBean has to be sent to request the public information of the game
 * these are the name of the opener, the names of the players that already joined and the
 * maximum number of clients allowed to join.
 *  
 * Directions: 
 * Set: Client -> Server
 * Result: Server -> Client
 * 
 * Payloads: 
 * Result: name of the game opener, a collection of the player names and the maximal
 * number of players allowed
 *  
 * 
 * @author Reik Mueller
 */
public class GameInformationBean extends XMPPBean {

	private static final long serialVersionUID = 1L;
	
	/** The Constant CHILD_ELEMENT. */
	public static final String CHILD_ELEMENT = "query";
    
    /** The Constant NAMESPACE. */
    public static final String NAMESPACE = "mobilislocpairs:iq:gameinformation";
    
    /** The Constant STATE_READY. */
    public static final boolean STATE_READY = true;
	
	/** The Constant STATE_PENDING. */
	public static final boolean STATE_PENDING = false;
    
    private String openerName;
    private Collection<String> playerNames;
    private Integer maxMemberCount;
    private boolean result;
	
	/**
	 * Constructor for type=SET.
	 */
	public GameInformationBean() {
		super();
		
	}
	
	/**
	 * Constructor for empty bean and type=RESULT.
	 *
	 * @param openerName the opener name
	 * @param playerNames the player names
	 * @param maxMemberCount the max member count
	 */
	public GameInformationBean(String openerName, Collection<String> playerNames, int maxMemberCount) {
		super();
		this.openerName = openerName;
		this.playerNames = playerNames;
		this.maxMemberCount = maxMemberCount;
		this.type=XMPPBean.TYPE_RESULT;
	}
	
	/**
	 * Constructor for type=ERROR.
	 *
	 * @param errorType the error type
	 * @param errorCondition the error condition
	 * @param errorText the error text
	 */
	public GameInformationBean(String errorType, String errorCondition, String errorText) {
		super(errorType, errorCondition, errorText);
	}
	
	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean#clone()
	 */
	@Override
	public GameInformationBean clone() {
		GameInformationBean twin;
		
		if (this.type == XMPPBean.TYPE_SET) twin = new GameInformationBean();
		else if (this.type == XMPPBean.TYPE_RESULT) twin = new GameInformationBean(openerName,playerNames, maxMemberCount);
		else twin = new GameInformationBean();
		twin = (GameInformationBean) cloneBasicAttributes(twin);
		return twin;
	}


	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean#payloadToXML()
	 */
	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();
		
		if (this.openerName != null)
			sb.append("<openerName>").append(this.openerName).append("</openerName>");
		
		if (this.playerNames != null)
			for(String s : playerNames){
				sb.append("<playerName>").append(s).append("</playerName>");
			}	
		if (this.maxMemberCount != null)
				sb.append("<maxMemberCount>").append(maxMemberCount.toString()).append("</maxMemberCount>");
		
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
		String childElement = GameInformationBean.CHILD_ELEMENT;
		this.playerNames = new ArrayList<String>();
		boolean done = false;
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
				} else if (tagName.equals("openerName")) {
					openerName = parser.nextText();
					parser.next();
				} else if (tagName.equals("playerName")) {
					playerNames.add(parser.nextText());
					parser.next();
				} else if (tagName.equals("maxMemberCount")) {
					maxMemberCount = Integer.valueOf(parser.nextText());
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
		return GameInformationBean.CHILD_ELEMENT;
	}

	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return GameInformationBean.NAMESPACE;
	
	
	}
	
	/*
	 * Setters & Getters
	 */
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
	 * Gets the opener name.
	 *
	 * @return the opener name
	 */
	public String getOpenerName() {
		return openerName;
	}

	/**
	 * Sets the opener name.
	 *
	 * @param openerName the new opener name
	 */
	public void setOpenerName(String openerName) {
		this.openerName = openerName;
	}

	/**
	 * Gets the player names.
	 *
	 * @return the player names
	 */
	public Collection<String> getPlayerNames() {
		return playerNames;
	}

	/**
	 * Sets the player names.
	 *
	 * @param playerNames the new player names
	 */
	public void setPlayerNames(Collection<String> playerNames) {
		this.playerNames = playerNames;
	}

	/**
	 * Gets the max member count.
	 *
	 * @return the max member count
	 */
	public Integer getMaxMemberCount() {
		return maxMemberCount;
	}

	/**
	 * Sets the max member count.
	 *
	 * @param maxMemberCount the new max member count
	 */
	public void setMaxMemberCount(Integer maxMemberCount) {
		this.maxMemberCount = maxMemberCount;
	}

}
