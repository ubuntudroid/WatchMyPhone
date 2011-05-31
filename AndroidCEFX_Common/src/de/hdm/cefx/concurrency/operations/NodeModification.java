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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.hdm.cefx.util.CEFXUtil;

/**
 * The NodeModification class was designed for two reasons:
 * <ul>
 * 1. To provide a simple method for storing the modified state of a node
 * object. A node thereby can be modified using the standard DOM API methods.
 * After modifying the node, it is passed as argument to the constructor of the
 * NodeModification class which then stores all changes in the new
 * NodeModification object.
 * </ul>
 * <ul>
 * 2. To provide a container format to transmit these modifications over the
 * network without having to transmit a complete node including possible child
 * element nodes. This allows reduction of network traffic when executing update
 * operations.
 * </ul>
 * A NodeModification object stores the attributes and the text content of a
 * node. The attributes are stored in a mapping table containing all attribute
 * names and their corresponding attribute values.
 *
 *
 * @author Ansgar Gerlicher
 *
 */
@SuppressWarnings("serial")
public class NodeModification implements Serializable {

	private HashMap attributes;
	private List textNodes;

	/**
	 * Class constructor. The NodeModification stores the attributes and
	 * textnodes of the given node in lists. This is done, so that the
	 * information contained in an element node can be transported via the
	 * network, without transporting all child nodes. Only the relevant
	 * attributes and text nodes contained in the node are copied.
	 *
	 * @param n
	 *            The node that was modified.
	 */
	@SuppressWarnings("unchecked")
	public NodeModification(Node n) {

		//n.normalize(); //TODO node normalize needed?
		System.out.println("NodeModification.NodeModification()");
		attributes = new HashMap();
		textNodes = new ArrayList();
		NamedNodeMap attr = n.getAttributes();
		for (int i = 0; i < attr.getLength(); i++) {
			Attr at = (Attr) attr.item(i);
			String nodeName = at.getNodeName();
			String nameSpace = at.getNamespaceURI();
			String value = at.getValue();
			AttributeIdentifier aident = new AttributeIdentifier(nodeName, nameSpace);
			attributes.put(aident, value);
//System.out.println("NodeModification.NodeModification() stored: ns: " + nameSpace + " : " + nodeName + "=" + value);
		}

		// if a node is no text node a null reference is stored
		NodeList nl = n.getChildNodes();
		Node lastNode = null;
		for (int i = 0; i < nl.getLength(); i++) {
			Node item = nl.item(i);

			if (item.getNodeType() == Node.TEXT_NODE) {

				// Android Workaround
				Node nextNode;
				try{
					nextNode = item.getNextSibling();
				}catch (IndexOutOfBoundsException e) {
					nextNode = null;
				}
				
				textNodes.add(new TextUpdate(getNodeId(n), getNodeId(lastNode), item.getNodeValue(), getNodeId(nextNode)));
			}
			lastNode = item;
		}
	}

	/**
	 * Allows to retrieve the UUID of the node.
	 * @param node node to be modified.
	 * @return the UUID of the node.
	 */
	private static String getNodeId(Node node) {

		if (node == null) {

			return null;
		} else {
			NamedNodeMap nnm = node.getAttributes();

//			for (int i = 0; i < nnm.getLength(); i++) {
//
//			}
			if (nnm == null) {

				return null;
			} else {
				Node uid = nnm.getNamedItem(CEFXUtil.CEFXUID);

				if (uid == null) {
					uid = nnm.getNamedItemNS(CEFXUtil.CEFX_NAMESPACE, CEFXUtil.CEFXUID);

					return uid.getNodeValue();
				} else {

					return uid.getNodeValue();
				}
			}
		}
	}

	/**
	 * Retrieves the attributes of the node.
	 * @return the attributes of the node as map.
	 */
	public HashMap getAttributes() {
		return attributes;
	}

	/**
	 * Set the attributes of the node.
	 * @param attributes the attributes as map (key, value pairs).
	 */
	public void setAttributes(HashMap attributes) {
		this.attributes = attributes;
	}

	/**
	 * Retrieves the text nodes of this node.
	 * @return the list of text nodes.
	 */
	public List getTextNodes() {
		return textNodes;
	}

	/**
	 * Sets the list of text nodes.
	 * @param textNodes the list of text nodes.
	 */
	public void setTextNodes(List textNodes) {
		this.textNodes = textNodes;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@SuppressWarnings("unchecked")
	public String toString() {
		StringBuffer text = new StringBuffer();
		Collection<AttributeIdentifier> attribIdent = this.attributes.keySet();
		for (AttributeIdentifier ai : attribIdent) {
			text.append("attr: " + ai.getLocalName() + " ns: " + ai.getNamespaceURI() + " value: " + attributes.get(ai) + "\n");
		}
		text.append("Text: " + this.textNodes);
		return text.toString();
	}

}
