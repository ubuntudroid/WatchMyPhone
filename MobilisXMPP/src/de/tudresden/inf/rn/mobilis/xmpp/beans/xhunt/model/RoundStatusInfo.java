package de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.model;

public class RoundStatusInfo {
	public String PlayerJid;
	public boolean IsTargetFinal;
	public int TargetId;
	public boolean TargetReached;
	
	public RoundStatusInfo() {}
	
	public RoundStatusInfo(String playerJid, int targetId, 
			boolean isTargetFinal, boolean targetReached) {
		this.PlayerJid = playerJid;
		this.TargetId = targetId;
		this.IsTargetFinal = isTargetFinal;
		this.TargetReached = targetReached;
	}

}
