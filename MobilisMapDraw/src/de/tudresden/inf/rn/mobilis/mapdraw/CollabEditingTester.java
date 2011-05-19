/*******************************************************************************
 * Copyright (C) 2011 Technische Universität Dresden
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
package de.tudresden.inf.rn.mobilis.mapdraw;

import java.io.IOException;
import java.io.InputStream;

import org.w3c.dom.Element;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import de.hdm.cefx.concurrency.operations.NodePosition;
import de.tudresden.inf.rn.mobilis.android.services.CollabEditingService;
import de.tudresden.inf.rn.mobilis.android.services.SessionService;

public class CollabEditingTester {

	private static final String TAG = "CollabEditingTester";
	private CollabEditingService collabEditingService;
	
	public CollabEditingTester(CollabEditingService service) { 
		collabEditingService = service;
	}

	public void test1() {
		
		// retrieve the test.xml from the Android application assets
		String fileToUpload = "test.xml";
		Context context = SessionService.getInstance().getContext();
		AssetManager assetManager = context.getAssets();
		InputStream inputStream = null;
		try {
			inputStream = assetManager.open(fileToUpload, AssetManager.ACCESS_BUFFER);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// upload xml document for testing, the server adds IDs for every element
		boolean success;
		if (inputStream == null) {
			success = false;
		} else {
			success = collabEditingService.uploadDocument(fileToUpload, inputStream);
		}
		if (!success) {
			Log.w(TAG, "Document already exists on the server or connection failure during upload");
		}

		// download the decorated document file again
		String fileForEditing = "test.xml";
		success = collabEditingService.loadDocumentFromServer(fileForEditing);
		if (success) collabEditingService.showDocument(); // debug
		
		// insert text under parent node 200 and before node 300 
		Log.i(TAG, System.currentTimeMillis() + ": " + "Inserting");
		collabEditingService.insertText("200", "300", NodePosition.INSERT_BEFORE, " Woohoo ", 0);
		// c1.insertText("200", null, 1, " Woohoo ", 0); // inserts text right under the parent node
		
		// insert a new Element node before Node 500
		Element newNode = collabEditingService.createElement("Foo");
		newNode.setAttribute("myAttrib", "69");
		collabEditingService.insertNode("200", newNode, "300", NodePosition.INSERT_AFTER);
		
		Log.i(TAG, collabEditingService.getDocumentString());
		collabEditingService.showStateVector();
	}

	public void test2() {
		
		// join a session, server creates new template KML file if none present
		String sessionName = "test2";
		boolean success = collabEditingService.joinSession(sessionName);
//		if (success) collabEditingService.showDocument(); // debug
		
	}
	
	public void waitMsec(long c) {
		try {
			Thread.sleep(c);			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}

}
