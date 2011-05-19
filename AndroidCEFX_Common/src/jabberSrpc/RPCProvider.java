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
package jabberSrpc;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

public class RPCProvider implements IQProvider {

	String   methodName;
	String   data;
	boolean  request;
	boolean  response;

	public IQ parseIQ(XmlPullParser parser) throws Exception {
		RPCPacket packet=null;
		boolean   run=true;
		String    text="";
		String    name;

		methodName=null;
		data=null;
		request=false;
		response=false;

        while(run) {
            int eventType;
            eventType = parser.next();

            switch(eventType) {
                case XmlPullParser.START_TAG:
                	text="";
                	name=parser.getName();
                	if ("methodCall".equals(name)) {
                		request=true;
                	}
                	if ("methodResponse".equals(name)) {
                		request=true;
                	}
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if("query".equals(name)) {
                        run = false;
                    } else {
                        endElement(name,text);
                    }
                    break;
                case XmlPullParser.TEXT:
                	String s=parser.getText();

                	if (s.length()>13) {
                		if (s.startsWith("<![CDATA[")) {
                			s=s.substring(9, s.length()-3);
                		}
                	}

                    text=text+s;
                    break;
            }
        }

        if (request || response) {
        	packet=new RPCPacket();
        	packet.setStringData(data);
        	packet.setMethodName(methodName);
        	if (request) { packet.setPacketType(RPCPacket.REQUEST); }
        	if (response) { packet.setPacketType(RPCPacket.RESPONSE); }
        }

        return packet;
	}

	private void endElement(String tag,String text) {
		if ("methodName".equals(tag)) {
			methodName=text.trim();
		}
		if ("base64".equals(tag)) {
			data=text;
		}
	}

}
