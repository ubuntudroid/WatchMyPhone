/*******************************************************************************
 * Copyright (C) 2010 Technische Universität Dresden
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
package de.tudresden.inf.rn.mobilis.server.services.collabed;

import java.util.HashMap;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;

import de.hdm.cefx.client.CEFXClient;
import de.hdm.cefx.client.net.CEFXSession;
import de.hdm.cefx.client.net.CEFXSessionImpl;
import de.hdm.cefx.client.net.NetworkController;
import de.hdm.cefx.client.net.RemoteOperationExecutor;
import de.hdm.cefx.concurrency.ConcurrencyController;
import de.hdm.cefx.concurrency.OrderingConcurrencyControllerImpl;
import de.hdm.cefx.concurrency.operations.CollabEditingHandlerImpl;
import de.hdm.cefx.concurrency.operations.Operation;
import de.hdm.cefx.concurrency.operations.OperationExecutor;
import de.hdm.cefx.server.util.ServerUtil;
import de.hdm.cefx.util.XMLHelper;
import de.tudresden.inf.rn.mobilis.server.MobilisManager;
import de.tudresden.inf.rn.mobilis.server.services.CollabEditingService;

/**
 * The collaboration session allows a group of participants (stored as CEFXClients in the attached CEFXSessionImpl)
 * to collaboratively edit a replicated XML document (referenced on this server by the ServerDocument wrapper object). ...
 * @author Dirk Hering
 *
 */
public class CollabSession implements OperationExecutor, RemoteOperationExecutor {
	
	private CollabEditingHandlerImpl collabEditingHandler;
	private String mucRoomName;
	private MultiUserChat collabMucRoom;
	private CEFXSession cefxSession;
	private ConcurrencyController cc;
	private ManagedDocument managedDocument;
	private int currentMaxClientID;
	private XMPPConnection brokerXMPPConnection;
	private String name;
	private boolean open;
	
	public CollabSession(String sessionName, ManagedDocument document) {
		this(sessionName, document, null);
	}
	
	public CollabSession(String sessionName, ManagedDocument document, String plainMucRoomName) {
		brokerXMPPConnection = ((CollabEditingService) MobilisManager.getInstance().
			getService("CollabEditingService")).getAgent().getConnection();
		name = sessionName;
		this.managedDocument = document;
		currentMaxClientID = 0;
		this.mucRoomName = ServerUtil.getFullUnusedMucRoomIdentifier(brokerXMPPConnection, plainMucRoomName);
	}

	/**
	 * Opens the collaboration session for the referenced ManagedDocument.
	 * Currently, it is prevented to have multiple parallel sessions for the same file,
	 * although each session would have its own DOM Document instance of the file and 
	 * therefore only saving to the same file again could lead to lost updates.
	 * If no MultiUserChat room name has been provided so far, a random name
	 * will be generated.
	 * @return boolean - true, if everything went fine
	 */
	public boolean open() {
		
		if (managedDocument == null) {
			// prevent session without file
			return false;
		}

		if (!managedDocument.open()) { 
			// error during opening
			return false;
		}
		
		cefxSession = new CEFXSessionImpl(managedDocument.getName());

		cc = new OrderingConcurrencyControllerImpl();
		cc.setOperationExecutor(this);
		// The server has the id 0, the state of the server is initialized as 0
		cc.getStateVector().setState(0, 0);

		if (mucRoomName == null) {
			mucRoomName = ServerUtil.genUniqueMucRoomName(null);
		}
		
		collabMucRoom = ServerUtil.createMucRoom(brokerXMPPConnection, mucRoomName);
		collabEditingHandler = new CollabEditingHandlerImpl(this, cefxSession, 0);
//		collabDrawingHandler = new CollabDrawingHandler(collabEditingHandler);
		ServerUtil.addMucRoomListener(collabMucRoom, collabEditingHandler);
		cefxSession.setMucRoomName(mucRoomName);
		cc.setSession(cefxSession);

		cc.setDocument(managedDocument.getDocument());
		cc.setCollaborationReady(true);
		open = true;
		return true;
	}

	public void close() {
		// TODO closeCollabSession impl
	}
	
	public void addClient(final CEFXClient client) {
		if (!managedDocument.isOpen() || (cefxSession == null) || (cc == null)) return;
		cefxSession.addClient(client);
		// notify the local cc of the new client
		if (!cc.getStateVector().containsKey(client.getID())) {
			cc.getStateVector().put(client.getID(), 0);
		}
	}

	public int getFreeClientID() {
		currentMaxClientID += 1;
		return currentMaxClientID;
	}
		
	public CollabEditingHandlerImpl getCollabEditingHandler() {
		return collabEditingHandler;
	}
	
	public String getMucRoomName() {
		return mucRoomName;
	}

	public void setMucRoomName(String mucRoomName) {
		this.mucRoomName = mucRoomName;
	}

	public ConcurrencyController getConcurrencyController() {
		return cc;
	}
	
	/**
	 * Returns the ID of the server which is zero by definition.
	 * @return int - the server ID (0)
	 */
	@Override
	public int getIdentifier() {
		return 0;
	}

	/**
	 * Called by the ConcurrencyController to receive the name of the client, or in this case the name of 
	 * the collaboration session.
	 * @return String - the file name edited by this collaboration session
	 */
	@Override
	public String getName() {
//		if (serverDocument.isOpen()) {
//			return serverDocument.getName();
//		} else return "";
		return name;
	}
	
	/**
	 * OperationExecutor demands a client-side NetworkController, this is why here is null returned.
	 * (CEFX needs to be modified to support a common network controller!)
	 * @return null
	 */
	@Override
	public NetworkController getNetworkController() {
		return null;
	}

	@Override
	public void notifyOfNotSupportedOperation(Operation o) {}

	public ManagedDocument getManagedDocument() {
		return managedDocument;
	}

	public CEFXSession getCefxSession() {
		return cefxSession;
	}
	
	public HashMap<String, CEFXClient> getClientMap() {
		return cefxSession.getClientMap();
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	@Override
	public void executeRemoteOperation(Operation operation) {
		managedDocument.stopEdit();
		cc.executeRemoteOperation(operation);
		managedDocument.startEdit();
//		XMLHelper.showDocumentContent(managedDocument.getDocument());
	}
}
