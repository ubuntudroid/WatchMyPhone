package de.tudresden.inf.rn.mobilis.xmpp.packet;

import java.util.HashMap;
import org.jivesoftware.smack.packet.IQ;

/**
 *
 * @author Istvan
 */
public class QueryGroupsIQ extends IQ {
    
    public static final String elementName = "query";
    public static final String namespace = "mobilis:iq:groups";
    private HashMap<String, String> mGroups = new HashMap<String, String>();

    public QueryGroupsIQ() {
        super();
        this.setType(IQ.Type.GET);
        //TODO should this be: setType(Type.RESULT); ?
    }
    
    @Override
    public String getChildElementXML() {
        StringBuffer buf = new StringBuffer();
        buf.append("<" + elementName + " xmlns=\"" + namespace + "\">\n");
        
        for (String item : mGroups.keySet()) {
            buf.append("<item name=\"" + item + "\" jid=\"" + mGroups.get(item) + "\"/>\n");
        }
        
        buf.append("</query>");
        return buf.toString();
    }

    public void addItem(String name, String jid) {
        mGroups.put(name, jid);
    }
    
    public HashMap<String, String> getGroups() {
    	return mGroups;
    }
}
