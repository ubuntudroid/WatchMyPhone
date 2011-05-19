package de.tudresden.inf.rn.mobilis.xmpp.filter;

import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

import de.tudresden.inf.rn.mobilis.xmpp.packet.SettingsIQ;

public class ServiceFilter implements PacketFilter {
	
	private String service;

	public ServiceFilter(String service) {
		this.service = service;
	}

	/**
	 * A filter for SettingsIQ packets. Returns true only if the packet is an
	 * SettingsIQ packet and its matches the service provided in the constructor.
	 * 
	 * @author Christopher
	 */
	@Override
	public boolean accept(Packet packet) {
		return (packet instanceof SettingsIQ && ((SettingsIQ) packet).getService().equals(service));
	}

}
