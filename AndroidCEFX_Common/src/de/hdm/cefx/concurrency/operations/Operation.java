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

import java.io.Serializable;

import de.hdm.cefx.exceptions.OperationExecutionException;

/**
 * The Operation interface is the main interface for all three types of
 * operations and defines the methods that all operations have in common. An
 * additional interface exists for each operation type (update, insert and
 * delete). These interfaces inherit the methods from the Operation interface.
 * The classes UpdateOperationImpl, InsertOperationImpl and DeleteOperationImpl
 * implement those interfaces. The OperationFactory is a utility class for the
 * instantiation of operation objects.
 *
 * @author Ansgar Gerlicher
 *
 */
public interface Operation extends Serializable {

	public static final int INSERT = 0;
	public static final int DELETE = 1;
	public static final int UPDATE = 2;

	/**
	 * The method execute(ExecutionContext context) is called by the
	 * ConcurrencyController in order to execute an operation. The
	 * ConcurrencyController thereby acts as the ExecutionContext.
	 *
	 * @param context
	 *            the context of execution.
	 * @return true if the execution was successfull.
	 * @throws OperationExecutionException
	 */
	public boolean execute(ExecutionContext context) throws OperationExecutionException;

	/**
	 * Allows to retrieve the id of the client that issued this operation.
	 *
	 * @return id of the client
	 */
	public int getClientId();

	/**
	 * Allows to retrieve the State Vector of this operation.
	 *
	 * @return the State Vector of this operation.
	 */
	public StateVector getStateVector();

	/**
	 * The method getType() returns the type (delete, insert or update) of the
	 * operation. This is used, for example in the DCRP in order to easily
	 * identify the type of an operation.
	 *
	 * @return the type of operation (inser, delete or update).
	 */
	public int getType();

	/**
	 * The method getClientName() returns the name of the client that created
	 * the operation. This is used for debugging purposes and in order to be
	 * able to later identify the operation's originating client if the client
	 * id has been swapped.
	 *
	 * @return the name of the client that issued this operation.
	 */
	public String getClientName();

	/**
	 * Allows to retrieve the UUID of the target node of this operation.
	 *
	 * @return the UUID of the target node.
	 */
	public String getTargetId();

	/**
	 * The undo(ExecutionContext context) method is called by the
	 * ConcurrencyController in order to undo an operation for example when
	 * removing it from the history buffer.
	 *
	 * @param context
	 *            the context of execution.
	 * @return true if the operation was successfully undone.
	 */
	public boolean undo(ExecutionContext context);

	/**
	 * The method <code>cloneOperation()</code> is used to copy an operation
	 * before it is propagated to the other sites. As the NetworkController runs
	 * in its own thread, it might occur that the operation is modified by
	 * another thread before it is eventually transmitted over the network. In
	 * order to make sure that the correct operation's state is transmitted, a
	 * copy of the current operation's values is taken before passing it to the
	 * NetworkController.
	 *
	 * @return a copy of this operation object.
	 */
	public Operation cloneOperation();

	/**
	 * Sets the state vector for this operation.
	 *
	 * @param stateVector
	 *            the state vector for this operation.
	 */
	public void setStateVector(StateVector stateVector);

	/**
	 * If a operation is discarded, its execution does not take any affect.
	 *
	 * @return true if the operation is marked as discarded.
	 */
	public boolean isDiscarded();

	/**
	 * Marks this operation as discarded.
	 *
	 * @param discarded
	 *            true if the operation should be discarded.
	 */
	public void setDiscarded(boolean discarded);

	/**
	 * Sets the id of the client that issued this operation.
	 *
	 * @param id
	 *            the id of the client.
	 */
	public void setClientId(int id);

	public OperationID getOperationID();

	public void setOperationID(OperationID oid);

}
