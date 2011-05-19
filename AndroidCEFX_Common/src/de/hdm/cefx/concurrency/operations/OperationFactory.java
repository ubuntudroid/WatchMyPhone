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
 */
package de.hdm.cefx.concurrency.operations;

import org.w3c.dom.Node;

import de.hdm.cefx.client.CEFXClient;

/**
 * Helper class to create Operation objects.
 *
 * @author Ansgar Gerlicher
 *
 */
public abstract class OperationFactory {

	/**
	 * Creates an InsertOperation.
	 *
	 * @param node
	 *            the node to be inserted.
	 * @param pos
	 *            the position where the node should be inserted.
	 * @param sv
	 *            the operations state vector.
	 * @param client
	 *            the client that issues this operation.
	 * @return the insert operation.
	 */
	public static Operation newInsertOperation(final Node node, final NodePosition pos, final StateVector sv, final CEFXClient client) {

		InsertOperationImpl op = new InsertOperationImpl(node, pos, (StateVector) sv.clone(), client.getName(), client.getID());
		return op;

	}

	/**
	 * Creates a DeleteOperation.
	 *
	 * @param targetNodeId
	 *            the UUID of the node to be deleted.
	 * @param sv
	 *            the state vector of this operation.
	 * @param client
	 *            the client that issued this operation.
	 * @return the delete operation.
	 */
	public static Operation newDeleteOperation(final String targetNodeId, final StateVector sv, final CEFXClient client,int level) {
		DeleteOperationImpl op = new DeleteOperationImpl(targetNodeId, (StateVector) sv.clone(), client.getName(), client.getID(),level);
		return op;

	}

	/**
	 * Creates an update operation.
	 *
	 * @param targetNodeId
	 *            the UUID of the node to be updated.
	 * @param modification
	 *            the modification set that will be applied.
	 * @param sv
	 *            the operations state vector.
	 * @param client
	 *            the client that issues this operation.
	 * @return the update operation.
	 */
/*	public static Operation newUpdateOperation(final String targetNodeId, final Node modification, final StateVector sv, final CEFXClient client) {

		UpdateOperationImpl op = new UpdateOperationImpl(targetNodeId, new NodeModification(modification), (StateVector) sv.clone(), client.getName(), client.getID());
		return op;

	}*/

	public static Operation newUpdateOperation(UpdateOperations disOperation, final StateVector sv, final CEFXClient client) {

		UpdateOperationImpl op = new UpdateOperationImpl(disOperation, (StateVector) sv.clone(), client.getName(), client.getID());
		return op;
	}


}
