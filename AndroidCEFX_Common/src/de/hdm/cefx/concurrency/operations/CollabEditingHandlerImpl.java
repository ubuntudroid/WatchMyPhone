/*******************************************************************************
 * Copyright (C) 2010 Ansgar Gerlicher
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
 * Stuttgart, Hochschule der Medien: http://www.mi.hdm-stuttgart.de/mmb/
 * Collaborative Editing Framework or XML:
 * http://sourceforge.net/projects/cefx/
 * 
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: http://mobilisplatform.sourceforge.net
 ******************************************************************************/
package de.hdm.cefx.concurrency.operations;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import de.hdm.cefx.client.net.CEFXSession;
import de.hdm.cefx.client.net.CEFXSessionImpl;
import de.hdm.cefx.client.net.OperationXMLTransformer;
import de.hdm.cefx.client.net.RemoteOperationExecutor;

public class CollabEditingHandlerImpl extends CollabEditingHandler {
	
	public CollabEditingHandlerImpl(RemoteOperationExecutor executor, CEFXSession currentSession, int clientID) {
		super(executor, currentSession, clientID);
	}

	@Override
	public void processMessage(Packet packet) {
		Message msg = (Message) packet;
		System.out.println("Received Remote Operation Message via Multi-User-Chat, from: " + msg.getFrom());
		System.out.println("Message: " + msg.toXML());
		Operation o = OperationXMLTransformer.transformMessage2Operation(msg,
				(CEFXSessionImpl) session, clientID);
		if (o != null) {
			handleOperation(o);
		}
	}

	@Override
	public void processPresencePacket(Packet packet) {}

	@Override
	public void handleOperation(Operation operation) {
		// TODO insert check if this operation is remote or local
		executor.executeRemoteOperation(operation);
	}

}
