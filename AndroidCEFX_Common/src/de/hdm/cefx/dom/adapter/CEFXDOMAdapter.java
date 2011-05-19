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
 * @author Michael Voigt
 */
package de.hdm.cefx.dom.adapter;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 *
 * The CEFXDOMAdapter interface defines a set of methods that correspond to the
 * W3C DOM API methods for manipulating the content of an XML document.
 *
 * @author Ansgar Gerlicher
 *
 */
public interface CEFXDOMAdapter {

	/**
	 * Sets the document factory that will be used for creating document
	 * objects. The method <code>setDocumentFactory(...)</code> is important
	 * for the framework initialisation and is used to pass a reference of the
	 * application's document factory to CEFX.
	 *
	 * @param factory
	 *            a reference to the application's document factory.
	 */
	public void setDocumentFactory(Object factory);

	/**
	 * The method <code>createDocument(...)</code> is used to instantiate a
	 * org.w3c.dom.Document object.
	 *
	 * @param uri
	 *            the document URI that identifies the location of the document.
	 * @return the Document object that is to be edited.
	 */
	//public Document createDocument(String uri);

	/**
	 * Creates an element of the type specified. Note that the instance returned
	 * implements the Element interface, so attributes can be specified directly
	 * on the returned object.
	 *
	 * @param tagName
	 *            the name of the element.
	 * @return the Element object.
	 */
	public Element Document_createElement(String tagName);

	/**
	 * Creates an Attr of the given name.
	 *
	 * @param name
	 *            the name of the attribute.
	 * @return the Attr object.
	 */
	public Attr Document_createAttribute(String name);

	/**
	 * Creates a Text node given the specified string.
	 *
	 * @param data
	 *            the text data.
	 * @return the Text object.
	 */
	public Text Document_createTextNode(String data);

	/**
	 * Adds a new attribute node. If an attribute with that name ( nodeName) is
	 * already present in the element, it is replaced by the new one.
	 *
	 * @param newAttr
	 *            the Attr object to be set.
	 * @return the Attr object.
	 */
	public Attr Element_setAttributeNode(Attr newAttr);

	/**
	 * Adds a new attribute. If an attribute with that name is already present
	 * in the element, its value is changed to be that of the value parameter.
	 * This value is a simple string.
	 *
	 * @param element
	 *            Element where to set the attribute.
	 * @param attr
	 *            the attribute name.
	 * @param value
	 *            the attribute value.
	 */
	public void Element_AttributeSet(Element element, String attr, String value);

	public void Element_AttributeInsert(Element element, String attr,
			String value, int pos);

	public void Element_AttributeDelete(Element element, String attr, int pos,
			int length);

	public void Element_TextSet(Element parent, Element fixNode,
			int beforeAfter, String value);

	public void Element_TextInsert(Element parent, Element fixNode,
			int beforeAfter, String value, int pos);

	public void Element_TextDelete(Element parent, Element fixNode,
			int beforeAfter, int pos, int length);

	/**
	 * Adds a new attribute. If an attribute with the same local name and
	 * namespace URI is already present on the element, its prefix is changed to
	 * be the prefix part of the qualifiedName, and its value is changed to be
	 * the value parameter. This value is a simple string.
	 *
	 * @param element
	 *            the element to add the attribute to.
	 * @param nameSpaceURI
	 *            the namespace URI.
	 * @param qualifiedName
	 *            the qualified name of the attribute.
	 * @param attributeValue
	 *            the value of the attribute.
	 */
	// public void Element_setAttributeNS(Element element, String nameSpaceURI,
	// String qualifiedName, String attributeValue);
	/**
	 * The method <code>Node_appendChild(...)</code> is called to append a
	 * node to another existing node within the document.
	 *
	 * @param parent
	 *            the parent node of the new child.
	 * @param newChild
	 *            the new child to be appended.
	 * @return The node added.
	 */
	public Node Node_appendChild(Node parent, Node newChild);

	/**
	 * Removes the child node indicated by child from the parent's list of
	 * children, and returns it.
	 *
	 * @param child
	 *            the child node to be removed.
	 * @return the removed child.
	 */
	public Node Node_removeChild(Node child);

	/**
	 * Inserts the node currentChild before/after the given existing child node (fixNode). If
	 * fixNode is null, insert currentChild at the end of the list of children
	 * of the parent node. If currentChild is a DocumentFragment object, all of
	 * its children are inserted, in the same order, before/after the fixNode.
	 *
	 * @param parent
	 *            the parent node.
	 * @param newChild
	 *            the child to insert before.
	 * @param fixNode
	 *            the addressed child node to insert the new node in relation to.
	 * @param before
	 * 			  defines, whether the new child should be inserted before (value set to 0) or after (value set to 1) the fixNode
	 * @return Node - the node being inserted.
	 */
	public Node Node_insert(Node parent, Node newChild, Node fixNode, int before);

	/**
	 * The CEFXDOMAdapter's <code>refresh()</code> method is called by the
	 * CEFXController when a remote operation is executed and the application
	 * should visualise the change to the user. The CEFXDOMAdapter in turn calls
	 * the <code>repaint()</code> method of the application's render context.
	 * If an application does not provide any such kind of render context, it
	 * has to take care of the visualisation of document changes by itself.
	 */
	public void refresh();

	//public native static void signalNodeModified(String node);

	/**
	 * The render context (passed to the
	 * <code>setRenderContext(Object renderContext)</code> method) of an
	 * application is responsible for the visualisation (the rendering) of the
	 * XML document to the screen. In most Java based applications this is a
	 * class derived from javax.swing.JComponent or java.awt.Component and
	 * implements a repaint() method. In order to be compatible with this
	 * implementation of CEFX, the class representing the application's render
	 * context can be of any type but must provide a repaint() method.
	 *
	 * @param renderContext
	 *            the application's render context.
	 */
	public void setRenderContext(Object renderContext);

	/**
	 * Allows to retrieve a reference to the current Document object.
	 *
	 * @return the current Document object.
	 */
	public Document getDocument();

	/**
	 * The method <code>closeSession()</code> is used to notify the framework
	 * that the client is leaving an editing session. In that case the
	 * CEFXDOMAdapter will close any open connections to the server and the
	 * other clients and sets the framework state to �not ready for
	 * collaboration�. Thenceforth all incoming remote operations are ignored.
	 * This method is usually called when the editing application is closed.
	 */
	public void closeSession();

	/**
	 * The method <code>lock(Node node)</code> is used to lock a certain
	 * document node and thus prevent other users from editing that node until
	 * the node is unlocked again by using the <code>unlock(Node node)</code>
	 * method. Locking of nodes is realized by marking a node as locked by a
	 * certain client. The framework checks each node for this �lock marker� and
	 * prevents changing or deleting a node that has been marked as locked by
	 * another client other than the one trying to execute an operation on it.
	 * This checking is done at each client site whenever the client tries to
	 * modify the document using one of the above CEFXDOMAdapter's methods
	 * corresponding to the DOM API.
	 *
	 * @param node
	 *            the node to be locked.
	 * @return true if the lock is successfully locked.
	 */
	public boolean lockNode(Node node);

	/**
	 * Used to unlock a node.
	 *
	 * @param node
	 *            the node to be unlocked.
	 * @return true if the node was successfully unlocked.
	 */
	public boolean unlockNode(Node node);

	public void setDocument(Document document);

}
