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

import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.Mobilis;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo;

/**
 * A PublishItemInfo can contain a list of ContextItemInfos.
 * Based on XEP-0060 - Publish-Subscribe
 * @author Robert Lübke
 */
public class PublishItemInfo implements XMPPInfo, Cloneable {

	private static final long serialVersionUID = 1L;
	public static final String CHILD_ELEMENT = "publish";
	public static final String NAMESPACE = Mobilis.NAMESPACE;

	private ArrayList<ContextItemInfo> contextItemInfos;
	private String node;
		
	/** Constructor for an empty PublishItemInfo	 */
	public PublishItemInfo() {
	}
	
	/** Constructor for a PublishItemInfo containing one ContextItemInfo  */
	public PublishItemInfo(String node, ContextItemInfo contextItemInfo) {
		this.node=node;
		this.contextItemInfos=new ArrayList<ContextItemInfo>();
		this.contextItemInfos.add(contextItemInfo);
	}
	
	@Override
	public PublishItemInfo clone() {
		PublishItemInfo twin = new PublishItemInfo();
		twin.node=this.node;
		twin.contextItemInfos=this.contextItemInfos;
		return twin;
	}
	
	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String childElement = PublishItemInfo.CHILD_ELEMENT;
		
		boolean done = false;

		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					for (int i = 0; i < parser.getAttributeCount(); i++)
						if (parser.getAttributeName(i).equals("node"))
							this.node = parser.getAttributeValue(i);					
					parser.next();					
				} else if (tagName.equals(ContextItemInfo.CHILD_ELEMENT)) {					
					ContextItemInfo cii = new ContextItemInfo();
					cii.fromXML(parser);
					addContextItemInfo(cii);
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
		String childElement = PublishItemInfo.CHILD_ELEMENT;
		
		StringBuilder sb = new StringBuilder()
				.append("<").append(childElement).append(" node=\""+node+"\"").append(">");
		
		if (this.contextItemInfos != null)
			for (ContextItemInfo cii : contextItemInfos)
				sb.append(cii.toXML());		
		
		sb.append("</").append(childElement).append(">");
		return sb.toString();
	}

	@Override
	public String getChildElement() {
		return PublishItemInfo.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		return PublishItemInfo.NAMESPACE;
	}

	/**
	 * @param contextItemInfos the contextItemInfos to set
	 */
	public void setContextItemInfos(ArrayList<ContextItemInfo> contextItemInfos) {
		this.contextItemInfos = contextItemInfos;
	}

	/**
	 * @return the contextItemInfos
	 */
	public ArrayList<ContextItemInfo> getContextItemInfos() {
		return contextItemInfos;
	}
	
	public void addContextItemInfo(ContextItemInfo contextItemInfo) {
		if (this.contextItemInfos==null) {
			this.contextItemInfos=new ArrayList<ContextItemInfo>();
		}
		contextItemInfos.add(contextItemInfo);
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

	
}
