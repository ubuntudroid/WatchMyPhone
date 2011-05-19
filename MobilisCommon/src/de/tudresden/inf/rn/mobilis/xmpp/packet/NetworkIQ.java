package de.tudresden.inf.rn.mobilis.xmpp.packet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jivesoftware.smack.packet.IQ;

/**
 *
 * @author Christopher
 */
public class NetworkIQ extends IQ {
    
    public static final String elementName = "query";
    public static final String namespace = "mobilis:iq:network";
    private String network;
    private String action;
    private Map<String, String> params;
    
    public String getNetwork() {
        return this.network;
    }

    public void setNetwork(String network) {
    	this.network = network;
    }
    
    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
    	this.action = action;
    }
    
    public Map<String, String> getParams() {
        return this.params;
    }

    public void setParams(Map<String, String> params) {
    	this.params = params;
    }
    
    public NetworkIQ() {
        super();
        this.setType(IQ.Type.GET);
        this.params = new HashMap<String, String>();
    }
    
    @Override
	public String getChildElementXML() {
    	StringBuffer buf = new StringBuffer();
    	buf.append("<" + elementName + " xmlns=\"" + namespace + "\">\n");
    	
    	buf.append("<network>");
    	buf.append(network);
    	buf.append("</network>\n");
    	
    	buf.append("<action>");
    	buf.append(action);
    	buf.append("</action>\n");
    	
        buf.append("<params>\n");
        
        if (this.params != null) {
        	Set<String> paramKeys = params.keySet();
	        Iterator<String> paramsIterator = paramKeys.iterator();
	    	while (paramsIterator.hasNext()) {
	    		String param = paramsIterator.next();
	            buf.append("<param name=\"" + param + "\">");
	            buf.append(params.get(param));
	            buf.append("</param>\n");
	    	}
        }
        
        buf.append("</params>\n");
        
    	buf.append("</" + elementName + ">");
        return buf.toString();
    }
}
