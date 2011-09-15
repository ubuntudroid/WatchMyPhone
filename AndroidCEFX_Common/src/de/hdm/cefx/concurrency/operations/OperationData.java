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
package de.hdm.cefx.concurrency.operations;

import java.io.Serializable;

/**
 * Bundles an operation with its type and if it is a part of a complex operation indicated by the
 * ComplexOperationStatus.
 * @author Dirk Hering
 * @author Sven Bendel
 */
public class OperationData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The executed operation.
	 */
	private Operation operation;
	
	/**
	 * Additional information on the operation type.
	 */
	private String operationType;
	
	/**
	 * Indicates whether this operation is a part of a complex operation.
	 */
	private ComplexOperationStatus complexOperationStatus;
	
	public OperationData() {
		
	}
	
	public OperationData(Operation operation) {
		this.operation = operation;
	}
	
	public Operation getOperation() {
		return operation;
	}
	
	public void setOperation(Operation operation) {
		this.operation = operation;
	}
	
	public String getOperationType() {
		return operationType;
	}
	
	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}
	
	public ComplexOperationStatus getComplexOperationStatus() {
		return complexOperationStatus;
	}
	
	public void setComplexOperationStatus(
			ComplexOperationStatus complexOperationStatus) {
		this.complexOperationStatus = complexOperationStatus;
	}
}
