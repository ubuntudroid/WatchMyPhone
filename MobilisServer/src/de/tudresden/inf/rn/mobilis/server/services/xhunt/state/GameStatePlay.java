package de.tudresden.inf.rn.mobilis.server.services.xhunt.state;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import de.tudresden.inf.rn.mobilis.server.services.xhunt.Game;
import de.tudresden.inf.rn.mobilis.server.services.xhunt.Settings;
import de.tudresden.inf.rn.mobilis.server.services.xhunt.XHunt;
import de.tudresden.inf.rn.mobilis.server.services.xhunt.model.XHuntPlayer;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.DepartureDataBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.GameDetailsBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.GameOverBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.LocationBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.PlayerExitBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.PlayersBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.RoundStatusBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.StartRoundBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.TargetBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.UsedTicketsBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.model.DepartureInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.model.LocationInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.model.RoundStatusInfo;

public class GameStatePlay extends GameState {
	
	private SubGameState mSubState;
	private boolean mWaitingFoarPlayersReachingTarget = false;
	private Timer mPollingTimer;
	
	public GameStatePlay(XHunt control, Game game){
		this.control = control;
		this.game = game;
		
		control.log("statePlay");
		mSubState = new GameStateRoundMrX();	
		startLocationPolling();
	}
	
	protected void changeGameStateTo(GameState state){	
		if(mPollingTimer != null)
			mPollingTimer.cancel();
		
		game.setGameState(state);
		control.log("Status changed to " + state.getClass().toString());
	}

	@Override
	public void processPacket(XMPPBean inBean) {
		if( inBean instanceof DepartureDataBean){
			handleDepartureDataBean((DepartureDataBean)inBean);
		}
		else if( inBean instanceof GameDetailsBean){
			handleGameDetailsBean((GameDetailsBean)inBean);
		}
		else  if( inBean instanceof LocationBean ){
			handleLocationBean((LocationBean) inBean);
		}
		else if( inBean instanceof RoundStatusBean){
			//just result
		}
		else if( inBean instanceof PlayerExitBean ){
			if(inBean.getType() != XMPPBean.TYPE_RESULT)
				handlePlayerExitBean((PlayerExitBean) inBean);
		}
		else if( inBean instanceof PlayersBean){
			//just result
		}
		else if( inBean instanceof StartRoundBean){
			//just result
		}
		else if( inBean instanceof UsedTicketsBean){
			handleUsedTicketsBean((UsedTicketsBean)inBean);
		}
		else
			mSubState.processPacket(inBean);		
	}
	
	private void handleDepartureDataBean(DepartureDataBean inBean){
		ArrayList<DepartureInfo> departures = new ArrayList<DepartureInfo>();
		departures.add(new DepartureInfo(1, "test", "test", "00"));
		
		control.getConnection().sendXMPPBeanResult(
				new DepartureDataBean(departures),
				inBean);
	}
	
	private void handleLocationBean(LocationBean inBean){
		XHuntPlayer updatePlayer = null;
		
		if(inBean.Locations.size() > 0){
			updatePlayer = game.getPlayerByJid(inBean.Locations.get(0).Jid);
			updatePlayer.setGeoLocation(inBean.Locations.get(0).Latitude, inBean.Locations.get(0).Longitude);
		}
		else{
			// no empty iq allowed
			return;
		}

		if(game.isPlayerAtTarget(updatePlayer)){
			updatePlayer.setReachedTarget(true);

			if(updatePlayer.isMrx())
				sendRoundStatusBeanForMrX();
			else
				sendRoundStatusBean();
		}
		else if(updatePlayer.getReachedTarget()){
			//If player diverge from target set target reached to false
			updatePlayer.setReachedTarget(false);
			sendRoundStatusBean();
		}
		
		if(game.isMrXAtSamePositionLikeAgent()){
			setGameOver("Mr.X was caught by agent " + game.getPlayerByJid(inBean.getFrom()).getName() + ".");			
			return;
		}
		
		if(game.areAllPlayersAtTarget()
				&& mWaitingFoarPlayersReachingTarget){
			mWaitingFoarPlayersReachingTarget = false;
			mSubState = new GameStateRoundMrX();
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
					sendRoundStatusBean();
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
	
	private void handleUsedTicketsBean(UsedTicketsBean inBean){
		control.getConnection().sendXMPPBeanResult(
				new UsedTicketsBean(game.getUsedTickets()),
				inBean);
	}
	
	private void setGameOver(String reason){
		game.setGameIsOpen(false);
		
		stopPollingLocations();
		game.setGameState(new GameStateGameOver(control, game));
		
		control.getConnection().sendXMPPBean(
				new GameOverBean(reason),
				game.getPlayers().keySet(),
				XMPPBean.TYPE_SET
		);
	}
	
	private void startLocationPolling(){		
		mPollingTimer = new Timer();
		mPollingTimer.schedule(
			new TimerTask() {
				public void run() {
					
					if(game.getPlayers().size() < control.getSettings().getMinPlayers()
							|| game.getMisterX() == null){						
						
						setGameOver("Game over. Not enough players or mrx. not available.");
						return;
					}
					
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
		}, 0, control.getSettings().getLocationPollingIntervallMillis());
	}
	
	private void stopPollingLocations(){
		if(mPollingTimer != null)
			mPollingTimer.cancel();
	}


	private class GameStateRoundMrX extends SubGameState {

		public GameStateRoundMrX(){
			control.log("SubGameState: GameStateRoundMrX");
			
			game.clearMisterXTarget();
			game.setRound(game.getRound() + 1);
			
			// maximum of game rounds reached --> mrx won
			if(game.getRound() > control.getSettings().getRounds()){
				setGameOver("End of game reached. Mr.X was not caught by the agents.");
			}
			else if(game.getRouteManagement().isPlayerUnmovable(game.getMisterX())){
				setGameOver("End of game reached. Mr.X is out of tickets.");
			}
			else{
				control.getConnection().sendXMPPBean(
						new StartRoundBean(game.getRound(), true, game.getMisterX().getTicketsAmount()),
						game.getMisterX().getJid(),
						XMPPBean.TYPE_SET);
			}
		}
		
		@Override
		public void processPacket(XMPPBean inBean) {
			
			if( inBean instanceof TargetBean ){
				handleTargetBean((TargetBean) inBean);
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
		
		private void handleTargetBean(TargetBean inBean){
			XHuntPlayer playerMrX = game.getPlayerByJid(inBean.getFrom());
			if(playerMrX != null && playerMrX.isMrx()){
				if(inBean.IsFinal){
					if(playerMrX.getTicketsAmount().get(inBean.TicketId) != null
							&& playerMrX.getTicketsAmount().get(inBean.TicketId) > 0){			
						control.log("TargetId: " + inBean.StationId);
						
						if(game.getRouteManagement().isTargetReachable(inBean.StationId, playerMrX)){
							if(inBean.Round == game.getRound()){
								playerMrX.setCurrentTargetToLastStation();
								playerMrX.setCurrentTarget(inBean.StationId);
								playerMrX.decreaseTicket(inBean.TicketId);
								
								control.getConnection().sendXMPPBeanResult(
										new TargetBean(inBean.TicketId),
										inBean
								);
								
								ArrayList<RoundStatusInfo> info = new ArrayList<RoundStatusInfo>();
								info.add(playerMrX.getRoundStatusInfo());
								
								control.getConnection().sendXMPPBean(
										new RoundStatusBean(game.getRound(), info), playerMrX.getJid(), XMPPBean.TYPE_SET);
								
								mSubState = new GameStateRoundAgents();
							}
							else{
								control.getConnection().sendXMPPBeanError(
										new TargetBean("cancel", "not-allowed", "Your not sync with the game server."),
										inBean
								);
								
								control.getConnection().handlePlayerNotReplies(playerMrX.getJid());
							}
						}
						else{
							control.getConnection().sendXMPPBeanError(
									new TargetBean("modify", "not-acceptable", "Target station is not reachable from current."),
									inBean
							);
						}
					}
					else{
						//if player is unmovable test if he knows (ticketId == 0)
						if(game.getRouteManagement().isPlayerUnmovable(playerMrX)){
							if(inBean.TicketId == Settings.TICKET_ID_UNMOVABLE){
								control.getConnection().sendXMPPBeanResult(
										new TargetBean(inBean.TicketId),
										inBean
								);
							}
							else{
								control.getConnection().sendXMPPBeanError(
										new TargetBean("modify", "not-acceptable", "You're unmovable at this position."),
										inBean
								);
							}
						}
						// everything else is not accepted
						else {
							control.getConnection().sendXMPPBeanError(
									new TargetBean("modify", "not-acceptable", "Not enough tickets available."),
									inBean
							);
						}
					}
				}
				else{
					control.getConnection().sendXMPPBeanError(
							new TargetBean("modify", "not-acceptable", "Target has to be final."),
							inBean
					);
				}
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
	}
	
	
	private class GameStateRoundAgents extends SubGameState{	

		public GameStateRoundAgents(){
			control.log("SubGameState: GameStateRoundAgents");
			
			game.clearAgentTargets();
			mWaitingFoarPlayersReachingTarget = true;
			
			for(XHuntPlayer playerAgent : game.getAgents()){
				control.getConnection().sendXMPPBean(
						new StartRoundBean(game.getRound(), game.showMisterX(), playerAgent.getTicketsAmount()),
						playerAgent.getJid(),
						XMPPBean.TYPE_SET);
			}
		}
		
		@Override
		public void processPacket(XMPPBean inBean) {
			
			if( inBean instanceof TargetBean ){
				handleTargetBean((TargetBean) inBean);
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
		
		private void handleTargetBean(TargetBean inBean){		
			XHuntPlayer player = game.getPlayerByJid(inBean.getFrom());
			
			if(player != null && !player.isMrx()){
				if(game.getRouteManagement().isTargetReachable(inBean.StationId, player)){
					if(player.getTicketsAmount().get(inBean.TicketId) != null
							&& player.getTicketsAmount().get(inBean.TicketId) > 0){
						if(inBean.Round == game.getRound()){
							if(inBean.IsFinal){
								player.setCurrentTargetToLastStation();					
								player.decreaseTicket(inBean.TicketId);
								game.getMisterX().increaseTicket(inBean.TicketId);
							}
							
							player.setCurrentTarget(inBean.StationId);
							player.setCurrentTargetFinal(inBean.IsFinal);
							
							control.getConnection().sendXMPPBeanResult(
									new TargetBean(inBean.TicketId),
									inBean
							);
							
							sendRoundStatusBean();
						}
						else{
							control.getConnection().sendXMPPBeanError(
									new TargetBean("cancel", "not-allowed", "Your not sync with the game server."),
									inBean
							);
							
							control.getConnection().handlePlayerNotReplies(player.getJid());
						}
					}
					else{
						if(game.getRouteManagement().isPlayerUnmovable(player)){
							if(inBean.TicketId == Settings.TICKET_ID_UNMOVABLE){
								control.getConnection().sendXMPPBeanResult(
										new TargetBean(inBean.TicketId),
										inBean
								);
							}
							else{
								control.getConnection().sendXMPPBeanError(
										new TargetBean("modify", "not-acceptable", "You're unmovable at this position."),
										inBean
								);
							}
						}
						// if ticketId == -1, its a suggestion
						else if(inBean.TicketId == Settings.TICKET_ID_SUGGESTION){
							player.setCurrentTarget(inBean.StationId);
							player.setCurrentTargetFinal(inBean.IsFinal);
							
							control.getConnection().sendXMPPBeanResult(
									new TargetBean(inBean.TicketId),
									inBean
							);
							
							sendRoundStatusBean();
						}
						else if(inBean.TicketId > 0){
							control.getConnection().sendXMPPBeanError(
									new TargetBean("modify", "not-acceptable", "Not enough tickets available."),
									inBean
							);
						}
					}
				}
				else{
					control.getConnection().sendXMPPBeanError(
							new TargetBean("modify", "not-acceptable", "Target station is not reachable from current."),
							inBean
					);
				}
				
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
	}
}
