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
 * @author Dirk Hering
 */
package de.hdm.cefx.concurrency.operations;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import de.hdm.cefx.exceptions.NodeNotFoundException;
import de.hdm.cefx.util.DOM3Methods;

/**
 * Implementing class of the InsertOperation interface.
 * 
 * Be careful with serializing this class as it incorporates a Node which will
 * *not* be serialized as it is simply not serializable! When trying to pass this
 * class via IPC on Android make sure, that you wrap it in a ParcelableAwarenessEvent,
 * as this class will make sure, that the operation gets transformed to a serializable
 * {@link UpdateInsertOperation}.
 *
 * @author Ansgar Gerlicher
 * @author Dirk Hering
 * @author Sven Bendel
 *
 */
@SuppressWarnings("serial")
public class InsertOperationImpl implements InsertOperation {

	transient private Node insertNode = null;

	private final NodePosition insertPosition;

	private StateVector stateVector;

	private final String clientName;

	private int clientId;

	private final int type;

	private boolean discarded = false;

	private boolean isMove = false;

	private OperationID opID=null;

	/**
	 * Class constructor.
	 *
	 * @param newnode
	 *            a reference to the org.w3c.dom.Node object that is to be
	 *            inserted in the document.
	 * @param pos
	 *            a reference to the NodePosition object that defines the
	 *            location where to insert the new node.
	 * @param sv
	 *            a reference to the StateVector containing the initial state
	 *            vector values for this operation.
	 * @param name
	 *            the name of the client that creates this operation as String.
	 * @param identifier
	 *            the identifier of the client that creates this operation.
	 */
	protected InsertOperationImpl(Node newnode, NodePosition pos, StateVector sv, String name, int identifier) {
		stateVector = (StateVector) sv.clone();
		clientName = name;
		clientId = identifier;
		type = INSERT;
		insertNode = newnode;
		insertPosition = pos;
		opID=new OperationID(this);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.concurrency.operations.Operation#execute(de.hdm.cefx.concurrency.operations.ExecutionContext)
	 */
	public synchronized boolean execute(ExecutionContext context) {

		Node parent;
		try {
			parent = context.getNodeForId(insertPosition.parentNodeId);
			DOM3Methods.adoptNode(parent.getOwnerDocument(), insertNode, true);
		} catch (NodeNotFoundException e1) {
			e1.printStackTrace();
		}

		context.refreshNodeMap();
		if (discarded)
			return true;
		try {
			System.out.println("InsertOperationImpl.execute() " + insertPosition.getClass().getName() + " pos: " + insertPosition);
			boolean success = false;

			success = insertNewNodeRelative(context);

			// no exception thrown, node insertion seemed to have worked
			// so insert node in node map
			if (success) {
				context.refreshNodeMap();
				return true;
			} else {

				return false;
			}
		} catch (Exception e) {

			e.printStackTrace();
			return false;

		}

	}

	/**
	 * Inserts the node in the document at the specified relative position.
	 *
	 * @param context
	 *            the context of execution.
	 * @return true if the insert was successful.
	 */
	private boolean insertNewNodeRelative(ExecutionContext context) {

		if (insertNode instanceof Document) {
			insertNode = ((Document) insertNode).getDocumentElement();
		}
		String newNodeUid = null;
		newNodeUid = context.getNodeId(insertNode);

		if (newNodeUid != null && !newNodeUid.equals("")) {
			Node existingNode = null;
			try {
				if (context.existsNode(newNodeUid)) {
					existingNode = context.getNodeForId(newNodeUid);
				}
			} catch (NodeNotFoundException e) {

				e.printStackTrace();
			}

			if (existingNode != null) {

				insertNode = existingNode;
				isMove = true;

			}
		}
		NodePosition pos = insertPosition;
		Node parent = null;
		Node fixNode = null;
		try {
			parent = context.getNodeForId(pos.getParentNodeId());
			if (pos.getFixNodeId() == null || pos.getFixNodeId().equals("") || pos.getFixNodeId().equals("null")) {
				return appendChild(parent, insertNode);
			} else {
				fixNode = context.getNodeForId(pos.getFixNodeId());
				if (pos.getRelativeInsertPosition() == NodePosition.INSERT_BEFORE) {
					// insert before fixNode
					return insertNode(insertNode, parent, fixNode);
				} else {
					// insert after fixNode
					Node nextNode = null;
					// Android workaround
					try {
						nextNode = fixNode.getNextSibling();
					} catch (IndexOutOfBoundsException e) {}
					if (nextNode == null) {
						return appendChild(parent, insertNode);
					} else {
						return insertNode(insertNode, parent, nextNode);
					}
				}
			}
		} catch (NodeNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Inserts the given node in the given parent node before (to the left of) the given
	 * rightsibling.
	 *
	 * @param insertNode
	 *            the node to be inserted.
	 * @param parent
	 *            the parent node.
	 * @param rightSibling
	 *            the fixnode to the right of the new inserted node.
	 * @return true if the insertion was successful.
	 */
	private boolean insertNode(Node insertNode, Node parent, Node rightSibling) {
		if (insertNode instanceof Document) {
			insertNode = ((Document) insertNode).getDocumentElement();
		}
		Node adoptedNode = null;
		Document ownerDoc = parent.getOwnerDocument();
		if (!isMove) {
			adoptedNode = DOM3Methods.adoptNode(ownerDoc, insertNode, true);
		} else {
			// first remove the child and then insert it again at the new
			// position
			parent.removeChild(insertNode);
			parent.insertBefore(insertNode, rightSibling);
			return true;
		}
		try {
			if (adoptedNode != null) {

				parent.insertBefore(adoptedNode, rightSibling);
			} else {

				Document doc = parent.getOwnerDocument();
				copyNodeComplete(doc, insertNode, parent);

			}

		} catch (DOMException e) {

			e.printStackTrace();
			return true;

		}
		return true;
	}

	/**
	 * Appends a child node to the given parent node.
	 *
	 * @param parent
	 *            the parent node to append the child at.
	 * @param insertNode
	 *            the node to insert.
	 * @return true if the insertion was successful.
	 */
	private boolean appendChild(Node parent, Node insertNode) {
		// Node nodecopy = insertNode.cloneNode(true);
		if (insertNode instanceof Document) {
			insertNode = ((Document) insertNode).getDocumentElement();
		}
		Node node = null;
		Document ownerDoc = parent.getOwnerDocument();
		if (!isMove) {
			node = DOM3Methods.adoptNode(ownerDoc, insertNode, true);
		} else {
			// first remove the child and then append it again at the new
			// position
			parent.removeChild(insertNode);
			parent.appendChild(insertNode);
			return true;
		}

		try {
			if (node != null) {
				parent.appendChild(node);
			} else {
				// this has to be done recursively
				copyNodeComplete(ownerDoc, insertNode, parent);
			}

		} catch (DOMException e) {
			e.printStackTrace();
			return true;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.concurrency.operations.InsertOperation#getInsertPosition()
	 */
	public final NodePosition getInsertPosition() {
		return insertPosition;
	}

	/**
	 * Creates a copy of the given Node object using the local Document objects
	 * DOM methods. This is necessary in order to insert a node of the specific
	 * DOM implementation of the document type.
	 *
	 * @param doc
	 *            the local document.
	 * @param insertNode
	 *            the standard W3C node.
	 * @param parent
	 *            the node parent where to insert the new child.
	 */
	public void copyNodeComplete(Document doc, Node insertNode, Node parent) {

		Node newNode = null;
		if (insertNode.getNodeType() == Node.ELEMENT_NODE) {
			newNode = doc.createElementNS(insertNode.getNamespaceURI(), insertNode.getLocalName());
			NamedNodeMap nnm = insertNode.getAttributes();
			for (int i = 0; i < nnm.getLength(); i++) {
				Attr attr = (Attr) nnm.item(i);
				((Element) newNode).setAttributeNS(attr.getNamespaceURI(), attr.getLocalName(), attr.getValue());
			}

		} else if (insertNode.getNodeType() == Node.TEXT_NODE) {
			Text text = doc.createTextNode(DOM3Methods.getTextContent(insertNode));
			parent.appendChild(text);
		}
		if (insertNode.hasChildNodes()) {
			NodeList nl = insertNode.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node ch = nl.item(i);
				copyNodeComplete(doc, ch, newNode);
			}
		}
		if (newNode != null) {
			parent.appendChild(newNode);
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.concurrency.operations.Operation#getTargetId()
	 */
	public final String getTargetId() {
		return insertPosition.getParentNodeId();
	}

	public String toString() {

		return "I(" + clientName + ")";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.concurrency.operations.Operation#undo(de.hdm.cefx.concurrency.operations.ExecutionContext)
	 */
	public boolean undo(ExecutionContext context) {

		if (discarded)
			return true;
		try {
			boolean success = false;
			// remove by id
			success = removeNodeFromDocument(context);

			// no exception thrown, node insertion seemed to have worked
			// so insert node in node map
			if (success) {
				context.refreshNodeMap();
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;

		}

	}

	/**
	 * Removes the inserted node from the document again as part of the undo
	 * action.
	 *
	 * @param context
	 *            the context of execution.
	 * @return true if the node was removed successfully.
	 */
	private boolean removeNodeFromDocument(ExecutionContext context) {
		Node parent = null;

		// get parent node to remove the node
		try {
			parent = context.getNodeForId(this.insertPosition.getParentNodeId());

			NodeList nl = parent.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);

				if (n.getNodeType() == Node.ELEMENT_NODE) {
					String chuid = context.getNodeId(n);
					System.out.println("InsertOperationImpl.removeNodeFromDocument() " + insertNode);
					if (insertNode.getNodeType() == Node.DOCUMENT_NODE) {
						insertNode = ((Document) insertNode).getDocumentElement();

					}
					String targetUid=context.getNodeId(insertNode);
					if (targetUid == null) {
						System.err.println("InsertOperationImpl.removeNodeFromDocument() uid attr not found " + insertNode.getAttributes() + " node: " + insertNode + " type: " + " "
								+ (insertNode.getNodeType() == Node.ELEMENT_NODE));

					} else {
						if (targetUid.equals(chuid)) {
							parent.removeChild(n);
							return true;
						}
					}
				}
			}
		} catch (NodeNotFoundException e) {
			e.printStackTrace();
			return false;

		}
		return false;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.concurrency.operations.Operation#cloneOperation()
	 */
	public Operation cloneOperation() {
		return new InsertOperationImpl(insertNode, insertPosition, stateVector, clientName, clientId);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.concurrency.operations.Operation#getClientId()
	 */
	public int getClientId() {

		return this.clientId;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.concurrency.operations.Operation#getStateVector()
	 */
	public final StateVector getStateVector() {
		return stateVector;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.concurrency.operations.Operation#getType()
	 */
	public int getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.concurrency.operations.Operation#getClientName()
	 */
	public final String getClientName() {
		return clientName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.concurrency.operations.Operation#setStateVector(de.hdm.cefx.concurrency.operations.StateVector)
	 */
	public void setStateVector(final StateVector sv) {
		stateVector = (StateVector) sv.clone();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.concurrency.operations.Operation#isDiscarded()
	 */
	public boolean isDiscarded() {
		return discarded;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.concurrency.operations.Operation#setDiscarded(boolean)
	 */
	public void setDiscarded(boolean d) {
		discarded = d;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.concurrency.operations.Operation#setClientId(int)
	 */
	public void setClientId(int id) {
		this.clientId = id;

	}

	public Node getInsertNode() {
		return insertNode;
	}

	public OperationID getOperationID() {
		return opID;
	}

	public void setOperationID(OperationID oid) {
		opID=oid;
	}
	
}
