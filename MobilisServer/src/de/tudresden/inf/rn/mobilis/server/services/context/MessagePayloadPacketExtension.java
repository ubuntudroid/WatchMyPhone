package de.tudresden.inf.rn.mobilis.server.services.context;

import org.jivesoftware.smack.packet.PacketExtension;

import de.tudresden.inf.rn.mobilis.xmpp.beans.context.UserContextInfo;

public class MessagePayloadPacketExtension implements PacketExtension {

	ContextNode node;
	String userNodeFormat;
	
	public MessagePayloadPacketExtension(ContextNode node, String userNodeFormat) {
		this.node=node;	
		this.userNodeFormat = userNodeFormat;
	}	
	
	@Override
	public String getElementName() {		
		return "event";
	}

	@Override
	public String getNamespace() {		
		return "http://jabber.org/protocol/pubsub#event";
	}

	@Override
	public String toXML() {		
		StringBuilder sb = new StringBuilder()
			.append("<").append(getElementName())
			.append(" xmlns=\"").append(getNamespace()).append("\">");
			
		if (node!=null) {
			sb.append("<items node='"+userNodeFormat+"'>");
			for (ContextItem ci : node.getEntries()) {
				UserContextInfo uci = new UserContextInfo(ci.getType(), ci.getKey(), ci.getValue(), null);
				sb.append("<item>").append(uci.toXML()).append("</item>");				
			}			
			sb.append("</items>");			
		}
		sb.append("</").append(getElementName()).append(">");
		
		return sb.toString();
	}

}
