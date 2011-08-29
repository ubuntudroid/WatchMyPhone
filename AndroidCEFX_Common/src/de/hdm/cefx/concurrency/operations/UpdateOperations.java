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
 * @author Michael Voigt
 */
package de.hdm.cefx.concurrency.operations;

import java.io.Serializable;


@SuppressWarnings("serial")
public class UpdateOperations implements Serializable {

	public static final int DELETE = 0;
	public static final int INSERT = 1;
	public static final int SET = 2;

	public static final int TEXT = 0;
	public static final int ATTRIBUT = 1;

	protected int           operation;
	protected int           nodeType;

	protected String        attributName;
	protected NodePosition  nodePosition;
	protected UpdateOperationImpl parent;

	UpdateOperations() {
		operation=0;
		nodeType=0;
		attributName=null;
		nodePosition=null;
	}

	public boolean isReady() {
		if (nodePosition==null) return false;
		if (nodeType==UpdateOperations.ATTRIBUT) {
			if (attributName==null) return false;
			if ("".equals(attributName)) return false;
		}
		return true;
	}

	public void undoTransformation() {
	}

	public int getOperation() {
		return operation;
	}

	public void setOperation(int operation) {
		this.operation = operation;
	}

	public int getNodeType() {
		return nodeType;
	}

	public void setNodeType(int nodeType) {
		this.nodeType = nodeType;
	}

	public String getAttributName() {
		return attributName;
	}

	public void setAttributName(String attributName) {
		this.attributName = attributName;
	}

	public NodePosition getNodePosition() {
		return nodePosition;
	}

	public void setNodePosition(NodePosition nodePosition) {
		this.nodePosition = nodePosition;
	}

	public UpdateOperationImpl getParent() {
		return parent;
	}

	public void setParent(UpdateOperationImpl parent) {
		this.parent = parent;
	}

}
