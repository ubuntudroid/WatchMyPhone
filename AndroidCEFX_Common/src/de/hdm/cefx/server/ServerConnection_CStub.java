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
 * @author Michael Voigt
 * @author Sven Bendel
 */
package de.hdm.cefx.server;

import jabberSrpc.JabberClient;
import jabberSrpc.Stub;

import java.util.Vector;

import org.w3c.dom.Document;

import de.hdm.cefx.client.CEFXClient;

public class ServerConnection_CStub extends Stub {

	public ServerConnection_CStub(JabberClient client) {
		super(client, "ServerConnection");
	}

	public DocumentData loadDocument(int docID) {
		DocumentData result=null;
		ServerConnection_loadDocument c=new ServerConnection_loadDocument();
		c.docID=docID;

		Object o=sendRPC("loadDocument",c, true);
		if (o!=null) {
		  result=(DocumentData)o;
		}

		return result;
	}

	public boolean uploadDocument(Document doc, String name) {
		boolean result=false;
		ServerConnection_uploadDocument c=new ServerConnection_uploadDocument();
		c.setDocument(doc);
		c.name=name;

		Object o=sendRPC("uploadDocument",c, true);
		if (o!=null) {
		  result=((Boolean)o).booleanValue();
		}

		return result;
	}

	public Vector<ServerObject> listFiles() {
		Vector<ServerObject> result=null;

		Object o=sendRPC("listFiles",null, true);
		if (o!=null) {
		  result=(Vector<ServerObject>)o;
		}

		return result;
	}

	public SessionData openDocument(int docID,CEFXClient client) {
		// TODO obsolete, see joinSession()
		SessionData result=null;

		ServerConnection_openDocument c=new ServerConnection_openDocument();
		c.docID=docID;
		c.client=client;

		Object o=sendRPC("openDocument",c, true);
		if (o!=null) {
		  result=(SessionData)o;
		}
		return result;
	}
	
	public SessionData joinSession(String sessionName, CEFXClient client) {
		SessionData result = null;

		ServerConnection_joinSession c = new ServerConnection_joinSession();
		c.sessionName = sessionName;
		c.client = client;

		Object o = sendRPC("joinSession", c, true);
		if (o != null) {
			result = (SessionData) o;
		}
		return result;
	}
	
	public boolean leaveSession(String sessionName, CEFXClient client) {
		boolean result = false;
		
		ServerConnection_leaveSession c = new ServerConnection_leaveSession();
		c.sessionName = sessionName;
		c.client = client;
		
		Object o = sendRPC("leaveSession", c, true);
		if (o != null) {
			result = (Boolean) o;
		}
		return result;
	}

}
