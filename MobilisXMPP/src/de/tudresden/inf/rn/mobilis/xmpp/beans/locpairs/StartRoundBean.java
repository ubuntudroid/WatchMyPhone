package de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.LocPairsDateFormat;

/**
 * Implementation for the bean StartRoundBean.
 * The StartRoundBean is used to tell the clients that the game starts and
 * the lobby is closed. It provides the clients with inform
 *
 * @author Reik Mueller
 */
public class StartRoundBean extends XMPPBean {

	private static final long serialVersionUID = 1L;
	
	/** The Constant CHILD_ELEMENT. */
	public static final String CHILD_ELEMENT = "query";
    
    /** The Constant NAMESPACE. */
    public static final String NAMESPACE = "mobilislocpairs:iq:startround";
	
    private Boolean active = null;
    private String startTime = null;
    private Integer duration = null;
    private Integer roundNumber = -1;
    private Map<Integer, Long> teamScores = new HashMap<Integer, Long>();
    private String activeTeam = null;
    
    /** The Constant sdf. */
    public static final SimpleDateFormat sdf = LocPairsDateFormat.getFormat();
    
	/**
	 * Constructor for type=SET.
	 *
	 * @param active the active
	 * @param startTime the start time
	 * @param duration the duration
	 * @param scores the scores
	 */
	public StartRoundBean(Boolean active, String startTime, Integer duration, Map<Integer, Long> scores, String activeTeam, Integer roundNumber) {
		super();
		this.roundNumber = roundNumber;
		this.active = active;
		this.startTime = startTime;
		this.duration = duration;
		this.teamScores = scores;
		this.activeTeam = activeTeam;
		this.type=XMPPBean.TYPE_SET;
	}
	
	/**
	 * Constructor for empty bean and type=RESULT.
	 */
	public StartRoundBean() {
		super();
		this.type=XMPPBean.TYPE_RESULT;
	}
	
	/**
	 * Constructor for type=ERROR.
	 *
	 * @param errorType the error type
	 * @param errorCondition the error condition
	 * @param errorText the error text
	 */
	public StartRoundBean(String errorType, String errorCondition, String errorText) {
		super(errorType, errorCondition, errorText);
	}
	
	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean#clone()
	 */
	@Override
	public StartRoundBean clone() {
		StartRoundBean twin = new StartRoundBean(
		this.active, 
		this.startTime,
		this.duration,
		this.teamScores,
		this.activeTeam,
		this.roundNumber);
		
		twin = (StartRoundBean) cloneBasicAttributes(twin);
		return twin;
	}

	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean#payloadToXML()
	 */
	@Override
	public String payloadToXML() {

		StringBuilder sb = new StringBuilder();
		
		if (this.active != null && activeTeam != null)
			sb.append("<active teamid=\"" + activeTeam + "\">").append(this.active.toString()).append("</active>");
		if (this.roundNumber != null && this.roundNumber != -1)
			sb.append("<roundNumber>").append(roundNumber.toString()).append("</roundNumber>");
		if (this.startTime != null)
			sb.append("<startTime>").append(startTime).append("</startTime>");
		if (this.duration != null)
			sb.append("<duration>").append(this.duration.toString()).append("</duration>");
		for(Integer s : teamScores.keySet()){
			sb.append("<teamScore teamid=\"" + s + "\">").append(teamScores.get(s).toString()).append("</teamScore>");
		}
		sb = appendErrorPayload(sb);			
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo#fromXML(org.xmlpull.v1.XmlPullParser)
	 */
	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String childElement = StartRoundBean.CHILD_ELEMENT;
		boolean done = false;
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
				} else if (tagName.equals("roundNumber")) {
					this.roundNumber = new Integer(parser.nextText());
				}else if (tagName.equals("active")) {
					this.activeTeam = parser.getAttributeValue(null, "teamid");
					this.active = (boolean)Boolean.valueOf(parser.nextText());
				} else if (tagName.equals("startTime")) {
					this.startTime = parser.nextText();
				}else if (tagName.equals("duration")) {
					this.duration = (int)Integer.valueOf(parser.nextText());
				}else if (tagName.equals("teamScore")) {
					int teamid = Integer.valueOf(parser.getAttributeValue(null, "teamid"));
					long score = Long.valueOf(parser.nextText());
					this.teamScores.put(teamid, score);
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
		return StartRoundBean.CHILD_ELEMENT;
	}

	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return StartRoundBean.NAMESPACE;
	}
	
	// Setter & Getter
	/**
	 * Gets the active.
	 *
	 * @return the active
	 */
	public Boolean getActive() {
		return active;
	}

	/**
	 * Sets the active.
	 *
	 * @param active the new active
	 */
	public void setActive(Boolean active) {
		this.active = active;
	}

	/**
	 * Gets the duration.
	 *
	 * @return the duration
	 */
	public Integer getDuration() {
		return duration;
	}

	/**
	 * Sets the duration.
	 *
	 * @param duration the new duration
	 */
	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	/**
	 * Gets the team scores.
	 *
	 * @return the team scores
	 */
	public Map<Integer, Long> getTeamScores() {
		return teamScores;
	}

	/**
	 * Sets the team scores.
	 *
	 * @param teamScores the team scores
	 */
	public void setTeamScores(Map<Integer, Long> teamScores) {
		this.teamScores = teamScores;
	}

	/**
	 * Gets the start time.
	 *
	 * @return the start time
	 */
	public String getStartTime() {
		return startTime;
	}
	
	/**
	 * Gets the start time in milliseconds.
	 *
	 * @return the start time in milliseconds
	 * @throws ParseException the parse exception
	 */
	public long getStartTimeInMilliseconds() throws ParseException {
		Calendar test = (Calendar)Calendar.getInstance().clone();
		test.setTime(sdf.parse(startTime));
		return test.getTimeInMillis();
	}

	/**
	 * Sets the start time.
	 *
	 * @param startTime the new start time
	 */
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getActiveTeam() {
		return activeTeam;
	}

	public void setActiveTeam(String activeTeam) {
		this.activeTeam = activeTeam;
	}

	public Integer getRoundNumber() {
		return roundNumber;
	}

	public void setRoundNumber(Integer roundNumber) {
		this.roundNumber = roundNumber;
	}
	
}
