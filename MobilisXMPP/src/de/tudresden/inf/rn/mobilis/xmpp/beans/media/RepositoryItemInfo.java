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

import java.util.Map;
import java.util.TreeMap;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.Mobilis;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo;

/**
 * @author Benjamin Söllner
 */
public class RepositoryItemInfo implements XMPPInfo {

	private static final long serialVersionUID = 1L;
	public static final String NAMESPACE = Mobilis.NAMESPACE + "#services/RepositoryService";
	public static final String CHILD_ELEMENT = "repository-item";
	
	protected Map<String, String> slices = new TreeMap<String, String>();
	protected String uid = null;
	protected String content = null;
	protected String owner = null;
	
	public Map<String, String> getSlices() { return this.slices; }
	public String getUid() { return this.uid; }
	public String getContent() { return this.content; }	
	public String getOwner() { return this.owner; }
	
	public void setUid(String uid) { this.uid = uid; }
	public void setOwner(String owner) { this.owner = owner; }
	public void setContent(String content) { this.content = content; }
	
	@Override
	public RepositoryItemInfo clone() {
		RepositoryItemInfo twin = new RepositoryItemInfo();
		twin.slices.clear(); twin.slices.putAll(this.slices);
		twin.uid = this.uid;
		return twin;
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String ce = RepositoryItemInfo.CHILD_ELEMENT;
		boolean done = false;
		this.slices.clear();
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				int attributesCount = parser.getAttributeCount();
				if (tagName.equals(ce)) {
					for (int i = 0; i < attributesCount; i++)
						if (parser.getAttributeName(i).equals("uid"))
							this.uid = parser.getAttributeValue(i);
						else if (parser.getAttributeName(i).equals("owner"))
							this.owner = parser.getAttributeValue(i);
						else if (parser.getAttributeName(i).equals("content"))
							this.content = parser.getAttributeValue(i);
					parser.next();
				} else if (tagName.equals("slice")) {
					String key = null;
					for (int i = 0; i < attributesCount; i++)
						if (parser.getAttributeName(i).equals("key"))
							key = parser.getAttributeValue(i);
					if (key != null)
						this.slices.put(key, parser.nextText());
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
		return RepositoryItemInfo.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		return RepositoryItemInfo.NAMESPACE;
	}
	
	@Override
	public String toXML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<").append(RepositoryItemInfo.CHILD_ELEMENT);
		if (this.uid != null)
			sb.append(" uid=\"").append(this.uid).append("\"");
		if (this.owner != null)
			sb.append(" owner=\"").append(this.owner).append("\"");
		if (this.content != null)
			sb.append(" content=\"").append(this.content).append("\"");
		sb.append(">");
		for (String key: this.slices.keySet())
			sb.append("<slice key=\"").append(key).append("\">")
					.append(this.slices.get(key))
					.append("</slice>");
		sb.append("</").append(RepositoryItemInfo.CHILD_ELEMENT).append(">");
		return sb.toString();
	}

}
