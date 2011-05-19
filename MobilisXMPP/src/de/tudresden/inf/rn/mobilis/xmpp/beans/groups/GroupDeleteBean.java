package de.tudresden.inf.rn.mobilis.xmpp.beans.groups;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.Mobilis;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

/**
 * 
 * @author Robert Lübke
 *
 */
public class GroupDeleteBean extends XMPPBean {

	private static final long serialVersionUID = 1L;
	public static final String NAMESPACE = Mobilis.NAMESPACE + "#services/GroupingService";
	public static final String CHILD_ELEMENT = "group-delete";
		
	public String groupId;
	
	/** Constructor for type=SET */
	public GroupDeleteBean(String groupId) {
		super();
		this.groupId=groupId;
		this.type=XMPPBean.TYPE_SET;
	}
	
	/** Constructor for type=ERROR */
	public GroupDeleteBean(String errorType, String errorCondition, String errorText) {
		super(errorType, errorCondition, errorText);
	}
	
	/** Constructor for empty bean and type=RESULT */
	public GroupDeleteBean() {
		super();
		this.type=XMPPBean.TYPE_RESULT;
	}
	
	@Override
	public GroupDeleteBean clone() {
		GroupDeleteBean twin = new GroupDeleteBean(groupId);
		twin = (GroupDeleteBean) cloneBasicAttributes(twin);
		return twin;
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();
		
		if (this.groupId != null)
			sb.append("<group-id>").append(this.groupId).append("</group-id>");
		
		sb = appendErrorPayload(sb);			
		return sb.toString();
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String childElement = GroupDeleteBean.CHILD_ELEMENT;

		boolean done = false;
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
				} else if (tagName.equals("group-id")) {
					this.groupId = parser.nextText();
				} else if (tagName.equals("error")) {
					parseErrorAttributes(parser);
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
		return GroupDeleteBean.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		return GroupDeleteBean.NAMESPACE;
	}
	
}
