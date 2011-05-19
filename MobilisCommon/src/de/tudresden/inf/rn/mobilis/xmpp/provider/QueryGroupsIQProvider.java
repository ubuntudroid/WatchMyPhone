package de.tudresden.inf.rn.mobilis.xmpp.provider;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.packet.QueryGroupsIQ;

public class QueryGroupsIQProvider implements IQProvider {

	/**
	 * Parse the IQ package and find all items.
	 */
	@Override
	public IQ parseIQ(XmlPullParser xpp) throws Exception {
		QueryGroupsIQ qig = new QueryGroupsIQ();
		
		int eventType = xpp.getEventType();
        do {
            if (eventType == XmlPullParser.START_TAG) {
            	if (xpp.getName().equals("item")) {
            		qig.addItem(xpp.getAttributeValue(null, "name"), xpp.getAttributeValue(null, "jid"));
            	}
            } else if (eventType == XmlPullParser.END_TAG) {
            	if (xpp.getName().equals("query"))
            		break;
            }
            eventType = xpp.next();
        } while (eventType != XmlPullParser.END_DOCUMENT);

		return qig;
	}

}
