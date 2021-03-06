package de.tudresden.inf.rn.mobilis.xmpp.beans.groups;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.Mobilis;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

/**
 * 
 * @author Robert L�bke
 *
 */
public class GroupLeaveBean extends XMPPBean {

	private static final long serialVersionUID = 1L;
	public static final String NAMESPACE = Mobilis.NAMESPACE + "#services/GroupingService";
	public static final String CHILD_ELEMENT = "group-leave";
	
	public String groupId;
	
	// Constructor for type=SET
	public GroupLeaveBean(String groupId) {
		super();
		this.groupId=groupId;
		this.type=XMPPBean.TYPE_SET;
	}	
	
	/** Constructor for type=ERROR */
	public GroupLeaveBean(String errorType, String errorCondition, String errorText) {
		super(errorType, errorCondition, errorText);
	}
	
	//Constructor for empty bean and type=RESULT
	public GroupLeaveBean() {
		super();
		this.type=XMPPBean.TYPE_RESULT;
	}
	
	@Override
	public GroupLeaveBean clone() {
		GroupLeaveBean twin = new GroupLeaveBean(groupId);
		twin = (GroupLeaveBean) cloneBasicAttributes(twin);
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
		String childElement = GroupLeaveBean.CHILD_ELEMENT;

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
		return GroupLeaveBean.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		return GroupLeaveBean.NAMESPACE;
	}
	
}
