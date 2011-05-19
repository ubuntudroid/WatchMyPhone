package de.tudresden.inf.rn.mobilis.xmpp.provider;

import java.util.Date;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.packet.LocationIQ;

public class LocationIQProvider implements IQProvider {
	
	@Override
	public IQ parseIQ(XmlPullParser xpp) throws Exception {
		
		LocationIQ locIQ = new LocationIQ();
		int eventType = xpp.getEventType();
		boolean done = false;
		do {
			if (eventType == XmlPullParser.START_TAG) {
				if (xpp.getName().equals("identity")) {
					locIQ.setIdentity( xpp.nextText() );
				} else if (xpp.getName().equals("altitude")) {
					locIQ.setAltitude( Double.parseDouble(xpp.nextText()) );
				} else if (xpp.getName().equals("latitude")) {
					locIQ.setLatitude( Double.parseDouble(xpp.nextText()) );
				} else if (xpp.getName().equals("longitude")) {
					locIQ.setLongitude( Double.parseDouble(xpp.nextText()) );
				} else if (xpp.getName().equals("speed")) {
					locIQ.setSpeed( Float.parseFloat(xpp.nextText()) );
				} else if (xpp.getName().equals("timestamp")) {
					String toParse = xpp.nextText();
					Date d = new Date(Long.parseLong(toParse));
					locIQ.setTimestamp(d);
				} else if (xpp.getName().equals("proximity")) {
					String text = xpp.nextText();
					locIQ.setProximity( text.equals("true") );
				} else if (xpp.getName().equals("alert")) {
					String text = xpp.nextText();
					locIQ.setAlert( text.equals("true") );
				}
				eventType = xpp.next();				
			} else if (eventType == XmlPullParser.END_TAG &&
					xpp.getName().equals(LocationIQ.elementName)) {
                done = true;
			} else {
				eventType = xpp.next();				
			}
		} while (!done);

		return locIQ;
		
	}

}
