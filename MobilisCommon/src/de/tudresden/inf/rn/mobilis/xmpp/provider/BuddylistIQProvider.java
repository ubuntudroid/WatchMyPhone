package de.tudresden.inf.rn.mobilis.xmpp.provider;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.packet.BuddylistIQ;

public class BuddylistIQProvider implements IQProvider {

    @Override
    public IQ parseIQ(XmlPullParser parser) throws Exception {

        BuddylistIQ iq = new BuddylistIQ();
        boolean done = false;
        boolean inBuddies = false;

        int eventType = parser.next();
        
        while (!done) {

            eventType = parser.next();

            // XML parser recognized an opening tag
            if (eventType == XmlPullParser.START_TAG) {

                if (!inBuddies) {
                    if (parser.getName().equals("network")) {
                        iq.setNetwork(parser.nextText());
                    } else if (parser.getName().equals("identity")) {
                        iq.setIdentity(parser.nextText());
                    } else if (parser.getName().equals("buddies")) {
                        inBuddies = true;
                    }
                } else {
                    // parser is inside the buddy list
                    if (parser.getName().equals("buddy")) {
                        iq.getBuddies().add(parser.nextText());
                    }
                }

            }

            // XML parser recognized a closing tag
            else if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals("buddies")) {
                    inBuddies = false;
                } 
                else if (parser.getName().equals(BuddylistIQ.elementName)) {
                    done = true;
                }
            }
        }

        return iq;
    }

}
