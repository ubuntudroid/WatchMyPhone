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
public class ContentTransferBean extends XMPPBean {

	private static final long serialVersionUID = 1L;
	public static final String NAMESPACE = Mobilis.NAMESPACE + "#services/ContentService";
	public static final String CHILD_ELEMENT = "content-transfer";
	
	protected String uid;
	protected String retrieveFrom;
	protected String sendTo;
	
	public void setUid(String uid) { this.uid = uid; }
	public void setRetrieveFrom(String retrieveFrom) { this.retrieveFrom = retrieveFrom; }
	public void setSendTo(String sendTo) { this.sendTo = sendTo; }
	
	public String getUid() { return this.uid; }
	public String getRetrieveFrom() { return this.retrieveFrom; }
	public String getSendTo() { return this.sendTo; }

	@Override
	public ContentTransferBean clone() {
		ContentTransferBean twin = new ContentTransferBean();
		twin.id = this.id;
		twin.uid = this.uid;
		twin.retrieveFrom = this.retrieveFrom;
		twin.sendTo = this.sendTo;
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
				else if (parser.getName().equals("retrieve-from"))
					this.retrieveFrom = parser.nextText();
				else if (parser.getName().equals("send-to"))
					this.sendTo = parser.nextText();
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
				.append("<retrieve-from>").append(this.retrieveFrom).append("</retrieve-from>")
				.append("<send-to>").append(this.sendTo).append("</send-to>")
				.toString();
	}
	
	@Override
	public String getChildElement() {
		return ContentTransferBean.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		return ContentTransferBean.NAMESPACE;
	}


}
