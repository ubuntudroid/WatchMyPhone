package de.tudresden.inf.rn.mobilis.xmpp.provider;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.packet.MonitoringIQ;

public class MonitoringIQProvider implements IQProvider {

	@Override
	public IQ parseIQ(XmlPullParser xpp) throws Exception {
		MonitoringIQ monitoringIQ = new MonitoringIQ();
		
		int eventType = xpp.getEventType();
        do {
            if (eventType == XmlPullParser.START_TAG) {
            	if (xpp.getName().equals("status")) {
            		monitoringIQ.setStatusMsg(xpp.nextText());
            	}
            } else if (eventType == XmlPullParser.END_TAG) {
            	if (xpp.getName().equals(MonitoringIQ.elementName))
            		break;
            }
            eventType = xpp.next();
        } while (eventType != XmlPullParser.END_DOCUMENT);
		
		return monitoringIQ;
	}

}
