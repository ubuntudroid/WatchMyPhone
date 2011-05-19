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

package de.hdm.cefx.util;


/**
 * A helper class to separate the AOP code from the java code.
 *
 * @author Ansgar Gerlicher
 *
 */
public class Advices {
	/**
	 * Checks if the framework is ready for collaboration and then calls the
	 * correspondig <code>Node_appendChild(p, c)</code> method of the
	 * CEFXDOMAdapter.
	 *
	 * @see de.hdm.cefx.dom.adapter.CEFXDOMAdapter#Node_appendChild(Node, Node)
	 *
	 * @param p
	 *            the parent element to append the child to.
	 * @param c
	 *            the child to append.
	 * @return the appended node.
	 */
/*	public static Node appendChild(Element p, Element c) {
		CEFXDOMAdapter doma = CEFXUtil.getDOMAdapter();
		if (doma != null) {

			if (doma.isCollaborationReady()) {
				// System.out.println("AppendChildAspekt.before() appending
				// child: <" + c.getNodeName()+":"+c.getNodeValue()+"> to parent
				// <" + p.getNodeName()+":"+p.getNodeValue()+">");
				return doma.Node_appendChild(p, c);
			}
		}
		return p.appendChild(c);
	}*/

	/**
	 * Checks if the framework is ready for collaboration and then calls the
	 * correspondig method of the CEFXDOMAdapter.
	 *
	 * @see de.hdm.cefx.dom.adapter.CEFXDOMAdapter#Node_insertBefore(Node, Node,
	 *      Node)
	 *
	 * @param p
	 *            the parent node to insert the new node into.
	 * @param c
	 *            the new child node
	 * @param o
	 *            the old child node as reference node.
	 * @return the inserted node.
	 */
/*	public static Node insertBefore(Element p, Node c, Node o) {
		CEFXDOMAdapter doma = CEFXUtil.getDOMAdapter();
		if (doma != null) {
			if (doma.isCollaborationReady()) {
				return doma.Node_insertBefore(p, c, o);
			}
		}
		return p.insertBefore(c, o);
	}*/

	/**
	 * Checks if the framework is ready for collaboration and then calls the
	 * correspondig method of the CEFXDOMAdapter.
	 *
	 * @see de.hdm.cefx.dom.adapter.CEFXDOMAdapter#Node_removeChild(Node, Node)
	 *
	 * @param p
	 *            the parent of the child to be removed.
	 * @param c
	 *            the node to be removed.
	 * @return the removed node.
	 */
/*	public static Node removeChild(Node p, Node c) {
		CEFXDOMAdapter doma = CEFXUtil.getDOMAdapter();
		if (doma != null) {
			if (doma.isCollaborationReady()) {
				return doma.Node_removeChild(p, c);
			}
		}
		return p.removeChild(c);
	}*/

	/**
	 *
	 * Checks if the framework is ready for collaboration and then calls the
	 * correspondig method of the CEFXDOMAdapter.
	 *
	 * @see de.hdm.cefx.dom.adapter.CEFXDOMAdapter#Element_setAttributeNS(Element,
	 *      String, String, String)
	 *
	 * @param p
	 *            the target node.
	 * @param ns
	 *            the namespace URI.
	 * @param attr
	 *            the attribute name.
	 * @param value
	 *            the attribute value.
	 */
/*
	public static void setAttributeNS(Element p, String ns, String attr, String value) {
		CEFXDOMAdapter doma = CEFXUtil.getDOMAdapter();
		if (doma != null) {

			if (doma.isCollaborationReady()) {
				doma.Element_setAttributeNS(p, ns, attr, value);
				return;
			}
		}
		p.setAttributeNS(ns, attr, value);
	}
*/
	/**
	 *
	 * Checks if the framework is ready for collaboration and then calls the
	 * correspondig method of the CEFXDOMAdapter.
	 *
	 * @see de.hdm.cefx.dom.adapter.CEFXDOMAdapter#Element_setAttribute(Element,
	 *      String, String)
	 *
	 * @param p
	 *            the target node.
	 * @param attr
	 *            the attribute name.
	 * @param value
	 *            the attribute value.
	 */
/*
	public static void setAttribute(Element p, String attr, String value) {
		CEFXDOMAdapter doma = CEFXUtil.getDOMAdapter();
		if (doma != null) {
			if (doma.isCollaborationReady()) {
				doma.Element_setAttribute(p, attr, value);
				return;
			}
		}
		p.setAttribute(attr, value);
	}
*/

}
