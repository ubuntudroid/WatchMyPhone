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

import de.tudresden.inf.rn.mobilis.server.services.xhunt.Game;
import de.tudresden.inf.rn.mobilis.server.services.xhunt.XHunt;
import de.tudresden.inf.rn.mobilis.server.services.xhunt.model.XHuntPlayer;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.GameDetailsBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.PlayersBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.RoundStatusBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.model.RoundStatusInfo;

/**Abstract class for the game states.
 * @author elmar, Daniel Esser
 *
 */
public abstract class GameState {

	protected XHunt control;
	protected Game game;
	
	/**Handles the IQ packet in the corresponding game state. Has to be overwritten by all subclasses.
	 * @param iq Packet.
	 */
	public abstract void processPacket(XMPPBean bean);
	
	protected void handleGameDetailsBean(GameDetailsBean inBean){
		if(game != null){
			ArrayList<String> playernames = new ArrayList<String>();
			
			for(XHuntPlayer player : game.getPlayers().values()){
				playernames.add(player.getName() + "; ");
			}
			
			control.getConnection().sendXMPPBeanResult(
					new GameDetailsBean(control.getServiceName(),
							(control.getSettings().getGamePassword() != null
									&& control.getSettings().getGamePassword().length() > 0),
							control.getSettings().getRounds(),
							control.getSettings().getStartTimer(),
							playernames,
							game.isGameOpen()
					),
					inBean
			);
		}
		else{
			control.getConnection().sendXMPPBeanResult(
					new GameDetailsBean(),
					inBean
			);
		}
	}
	
	// Notify all players about the player states status (even sender)
	public void sendPlayersBean(String info){
		if(game != null && control != null){
			System.out.println("gameplayers: " + game.getPlayers().size());
			control.getConnection().sendXMPPBean(
					new PlayersBean(game.getPlayerInfos(), info),
					game.getPlayers().keySet(),
					XMPPBean.TYPE_SET
			);
		}
	}
	
	protected void sendRoundStatusBean(){
		if(game != null && control != null){
			ArrayList<RoundStatusInfo> info = new ArrayList<RoundStatusInfo>();
			
			for(XHuntPlayer player : game.getPlayers().values())
				info.add(player.getRoundStatusInfo());
			
			control.getConnection().sendXMPPBean(
					new RoundStatusBean(game.getRound(), info), game.getPlayers().keySet(), XMPPBean.TYPE_SET);
		}
	}
	
	protected void sendRoundStatusBeanForMrX(){
		if(game != null && control != null){
			ArrayList<RoundStatusInfo> info = new ArrayList<RoundStatusInfo>();
			info.add(game.getMisterX().getRoundStatusInfo());
			
			control.getConnection().sendXMPPBean(
					new RoundStatusBean(game.getRound(), info), game.getMisterX().getJid(), XMPPBean.TYPE_SET);
		}
	}
	
	protected void sendRoundStatusBeanForAgents(){
		if(game != null && control != null){
	
			ArrayList<RoundStatusInfo> info = new ArrayList<RoundStatusInfo>();
			
			for(XHuntPlayer player : game.getAgents())
				info.add(player.getRoundStatusInfo());
			
			control.getConnection().sendXMPPBean(
					new RoundStatusBean(game.getRound(), info), game.getPlayers().keySet(), XMPPBean.TYPE_SET);
		}
	}
}
