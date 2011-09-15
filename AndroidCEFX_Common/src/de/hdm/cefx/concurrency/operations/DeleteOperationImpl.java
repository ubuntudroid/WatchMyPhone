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
 */
package de.hdm.cefx.concurrency.operations;

import java.io.IOException;

import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.SerializerFactory;
import org.apache.xml.serialize.TextSerializer;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.hdm.cefx.exceptions.NodeNotFoundException;

/**
 * Implementing class of the DeleteOperation interface.
 *
 * @author Ansgar Gerlicher
 * @author Sven Bendel
 *
 */
@SuppressWarnings("serial")
public class DeleteOperationImpl implements DeleteOperation {

	private final String targetId;

	private StateVector stateVector;

	private final String clientName;

	private int clientId;

	private final int type;

	// Remember the position of the node within the Childnodelist
	private Node nextSibling = null;

	private boolean undone = false;

	private Node parent = null;

	private boolean discarded = false;

	private OperationID opID;

	/**
	 * Class constructor.
	 *
	 * @param targetNodeId
	 *            the UUID of the node that is to be deleted.
	 * @param sv
	 *            a reference to the StateVector containing the initial state
	 *            vector values for this operation.
	 * @param cName
	 *            the name of the client that creates this operation as String.
	 * @param cId
	 *            the identifier of the client that creates this operation.
	 */
	protected DeleteOperationImpl(final String targetNodeId, final StateVector sv, final String cName, final int cId,int DOMLevel) {
		targetId = targetNodeId;
		stateVector = (StateVector) sv.clone();
		clientName = cName;
		clientId = cId;
		type = DELETE;
		opID=new OperationID(this);
		opID.setDOMLevel(DOMLevel);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.concurrency.operations.Operation#execute(de.hdm.cefx.concurrency.operations.ExecutionContext)
	 */
	public synchronized boolean execute(ExecutionContext context) {
		context.refreshNodeMap();
		// Log.out(Log.ALL, this,"Executing Delete OperationImpl");
		if (discarded)
			return true;
		Node targetNode = null;
		try {
			targetNode = context.getNodeForId(targetId);

		} catch (NodeNotFoundException e) {

			e.printStackTrace();
			return false;
		}
		if (targetNode != null) {

			parent = targetNode.getParentNode();
			if (parent != null) {
				
				// Android Workaround
				try {
					nextSibling = targetNode.getNextSibling();
				} catch (IndexOutOfBoundsException e) {
					nextSibling = null;
				}

				parent.removeChild(targetNode);
				context.addNodeToDeletedNodeMap(this, targetNode);
				context.refreshNodeMap();
				undone = false;

				return true;
			} else {
				// tried to delete root node!
				// this is not allowed
				// tell user and react correspondingly
				System.out.println("!!!!!!!!!!!!!!!!!!!!!DeleteOperationImpl.execute() parent is null (root document node not allowed to be deleted)");
				return false;
			}

		}
		return false;
	}

	/* (non-Javadoc)
	 * @see de.hdm.cefx.concurrency.operations.Operation#getTargetId()
	 */
	public String getTargetId() {
		return targetId;
	}


	/* (non-Javadoc)
	 * @see de.hdm.cefx.concurrency.operations.Operation#undo(de.hdm.cefx.concurrency.operations.ExecutionContext)
	 */
	public boolean undo(ExecutionContext context) {
		// Log.out(Log.ALL, this, "Undoing Delete OperationImpl: "
		// + this.getTargetId());
		if (discarded)
			return true;
		try {
			boolean success = insertNodeInDocument(context);
			// no exception thrown, node insertion seemed to have worked
			// so insert node in node map
			if (success) {
				context.refreshNodeMap();
				undone = true;
				return true;
			} else {
				// Log.out(Log.ALL, this, context.getName()
				// + "Executing Undo of Delete OperationImpl failed: "
				// + this.getTargetId());
				return false;
			}
		} catch (Exception e) {
			// Log.out(Log.ALL, this, context.getName()
			// + "Executing Undo of Delete OperationImpl failed: "
			// + this.getTargetId());
			e.printStackTrace();
			return false;

		}

	}

	/**
	 * Inserts the insert node into the target node at the specified location
	 * within the document.
	 *
	 * @param context
	 *            the context of execution.
	 * @return true if the node was inserted successfully.
	 */
	private boolean insertNodeInDocument(ExecutionContext context) {
		try {
			Node node = context.getNodeFromDeletedNodeMap(this);
			if (nextSibling != null) {
				if (parent != null) {
					parent.insertBefore(node, nextSibling);
				} else {
					System.out.println("!!ERROR PARENT is Null. Undo not possible. DeleteOperationImpl.insertNodeInDocument()");
				}
			} else {
				if (parent != null) {
					parent.appendChild(node);
				} else {
					System.out.println("!!ERROR PARENT is Null2. Undo not possible. DeleteOperationImpl.insertNodeInDocument()");
				}

			}
		} catch (DOMException e) {

			e.printStackTrace();
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.concurrency.operations.Operation#cloneOperation()
	 */
	public Operation cloneOperation() {
		return new DeleteOperationImpl(targetId, stateVector, clientName, clientId,opID.getDOMLevel());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hdm.cefx.concurrency.operations.Operation#getClientId()
	 */
	public final int getClientId() {
		return clientId;
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
	public final int getType() {
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
	public void setStateVector(StateVector sv) {
		this.stateVector = (StateVector) sv.clone();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	public String toString() {

		// return "D(" + this.getStateVector() + ", uid=" + targetId
		// + ", ORIG:" + clientName+" Undone="+undone+")";

		return "D(" + clientName + " " + this.getStateVector() + "," + hashCode() + " discarded=" + discarded + " undone=" + undone + " target=" + targetId + " )";
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

	public OperationID getOperationID() {
		return opID;
	}

	public void setOperationID(OperationID oid) {
		opID=oid;
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		SerializerFactory fac = SerializerFactory.getSerializerFactory(Method.TEXT);
		TextSerializer serializer = (TextSerializer) fac.makeSerializer(out, new OutputFormat());
		serializer.serialize((Element) parent);
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
	}
}
