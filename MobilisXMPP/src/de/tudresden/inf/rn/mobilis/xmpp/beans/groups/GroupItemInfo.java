/**
 * Copyright (C) 2010 Technische Universität Dresden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: http://mobilisplatform.sourceforge.net
 */

package de.tudresden.inf.rn.mobilis.xmpp.beans.groups;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.Mobilis;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo;

/**
 * @author Robert Lübke
 */
public class GroupItemInfo implements XMPPInfo {

	private static final long serialVersionUID = 1L;
	public static final String NAMESPACE = Mobilis.NAMESPACE + "#services/GroupingService";
	public static final String CHILD_ELEMENT = "group-item";
	
	public String groupId, name;
	public int latitudeE6, longitudeE6, memberCount;
	
	/** Constructor for Group Information with all attributes */
	public GroupItemInfo(String groupId, String name, int latitudeE6, int longitudeE6, int memberCount) {
		super();
		this.groupId=groupId;
		this.name=name;
		this.latitudeE6=latitudeE6;
		this.longitudeE6=longitudeE6;
		this.memberCount=memberCount;		
	}	
	
	/** Constructor for empty Group Information */
	public GroupItemInfo() {
		super();
		this.latitudeE6=Integer.MIN_VALUE;
		this.longitudeE6=Integer.MIN_VALUE;
		this.memberCount=Integer.MIN_VALUE;	
	}


	@Override
	public GroupItemInfo clone() {
		GroupItemInfo twin = new GroupItemInfo(this.groupId, this.name, this.latitudeE6, this.longitudeE6, this.memberCount);		
		return twin;
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String ce = GroupItemInfo.CHILD_ELEMENT;
		boolean done = false;
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				int attributesCount = parser.getAttributeCount();
				if (tagName.equals(ce)) {
					for (int i = 0; i < attributesCount; i++)
						if (parser.getAttributeName(i).equals("group-id"))
							this.groupId = parser.getAttributeValue(i);
						else if (parser.getAttributeName(i).equals("name"))
							this.name= parser.getAttributeValue(i);
						else if (parser.getAttributeName(i).equals("latitude_e6"))
							this.latitudeE6 = Integer.valueOf(parser.getAttributeValue(i));
						else if (parser.getAttributeName(i).equals("longitude_e6"))
							this.longitudeE6 = Integer.valueOf(parser.getAttributeValue(i));
						else if (parser.getAttributeName(i).equals("member-count"))
							this.memberCount = Integer.valueOf(parser.getAttributeValue(i));
					parser.next();				
				} else
					parser.next();
				break;
			case XmlPullParser.END_TAG:
				if (parser.getName().equals(ce))
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
		return GroupItemInfo.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		return GroupItemInfo.NAMESPACE;
	}
	
	@Override
	public String toXML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<").append(GroupItemInfo.CHILD_ELEMENT);
		if (this.groupId != null)
			sb.append(" group-id=\"").append(this.groupId).append("\"");
		if (this.name != null)
			sb.append(" name=\"").append(this.name).append("\"");
		if (this.latitudeE6 > Integer.MIN_VALUE)
			sb.append(" latitude_e6=\"").append(this.latitudeE6).append("\"");
		if (this.longitudeE6 > Integer.MIN_VALUE)
			sb.append(" longitude_e6=\"").append(this.longitudeE6).append("\"");
		if (this.memberCount > Integer.MIN_VALUE)
			sb.append(" member-count=\"").append(this.memberCount).append("\"");
		sb.append(" />");		
		return sb.toString();
	}

}
