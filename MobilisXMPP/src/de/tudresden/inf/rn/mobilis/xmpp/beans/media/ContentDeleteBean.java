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

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.Mobilis;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

/**
 * @author Benjamin Söllner
 */
public class ContentDeleteBean extends XMPPBean {

	private static final long serialVersionUID = 1L;
	public static final String NAMESPACE = Mobilis.NAMESPACE + "#services/ContentService";
	public static final String CHILD_ELEMENT = "content-delete";
	
	protected String uid;
	
	public void setUid(String uid) { this.uid = uid; }
	
	public String getUid() { return this.uid; }

	@Override
	public ContentDeleteBean clone() {
		ContentDeleteBean twin = new ContentDeleteBean();
		twin.id = this.id;
		twin.uid = this.uid;
		twin.from = this.from;
		twin.to = this.to;
		twin.type = this.type;
		return twin;
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		int eventType = parser.getEventType();
		String childElement = this.getChildElement();
		do {
			switch (eventType) {
			case XmlPullParser.START_TAG:
				if (parser.getName().equals("uid"))
					this.uid = parser.nextText();
				else
					parser.next();
				break;
			default:
				parser.next();
			}
			eventType = parser.getEventType(); 
		} while ( ( eventType != XmlPullParser.END_TAG
					|| !parser.getName().equals(childElement) ) 
				 && eventType != XmlPullParser.END_DOCUMENT );
	}
		
	@Override
	public String payloadToXML() {
		return new StringBuilder()
				.append("<uid>").append(this.uid).append("</uid>")
				.toString();
	}
	
	@Override
	public String getChildElement() {
		return ContentDeleteBean.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		return ContentDeleteBean.NAMESPACE;
	}

}
