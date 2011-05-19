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
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import de.tudresden.inf.rn.mobilis.server.services.xhunt.XHunt;

public class MessageService implements PacketListener{

	private XHunt control;
	
	/**
	 * Constructor
	 * @param control XHuntController, who administrates the whole life cycle
	 */
	public MessageService(XHunt control){
		this.control = control;
	}
	
	@Override
	public void processPacket(Packet packet) {
		
		if (packet instanceof Message){
			
			Message mes = (Message) packet;
			
			if (mes.getBody() != null){
				System.out.println("ChatMsg: " + packet.getFrom() + " - " + mes.getBody());
			}
				
		}
		
	}

}
