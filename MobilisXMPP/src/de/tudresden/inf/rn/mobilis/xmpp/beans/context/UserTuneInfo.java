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
 * Class for exchanging information about the music a user
 * listens to. Based on XEP-0118 - User Tune.
 * @author Robert Lübke
 */
public class UserTuneInfo implements XMPPInfo, Cloneable {

	private static final long serialVersionUID = 1L;
	public static final String CHILD_ELEMENT = "tune";
	public static final String NAMESPACE = "http://jabber.org/protocol/tune";

	public String artist, source, title, track, uri;	
	public int length, rating;
	
	/** Constructor for an empty User Tune Item	 */
	public UserTuneInfo() {
		initializeNumbers();
	}
	
	private void initializeNumbers() {
		this.length = Integer.MIN_VALUE;
		this.rating = Integer.MIN_VALUE;		
	}

	@Override
	public UserTuneInfo clone() {
		UserTuneInfo twin = new UserTuneInfo();
		
		twin.artist = this.artist;
		twin.length = this.length;
		twin.rating = this.rating;
		twin.source = this.source;
		twin.title = this.title;
		twin.track = this.track;
		twin.uri = this.uri;
		
		return twin;
	}
	
	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String childElement = UserTuneInfo.CHILD_ELEMENT;
		
		boolean done = false;

		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
				} else if (tagName.equals("artist")) {
					this.artist = parser.nextText();
				} else if (tagName.equals("length")) {
					this.length = Integer.valueOf(parser.nextText()).intValue();
				} else if (tagName.equals("rating")) {
					this.rating = Integer.valueOf(parser.nextText()).intValue();
				} else if (tagName.equals("source")) {
					this.source = parser.nextText();
				} else if (tagName.equals("title")) {
					this.title = parser.nextText();
				} else if (tagName.equals("track")) {
					this.track = parser.nextText();
				} else if (tagName.equals("uri")) {
					this.uri = parser.nextText();					
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
		String childElement = UserTuneInfo.CHILD_ELEMENT;
		String namespace = UserTuneInfo.NAMESPACE;		

		StringBuilder sb = new StringBuilder()
				.append("<").append(childElement)
				.append(" xmlns=\"").append(namespace).append("\">");
		
		if (this.artist != null)
			sb.append("<artist>").append(this.artist).append("</artist>");
		if (this.length > Integer.MIN_VALUE)
			sb.append("<length>").append(this.length).append("</length>");
		if (this.rating > Integer.MIN_VALUE)
			sb.append("<rating>").append(this.rating).append("</rating>");
		if (this.source != null)
			sb.append("<source>").append(this.source).append("</source>");
		if (this.title != null)
			sb.append("<title>").append(this.title).append("</title>");
		if (this.track != null)
			sb.append("<track>").append(this.track).append("</track>");
		if (this.uri != null)
			sb.append("<uri>").append(this.uri).append("</uri>");		
		
		sb.append("</").append(childElement).append(">");
		return sb.toString();
	}

	@Override
	public String getChildElement() {
		return UserTuneInfo.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		return UserTuneInfo.NAMESPACE;
	}	
	
}
