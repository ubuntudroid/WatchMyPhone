package de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.model;

public class OpenGameInfo {
	
	public String GameJid;
	public String GameName;
	public boolean PasswordRequired = false;
	public int WatingPlayers = -1;
	
	public OpenGameInfo() {}

	public OpenGameInfo(String gameId, String gameName, boolean passwordRequired,
			int watingPlayers) {
		this.GameJid = gameId;
		this.GameName = gameName;
		this.PasswordRequired = passwordRequired;
		this.WatingPlayers = watingPlayers;
	}
}
