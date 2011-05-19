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
package de.tudresden.inf.rn.mobilis.server.services.xhunt.state;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import de.tudresden.inf.rn.mobilis.server.services.xhunt.Game;
import de.tudresden.inf.rn.mobilis.server.services.xhunt.XHunt;
import de.tudresden.inf.rn.mobilis.server.services.xhunt.model.XHuntPlayer;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.CancelStartTimerBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.DepartureDataBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.GameDetailsBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.GameOverBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.LocationBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.PlayerExitBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.PlayersBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.RoundStatusBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.StartRoundBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.model.LocationInfo;

/**First round of the game. The start positions for the players are send. If all players are at the start positions, the state changes to
 * GameStateRoundMrX.
 * @author elmar, Daniel Esser
 *
 */
public class GameStateRoundInitial extends GameState{
	private boolean mIsFirstLocationRequest = true;
	
	private Timer mPollingTimer;
	private Timer mStartTimerMrX;
	private Timer mStartTimerAgents;
	
	private boolean mIsStartTimerMrxRunning;
	private boolean mIsStartTimerAgentsRunning;

	public GameStateRoundInitial(XHunt control, Game game){
		this.control = control;
		this.game = game;		
		
		startInitialRound();
	}
	
	@Override
	public void processPacket(XMPPBean inBean) {
		
		if(inBean instanceof CancelStartTimerBean){
			handleCancelStartTimerBean((CancelStartTimerBean)inBean);
		}
		else if( inBean instanceof GameDetailsBean){
			handleGameDetailsBean((GameDetailsBean)inBean);
		}
		else if( inBean instanceof DepartureDataBean){
			//TODO: handle departure
			control.getConnection().sendXMPPBeanResult(
					new DepartureDataBean(),
					inBean
			);
		}
		else if( inBean instanceof LocationBean){
			handleLocationBean((LocationBean) inBean);
		}
		else if( inBean instanceof PlayerExitBean){
			handlePlayerExitBean((PlayerExitBean) inBean);
		}
		else if( inBean instanceof StartRoundBean){
			handleStartRoundBean((StartRoundBean) inBean);
		}
		else if( inBean instanceof PlayersBean ){
			//Just result
		}
		else if( inBean instanceof RoundStatusBean){
			//just result
		}
		else{
			inBean.errorType = "wait";
			inBean.errorCondition = "unexpected-request";
			inBean.errorText = "This request is not supportet at this game state";
			
			control.getConnection().sendXMPPBeanError(
					inBean,
					inBean
			);	
		}	
	}
	
	private void dismissStartTimerAgents(){
		for(XHuntPlayer player : game.getAgents())
			if(!game.setInitialTarget(player)){
				game.removePlayer(player);
				
				// If not enough players left to continue, notify players
				if(game.getPlayers().size() < control.getSettings().getMinPlayers()){
					setGameOver("Not enough players to carry on with this game!");
				}
				else{
					sendPlayersBean("Player " + player.getName() + " can not provide a geo location and was kicked.");
				}
			}
		
		sendRoundStatusBeanForAgents();
		mIsStartTimerAgentsRunning = false;
	}
	
	private void dismissStartTimerMrX(){
		if(!game.setInitialTarget(game.getMisterX())){
			setGameOver("Mr.X can not provide a geo location.");
		}
		else{			
			sendRoundStatusBeanForMrX();
		}
		
		mIsStartTimerMrxRunning = false;		
	}
	
	private void handleCancelStartTimerBean(CancelStartTimerBean inBean){
		if( mIsStartTimerAgentsRunning
				&& mStartTimerAgents != null ){
			mStartTimerAgents.cancel();
			dismissStartTimerAgents();
		}
		
		if( mIsStartTimerMrxRunning
				&& mStartTimerMrX != null ){
			mStartTimerMrX.cancel();
			dismissStartTimerMrX();
		}
		
		control.getConnection().sendXMPPBeanResult(
				new CancelStartTimerBean(),
				inBean
		);
	}
	
	private void handleLocationBean(LocationBean inBean){
		XHuntPlayer updatePlayer = null;
		
		control.log("Location received: player: " + inBean.getFrom() 
				+ " loc: " + inBean.Locations.get(0).Latitude + ";" + inBean.Locations.get(0).Longitude
				+ " time: " + System.currentTimeMillis());
		
		if(inBean.Locations.size() > 0){
			updatePlayer = game.getPlayerByJid(inBean.Locations.get(0).Jid);
			updatePlayer.setGeoLocation(inBean.Locations.get(0).Latitude, inBean.Locations.get(0).Longitude);
		}
		else{
			// no empty iq allowed
			return;
		}
		
		// in initial round we need a fix update of all players locations --> resovled by sending 2 locationbeans
		if(mIsFirstLocationRequest){
			boolean allLocationsAvailable = true;
			
			for(XHuntPlayer player : game.getPlayers().values()){
				allLocationsAvailable = allLocationsAvailable && ( player.getGeoLocation() != null);
			}
			
			if(allLocationsAvailable){
				mIsFirstLocationRequest = false;
				startLocationPolling();
			}
		}

		if(game.isPlayerAtTarget(updatePlayer)){
			updatePlayer.setReachedTarget(true);

			if(updatePlayer.isMrx())
				sendRoundStatusBeanForMrX();
			else
				sendRoundStatusBeanForAgents();
		}
		
		if(game.areAllPlayersAtTarget()){
			stopPollingLocations();
			game.setGameState(new GameStatePlay(control, game));
		}
	}
	
	private void handlePlayerExitBean(PlayerExitBean inBean){
		XHuntPlayer exitPlayer = game.getPlayerByJid(inBean.Jid);
		
		if(exitPlayer != null){
			//player can only leave the current game
			if(exitPlayer.getJid().equals(inBean.getFrom())){
				
				game.removePlayer(exitPlayer);
				
				// Confirm exit player
				control.getConnection().sendXMPPBeanResult(
						inBean.clone(),
						inBean
				);
				
				String gameOverReason = null;
				
				// check for game over conditions
				if(exitPlayer.isMrx()) gameOverReason = "Mr.X has left!";
				else if(game.getPlayers().size() < control.getSettings().getMinPlayers())
					gameOverReason = "Not enough players to carry on with this game!";
				
				// If game over happens notify players
				if(gameOverReason != null){
					setGameOver(gameOverReason);
				}
				else{					
					// Notify rest of players about the exit player
					sendPlayersBean("Player " + exitPlayer.getName() + " has left the game.");
					
					boolean allPlayersReachedTargets = true;
					for(XHuntPlayer player : game.getPlayers().values()){
						allPlayersReachedTargets = allPlayersReachedTargets && game.isPlayerAtTarget(player);
					}
					
					if(allPlayersReachedTargets) {
						stopPollingLocations();
						game.setGameState(new GameStatePlay(control, game));
					}
				}
			}
			else{
				control.getConnection().sendXMPPBeanError(
						new PlayerExitBean("modify", "not-acceptable", "This player can not be dropped (permission issues)."),
						inBean
				);
			}
		}
		else{
			control.getConnection().sendXMPPBeanError(
					new PlayerExitBean("modify", "not-acceptable", "Player not found."),
					inBean
			);
		}
	}
	
	private void handleStartRoundBean(StartRoundBean inBean){		
		control.getConnection().sendXMPPBean(
				new LocationBean(),
				game.getPlayers().keySet(),
				XMPPBean.TYPE_GET);
	}
	
	private void setGameOver(String reason){
		game.setGameIsOpen(false);
		
		if(mStartTimerAgents != null)
			mStartTimerAgents.cancel();
		
		if(mStartTimerMrX != null)
			mStartTimerMrX.cancel();
		
		stopPollingLocations();
		game.setGameState(new GameStateGameOver(control, game));
		
		control.getConnection().sendXMPPBean(
				new GameOverBean(reason),
				game.getPlayers().keySet(),
				XMPPBean.TYPE_SET
		);
	}
	
	private void startInitialRound(){
		// init and send tickets to mrx
		game.getMisterX().setTicketsAmount(control.getSettings().getTicketsMrX());
		control.getConnection().sendXMPPBean(
				new StartRoundBean(game.getRound(), true, control.getSettings().getTicketsMrX()),
				game.getMisterX().getJid(),
				XMPPBean.TYPE_SET);
		
		// init and send tickets to agents
		for(XHuntPlayer player : game.getAgents()){
			player.setTicketsAmount(control.getSettings().getTicketsAgents());
		}
		control.getConnection().sendXMPPBean(
				new StartRoundBean(game.getRound(), true, control.getSettings().getTicketsAgents()),
				game.getAgentsJids(),
				XMPPBean.TYPE_SET);
		
		mStartTimerMrX = new Timer();
		mStartTimerMrX.schedule(
			new TimerTask() {
				public void run() {
					dismissStartTimerMrX();
		        }
		}, control.getSettings().getStartTimer());
		mIsStartTimerMrxRunning = true;
    	
		mStartTimerAgents = new Timer();
		mStartTimerAgents.schedule(
			new TimerTask() {
				public void run() {
					dismissStartTimerAgents();
		        }
		}, control.getSettings().getStartTimer() / 2);
		mIsStartTimerAgentsRunning = true;
    	
	}
	
	private void startLocationPolling(){
		mPollingTimer = new Timer();
		mPollingTimer.schedule(
			new TimerTask() {
				public void run() {
					ArrayList<LocationInfo> infos = new ArrayList<LocationInfo>();
					XHuntPlayer playerMrX = null;
					
					for(XHuntPlayer player : game.getPlayers().values()){
						if(!player.isMrx()){
							infos.add(new LocationInfo(player.getJid(),
									player.getGeoLocation().getLatitudeE6(),
									player.getGeoLocation().getLongitudeE6()));
						}
						else{
							playerMrX = player;
						}
					}
					
					control.getConnection().sendXMPPBean(
							new LocationBean(infos), game.getAgentsJids(), XMPPBean.TYPE_SET);
					
					if(playerMrX != null){
						infos.add(new LocationInfo(playerMrX.getJid(),
								playerMrX.getGeoLocation().getLatitudeE6(),
								playerMrX.getGeoLocation().getLongitudeE6()));
						control.getConnection().sendXMPPBean(
								new LocationBean(infos), game.getMisterX().getJid(), XMPPBean.TYPE_SET);
					}
		        }
		}, 5000, control.getSettings().getLocationPollingIntervallMillis());
	}
	
	private void stopPollingLocations(){
		if(mPollingTimer != null)
			mPollingTimer.cancel();
	}
}
