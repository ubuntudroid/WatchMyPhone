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

	void insertText(in String parentNodeID, in String fixNodeID, in int before,
			in String text, in int textPos);

	void deleteText(in String parentNodeID, in String fixNodeID, in int before, in int pos,
			in int len);

	void setText(in String parentNodeID, in String fixNodeID, in int before, in String text);

	void deleteNode(in String id);

	boolean uploadDocument(in String filePath);

	/**
	 * Shows the current XML document in the console, just for debugging.
	 */
	void showDocument();

	void showStateVector();

	String getDocumentString();

	boolean loadDocumentFromServer(String uri);

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

	boolean leaveSession(in String sessionName);
	
	String getCEFXIDForName(in String wmpId);
	
	String getUsername();
	
	String getMucRoomName();
	
	int getCEFXUserID();
	
	void fireAndForgetIQ(in XMPPIQ iq);
	
	void fireAndForgetMUCIQ(in XMPPIQ iq);
	
	void sendIQ(in Messenger acknowledgement, in Messenger result, in int requestCode, in XMPPIQ iq);
	
	void registerCollabEditingCallback(in ICollabEditingCallback callback);
	
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