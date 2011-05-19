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

package de.tudresden.inf.rn.mobilis.xmpp.beans.media;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import de.tudresden.inf.rn.mobilis.xmpp.beans.Mobilis;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo;

/**
 * @author Benjamin Söllner
 */
public class ContentItemInfo implements XMPPInfo {

	private static final long serialVersionUID = 1L;
	public static final String NAMESPACE = Mobilis.NAMESPACE + "#services/ContentService";
	public static final String CHILD_ELEMENT = "content-item";

	private String uid;
	private String repository;
	private String description;
	
	public ContentItemInfo() {}
	
	public void setUid(String uid) { this.uid = uid; }
	public void setRepository(String repository) { this.repository = repository; }
	public void setDescription(String description) { this.description = description; }
	
	public String getUid() { return this.uid; }
	public String getRepository() { return this.repository; }
	public String getDescription() { return this.description; }
	
	public ContentItemInfo(XmlPullParser parser) throws XmlPullParserException, IOException {
		this.fromXML(parser);
	}
	
	@Override
	public void fromXML(XmlPullParser parser) throws XmlPullParserException, IOException {
		int eventType = parser.getEventType();
		String childElement = ContentItemInfo.CHILD_ELEMENT;
		boolean inside = false;
		do {
			switch (eventType) {
			case XmlPullParser.START_TAG:
				if (parser.getName().equals(childElement)) {
					inside = true;
					parser.next();
				} else if (inside && parser.getName().equals("description"))
					this.description = parser.nextText();
				else if (inside && parser.getName().equals("uid"))
					this.uid = parser.nextText();
				else if (inside && parser.getName().equals("repository"))
					this.repository = parser.nextText();
				else
					parser.next();
				break;
			case XmlPullParser.END_TAG:
				if (inside && parser.getName().equals(childElement))
					inside = false;
				parser.next();
				break;
			case XmlPullParser.END_DOCUMENT:
				break;
			default:
				parser.next();
			}
			eventType = parser.getEventType(); 
		} while ( eventType != XmlPullParser.END_DOCUMENT );
	}

	
	
	@Override
	public String toXML() {
		// TODO Auto-generated method stub
		return new StringBuilder()
				.append("<")
						.append(ContentItemInfo.CHILD_ELEMENT)
						.append(" namespace=\"")
						.append(ContentItemInfo.NAMESPACE)
						.append("\">")
					.append("<uid>").append(this.uid).append("</uid>")
					.append("<repository>").append(this.repository).append("</repository>")
					.append("<description>").append(this.description).append("</description>")
				.append("</").append(ContentItemInfo.CHILD_ELEMENT).append(">")
				.toString();	
	}

	@Override
	public String getChildElement() {
		return ContentItemInfo.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		// TODO Auto-generated method stub
		return ContentItemInfo.NAMESPACE;
	}
}
