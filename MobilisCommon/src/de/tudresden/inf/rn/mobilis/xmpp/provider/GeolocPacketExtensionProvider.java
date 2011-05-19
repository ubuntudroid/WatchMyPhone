package de.tudresden.inf.rn.mobilis.xmpp.provider;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.packet.GeolocPacketExtension;

public class GeolocPacketExtensionProvider implements PacketExtensionProvider {

	@Override
	public PacketExtension parseExtension(XmlPullParser xpp) {
		GeolocPacketExtension geoLocation = new GeolocPacketExtension();
//		geoLocation.setLat(51.04);
//		geoLocation.setLon(13.73);
//		return geoLocation;
		
		// pull XEP-0080 attributes out of pubsub message
		boolean in_node = false;
		boolean bored_yet = false;
		String lastNode = "";

		try {
			// see
			// http://www.xmlpull.org/v1/doc/api/org/xmlpull/v1/XmlPullParser.html
			// http://www.xmlpull.org/v1/download/unpacked/src/java/samples/MyXmlPullApp.java
			int eventType = xpp.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT && !bored_yet) {
				if (eventType == XmlPullParser.START_DOCUMENT) {
				} else if (eventType == XmlPullParser.END_DOCUMENT) {
				} else if (eventType == XmlPullParser.START_TAG) {
					// buffer current node 
					in_node = true;
					lastNode = xpp.getName();
					if (lastNode.equals("lat") || lastNode.equals("lon"));
				} else if (eventType == XmlPullParser.END_TAG) {
				} else if (eventType == XmlPullParser.TEXT) {
					if (in_node) {
						String txt = xpp.getText();
						if (lastNode.equals("alt"))
							geoLocation.setAlt(Double.parseDouble(txt));
						else if (lastNode.equals("lat"))
							geoLocation.setLat(Double.parseDouble(txt));
						else if (lastNode.equals("lon")) {
							geoLocation.setLon(Double.parseDouble(txt));
							return geoLocation;
						}
						
						in_node = false;
						// TODO implement the rest
					}
				}
				eventType = xpp.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return geoLocation;
		}
		return geoLocation;
	}

}