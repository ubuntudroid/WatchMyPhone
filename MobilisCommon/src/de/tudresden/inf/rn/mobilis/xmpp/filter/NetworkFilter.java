package de.tudresden.inf.rn.mobilis.xmpp.filter;

import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

import de.tudresden.inf.rn.mobilis.xmpp.packet.BuddylistIQ;
import de.tudresden.inf.rn.mobilis.xmpp.packet.NetworkIQ;

public class NetworkFilter implements PacketFilter {
	
	private String network;

	public NetworkFilter(String network) {
		this.network = network;
	}

	/**
	 * A filter for BuddylistIQ packets. Returns true only if the packet is an
	 * BuddylistIQ packet and its matches the network provided in the constructor.
	 * 
	 * @author Christopher
	 */
	@Override
	public boolean accept(Packet packet) {
		return (packet instanceof BuddylistIQ && ((BuddylistIQ) packet).getNetwork().equals(network))
				|| (packet instanceof NetworkIQ && ((NetworkIQ) packet).getNetwork().equals(network));
	}

}
