package de.tudresden.inf.rn.mobilis.xmpp.packet;

import org.jivesoftware.smack.packet.IQ;

public class CreateGroupIQ extends IQ {

    public static final String elementName = "query";
    public static final String namespace = "mobilis:iq:creategroup";
//    private String node;
    private String group;

//    public String getNode() {
//        return node;
//    }
//
//    public void setNode(String node) {
//        this.node = node;
//    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public CreateGroupIQ() {
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
