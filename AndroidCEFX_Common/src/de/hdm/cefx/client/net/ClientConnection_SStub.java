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
/**
 * This sourcecode is part of the Collaborative Editing Framework for XML (CEFX).
 * @author Michael Voigt
 * @author Sven Bendel
 */

package de.hdm.cefx.client.net;

import jabberSrpc.JabberClient;
import jabberSrpc.Stub;

import java.util.logging.Logger;

import de.hdm.cefx.awareness.AwarenessEvent;
import de.hdm.cefx.client.CEFXClient;
import de.hdm.cefx.concurrency.operations.Operation;

public class ClientConnection_SStub extends Stub {

	private final Logger LOG = Logger.getLogger(ClientConnection_SStub.class.getName());
	private NetworkController controller;

	public ClientConnection_SStub(JabberClient client,CEFXClient cefxclient) {
		super(client, "ClientConnection");
		client.registerMethod("ClientConnection", "executeOperation", this,cefxclient.getThreadID());
		client.registerMethod("ClientConnection", "notifyOfNewClientInSession", this,cefxclient.getThreadID());
		client.registerMethod("ClientConnection", "awarenessEvent", this,cefxclient.getThreadID());
		client.registerMethod("ClientConnection", "notifyOfDisconnectedClientInSession", this, cefxclient.getThreadID());
	}

	public void setNetworkController(NetworkController impl) {
		controller = impl;
		System.out.println("ClientConnectionImpl.setNetworkController() " + controller);
	}

	public void executeOperation(Object o) {
		Operation operation=(Operation)o;
		System.out.println("ClientConnectionImpl.executeOperation() " + operation);
		controller.executeAndPropagateRemoteOperation(operation);
	}

	public void notifyOfNewClientInSession(Object o)  {
		CEFXClient client=(CEFXClient)o;
		controller.notifyOfNewClientInSession(client);
	}
	
	public void notifyOfDisconnectedClientInSession(Object o) {
		CEFXClient client = (CEFXClient) o;
		controller.notifyOfDisconnectedClientInSession(client);
	}

	public void awarenessEvent(Object o) {
		AwarenessEvent event=(AwarenessEvent)o;
		LOG.info("Received Awareness Event: " + event);
		controller.awarenessEvent(event);
	}

}
