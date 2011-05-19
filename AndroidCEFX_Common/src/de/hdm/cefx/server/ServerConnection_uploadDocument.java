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
 */
package de.hdm.cefx.server;


import java.io.ByteArrayInputStream;
import java.io.Serializable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import de.hdm.cefx.util.XMLHelper;

public class ServerConnection_uploadDocument implements Serializable {
	private String   document;
	public  String   name;

	public Document getDocument() {
		System.out.println(document);
		Document doc=null;
		DocumentBuilder builder;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			ByteArrayInputStream is=new ByteArrayInputStream(document.getBytes());
			doc = builder.parse(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return doc;
	}

	public void setDocument(Document doc) {
//		try {
//			Transformer trans;
//			trans = TransformerFactory.newInstance().newTransformer();
//			ByteArrayOutputStream ba=new ByteArrayOutputStream();
//			StreamResult sr=new StreamResult(ba);
//			trans.transform(new DOMSource(doc), sr);
//			try {
//				ba.flush();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//
//			document=ba.toString();
//		} catch (TransformerException e) {
//			e.printStackTrace();
//		}
		document = XMLHelper.getDocumentString(doc, false);
		//System.out.println("Document to upload: " + System.getProperty("line.separator") + document);
	}

}
