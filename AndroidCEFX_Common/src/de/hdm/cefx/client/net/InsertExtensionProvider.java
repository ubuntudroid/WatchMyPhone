/*******************************************************************************
 * Copyright (C) 2010 Ansgar Gerlicher
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Stuttgart, Hochschule der Medien: http://www.mi.hdm-stuttgart.de/mmb/
 * Collaborative Editing Framework or XML:
 * http://sourceforge.net/projects/cefx/
 * 
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: http://mobilisplatform.sourceforge.net
 ******************************************************************************/
/**
 * This sourcecode is part of the Collaborative Editing Framework for XML (CEFX).
 * @author Michael Voigt
 */

package de.hdm.cefx.client.net;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class InsertExtensionProvider implements PacketExtensionProvider {

	private Document doc;

	public InsertExtensionProvider() {
		doc=null;
		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
		try {
			docBuilder = dbfac.newDocumentBuilder();
	        doc = docBuilder.newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}


	private Element String2Element(String s) {
        //XmlPullParser xpp = new MXParser();
		
		// only retrieves the last found element! -> if you need all children, use XMLHelper.getElement(String) instead
        XmlPullParserFactory factory;
        XmlPullParser xpp;
		Element el=null;

        try {
			factory = XmlPullParserFactory.newInstance(
			        System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
            factory.setNamespaceAware(true);
            xpp = factory.newPullParser();
            xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false); // cefx namespace probably needs extra processing
        	xpp.setInput( new StringReader (s) );
	        int eventType = xpp.getEventType();
	        while (eventType != XmlPullParser.END_DOCUMENT) {
	        	if (eventType == XmlPullParser.START_TAG) {
	        		el=doc.createElement(xpp.getName());
	        		int count=xpp.getAttributeCount();
	        		int ii;
	        		for (ii=0; ii<count; ii++) {
	        			String name=xpp.getAttributeName(ii);
	        			String val=xpp.getAttributeValue(ii);
	        			el.setAttribute(name, val);
	        		}
	        	} else if(eventType == XmlPullParser.END_TAG) {
	        		//xpp.getName();
	        	} else if(eventType == XmlPullParser.TEXT) {
	        		//xpp.getText();
	        	}
	        	eventType = xpp.next();
	        }
	    } catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    return el;
	}

	public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
		InsertExtension pe=new InsertExtension();

		int eventType = parser.getEventType();
		while (eventType != XmlPullParser.END_TAG) {
			if (eventType == XmlPullParser.START_TAG) {
				int count=parser.getAttributeCount();
				int ii;
				for (ii=0; ii<count; ii++) {
					String name=parser.getAttributeName(ii);
					String val=parser.getAttributeValue(ii);
					pe.setParameterValue(name, val);
				}
			} else if (eventType == XmlPullParser.TEXT) {
				pe.setContent(parser.getText());
				pe.setElement(String2Element(pe.getContent()));
			}
			eventType = parser.next();
		}

		return pe;
	}

}
