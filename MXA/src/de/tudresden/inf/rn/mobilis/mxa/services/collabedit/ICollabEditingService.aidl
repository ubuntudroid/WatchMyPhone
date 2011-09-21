package de.tudresden.inf.rn.mobilis.mxa.services.collabedit;

import android.os.Messenger;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPIQ;
import de.tudresden.inf.rn.mobilis.mxa.services.callbacks.ICollabEditingCallback;

interface ICollabEditingService {

	/**
	 * @return <code>true</code> if it is now safe to send new operations to the
	 * Collaborative Editing Framework, <code>false</code> otherwise
	 */
	boolean isReadyForEditing();

	/**
	 * @return <code>true</code> if the connection to the CEFX server is established,
	 * <code>false</code> otherwise
	 */
	boolean isConnected();

	/**
	 * Inserts the given text at the given position in a text node under a parent
	 * node before/after another specified (fix) sub-node.
	 * 
	 * @param parentNodeID
	 * 				the ID of the node being the parent of the text node
	 * @param fixNodeID
	 * 				the ID of the node which determines the position of the text node
	 * @param before
	 * 				either {@link NodePosition#INSERT_BEFORE} or {@link NodePosition#INSERT_AFTER},
	 *				depending on whether the text should be inserted in a text node before or after
	 *				the fix node
	 * @param text
	 *				the text to be inserted into the text node
	 * @param textPos
	 *				the position where the text will be inserted at
	 */ 
	void insertText(in String parentNodeID, in String fixNodeID, in int before,
			in String text, in int textPos);

	/**
	 * Deletes a chunk of text at the given position in a text node under a parent
	 * node before/after another specified (fix) sub-node.
	 * 
	 * @param parentNodeID
	 * 				the ID of the node being the parent of the text node
	 * @param fixNodeID
	 * 				the ID of the node which determines the position of the text node
	 * @param before
	 * 				either {@link NodePosition#INSERT_BEFORE} or {@link NodePosition#INSERT_AFTER},
	 *				depending on whether the text should be deleted in a text node before or after
	 *				the fix node
	 * @param pos
	 *				the beginning of the chunk marked for deletion
	 * @param textPos
	 *				the length of the chunk marked for deletion
	 */ 
	void deleteText(in String parentNodeID, in String fixNodeID, in int before, in int pos,
			in int len);

	/**
	 * Sets the text of a text node under a parent
	 * node before/after another specified (fix) sub-node.
	 * 
	 * @param parentNodeID
	 * 				the ID of the node being the parent of the text node
	 * @param fixNodeID
	 * 				the ID of the node which determines the position of the text node
	 * @param before
	 * 				either {@link NodePosition#INSERT_BEFORE} or {@link NodePosition#INSERT_AFTER},
	 *				depending on whether the text should be set in a text node before or after
	 *				the fix node
	 * @param text
	 *				the text to be set as content of the specified text node
	 */ 
	void setText(in String parentNodeID, in String fixNodeID, in int before, in String text);

	/**
	 * Deletes the node with the given id.
	 * 
	 * @param id
	 * 			the id of the node which shall be removed from the document
	 */
	void deleteNode(in String id);
	
	/**
	 * Uploads the XML specified by its file path to the server.
	 *
	 * @param filePath
	 * 				the fully qualified path to the XML-document to be uploaded to the server
	 */
	boolean uploadDocument(in String filePath);

	/**
	 * Shows the current XML document in the console, just for debugging purposes.
	 */
	void showDocument();

	/**
	 * Shows the current state vector in the console, just for debugging purposes.
	 */
	void showStateVector();
	
	/**
	 * @return a String representing the content of the current XML-document
	 */
	String getDocumentString();
	
	/**
	 * Loads a specific document from the server.
	 * 
	 * @param uri
	 *				the uri of the file on the server
	 * @returns
	 *				<code>true</code> if the document was successfully loaded from the server,
	 * 				<code>false</code> otherwise
	 */
	boolean loadDocumentFromServer(String uri);
	
	/**
	 * By calling this method the service attempts to connect to a running CEFX session.
	 * 
	 * @param sessionName
	 *				the name of the session to be joined
	 * @returns
	 *				<code>true</code> if the session has been successfully joined,
	 *				<code>false</code> otherwise
	 */
	boolean joinSession(String sessionName);

	/**
	 * Creates a new Element with the given name and returns the corresponding CEFX-UUID.
	 * TODO: This is not yet implemented properly, as there is not yet an CEFX-UUID assigned.
	 */
	String createElement(String name);

	/**
	 * Creates a new Element with the given name and returns the corresponding CEFX-UUID.
	 * TODO: This is not yet implemented properly, as there is not yet an CEFX-UUID assigned.
	 */
	String createElementNS(String namespaceURI, String name);

	/**
	 * Creates a new Text node with the given name and returns the corresponding CEFX-UUID.
	 * TODO: This is not yet implemented properly, as there is not yet an CEFX-UUID assigned.
	 */
	String createTextNode(String textContent);

	/**
	 * Inserts a new Node with the given name at the defined position.
	 */
	void insertTextNode(String parentNodeID, String content, String fixNodeID,
			int before);

	/**
	 * This method replaces the specified extract with the given text.
	 * Convenience method for executing deleteText() and insertText().
	 * 
	 * **IMPORTANT** This method is run synchronously which means it returns
	 * after the text has been inserted .
	 * 
	 * @param parent
	 * @param fixNode
	 * @param before
	 * @param text
	 * @param pos
	 * @param len
	 */
	void replaceText(String parentNodeID, String fixNodeID, int before,
			String text, int pos, int len);

	/**
	 * Attempts to leave a currently running collaboration session.
	 * @param sessionName
	 *				the name of the session to be left
	 * @returns
	 *				<code>true</code> if the session was successfully left,
	 *				<code>false</code> otherwise
	 */	 
	boolean leaveSession(in String sessionName);
	
	/**
	 * Returns the CEFXID for the node with the specified name.
	 * @param nodeName
	 * 				the name of the node whose CEFXID shall be returned
	 * @returns
	 *				the CEFXID of the specified node
	 */
	String getCEFXIDForName(in String nodeName);
	
	/**
	 * @returns
	 *				the current user JID
	 */
	String getUsername();
	
	/**
	 * @returns
	 * 				the JID of the MUC room currently being used for the collaboration session
	 */
	String getMucRoomName();
	
	/**
	 * @returns
	 *				the current CEFX user ID
	 */
	int getCEFXUserID();
	
	/**
	 * Sends the given IQ via the currently used XMPP connection without caring for the result.
	 * 
	 * @param iq
	 *				the {@link XMPPIQ} iq to be sent
	 */
	void fireAndForgetIQ(in XMPPIQ iq);
	
	/**
	 * Sends the given IQ to all participants of the currently used MUC room without caring for the
	 * result.
	 * 
	 * @param iq
	 *				the {@link XMPPIQ} iq to be sent
	 */
	void fireAndForgetMUCIQ(in XMPPIQ iq);
	
	/**
	 * Sends the given IQ via the currently used XMPP connection.
	 * 
	 * @param acknowledgement
	 * 				this {@link Messenger} will be called when the ACK is received for the iq
	 * @param result
	 *				this {@link Messenger} will be called when the iq's result has been returned to us
	 * @param requestCode
	 * @param iq
	 *				the {@link XMPPIQ} to be sent
	 */
	void sendIQ(in Messenger acknowledgement, in Messenger result, in int requestCode, in XMPPIQ iq);
	
	/**
	 * This method should be called by all classes implementing the {@link ICollabEditingCallback} interface.
	 * Otherwise they won't receive any awareness event updates.
	 *
	 * @param callback
	 * 				the {@link ICollabEditingCallback} which wishes to register at the service to receive
	 *				{@link ParcelableAwarenessEvent} updates
	 */  
	void registerCollabEditingCallback(in ICollabEditingCallback callback);
	
	/**
	 * This method should be called by all classes implementing the {@link ICollabEditingCallback} interface,
	 * if they no longer want to receive awareness event updates.
	 *
	 * @param
	 *				the {@link ICollabEditingCallback} which wishes to no longer receive awareness event updates
	 */
	void deregisterCollabEditingCallback(in ICollabEditingCallback callback);
	
	/**
	 * Disconnects from the current CEFX session.
	 */
	void disconnect();
	
	/**
	 * Creates and inserts a new top-level node within CEFX. This node will have the
	 * specified name. If there already exists an element with such a name, nothing will
	 * be done.
	 * @param nodeId
	 *				the name for the new node
	 */
	void createNewTopLevelNode(String nodeId);
}