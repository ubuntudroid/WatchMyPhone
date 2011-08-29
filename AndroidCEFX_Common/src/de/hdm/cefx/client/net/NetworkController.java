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
 * @author Dirk Hering
 * @author Sven Bendel
 */
package de.hdm.cefx.client.net;

import java.io.Serializable;
import java.util.Vector;

import org.w3c.dom.Document;

import de.hdm.cefx.CEFXController;
import de.hdm.cefx.awareness.AwarenessEvent;
import de.hdm.cefx.client.CEFXClient;
import de.hdm.cefx.concurrency.operations.Operation;
import de.hdm.cefx.server.DocumentData;
import de.hdm.cefx.server.ServerObject;
import de.hdm.cefx.server.SessionData;

/**
 * The NetworkController interface defines �outgoing� and �incoming� methods.
 * �Outgoing� methods are those methods used by the CEFXController in order to
 * send information to other clients or the server. �Incoming� methods are
 * methods that are indirectly called by either another client or the server.
 * They are called indirectly because all calls to a client are invoked on the
 * ClientConnection interface which in turn calls the NetworkController methods.
 *
 * @see de.hdm.cefx.client.net.ClientConnection
 *
 * @author Ansgar Gerlicher
 * @author Sven Bendel
 */
public interface NetworkController extends Serializable {

	/**
	 * The method <code>connect(String documentURI)</code> is called by the
	 * CEFXController when the client wants to open or join a document editing
	 * session. The OutgoingServerConnectionHandler then uses the
	 * java.rmi.Naming interface to lookup the server interface and open a
	 * connection to it using the server connection URI. The server connection
	 * URI identifies the server resource in the network. If the connection to
	 * the server was established, the NetworkController retrieves a
	 * ServerConnection object from the OutgoingServerConnectionHandler and
	 * calls its connect(CEFXClient client, String documentURI) method.
	 *
	 * @param documentURI
	 *            the document URI of the document that is to be edited in this
	 *            session.
	 * @return true if a connection was established successfully.
	 */
	//public boolean connect(String documentURI);

	public boolean connect(String serverJid);

	/**
	 * In order to retrieve a document from the server or upload a document to
	 * it, the methods <code>loadDocument(...)</code> and
	 * <code>uploadDocument(...)</code> are used by the CEFXController. When
	 * calling these methods, the NetworkController calls the corresponding
	 * methods of the ServerConnection interface
	 *
	 * @param documentURI
	 *            the document URI of the document that is to be loaded.
	 * @return the Document object that is to be edited.
	 */
	//public Document loadDocument(String documentURI);

	public DocumentData loadDocument();

	/**
	 * In order to retrieve a document from the server or upload a document to
	 * it, the methods <code>loadDocument(...)</code> and
	 * <code>uploadDocument(...)</code> are used by the CEFXController. When
	 * calling these methods, the NetworkController calls the corresponding
	 * methods of the ServerConnection interface.
	 *
	 * @param doc
	 *            the Document object that is to be edited and therefore
	 *            initially send to the server.
	 * @param name
	 *            the document name.
	 * @return true if the document was successfully uploaded to the server.
	 */
	public boolean uploadDocument(Document doc, String name);

	/**
	 * In order to propagate operations or awareness events, the
	 * NetworkController interface defines the methods
	 * <code>propagateOperation(...)</code> and
	 * <code>propagateAwarenessEvent(...)</code>. Operations are propagated
	 * to both, the server and all clients in a session. Awareness events are
	 * only propagated to the clients. The propagation of operations and
	 * awareness events to the clients is handled by the
	 * OutgoingClientConnectionHandler class. The propagation of operations to
	 * the server is handled by the OutgoingServerConnectionHandler class.
	 *
	 * @param operation
	 *            the operation that is to be propagated to the other sites.
	 */
	public void propagateOperation(Operation operation);

	/**
	 * The method <code>notifyOfNewClientInSession(CEFXClient client)</code>
	 * is called on the ClientConnection when a new client has joined the
	 * session. The ClientConnection in turn calls the
	 * <code>notifyOfNewClientInSession(CEFXClient
	 * client)</code> method of
	 * the NetworkController. The NetworkController then notifies the
	 * CEFXController of the new joining client and adds the client to the
	 * session represented by the CEFXSession object owned by the
	 * NetworkController. The last step is to inform the
	 * OutgoingClientConnectionHandler of the new client. The
	 * OutgoingClientConnectionHandler stores a reference to each client's
	 * ClientConnection interface and uses the java.rmi.Naming interface to
	 * lookup the remote interface (ClientConnection) of the new joined client.
	 * The CEFXClient interface (provided as argument) contains all information
	 * necessary for a RMI lookup. To prevent unecessary remote calls, the
	 * client is added directly to the map instead of using the
	 * CEFXSession.addClient() method.
	 *
	 * @param client
	 *            the CEFXClient object that specifies the remote client handle
	 */
	public void notifyOfNewClientInSession(CEFXClient client);

	/**
	 * This method is called on the ClientConnection when a client has disconnected
	 * from the session. The ClientConnection in turn calls the
	 * <code>notifyOfDisconnectedClientInSession(CEFXClient client)</code> method of
	 * the NetworkController. The NetworkController then notifies the
	 * CEFXController of the disconnected client and removes the client from the
	 * session represented by the CEFXSession object owned by the
	 * NetworkController. The last step is to inform the
	 * OutgoingClientConnectionHandler of the disconnected client.
	 * 
	 * @param client
	 *            the CEFXClient object that specifies the remote client handle
	 */
	public void notifyOfDisconnectedClientInSession(CEFXClient client);
	
	/**
	 * The method <code>executeRemoteOperation(Operation operation)</code> of
	 * the NetworkController is called by the ClientConnection when another
	 * client calls its <code>executeOperation(Operation operation)</code>
	 * method. The NetworkController simply delegates the call to the
	 * CEFXController.
	 *
	 * @param operation
	 *            the remote operation that is to be executed.
	 */
	public void executeAndPropagateRemoteOperation(Operation operation);

	/**
	 * This method is called, when the CEFXController initialises the
	 * NetworkController and is used to provide it with a reference to the
	 * CEFXController.
	 *
	 * @param controller
	 *            a reference to the CEFXController.
	 */
	public void setCEFXController(CEFXController controller);

	/**
	 * In order to propagate operations or awareness events, the
	 * NetworkController interface defines the methods
	 * <code>propagateOperation(...)</code> and
	 * <code>propagateAwarenessEvent(...)</code>. Operations are propagated
	 * to both, the server and all clients in a session. Awareness events are
	 * only propagated to the clients. The propagation of operations and
	 * awareness events to the clients is handled by the
	 * OutgoingClientConnectionHandler class. The propagation of operations to
	 * the server is handled by the OutgoingServerConnectionHandler class.
	 *
	 * @param event
	 *            the awareness event that is to be propagated to the other
	 *            sites.
	 */
	public void propagateAwarenessEvent(AwarenessEvent event);

	/**
	 * When the method <code>awarenessEvent(AwarenessEvent event)</code> of
	 * the ClientConnection interface is called, the call is delegated to the
	 * corresponding method of the NetworkController. The NetworkController in
	 * turn delegates the call to the AwarenessController.
	 *
	 * @param event
	 *            the awareness event.
	 */
	public void awarenessEvent(AwarenessEvent event);

	/**
	 * Is used to retrieve the current session object.
	 *
	 * @return the current session object.
	 */
	public CEFXSession getSession();

	public SessionData openDocument(int docID);
	
	public SessionData joinSession(String sessionName);
	
	public boolean leaveSession(String sessionName);

	public Vector<ServerObject> listFiles();
}
