package de.tudresden.inf.rn.mobilis.xmpp.beans.groups;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.Mobilis;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

/**
 * 
 * @author Robert Lübke
 *
 */
public class GroupJoinBean extends XMPPBean {

	private static final long serialVersionUID = 1L;
	public static final String NAMESPACE = Mobilis.NAMESPACE + "#services/GroupingService";
	public static final String CHILD_ELEMENT = "group-join";

	public int userLongitude, userLatitude;
	
	public String groupId;
	
	// Constructor for type=SET
	public GroupJoinBean(String groupId, int userLongitude, int userLatitude) {
		super();
		this.groupId=groupId;
		this.userLongitude=userLongitude;
		this.userLatitude=userLatitude;		
		this.type=XMPPBean.TYPE_SET;
	}	
	
	/** Constructor for type=ERROR */
	public GroupJoinBean(String errorType, String errorCondition, String errorText) {
		super(errorType, errorCondition, errorText);				
		this.initializeNumbers();
	}
	
	//Constructor for empty bean and type=RESULT
	public GroupJoinBean() {
		super();		
		this.initializeNumbers();		
		this.type=XMPPBean.TYPE_RESULT;
	}
	
	@Override
	public GroupJoinBean clone() {
		GroupJoinBean twin = new GroupJoinBean(groupId, userLongitude, userLatitude);

		twin = (GroupJoinBean) cloneBasicAttributes(twin);		
		return twin;
	}
	
	/**
	 * Sets the default value for all integer and long attributes.
	 */
	private void initializeNumbers() {
		this.userLongitude=Integer.MIN_VALUE;
		this.userLatitude=Integer.MIN_VALUE;
	}	

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();
		
		if (this.groupId != null)
			sb.append("<group-id>").append(this.groupId).append("</group-id>");		
		sb.append("<user>");
		if (this.userLongitude > Integer.MIN_VALUE)
			sb.append("<longitude>").append(this.userLongitude).append("</longitude>");
		if (this.userLatitude > Integer.MIN_VALUE)
			sb.append("<latitude>").append(this.userLatitude).append("</latitude>");			
		sb.append("</user>");
		
		sb = appendErrorPayload(sb);			
		return sb.toString();
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String childElement = GroupJoinBean.CHILD_ELEMENT;

		boolean done = false;
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
				} else if (tagName.equals("group-id")) {
					this.groupId = parser.nextText();	
				} else if (tagName.equals("longitude")) {
					this.userLongitude = Integer.valueOf(parser.nextText()).intValue();
				} else if (tagName.equals("latitude")) {
					this.userLatitude = Integer.valueOf(parser.nextText()).intValue();
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

	@Override
	public String getChildElement() {
		return GroupJoinBean.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		return GroupJoinBean.NAMESPACE;
	}
	
}
