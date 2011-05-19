package de.tudresden.inf.rn.mobilis.xmpp.beans.groups;

import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.Mobilis;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

/**
 * 
 * @author Robert Lübke
 *
 */
public class GroupMemberInfoBean extends XMPPBean {

	private static final long serialVersionUID = 1L;
	public static final String NAMESPACE = Mobilis.NAMESPACE + "#services/GroupingService";
	public static final String CHILD_ELEMENT = "group-member-info";
		
	public String jidWithoutResource;
	public String realName, city, email, homepage;
	public int age;
	public Map<String, String> groups;
	
	/** Constructor for type=SET */
	public GroupMemberInfoBean(String realName, String city, String email, String homepage, int age) {
		super();		
		this.realName=realName;
		this.city=city;
		this.email=email;
		this.homepage=homepage;
		this.age=age;		
		this.type=XMPPBean.TYPE_SET;
	}
	
	/** Constructor for type=RESULT */
	public GroupMemberInfoBean(String realName, String city, String email, String homepage, int age, String jidWithoutResource, Map<String, String> groups) {
		super();		
		this.realName=realName;
		this.city=city;
		this.email=email;
		this.homepage=homepage;
		this.age=age;		
		this.groups=groups;
		this.jidWithoutResource=jidWithoutResource;
		this.type=XMPPBean.TYPE_RESULT;
	}
	
	/** Constructor for type=ERROR */
	public GroupMemberInfoBean(String errorType, String errorCondition, String errorText) {
		super(errorType, errorCondition, errorText);		
		this.age=Integer.MIN_VALUE;		
	}
	
	/** Constructor for empty bean  and type=GET (from server side) and type=RESULT */
	public GroupMemberInfoBean() {
		super();
		this.age=Integer.MIN_VALUE;		
	}
	
	/** Constructor for type=GET (from client side) */
	public GroupMemberInfoBean(String jidWithoutResource) {
		super();		
		this.jidWithoutResource=jidWithoutResource;
		
		this.type=XMPPBean.TYPE_GET;
		
		this.age=Integer.MIN_VALUE;		
	}
	
	@Override
	public GroupMemberInfoBean clone() {
		GroupMemberInfoBean twin = new GroupMemberInfoBean(realName, city, email, homepage, age);
		twin.groups=groups;
		twin.jidWithoutResource=jidWithoutResource;

		twin = (GroupMemberInfoBean) cloneBasicAttributes(twin);
		
		return twin;
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();
		if (this.jidWithoutResource != null)
			sb.append("<jidwithoutresource>").append(this.jidWithoutResource).append("</jidwithoutresource>");
		if (this.realName != null)
			sb.append("<realname>").append(this.realName).append("</realname>");
		if (this.city != null)
			sb.append("<city>").append(this.city).append("</city>");		
		if (this.age > Integer.MIN_VALUE)
			sb.append("<age>").append(this.age).append("</age>");
		if (this.email != null)
			sb.append("<email>").append(this.email).append("</email>");			
		if (this.homepage != null)
			sb.append("<homepage>").append(this.homepage).append("</homepage>");		
		if (this.groups != null) {			
			sb.append("<groups>");
			for (String groupId : groups.keySet())
				sb.append("<group group-id=\""+groupId+"\" name=\""+groups.get(groupId)+"\"></group>");
			sb.append("</groups>");
		}
		
		sb = appendErrorPayload(sb);			
		return sb.toString();
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String childElement = GroupMemberInfoBean.CHILD_ELEMENT;

		this.groups = new HashMap<String, String>();
		
		boolean done = false;
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
				} else if (tagName.equals("jidwithoutresource")) {
					this.jidWithoutResource = parser.nextText();
				} else if (tagName.equals("realname")) {
					this.realName = parser.nextText();
				} else if (tagName.equals("city")) {
					this.city = parser.nextText();
				} else if (tagName.equals("age")) {
					this.age = Integer.valueOf(parser.nextText()).intValue();
				} else if (tagName.equals("email")) {
					this.email = parser.nextText();
				} else if (tagName.equals("homepage")) {
					this.homepage = parser.nextText();	
				} else if (tagName.equals("group")) {					
					groups.put(parser.getAttributeValue(0), parser.getAttributeValue(1));
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
		if (groups.size()==0) groups=null;
	}

	@Override
	public String getChildElement() {
		return GroupMemberInfoBean.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		return GroupMemberInfoBean.NAMESPACE;
	}
	
	//GETTER & SETTER
	
	public String getJidWithoutResource() {
		return jidWithoutResource;
	}
	public void setJidWithoutResource (String jidWithoutResource) {
		this.jidWithoutResource=jidWithoutResource;
	}
	
}
