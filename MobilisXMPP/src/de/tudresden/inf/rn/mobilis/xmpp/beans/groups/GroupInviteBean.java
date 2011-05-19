package de.tudresden.inf.rn.mobilis.xmpp.beans.groups;

import java.util.HashSet;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.Mobilis;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

/**
 * 
 * @author Robert Lübke
 *
 */
public class GroupInviteBean extends XMPPBean {

	private static final long serialVersionUID = 1L;
	public static final String NAMESPACE = Mobilis.NAMESPACE + "#services/GroupingService";
	public static final String CHILD_ELEMENT = "group-invite";
	
	public String groupId;
	public Set<String> invitees;
	
	// Constructor for type=SET
	public GroupInviteBean(Set<String> invitees, String groupId) {
		super();
		this.groupId=groupId;
		this.invitees=invitees;
		this.type=XMPPBean.TYPE_SET;
	}	
	
	/** Constructor for type=ERROR */
	public GroupInviteBean(String errorType, String errorCondition, String errorText) {
		super(errorType, errorCondition, errorText);	
	}
	
	//Constructor for empty bean and type=RESULT
	public GroupInviteBean() {
		super();
		this.type=XMPPBean.TYPE_RESULT;
	}
	
	@Override
	public GroupInviteBean clone() {
		GroupInviteBean twin = new GroupInviteBean(invitees, groupId);
		twin = (GroupInviteBean) cloneBasicAttributes(twin);			
		return twin;
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();
		
		if (this.groupId != null)
			sb.append("<group-id>").append(this.groupId).append("</group-id>");
		if (this.invitees != null)
			for (String jid : invitees)
				sb.append("<invitee>").append(jid).append("</invitee>");		
		
		sb = appendErrorPayload(sb);		
		return sb.toString();
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String childElement = GroupInviteBean.CHILD_ELEMENT;

		this.invitees = new HashSet<String>();
		
		boolean done = false;
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
				} else if (tagName.equals("group-id")) {
					this.groupId = parser.nextText();
				} else if (tagName.equals("invitee")) {
					this.invitees.add(parser.nextText());
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
		
		if (this.invitees.size()<=0) this.invitees=null;
	}

	@Override
	public String getChildElement() {
		return GroupInviteBean.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		return GroupInviteBean.NAMESPACE;
	}
	
}
