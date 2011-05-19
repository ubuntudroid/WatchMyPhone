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

import de.tudresden.inf.rn.mobilis.server.services.xhunt.Game;
import de.tudresden.inf.rn.mobilis.server.services.xhunt.XHunt;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.GameDetailsBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.GameOverBean;

public class GameStateGameOver extends GameState{

	public GameStateGameOver(XHunt control, Game game){
		this.control = control;
		this.game = game;
		
		game.setGameIsOpen(false);
		
		checkShutdownCondition();
	}
	
	private void checkShutdownCondition(){
		control.log("shutdown in gameOverState?: " + (game.getPlayers().size() == 0));
		if(game.getPlayers().size() == 0){
			try {
				control.shutdown();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void processPacket(XMPPBean inBean) {

		if( inBean instanceof GameDetailsBean){
			handleGameDetailsBean((GameDetailsBean)inBean);
		}
		else if( inBean instanceof GameOverBean){			
			game.getPlayers().remove(inBean.getFrom());
		}
		else{
			inBean.errorType = "modify";
			inBean.errorCondition = "not-acceptable";
			inBean.errorText = "Just GameOver is accepted in this state.";
			
			control.getConnection().sendXMPPBeanError(
					inBean,
					inBean
			);			
		}
		checkShutdownCondition();
	}
}
