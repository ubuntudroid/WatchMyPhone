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

package de.hdm.cefx.client.net;

public class DeleteExtension extends OperationExtension {

	private int DOMLevel;

	public String getNamespace() {
		return super.getNamespace()+OperationExtension.DELETE;
	}

	public String toXML() {
		attr.put(ATTRNAME_DOMLEVEL, Integer.toHexString(DOMLevel));
		return super.toXML();
	}

	public void setParameterValue(String parameter,String val) {
		if (parameter.equals(ATTRNAME_DOMLEVEL)) {
			setDOMLevel(Integer.parseInt(val,16));
		}
		super.setParameterValue(parameter, val);
	}

	public int getDOMLevel() {
		return DOMLevel;
	}

	public void setDOMLevel(int level) {
		DOMLevel = level;
	}

}
