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
 * An UnsubscribeItemInfo is sent to unsubscribe from another user's context node.
 * Based on XEP-0060 - Publish-Subscribe
 * @author Robert Lübke
 */
public class UnsubscribeItemInfo implements XMPPInfo, Cloneable {

	private static final long serialVersionUID = 1L;
	public static final String CHILD_ELEMENT = "unsubscribe";
	public static final String NAMESPACE = Mobilis.NAMESPACE;

	private String node, jid;
		
	/** Constructor for an empty UnsubscribeItemInfo	 */
	public UnsubscribeItemInfo() {
	}
	
	/**
	 * Constructor for a UnsubscribeItemInfo to unsubscribe from a user's context node
	 * @param node the Node of the context tree to unsubscribe from
	 * @param jid XMPP-ID of the user to unsubscribe from
	 */
	public UnsubscribeItemInfo(String node, String jid) {
		this.node=node;
		this.jid=jid;
	}
	
	@Override
	public UnsubscribeItemInfo clone() {
		UnsubscribeItemInfo twin = new UnsubscribeItemInfo(node, jid);		
		return twin;
	}
	
	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String childElement = UnsubscribeItemInfo.CHILD_ELEMENT;
		
		boolean done = false;

		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					for (int i = 0; i < parser.getAttributeCount(); i++)
						if (parser.getAttributeName(i).equals("node"))
							this.node = parser.getAttributeValue(i);	
						else if (parser.getAttributeName(i).equals("jid"))
							this.jid = parser.getAttributeValue(i);	
					parser.next();				
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
		String childElement = UnsubscribeItemInfo.CHILD_ELEMENT;
		
		StringBuilder sb = new StringBuilder()
				.append("<").append(childElement)
				.append(" node=\""+this.node+"\"")
				.append(" jid=\""+this.jid+"\"")
				.append("/>");
		
		return sb.toString();
	}

	@Override
	public String getChildElement() {
		return UnsubscribeItemInfo.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		return UnsubscribeItemInfo.NAMESPACE;
	}

	/**
	 * @param node the node to set
	 */
	public void setNode(String node) {
		this.node = node;
	}

	/**
	 * @return the node
	 */
	public String getNode() {
		return node;
	}

	/**
	 * @param jid the jid to set
	 */
	public void setJid(String jid) {
		this.jid = jid;
	}

	/**
	 * @return the jid
	 */
	public String getJid() {
		return jid;
	}

	
}
