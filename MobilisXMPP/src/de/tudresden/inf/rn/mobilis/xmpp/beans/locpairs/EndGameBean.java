package de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs;

import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

/**
 * This Bean is responsible for informing the client that the game has ended.
 * Contains List of Points for each Team and Highscores.
 * 
 * Directions 
 * Set: Server -> Client 
 * Result: Client -> Server
 * 
 * Payloads 
 * Set: Map Team ID to Points, Map Teamnames to Points 
 * Result: Ok
 * 
 * @author Norbert Harder
 * @author Reik Mueller
 */
public class EndGameBean extends XMPPBean {

	private static final long serialVersionUID = 1L;
	
	/** The Constant CHILD_ELEMENT. */
	public static final String CHILD_ELEMENT = "query";
	
	/** The Constant NAMESPACE. */
	public static final String NAMESPACE = "mobilislocpairs:iq:endgame";

	/**
	 * The Enum EndType.
	 */
	public static enum EndType {
		ENDBYREGULAREND, ENDBYTEAMMATEQUIT, ENDBYTEAMMATEERROR
	};

	private Map<Integer, Long> points;
	private Map<String, Long> highscores;
	private EndType reason;
	private boolean result;

	/**
	 * Constructor for type=SET.
	 *
	 * @param points the points
	 * @param highscores the highscores
	 * @param reason the reason
	 */
	public EndGameBean(Map<Integer, Long> points, Map<String, Long> highscores,
			EndType reason) {
		super();
		this.points = points;
		this.highscores = highscores;
		this.reason = reason;
		this.type = XMPPBean.TYPE_SET;
	}

	/**
	 * Constructor for empty bean and type=RESULT.
	 *
	 * @param result the result
	 */
	public EndGameBean(boolean result) {
		super();
		this.result = result;
		this.type = XMPPBean.TYPE_RESULT;
	}

	/**
	 * Constructor for type=ERROR.
	 *
	 * @param errorType the error type
	 * @param errorCondition the error condition
	 * @param errorText the error text
	 */
	public EndGameBean(String errorType, String errorCondition, String errorText) {
		super(errorType, errorCondition, errorText);
	}

	/**
	 * Instantiates a new end game bean.
	 */
	public EndGameBean() {}

	/**
	 * Gets the result bean with test data.
	 *
	 * @return the result bean with test data
	 */
	public static EndGameBean getResultBeanWithTestData() {
		return new EndGameBean(true);
	}

	/**
	 * Gets the sets the bean with test data.
	 *
	 * @return the sets the bean with test data
	 */
/*	public static EndGameBean getSetBeanWithTestData() {
		Map<Integer, Long> points = new HashMap<Integer, Long>();
		Map<String, Long> highscore = new HashMap<String, Long>();

		points.put(1, Long.parseLong("12345"));
		points.put(2, Long.parseLong("23456"));
		points.put(3, Long.parseLong("34567"));

		highscore.put(Long.parseLong("212345"), "Mickey+Donald");
		highscore.put(Long.parseLong("423456"), "Bonnie+Clyde");
		highscore.put(Long.parseLong("634567"), "Clark+Louis");

		EndGameBean result = new EndGameBean(points, highscore,
				EndType.ENDBYREGULAREND);
		return result;
	}
*/
	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean#clone()
	 */
	@Override
	public EndGameBean clone() {
		EndGameBean twin;

		if (this.type == XMPPBean.TYPE_SET)
			twin = new EndGameBean(points, highscores, reason);
		else if (this.type == XMPPBean.TYPE_RESULT)
			twin = new EndGameBean(result);
		else
			twin = new EndGameBean();
		twin = (EndGameBean) cloneBasicAttributes(twin);
		return twin;
	}

	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean#payloadToXML()
	 */
	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();

		if (this.type == XMPPBean.TYPE_SET) {

			if (this.points != null) {
				sb.append("<points>");
				for (Integer team : points.keySet()) {
					sb.append("<team points=\"" + points.get(team) + "\">")
							.append(team).append("</team>");
				}
				sb.append("</points>");
			}
			if (this.highscores != null) {
				sb.append("<highscores>");
				for (String name : highscores.keySet()) {
					sb.append(
							"<highscore teamname=\"" + name
									+ " \" >").append(highscores.get(name))
							.append("</highscore>");
				}
				sb.append("</highscores>");
			}
			if (this.reason != null) {
				sb.append("<reason>").append(reason).append("</reason>");
			}
		} else if (this.type == XMPPBean.TYPE_RESULT)
			sb.append("<result>").append(result).append("</result>");
		sb = appendErrorPayload(sb);
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo#fromXML(org.xmlpull.v1.XmlPullParser)
	 */
	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String childElement = EndGameBean.CHILD_ELEMENT;
		boolean done = false;
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
				} else if (tagName.equals("points")) {
					points = new HashMap<Integer, Long>();
					parser.next();
				} else if (tagName.equals("team")) {
					Long _points;
					_points = Long.parseLong(parser.getAttributeValue(null,
							"points"));
					points.put(Integer.parseInt(parser.nextText()), _points);
					parser.next();
				} else if (tagName.equals("highscores")) {
					highscores = new HashMap<String, Long>();
					parser.next();
				} else if (tagName.equals("highscore")) {
					String _teamname;
					_teamname = parser.getAttributeValue(null,
							"teamname");
					highscores.put(_teamname, Long.valueOf(parser.nextText()));
					parser.next();
				} else if (tagName.equals("reason")) {
					reason = EndType.valueOf(parser.nextText());
					parser.next();
				} else if (tagName.equals("result")) {
					result = Boolean.parseBoolean(parser.nextText());
					parser.next();
				}else if (tagName.equals("error")) {
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
		return EndGameBean.CHILD_ELEMENT;
	}

	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return EndGameBean.NAMESPACE;

	}

	/*
	 * Getters & Setters
	 */

	/**
	 * Gets the points.
	 *
	 * @return the points
	 */
	public Map<Integer, Long> getPoints() {
		return points;
	}

	/**
	 * Sets the points.
	 *
	 * @param points the points
	 */
	public void setPoints(Map<Integer, Long> points) {
		this.points = points;
	}

	/**
	 * Gets the highscores.
	 *
	 * @return the highscores
	 */
	public Map<String, Long> getHighscores() {
		return highscores;
	}

	/**
	 * Sets the highscores.
	 *
	 * @param highscores the highscores
	 */
	public void setHighscores(Map<String, Long> highscores) {
		this.highscores = highscores;
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

}
