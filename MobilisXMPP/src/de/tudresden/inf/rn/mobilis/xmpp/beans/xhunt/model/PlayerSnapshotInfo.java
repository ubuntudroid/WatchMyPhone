package de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.model;

public class PlayerSnapshotInfo {
	
	public String Jid;
	public String PlayerName;
	public boolean IsModerator;
	public boolean IsMrX;
	public boolean IsReady;
	
	public int Latitude = -1;
	public int Longitude = -1;
	
	public boolean IsTargetFinal;
	public int TargetId;
	public boolean TargetReached;	
	public int LastStationId;
	
	public PlayerSnapshotInfo() {}
	
	public PlayerSnapshotInfo(String jid, String playerName, boolean isModerator,
			boolean isMrx, boolean isReady, int lat, int lon, int targetId, 
			boolean isTargetFinal, boolean targetReached, int lastStationid) {
		this.Jid = jid;
		this.PlayerName = playerName;
		this.IsModerator = isModerator;
		this.IsMrX = isMrx;
		this.IsReady = isReady;
		
		this.Latitude = lat;
		this.Longitude = lon;
		
		this.TargetId = targetId;
		this.IsTargetFinal = isTargetFinal;
		this.TargetReached = targetReached;
		this.LastStationId = lastStationid;
	}
	
	public PlayerSnapshotInfo clone(){
		return new PlayerSnapshotInfo(Jid, PlayerName, IsModerator,
				IsMrX, IsReady, Latitude, Longitude, TargetId, IsTargetFinal, TargetReached, LastStationId);
	}

}
