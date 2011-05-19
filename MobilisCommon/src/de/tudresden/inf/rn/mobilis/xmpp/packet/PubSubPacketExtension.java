package de.tudresden.inf.rn.mobilis.xmpp.packet;

import org.jivesoftware.smack.packet.PacketExtension;
import se.su.it.smack.packet.XMPPElement;

/**
 * A simple packet extension for including PubSub Elements in Messages (especially in notification messages).
 * @author Istvan
 */
public class PubSubPacketExtension implements PacketExtension {

    private XMPPElement child;
    private String mNode;
    private boolean createNode = false;
    
    public static final String elementName = "pubsub";
    public static final String namespace = "http://jabber.org/protocol/pubsub";

    public void setChild(XMPPElement child) {
        this.child = child;
    }

    public String toXML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<pubsub xlmns='http://jabber.org/protocol/pubsub'>\n");
        if (createNode)
        	sb.append("<create/>\n");
        sb.append(child.toXML());
        sb.append("</pubsub>");

        return sb.toString();
    }

	public void setNode(String node) {
		this.mNode = node;
	}

	/**
	 * Gets the node the client was affiliated to.
	 * @return
	 */
	public String getNode() {
		return mNode;
	}
	
	public void setCreateNode(boolean create) {
		createNode = create;
	}

	@Override
	public String getElementName() {
		return elementName;
	}

	@Override
	public String getNamespace() {
		return namespace;
	}
}

