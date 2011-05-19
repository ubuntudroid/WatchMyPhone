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
package de.hdm.cefx.util;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * This class provides some functionality introduced with DOM Level 3 (used by CEFX), but not available
 * in DOM Level 2 (partly supported by Android).
 * @author Dirk Hering
 * 
 */
public class DOM3Methods {

	/**
	 * Detaches the given Node from its owner Document. Works only for Elements, Attributes and Text nodes.
	 * @param node the node to be detached
	 * @return Node
	 */
	public static Node detachNode(Node node) {
        if (node == null) {
        	return null;
        }
        
        // removing the node from its current tree
        switch (node.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
                Attr attr = (Attr) node;
                if( attr.getOwnerElement() != null){
                    // owner element attribute is set to null
                    attr.getOwnerElement().removeAttributeNode(attr);
                }
                break;
            case Node.ELEMENT_NODE:
            case Node.TEXT_NODE:
                Node parent = node.getParentNode();
                if (parent != null) {
                    parent.removeChild(node);
                }
                break;
            default:
            	return null;
        }
        
        return node;
	}
	
    /**
     * Changes the node's owner Document, and its subtree, to this Document. 
     * Therefore, a deep copy is created.
     * Only Element, Attribute or Text nodes are detached (deleted) from their original tree structure.
     * (Basis code from Xerces)
     * @param document the destination document
     * @param node the node to adopt
     * @param deep determines, if the returned copy should be a deep copy
     * @return Node a copy of the originating node in the destination document
     **/
	public static Node adoptNode(Document document, Node node, boolean deep) {
		
//		System.out.println("Target Document:");
//		XMLHelper.showDocumentContent(document);
//		System.out.println("Original Document:");
//		XMLHelper.showDocumentContent(node.getOwnerDocument());
		
		if ((document == null) || (node == null)) {
			return null;
		}
		if (node.getOwnerDocument() == document) {
			return node;
		}
		Node copy = null;
		try {
			//detachNode(node);
			// TODO probably a NAMESPACE_ERR will be raised -> node.getOwnerDocument().getDocumentElement().setAttributeNS(...); 
			copy = document.importNode(node, deep);
		} catch (DOMException e) {
			System.err.println("DOMException - Error Code: "+ e.code);
			e.printStackTrace();
		}                                                                                                                                                      
		return copy;
	}

	/**
	 * Returns the text content of the Node. The internal implementation of DOM defines for which node types
	 * a string value will be returned.
	 * @param node the node to get the String from
	 * @return String
	 */
	public static String getTextContent(Node node) {
		switch (node.getNodeType()) {
		case Node.DOCUMENT_NODE:
			return null;
		//case Node.CDATA_SECTION_NODE:
		//	return null;
		default:
			return node.getNodeValue();
	    }
	}
	
	/**
	 * Sets the text content of the TextNode. Actually, the old node is removed and an identical node gets inserted with the new text value.
	 * @param textNode
	 * @param text
	 */
	public static void setTextContent(Text textNode, String text) {
		Document ownerDoc = textNode.getOwnerDocument();
		if (ownerDoc != null) {
			Text newTextNode = ownerDoc.createTextNode(text);
			Node parentNode = textNode.getParentNode();
			parentNode.replaceChild(newTextNode, textNode);
		} 
	}
	
	/**
	 * Looks up the prefix associated to the given namespace URI, starting from the given Node upwards in the tree.
	 * (Basis code from Xerces)
	 * @param node the node to start the lookup from
	 * @param namespaceURI the namespace for which to look for a prefix
	 * @return String
	 */
	public static String lookupPrefix(Node node, String namespaceURI) {
        // REVISIT: When Namespaces 1.1 comes out this may not be true
        // Prefix can't be bound to null namespace
        if (namespaceURI == null) {
            return null;
        }

        short type = node.getNodeType();

        switch (type) {
        case Node.ELEMENT_NODE: {
                node.getNamespaceURI(); // to flip out children 
                return lookupNamespacePrefix(node, namespaceURI, (Element)node);
            }
        case Node.DOCUMENT_NODE:{
                return lookupPrefix((Node)((Document)node).getDocumentElement(), namespaceURI);
            }

        case Node.ENTITY_NODE :
        case Node.NOTATION_NODE:
        case Node.DOCUMENT_FRAGMENT_NODE:
        case Node.DOCUMENT_TYPE_NODE:
            // type is unknown
            return null;
        case Node.ATTRIBUTE_NODE:{
        	Node ownerNode = node.getParentNode();
                if (ownerNode.getNodeType() == Node.ELEMENT_NODE) {
                    return lookupPrefix(ownerNode, namespaceURI);
                }
                return null;
            }
        default:{   
                Node ancestor = getElementAncestor(node);
                if (ancestor != null) {
                    return lookupPrefix(ancestor, namespaceURI);
                }
                return null;
            }
        }
	}
	
	/**
	 * Looks up the prefix associated to the given namespace URI, starting from the given Node (including its attributes) upwards in the tree,
	 * which matches with the namespace URI of an given Element.
	 * (Basis code from Xerces)
	 * @param node the node to start the lookup from
	 * @param namespaceURI the namespace for which to look for a prefix
	 * @param el the element of which the namespace should be matching
	 * @return String
	 */
	public static String lookupNamespacePrefix(Node node, String namespaceURI, Element el){
        String namespace = node.getNamespaceURI();
        // REVISIT: if no prefix is available is it null or empty string, or 
        //          could be both?
        String prefix = node.getPrefix();

        if (namespace!=null && namespace.equals(namespaceURI)) {
            if (prefix != null) {
                String foundNamespace = lookupNamespaceURI(el, prefix);
                if (foundNamespace !=null && foundNamespace.equals(namespaceURI)) {
                    return prefix;
                }
            }
        }
        if (node.hasAttributes()) {
            NamedNodeMap map = node.getAttributes();
            int length = map.getLength();
            for (int i=0;i<length;i++) {
                Node attr = map.item(i);
                String attrPrefix = attr.getPrefix();
                String value = attr.getNodeValue();
                namespace = attr.getNamespaceURI();
                if (namespace !=null && namespace.equals("http://www.w3.org/2000/xmlns/")) {
                    // DOM Level 2 nodes
                    if (((attr.getNodeName().equals("xmlns")) ||
                         (attrPrefix !=null && attrPrefix.equals("xmlns")) &&
                         value.equals(namespaceURI))) {

                        String localname= attr.getLocalName();
                        String foundNamespace = lookupNamespaceURI(el, localname);
                        if (foundNamespace !=null && foundNamespace.equals(namespaceURI)) {
                            return localname;
                        }
                    }
                }
            }
        }
        
        Node ancestor = (Node)getElementAncestor(node);
        if (ancestor != null) {
            return lookupNamespacePrefix(ancestor, namespaceURI, el);
        }
        return null;
    }
	
    /**
     * Looks up the namespace URI associated to the given prefix, starting from the given node.
     * Use lookupNamespaceURI(null) to lookup the default namespace.
     * (Basis code from Xerces)
     * @param node the node to start the lookup from
     * @param specifiedPrefix
     * @return the URI for the namespace
     * @since DOM Level 3
     */
    public static String lookupNamespaceURI(Node node, String specifiedPrefix) {
        short type = node.getNodeType();
        switch (type) {
        case Node.ELEMENT_NODE : {  
                
                String namespace = node.getNamespaceURI();
                String prefix = node.getPrefix();
                if (namespace !=null) {
                    // REVISIT: is it possible that prefix is empty string?
                    if (specifiedPrefix== null && prefix==specifiedPrefix) {
                        // looking for default namespace
                        return namespace;
                    } else if (prefix != null && prefix.equals(specifiedPrefix)) {
                        // non default namespace
                        return namespace;
                    }
                } 
                if (node.hasAttributes()) {
                    NamedNodeMap map = node.getAttributes();
                    int length = map.getLength();
                    for (int i=0;i<length;i++) {
                        Node attr = map.item(i);
                        String attrPrefix = attr.getPrefix();
                        String value = attr.getNodeValue();
                        namespace = attr.getNamespaceURI();
                        if (namespace !=null && namespace.equals("http://www.w3.org/2000/xmlns/")) {
                            // at this point we are dealing with DOM Level 2 nodes only
                            if (specifiedPrefix == null &&
                                attr.getNodeName().equals("xmlns")) {
                                // default namespace
                                return value;
                            } else if (attrPrefix !=null && 
                                       attrPrefix.equals("xmlns") &&
                                       attr.getLocalName().equals(specifiedPrefix)) {
                                // non default namespace
                                return value;
                            }
                        }
                    }
                }
                Node ancestor = getElementAncestor(node);
                if (ancestor != null) {
                    return lookupNamespaceURI(ancestor, specifiedPrefix);
                }

                return null;
            }
        case Node.DOCUMENT_NODE : {   
                return lookupNamespaceURI((Node)((Document)node).getDocumentElement(), specifiedPrefix);
            }
        case Node.ENTITY_NODE :
        case Node.NOTATION_NODE:
        case Node.DOCUMENT_FRAGMENT_NODE:
        case Node.DOCUMENT_TYPE_NODE:
            // type is unknown
            return null;
        case Node.ATTRIBUTE_NODE:{
        	Node ownerNode = node.getParentNode();
                if (ownerNode.getNodeType() == Node.ELEMENT_NODE) {
                    return lookupNamespaceURI(ownerNode, specifiedPrefix);
                }
                return null;
            }
        default:{ 
                Node ancestor = getElementAncestor(node);
                if (ancestor != null) {
                    return lookupNamespaceURI(ancestor,specifiedPrefix);
                }
                return null;
            }

        }
    }
	
	/**
	 * Returns the first found parent Element for the given Node.
	 * (Basis code from Xerces)
	 * @param node the node to look for an ancestor element
	 * @return Node
	 */
	public static Node getElementAncestor(Node node) {
        Node parent = node.getParentNode();
        if (parent != null) {
            short type = parent.getNodeType();
            if (type == Node.ELEMENT_NODE) {
                return parent;
            }
            return getElementAncestor(parent);
        }
        return null;
    }
}
