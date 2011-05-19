package de.tudresden.inf.rn.mobilis.xmpp.packet;

import org.jivesoftware.smack.packet.IQ;

public class MonitoringIQ extends IQ {

	public static final String elementName = "query";
	public static final String namespace = "mobilis:iq:monitoring";
	public static final String PING = "ping";
	public static final String START_TIMER = "startTimer";
	private String statusMsg;

	public String getStatusMsg() {
		return statusMsg;
	}

	public void setStatusMsg(String statusMsg) {
		this.statusMsg = statusMsg;
	}

	@Override
	public String getChildElementXML() {
		StringBuffer buf = new StringBuffer();
		buf.append("<" + elementName + " xmlns=\"" + namespace + "\">\n");
		buf.append("<status>").append(statusMsg == null ? "" : statusMsg).append("</status>\n");
		buf.append("</" + elementName + ">");
		return buf.toString();
	}
}