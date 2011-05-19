package de.tudresden.inf.rn.mobilis.xmpp.provider;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.packet.PubSubPacketExtension;

public class PubSubPacketExtensionProvider implements PacketExtensionProvider {

	@Override
	public PacketExtension parseExtension(XmlPullParser xpp) throws Exception {
		
		PubSubPacketExtension pspe = new PubSubPacketExtension();

		boolean bored_yet = false;
		String lastNode = "";
		String node;

		try {
			int eventType = xpp.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT && !bored_yet) {
				if (eventType == XmlPullParser.START_DOCUMENT) {
				} else if (eventType == XmlPullParser.END_DOCUMENT) {
				} else if (eventType == XmlPullParser.START_TAG) {
					lastNode = xpp.getName();
					if (lastNode.equals("affiliations")) {
						node = xpp.getAttributeValue(null, "node");
						pspe.setNode(node);
						bored_yet = true;
					} else if (lastNode.equals("create")) {
						node = xpp.getAttributeValue(null, "node");
						pspe.setNode(node);
						bored_yet = true;
					}
				}
				if (!bored_yet)
					eventType = xpp.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return pspe;
		}

		return pspe;
	}
}
