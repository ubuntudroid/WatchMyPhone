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

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import de.hdm.cefx.exceptions.NodeNotFoundException;

/**
 * The context of execution is the local document state at each editing site.
 * The ExecutionContext interface declares methods that provide information on
 * the current context of execution and allows modification of it. Operations
 * that are executed use those methods to achieve the required effect and thus
 * change the context.
 *
 * @author Ansgar Gerlicher
 *
 */
public interface ExecutionContext {
	/**
	 * The method <code>getNodeForId(String uuid)</code> retrieves the node
	 * with the given UUID from the document. When an operation is transmitted
	 * over the network the target node of an operation is identified by the
	 * UUID. This method is used to get a reference to the node object within
	 * the local document.
	 *
	 * @param uuid
	 *            the UUID of the node that should be retrieved.
	 * @return the Node object from the document.
	 * @throws NodeNotFoundException
	 */
	public Node getNodeForId(String uuid) throws NodeNotFoundException;

	/**
	 * The method <code>getNodeId(Node node)</code> retrieves the UUID of a
	 * given node in order to, for example, compare it with an UUID of another
	 * node.
	 *
	 * @param node
	 *            the Node object.
	 * @return the UUID of the given Node object.
	 */
	public String getNodeId(Node node);

	/**
	 * A second map of nodes is used to store all nodes that were deleted from
	 * the document (deletedNodesMap). This is done in order to be able to undo
	 * a delete operation easily. The method <code>getNodeFromDeletedNodeMap(Operation
	 * op)</code> is used to retrieve a node from the map of deleted nodes.
	 *
	 * @param operation the operation that is to be undone and deleted a node.
	 * @return the node that was deleted before.
	 */
	public Node getNodeFromDeletedNodeMap(Operation operation);

	/**
	 * The ExecutionContext (represented by the concurrency controller
	 * implementation) owns a map of all nodes in the document (nodeMap). After
	 * an operation is executed, the operation calls the method
	 * <code>refreshNodeMap()</code> so that all changes to the document are
	 * reflected in the map of nodes. That is for example, if a new node has
	 * been added to the document, the node map is updated and the new node is
	 * added to the nodeMap.
	 */
	public void refreshNodeMap();

	/**
	 * The method <code>addNodeToDeletedNodeMap(Operation op, Node node)</code>
	 * is used to store a deleted node in the deletedNodesMap of the concurrency
	 * controller implementation.
	 *
	 * @param operation
	 *            the operation that deleted this node.
	 * @param node
	 *            the node that was deleted.
	 */
	public void addNodeToDeletedNodeMap(Operation operation, Node node);

	/**
	 * The method <code>getLocalDoc()</code> provides the caller with a
	 * reference to the current local document object. Operations use this
	 * method to modify the document directly.
	 *
	 * @return the local document.
	 */
	public Document getLocalDoc();

	/**
	 * In order to find out if a node with a certain UUID still exists in the
	 * local context, the method <code>existsNode(String nodeUuid)</code> is
	 * used.
	 *
	 * @param nodeUuid
	 *            the UUID of the node searching for.
	 * @return true if the node still exists in the local document.
	 */
	public boolean existsNode(String nodeUuid);

	/**
	 * The method
	 * <code>isChildOfNode(String childNode, String parentNode)</code> is
	 * called by the ConflictResolutionProvider. It is used to find out, if a
	 * node with the given UUID in the childNode argument is part of a sub tree
	 * starting from the node with the UUID in the parentNode argument. This is
	 * necessary in order to know if a deletion of a node might interfere with
	 * an insertion or an update on another node which is part of the sub tree.
	 *
	 * @param childNode
	 *            the node that is the child of the parent node.
	 * @param parentNode
	 *            the parent node.
	 * @return true if the childnode is a child of the parentnode.
	 */
	public boolean isChildOfNode(String childNode, String parentNode);
}
