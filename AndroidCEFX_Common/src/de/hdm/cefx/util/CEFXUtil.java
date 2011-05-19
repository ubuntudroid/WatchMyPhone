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
 */

package de.hdm.cefx.util;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.hdm.cefx.client.CEFXClient;
import de.hdm.cefx.dom.adapter.CEFXDOMAdapter;

/**
 * A helper class to ease the use of CEFX.
 * @author Ansgar Gerlicher
 * @author Michael Voigt
 * @author Dirk Hering
 */
public class CEFXUtil {

	public static final String CEFX_NAMESPACE = "http://www.hdm-stuttgart.de/~gerlicher/cefx";
	public static final String CEFX_PREFIX = "cefx";
	public static final String CEFXUID = "cefx:uid";
	public static final String CEFX_LOCKED_ATTR_NAME = "CEFXLOCKED";

	private static CEFXDOMAdapter domAdapter;


	/**
	 * Sets the CEFXDOMAdapter.
	 * @param adapter reference to the CEFXDOMAdapter.
	 */
	public static void setDOMAdapter(CEFXDOMAdapter adapter) {
		domAdapter = adapter;
	}

	/**
	 * Retrieves a document Identifier from a given URI to the document location
	 * This is a helper Method used in the Server and Client in order to identify a
	 * Document independently of its location in the file system.
	 * This Method is under improval.
	 * @param uri the URI to the document location.
	 * @return the document identifier.
	 */
	public static String getDocumentIdentifier(String uri) {
		//Parse the documentURI and generate a document ID
		String docID = uri.substring(uri.lastIndexOf("/")+1,uri.length());
		return docID;
	}



	/**
	 * Retrieves the CEFXDOMAdapter.
	 * @return the CEFXDOMAdapter.
	 */
	public static CEFXDOMAdapter getDOMAdapter() {
		return domAdapter;

	}

	/**
	 * Creates a new UUID to be later used for a node.
	 * @param clientID the client object.
	 * @param counter the counter value to use.
	 * @return the UUID.
	 */
	public static String newUUID(int clientID,int counter) {
		String res=Integer.toHexString(clientID);
		if (res.length()<2) {
			res="0"+res;
		}
		res=Integer.toHexString(counter)+res;
		return res;
	}

	/**
	 * Adds a UUID to the given node and all its children recursively.
	 * @param doc the owner document (needed for attribute creation)
	 * @param node the node to decorate
	 * @param isRoot indicates, whether the given node is the root document element
	 * @param client the client who initially inserts this node
	 */
	public static void addUIDToNode(Document doc, Node node, boolean isRoot, CEFXClient client) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element e = (Element) node;
			
			// TODO needed to check if there are CEFX IDs already?
			if (isRoot) {
				e.setAttribute("xmlns:" + CEFXUtil.CEFX_PREFIX, CEFXUtil.CEFX_NAMESPACE);
			}
			
			Attr uid = doc.createAttributeNS(CEFXUtil.CEFX_NAMESPACE, CEFXUtil.CEFXUID);
			String id = CEFXUtil.newUUID(client.getID(), client.getCounter());
			uid.setValue(id);
			e.setAttributeNodeNS(uid);

			NodeList nl = node.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				addUIDToNode(doc, n, false, client);
			}
		}
	}
	
	/**
	 * Checks if the given node is locked.
	 * @param element the node that is to be checked.
	 * @return true if the node is locked.
	 */
	public static boolean isLocked(Element element)
	{
		String locked = element.getAttributeNS(CEFX_NAMESPACE, CEFX_LOCKED_ATTR_NAME);
		System.out.println("CEFXUtil.isLocked() "+locked+" element: "+element);
		if (locked.equals("")||locked == null) {
			// The node does not carry the Lock Attribute. It is not locked
			return false;
		}
		else
		{
			return true;
		}
	}

	/**
	 * Retrieves the client id of the client that locked the given node.
	 * @param element the node that is locked.
	 * @return the id of the locking client.
	 */
	public static String getLockerId(Element element)
	{
		return element.getAttributeNS(CEFX_NAMESPACE, CEFX_LOCKED_ATTR_NAME);

	}

	/**
	 * Allows to retrieve the UUID of the node.
	 * @param node node to be modified.
	 * @return the UUID of the node.
	 */
	public static String getNodeId(Node node) {
		String nodeId = null;
		NamedNodeMap nnm = node.getAttributes();
		if (nnm != null) {
			Attr a=(Attr)nnm.getNamedItemNS(CEFX_NAMESPACE, CEFXUID);
			if (a==null) {
				String name = CEFXUID;
				a=(Attr)nnm.getNamedItem(name);
			}
			if (a!=null) {
				nodeId = a.getNodeValue();
			}
		}
		return nodeId;
	}
	
	public static boolean compareNode(Node n1, Node n2, Document document) {
		String s1=CEFXUtil.getNodeId(n1);
		String s2=CEFXUtil.getNodeId(n2);
		boolean b=false;
		if ((s1!=null) && (s2!=null)) {
			if (s1.equals(s2)) {
				b=true;
			}
		}
		return b;
	}
}
