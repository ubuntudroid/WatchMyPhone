package de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.model;


public class PlayerInfo {

	public String Jid;
	public String PlayerName;
	public boolean IsModerator;
	public boolean IsMrX;
	public boolean IsReady;
	
	public PlayerInfo() {}
	
	public PlayerInfo(String jid, String playerName, boolean isModerator,
			boolean isMrx, boolean isReady){
		this.Jid = jid;
		this.PlayerName = playerName;
		this.IsModerator = isModerator;
		this.IsMrX = isMrx;
		this.IsReady = isReady;
	}
	
	public String toString(){
		return "jid: " + Jid 
			+ " name: " + PlayerName
			+ " isMod: " + IsModerator
			+ " isMrX: " + IsMrX
			+ " isReady: " + IsReady;
	}
}
