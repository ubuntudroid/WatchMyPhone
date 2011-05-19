package de.tudresden.inf.rn.mobilis.xmpp.provider;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.packet.SettingsIQ;

public class SettingsIQProvider implements IQProvider {

	@Override
	public IQ parseIQ(XmlPullParser xpp) throws Exception {
		
		SettingsIQ sIQ = new SettingsIQ();
		int eventType = xpp.getEventType();
		boolean done = false;
		do {
			if (eventType == XmlPullParser.START_TAG) {
				if (xpp.getName().equals("service")) {
					String text = xpp.nextText();
					sIQ.setService(text);
				} else if (xpp.getName().equals("name")) {
					String text = xpp.nextText();
					sIQ.setName(text);
				} else if (xpp.getName().equals("value")) {
					String text = xpp.nextText();
					sIQ.setValue(text);
				}
				eventType = xpp.next();
			} else if (eventType == XmlPullParser.END_TAG &&
					xpp.getName().equals(SettingsIQ.elementName)) {
				done = true;
			} else {
				eventType = xpp.next();
			}
		} while (!done);
		
		return sIQ;
	}

}
