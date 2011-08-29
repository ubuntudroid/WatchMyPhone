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

import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.hdm.cefx.CEFXController;
import de.hdm.cefx.CEFXControllerImpl;
import de.hdm.cefx.awareness.AwarenessEvent;
import de.hdm.cefx.awareness.events.AwarenessEventDescriptions;
import de.hdm.cefx.awareness.events.AwarenessEventTypes;
import de.hdm.cefx.awareness.events.EventPropagator;
import de.hdm.cefx.client.CEFXClient;
import de.hdm.cefx.concurrency.operations.CollabEditingHandlerImpl;
import de.hdm.cefx.concurrency.operations.InsertOperationImpl;
import de.hdm.cefx.concurrency.operations.NodePosition;
import de.hdm.cefx.concurrency.operations.Operation;
import de.hdm.cefx.concurrency.operations.OperationData;
import de.hdm.cefx.concurrency.operations.UpdateInsertOperation;
import de.hdm.cefx.dom.adapter.CEFXDOMAdapterImpl;
import de.hdm.cefx.server.DocumentData;
import de.hdm.cefx.server.ServerConnection_CStub;
import de.hdm.cefx.server.ServerObject;
import de.hdm.cefx.server.SessionData;

/**
 * The NetworkControllerImpl class implements the NetworkController interface.
 * It is composed of an OutgoingClientConnectionHandler, an
 * OutgoingServerConnectionHandler and a ClientConnection. Additionally it is
 * responsible for the client side session handling and thus owns a CEFXSession
 * object (implemented by the class CEFXSessionImpl), which is retrieved from
 * the server at the beginning of a session.
 *
 * @author Ansgar Gerlicher
 * @author Sven Bendel
 *
 */
@SuppressWarnings("serial")
public class NetworkControllerImpl implements NetworkController, RemoteOperationExecutor {

	private static Logger log = Logger.getLogger("NetworkControllerImpl");
	private OutgoingClientConnectionHandler clientCH;
	private ClientConnection_SStub clientConnection;

	private OutgoingServerConnectionHandler serverCH;

	private CEFXController cefx;
	private CEFXSession session;

	private Boolean connected;
	private int documentID;
	
	private CollabEditingHandlerImpl collabEditingHandler;
	
	private ExecutorService propagationThreadExecutor;

	/**
	 * Class constructor.
	 */
	public NetworkControllerImpl() {
		
	}

	/**
	 * This method is called to initialise and open the client connection for
	 * incoming remote calls. It creates the OutgoingClientConnectionHandler
	 * object.
	 */
	private void init() {
		JabberClient.getInstance().addExtensionProvider("x",OperationExtension.NAMESPACE+OperationExtension.UPDATE_DELETE,new UpdateDeleteExtensionProvider());
		JabberClient.getInstance().addExtensionProvider("x",OperationExtension.NAMESPACE+OperationExtension.UPDATE_INSERT,new UpdateInsertExtensionProvider());
		JabberClient.getInstance().addExtensionProvider("x",OperationExtension.NAMESPACE+OperationExtension.UPDATE_SET,new UpdateSetExtensionProvider());
		JabberClient.getInstance().addExtensionProvider("x",OperationExtension.NAMESPACE+OperationExtension.INSERT,new InsertExtensionProvider());
		JabberClient.getInstance().addExtensionProvider("x",OperationExtension.NAMESPACE+OperationExtension.DELETE,new DeleteExtensionProvider());

		documentID=-1;
		connected=false;
		openClientConnection();
		clientCH = new OutgoingClientConnectionHandler();
		
		propagationThreadExecutor = Executors.newSingleThreadExecutor();
	}

	public SessionData openDocument(int docID) {
		// TODO obsolete, see joinSession
		if (docID<0) return null;

		SessionData data=serverCH.getServerConnection().openDocument(docID, cefx.getClient());
		if (data==null) return null;

		documentID=docID;
		session=data.getSession();
		cefx.getClient().setID(data.getClientID());
		collabEditingHandler = new CollabEditingHandlerImpl(this, session, data.getClientID());

		if (session!=null) {
			String name=JabberClient.getInstance().getUserName()+"_"+Integer.toHexString(data.getClientID());
			JabberClient.getInstance().joinMucRoom(name,session.getMucRoomName(), collabEditingHandler);
		}

		return data;
	}
	
	public boolean leaveSession(String sessionName) {
		if ((sessionName == null) || (sessionName.equals("")))
			return false;
		
		boolean result = serverCH.getServerConnection().leaveSession(sessionName, cefx.getClient());
		
		if (result && session != null) {
			JabberClient.getInstance().leaveMucRoom(session.getMucRoomName());
			documentID = -1;
			session = null;
			cefx.getClient().setID(-1);
			collabEditingHandler = null;
		}
		return result;
	}

	public SessionData joinSession(String sessionName) {
		if ((sessionName == null) || (sessionName.equals("")))
			return null;

		SessionData data = serverCH.getServerConnection().joinSession(
				sessionName, cefx.getClient());
		if (data == null)
			return null;

		documentID = data.getDocID();
		session = data.getSession();
		cefx.getClient().setID(data.getClientID());
		collabEditingHandler = new CollabEditingHandlerImpl(this, session, data.getClientID());

		if (session != null) {
			String name = JabberClient.getInstance().getUserName() + "_"
					+ Integer.toHexString(data.getClientID());
			JabberClient.getInstance().joinMucRoom(name,
					session.getMucRoomName(), collabEditingHandler);
		}

		return data;
	}
	
	public void closeDocument() {
		//TODO closeDocument beim Server aufrufen
		documentID=-1;
	}

	public boolean connect(String serverJid) {
		if (serverCH == null) {
			serverCH = new OutgoingServerConnectionHandler();
		}
		return serverCH.connectToServer(serverJid);
	}

	public Vector<ServerObject> listFiles() {
		if (serverCH==null) {
			return null;
		}
		if (!connected) {
			return null;
		}

		ServerConnection_CStub con=serverCH.getServerConnection();
		return con.listFiles();
	}

	public DocumentData loadDocument() {
		if (documentID==-1) {
			return null;
		}
		DocumentData data=null;
		data=serverCH.getServerConnection().loadDocument(documentID);
		if (data==null) {
			System.err.println("Document could not be loaded from server!");
		}
		return data;
	}

	/**
	 * Opens an connection for incoming RPC message calls (uses ClientConnection_SStub).
	 */
	private void openClientConnection() {
		clientConnection=new ClientConnection_SStub(JabberClient.getInstance(),cefx.getClient());
		clientConnection.setNetworkController(this);

System.out.println("openClientConnection "+JabberClient.getInstance().getUserName());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.client.net.NetworkController#uploadDocument(org.w3c.dom.Document,
	 *      java.lang.String)
	 */
	public boolean uploadDocument(Document tempDoc, String name) {
		if ((tempDoc==null) || (name==null) || (name.length()==0)) {
			return false;
		}
		return serverCH.getServerConnection().uploadDocument(tempDoc, name);
	}

	/**
	 * Is used to retrieve the current session object.
	 *
	 * @return the current session object.
	 */
	public CEFXSession getSession() {
		return session;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.client.net.NetworkController#propagateOperation(de.hdm.cefx.concurrency.operations.Operation)
	 */
	public void propagateOperation(final Operation operation) {
		/*System.out.println("NetworkControllerImpl.propagateOperation()");
		serverCH.executeOperation(operation);*/
		// and last but not least we tell the clients to execute the
		// operation
		//clientCH.executeOperation(operation);

		
//		EventPropagator.propagateEvent(
//				new OperationData(operation), AwarenessEventTypes.OPERATION_EXECUTION.toString(), 
//				AwarenessEventDescriptions.LOCAL_OPERATION.toString(), 
//				EventPropagator.SCOPE_INTERNAL, operation.getClientName());
		
		Runnable propagateRunnable = new Runnable() {
			
			@Override
			public void run() {
				JabberClient.getInstance().sendMucRoomMessage(session.getMucRoomName(),OperationXMLTransformer.transformOperation2Message(operation,session.getMucRoomName()));
			}
		};
		
		propagationThreadExecutor.execute(propagateRunnable);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.client.net.NetworkController#notifyOfNewClientInSession(de.hdm.cefx.client.CEFXClient)
	 */
	public void notifyOfNewClientInSession(CEFXClient client) {

		if (session != null) {
			session.getClientMap().put(client.getName(), client);
			session.updateIdMap();
		}
		cefx.notifyOfNewClientInSession(client);
		clientCH.addClientConnection(client);
		
		System.out.println("NetworkControllerImpl.notifyOfNewClientInSession() client " + client.getID() + 
				" current state: " + cefx.getConcurrencyController().getStateVector());
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hdm.cefx.client.net.NetworkController#notifyOfDisconnectedClientInSession(de.hdm.cefx.client.CEFXClient)
	 */
	public void notifyOfDisconnectedClientInSession(CEFXClient client) {
		
		if (session != null) {
			session.getClientMap().remove(client.getName());
			session.updateIdMap();
		}
		cefx.notifyOfDisconnectedClientInSession(client);
		clientCH.removeClientConnection(client);
		
		System.out.println("NetworkControllerImpl.notifyOfDisconnectedClientInSession() client " + client.getID() + 
				" current state: " + cefx.getConcurrencyController().getStateVector());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.client.net.NetworkController#executeRemoteOperation(de.hdm.cefx.concurrency.operations.Operation)
	 */
	public void executeAndPropagateRemoteOperation(Operation operation) {

		cefx.executeRemoteOperation(operation);
		
//		// notify the GUI (and so the user) about the changes
//		EventPropagator.propagateEvent(
//				new OperationData(operation), AwarenessEventTypes.OPERATION_EXECUTION.toString(), 
//				AwarenessEventDescriptions.REMOTE_OPERATION.toString(), 
//				EventPropagator.SCOPE_INTERNAL, operation.getClientName());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.client.net.NetworkController#setCEFXController(de.hdm.cefx.CEFXController)
	 */
	public void setCEFXController(CEFXController impl) {
		//serverConnectionID=StringRegistry.getInstance().getValue("ClientNetworkSettings", "NetworkControllerImpl.server.connection.name");
		cefx = impl;
		//war schon auskommentiert:
		// create the listener
		// operationListener = new OperationExecutionListener();
		// cefx.getAwarenessController().addListener(operationListener);
		init();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.client.net.NetworkController#propagateAwarenessEvent(de.hdm.cefx.awareness.AwarenessEvent)
	 */
	public void propagateAwarenessEvent(AwarenessEvent event) {
		clientCH.awarenessEvent(event);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.client.net.NetworkController#awarenessEvent(de.hdm.cefx.awareness.AwarenessEvent)
	 */
	public void awarenessEvent(AwarenessEvent event) {
		cefx.getAwarenessController().awarenessEvent(event);
	}
	
//	public void processMessage(Packet packet) {
//		log.info("Received Remote Operation Message via Multi-User-Chat");
//		Message msg=(Message)packet;
//		Operation o=OperationXMLTransformer.transformMessage2Operation(msg, (CEFXSessionImpl)session,cefx.getClient().getID());
//		if (o!=null) {
//			executeAndPropagateRemoteOperation(o);
//		}
//	}


	@Override
	public void executeRemoteOperation(Operation operation) {
		executeAndPropagateRemoteOperation(operation);
	}

}
