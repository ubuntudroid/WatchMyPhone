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
 * Class for exchanging information about the context
 * of a user.
 * @author Robert Lübke
 */
public class UserContextInfo implements XMPPInfo, Cloneable {

	private static final long serialVersionUID = 1L;
	public static final String CHILD_ELEMENT = "contextitem";
	public static final String NAMESPACE = "mobilis:context";

	private int type;
	private String key, value, path;
	private long expirationDate;
	
	/** Constructor for User Context Item with all neccessary parameters */
	public UserContextInfo(int type, String key, String value, String path) {
		this.type=type;
		this.key=key;
		this.value=value;
		this.path=path;
		this.expirationDate = Long.MIN_VALUE;
	}
	
	/** Constructor for User Context Item  */
	public UserContextInfo(int type, String key, String value, String path, long expirationdate) {
		this.type=type;
		this.key=key;
		this.value=value;
		this.path=path;
		this.expirationDate=expirationdate;
	}
	
	/** Constructor for an empty User Tune Item	 */
	public UserContextInfo() {		
	}
		

	@Override
	public UserContextInfo clone() {
		UserContextInfo twin = new UserContextInfo(type, key, value, path);
		twin.setExpirationDate(this.expirationDate);
		return twin;
	}
	
	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String childElement = UserContextInfo.CHILD_ELEMENT;
		
		boolean done = false;

		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					int attributesCount = parser.getAttributeCount();
					for (int i = 0; i < attributesCount; i++)
						if (parser.getAttributeName(i).equals("key"))
							this.key = parser.getAttributeValue(i);
						else if (parser.getAttributeName(i).equals("value"))
							this.value= parser.getAttributeValue(i);
						else if (parser.getAttributeName(i).equals("type"))
							this.type = Integer.parseInt(parser.getAttributeValue(i));
						else if (parser.getAttributeName(i).equals("path"))
							this.path = parser.getAttributeValue(i);
						else if (parser.getAttributeName(i).equals("expirationDate"))
							this.expirationDate= Long.valueOf(parser.getAttributeValue(i));					
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
		String childElement = UserContextInfo.CHILD_ELEMENT;	

		StringBuilder sb = new StringBuilder()
				.append("<").append(childElement).append(" ");		
		if (this.key != null && this.value!=null) {
			sb.append("key='"+key+"' value='"+value+"' ");
			if (this.path!=null)
				sb.append("path='"+path+"' ");
			if (this.type>Integer.MIN_VALUE)
				sb.append("type='"+type+"' ");	
			if (this.expirationDate>Long.MIN_VALUE)
				sb.append("expirationDate='"+expirationDate+"' ");	
		}
		sb.append(" />");
		return sb.toString();
	}

	@Override
	public String getChildElement() {
		return UserContextInfo.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		return UserContextInfo.NAMESPACE;
	}

	/**
	 * @param expirationDate the expirationDate to set
	 */
	public void setExpirationDate(long expirationDate) {
		this.expirationDate = expirationDate;
	}

	/**
	 * @return the expirationDate
	 */
	public long getExpirationDate() {
		return expirationDate;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}	
	
}
