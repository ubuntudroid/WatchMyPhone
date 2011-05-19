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

import java.util.Comparator;

public class OperationIDComparator implements Comparator {

	public int compare(Object arg0, Object arg1) {
		OperationID o1=((Operation)arg0).getOperationID();
		OperationID o2=((Operation)arg1).getOperationID();
		StateVector s1=((Operation)arg0).getStateVector();
		StateVector s2=((Operation)arg1).getStateVector();

		boolean b1=UpdateOperationsProcessor.isTransformationNeeded(s1, s2);
		boolean b2=UpdateOperationsProcessor.isTransformationNeeded(s2, s1);
		if ((b1==true) && (b2==false)) {
			return 1;
		}
		if ((b1==false) && (b2==true)) {
			return -1;
		}

		int tmp;
		tmp=o1.getOperationPriority()-o2.getOperationPriority();
		if (tmp!=0) {
			return tmp;
		}
		if (o1.getOp().getType()==Operation.UPDATE) {
			tmp=o1.getUpdateOperationPriority()-o2.getUpdateOperationPriority();
			if (tmp!=0) {
				return tmp;
			}
		} else if (o1.getOp().getType()==Operation.DELETE) {
			tmp=o2.getDOMLevel()-o1.getDOMLevel();
			if (tmp!=0) {
				return tmp;
			}
		}
		tmp=o1.getSVSum()-o2.getSVSum();
		if (tmp!=0) {
			return tmp;
		}
		return o1.getClientID()-o2.getClientID();
	}

}
