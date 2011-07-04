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
 * Copyright 2007 Ansgar Gerlicher.
 * @author Ansgar Gerlicher
 * @author Michael Voigt
 * @author Sven Bendel
 */
package de.hdm.cefx.client.net;

import jabberSrpc.JabberClient;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdm.cefx.awareness.AwarenessEvent;
import de.hdm.cefx.client.CEFXClient;
import de.hdm.cefx.concurrency.operations.Operation;

/**
 * The OutgoingClientConnectionHandler stores a reference to each client's
 * ClientConnection interface and uses the java.rmi.Naming interface to lookup
 * the remote interface (ClientConnection) of the new joined client.
 *
 * @author Ansgar Gerlicher
 * @author Sven Bendel
 *
 */
public class OutgoingClientConnectionHandler {
	private HashMap<CEFXClient, ClientConnection_CStub> clientConnections;

	ExecutorService tpe;

	/**
	 * Class constructor. This class is aware of all connections to clients
	 * within a session. If any message is to be send to a client, it is done
	 * via this class.
	 */
	public OutgoingClientConnectionHandler() {
		clientConnections = new HashMap<CEFXClient, ClientConnection_CStub>();
		// Ansgar Test
		// tpe = Executors.newCachedThreadPool();
		tpe = Executors.newSingleThreadExecutor();
	}

	/**
	 * Delegates the execution of operations to the other clients in the
	 * session.
	 *
	 * @param operation
	 *            the operation to be executed.
	 */
	public void executeOperation(final Operation operation) {

		Collection<ClientConnection_CStub> clients = clientConnections.values();
		for (final ClientConnection_CStub client : clients) {

			Runnable runner = new Runnable() {
				public void run() {
					Thread.yield();
					client.executeOperation(operation);
					Thread.yield();
				}
			};

			tpe.execute(runner);

		}

	}

	/**
	 * Adding the connection of a new client to the
	 * OutgoingClientConnectionHandler.
	 *
	 * @param client
	 *            the client that will be added.
	 */
	public void addClientConnection(CEFXClient client) {
		ClientConnection_CStub connection = new ClientConnection_CStub(JabberClient.getInstance());
		connection.setTarget(client.getConnectionString());
		connection.setThreadID(client.getThreadID());
		clientConnections.put(client, connection);
	}
	
	/**
	 * Removes the connection of a disconnected client
	 * from the OutgoingClientConnectionHandler.
	 * 
	 * @param client the client to be removed
	 */
	public void removeClientConnection(CEFXClient client) {
		clientConnections.remove(client);
	}

	/**
	 * Propagates the awareness event to all clients in the session.
	 *
	 * @param event
	 *            the awareness event object.
	 */
	public void awarenessEvent(final AwarenessEvent event) {
		Collection<ClientConnection_CStub> clients = clientConnections.values();
		for (final ClientConnection_CStub client : clients) {

			Runnable runner = new Runnable() {
				public void run() {
					client.awarenessEvent(event);
					Thread.yield();
				}
			};
			tpe.execute(runner);

		}

	}

}
