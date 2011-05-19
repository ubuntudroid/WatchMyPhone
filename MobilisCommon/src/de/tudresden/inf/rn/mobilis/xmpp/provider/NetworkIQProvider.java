package de.tudresden.inf.rn.mobilis.xmpp.provider;

import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.packet.NetworkIQ;

public class NetworkIQProvider implements IQProvider {

    @Override
    public IQ parseIQ(XmlPullParser parser) throws Exception {

        NetworkIQ iq = new NetworkIQ();
        Map<String,String> params = new HashMap<String,String>();
        boolean done = false;
        boolean inParams = false;

        int eventType = parser.next();
        
        while (!done) {

            eventType = parser.next();

            // XML parser recognized an opening tag
            if (eventType == XmlPullParser.START_TAG) {

                if (!inParams) {
                    if (parser.getName().equals("network")) {
                        iq.setNetwork(parser.nextText());
                    } else if (parser.getName().equals("action")) {
                        iq.setAction(parser.nextText());
                    } else if (parser.getName().equals("params")) {
                        params.clear();
                        inParams = true;
                    }
                } else {
                    // parser is inside the params list
                    if (parser.getName().equals("param")) {
                        String name = "";
                        for (int i = 0; i < parser.getAttributeCount(); i++) {
                            if (parser.getAttributeName(i).equals("name")) {
                                name = parser.getAttributeValue(i);
                                break;
                            }
                        }
                        params.put(name, parser.nextText());
                    }
                }

            }

            // XML parser recognized a closing tag
            else if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals("params")) {
                    iq.setParams(params);
                    inParams = false;
                } 
                else if (parser.getName().equals(NetworkIQ.elementName)) {
                    done = true;
                }
            }
        }

        return iq;
    }

}
