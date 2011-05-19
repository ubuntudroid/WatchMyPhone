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
package de.tudresden.inf.rn.mobilis.server.services.xhunt.services;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import de.tudresden.inf.rn.mobilis.server.services.xhunt.XHunt;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.GameDetailsBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.JoinGameBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.PlayerExitBean;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanIQAdapter;

public class IQListener implements PacketListener{
		
	private XHunt control;

	public IQListener(XHunt control){
		this.control = control;
	}	

	@Override
	public void processPacket(Packet packet) {
		control.log("incoming packet: " + packet.toXML());
		if (packet instanceof BeanIQAdapter) {
    		XMPPBean xmppBean = ((BeanIQAdapter) packet).getBean();
    		
    		// Check if the incoming packet is a JoinGameBean for a new spectator
    		if( xmppBean instanceof JoinGameBean
    				&& xmppBean.getType()==XMPPBean.TYPE_SET
    				&& ((JoinGameBean) xmppBean).isSpectator) {
    			control.addSpectator(xmppBean.getFrom());
    			// Send the result
    			control.getConnection().sendXMPPBeanResult(new JoinGameBean(), xmppBean);    			
    		// Check if the incoming packet is a PlayerExitBean to remove a spectator
    		} else if (xmppBean instanceof PlayerExitBean 
    				&& xmppBean.getType()==XMPPBean.TYPE_SET
    				&& ((PlayerExitBean) xmppBean).isSpectator) {
    			control.removeSpectator(xmppBean.getFrom());
    			// Send the result
    			control.getConnection().sendXMPPBeanResult(new PlayerExitBean(), xmppBean);
    		} else {
    		
	    		// Notify all registered spectators
	    		control.getConnection().sendXMPPBean(xmppBean, control.getSpectators(), xmppBean.getType());
	    		
	    		if(!control.getConnection().verifyIncomingBean(xmppBean)){
	    			control.log("!!!Bean not verified: " + xmppBean.getId());
	    		}
	    		else if(xmppBean.getType() != XMPPBean.TYPE_ERROR){
	    			control.getActGame().processPacket(xmppBean);
	    		}
	    		else{
	    			control.log("ERROR: Bean of Type ERROR received: " 
	    					+ "type: " + xmppBean.errorType  
	    					+ " condition:" + xmppBean.errorCondition
	    					+ " text: " + xmppBean.errorText
	    					+ "\n" + control.getConnection().beanToString(xmppBean));
	    		}
    		}
		}
	}

}
