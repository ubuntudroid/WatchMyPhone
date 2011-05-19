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
 * @author Dirk Hering
 */
package de.hdm.cefx.server.util;

import java.util.Collection;

import jabberSrpc.MucRoomListener;
import jabberSrpc.MucRoomMessageListener;
import jabberSrpc.MucRoomPresenceListener;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.hdm.cefx.util.CEFXUtil;
import de.tudresden.inf.rn.mobilis.server.MobilisManager;

/**
 * The server's utility class.
 *
 * @author Ansgar Gerlicher
 * @author Dirk Hering
 */
public class ServerUtil {
	
	private int counter = 0;

	private Document doc;
	
	/**
	 * Adds a UUID to each node in the document.
	 *
	 * @param document
	 *            the document to add UUIDs.
	 * @return the processed document.
	 */
	public Document addUUIDsToDocument(Document document) {
		doc = document;
		doc.normalizeDocument();

		Node root = doc.getDocumentElement();

		addUIDToNode(root, true);
		return doc;
	}

	private int getNextCounterVal() {
		counter=counter+1;
		return counter;
	}

	/**
	 * Adds a UUID to the given node and all its children recursively (uses client ID 0 for the server)
	 * @param node the node to decorate
	 * @param isRoot indicates, whether the given node is the root document element
	 */
	private void addUIDToNode(Node node, boolean isRoot) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element e = (Element) node;
			
			if (isRoot) {
				e.setAttribute("xmlns:" + CEFXUtil.CEFX_PREFIX, CEFXUtil.CEFX_NAMESPACE);
			}
			
			Attr uid = doc.createAttributeNS(CEFXUtil.CEFX_NAMESPACE, CEFXUtil.CEFXUID);
			String id=CEFXUtil.newUUID(0,getNextCounterVal());
			uid.setValue(id);

			NodeList nl = node.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				addUIDToNode(n, false);
			}
		}
	}

	public static String getFullMucRoomIdentifier(String plainMucName) {
		String serviceName = MobilisManager.getInstance().getSettingString(
				"services", "CollabEditingService", "ChatRoomServer");
		String fullName = plainMucName + "@" + serviceName;
		return fullName;
	}
	
	public static String genUniqueMucRoomName(String suffix) {
		String mucRoomName = "";
		mucRoomName = Integer
				.toHexString(((int) (Math.random() * ((float) 0xFFFFFF))));
		if (suffix != null) {
			mucRoomName = mucRoomName + suffix;
		}
		mucRoomName = getFullMucRoomIdentifier(mucRoomName);
		return mucRoomName;
		// TODO check if name is unique
	}
	
	public static String getFullUnusedMucRoomIdentifier(XMPPConnection connection, String plainMucName) {
		String serviceName = MobilisManager.getInstance().getSettingString(
				"services", "CollabEditingService", "ChatRoomServer");
        String newName = plainMucName;
        
        // check if group name is occupied, if so, choose another name
		try {
	        Collection<HostedRoom> hostedRooms;
			hostedRooms = MultiUserChat.getHostedRooms(connection, serviceName);
	        boolean inUse;
	        int i = 1;
	        do {
	        	inUse = false;
	        	for (HostedRoom hr : hostedRooms) {
	        		if (hr.getName().equals(plainMucName)) {
	        			inUse = true;
	        			newName = plainMucName + "_" + i;
	        			i++;
	        			break;
	        		}
	        	}
	        } while (inUse);
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        String fullName = newName + "@" + serviceName;
        return fullName;
	}
	
	public static MultiUserChat createMucRoom(XMPPConnection connection, String fullMucName) {
		MultiUserChat m = new MultiUserChat(connection, fullMucName);
		try {
			m.create(StringUtils.parseName(connection.getUser()));			
			m.sendConfigurationForm(new Form(Form.TYPE_SUBMIT));
			return m;
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static MultiUserChat createMucRoom(XMPPConnection connection, String name, MucRoomListener handler) {
		MultiUserChat muc = createMucRoom(connection, name);
		addMucRoomListener(muc, handler);
		return muc;
	}
	
	public static void addMucRoomListener(MultiUserChat muc, MucRoomListener handler) {
		MucRoomMessageListener l = new MucRoomMessageListener();
		l.setHandler(handler);
		muc.addMessageListener(l);

		MucRoomPresenceListener lp = new MucRoomPresenceListener();
		lp.setHandler(handler);
		muc.addParticipantListener(lp);
	}
	
	public static String getFileNameWithoutExtension(String fileName) {
		// TODO substring(0,-1) problematic?
		return fileName.substring(0, fileName.lastIndexOf("."));
	}
}
