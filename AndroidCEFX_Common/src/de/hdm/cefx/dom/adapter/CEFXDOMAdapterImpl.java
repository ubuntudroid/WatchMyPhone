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
package de.hdm.cefx.dom.adapter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import de.hdm.cefx.CEFXController;
import de.hdm.cefx.CEFXControllerImpl;
import de.hdm.cefx.concurrency.operations.NodePosition;
import de.hdm.cefx.concurrency.operations.Operation;
import de.hdm.cefx.concurrency.operations.OperationFactory;
import de.hdm.cefx.concurrency.operations.UpdateDeleteOperation;
import de.hdm.cefx.concurrency.operations.UpdateInsertOperation;
import de.hdm.cefx.concurrency.operations.UpdateOperations;
import de.hdm.cefx.concurrency.operations.UpdateSetOperation;
import de.hdm.cefx.exceptions.NodePositionException;
import de.hdm.cefx.util.CEFXUtil;

/**
 *
 * Implementing class of the CEFXDOMAdapter.
 *
 * @author Ansgar Gerlicher
 * @author Michael Voigt
 * @author Dirk Hering
 *
 */
public class CEFXDOMAdapterImpl implements CEFXDOMAdapter {

	private final Logger LOG = Logger.getLogger(CEFXDOMAdapterImpl.class.getName());

	private static final String cefxReposIdentifier = "CEFXRepository"; //$NON-NLS-1$

	DOMImplementation implementation;

	Object documentFactory;

	Object renderContext;

	Document document;

	CEFXController cefx;

	Thread refreshThread;

	/**
	 * Class constructor.
	 */
	public CEFXDOMAdapterImpl() {
		LOG.setLevel(Level.ALL);

		System.out.println("CEFXDOMAdapterImpl.CEFXDOMAdapterImpl() 2");
		cefx = new CEFXControllerImpl(this);
		CEFXUtil.setDOMAdapter(this);
	}

	public CEFXController getCEFXController() {
		return cefx;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.dom.adapter.CEFXDOMAdapter#setDocumentFactory(java.lang.Object)
	 */
	public void setDocumentFactory(Object factory) {
		documentFactory = factory;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.dom.adapter.CEFXDOMAdapter#Node_appendChild(org.w3c.dom.Node,
	 *      org.w3c.dom.Node)
	 */
	public Node Node_appendChild(Node parent, Node newChild) {
		
		if (isWithinDocument(newChild, document.getDocumentElement())) {
			System.out.println("CEFXDOMAdapterImpl.Node_appendChild() seems to be a move not an insert. The node " + getNodeId(newChild) + " already exists within the document");
		}
		
		if (	(parent == null) || (newChild == null) ||
				(newChild == parent) ||
				isLockedByOtherClient(parent) ||
				(parent.getNodeType() != Node.ELEMENT_NODE)
			) {
			return null;
		}
		
		String nodeId = getNodeId(newChild);
		if (nodeId == null) {
			((Element) newChild).setAttributeNS(
					CEFXUtil.CEFX_NAMESPACE, CEFXUtil.CEFXUID, 
					CEFXUtil.newUUID(cefx.getClient().getID(),cefx.getClient().getCounter()));
		}
		// check if the parent should be transmitted or not
		if (!isWithinDocument(parent, document.getDocumentElement())) {
			System.out.println("CEFXDOMAdapterImpl.Node_appendChild() not within document parent: " + parent + " child: " + newChild);
			return parent.appendChild(newChild);
		}
		Operation insert = createInsertUnder(parent, newChild, null, NodePosition.INSERT_AFTER);
		if (insert == null) {
			System.out.println("CEFXDOMAdapterImpl.Node_appendChild() could not transmit. " + parent + " child " + newChild.getNodeName());
			// parent has no node Id, this means, it has not been added to the
			// document yet.
			return parent.appendChild(newChild);
		}
		cefx.executeOperation(insert);
		return newChild;
	}

	/**
	 * Checks if the given node is within the document represented by the given
	 * root node.
	 *
	 * @param searchNode
	 *            the node we are looking for.
	 * @param root
	 *            the root node of the document.
	 * @return true if it is within the document.
	 */
//TODO ist sehr langsam - getOwnerDocument beider Knoten vergleichen oder root-Knoten vergleichen
	private boolean isWithinDocument(Node searchNode, Node root) {
		if (CEFXUtil.compareNode(searchNode,root,document)) {
//			System.out.println("CEFXDOMAdapterImpl.isWithinDocument() true");
			return true;
		} else {

			if (root.getNodeType() == Document.ELEMENT_NODE) {
//				System.out.println("CEFXDOMAdapterImpl.isWithinDocument() Element " + root.getNodeName());
				NodeList nl = root.getChildNodes();
				for (int i = 0; i < nl.getLength(); i++) {
					if (isWithinDocument(searchNode, nl.item(i))) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Retrieves the UUID of the given node.
	 *
	 * @param node
	 *            the node to get the UUID from.
	 * @return the UUID.
	 */
	private String getNodeId(Node node) {
		return CEFXUtil.getNodeId(node);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.dom.adapter.CEFXDOMAdapter#refresh()
	 */
	public void refresh() {
		if (renderContext == null)
			return;
		Method method;
		try {
			method = renderContext.getClass().getMethod("repaint", new Class[] {});
			method.invoke(renderContext, new Object[] {});
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.dom.adapter.CEFXDOMAdapter#setRenderContext(java.lang.Object)
	 */
	public void setRenderContext(Object rc) {
		renderContext = rc;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.dom.adapter.CEFXDOMAdapter#Node_removeChild(org.w3c.dom.Node,
	 *      org.w3c.dom.Node)
	 */
	public Node Node_removeChild(Node child) {
		Node parent=child.getParentNode();
		if (parent==null) {
			return null;
		}
		if (isLockedByOtherClient(parent)) {
			return child;
		}

		Operation delete = createDeleteUnder(parent, child);
		if (delete == null) {
			return parent.removeChild(child);
		}
		cefx.executeOperation(delete);
		return child;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.dom.adapter.CEFXDOMAdapter#Element_setAttributeNS(org.w3c.dom.Element,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public void Element_setAttributeNS(Element element, String nameSpaceURI, String qualifiedName, String attributeValue) {

		if (isLockedByOtherClient(element)) {
			return;
		}
		Operation modify = createModify(element, nameSpaceURI, qualifiedName, attributeValue);
		if (modify == null) {
			element.setAttributeNS(nameSpaceURI, qualifiedName, attributeValue);
			return;
		}
		cefx.executeOperation(modify);
	}

	/**
	 * Checks if the node target is locked by another client than this.
	 *
	 * @param target
	 *            the node to be checked.
	 * @return true if the node is locked by another client.
	 */
	private boolean isLockedByOtherClient(Node target) {

		NamedNodeMap map = target.getAttributes();
		if (map != null) {
			Node n = map.getNamedItem(CEFXUtil.CEFX_LOCKED_ATTR_NAME);
			if (n == null) {
				return false;
			}
			String locker = n.getNodeValue();
			if (locker == null || locker.equals("")) {
				return false;
			}
			int lockId = Integer.parseInt(locker);
			int thisId = this.cefx.getClient().getID();
			if (lockId == thisId) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}

	}

	/**
	 * Creates an Insert Operation.
	 *
	 * @param parent
	 *            the parent node to insert the new node at.
	 * @param n
	 *            the node to insert.
	 * @param fixChild
	 *            the fix node.
	 * @return the InsertOperation object.
	 */
	public de.hdm.cefx.concurrency.operations.Operation createInsertUnder(Node parent, Node n, Node fixChild, int before) {
		Operation op = null;
		String fixNodeId = null;
		if (n.equals(fixChild)) {
			return null;
		}
		int relativePosition = NodePosition.INSERT_AFTER;
		if (fixChild != null) {
			fixNodeId = getNodeId(fixChild);
			if (fixNodeId != null) {
				// only if we have an addressable node (with cefx uid), the given relative position is taken
				relativePosition = before;
			}
		}
		String targetId = getNodeId(parent);
		if (targetId == null)
			return null;
		try {
			op = OperationFactory.newInsertOperation(n, new NodePosition(targetId, fixNodeId, relativePosition), cefx.getConcurrencyController().getStateVector(), cefx.getClient());
		} catch (NodePositionException e) {
			e.printStackTrace();
		}
		return op;
	}

	/**
	 * Creates a DeleteOperation.
	 *
	 * @param parent
	 *            the parent of the node to delete.
	 * @param n
	 *            the node to delete.
	 * @return the DeleteOperation.
	 */
	private de.hdm.cefx.concurrency.operations.Operation createDeleteUnder(Node parent, Node n) {
		Operation op = null;

		String targetId = getNodeId(n);
		if (targetId == null)
			return null;
		Node root=document.getDocumentElement();
		int count=0;
		while (!CEFXUtil.compareNode(root, n, document)) {
			n=n.getParentNode();
			count=count+1;
		}

		op = OperationFactory.newDeleteOperation(targetId, cefx.getConcurrencyController().getStateVector(), cefx.getClient(),count);

		return op;
	}

	/**
	 * Creates an UpdateOperation
	 *
	 * @param node
	 *            the node to update.
	 * @param nameSpaceURI
	 *            the namespace URI of the node.
	 * @param qualifiedName
	 *            the name of the attribute to add or change.
	 * @param attributeValue
	 *            the value of the attribute to change.
	 * @return
	 */
	private de.hdm.cefx.concurrency.operations.Operation createModify(Node node, String nameSpaceURI, String qualifiedName, String attributeValue) {
		Operation op = null;

		String targetId = getNodeId(node);
		System.out.println("CEFXDOMAdapterImpl.createModify() ");
		if (targetId == null)
			return null;

		Node modNode = node.cloneNode(false);
		((Element) modNode).setAttributeNS(nameSpaceURI, qualifiedName, attributeValue);

		System.out.println("CEFXDOMAdapterImpl.createModify() " + op);
		return op;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.dom.adapter.CEFXDOMAdapter#isCollaborationReady()
	 */
	public boolean isCollaborationReady() {
		return cefx.getConcurrencyController().isCollaborationReady();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.dom.adapter.CEFXDOMAdapter#Element_setAttribute(org.w3c.dom.Element,
	 *      java.lang.String, java.lang.String)
	 */
	public void Element_setAttribute(Element element, String attr, String value) {
		Element_setAttributeNS(element, null, attr, value);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.dom.adapter.CEFXDOMAdapter#closeSession()
	 */
	public void closeSession() {
		// TODO close the connection to the server and the clients and clean up
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.dom.adapter.CEFXDOMAdapter#lockNode(org.w3c.dom.Node)
	 */
	public boolean lockNode(Node node) {
		if (isLockedByOtherClient(node)) {
			return false;
		}
		// the locking of a node is nothing else but adding a new attribute
		// that states that this node is locked.
		if (node instanceof Element) {
			System.out.println("CEFXDOMAdapterImpl.lockNode()");
			final Element element = (Element) node;
			final String lockid = "" + cefx.getClient().getID();
			String locked = element.getAttribute(CEFXUtil.CEFX_LOCKED_ATTR_NAME);
			// check if the node is already locked
			if (locked != null && !locked.equals("")) {
				// Either the node is already locked by another or this client.
				// In both cases it is unnecessary or unwanted to lock it. So we
				// return.
				return false;
			}

			System.out.println("CEFXDOMAdapterImpl.lockNode() create modify operation");
			// Operation modify = createModify(element, CEFX_NAMESPACE,
			// CEFX_LOCKED_ATTR_NAME, lockid);
			Operation modify = createModify(element, null, CEFXUtil.CEFX_LOCKED_ATTR_NAME, lockid);
			if (modify == null) {
				// locking of this node not possible. It may not be part of the
				// document yet?
				return false;
			}
			System.out.println("CEFXDOMAdapterImpl.lockNode() " + modify + " is executed");
			cefx.executeOperation(modify);
			return true;
		}
		// the node is no element node. Only element nodes can be locked
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.dom.adapter.CEFXDOMAdapter#unlockNode(org.w3c.dom.Node)
	 */
	public boolean unlockNode(Node node) {
		if (isLockedByOtherClient(node)) {
			return false;
		}
		// Unlocking means to remove the lock attribute from the node
		if (node instanceof Element) {
			final Element element = (Element) node;
			String locked = element.getAttribute(CEFXUtil.CEFX_LOCKED_ATTR_NAME);
			if (locked == null || locked.equals("")) {
				// The node does not carry the Lock Attribute. It is not locked
				return false;
			}
			Operation removeLock = createModify(element, null, CEFXUtil.CEFX_LOCKED_ATTR_NAME, "");
			if (removeLock == null) {
				// unlocking of this node not possible. It may not be part of
				// the
				// document yet?
				// unlock it anyhow to make shure
				System.out.println("CEFXDOMAdapterImpl.unlockNode() " + removeLock);
				// element.removeAttributeNS(CEFX_NAMESPACE,
				// CEFX_LOCKED_ATTR_NAME);
				element.removeAttribute(CEFXUtil.CEFX_LOCKED_ATTR_NAME);
				return false;
			}
			System.out.println("CEFXDOMAdapterImpl.unlockNode() executing");
			cefx.executeOperation(removeLock);
		}
		// the node is no element node. Only element nodes can be unlocked
		return false;

	}

	/**
	 * Creates an UpdateOperation that will remove the lock attribute and thus
	 * unlock the node.
	 *
	 * @param element
	 *            the node to unlock.
	 * @return the UpdateOperation.
	 */
	private Operation createRemoveLockOperation(Element element) {
		Operation op = null;

		String targetId = getNodeId(element);

		if (targetId == null)
			return null;

		Node modNode = element.cloneNode(false);
		((Element) modNode).removeAttributeNS(CEFXUtil.CEFX_NAMESPACE, CEFXUtil.CEFX_LOCKED_ATTR_NAME);

//TODO reactivate createRemoveLockOperation
//		op = OperationFactory.newUpdateOperation(targetId, modNode, cefx.getConcurrencyController().getStateVector(), cefx.getClient());

		return op;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.dom.adapter.CEFXDOMAdapter#Node_insert(org.w3c.dom.Node,
	 *      org.w3c.dom.Node, org.w3c.dom.Node)
	 */
	public Node Node_insert(Node parent, Node newNode, Node fixNode, int before) {
		
		if (isWithinDocument(newNode, document.getDocumentElement())) {
			System.out.println("Node_insert seems to be a move. The node " + getNodeId(newNode) + " already exists within the document");
		}
		if (	(parent == null) || (newNode == null) ||
				(newNode == fixNode) ||
				(newNode == parent) ||
				(fixNode == parent) ||
				isLockedByOtherClient(parent) ||
				(parent.getNodeType() != Node.ELEMENT_NODE)
			) {
			System.out.println("Node_insert not allowed.");
			return null;
		}
		
		String nodeId = getNodeId(newNode);
		if (nodeId == null) {
//			String newUID = CEFXUtil.newUUID(cefx.getClient().getID(),cefx.getClient().getCounter());
//			((Element) newNode).setAttributeNS(CEFXUtil.CEFX_NAMESPACE, CEFXUtil.CEFXUID, newUID);
			
			// add UUIDs to this node and all its children recursively
			CEFXUtil.addUIDToNode(document, newNode, false, cefx.getClient());
			
		}
		// check if the parent should be transmitted or not
		if (!isWithinDocument(parent, document.getDocumentElement())) {
			return insertFallback(parent, newNode, fixNode, before);
		}
		Operation insert = createInsertUnder(parent, newNode, fixNode, before);
		if (insert == null) {
			// parent e.g. has no node Id, this means, it has not been added to the document yet.
			return insertFallback(parent, newNode, fixNode, before);
		}
		// everything is prepared -> execute the insert operation
		cefx.executeOperation(insert);
		return newNode;
	}
	
	/**
	 * E.g. gets called if there is the parent node ID missing. Just a DOM insertBefore() or appendChild() 
	 * is invoked, depending on the given relative position.
	 * @return Node
	 */
	private Node insertFallback(Node parent, Node newNode, Node fixNode, int before) {
		if (fixNode == null) {
			return null;
		}
		switch (before) {
		case NodePosition.INSERT_BEFORE:
			return parent.insertBefore(newNode, fixNode);
		case NodePosition.INSERT_AFTER:
			Node nextNode = null;
			// Android workaround
			try {
				nextNode = fixNode.getNextSibling();
			} catch (IndexOutOfBoundsException e) {}
			if (nextNode == null) {
				return parent.appendChild(newNode);
			} else {
				return parent.insertBefore(newNode, nextNode);
			}
		}
		return null;
	}
	
	public Attr Document_createAttribute(String name) {
		return null;
	}

	public Element Document_createElement(String tagName) {
		return null;
	}

	public Text Document_createTextNode(String data) {
		return null;
	}

	private NodePosition Element_Attribute_createNodePosition(Element element, String attributName) {
		if (element==null) {
			return null;
		}
		if ((attributName==null) || ("".equals(attributName))) {
			return null;
		}

		String targetId = getNodeId(element);
		if (targetId == null) {
			return null;
		}

		try {
			return new NodePosition(targetId,null,0);
		} catch (NodePositionException e) {
			e.printStackTrace();
		}
		return null;
	}

	private NodePosition Element_Text_createNodePosition(Element element, Element fixNode, int pos) {
		if (element==null) {
			return null;
		}

		String targetId = getNodeId(element);
		if (targetId == null) {
			return null;
		}
		String fixNodeId=null;
		if (fixNode!=null) {
			fixNodeId=getNodeId(fixNode);
		}

		try {
			return new NodePosition(targetId,fixNodeId,pos);
		} catch (NodePositionException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void Element_AttributeDelete(Element element, String attr, int pos,int length) {
		if ((pos<0) || (length<0)) {
			return;
		}
		NodePosition n=Element_Attribute_createNodePosition(element,attr);
		if (n==null) {
			return;
		}
		UpdateDeleteOperation dis=new UpdateDeleteOperation(pos,length,UpdateOperations.ATTRIBUT,n,attr);
		Operation o=OperationFactory.newUpdateOperation(dis, cefx.getConcurrencyController().getStateVector(), cefx.getClient());
		cefx.executeOperation(o);
	}

	public void Element_AttributeInsert(Element element, String attr,String value, int pos) {
		if (pos<0) {
			return;
		}
		NodePosition n=Element_Attribute_createNodePosition(element,attr);
		if (n==null) {
			return;
		}

		UpdateOperations dis=new UpdateInsertOperation(value,pos,UpdateOperations.ATTRIBUT,n,attr);
		Operation o=OperationFactory.newUpdateOperation(dis, cefx.getConcurrencyController().getStateVector(), cefx.getClient());
		cefx.executeOperation(o);
	}

	public void Element_AttributeSet(Element element, String attr, String value) {
		NodePosition n=Element_Attribute_createNodePosition(element,attr);
		if (n==null) {
			return;
		}
		UpdateSetOperation dis=new UpdateSetOperation(value,UpdateOperations.ATTRIBUT,n,attr);
		Operation o=OperationFactory.newUpdateOperation(dis, cefx.getConcurrencyController().getStateVector(), cefx.getClient());
		cefx.executeOperation(o);
	}

	public Attr Element_setAttributeNode(Attr newAttr) {
		return null;
	}

	public void Element_TextDelete(Element parent, Element fixNode,int beforeAfter, int pos, int length) {
		if ((pos<0) || (length<0)) {
			return;
		}
		NodePosition n=Element_Text_createNodePosition(parent,fixNode,beforeAfter);
		if (n==null) {
			return;
		}
		UpdateDeleteOperation dis=new UpdateDeleteOperation(pos,length,UpdateOperations.TEXT,n,null);
		Operation o=OperationFactory.newUpdateOperation(dis, cefx.getConcurrencyController().getStateVector(), cefx.getClient());
		cefx.executeOperation(o);
	}

	public void Element_TextInsert(Element parent, Element fixNode,int beforeAfter, String value, int pos) {
		if (pos<0) {
			return;
		}
		NodePosition n=Element_Text_createNodePosition(parent,fixNode,beforeAfter);
		if (n==null) {
			return;
		}

		UpdateOperations dis=new UpdateInsertOperation(value,pos,UpdateOperations.TEXT,n,null);
		Operation o=OperationFactory.newUpdateOperation(dis, cefx.getConcurrencyController().getStateVector(), cefx.getClient());
		cefx.executeOperation(o);
	}

	public void Element_TextSet(Element parent, Element fixNode,int beforeAfter, String value) {
		NodePosition n=Element_Text_createNodePosition(parent,fixNode,beforeAfter);
		if (n==null) {
			return;
		}
		UpdateSetOperation dis=new UpdateSetOperation(value,UpdateOperations.TEXT,n,null);
		Operation o=OperationFactory.newUpdateOperation(dis, cefx.getConcurrencyController().getStateVector(), cefx.getClient());
		cefx.executeOperation(o);
	}

// can be used for transparent adaption (called by CEFXControllerImpl):
//	public native static void signalNodeModified(String node);
}
