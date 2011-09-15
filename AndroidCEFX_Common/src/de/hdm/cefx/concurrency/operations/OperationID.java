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

public class OperationID implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int       SVSum; //StateVectorSum
	private int       DOMLevel;
	private Operation op;

	public OperationID(Operation o) {
		op=o;
		SVSum=o.getStateVector().getSumOfStates();
		DOMLevel=0;
	}

	public int getSVSum() {
		return SVSum;
	}

	public void setSVSum(int sum) {
		SVSum = sum;
	}

	public int getDOMLevel() {
		return DOMLevel;
	}

	public void setDOMLevel(int level) {
		DOMLevel = level;
	}

	public Operation getOp() {
		return op;
	}

	public void setOp(Operation op) {
		this.op = op;
	}

	public int getClientID() {
		return op.getClientId();
	}

	public int getOperationPriority() {
		int val=0;
		switch (op.getType()) {
			case Operation.UPDATE:
				val=1;
				break;
			case Operation.INSERT:
				val=2;
				break;
			case Operation.DELETE:
				val=3;
				break;
		}
		return val;
	}

	public int getUpdateOperationPriority() {
		if (op.getType()==Operation.UPDATE) {
			UpdateOperationImpl uo=(UpdateOperationImpl)op;
			return uo.getDISOperation().getOperation();
		}
		return 0;
	}
}
