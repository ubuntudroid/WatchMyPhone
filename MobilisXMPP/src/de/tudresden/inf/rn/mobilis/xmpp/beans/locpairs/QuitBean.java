package de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs;

import org.xmlpull.v1.XmlPullParser;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;


/**
 * This Bean is responsible for notifying the server and other clients, that a player
 * has left the game
 * 
 * Directions
 * Set: Client -> Server AND Server -> multiple Clients
 * Result: Client -> Server ; Server -> Client
 * 
 * Payloads
 * Set: Player ID
 * Result: Ok.
 *
 * @author Norbert Harder
 */
public class QuitBean extends XMPPBean {

	private static final long serialVersionUID = 1L;
	
	/** The Constant CHILD_ELEMENT. */
	public static final String CHILD_ELEMENT = "query";
    
    /** The Constant NAMESPACE. */
    public static final String NAMESPACE = "mobilislocpairs:iq:quitgame";

    private String playerID;
    private boolean result;

	
	/**
	 * Constructor for type=SET.
	 *
	 * @param playerID the player id
	 */
	public QuitBean(String playerID) {
		super();
		this.playerID = playerID;
		this.type=XMPPBean.TYPE_SET;
	}
	
	/**
	 * Constructor for empty bean and type=RESULT.
	 *
	 * @param result the result
	 */
	public QuitBean(boolean result) {
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
	public QuitBean(String errorType, String errorCondition, String errorText) {
		super(errorType, errorCondition, errorText);
	}
	
	/**
	 * Instantiates a new quit bean.
	 */
	public QuitBean() {
	}

	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean#clone()
	 */
	@Override
	public QuitBean clone() {
		QuitBean twin;
		
		if (this.type == XMPPBean.TYPE_SET) twin = new QuitBean(playerID);
		else if (this.type == XMPPBean.TYPE_RESULT) twin = new QuitBean(result);
		else twin = new QuitBean();
		twin = (QuitBean) cloneBasicAttributes(twin);
		return twin;
	}
	
	/**
	 * Gets the sets the bean with test data.
	 *
	 * @return the sets the bean with test data
	 */
	public static QuitBean getSetBeanWithTestData(){
		return new QuitBean("AlphaID");
	}
	
	/**
	 * Gets the result bean with test data.
	 *
	 * @return the result bean with test data
	 */
	public static QuitBean getResultBeanWithTestData(){
		return new QuitBean(true);
	}

	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean#payloadToXML()
	 */
	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();

		if (this.playerID != null)
			sb.append("<playerID>").append(this.playerID).append("</playerID>");

		sb = appendErrorPayload(sb);			
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo#fromXML(org.xmlpull.v1.XmlPullParser)
	 */
	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String childElement = QuitBean.CHILD_ELEMENT;
		
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
		return QuitBean.CHILD_ELEMENT;
	}

	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return QuitBean.NAMESPACE;
	
	
	}
	
	/*
	 * Setters & Getters
	 */
	/**
	 * Gets the player id.
	 *
	 * @return the player id
	 */
	public String getPlayerID() {
		return playerID;
	}
	
	/**
	 * Gets the result.
	 *
	 * @return the result
	 */
	public boolean getResult() {
		return result;
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
	 * Sets the result.
	 *
	 * @param result the new result
	 */
	public void setResult(boolean result){
		this.result = result;
	}
}
