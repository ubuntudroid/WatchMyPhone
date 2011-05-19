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

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo;

/**
 * Class for exchanging information about the mood a user
 * is in. Based on XEP-0107 - User Mood.
 * @author Robert Lübke
 */
public class UserMoodInfo implements XMPPInfo, Cloneable {

	private static final long serialVersionUID = 1L;
	public static final String CHILD_ELEMENT = "mood";
	public static final String NAMESPACE = "http://jabber.org/protocol/mood";
	
	private String moodElement, moodDescription;
	
	/** Constructor for a normal User Mood Item	 */
	public UserMoodInfo(String moodElement, String moodDescription) {	
		this.moodElement=moodElement;
		this.moodDescription=moodDescription;
	}
	
	/** Constructor for an empty User Mood Item.
	 * For example used to disable publishing	 */
	public UserMoodInfo() {		
	}
		

	@Override
	public UserMoodInfo clone() {
		UserMoodInfo twin = new UserMoodInfo(moodElement, moodDescription);
		return twin;
	}
	
	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String childElement = UserMoodInfo.CHILD_ELEMENT;
		
		boolean done = false;

		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
					if (parser.getEventType()==XmlPullParser.START_TAG) {
						moodElement=parser.getName();
					}
					parser.next();
				} else if (tagName.equals("text")) {
					this.moodDescription = parser.nextText();				
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
		String childElement = UserMoodInfo.CHILD_ELEMENT;
		String namespace = UserMoodInfo.NAMESPACE;		

		StringBuilder sb = new StringBuilder()
				.append("<").append(childElement)
				.append(" xmlns=\"").append(namespace).append("\">");
		
		if (this.moodElement != null)
			sb.append("<"+moodElement+" />");		
		if (this.moodDescription != null)
			sb.append("<text>").append(this.moodDescription).append("</text>");		
		
		sb.append("</").append(childElement).append(">");
		return sb.toString();
	}

	@Override
	public String getChildElement() {
		return UserMoodInfo.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		return UserMoodInfo.NAMESPACE;
	}

	/**
	 * @return the moodElement
	 */
	public String getMoodElement() {
		return moodElement;
	}

	/**
	 * @param moodElement the moodElement to set
	 */
	public void setMoodElement(String moodElement) {
		this.moodElement = moodElement;
	}

	/**
	 * @return the moodDescription
	 */
	public String getMoodDescription() {
		return moodDescription;
	}

	/**
	 * @param moodDescription the moodDescription to set
	 */
	public void setMoodDescription(String moodDescription) {
		this.moodDescription = moodDescription;
	}	
	
}
