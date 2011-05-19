package de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs;

import java.text.SimpleDateFormat;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.LocPairsDateFormat;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.NetworkFingerPrint;

/**
 * Implementation for the bean UncovercardBean the UncovercardBean is sent from
 * a client (after scanning a bar code) to the server to notify the server what
 * card has been uncovered. It contains the id of the player that has uncovered
 * the card, the bar code that was been scanned, the network finger print that
 * was detected during the scanning process and the time stamp of the scanning
 * process.
 * 
 * Directions Set: Client -> Server Result: Server -> Client
 * 
 * Payloads Set: barCodeId, networkFingerPrint, timeStamp, playerId
 * 
 * @author Reik Mueller
 * 
 */
public class UncoverCardBean extends XMPPBean {

	private static final long serialVersionUID = 1L;

	/** The Constant CHILD_ELEMENT. */
	public static final String CHILD_ELEMENT = "query";

	/** The Constant NAMESPACE. */
	public static final String NAMESPACE = "mobilislocpairs:iq:uncovercard";

	/** The Constant sdf. */
	public static final SimpleDateFormat sdf = LocPairsDateFormat.getFormat();

	private String barCodeId = null;
	private NetworkFingerPrint networkFingerPrint = new NetworkFingerPrint();
	private String playerId = null;
	private String timeStamp = null;
	private boolean result = false;

	/**
	 * standard Constructor (type = SET) *.
	 */
	public UncoverCardBean() {
		super();
		this.type = XMPPBean.TYPE_SET;
	}

	/**
	 * Constructor for type=SET.
	 * 
	 * @param barCodeId
	 *            the bar code id
	 * @param networkFingerPrint
	 *            the network finger print
	 * @param timeStamp
	 *            the time stamp
	 * @param playerId
	 *            the player id
	 */
	public UncoverCardBean(String barCodeId,
			NetworkFingerPrint networkFingerPrint, String timeStamp,
			String playerId) {
		super();
		this.playerId = playerId;
		this.barCodeId = barCodeId;
		this.networkFingerPrint = networkFingerPrint;
		this.timeStamp = timeStamp;
		this.type = XMPPBean.TYPE_SET;
	}

	/**
	 * Constructor for empty bean and type=RESULT.
	 * 
	 * @param result
	 *            the result
	 */
	public UncoverCardBean(boolean result) {
		super();
		this.result = result;
		this.type = XMPPBean.TYPE_RESULT;
	}

	/**
	 * Constructor for type=ERROR.
	 * 
	 * @param errorType
	 *            the error type
	 * @param errorCondition
	 *            the error condition
	 * @param errorText
	 *            the error text
	 */
	public UncoverCardBean(String errorType, String errorCondition,
			String errorText) {
		super(errorType, errorCondition, errorText);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean#clone()
	 */
	@Override
	public UncoverCardBean clone() {
//		System.out.println("UncoverCardBean.clone()0");
		UncoverCardBean twin = null;
		if (this.type == XMPPBean.TYPE_SET) {
			twin = new UncoverCardBean(this.barCodeId, this.networkFingerPrint,
					this.timeStamp, this.playerId);
		} else if (this.type == XMPPBean.TYPE_RESULT) {
			twin = new UncoverCardBean(this.result);
		}
//		System.out.println("UncoverCardBean.clone()1");
		twin = (UncoverCardBean) cloneBasicAttributes(twin);
//		System.out.println("UncoverCardBean.clone()2");
		return twin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean#payloadToXML()
	 */
	@Override
	public String payloadToXML() {

		StringBuilder sb = new StringBuilder();

		switch (this.type) {
		case XMPPBean.TYPE_SET:
			if (this.playerId != null)
				sb.append("<playerId>").append(playerId).append("</playerId>");
			if (this.timeStamp != null)
				sb.append("<timeStamp>").append(this.timeStamp).append(
						"</timeStamp>");
			if (this.barCodeId != null)
				sb.append("<barCodeId>").append(this.barCodeId).append(
						"</barCodeId>");
			if (this.networkFingerPrint != null) {
				for (String bssid : networkFingerPrint.getNetworkFingerPrint()
						.keySet()) {
					sb.append("<fingerPrint bssid=\""
							+ bssid
							+ "\" strength=\""
							+ networkFingerPrint.getNetworkFingerPrint().get(
									bssid) + "\">a</fingerPrint>");
				}
			}
			break;
		case XMPPBean.TYPE_RESULT:
//			System.out.println("UncoverCardBean.payloadToXML() result: " + result);
			sb.append("<result>true</result>");
			break;
		}
		sb = appendErrorPayload(sb);
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo#fromXML(org.xmlpull.v1
	 * .XmlPullParser)
	 */
	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
//		System.out.println("UncoverCardBean.fromXML()");
		String childElement = UncoverCardBean.CHILD_ELEMENT;
		networkFingerPrint = new NetworkFingerPrint();
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
				} else if (tagName.equals("timeStamp")) {
					this.timeStamp = parser.nextText();
				} else if (tagName.equals("fingerPrint")) {
					String bssid = parser.getAttributeValue(null, "bssid");
					Integer signalStrength = new Integer(parser
							.getAttributeValue(null, "strength"));
					this.networkFingerPrint.addFingerPrint(bssid,
							signalStrength);
					parser.nextText();
				} else if (tagName.equals("error")) {
					parser = parseErrorAttributes(parser);
				} else
					parser.next();
				break;
			case XmlPullParser.END_TAG:
//				System.out.println("UncoverCardBean.fromXML() endTag");
				if (parser.getName().equals(childElement))
					done = true;
				else
//					System.out.println(parser.getName());
					parser.next();
				break;
			case XmlPullParser.END_DOCUMENT:
//				System.out.println("UncoverCardBean.fromXML() endDocument");
				done = true;
				break;
			default:
				parser.next();
			}
		} while (!done);
//		System.out.println("UncoverCardBean.fromXML() end");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo#getChildElement()
	 */
	@Override
	public String getChildElement() {
		return UncoverCardBean.CHILD_ELEMENT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return UncoverCardBean.NAMESPACE;
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
	 * @param playerId
	 *            the new player id
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
	 * @param barCodeId
	 *            the new bar code id
	 */
	public void setBarCodeId(String barCodeId) {
		this.barCodeId = barCodeId;
	}

	/**
	 * Gets the network finger print.
	 * 
	 * @return the network finger print
	 */
	public NetworkFingerPrint getNetworkFingerPrint() {
		return networkFingerPrint;
	}

	/**
	 * Sets the network finger print.
	 * 
	 * @param networkFingerPrint
	 *            the new network finger print
	 */
	public void setNetworkFingerPrint(NetworkFingerPrint networkFingerPrint) {
		this.networkFingerPrint = networkFingerPrint;
	}

	/**
	 * Gets the time stamp.
	 * 
	 * @return the time stamp
	 */
	public String getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Sets the time stamp.
	 * 
	 * @param timeStamp
	 *            the new time stamp
	 */
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
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
	 * @param result
	 *            the new result
	 */
	public void setResult(boolean result) {
		this.result = result;
	}

}
