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

import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.ConditionInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.Mobilis;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

/**
 * @author Robert Lübke
 */
public class GroupQueryBean extends XMPPBean {

	private static final long serialVersionUID = 1L;
	public static final String NAMESPACE = Mobilis.NAMESPACE + "#services/GroupingService";
	public static final String CHILD_ELEMENT = "group-query";

	protected ConditionInfo condition = null; // for get 
	protected ArrayList<GroupItemInfo> items = new ArrayList<GroupItemInfo>(); // for result
	public int userLongitude, userLatitude;
	
	public ConditionInfo getCondition() { return this.condition; }
	public ArrayList<GroupItemInfo> getItems() { return this.items; }
	
	public void setCondition(ConditionInfo condition) { this.condition = condition; }
	
	/** Constructor for type=GET */
	public GroupQueryBean(int userLongitude, int userLatitude) {
		super();
		this.userLongitude=userLongitude;
		this.userLatitude=userLatitude;		
		this.type=XMPPBean.TYPE_GET;
	}
	
	/** Constructor for empty bean & type=RESULT */
	public GroupQueryBean() {
		super();
		this.userLongitude=Integer.MIN_VALUE;
		this.userLatitude=Integer.MIN_VALUE;
		this.type=XMPPBean.TYPE_RESULT;
	}	
		
	@Override
	public GroupQueryBean clone() {
		GroupQueryBean twin = new GroupQueryBean(userLongitude, userLatitude);

		twin = (GroupQueryBean) cloneBasicAttributes(twin);

		if (this.condition != null)
			twin.condition = this.condition.clone();
		twin.items.clear();
		for (GroupItemInfo gi: this.items)
			twin.items.add(gi.clone());
		return twin;
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String childElement = GroupQueryBean.CHILD_ELEMENT;
		boolean done = false;
		this.items.clear();
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
				} else if (tagName.equals(ConditionInfo.CHILD_ELEMENT)) {
					ConditionInfo ci = new ConditionInfo();
					ci.fromXML(parser);
					this.condition = ci;
				} else if (tagName.equals(GroupItemInfo.CHILD_ELEMENT)) {
					GroupItemInfo gi = new GroupItemInfo();
					gi.fromXML(parser);
					this.items.add(gi);
				} else if (tagName.equals("longitude")) {
					this.userLongitude = Integer.valueOf(parser.nextText()).intValue();
				} else if (tagName.equals("latitude")) {
					this.userLatitude = Integer.valueOf(parser.nextText()).intValue();
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
		return GroupQueryBean.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		return GroupQueryBean.NAMESPACE;
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();
		// User position
		if (this.userLatitude>Integer.MIN_VALUE || this.userLongitude>Integer.MIN_VALUE) {
			sb.append("<user>");			
			if (this.userLongitude > Integer.MIN_VALUE)
				sb.append("<longitude>").append(this.userLongitude).append("</longitude>");
			if (this.userLatitude > Integer.MIN_VALUE)
				sb.append("<latitude>").append(this.userLatitude).append("</latitude>");						
			sb.append("</user>");		
		}		
		//for get
		if (this.condition != null)
			sb.append(this.condition.toXML());
		//for result
		for (GroupItemInfo ri: this.items)
			sb.append(ri.toXML());
		return sb.toString();
	}

}
