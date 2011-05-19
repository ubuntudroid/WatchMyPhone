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
package de.tudresden.inf.rn.mobilis.server.services.xhunt.state;

import java.io.File;
import java.util.ArrayList;

import de.tudresden.inf.rn.mobilis.server.services.xhunt.Game;
import de.tudresden.inf.rn.mobilis.server.services.xhunt.XHunt;
import de.tudresden.inf.rn.mobilis.server.services.xhunt.model.Ticket;
import de.tudresden.inf.rn.mobilis.server.services.xhunt.model.XHuntPlayer;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.GameDetailsBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.GameOverBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.JoinGameBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.PlayerExitBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.PlayersBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.UpdatePlayerBean;

/**Lobby state. Players can connect to the game and get the GameDataIQ and StatusGameIQ. Also sends UpdatePlayerIQ
 * if the players settings have changed.
 * @author elmar, Daniel Esser
 *
 */
class GameStateLobby extends GameState{
	
	private File mGameDataFile = null;
	
	public GameStateLobby(XHunt control, Game game){
		this.control = control;
		this.game = game;
		
		loadGameData();
		
		control.getConnection().startDelayedResultBeansTimer();
	}
	
	private void loadGameData(){
		game.getRouteManagement().setAreaId(control.getSettings().getAreaId());
		/*game.getRouteManagement().setAreaName();
		game.getRouteManagement().setAreaDescription();*/
		
		game.getRouteManagement().setAreaRoutes(
				control.getSqlHelper().queryAreaRoutesMap(control.getSettings().getAreaId()));
		game.getRouteManagement().setAreaStations(
				control.getSqlHelper().queryAreaStationsMap(control.getSettings().getAreaId()));
		game.getRouteManagement().setAreaTickets(
				control.getSqlHelper().queryAreaTicketsMap(control.getSettings().getAreaId()));
		
		mGameDataFile = control.getSqlHelper().exportAreaData(
				control.getSettings().getAreaId(), control.getSettings().getResXhuntFolderPath());
	}
	
	@Override
	public void processPacket(XMPPBean inBean) {
		
		if( inBean instanceof GameDetailsBean){
			handleGameDetailsBean((GameDetailsBean)inBean);
		}
		else if( inBean instanceof JoinGameBean){
			handleJoinGameBean((JoinGameBean) inBean);
		}
		else if(inBean instanceof PlayerExitBean){
			handlePlayerExitBean((PlayerExitBean) inBean);
		}
		else if(inBean instanceof UpdatePlayerBean){
			handleUpdatePlayerBean((UpdatePlayerBean) inBean);
		}
		else if(inBean instanceof PlayersBean){
			//just result
		}
		else if(inBean instanceof UpdatePlayerBean){
			handleUpdatePlayerBean((UpdatePlayerBean) inBean);
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
	
	private void handleJoinGameBean(JoinGameBean inBean){
		control.log("game is open: " + game.isGameOpen());
		if(game.isGameOpen()){			
			XHuntPlayer player;
			if(game.getPlayers().containsKey(inBean.getFrom())){
				player = game.getPlayerByJid(inBean.getFrom());
			}
			else if(game.getPlayers().size() == 0){
				player = new XHuntPlayer(inBean.getFrom(), inBean.PlayerName, true, true, false);
			}
			else{
				player = new XHuntPlayer(inBean.getFrom(), inBean.PlayerName, false, false, false);
			}
			
			game.addPlayer(player);
			
			if(game.getPlayers().size() == control.getSettings().getMaxPlayers())
				game.setGameIsOpen(false);
			
			ArrayList<String> incomingFileNames = new ArrayList<String>();
			if(mGameDataFile != null)
				incomingFileNames.add(mGameDataFile.getName());
			
			for(Ticket ticket : game.getRouteManagement().getAreaTickets().values()){
				incomingFileNames.add(ticket.getIcon());
			}
			
			control.getConnection().sendXMPPBeanResult(
					new JoinGameBean(control.getSettings().getChatID(),
							control.getSettings().getChatPW(),
							control.getSettings().getStartTimer(),
							incomingFileNames
					),
					inBean
			);
			
			sendPlayersBean("Player " + player.getName() + " has joined.");
			
			transmitGameData(player.getJid());
		}
		else{
			control.getConnection().sendXMPPBeanError(
					new JoinGameBean("cancel", "not-allowed", "Maximum of players reached."),
					inBean
			);
		}
	}
	
	private void handlePlayerExitBean(PlayerExitBean inBean){
		XHuntPlayer exitPlayer = game.getPlayerByJid(inBean.Jid);
		String updateInfo = null;
		
		if(exitPlayer != null){
			XHuntPlayer fromPlayer = game.getPlayerByJid(inBean.getFrom());
			
			//player leaves game or moderator kicks player
			if(exitPlayer.getJid().equals(inBean.getFrom())
					|| (fromPlayer != null && fromPlayer.isModerator())){
				
				game.removePlayer(exitPlayer);
				// default info
				updateInfo = "Player " + exitPlayer.getName() + " has left the game.";
				
				// Confirm exit player
				control.getConnection().sendXMPPBeanResult(
						inBean.clone(),
						inBean
				);
				
				// If moderator leaves game send GameOver
				if(exitPlayer.isModerator()){
					game.setGameIsOpen(false);
					
					// Switch to GameOver
					game.setGameState(new GameStateGameOver(control, game));
					control.log("Status changed to GameStateGameOver");
					
					control.getConnection().sendXMPPBean(
							new GameOverBean("Moderator has left!"),
							game.getPlayers().keySet(),
							XMPPBean.TYPE_SET
					);
				}
				else{
					// if exit player is mr.x, moderator got mr.x
					if(exitPlayer.isMrx()){
						XHuntPlayer moderator = fromPlayer.isModerator()
							? fromPlayer
							: game.getModerator();
						
						if(moderator != null){
							moderator.setMrx(true);
							moderator.setReady(false);
							
							updateInfo = "Mr.X(" + exitPlayer.getName() +") has left the game. Moderator("
								+ moderator.getName() + ") is now Mr.X.";
						}
						else{
							// Switch to GameOver
							game.setGameState(new GameStateGameOver(control, game));
							control.log("Status changed to GameStateGameOver");
							
							control.getConnection().sendXMPPBean(
									new GameOverBean("No Moderator available!"),
									game.getPlayers().keySet(),
									XMPPBean.TYPE_SET);
						}
					}
					
					// Player was kicked by moderator
					if(fromPlayer.isModerator()){
						control.getConnection().sendXMPPBean(
								new PlayerExitBean(exitPlayer.getJid()),
								exitPlayer.getJid(),
								XMPPBean.TYPE_SET
						);
						updateInfo = "Player " + exitPlayer.getName() + " was kicked by Moderator.";
					}
					
					// Notify rest of players about the exit player
					sendPlayersBean(updateInfo);
					
					// check if rest of players are ready to play and minimum of players is reached
					if(game.areAllPlayersReady()
							&& game.getPlayers().size() >= control.getSettings().getMinPlayers()) {
						game.setGameState(new GameStateRoundInitial(control, game));
						control.log("Status changed to GameStateRoundInitial");
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
		else if(inBean.getType() == XMPPBean.TYPE_RESULT){
			// answer from kicked player			
		}
		else{
			control.getConnection().sendXMPPBeanError(
					new PlayerExitBean("modify", "not-acceptable", "Player not found."),
					inBean
			);
		}
	}
	
	private void handleUpdatePlayerBean(UpdatePlayerBean inBean){
		XHuntPlayer fromPlayer = game.getPlayerByJid(inBean.getFrom());
		UpdatePlayerBean resultBean = new UpdatePlayerBean();
		boolean updatePlayers = false;
		String updateInfo = null;
		
		if(fromPlayer != null){
			// Player likes to change his own properties
			if(fromPlayer.getJid().equals(inBean.PlayerInfo.Jid)
					&& !fromPlayer.isModerator()){
				//If player is not moderator, he can only change his ready status
				if(inBean.PlayerInfo.IsReady){
					fromPlayer.setReady(true);
					
					updatePlayers = true;
					updateInfo = "Player " + fromPlayer.getName() 
						+ " has changed status ready to " + fromPlayer.isReady();
				}
				else {
					resultBean = 
						new UpdatePlayerBean("modify", "not-acceptable", "You can only change your ready status.");
				}
			}
			else if(fromPlayer.isModerator()){
				// If player is moderator, he can change everything
				XHuntPlayer updatePlayer = game.getPlayerByJid(inBean.PlayerInfo.Jid);
				
				if(updatePlayer != null){
					boolean statusChanged = false;
					
					updatePlayer.setReady(inBean.PlayerInfo.IsReady);
					updateInfo = "Player " + updatePlayer.getName() 
						+ " has changed status ready to " + updatePlayer.isReady();
					
					if(inBean.PlayerInfo.IsModerator
							&& !updatePlayer.isModerator()){
						
						game.clearModeratorStatus();
						updatePlayer.setModerator(true);
						
						statusChanged = true;
						updateInfo = "Player " + updatePlayer.getName() 
							+ " is now Moderator";
					}
					
					if(inBean.PlayerInfo.IsMrX
							&& !updatePlayer.isMrx()){
						
						game.clearMrXStatus();
						updatePlayer.setMrx(true);
						
						statusChanged = true;
						updateInfo = "Player " + updatePlayer.getName() 
							+ " is now Mr.X";
					}
					
					if(statusChanged){
						updatePlayer.setReady(false);
						fromPlayer.setReady(false);
						
						updatePlayers = true;
					}
					else if(inBean.PlayerInfo.IsReady)
						updatePlayer.setReady(inBean.PlayerInfo.IsReady);
						updatePlayers = true;
				}
				else{
					resultBean = new UpdatePlayerBean("modify", "not-acceptable", "Player not found.");
				}
			}
			else{
				resultBean = new UpdatePlayerBean("auth", "forbidden", "You don't have moderator status.");
			}
			
			if(resultBean.errorType != null){
				control.getConnection().sendXMPPBeanError(resultBean, inBean);
			}
			else{
				String info = null;
				
				//check if all players are ready, minimum of players and if there is a mrx available
				if(game.areAllPlayersReady()){
					if(game.getPlayers().size() >= control.getSettings().getMinPlayers()){
						if(game.getMisterX() != null){
							info = "All players are ready!";
							
							game.setGameState(new GameStateRoundInitial(control, game));
							control.log("Status changed to GameStateRoundInitial");
						}
						else{
							info = "Please set Mr.X again.";
						}
					}
					else{
						info = "Current count of players is "
							+ game.getPlayers().size()
							+ " but this game requires a minimum of "
							+ control.getSettings().getMinPlayers()
							+ " to start.";
					}
				}
				else{
					if(fromPlayer.isReady())
						info = "There are still unready players. Please Wait!";
				}
				
				if(info == null)
					info = "Nothing has changed!";
				
				control.getConnection().sendXMPPBeanResult(new UpdatePlayerBean(info), inBean);
				if(updatePlayers)
					sendPlayersBean(updateInfo);
			}
		}
		else{
			control.getConnection().sendXMPPBeanError(
					new UpdatePlayerBean("auth", "forbidden", "You're not a player of this game."),
					inBean
			);
		}
	}
	
	//TODO: Failure handling if a file doesn't exists
	private void transmitGameData(String playerJid){		
		// Transmit route, station and ticket information via file transfer
		if(mGameDataFile != null)
			control.getConnection().transmitFile(
					new File(mGameDataFile.getAbsolutePath()),
					"gameDataIQ.Namespace",
					playerJid);
		
		// Transmit ticket icons
		for(Ticket ticket : game.getRouteManagement().getAreaTickets().values()){
			File iconFile = new File(control.getSettings().getResXhuntFolderPath() + ticket.getIcon());
			
			if(iconFile.exists())
				control.getConnection().transmitFile(
						new File(iconFile.getAbsolutePath()),
						"icon",
						playerJid);
		}
	}
}
