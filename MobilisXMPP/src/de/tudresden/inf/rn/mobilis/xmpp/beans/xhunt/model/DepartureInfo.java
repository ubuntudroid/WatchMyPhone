package de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.model;

public class DepartureInfo {
	
	public int VehicleId = -1;
	public String VehicleName;
	public String Direction;
	public String TimeLeft;
	
	public DepartureInfo() {}
	
	public DepartureInfo(int vehicleId, String vehicleName, String direction, String timeLeft) {
		this.VehicleId = vehicleId;
		this.VehicleName = vehicleName;
		this.Direction = direction;
		this.TimeLeft = timeLeft;
	}

}
