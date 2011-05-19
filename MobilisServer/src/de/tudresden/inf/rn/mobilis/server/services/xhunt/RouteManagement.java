/*******************************************************************************
 * Copyright (C) 2010 Technische Universität Dresden
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: http://mobilisplatform.sourceforge.net
 ******************************************************************************/
package de.tudresden.inf.rn.mobilis.server.services.xhunt;

import java.util.ArrayList;
import java.util.HashMap;

import de.tudresden.inf.rn.mobilis.server.services.xhunt.model.GeoPoint;
import de.tudresden.inf.rn.mobilis.server.services.xhunt.model.Route;
import de.tudresden.inf.rn.mobilis.server.services.xhunt.model.Station;
import de.tudresden.inf.rn.mobilis.server.services.xhunt.model.Ticket;
import de.tudresden.inf.rn.mobilis.server.services.xhunt.model.XHuntPlayer;

/**
 * The Class RouteManagement.
 */
public class RouteManagement {
	
	public static final String TAG = "RouteManagement";
	
	private int mAreaId;
	private String mAreaName;
	private String mAreaDescription;
	
	/** All routes for this game. */
	private HashMap<Integer, Route> mAreaRoutes;
	
	/** The stations. */
	private HashMap<Integer, Station> mAreaStations;
	
	private HashMap<Integer, Ticket> mAreaTickets;
	
	private XHunt mController;
	
	/**
	 * Instantiates a new route management.
	 */
	public RouteManagement(XHunt controller) {
		mController = controller;
		mAreaRoutes = new HashMap<Integer, Route>();
		mAreaStations = new HashMap<Integer, Station>();
		mAreaTickets = new HashMap<Integer, Ticket>();
	}
	
	/**
	 * Compute distance between 2 geopoints.
	 * 
	 * @param location1 the first location
	 * @param location2 the second location
	 * 
	 * @return the distance
	 */
	public double computeDistance(GeoPoint loc1, GeoPoint loc2)	{
		double x = 71.5 *  (loc1.getLongitudeE6() / 1E6 - loc2.getLongitudeE6() / 1E6);
		double y = 111.3* (loc1.getLatitudeE6() / 1E6 - loc2.getLatitudeE6() / 1E6);
		
		return Math.sqrt(x * x + y * y);
	}
	
	
	public String getAreaDescription() {
		return mAreaDescription;
	}
	
	public int getAreaId() {
		return mAreaId;
	}
	
	public String getAreaName() {
		return mAreaName;
	}
	
	public HashMap<Integer, Ticket> getAreaTickets() {
		return mAreaTickets;
	}
	
	public Station getNearestStation(GeoPoint geoPoint){
		Station nearestStation = null;
		double minDistance = Double.MAX_VALUE;
		double computedDistance = Double.MAX_VALUE;
		
		for(Station station : mAreaStations.values()){
			computedDistance = computeDistance(geoPoint, station.getGeoPoint());
			
			if(computedDistance < minDistance){
				minDistance = computedDistance;
				nearestStation = station;
			}
		}
		
		return nearestStation;
	}
	
	/**
	 * Gets the routes.
	 * 
	 * @return all routes
	 */
	public HashMap<Integer, Route> getRoutes() {
		return mAreaRoutes;
	}
	
	/**
	 * Gets the routes for station.
	 * 
	 * @param station the station
	 * 
	 * @return the routes for station
	 */
	public ArrayList<Route> getRoutesForStation(int stationId){
		ArrayList<Route> result = new ArrayList<Route>();
		
		for(Route route : mAreaRoutes.values()){
			if(route.containsStation(stationId))
				result.add(route);
		}

		return result;
	}
	
	/**
	 * Gets the routes for station.
	 * 
	 * @param station the station
	 * 
	 * @return the routes for station
	 */
	public ArrayList<Route> getRoutesForStation(Station station){
		return getRoutesForStation(station.getId());
	}
	
	public Station getStation(int stationId){
		return mAreaStations.get(stationId);
	}
	
	public HashMap<Integer, Station> getStations(){
		return this.mAreaStations;
	}
	
	
	public ArrayList<Station> getStationsAsList(){
		return new ArrayList<Station>(mAreaStations.values());
	}
	
	public boolean isPlayerUnmovable(XHuntPlayer player){		
		boolean unmoveable = true;
		
		for(Route route : getRoutesForStation(player.getLastStationId())){
			if(player.getTicketsAmount().get(route.getTicketId()) > 0
					|| mAreaTickets.get(route.getTicketId()).isSuperior()){
				unmoveable = false;
				break;
			}
		}
	 	
	 	return unmoveable;
	}
	
	/**Test if the target can be reached by the player, depends on the tickets of the player
	 * @param tariq IQ Packet contains the target.
	 * @param p Player object.
	 * @return True, if the target is reachable by the player, else false.
	 */
	public boolean isTargetReachable(int stationId, XHuntPlayer player){
		mController.log("current station: " + player.getLastStationId());
		for(Route route : getRoutesForStation(stationId)){mController.log("route: " + route.getId() + " target: " + stationId);
			if(route.containsStation(player.getLastStationId())){
				mController.log("route: " + route.getId() + " contains target " + " nextStations: " + route.getNextStationIds(stationId).toString());
				for(int nextStationId : route.getNextStationIds(stationId)){mController.log("nextStation: " + nextStationId);
					if(nextStationId == player.getLastStationId())
						return true;
				}
			}
		}
		
		return false;
	}

	public void setAreaDescription(String areaDescription) {
		this.mAreaDescription = areaDescription;
	}
	
	public void setAreaId(int areaId) {
		this.mAreaId = areaId;
	}

	public void setAreaName(String areaName) {
		this.mAreaName = areaName;
	}
	
	public void setAreaRoutes(HashMap<Integer, Route> mAreaRoutes) {
		this.mAreaRoutes = mAreaRoutes;
	}

	public void setAreaStations(HashMap<Integer, Station> mAreaStations) {
		this.mAreaStations = mAreaStations;
	}

	public void setAreaTickets(HashMap<Integer, Ticket> areaTickets) {
		this.mAreaTickets = areaTickets;
	}

}
