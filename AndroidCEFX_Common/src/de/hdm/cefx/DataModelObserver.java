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
package de.hdm.cefx;

import org.w3c.dom.Node;

/**
 * Abstract observer class, which can be registered at the DataModelAbstraction to
 * get notified if the data model got altered by remote operations.
 * @author Dirk Hering
 */
public abstract class DataModelObserver {

	/**
	 * Called by the DataModelAbstraction to notify this observer of changes.
	 * @param changedNode the Node which got affected by a change, more precisely the node which got 
	 * inserted, updated or deleted (in case of an atomic operation on the xml document)
	 * @param modification
	 * @param complex
	 */
//	public abstract void update(Node changedNode, ModelModificationType modification, ComplexOperationEvent complex);
	
}
