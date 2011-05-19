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

import java.util.LinkedList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.Mobilis;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

/**
 * @author Benjamin Söllner
 */
public class RepositoryDeleteBean extends XMPPBean {

	private static final long serialVersionUID = 1L;
	public static final String NAMESPACE = Mobilis.NAMESPACE + "#services/RepositoryService";
	public static final String CHILD_ELEMENT = "repository-delete";

	protected List<RepositoryItemInfo> items = new LinkedList<RepositoryItemInfo>();

	public List<RepositoryItemInfo> getItems() { return this.items; }

	@Override
	public RepositoryDeleteBean clone() {
		RepositoryDeleteBean twin = new RepositoryDeleteBean();
		twin.id   = this.id;
		twin.from = this.from;
		twin.to   = this.to;
		twin.type = this.type;
		twin.items.clear();
		for (RepositoryItemInfo ri: this.items)
			twin.items.add(ri.clone());
		return twin;
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String childElement = RepositoryDeleteBean.CHILD_ELEMENT;
		boolean done = false;
		this.items.clear();
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(RepositoryItemInfo.CHILD_ELEMENT)) {
					RepositoryItemInfo ri = new RepositoryItemInfo();
					ri.fromXML(parser);
					this.items.add(ri);
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
		return RepositoryDeleteBean.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		return RepositoryDeleteBean.NAMESPACE;
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();
		for (RepositoryItemInfo ri: this.items)
			sb.append(ri.toXML());
		return sb.toString();
	}

}
