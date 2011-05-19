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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.muc.MultiUserChat;

import de.tudresden.inf.rn.mobilis.server.services.xhunt.model.Ticket;
import de.tudresden.inf.rn.mobilis.server.services.xhunt.model.XHuntPlayer;
import de.tudresden.inf.rn.mobilis.server.services.xhunt.state.GameState;
import de.tudresden.inf.rn.mobilis.server.services.xhunt.state.GameStateUninitialized;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.SnapshotBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.model.PlayerInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.model.PlayerSnapshotInfo;

/**
 * The game class represents a whole game. 
 * It organizes locations, targets, players und the state of the game.
 * @author elmar, Daniel Esser
 *
 */

public class Game {
	
	//Components
	private RouteManagement mRoutemanagement;
	private MultiUserChat muc;
	private XHunt control;
	private boolean gameIsOpen;
	
	/** The game players. (jid, xhuntplayer) */
	private HashMap<String, XHuntPlayer> gamePlayers;	
	
	/** id, ticket */
	private HashMap<Integer, Ticket> mTickets;
	
	private GameState state;
    
	private int round;

	/**Initilizes the Game Component. All private members are initialized. The Statemachine gets
	 * the initial state and the XML with stations and routes is parsed.
	 * @param control XHuntController, who administrates the whole life cycle
	 * @param gameId Number of the game. Used for multiple Games per Server.
	 */
	public Game(XHunt control) throws XMPPException, FileNotFoundException, XMLStreamException, FactoryConfigurationError, Exception{
		this.control = control;
		
		this.round = 0;
		this.gameIsOpen = false;
		
		gamePlayers = new HashMap<String, XHuntPlayer>();
		mTickets = new HashMap<Integer, Ticket>();
		mRoutemanagement = new RouteManagement(control);
        
        openMultiUserChat();
		
        System.out.println("Game created (PlayerMin: " + control.getSettings().getMinPlayers() + ", PlayerMax: " + control.getSettings().getMaxPlayers() +")");
        
        //set status
		setGameState(new GameStateUninitialized(control, this));
				
	}
	
	public void addTicket(Ticket ticket){
		this.mTickets.put(ticket.getId(), ticket);
	}
	
	public boolean isRoundStart(String playerJid){
		boolean isRoundStart = false;
		
		XHuntPlayer player = getPlayerByJid(playerJid);
		//check round start conditions
		if(player.getLastStationId() > 0
				&& player.getCurrentTargetId() < 1
				&& !player.getReachedTarget())
			isRoundStart = true;

		return isRoundStart;
	}
	
	public SnapshotBean createSnapshotBean(String playerJid){
		SnapshotBean bean = new SnapshotBean();
		XHuntPlayer toPlayer = getPlayerByJid(playerJid);
		
		if(toPlayer != null){
			bean.GameName = control.getSettings().getGameName();
			bean.Round = round;
			bean.IsRoundStart = isRoundStart(playerJid);
			bean.ShowMrX = showMisterX();
			bean.StartTimer = control.getSettings().getStartTimer();
			bean.Tickets = toPlayer.getTicketsAmount();
			
			ArrayList<PlayerSnapshotInfo> playersSnapshot = new ArrayList<PlayerSnapshotInfo>();
			for(XHuntPlayer player : getAgents()){
				playersSnapshot.add(new PlayerSnapshotInfo(player.getJid(),
						player.getName(), player.isModerator(), player.isMrx(),
						player.isReady(), player.getGeoLocation().getLatitudeE6(),
						player.getGeoLocation().getLongitudeE6(),
						player.getCurrentTargetId(),
						player.isCurrentTargetFinal(),
						player.getReachedTarget(),
						player.getLastStationId()));
			}
			
			XHuntPlayer mrxPlayer = getMisterX();
			if(mrxPlayer != null){
				PlayerSnapshotInfo mrxSnapshotInfo = null;
				if(toPlayer.isMrx())
					mrxSnapshotInfo = new PlayerSnapshotInfo(mrxPlayer.getJid(),
							mrxPlayer.getName(), mrxPlayer.isModerator(), mrxPlayer.isMrx(),
							mrxPlayer.isReady(), mrxPlayer.getGeoLocation().getLatitudeE6(),
							mrxPlayer.getGeoLocation().getLongitudeE6(),
							mrxPlayer.getCurrentTargetId(),
							mrxPlayer.isCurrentTargetFinal(),
							mrxPlayer.getReachedTarget(),
							mrxPlayer.getLastStationId());
				else
					mrxSnapshotInfo = new PlayerSnapshotInfo(mrxPlayer.getJid(),
							mrxPlayer.getName(), mrxPlayer.isModerator(), mrxPlayer.isMrx(),
							mrxPlayer.isReady(), -1,
							-1,
							mrxPlayer.getCurrentTargetId(),
							mrxPlayer.isCurrentTargetFinal(),
							mrxPlayer.getReachedTarget(),
							mrxPlayer.getLastStationId());
				playersSnapshot.add(mrxSnapshotInfo);
			}
			
			bean.PlayersSnapshot = playersSnapshot;
		}
		
		return bean;
	}

	public HashMap<Integer, Ticket> getAllTickets(){
		return mTickets;
	}

	/**
	 * @return The list of players.
	 */
	public HashMap<String, XHuntPlayer> getPlayers() {
		return gamePlayers;
	}
		
	public boolean isGameOpen() {
		return gameIsOpen;
	}


	public void setGameIsOpen(boolean gameIsOpen) {
		this.gameIsOpen = gameIsOpen;
	}

	/**
	 * @return The player that is MisterX.
	 */
	public XHuntPlayer getMisterX() {
		for(XHuntPlayer player : gamePlayers.values()){
			if(player.isMrx())
				return player;
		}
		
		return null;
	}
		
	/**
	 * @return List of the agents.
	 */
	public ArrayList<XHuntPlayer> getAgents(){
		ArrayList<XHuntPlayer> agents = new ArrayList<XHuntPlayer>();

		for(XHuntPlayer player : gamePlayers.values()){
			if(!player.isMrx())
				agents.add(player);
		}
		
		return agents;
	}
	
	/**
	 * @return List of the agents.
	 */
	public Set<String> getAgentsJids(){
		Set<String> jids = new HashSet<String>();

		for(XHuntPlayer player : gamePlayers.values()){
			if(!player.isMrx())
				jids.add(player.getJid());
		}
		
		return jids;
	}
	
	/**
	 * @return The player that is MisterX.
	 */
	public XHuntPlayer getModerator() {
		for(XHuntPlayer player : gamePlayers.values()){
			if(player.isModerator())
				return player;
		}
		
		return null;
	}
	
	public ArrayList<PlayerInfo> getPlayerInfos(){
		ArrayList<PlayerInfo> playerInfos = new ArrayList<PlayerInfo>();
		
		for(XHuntPlayer player : gamePlayers.values()){
			playerInfos.add(new PlayerInfo(
					player.getJid(),
					player.getName(),
					player.isModerator(),
					player.isMrx(),
					player.isReady())
			);
		}
		
		return playerInfos;
	}

	/**Returns the player corresponding to the JabberID that is used. Necessary for indirect packages (XHuntLocation).
	 * @param jid The JabberID of the player.
	 * @return Object XHuntPlayer that matches the JabberID.
	 */
	public XHuntPlayer getPlayerByJid(String jid)
	{
		return gamePlayers.get(jid);
	}
	
	public HashMap<String, ArrayList<Integer>> getUsedTickets(){
		HashMap<String, ArrayList<Integer>> usedTickets =
			new HashMap<String, ArrayList<Integer>>();
		
		for(XHuntPlayer player : gamePlayers.values()){
			usedTickets.put(player.getJid(), player.getUsedTickets());
		}
		
		return usedTickets;
	}
	
	/**
	 * Adds a new player and updates all player lists
	 * @param p
	 */
	public void addPlayer(XHuntPlayer p){
		this.gamePlayers.put(p.getJid(), p);
	}
	
	/**Removes the players and updates all player lists. Kicks him out of the chat. 
	 * @param p Player object that should be removed.
	 */
	public void removePlayer(XHuntPlayer p){
		this.gamePlayers.remove(p.getJid());
		
		kickPlayerFromChat(p.getJid());
	}

	public void removePlayerByJid(String jid){
		this.gamePlayers.remove(jid);
		
		kickPlayerFromChat(jid);
	}
	
	public void clearModeratorStatus(){
		for(XHuntPlayer player : gamePlayers.values()){
			player.setModerator(false);
		}
	}
	
	public void clearMrXStatus(){
		for(XHuntPlayer player : gamePlayers.values()){
			player.setMrx(false);
		}
	}
	
	/**Deletes all the targets of the players and saves them in oldTargetLocations.
	 * 
	 */
	public void clearAgentTargets(){
		for(XHuntPlayer player : gamePlayers.values()){
			if(!player.isMrx()){
				player.setCurrentTargetToLastStation();
				player.setReachedTarget(false);
			}
		}
	}
	
	/**Deletes the target of MisterX and saves it in oldTargetLocations.
	 * 
	 */
	public void clearMisterXTarget(){	
		for(XHuntPlayer player : gamePlayers.values()){
			if(player.isMrx()){
				player.setCurrentTargetToLastStation();
				player.setReachedTarget(false);
				break;
			}
		}
	}	
	
	/**Set the current game state to a new one.
	 * @param newState The next state for the state machine.
	 */
	public void setGameState(GameState newState)
	{
		state = newState;
	}
	
	
	
	public GameState getGameState() {
		return state;
	}

	/**Forwards the IQ packet to the state machine.
	 * @param iq
	 */
	public void processPacket(XMPPBean bean)
	{
		state.processPacket(bean);
	}
	
	/**Computes the distance between the current and the target location.
	 * @param p Player object.
	 * @return True, if distance is smaller than 50 meters, else false.
	 */
	public boolean isPlayerAtTarget(XHuntPlayer p) {
		if(p.getGeoLocation() != null && p.getCurrentTargetId() > 0){
			return (mRoutemanagement.computeDistance(p.getGeoLocation(),
						mRoutemanagement.getStation(p.getCurrentTargetId()).getGeoPoint())
					< control.getSettings().getDistanceTargetReached());
		} 
		
		return false;
	}
	
	/**Computes if all players have already reached their target. Uses isPlayerAtTarget().
	 * @return True, if the distance of all players between them and their targets is smaller that 50 meters.
	 */
	public boolean areAllPlayersAtTarget() {
		boolean result = true;
		
		for(XHuntPlayer player : gamePlayers.values()){
			result = result && isPlayerAtTarget(player);
		}
		
		return result;
	}
	
	public boolean areAllPlayersReady(){
		boolean result = true;
		
		for(XHuntPlayer player : gamePlayers.values()){
			result = result && player.isReady();
		}
		
		return result;
	}
	
	
	/**If we have a special round, show MisterX.
	 * @return True, if MisterX should be shown, else false.
	 */
	public boolean showMisterX() {
		if((round % 3) == 0 && round > 0){
			return true;
		}		
		return false;
	}

	/**
	 * @return RouteManagement object. Responsible for stations and routes.
	 */
	public RouteManagement getRouteManagement() {
		return mRoutemanagement;
	}

	
	
	//MultiUserChat	
	/**Opens the MultiUserChat with initialized members.
	 * 
	 */
	public void openMultiUserChat() throws XMPPException{
		
		muc = control.getConnection().createMultiUserChat(control.getSettings().getChatID());

		muc.create("Server");
		
		Form oldForm = muc.getConfigurationForm();
		Form newForm = oldForm.createAnswerForm();
		
		for (Iterator<FormField> fields = oldForm.getFields(); fields.hasNext();) {
		    FormField field = (FormField) fields.next();
		    if (!FormField.TYPE_HIDDEN.equals(field.getType()) && field.getVariable() != null) {
		    	newForm.setDefaultAnswer(field.getVariable());
		    }
		}
		
		newForm.setAnswer("muc#roomconfig_passwordprotectedroom", true);
		newForm.setAnswer("muc#roomconfig_roomsecret", control.getSettings().getChatPW());
		
		muc.sendConfigurationForm(newForm);
		
		System.out.println("Chat created (ID: " + control.getSettings().getChatID() + ", Pw: " + control.getSettings().getChatPW() +")");

	}
	
	/**Kicks a player from the chat.
	 * @param jid The JabberID of the player.
	 */
	public void kickPlayerFromChat(String jid){
		
		try {
			muc.kickParticipant(jid, "No reason");
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		
		
	}

	/**Closes the MultiUserChat.
	 * 
	 */
	public void closeMultiUserChat() throws XMPPException{
		
		if(muc.isJoined()){
			muc.destroy("", "");
		}		
	}


	public int getRound() {
		return round;
	}


	public void setRound(int round) {
		this.round = round;
	}

	public boolean setInitialTarget(XHuntPlayer player){
		if(player != null 
				&& player.getGeoLocation() != null){			
			control.log("initial target for " + player.getJid() + " is " + mRoutemanagement.getNearestStation(player.getGeoLocation()).getId());
			player.setCurrentTarget(mRoutemanagement.getNearestStation(player.getGeoLocation()).getId());
			player.setCurrentTargetFinal(true);
			
			return true;
		}
		else
			System.err.println("Cannot set intial target, geoloc of player " + player.getJid() + " is null!");
		
		return false;
	}

	public boolean isMrXAtSamePositionLikeAgent(){
		XHuntPlayer playerMrX = getMisterX();
		boolean isSame = false;
		
		for(XHuntPlayer player : getAgents()){
			if(player.getCurrentTargetId() == playerMrX.getCurrentTargetId()
					&& player.getCurrentTargetId() > 0
					&& mRoutemanagement.computeDistance(player.getGeoLocation()
							, playerMrX.getGeoLocation()) 
						< control.getSettings().getDistanceTargetReached())
				return true;
		}
		
		return isSame;
	}

}
