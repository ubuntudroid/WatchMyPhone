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
package de.tudresden.inf.rn.mobilis.server.services;

import jabberSrpc.JabberClient;
import jabberSrpc.Stub;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.hdm.cefx.client.CEFXClient;
import de.hdm.cefx.client.net.CEFXSession;
import de.hdm.cefx.client.net.CEFXSessionImpl;
import de.hdm.cefx.client.net.DeleteExtensionProvider;
import de.hdm.cefx.client.net.InsertExtensionProvider;
import de.hdm.cefx.client.net.OperationExtension;
import de.hdm.cefx.client.net.UpdateDeleteExtensionProvider;
import de.hdm.cefx.client.net.UpdateInsertExtensionProvider;
import de.hdm.cefx.client.net.UpdateSetExtensionProvider;
import de.hdm.cefx.concurrency.ConcurrencyController;
import de.hdm.cefx.concurrency.operations.Operation;
import de.hdm.cefx.concurrency.operations.StateVector;
import de.hdm.cefx.server.DocumentData;
import de.hdm.cefx.server.ServerConnection_joinSession;
import de.hdm.cefx.server.ServerConnection_loadDocument;
import de.hdm.cefx.server.ServerConnection_openDocument;
import de.hdm.cefx.server.ServerConnection_uploadDocument;
import de.hdm.cefx.server.ServerObject;
import de.hdm.cefx.server.SessionData;
import de.hdm.cefx.server.SoXmlFile;
import de.hdm.cefx.server.util.ServerUtil;
import de.hdm.cefx.util.XMLHelper;
import de.tudresden.inf.rn.mobilis.Constants;
import de.tudresden.inf.rn.mobilis.server.MobilisManager;
import de.tudresden.inf.rn.mobilis.server.agents.MobilisAgent;
import de.tudresden.inf.rn.mobilis.server.services.collabed.CollabSession;
import de.tudresden.inf.rn.mobilis.server.services.collabed.DocumentSaver;
import de.tudresden.inf.rn.mobilis.server.services.collabed.ManagedDocument;
import de.tudresden.inf.rn.mobilis.services.cores.DocumentContentBuilder;
import de.tudresden.inf.rn.mobilis.services.cores.MapDrawKMLDocumentBuilder;

/**
 * A MobilisService managing real-time collaborative editing sessions on arbitrary XML documents.
 * @author Dirk Hering
 * @author Ansgar Gerlicher
 * @author Michael Voigt
 */
public class CollabEditingService extends MobilisService {
	
	private ServerConnection_ServerStub serverRPCHandler;
	private final Map<String, CollabSession> collabSessions = Collections.synchronizedMap(new HashMap<String, CollabSession>());
	private final List<ManagedDocument> documents = Collections.synchronizedList(new ArrayList<ManagedDocument>());
	private int currentMaxDocumentID;
	private DocumentSaver saver;
	private String documentsFolder;			
	private boolean creatingNewDocs;
	private DocumentContentBuilder docContentBuilder;

	public CollabEditingService() {
		super();  	
    	
		creatingNewDocs = true;
		currentMaxDocumentID = 0;
		documentsFolder = "documents"; // TODO use mobilis settings
		File docFolder = new File(documentsFolder);
		if (!docFolder.exists()) {
			docFolder.mkdir();
		}
				
		loadDocuments();
		saver = new DocumentSaver(this);
		//TODO start saverThread
//		Thread saverThread = new Thread(saver);
//		saverThread.setPriority(Thread.MIN_PRIORITY);
//		saverThread.start();
	}
	
	
	// ##################### XMPP-related #####################
	
	public class ServerConnection_ServerStub extends Stub {
		
		public ServerConnection_ServerStub(JabberClient client) {
			super(client, "ServerConnection");
			client.registerMethod("ServerConnection", "openDocument", this, null);
			client.registerMethod("ServerConnection", "closeDocument", this, null);
			client.registerMethod("ServerConnection", "loadDocument", this, null);
			client.registerMethod("ServerConnection", "uploadDocument", this, null);
			client.registerMethod("ServerConnection", "listFiles", this, null);
			client.registerMethod("ServerConnection", "joinSession", this, null);
		}

		public SessionData openDocument(Object o) {
			// TODO obsolete (see joinSession)
			ServerConnection_openDocument c=(ServerConnection_openDocument)o;
			String sessionName = ServerUtil.getFileNameWithoutExtension(getDocument(c.docID).getName());
			translateClientJID(c.client);
			return CollabEditingService.this.joinSession(sessionName, c.client);
		}

		public void closeDocument(Object o) {
			//TODO closeDocument impl
		}

		public DocumentData loadDocument(Object o) {
			ServerConnection_loadDocument c=(ServerConnection_loadDocument)o;
			return CollabEditingService.this.loadSessionDocument(getDocument(c.docID).getName());
		}

		public Boolean uploadDocument(Object o) {
			ServerConnection_uploadDocument c=(ServerConnection_uploadDocument)o;
			return new Boolean(CollabEditingService.this.uploadDocument(c.getDocument(),c.name));
		}

		public Vector<ServerObject> listFiles(Object o) {
			return CollabEditingService.this.listFiles();
		}
		
		public SessionData joinSession(Object o) {
			ServerConnection_joinSession c=(ServerConnection_joinSession)o;
			translateClientJID(c.client);
			return CollabEditingService.this.joinSession(c.sessionName, c.client);
		}
	}
	
	/**
	 * Changes the transmitted client JID host part to the one locally visible to correctly connect to clients
	 * behind routers or firewalls (as it is the case with a local Android client).
	 * @param client the client for which the JID should be updated
	 */
	private void translateClientJID(CEFXClient client) {
		String fullJID = client.getConnectionString();
		String name = StringUtils.parseName(fullJID);
		String resource = StringUtils.parseResource(fullJID);
		client.setConnectionString(name + "@" + JabberClient.getInstance().getHost() + "/" + resource);
	}
	
	@Override
	public void startup(MobilisAgent agent) throws Exception {
		super.startup(agent);
		XMPPConnection xmpp = mAgent.getConnection();
		JabberClient cefxJabberClient = JabberClient.getInstance();
		cefxJabberClient.setUsesProvidedConnection(true);
		cefxJabberClient.setProvidedXMPPConnection(xmpp);
		cefxJabberClient.connect();
		serverRPCHandler = new ServerConnection_ServerStub(cefxJabberClient);
	}

	@Override
	protected void registerPacketListener() {
		ProviderManager pm = ProviderManager.getInstance();
		pm.addExtensionProvider("x",OperationExtension.NAMESPACE+OperationExtension.UPDATE_DELETE,new UpdateDeleteExtensionProvider());
		pm.addExtensionProvider("x",OperationExtension.NAMESPACE+OperationExtension.UPDATE_INSERT,new UpdateInsertExtensionProvider());
		pm.addExtensionProvider("x",OperationExtension.NAMESPACE+OperationExtension.UPDATE_SET,new UpdateSetExtensionProvider());
		pm.addExtensionProvider("x",OperationExtension.NAMESPACE+OperationExtension.INSERT,new InsertExtensionProvider());
		pm.addExtensionProvider("x",OperationExtension.NAMESPACE+OperationExtension.DELETE,new DeleteExtensionProvider());
	}
	
	@Override
	public List<String> getNodeFeatures() {
		List<String> features = super.getNodeFeatures();
//		features.add(MobilisManager.discoNamespace + "#MyInterfaceFeature");
		return features;
    }
	
	@Override
	public void shutdown() throws Exception {
		// closing all collaboration sessions
		XMPPConnection xmpp = mAgent.getConnection();
		if ((xmpp != null) && xmpp.isConnected()) {
			synchronized(collabSessions) {
				for (CollabSession session : collabSessions.values()) {
					try {
						session.close();
					} catch (Exception e) {
						MobilisManager.getLogger().warning("Error during closing a collaboration session!");
					}
				}
				collabSessions.clear();
			}
		}
		super.shutdown();
	}
	
	
	
	
	
	// ##################### ManagedDocument-related #####################
	
	public void loadDocuments() {
		File dir = new File(documentsFolder);
		String[] children = dir.list();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				String filename = children[i];
				ManagedDocument d = new ManagedDocument(getFreeDocumentID(), dir.getPath() + "/" + filename);
				documents.add(d);
			}
		}

	}
	
	private synchronized int getFreeDocumentID() {
		currentMaxDocumentID += 1;
		return currentMaxDocumentID;
	}
	
	public Vector<ServerObject> listFiles() {
		Vector<ServerObject> objects = new Vector<ServerObject>();
		int ii;
		for (ii = 0; ii < documents.size(); ii++) {
			ManagedDocument doc = documents.get(ii);
			ServerObject so = new SoXmlFile(ServerObject.XML_FILE, doc
					.getDocumentId(), doc.getName());
			objects.add(so);
		}
		return objects;
	}
	
	/**
	 * Creates the ManagedDocument as a wrapper object for a DOM Document, which can be
	 * generated later from the given path (including file name). An empty file with the given name
	 * is created under the service's documentsPath for the new ManagedDocument.
	 * @param id the ID to assign to this ManagedDocument
	 * @param fileName the path of the file the ManagedDocument should point to
	 * @param overwrite indicates, whether an existing file should be overwritten
	 * @return ManagedDocument the created ManagedDocument, or null in case of errors
	 */
	public ManagedDocument createDocument(int id, String fileName, boolean overwrite) {
		
		if ((fileName == null) || ("".equals(fileName))) {
			return null;
		}
		
		String path = documentsFolder + "/" + fileName;
		ManagedDocument managedDoc = new ManagedDocument(id, path);
		File file = new File(path);
		if (file.exists()) {
			if (overwrite) {
				if (!file.delete()) return null;
				ManagedDocument formerLoadedDoc = getDocument(fileName);
				if (formerLoadedDoc != null) {
					documents.remove(formerLoadedDoc);
				}
			} else {
				return managedDoc;
			}
		} 
		
		try {
			if (!file.createNewFile()) return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		documents.add(managedDoc);
		return managedDoc;
	}
	
	/**
	 * Only allows to upload a DOM Document with the given fileName to the server - not creating any session.
	 * Internally, a ManagedDocument is therefore created and backed with the provided DOM Document, which gets 
	 * decorated by new CEFX UUIDs to enable editing access to its elements.
	 * @param doc the DOM Document to upload
	 * @param fileName the file name of the document
	 * @return boolean - true if everything went fine, false if the document already exists or if wrong provided parameters
	 */
	public boolean uploadDocument(Document doc, String fileName) {
		if (doc == null) {
			return false;
		}
		ManagedDocument mdoc = null;
		ManagedDocument docAlreadyOnServer = getDocument(fileName);
		if (docAlreadyOnServer == null) {
			mdoc = createDocument(getFreeDocumentID(), fileName, false);
		}
		if (mdoc == null) {
			// document was not loaded on the server, but anyway a file with that name already exists
			return false;
		}
		storeDOMDocument(mdoc, doc);
		documents.add(mdoc);
		return true;
	}

	/**
	 * Stores the given DOM Document in the ManagedDocument by decorating it with CEFX UUIDs and
	 * serializing it to the internally referenced file of the ManagedDocument.
	 * @param mdoc the ManagedDocument which should hold the DOM Document
	 * @param doc the DOM Document to store
	 */
	private void storeDOMDocument(ManagedDocument mdoc, Document doc) {
		ServerUtil util = new ServerUtil();
		Document decoratedDoc = util.addUUIDsToDocument(doc);
		mdoc.setDocument(decoratedDoc);
		mdoc.saveDocument();
	}

	private ManagedDocument getDocument(String name) {
		synchronized(documents) {
			for (ManagedDocument mdoc : documents) {
				if (mdoc.getName().equals(name)) return mdoc;
			}
		}
		return null;
	}

	private ManagedDocument getDocument(int documentId) {
		synchronized(documents) {
			for (ManagedDocument mdoc : documents) {
				if (mdoc.getDocumentId() == documentId) return mdoc;
			}
		}
		return null;
	}
	
	
	
	// ##################### CollabSession-related #####################
	
	/**
	 * Looks for a ManagedDocument with the same name and creates a CollabSession
	 * for it. If there is no such document, a new document gets created, according to the 
	 * associated ManagedDocumentBuilder (provided that this service is set to do so)
	 * @param sessionName the name of the collaboration session to create
	 * @return CollabSession - the created collaboration session or null if there is 
	 * already existing a session with that name
	 */
	private CollabSession createCollabSession(String sessionName) {
		// check if this session exists
		if (collabSessions.get(sessionName) != null) return null;
		// look up document with the provided name
		ManagedDocument mdoc = null;
		synchronized(documents) {
			for (ManagedDocument doc : documents) {
				if (ServerUtil.getFileNameWithoutExtension(doc.getName()).equals(sessionName)) {
					mdoc = doc;
				}
			}
		}
		// create the document if none found
		if (mdoc == null) {
			if (creatingNewDocs) {
				
				// TODO support to choose different document content builders
				docContentBuilder = new MapDrawKMLDocumentBuilder();
				String fileExt = docContentBuilder.getFileExtension();
				mdoc = createDocument(getFreeDocumentID(), sessionName + fileExt, true);
				Document doc = XMLHelper.createEmptyDocument();
				docContentBuilder.createInitialDocumentStructure(doc);
				storeDOMDocument(mdoc, doc);
				
			} else {
				return null;
			}
		}
		CollabSession newSession = new CollabSession(sessionName, mdoc, Constants.COLLAB_MUC_ROOM_PREFIX + sessionName);
		collabSessions.put(sessionName, newSession);
		return newSession;
	}
	
	/**
	 * Lets the given client join the collaboration session indicated by the session name.
	 * @param sessionName the name of the collaboration session to join
	 * @param client the client which joins
	 * @return SessionData - bundle consisting of the internal CEFXSession and CEFXClient
	 */
	public SessionData joinSession(String sessionName, CEFXClient client) {
		CollabSession session = collabSessions.get(sessionName);
		if (session == null) {
			session = createCollabSession(sessionName);
		}
		return joinCollabSession(session, client);
	}
	
	/**
	 * Lets the given client join the existing collaboration session.
	 * @param session the collaboration session to join
	 * @param client the client which joins
	 * @return SessionData - bundle consisting of the internal CEFXSession and CEFXClient
	 */
	private SessionData joinCollabSession(CollabSession session, CEFXClient client) {
		if ((session == null) || (client == null)) return null;
		SessionData sessionData = null;
		
		// first, open the session if it is not yet open
		if (!session.isOpen()) {
			if (!session.open()) {
				// session could not be opened
				return null;
			}
		}
		
		ManagedDocument mdoc = session.getManagedDocument();
		if (mdoc != null) {
			if (!mdoc.isOpen()) {
				mdoc.open();
				// debug
				System.out.println("Loading document:");
				XMLHelper.showDocumentContent(mdoc.getDocument());
			}
			
			sessionData = new SessionData();
			int newClientID = session.getFreeClientID();
			client.setID(newClientID);
			session.addClient(client);

			CEFXSession cefxSession = session.getCefxSession();
			sessionData.setSession(((CEFXSessionImpl) cefxSession).clone());
			sessionData.setClientID(newClientID);
			sessionData.setDocID(mdoc.getDocumentId());
		}

		return sessionData;
	}

	public void closeCollabSessions() {
		synchronized(collabSessions) {
			for (CollabSession session : collabSessions.values()) {
				closeCollabSession(session);
			}
		}
		collabSessions.clear();
	}
	
	private void closeCollabSession(CollabSession session) {
		// TODO impl close collab session
//		session.close();
//		collabSessions.remove(session.getName());
//		...
	}
	
	public void leaveCollabSession(String sessionName, CEFXClient client) {
		// TODO impl leave collab session
//		CollabSession session = collabSessions.get(sessionName);
//		if (session != null) {
//			session.removeClient(client);
//		}
//		...
	}
	
	/**
	 * Called by a client after session join to retrieve the collaboratively-edited document.
	 * @param sessionName the name of the collaboration session to load the document from
	 * @return DocumentData - a bundle of the DOM Document, the history buffer, deleted nodes, 
	 * the current state vector of the edited document
	 */
	public synchronized DocumentData loadSessionDocument(String sessionName) {
		DocumentData data = null;
		CollabSession session = collabSessions.get(ServerUtil.getFileNameWithoutExtension(sessionName));
		if (session != null) {
			ManagedDocument mdoc = session.getManagedDocument();
			if (mdoc != null) {
				data = new DocumentData();
				mdoc.stopEdit();
				Document doc = mdoc.getDocument();
				if (doc != null) {
					data.setDocument(doc);
					ConcurrencyController cc = session.getConcurrencyController();
					Stack<Operation> hb = (Stack<Operation>) ((Stack<Operation>) cc.getHistoryBuffer()).clone();
					data.setHistoryBuffer(hb);
					data.setDeletedNodesMap((HashMap<Operation, Node>) cc.getDeletedNodesMap().clone());
					data.setStateVector((StateVector) cc.getStateVector().clone());
				}
				mdoc.startEdit();
			}
		}
		return data;
	}

	public String getDocumentsFolder() {
		return documentsFolder;
	}

	public void setDocumentsFolder(String documentsFolder) {
		this.documentsFolder = documentsFolder;
	}
	
	public boolean isCreatingNewDocs() {
		return creatingNewDocs;
	}

	public void setCreatingNewDocs(boolean creatingNewDocs) {
		this.creatingNewDocs = creatingNewDocs;
	}

}
