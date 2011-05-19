package de.tudresden.inf.rn.mobilis.xmpp.beans.groups;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.Mobilis;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

/**
 * 
 * @author Robert Lübke
 *
 */
public class GroupCreateBean extends XMPPBean {

	private static final long serialVersionUID = 1L;
	public static final String NAMESPACE = Mobilis.NAMESPACE + "#services/GroupingService";
	public static final String CHILD_ELEMENT = "group-create";

	public String name, description, address;
	public int longitude_e6, latitude_e6;
	public int visibilityRadius, joinRadius;
	public int visibilityLongitude_e6, visibilityLatitude_e6;
	public int joinLongitude_e6, joinLatitude_e6;
	public long joinStartTime, joinEndTime, startTime, endTime;
	public String privacy, link;
	public String groupId, founder;
	
	
	//Constructor for type=SET
	public GroupCreateBean(String name, String description, String address,
			int longitude_e6, int latitude_e6,  int visibilityRadius,
			int visibilityLongitude_e6, int visibilityLatitude_e6,
			int joinRadius, int joinLongitude_e6, int joinLatitude_e6,
			long joinStartTime, long joinEndTime,
			long startTime,	long endTime, String privacy, String link) {
		super();
		this.name=name;
		this.description=description;
		this.address=address;
		this.longitude_e6=longitude_e6;
		this.latitude_e6=latitude_e6;
		this.visibilityRadius=visibilityRadius;
		this.visibilityLongitude_e6=visibilityLongitude_e6;
		this.visibilityLatitude_e6=visibilityLatitude_e6;
		this.joinRadius=joinRadius;
		this.joinLongitude_e6=joinLongitude_e6;
		this.joinLatitude_e6=joinLatitude_e6;
		this.joinStartTime=joinStartTime;
		this.joinEndTime=joinEndTime;
		this.startTime=startTime;
		this.endTime=endTime;
		this.privacy=privacy;
		this.link=link;
		this.groupId=null;
		this.founder=this.from;
		this.type=XMPPBean.TYPE_SET;
	}	
	
	//Constructor for type=RESULT
	public GroupCreateBean(String groupId) {
		super();
		this.groupId=groupId;
		
		this.initializeNumbers();
		
		this.type=XMPPBean.TYPE_RESULT;
	}
		
	//Constructor for empty bean
	public GroupCreateBean() {
		super();
		this.initializeNumbers();		
	}
	
	/** Constructor for type=ERROR */
	public GroupCreateBean(String errorType, String errorCondition, String errorText) {
		super(errorType, errorCondition, errorText);
		this.initializeNumbers();
	}
	
	@Override
	public GroupCreateBean clone() {
		GroupCreateBean twin = new GroupCreateBean(name, description, address,
				longitude_e6, latitude_e6, visibilityRadius, visibilityLongitude_e6,
				visibilityLatitude_e6, joinRadius, joinLongitude_e6,
				joinLatitude_e6, joinStartTime, joinEndTime ,startTime ,endTime, privacy, link);
		twin.groupId = this.groupId;
		twin.founder = this.founder;

		twin = (GroupCreateBean) cloneBasicAttributes(twin);
		return twin;
	}

	/**
	 * Sets the default value for all integer and long attributes.
	 */
	private void initializeNumbers() {
		this.longitude_e6=Integer.MIN_VALUE;
		this.latitude_e6=Integer.MIN_VALUE;
		this.visibilityRadius=Integer.MIN_VALUE;
		this.visibilityLongitude_e6=Integer.MIN_VALUE;
		this.visibilityLatitude_e6=Integer.MIN_VALUE;
		this.joinRadius=Integer.MIN_VALUE;
		this.joinLongitude_e6=Integer.MIN_VALUE;
		this.joinLatitude_e6=Integer.MIN_VALUE;
		this.joinStartTime=Long.MIN_VALUE;
		this.joinEndTime=Long.MIN_VALUE;
		this.startTime=Long.MIN_VALUE;
		this.endTime=Long.MIN_VALUE;
	}	
	
	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();
		
		if (this.name != null)
			sb.append("<name>").append(this.name).append("</name>");
		if (this.description != null)
			sb.append("<description>").append(this.description).append("</description>");		
		if (this.address != null)
			sb.append("<address>").append(this.address).append("</address>");
		if (this.longitude_e6 > Integer.MIN_VALUE)
			sb.append("<longitude_e6>").append(this.longitude_e6).append("</longitude_e6>");
		if (this.latitude_e6 > Integer.MIN_VALUE)
			sb.append("<latitude_e6>").append(this.latitude_e6).append("</latitude_e6>");	
		if (this.visibilityRadius > Integer.MIN_VALUE ||
				this.startTime > Long.MIN_VALUE || this.endTime > Long.MIN_VALUE ||
				(this.visibilityLatitude_e6 > Integer.MIN_VALUE &&
				this.visibilityLongitude_e6 > Integer.MIN_VALUE)) {
			sb.append("<restriction type=\"visibility\" ");
			if (this.visibilityRadius > Integer.MIN_VALUE)
				sb.append("radius=\"").append(this.visibilityRadius).append("\" ");
			if (this.visibilityLatitude_e6 > Integer.MIN_VALUE && this.visibilityLongitude_e6 > Integer.MIN_VALUE) {
				sb.append("latitude_e6=\"").append(this.visibilityLatitude_e6).append("\" ");
				sb.append("longitude_e6=\"").append(this.visibilityLongitude_e6).append("\" ");
			}
			if (this.startTime > Long.MIN_VALUE)
				sb.append("starttime=\"").append(this.startTime).append("\" ");
			if (this.endTime > Long.MIN_VALUE)
				sb.append("endtime=\"").append(this.endTime).append("\" ");
			sb.append("/>");
		}
		if (this.joinRadius > Integer.MIN_VALUE ||
				this.joinStartTime > Long.MIN_VALUE || this.joinEndTime > Long.MIN_VALUE ||
				(this.joinLatitude_e6 > Integer.MIN_VALUE &&
				this.joinLongitude_e6 > Integer.MIN_VALUE))	{
			sb.append("<restriction type=\"join\" ");
			if (this.joinRadius > Integer.MIN_VALUE)
				sb.append("radius=\"").append(this.joinRadius).append("\" ");
			if (this.joinLatitude_e6 > Integer.MIN_VALUE && this.joinLongitude_e6 > Integer.MIN_VALUE) {
				sb.append("latitude_e6=\"").append(this.joinLatitude_e6).append("\" ");
				sb.append("longitude_e6=\"").append(this.joinLongitude_e6).append("\" ");
			}
			if (this.joinStartTime > Long.MIN_VALUE)
				sb.append("starttime=\"").append(this.joinStartTime).append("\" ");
			if (this.joinEndTime > Long.MIN_VALUE)
				sb.append("endtime=\"").append(this.joinEndTime).append("\" ");
			sb.append("/>");
		}		
		if (this.privacy != null)
			sb.append("<privacy>").append(this.privacy).append("</privacy>");
		if (this.link != null)
			sb.append("<link>").append(this.link).append("</link>");
		if (this.groupId != null)
			sb.append("<group-id>").append(this.groupId).append("</group-id>");

		sb = appendErrorPayload(sb);		
		return sb.toString();
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String childElement = GroupCreateBean.CHILD_ELEMENT;
		boolean done = false;
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
				} else if (tagName.equals("name")) {
					this.name = parser.nextText();
				} else if (tagName.equals("description")) {
					this.description = parser.nextText();				
				} else if (tagName.equals("address")) {
					this.address = parser.nextText();
				} else if (tagName.equals("latitude_e6")) {
					this.latitude_e6 = Integer.valueOf(parser.nextText()).intValue();
				} else if (tagName.equals("longitude_e6")) {
					this.longitude_e6 = Integer.valueOf(parser.nextText()).intValue();
				} else if (tagName.equals("restriction")) {
					if (parser.getAttributeValue(0).equals("visibility")) {
						for (int i=1; i<parser.getAttributeCount(); i++) {
							if (parser.getAttributeName(i).equals("radius"))
								this.visibilityRadius = Integer.valueOf(parser.getAttributeValue(i));								
							else if (parser.getAttributeName(i).equals("longitude_e6"))
								this.visibilityLongitude_e6 = Integer.valueOf(parser.getAttributeValue(i));
							else if (parser.getAttributeName(i).equals("latitude_e6"))
								this.visibilityLatitude_e6 = Integer.valueOf(parser.getAttributeValue(i));
							else if (parser.getAttributeName(i).equals("starttime"))
								this.startTime = Long.valueOf(parser.getAttributeValue(i)).longValue();
							else if (parser.getAttributeName(i).equals("endtime"))
								this.endTime = Long.valueOf(parser.getAttributeValue(i)).longValue();
						}							
					} else if (parser.getAttributeValue(0).equals("join")) {
						for (int i=1; i<parser.getAttributeCount(); i++) {
							if (parser.getAttributeName(i).equals("radius"))
								this.joinRadius = Integer.valueOf(parser.getAttributeValue(i));								
							else if (parser.getAttributeName(i).equals("longitude_e6"))
								this.joinLongitude_e6 = Integer.valueOf(parser.getAttributeValue(i));
							else if (parser.getAttributeName(i).equals("latitude_e6"))
								this.joinLatitude_e6 = Integer.valueOf(parser.getAttributeValue(i));
							else if (parser.getAttributeName(i).equals("starttime"))
								this.joinStartTime = Long.valueOf(parser.getAttributeValue(i)).longValue();
							else if (parser.getAttributeName(i).equals("endtime"))
								this.joinEndTime = Long.valueOf(parser.getAttributeValue(i)).longValue();
						}
					}
					parser.next();									
				} else if (tagName.equals("privacy")) {
					this.privacy = parser.nextText();
				} else if (tagName.equals("link")) {
					this.link = parser.nextText();
				} else if (tagName.equals("group-id")) {
					this.groupId = parser.nextText();
				} else if (tagName.equals("error")) {
					if (parser.getAttributeName(0).equals("type"))
						errorType = parser.getAttributeValue(0);
					parser.next();
					//Now the parser is at START_TAG of error condition
					errorCondition=parser.getName();
					parser.next(); parser.next();
					//Now the parser is at START_TAG of error text
					errorText=parser.nextText();
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
		return GroupCreateBean.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		return GroupCreateBean.NAMESPACE;
	}	
	
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId=groupId;
	}
	
}
