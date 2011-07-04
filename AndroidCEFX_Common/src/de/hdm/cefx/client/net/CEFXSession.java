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

import java.io.Serializable;
import java.util.HashMap;

import de.hdm.cefx.client.CEFXClient;

/**
 * A <code>CEFXSession</code> represents a concurrent editing session and
 * contains information on the different clients and the document that is
 * edited. A CEFXSession object is created by the server and transmitted over
 * the network to all clients taking part in the editing session. Each client
 * thereby retrieves a copy of the server's CEFXSession object when it connects
 * to the server. The CEFXSession interface is implemented by the
 * CEFXSessionImpl class
 *
 * @author Ansgar Gerlicher
 * @author Sven Bendel
 *
 */
public interface CEFXSession extends Serializable {

	/**
	 * The method <code>addClient(CEFXClient client)</code> is called by the
	 * server when a client connects to it. It adds the new client to the
	 * session's map of clients. The map of clients (clientMap) contains the
	 * name and a CEFXClient object identifying each client.
	 *
	 * @param client
	 *            a reference to the CEFXClient.
	 */
	void addClient(CEFXClient client);

	/**
	 * This method is called by the server when a client wants to disconnect from
	 * a running session. It removes the client from the session's map of clients.
	 * @param client a reference to the CEFXClient
	 * @return <code>true</code> if the client was found in the client map, <code>false</code> otherwise
	 */
	boolean removeClient(CEFXClient client);
	
	/**
	 * The method <code>getDocumentID()</code> returns the document's URI
	 * which is a path to the document relative to its location in the local
	 * document repository. This URI is used for the identification of a
	 * document and thus is called document id in this context. The server uses
	 * this document id to check if a client requests to connect to an existing
	 * session or to a new one.
	 *
	 * @return the URI of the document.
	 */
	String getDocumentID();

	/**
	 * The method <code>getClientMap()</code> returns the session's map of
	 * clients. This method is called by the NetworkController in order to add a
	 * new client to its local CEFXSession object. This is done in the
	 * <code>notifyOfNewClientInSession(CEFXClient client)</code> method of
	 * the NetworkController.
	 *
	 * @see de.hdm.cefx.client.net.NetworkController#notifyOfNewClientInSession(CEFXClient)
	 *
	 * @return the map of clients.
	 */
	HashMap<String, CEFXClient> getClientMap();

	public String getMucRoomName();

	public void setMucRoomName(String mucRoomName);

	public void updateIdMap();
}
