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

/**
 * Class to identify attributes.
 * @author Ansgar Gerlicher
 *
 */
@SuppressWarnings("serial")
public class AttributeIdentifier implements Serializable {

	private String localName;

	private String namespaceURI;

	/**
	 * Class constructor.
	 *
	 * @param localname
	 *            the name of the attribute.
	 * @param nsUri
	 *            the namespace URI of the attribute.
	 */
	public AttributeIdentifier(String localname, String nsUri) {
		localName = localname;
		namespaceURI = nsUri;
	}

	/**
	 * Property retrieval method.
	 *
	 * @return the local name of the attribute
	 */
	public String getLocalName() {
		return localName;
	}

	/**
	 * Property set method.
	 *
	 * @param localName
	 *            the local name of the attribute.
	 */
	public void setLocalName(String localName) {
		this.localName = localName;
	}

	/**
	 * Property retrieval method.
	 *
	 * @return the namespace URI name of the attribute
	 */
	public String getNamespaceURI() {
		return namespaceURI;
	}

	/**
	 * Property set method.
	 *
	 * @param namespaceURI
	 *            the namespace URI of the attribute.
	 */
	public void setNamespaceURI(String namespaceURI) {
		this.namespaceURI = namespaceURI;
	}
}
