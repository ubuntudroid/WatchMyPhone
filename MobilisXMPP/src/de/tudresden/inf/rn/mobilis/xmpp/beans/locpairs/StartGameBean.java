package de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs;

import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.GeoPosition;

/**
 * This Bean is responsible for sending the gameID, a map of GeoPositions an a
 * map of pictures to the Clients
 * 
 * Directions Set: Server -> multiple Clients Result: Client -> Server
 * 
 * Payloads Set: Game ID, Map Barcode to GeoPosition, Map Barcode to PictureURL
 * Result: Ok.
 * 
 * @author Norbert Harder
 */
public class StartGameBean extends XMPPBean {

	private static final long serialVersionUID = 1L;

	/** The Constant CHILD_ELEMENT. */
	public static final String CHILD_ELEMENT = "query";

	/** The Constant NAMESPACE. */
	public static final String NAMESPACE = "mobilislocpairs:iq:startgame";

	private String gameID;
	private boolean result;

	/** Mapping from Barcode to GeoPosition */
	private Map<String, GeoPosition> barcodes;
	/** Mapping from Barcode to Picture URL */
	private Map<String, String> pictures;

	/**
	 * Constructor for type=SET.
	 * 
	 * @param gameID
	 *            the game id
	 * @param barcodes
	 *            the barcodes
	 * @param pictures
	 *            the pictures
	 */
	public StartGameBean(String gameID, Map<String, GeoPosition> barcodes,
			Map<String, String> pictures) {
		super();
		this.gameID = gameID;
		this.barcodes = barcodes;
		this.pictures = pictures;
		this.type = XMPPBean.TYPE_SET;
	}

	/**
	 * Constructor for empty bean and type=RESULT.
	 * 
	 * @param result
	 *            the result
	 */
	public StartGameBean(boolean result) {
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
	public StartGameBean(String errorType, String errorCondition,
			String errorText) {
		super(errorType, errorCondition, errorText);
	}

	/**
	 * Gets the sets the bean with test data.
	 * 
	 * @return the sets the bean with test data
	 */
	public static StartGameBean getSetBeanWithTestData() {
		String gameID = "game1234";
		Map<String, GeoPosition> barcodes = new HashMap<String, GeoPosition>();
		Map<String, String> pictures = new HashMap<String, String>();

		barcodes.put("barcode1", new GeoPosition(1, 2, 3));
		barcodes.put("barcode2", new GeoPosition(4, 5, 6));
		barcodes.put("barcode3", new GeoPosition(7, 8, 9));

		pictures.put("barcode1",
				"http://imgs.xkcd.com/comics/cat_proximity.png");
		pictures.put("barcode2", "http://imgs.xkcd.com/comics/photoshops.png");
		pictures
				.put("barcode3",
						"http://imgs.xkcd.com/comics/alternative_energy_revolution.jpg");

		return new StartGameBean(gameID, barcodes, pictures);
	}

	/**
	 * Gets the result bean with test data.
	 * 
	 * @return the result bean with test data
	 */
	public static StartGameBean getResultBeanWithTestData() {
		return new StartGameBean(true);
	}

	/**
	 * Instantiates a new start game bean.
	 */
	public StartGameBean() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean#clone()
	 */
	@Override
	public StartGameBean clone() {
		StartGameBean twin;

		if (this.type == XMPPBean.TYPE_SET)
			twin = new StartGameBean(gameID, barcodes, pictures);
		else if (this.type == XMPPBean.TYPE_RESULT)
			twin = new StartGameBean(result);
		else
			twin = new StartGameBean();
		twin = (StartGameBean) cloneBasicAttributes(twin);
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

		if (this.gameID != null)
			sb.append("<gameID>").append(this.gameID).append("</gameID>");

		if (this.barcodes != null) {
			sb.append("<barcodes>");
			for (String barcode : barcodes.keySet()) {
				sb.append("<barcode name=\"" + barcode + "\" ");
				if (this.pictures.get(barcode) != null)
					sb.append("picture=\"" + pictures.get(barcode) + "\"");
				sb.append(">").append(barcodes.get(barcode).toXML()).append(
						"</barcode>");
			}
			sb.append("</barcodes>");
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
		String childElement = StartGameBean.CHILD_ELEMENT;
		String _barcode = "";
		GeoPosition geo = new GeoPosition(0, 0, 0);

		boolean done = false;
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
				} else if (tagName.equals("gameID")) {
					gameID = parser.nextText();
					parser.next();
				} else if (tagName.equals("barcodes")) {
					barcodes = new HashMap<String, GeoPosition>();
					pictures = new HashMap<String, String>();
					parser.next();
				} else if (tagName.equals("barcode")) {
					_barcode = parser.getAttributeValue(null, "name");
					geo = new GeoPosition(0, 0, 0);
//					barcodes.put(_barcode, geo);
					pictures.put(_barcode, parser.getAttributeValue(null,
							"picture"));
					parser.next();
				} else if (tagName.equals("location")) {
					//_location = barcodes.get(_barcode);
					parser.next();
				} else if (tagName.equals("latitude")) {
					geo.setLatitude(Double.parseDouble(parser.nextText()));
					parser.next();
				} else if (tagName.equals("longitude")) {
					geo.setLongitude(Double
							.parseDouble(parser.nextText()));
					parser.next();
				} else if (tagName.equals("altitude")) {
					geo.setAltitude(Double.parseDouble(parser.nextText()));
					barcodes.put(_barcode, geo);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo#getChildElement()
	 */
	@Override
	public String getChildElement() {
		return StartGameBean.CHILD_ELEMENT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return StartGameBean.NAMESPACE;

	}

	/*
	 * Setters & Getters
	 */
	/**
	 * Gets the game id.
	 * 
	 * @return the game id
	 */
	public String getGameID() {
		return gameID;
	}

	/**
	 * Sets the game id.
	 * 
	 * @param gameID
	 *            the new game id
	 */
	public void setGameID(String gameID) {
		this.gameID = gameID;
	}

	/**
	 * Gets the pictures.
	 * 
	 * @return the pictures
	 */
	public Map<String, String> getPictures() {
		return pictures;
	}

	/**
	 * Sets the pictures.
	 * 
	 * @param pictures
	 *            the pictures
	 */
	public void setPictures(Map<String, String> pictures) {
		this.pictures = pictures;
	}

	/**
	 * Gets the barcodes.
	 * 
	 * @return the barcodes
	 */
	public Map<String, GeoPosition> getBarcodes() {
		return barcodes;
	}

	/**
	 * Sets the barcodes.
	 * 
	 * @param barcodes
	 *            the barcodes
	 */
	public void setBarcodes(Map<String, GeoPosition> barcodes) {
		this.barcodes = barcodes;
	}
}
