package de.tudresden.inf.rn.mobilis.xmpp.packet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.jivesoftware.smack.packet.IQ;

/**
 *
 * @author Christopher, Benjamin
 */
public class BuddylistIQ extends IQ {
    
    public static final String elementName = "query";
    public static final String namespace = "mobilis:iq:buddylist";
    private String network;
    private String identity;
    private Collection<String> buddies;
    
    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
    	this.network = network;
    }
    
    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
    	this.identity = identity;
    }
    
    public Collection<String> getBuddies() {
        return buddies;
    }

    public void setBuddies(Collection<String> buddies) {
    	this.buddies = buddies;
    }

    public BuddylistIQ() {
        super();
        this.setType(IQ.Type.GET);
        buddies = new HashSet<String>();
    }
    
    @Override
	public String getChildElementXML() {
        
        StringBuffer buf = new StringBuffer();
        buf.append("<" + elementName + " xmlns=\"" + namespace + "\">\n");
  		buf.append("<network>").append(network == null ? "" : network).append("</network>\n");
   		buf.append("<identity>").append(identity == null ? "" : identity).append("</identity>\n");
    	
        buf.append("<buddies>\n");        
		Iterator<String> buddiesIterator = buddies.iterator();
		while (buddiesIterator.hasNext()) {
		    buf.append("<buddy>");
		    buf.append(buddiesIterator.next());
		    buf.append("</buddy>\n");
		}
        buf.append("</buddies>\n");
        
    	buf.append("</" + elementName + ">");
        return buf.toString();
    }
}
