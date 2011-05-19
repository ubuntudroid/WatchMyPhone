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

import java.util.ArrayList;
import java.util.Map;

import de.tudresden.inf.rn.mobilis.server.services.xhunt.Game;
import de.tudresden.inf.rn.mobilis.server.services.xhunt.XHunt;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.AreasBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.CreateGameBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.GameDetailsBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.JoinGameBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.UsedTicketsBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.model.AreaInfo;

public class GameStateUninitialized extends GameState{
	
	public GameStateUninitialized(XHunt control, Game game)
	{
		this.control = control;
		this.game = game;
	}
	
	@Override
	public void processPacket(XMPPBean inBean) {
		if( inBean instanceof AreasBean){
			handleAreasBean((AreasBean) inBean);
		}
		else if( inBean instanceof CreateGameBean){
			handleCreateGameBean((CreateGameBean) inBean);
		}
		else if( inBean instanceof GameDetailsBean){
			handleGameDetailsBean((GameDetailsBean)inBean);
		}
		else if( inBean instanceof JoinGameBean){
			handleJoinGameBean((JoinGameBean) inBean);
		}
		else if( inBean instanceof UsedTicketsBean){
			handleUsedTicketsBean((UsedTicketsBean)inBean);
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
	
	private void handleAreasBean(AreasBean inBean){
		ArrayList<AreaInfo> areas = control.getSqlHelper().queryAreas();
		
		control.getConnection().sendXMPPBeanResult(
				new AreasBean(areas),
				inBean);
	}
	
	private void handleCreateGameBean(CreateGameBean inBean){
		String errorText = "";
		
		control.getSettings().setAreaId(inBean.AreaId);
		control.getSettings().setGameName(inBean.GameName);
		control.getSettings().setGamePassword(inBean.GamePassword);
		
		if(inBean.CountRounds < 21)
			control.getSettings().setRounds(inBean.CountRounds);
		else 
			errorText += " max round: 21";
		
		if(inBean.MinPlayers > 0)
			control.getSettings().setMinPlayers(inBean.MinPlayers);
		else 
			errorText += " min players: 1";
		
		if(inBean.MaxPlayers < 7)
			control.getSettings().setMaxPlayers(inBean.MaxPlayers);
		else 
			errorText += " max players: 7";
		
		if(inBean.StartTimer > 0)
			control.getSettings().setStartTimer(120000);//inBean.StartTimer
		else 
			errorText += " starttimer to low";
		
		if(errorText.length() == 0){			
			control.getSettings().setTicketsMrX(inBean.TicketsMrX);
			control.getSettings().setTicketsAgents(inBean.TicketsAgents);
			
			for(Map.Entry<Integer, Integer> e : inBean.TicketsAgents.entrySet()){
				control.log("id: " + e.getKey() + " amount: " + e.getValue());
			}
			
			for(Map.Entry<Integer, Integer> e : inBean.TicketsMrX.entrySet()){
				control.log("id: " + e.getKey() + " amount: " + e.getValue());
			}
			
			game.setGameIsOpen(true);
			game.setGameState(new GameStateLobby(control, game));
			control.log("Status changed to GameStateLobby");			
			
			control.getConnection().sendXMPPBeanResult(
					new CreateGameBean(),
					inBean);
		}
		else{
			inBean.errorType = "modify";
			inBean.errorCondition = "not-acceptable";
			inBean.errorText = "Errors: " + errorText;
			
			control.getConnection().sendXMPPBeanError(
					inBean,
					inBean
			);
		}
	}
	
	private void handleJoinGameBean(JoinGameBean inBean){
		control.getConnection().sendXMPPBeanError(
				new JoinGameBean("wait", "internal-server-error", "This GameService is not yet configured properly."),
				inBean
		);
	}
	
	private void handleUsedTicketsBean(UsedTicketsBean inBean){
		control.getConnection().sendXMPPBeanResult(
				new UsedTicketsBean(game.getUsedTickets()),
				inBean);
	}
}

