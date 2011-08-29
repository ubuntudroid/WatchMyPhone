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
package de.hdm.cefx;

import jabberSrpc.JabberClient;

import java.io.File;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.hdm.cefx.awareness.AwarenessController;
import de.hdm.cefx.awareness.AwarenessEvent;
import de.hdm.cefx.awareness.events.AwarenessEventTypes;
import de.hdm.cefx.client.CEFXClient;
import de.hdm.cefx.client.CEFXClientImpl;
import de.hdm.cefx.client.net.NetworkController;
import de.hdm.cefx.concurrency.ConcurrencyController;
import de.hdm.cefx.concurrency.operations.NodePosition;
import de.hdm.cefx.concurrency.operations.Operation;
import de.hdm.cefx.concurrency.operations.OperationData;
import de.hdm.cefx.concurrency.operations.OperationFactory;
import de.hdm.cefx.concurrency.operations.StateVector;
import de.hdm.cefx.dom.adapter.CEFXDOMAdapter;
import de.hdm.cefx.dom.adapter.CEFXDOMAdapterImpl;
import de.hdm.cefx.server.DocumentData;
import de.hdm.cefx.server.ServerObject;
import de.hdm.cefx.server.SessionData;
import de.hdm.cefx.util.CEFXUtil;
import de.hdm.cefx.util.XMLHelper;

/**
 * The implementing class of the CEFXController interface.
 * 
 * @author Ansgar Gerlicher
 * @author Michael Voigt
 * @author Dirk Hering
 * @author Sven Bendel
 */
public class CEFXControllerImpl implements CEFXController {

	// Create the logger object
	private static final Logger LOG = Logger.getLogger(CEFXControllerImpl.class.getName());

	// The DOM Adapter is the connection to the editing application
	CEFXDOMAdapter da;

	// The document that is currently edited on
	Document doc;

	// The factory that is used to create the document on the client site
	Object documentFactory;

	// The original URI to the document as it is used by the client
	String orgURI;

	// The new URI to the actually loaded document, after it was parsed by the
	// server
	// and stored locally at the client site
	String newURI;

	// The connection to the server and other clients
	NetworkController nc;

	// The ConcurrencyController takes care of the executed operations
	ConcurrencyController cc;

	// The client object
	CEFXClient client;

	// The AwarenessController takes care of the AwarenessWidgets etc.
	AwarenessController ac;
	
	/**
	 * Class constructor. The CEFXController is initialised by the
	 * CEFXDOMAdapter and provided with a reference to it.
	 * 
	 * @param impl
	 *            reference to the CEFXDOMAdapter.
	 */
	public CEFXControllerImpl(CEFXDOMAdapterImpl impl) {
		LOG.setLevel(Level.ALL);
		da = impl;
		client = new CEFXClientImpl();
	}
	
	public CEFXDOMAdapter getCEFXDOMAdapter() {
		return da;
	}
	
	public void setAwarenessController(AwarenessController awarenessController) {
		ac = awarenessController;
		ac.setCEFXController(this);
	}

	public void setNetworkController(NetworkController networkController) {
		nc = networkController;
		nc.setCEFXController(this);		
	}
	
	public void setConcurrencyController(ConcurrencyController concurrencyController) {
		cc = concurrencyController;
		cc.setOperationExecutor(this);
		
		cc.getStateVector().setState(0, 0);
		cc.getStateVector().setState(client.getID(), 0);
	}	
	
	public void loadAwarenessController(String clazz) {
		ac = (AwarenessController) createObjectForClass(clazz);
		ac.setCEFXController(this);
	}

	public void loadNetworkController(String clazz) {
		nc = (NetworkController) createObjectForClass(clazz);
		nc.setCEFXController(this);		
	}
	
	public void loadConcurrencyController(String clazz) {
		cc = (ConcurrencyController) createObjectForClass(clazz);
		cc.setOperationExecutor(this);
		
		cc.getStateVector().setState(0, 0);
		cc.getStateVector().setState(client.getID(), 0);
	}	
	
	public void setJabberHost(String host) {
		JabberClient.getInstance().setHost(host);
	}

	public void setJabberUserName(String name) {
		JabberClient.getInstance().setUserName(name);
	}

	public void setJabberPassword(String password) {
		JabberClient.getInstance().setPassword(password);
	}

	public void setJabberPort(int port) {
		JabberClient.getInstance().setPort(port);
	}

	public void setJabberResourceSuffix(String resourceSuffix) {
		JabberClient.getInstance().setResourceSuffix(resourceSuffix);
	}

	public void setJabberResource(String resource) {
		JabberClient.getInstance().setResource(resource);
	}
	
	public void setJabberServiceName(String serviceName) {
		JabberClient.getInstance().setServiceName(serviceName);
	}

	public boolean jabberConnect() {
		boolean b=JabberClient.getInstance().connect();
		if (b) {
			System.out.println(JabberClient.getInstance().getUserName()+" (Client) connected");
		} else {
			System.out.println(JabberClient.getInstance().getUserName()+" (Client) NOT connected");
		}
		return b;
	}

	/**
	 * Initialises the CEFXController.
	 */
	public void init() {
		// After all AwarenessListeners are created, now the AwarenessWidgets
		// can be created
//		initAwarenessWidgets();
		client.init();
	}

	/**
	 * Initialises the awareness widgets. (Use AwarenessController.registerWidget() from your app!)
	 */
	private void initAwarenessWidgets() {
//		LOG.info("create the Awareness Widgets");
//		AwarenessExtension acConfiguration = (AwarenessExtension) registry.getExtensionPointConfigurationItem(ExtensionRegistry.AWARENESS_EXTENSION);
//		List<AwarenessWidget> widgetConfigs = acConfiguration.getAwarenessWidget();
//		if (widgetConfigs.size() <= 0) {
//			LOG.log(Level.INFO, "Awareness Widget List is empty. No widgets are initialized");
//			return;
//		}
//
//		for (AwarenessWidget widget : widgetConfigs) {
//			String wclassname = widget.getClazz();
//			de.hdm.cefx.awareness.AwarenessWidget aw = (de.hdm.cefx.awareness.AwarenessWidget) createObjectForClass(wclassname);
//			ac.registerWidget(aw);
//		}
	}

	/**
	 * Creates an object for a given class name.
	 * 
	 * @param ncClazz
	 *            the name of the class to create an object for.
	 * @return the created object.
	 */
	private Object createObjectForClass(String ncClazz) {

		Object object = null;
		try {
			Class ncClass = Class.forName(ncClazz);
			object = ncClass.newInstance();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return object;
	}
	
	private int getMaxIDCounter(Document doc,Node node,int val,int clientID) {
		String id=CEFXUtil.getNodeId(node);
		int len=id.length();
		String counter=id.substring(0, len-2);
		String scid=id.substring(len-2, len);
		
		int cid=Integer.parseInt(scid, 16);
		if (cid==clientID) {			
			int tmp=Integer.parseInt(counter, 16);
			if (tmp>val) {
				val=tmp;
			}
		}
				
		NodeList nl = node.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			
			if (n.getNodeType()==Node.ELEMENT_NODE) {
				int v=getMaxIDCounter(doc,n,val,clientID);
				if (v>val) {
					val=v;
				}
			}
		}		
		
		return val;
	}
	
	public Boolean openDocument(int docID) {
		// TODO obsolete, see joinSession()
		if (docID<0) return false;
		cc.setCollaborationReady(false);
		cc.reset();
		
		SessionData sdata=nc.openDocument(docID);
		if (sdata==null) {
			return false;
		}
		DocumentData data=nc.loadDocument();
		if (data==null) {
			return false;
		}
		StateVector sv=data.getStateVector();
		cc.setStateVector(sv);
		cc.setSession(sdata.getSession());		
		doc=cc.setDocumentData(data);
		int counter=getMaxIDCounter(doc,doc.getDocumentElement(),0,sdata.getClientID());
		client.setCounter(counter);
		da.setDocument(doc);
		
		cc.setCollaborationReady(true);
		return true;
	}
	
	public Boolean leaveSession(String sessionName) {
		if ((sessionName == null) || (sessionName.equals("")))
			return false;
		boolean result = nc.leaveSession(sessionName);
		if (result) {
			cc.setCollaborationReady(false);
			cc.reset();
		}
		return result;
	}
	
	public Boolean joinSession(String sessionName) {
		if ((sessionName == null) || (sessionName.equals("")))
			return false;
		cc.setCollaborationReady(false);
		cc.reset();

		SessionData sdata = nc.joinSession(sessionName);
		if (sdata == null) {
			return false;
		}
		DocumentData data = nc.loadDocument();
		if (data == null) {
			return false;
		}
		StateVector sv = data.getStateVector();
		cc.setStateVector(sv);
		cc.setSession(sdata.getSession());
		doc = cc.setDocumentData(data);
		int counter = getMaxIDCounter(doc, doc.getDocumentElement(), 0, sdata
				.getClientID());
		client.setCounter(counter);
		da.setDocument(doc);

		// basic implementation for awareness events for first class nodes (i.e. nodes being direct children of the cefx root element)
		Node mainElement = null;
		int mainElementsCount = doc.getChildNodes().getLength();
		for (int i = 0; i < mainElementsCount; i++) {
			if (doc.getChildNodes().item(i) instanceof Element) {
				mainElement = doc.getChildNodes().item(i);
				break;
			}
		}
		int childrenCount = mainElement.getChildNodes().getLength();
		for (int i = 0; i < childrenCount; i++) {
			Node child = mainElement.getChildNodes().item(i);
			if (child instanceof Element) {
				Operation o = ((CEFXDOMAdapterImpl)da).createInsertUnder(mainElement, child, null, NodePosition.INSERT_BEFORE);
				AwarenessEvent ae = new AwarenessEvent(AwarenessEventTypes.OPERATION_EXECUTION.toString(), "", new OperationData(o), null);
				ac.awarenessEvent(ae);
			}
		}
		
		cc.setCollaborationReady(true);
		LOG.info("document state: " + XMLHelper.getDocumentString(da.getDocument(), true));
		return true;
	}

	public Vector<ServerObject> listFiles() {
		return nc.listFiles();
	}
	
	public int getDocumentID(String name,Vector<ServerObject> files) {
		int ii;
		
		for (ii=0; ii<files.size(); ii++) {
			if (files.elementAt(ii).getName().equals(name)) {
				return files.elementAt(ii).getId();
			}
		}
		
		return -1;
	}
	
	/* (non-Javadoc)
	 * @see de.hdm.cefx.concurrency.operations.OperationExecutor#getName()
	 */
	public String getName() {

		return client.getName();
	}

	/* (non-Javadoc)
	 * @see de.hdm.cefx.concurrency.operations.OperationExecutor#getIdentifier()
	 */
	public int getIdentifier() {

		return this.client.getID();
	}

	/* (non-Javadoc)
	 * @see de.hdm.cefx.concurrency.operations.OperationExecutor#notifyOfNotSupportedOperation(de.hdm.cefx.concurrency.operations.Operation)
	 */
	public void notifyOfNotSupportedOperation(Operation o) {
	}

	/* (non-Javadoc)
	 * @see de.hdm.cefx.concurrency.operations.OperationExecutor#getNetworkController()
	 */
	public NetworkController getNetworkController() {

		return nc;
	}

	/* (non-Javadoc)
	 * @see de.hdm.cefx.CEFXController#getClient()
	 */
	public CEFXClient getClient() {

		return this.client;
	}

	/* (non-Javadoc)
	 * @see de.hdm.cefx.CEFXController#getConcurrencyController()
	 */
	public ConcurrencyController getConcurrencyController() {

		return cc;
	}

	/* (non-Javadoc)
	 * @see de.hdm.cefx.CEFXController#executeOperation(de.hdm.cefx.concurrency.operations.Operation)
	 */
	public boolean executeOperation(Operation operation) {
		boolean result = this.cc.executeLocalOperation(operation);
		System.out.println("document state: " + XMLHelper.getDocumentString(da.getDocument(), true));
		return result;
	}

	/* (non-Javadoc)
	 * @see de.hdm.cefx.CEFXController#executeRemoteOperation(de.hdm.cefx.concurrency.operations.Operation)
	 */
	public void executeRemoteOperation(Operation operation) {
		final String node=operation.getTargetId();
		
		LOG.log(Level.INFO, "CEFXControllerImpl.executeRemoteOperation()");
		if (!cc.executeRemoteOperation(operation)) {
			LOG.log(Level.SEVERE, "Could not execute remote operation: " + operation);
		}
//		LOG.log(Level.INFO, "document state: " + XMLHelper.getDocumentString(da.getDocument(), true));
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {

			public void run() {
				da.refresh();				
//				CEFXDOMAdapterImpl dai=(CEFXDOMAdapterImpl)da;
//				dai.signalNodeModified(node);
			}

		};
		timer.schedule(task, 100); // TODO needed 100ms delay?
		System.out.println("document state: " + XMLHelper.getDocumentString(da.getDocument(), true));
	}

	/* (non-Javadoc)
	 * @see de.hdm.cefx.CEFXController#notifyOfNewClientInSession(de.hdm.cefx.client.CEFXClient)
	 */
	public void notifyOfNewClientInSession(CEFXClient client) {
		if (!cc.getStateVector().containsKey(client.getID())) {
			cc.getStateVector().put(client.getID(), 0);
		}
	}
	
	public void notifyOfDisconnectedClientInSession(CEFXClient client) {
		if (cc.getStateVector().containsKey(client.getID())) {
			cc.getStateVector().remove(client.getID());
			cc.removeClientFromLastStateVectors(client.getID());
			cc.removeClientFromHistoryBufferStateVectors(client.getID());
		} else {
			System.out.println("Couldn't remove client " + client.getName() + " with ID " + client.getID() + " from state vector, as it didn\'t contain this ID!");
		}
	}

	/* (non-Javadoc)
	 * @see de.hdm.cefx.CEFXController#getAwarenessController()
	 */
	public AwarenessController getAwarenessController() {
		return ac;

	}

	public Vector<String> getServerList(String name) {
		Vector<String> jids=JabberClient.getInstance().getRosterJIDs();
		Vector<String> result=new Vector<String>();
		
		if (name==null) {
			name="cefxserver";
		}
		
		int ii;
		for (ii=0; ii<jids.size(); ii++) {
			String tmp=jids.elementAt(ii);
			if (tmp.endsWith(name)) {
				result.add(tmp);
			}
		}
		
		return result;
	}

	public boolean uploadDocument(String path) {
		Document document=null;
		File file=new File(path);
		if (!file.exists()) return false;
		
		DocumentBuilder builder;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			document = builder.parse(new File(path));
			
			/*Document temporaryDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			TransformerFactory.newInstance().newTransformer().transform(new DOMSource(document), new DOMResult(temporaryDoc));
			document=temporaryDoc;*/		
			
			return nc.uploadDocument(document, file.getName());
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return false;
	}
	
	public boolean uploadDocument(String fileName, InputStream inputStream) {
		Document document = null;
		if (inputStream == null) return false;
		DocumentBuilder builder;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			document = builder.parse(inputStream);
			return nc.uploadDocument(document, fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return false;
	}

	public boolean connect(String serverJid) {
		return nc.connect(serverJid);
	}
	
	public Document getDocument() {
		return doc;
	}
}
