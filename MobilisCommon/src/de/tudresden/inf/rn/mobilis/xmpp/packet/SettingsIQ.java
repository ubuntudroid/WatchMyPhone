package de.tudresden.inf.rn.mobilis.xmpp.packet;

import org.jivesoftware.smack.packet.IQ;

/**
 *
 * @author Christopher
 */
public class SettingsIQ extends IQ {
    
    public static final String elementName = "query";
    public static final String namespace = "mobilis:iq:setting";
    private String service;
    private String name;
    private String value;
    
    public String getService() {
        return this.service;
    }

    public void setService(String service) {
    	this.service = service;
    }
    
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
    	this.name = name;
    }
    
    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
    	this.value = value;
    }
    
    public SettingsIQ() {
        super();
        this.setType(IQ.Type.SET);
    }
    
    @Override
	public String getChildElementXML() {
    	StringBuffer buf = new StringBuffer();
    	buf.append("<" + elementName + " xmlns=\"" + namespace + "\">\n");
    	
    	buf.append("<service>").append(service).append("</service>\n");
    	buf.append("<name>").append(name).append("</name>\n");
    	buf.append("<value>").append(value).append("</value>\n");
    	
    	buf.append("</" + elementName + ">");
        return buf.toString();
    }
}
