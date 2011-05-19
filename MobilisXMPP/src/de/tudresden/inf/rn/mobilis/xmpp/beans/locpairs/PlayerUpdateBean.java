package de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs;

import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.GeoPosition;

/**
 * This Bean is responsible for informing all players about current players,
 * their states and teams on the server side
 * 
 * Directions Set: Server -> Client Result: Client -> Server
 * 
 * Payloads Set: Game ID, Map of players and their team, Map of Players and
 * their names, Map of Players and their state.
 * 
 * @author Norbert Harder
 * @author Reik Mueller
 */
public class PlayerUpdateBean extends XMPPBean {

	private static final long serialVersionUID = 1L;

	/** The Constant CHILD_ELEMENT. */
	public static final String CHILD_ELEMENT = "query";

	/** The Constant NAMESPACE. */
	public static final String NAMESPACE = "mobilislocpairs:iq:playerupdate";

	/** The Constant STATE_READY. */
	public static final boolean STATE_READY = true;

	/** The Constant STATE_PENDING. */
	public static final boolean STATE_PENDING = false;

	private String gameID;
	private Map<String, Integer> players;
	private Map<String, String> names;
	private Map<String, Boolean> states;
	private Map<String, GeoPosition> positions;
	private boolean result;

	/**
	 * Constructor for type=SET.
	 * 
	 * @param gameID
	 *            the game id
	 * @param players
	 *            the players
	 * @param names
	 *            the names
	 * @param states
	 *            the states
	 * @param positions
	 *            the positions
	 */
	public PlayerUpdateBean(String gameID, Map<String, Integer> players,
			Map<String, String> names, Map<String, Boolean> states,
			Map<String, GeoPosition> positions) {
		super();
		this.gameID = gameID;
		this.players = players;
		this.states = states;
		this.names = names;
		this.positions = positions;

		this.type = XMPPBean.TYPE_SET;
	}

	/**
	 * Constructor for empty bean and type=RESULT.
	 * 
	 * @param result
	 *            the result
	 */
	public PlayerUpdateBean(boolean result) {
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
	public PlayerUpdateBean(String errorType, String errorCondition,
			String errorText) {
		super(errorType, errorCondition, errorText);
	}

	/**
	 * Instantiates a new player update bean.
	 */
	public PlayerUpdateBean() {
		super();
	}

	/**
	 * Gets the sets the bean with test data.
	 * 
	 * @return the sets the bean with test data
	 */
	public static PlayerUpdateBean getSetBeanWithTestData() {

		String gameID = "game1234";
		Map<String, Integer> players = new HashMap<String, Integer>();
		Map<String, String> names = new HashMap<String, String>();
		Map<String, Boolean> states = new HashMap<String, Boolean>();
		Map<String, GeoPosition> positions = new HashMap<String, GeoPosition>();

		players.put("Alpha_ID", 1);
		players.put("Beta_ID", 1);
		players.put("Gamma_ID", 2);
		players.put("Delta_ID", 2);

		names.put("Alpha_ID", "Alpha");
		names.put("Beta_ID", "Beta");
		names.put("Gamma_ID", "Gamma");
		names.put("Delta_ID", "Delta");

		states.put("Alpha_ID", STATE_READY);
		states.put("Beta_ID", STATE_PENDING);
		states.put("Gamma_ID", STATE_READY);
		states.put("Delta_ID", STATE_READY);

		positions.put("Alpha_ID", new GeoPosition(0, 0, 0));
		positions.put("Beta_ID", new GeoPosition(0, 0, 0));
		positions.put("Gamma_ID", new GeoPosition(0, 0, 0));
		positions.put("Delta_ID", new GeoPosition(0, 0, 0));

		PlayerUpdateBean result = new PlayerUpdateBean(gameID, players, names,
				states, positions);
		return result;
	}

	/**
	 * Gets the result bean with test data.
	 * 
	 * @return the result bean with test data
	 */
	public static PlayerUpdateBean getResultBeanWithTestData() {
		return new PlayerUpdateBean(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean#clone()
	 */
	@Override
	public PlayerUpdateBean clone() {
		PlayerUpdateBean twin;

		if (this.type == XMPPBean.TYPE_SET)
			twin = new PlayerUpdateBean(gameID, players, names, states,
					positions);
		else if (this.type == XMPPBean.TYPE_RESULT)
			twin = new PlayerUpdateBean(result);
		else
			twin = new PlayerUpdateBean();
		twin = (PlayerUpdateBean) cloneBasicAttributes(twin);
		return twin;
	}

	/**
	 * Sets the states.
	 * 
	 * @param states
	 *            the states
	 */
	public void setStates(Map<String, Boolean> states) {
		this.states = states;
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

		if (this.players != null) {
			sb.append("<players>");
			for (String _playerid : players.keySet()) {
				sb.append(
						"<player team=\"" + players.get(_playerid)
								+ "\" state=\"" + states.get(_playerid)
								+ "\" name=\"" + names.get(_playerid)
								+ "\" latitude=\""
								+ positions.get(_playerid).getLatitude()
								+ "\" longitude=\""
								+ positions.get(_playerid).getLongitude()
								+ "\" altitude=\""
								+ positions.get(_playerid).getAltitude()
								+ "\">").append(_playerid).append("</player>");
			}
			sb.append("</players>");
		}

		if (this.type == XMPPBean.TYPE_RESULT)
			sb.append("<result>").append(result).append("</result>");

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
		String childElement = PlayerUpdateBean.CHILD_ELEMENT;

		boolean done = false;
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
				} else if (tagName.equals("players")) {
					players = new HashMap<String, Integer>();
					states = new HashMap<String, Boolean>();
					names = new HashMap<String, String>();
					positions = new HashMap<String, GeoPosition>();
					parser.next();
				} else if (tagName.equals("player")) {
					int team = Integer.parseInt(parser.getAttributeValue(null,
							"team"));
					String name = new String(parser.getAttributeValue(null,
							"name"));
					boolean state = Boolean.parseBoolean(parser
							.getAttributeValue(null, "state"));
					GeoPosition position = new GeoPosition(Double
							.parseDouble(parser.getAttributeValue(null,
									"latitude")), Double.parseDouble(parser
							.getAttributeValue(null, "longitude")), Double
							.parseDouble(parser.getAttributeValue(null,
									"altitude")));
					String _playerID = parser.nextText();
					players.put(_playerID, team);
					names.put(_playerID, name);
					states.put(_playerID, state);
					positions.put(_playerID, position);
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

	/**
	 * Gets the positions.
	 * 
	 * @return the positions
	 */
	public Map<String, GeoPosition> getPositions() {
		return positions;
	}

	/**
	 * Sets the positions.
	 * 
	 * @param positions
	 *            the positions
	 */
	public void setPositions(Map<String, GeoPosition> positions) {
		this.positions = positions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo#getChildElement()
	 */
	@Override
	public String getChildElement() {
		return PlayerUpdateBean.CHILD_ELEMENT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return PlayerUpdateBean.NAMESPACE;

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
	 * Gets the players.
	 * 
	 * @return the players
	 */
	public Map<String, Integer> getPlayers() {
		return players;
	}

	/**
	 * Sets the players.
	 * 
	 * @param players
	 *            the players
	 */
	public void setPlayers(Map<String, Integer> players) {
		this.players = players;
	}

	/**
	 * Gets the states.
	 * 
	 * @return the states
	 */
	public Map<String, Boolean> getStates() {
		return states;
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

	/**
	 * Gets the names.
	 * 
	 * @return the names
	 */
	public Map<String, String> getNames() {
		return names;
	}

	/**
	 * Sets the names.
	 * 
	 * @param names
	 *            the names
	 */
	public void setNames(Map<String, String> names) {
		this.names = names;
	}

}
