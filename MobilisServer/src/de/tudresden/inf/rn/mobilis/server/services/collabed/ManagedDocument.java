/*******************************************************************************
 * Copyright (C) 2010 Technische Universität Dresden
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
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: http://mobilisplatform.sourceforge.net
 ******************************************************************************/
package de.tudresden.inf.rn.mobilis.server.services.collabed;

import java.util.concurrent.locks.ReentrantLock;

import org.w3c.dom.Document;

import de.hdm.cefx.util.XMLHelper;

/**
 * A wrapper for a file on the server which can be edited collaboratively, providing
 * an easier access and management.
 * @author Dirk Hering
 * @author Michael Voigt
 */

public class ManagedDocument {

	private Document document = null;
	private String filePath = null;
	private int documentID;
	private String name;
	private boolean open;
	protected final ReentrantLock editLock = new ReentrantLock();

	/**
	 * Creates the ManagedDocument as a wrapper object for the DOM Document, which can be
	 * generated later from the given path (including file name)
	 * @param id the ID to assign to this ManagedDocument (you can use getFreeDocumentID() from the CollabEditingService)
	 * @param filePath the path of the file the ManagedDocument should point to
	 */
	public ManagedDocument(int id, String filePath) {
		documentID = id;
		int lastSlashAt = filePath.lastIndexOf("/");
		if (lastSlashAt != filePath.length()-1) {
			name = filePath.substring(filePath.lastIndexOf("/")+1);
		}
		this.filePath = filePath;
		open = false;
	}

	public void stopEdit() {
		editLock.lock();
	}

	public void startEdit() {
		editLock.unlock();
	}

	/**
	 * Tries to open the document on the server by parsing it into a DOM Document.
	 * @return boolean - true, if everything went fine or if the document was already open
	 */
	public synchronized boolean open() {
		if (open) return true;
		document = XMLHelper.getDocumentFromPath(filePath);
		if (document != null) {
			open = true;
			return true;
		} else return false;
	}

	public void close() {
		// TODO close document impl
		if (open) {
			saveDocument();
			// ...
		}
	}

	public void saveDocument() {
//		if (document == null)
//			return;
//		if ((filePath == null) || ("".equals(filePath)))
//			return;
//
//		Transformer trans;
//		try {
//			trans = TransformerFactory.newInstance().newTransformer();
//			File file = new File(filePath);
//			StreamResult sr = new StreamResult(file);
//			trans.transform(new DOMSource(document), sr);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		if (document == null)
			return;
		if ((filePath == null) || ("".equals(filePath)))
			return;
		
		XMLHelper.storeDocument(document, filePath);
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public int getDocumentId() {
		return documentID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isOpen() {
		return open;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String path) {
		this.filePath = path;
	}
}
