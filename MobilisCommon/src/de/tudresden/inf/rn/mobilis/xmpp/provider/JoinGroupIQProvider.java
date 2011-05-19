package de.tudresden.inf.rn.mobilis.xmpp.provider;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.packet.JoinGroupIQ;

public class JoinGroupIQProvider implements IQProvider {

	@Override
	public IQ parseIQ(XmlPullParser xpp) throws Exception {
		JoinGroupIQ jgi = new JoinGroupIQ();
		
		int eventType = xpp.getEventType();
        do {
            if (eventType == XmlPullParser.START_TAG) {
            	if (xpp.getName().equals("group")) {
            		jgi.setGroup(xpp.nextText());
            	}
            } else if (eventType == XmlPullParser.END_TAG) {
            	if (xpp.getName().equals(JoinGroupIQ.elementName))
            		break;
            }
            eventType = xpp.next();
        } while (eventType != XmlPullParser.END_DOCUMENT);
		
		return jgi;
	}

}
