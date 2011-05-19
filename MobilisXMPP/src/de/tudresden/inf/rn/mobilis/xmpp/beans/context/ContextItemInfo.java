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

package de.tudresden.inf.rn.mobilis.xmpp.beans.context;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.Mobilis;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo;

/**
 * A ContextItemInfo can contain one of the following elements:
 * <tune/>, <mood/>, <location/> and the generic <contextitem/>.
 * Based on XEP-0060 - Publish-Subscribe
 * @author Robert Lübke
 */
public class ContextItemInfo implements XMPPInfo, Cloneable {

	private static final long serialVersionUID = 1L;
	public static final String CHILD_ELEMENT = "item";
	public static final String NAMESPACE = Mobilis.NAMESPACE;

	private UserTuneInfo userTuneInfo;
	private UserLocationInfo userLocationInfo;
	private UserMoodInfo userMoodInfo;
	private UserContextInfo userContextInfo;
		
	/** Constructor for an empty ContextItemInfo	 */
	public ContextItemInfo() {
	}
	
	/** Constructor for a ContextItemInfo containing a UserTuneInfo  */
	public ContextItemInfo(UserTuneInfo userTuneInfo) {
		this.userTuneInfo=userTuneInfo;
	}
	
	/** Constructor for a ContextItemInfo containing a UserLocationInfo  */
	public ContextItemInfo(UserLocationInfo userLocationInfo) {
		this.userLocationInfo=userLocationInfo;
	}
		
	/** Constructor for a ContextItemInfo containing a UserMoodInfo  */
	public ContextItemInfo(UserMoodInfo userMoodInfo) {
		this.userMoodInfo=userMoodInfo;
	}
	
	/** Constructor for a ContextItemInfo containing a UserContextInfo  */
	public ContextItemInfo(UserContextInfo userContextInfo) {
		this.userContextInfo=userContextInfo;
	}
	

	@Override
	public ContextItemInfo clone() {
		ContextItemInfo twin = new ContextItemInfo();		
		twin.userContextInfo = this.userContextInfo.clone();
		twin.userLocationInfo = this.userLocationInfo.clone();
		twin.userTuneInfo = this.userTuneInfo.clone();
		twin.userMoodInfo = this.userMoodInfo.clone();
		
		return twin;
	}
	
	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String childElement = ContextItemInfo.CHILD_ELEMENT;
		
		boolean done = false;

		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
					
					if (parser.getName().equals(UserContextInfo.CHILD_ELEMENT)) {
						this.userContextInfo = new UserContextInfo();
						userContextInfo.fromXML(parser);						
					} else if (parser.getName().equals(UserLocationInfo.CHILD_ELEMENT)) {
						this.userLocationInfo = new UserLocationInfo();
						userLocationInfo.fromXML(parser);					
					} else if (parser.getName().equals(UserMoodInfo.CHILD_ELEMENT)) {
						this.userMoodInfo = new UserMoodInfo();
						userMoodInfo.fromXML(parser);
					} else if (parser.getName().equals(UserTuneInfo.CHILD_ELEMENT)) {
						this.userTuneInfo = new UserTuneInfo();
						userTuneInfo.fromXML(parser);						
					}
					
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
	public String toXML() {
		String childElement = ContextItemInfo.CHILD_ELEMENT;

		StringBuilder sb = new StringBuilder()
				.append("<").append(childElement).append(">");
		
		if (this.userContextInfo != null)
			sb.append(userContextInfo.toXML());
		else if (this.userLocationInfo != null)
			sb.append(userLocationInfo.toXML());
		else if (this.userTuneInfo != null)
			sb.append(userTuneInfo.toXML());		
		else if (this.userMoodInfo != null)
			sb.append(userMoodInfo.toXML());		
		
		sb.append("</").append(childElement).append(">");
		return sb.toString();
	}

	@Override
	public String getChildElement() {
		return ContextItemInfo.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		return ContextItemInfo.NAMESPACE;
	}

	/**
	 * @param userLocationInfo the userLocationInfo to set
	 */
	public void setUserLocationInfo(UserLocationInfo userLocationInfo) {
		this.userLocationInfo = userLocationInfo;
	}

	/**
	 * @return the userLocationInfo
	 */
	public UserLocationInfo getUserLocationInfo() {
		return userLocationInfo;
	}

	/**
	 * @param userTuneInfo the userTuneInfo to set
	 */
	public void setUserTuneInfo(UserTuneInfo userTuneInfo) {
		this.userTuneInfo = userTuneInfo;
	}

	/**
	 * @return the userTuneInfo
	 */
	public UserTuneInfo getUserTuneInfo() {
		return userTuneInfo;
	}

	/**
	 * @param userContextInfo the userContextInfo to set
	 */
	public void setUserContextInfo(UserContextInfo userContextInfo) {
		this.userContextInfo = userContextInfo;
	}

	/**
	 * @return the userContextInfo
	 */
	public UserContextInfo getUserContextInfo() {
		return userContextInfo;
	}

	/**
	 * @return the userMoodInfo
	 */
	public UserMoodInfo getUserMoodInfo() {
		return userMoodInfo;
	}

	/**
	 * @param userMoodInfo the userMoodInfo to set
	 */
	public void setUserMoodInfo(UserMoodInfo userMoodInfo) {
		this.userMoodInfo = userMoodInfo;
	}	
	
}
