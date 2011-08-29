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
package de.hdm.cefx;

import java.util.Vector;

import de.hdm.cefx.awareness.AwarenessController;
import de.hdm.cefx.client.CEFXClient;
import de.hdm.cefx.concurrency.ConcurrencyController;
import de.hdm.cefx.concurrency.operations.Operation;
import de.hdm.cefx.concurrency.operations.OperationExecutor;
import de.hdm.cefx.server.ServerObject;

/**
 * The CEFXController takes care of the session handling and the server
 * connection. When a document is opened, the CEFXController checks, if the
 * client is already connected to a session with the server. If no session
 * exists, the controller connects the client to the server and opens a new
 * session. A document that is opened for the first time in a collaborative
 * editing session, is opened locally by the CEFXController and then send to the
 * server. The server then stores this document. The next time the document is
 * opened, the CEFXController loads it from the server instead of the local file
 * system. The CEFXController is created in the initialisation phase of the DA
 * and loads all plug-ins - as configured in the CEFX configuration file - and
 * initialises them. This is done with the help of the ExtensionRegistry. The
 * controller has a connection to every component of the system. It is
 * responsible for delegating the execution of operations to the concurrency
 * controller and allows retrieval of a reference to each of the other framework
 * components. The CEFXController implements the OperationExecutor interface and
 * thus acts, from the perspective of the concurrency controller, as a client.
 * 
 * @author Ansgar Gerlicher
 * @author Sven Bendel
 * 
 */
public interface CEFXController extends OperationExecutor {
	/**
	 * Loads the document using the given URI from the server or creates it using the document factory
	 * @param documentFactory the document factory of the application.
	 * @param URI the URI to the document.
	 * @return the document object to be edited.
	 */
	//public Document loadDocument(Object documentFactory, String URI);

	public Boolean openDocument(int docID);
	
	/**
	 * The CEFXController owns a CEFXClient object and the method
	 * <code>getClient()</code> is used by the NetworkController to retrieve
	 * this object. The CEFXClient object contains information necessary for
	 * connecting to the server and opening incoming connection ports.
	 * 
	 * @return the client object.
	 */
	public CEFXClient getClient();

	/**
	 * The CEFXController initialises � next to the CEFXClient object - the
	 * ConcurrencyController including the ConflictResolutionProvider, the
	 * NetworkController and the AwarenessController. References to these
	 * components can be retrieved by using the methods
	 * <code>getConcurrencyController()</code>,
	 * <code>getNetworkController()</code> and
	 * <code>getAwarenessController()</code>.
	 * 
	 * @return the concurrency controller object.
	 */

	public ConcurrencyController getConcurrencyController();

	/**
	 * The method <code>executeOperation(Operation operation)</code> is called
	 * by the CEFXDOMAdapter after the user issued a change of the document in
	 * the application. The CEFXDOMAdapter creates an Operation object and
	 * passes it on to the CEFXController which in turn delegates the execution
	 * of the local operation to the ConcurrencyController.
	 * 
	 * @param operation
	 *            the operation to be executed.
	 * @return true if the operation was executed successfully.
	 */
	public boolean executeOperation(Operation operation);

	/**
	 * Remote operations are received by the NetworkController and passed to the
	 * CEFXController via the <code>executeRemoteOperation(...)</code> method.
	 * The CEFXController delegates the execution of the remote operation to the
	 * ConcurrencyController and notifies the CEFXDOMAdapter to refresh the
	 * application's user interface in order to visualise the changes to the
	 * user.
	 * 
	 * @param operation
	 *            the remote operation to be executed.
	 */
	public void executeRemoteOperation(Operation operation);

	/**
	 * The method <code>notifyOfNewClientInSession(CEFXClient client)</code>
	 * is called by the NetworkController when a new client joins an open
	 * session. The CEFXController then adds the new client ID to the
	 * ConcurrencyController's state vector so that the new client is taken into
	 * consideration in the concurrency control. After this the state vector and
	 * the history buffer are cleared in order have the same default state at
	 * all editing sites.
	 * 
	 * @param client
	 *            the client that joined the session.
	 */
	public void notifyOfNewClientInSession(CEFXClient client);

	/**
	 * This method is called by the NetworkController when a client disconnects from an open
	 * session. The CEFXController then removes the client ID from the
	 * ConcurrencyController's state vector so that the disconnected client isn't taken into
	 * consideration in the concurrency control any more. After this the state vector and
	 * the history buffer are cleared in order have the same default state at
	 * all editing sites.
	 * 
	 * @param client
	 *            the client that disconnected from the session.
	 */
	public void notifyOfDisconnectedClientInSession(CEFXClient client);
	
	/**
	 * The CEFXController initialises � next to the CEFXClient object - the
	 * ConcurrencyController including the ConflictResolutionProvider, the
	 * NetworkController and the AwarenessController. References to these
	 * components can be retrieved by using the methods
	 * <code>getConcurrencyController()</code>,
	 * <code>getNetworkController()</code> and
	 * <code>getAwarenessController()</code>.
	 * 
	 * @return the awareness controller object.
	 */
	public AwarenessController getAwarenessController();

	public Vector<String> getServerList(String name);

	public Vector<ServerObject> listFiles();
	
	public boolean uploadDocument(String path);
	
	public boolean connect(String serverJid);
	
	public int getDocumentID(String name,Vector<ServerObject> files);
}
