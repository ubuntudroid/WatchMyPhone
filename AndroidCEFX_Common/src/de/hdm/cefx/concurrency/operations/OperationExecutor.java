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

import de.hdm.cefx.client.net.NetworkController;

/**
 * The OperationExecutor is an interface to the client, in this case represented
 * by the CEFXController, providing information on the client's name and
 * identifier. It also allows notification of the client if an issued operation
 * is not supported. It depends on the implementation of the
 * ConcurrecyController if an operation is supported or not. The
 * OperationExecutor interface also provides a method that allows notification
 * of the client on real conflicts. A real conflict is one that can not be
 * solved automatically by the ConcurrencyController implementation.
 *
 * @author Ansgar Gerlicher
 *
 */
public interface OperationExecutor {
	/**
	 * This is called by the ConcurrencyController in order to retrieve the name
	 * of the client.
	 *
	 * @return the name of the client.
	 */
	public String getName();

	/**
	 * This is used by the ConcurrencyController to retrieve the client's id
	 * which is necessary, for example, for total ordering.
	 *
	 * @return the id of the client.
	 */
	public int getIdentifier();

	/**
	 * This method can be used to notify the OperationExecutor (Client) of an
	 * operation that is not supported for execution. This could, for example,
	 * be a delete operation that removes the root node of the document, which
	 * does not make sense.
	 *
	 * @param o
	 *            the not supported operation.
	 */
	public void notifyOfNotSupportedOperation(Operation o);

	/**
	 * This method is used to retrieve a reference of the NetworkController.
	 * This is called by the ConcurrencyController in order to access the
	 * NetworkController for propagating operations.
	 *
	 * @return a reference to the NetworkController.
	 */
	public NetworkController getNetworkController();

}
