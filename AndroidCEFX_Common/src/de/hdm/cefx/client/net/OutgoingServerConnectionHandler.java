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
  */
package de.hdm.cefx.client.net;

import jabberSrpc.JabberClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdm.cefx.server.ServerConnection_CStub;

/**
 * The OutgoingServerConnectionHandler then uses the java.rmi.Naming interface
 * to lookup the server interface and open a connection to it using the server
 * connection URI. The server connection URI identifies the server resource in
 * the network. If the connection to the server was established, the
 * NetworkController retrieves a ServerConnection object from the
 * OutgoingServerConnectionHandler and calls its
 * <code>connect(CEFXClient client,
 * String documentURI)</code> method.
 *
 * @author Ansgar Gerlicher
 *
 */
public class OutgoingServerConnectionHandler {
	private ServerConnection_CStub serverConnection = null;

	private boolean isConnected = false;

	ExecutorService tpe;

	/**
	 * Class constructor.
	 */
	public OutgoingServerConnectionHandler() {
		tpe = Executors.newSingleThreadExecutor();
	}

	/**
	 * Establishes a connection to the CEFX server.
	 *
	 * @param connectionString
	 *            the URI that identifies the server within the network.
	 * @return true if a connection was successfully established.
	 */
	public boolean connectToServer(String connectionString) {
		serverConnection=new ServerConnection_CStub(JabberClient.getInstance());
		serverConnection.setTarget(connectionString);

		//prüfen ob der CEFXServer bei Jabber angemeldet ist
//		boolean online=JabberClient.getInstance().isUserAvailable(connectionString);
		return true;
	}

	/**
	 * Allows to retrieve the server connection interface.
	 *
	 * @return the ServerConnection interface.
	 */
	public ServerConnection_CStub getServerConnection() {
		return serverConnection;
	}

	/**
	 * Propagates the operation that is to be executed to the server.
	 *
	 * @param operation
	 *            the operation that is to be executed.
	 */
/*	public void executeOperation(final Operation operation) {
		System.out.println("OutgoingServerConnectionHandler.executeOperation()");

		Runnable runner = new Runnable() {
			public void run() {
				Thread.yield();
				boolean executed = serverConnection.executeOperation(operation);
				System.out.println("OutgoingServerConnectionHandler.executeOperation() execution successfull? " + executed);
				Thread.yield();
			}
		};

		tpe.execute(runner);

	}*/
}
