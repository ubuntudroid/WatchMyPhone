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

public enum ComplexOperationStatus {

	/**
	 * This Operation is an atomic operation (UPDATE, INSERT or DELETE) and not a ComplexOperation.
	 */
	ATOMIC, 
	
	/**
	 * This is an ComplexOperation, consisting of only one single atomic operation.
	 * In other words, this only lets a client distinguish from several atomic operations more easily
	 * (e.g. inserting an application-specific node)
	 */
	COMPLEX_SINGLE, 
	
	/**
	 * This Operation is the first one in a series of atomic operations constituting a ComplexOperation.
	 */
	COMPLEX_BEGIN, 
	
	/**
	 * This Operation is in a series of atomic operations constituting a ComplexOperation and its not the first or last one.
	 */
	COMPLEX_INSIDE, 
	
	/**
	 * This Operation is the last one in a series of atomic operations constituting a ComplexOperation.
	 */
	COMPLEX_END
	
}
