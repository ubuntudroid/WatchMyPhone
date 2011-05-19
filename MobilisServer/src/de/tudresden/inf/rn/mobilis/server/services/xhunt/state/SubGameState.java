package de.tudresden.inf.rn.mobilis.server.services.xhunt.state;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

public abstract class SubGameState {

	public abstract void processPacket(XMPPBean bean);
	
}
