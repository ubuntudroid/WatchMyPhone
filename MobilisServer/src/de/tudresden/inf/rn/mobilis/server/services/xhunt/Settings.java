/*******************************************************************************
 * Copyright (C) 2010 Technische Universit�t Dresden
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

import java.util.HashMap;
import java.util.Map;

import de.tudresden.inf.rn.mobilis.server.agents.MobilisAgent;

public class Settings {
	
	private int maxPlayers;
	private int minPlayers;
	
	private int areaId;
	private int rounds;
	
	private String chatID;
	private String chatPW;
	
	private String serviceRessource;
	private String gameName;
	private String gamePassword;
	private int startTimer; // in milli
	
	private HashMap<Integer, Integer> ticketsMrX;
	private HashMap<Integer, Integer> ticketsAgents;
	
	private int locationPollingIntervallMillis = 15000;
	
	public static final int TICKET_ID_SUGGESTION = -1;
	public static final int TICKET_ID_UNMOVABLE = 0;
	
	private double distanceTargetReached = 0.1; //in km
	
	private String resXhuntFolderPath = "res/xhunt/";
	
	/**
	 * Contructor
	 * Initializes the settings with predefined values
	 */
	public Settings(MobilisAgent agent){
		serviceRessource = agent.getResource();
		
		initDefaultValues(agent.getIdent());
	}
	
	private void initDefaultValues(String serverIdent){
		gameName = serverIdent;
		startTimer = 15 * 60 * 1000;
		
		this.areaId = -1;
		rounds = 10;
		
		maxPlayers = 6;
		minPlayers = 1;
		
		chatID = serviceRessource + "@conference." + serverIdent;
		chatPW = "tnuhx";
		
		ticketsMrX = new HashMap<Integer, Integer>();
		ticketsAgents = new HashMap<Integer, Integer>();
	}
	
	

	public double getDistanceTargetReached() {
		return distanceTargetReached;
	}

	public void setDistanceTargetReached(double distanceTargetReached) {
		this.distanceTargetReached = distanceTargetReached;
	}

	public int getLocationPollingIntervallMillis() {
		return locationPollingIntervallMillis;
	}

	public void setLocationPollingIntervallMillis(int locationPollingIntervallMillis) {
		this.locationPollingIntervallMillis = locationPollingIntervallMillis;
	}

	public HashMap<Integer, Integer> getTicketsMrX() {
		HashMap<Integer, Integer> tickets = new HashMap<Integer, Integer>();
		
		for(Map.Entry<Integer, Integer> entry : ticketsMrX.entrySet()){
			tickets.put(entry.getKey(), entry.getValue());
		}
		
		return tickets;
	}

	public void setTicketsMrX(HashMap<Integer, Integer> ticketsMrX) {
		this.ticketsMrX = ticketsMrX;
	}

	public HashMap<Integer, Integer> getTicketsAgents() {
		HashMap<Integer, Integer> tickets = new HashMap<Integer, Integer>();
		
		for(Map.Entry<Integer, Integer> entry : ticketsAgents.entrySet()){
			tickets.put(entry.getKey(), entry.getValue());
		}
		
		return tickets;
	}

	public void setTicketsAgents(HashMap<Integer, Integer> ticketsAgents) {
		this.ticketsAgents = ticketsAgents;
	}

	public String getResXhuntFolderPath() {
		return resXhuntFolderPath;
	}

	public String getGamePassword() {
		return gamePassword;
	}

	public void setGamePassword(String gamePassword) {
		this.gamePassword = gamePassword;
	}

	public int getAreaId() {
		return areaId;
	}

	public void setAreaId(int areaId) {
		this.areaId = areaId;
	}

	public String getGameName() {
		return gameName;
	}

	public void setGameName(String gameName) {
		this.gameName = gameName;
	}

	public int getStartTimer() {
		return startTimer;
	}

	public void setStartTimer(int startTimer) {
		this.startTimer = startTimer;
	}

	public String getServiceRessource() {
		return serviceRessource;
	}

	public void setMaxPlayers(int maxPlayer) {
		this.maxPlayers = maxPlayer;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public void setMinPlayers(int minPlayer) {
		this.minPlayers = minPlayer;
	}

	public int getMinPlayers() {
		return minPlayers;
	}

	public void setChatID(String chatID) {
		this.chatID = chatID;
	}

	public String getChatID() {
		return chatID;
	}

	public void setChatPW(String chatPW) {
		this.chatPW = chatPW;
	}

	public String getChatPW() {
		return chatPW;
	}


	public void setRounds(int rounds) {
		this.rounds = rounds;
	}


	public int getRounds() {
		return rounds;
	}

}
