package de.tudresden.inf.rn.mobilis.xmpp.packet;

import org.jivesoftware.smack.packet.IQ;

/**
 *
 * @author Istvan
 */
public class JoinGroupIQ extends IQ {
    
    public static final String elementName = "query";
    public static final String namespace = "mobilis:iq:joingroup";
    private String group;
    
    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
    
    public JoinGroupIQ() {
        super();
        this.setType(IQ.Type.GET);
    }
    
    @Override
	public String getChildElementXML() {
        StringBuffer buf = new StringBuffer();
        buf.append("<" + elementName + " xmlns=\"" + namespace + "\">\n");
  		buf.append("<group>").append(group == null ? "" : group).append("</group>\n");
    	buf.append("</" + elementName + ">");
        return buf.toString();
    }
}
