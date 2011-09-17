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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.jivesoftware.smack.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Helper class for parsing and serializing XML files.
 * @author Dirk Hering
 */
public class XMLHelper {
	
	/**
	 * Only shows the document string in the console for debugging.
	 * @param xmlDoc the DOM Document to serialize and show
	 */
    public static void showDocumentContent(Document document) {
    	System.out.println(getDocumentString(document, true));
    }
    
	/**
	 * Serializes a DOM Document into a String.
	 * @param sourceDocument the Document to be serialized
	 * @param prettyPrint indicates, whether the String will be needed to transmit (not pretty printed) 
	 * or just shown locally (formatted with line breaks and indentation)
	 * @return String
	 */
    public static String getDocumentString(Document sourceDocument, boolean prettyPrint) {
 	
    	if (sourceDocument == null) {
    		return null;
    	}
    	
        OutputFormat format = new OutputFormat(sourceDocument);
        if (prettyPrint) {
        	format.setLineSeparator(System.getProperty("line.separator"));
        	format.setIndenting(true);
        	format.setLineWidth(80);
        	format.setIndent(4);
        } else {
        	format.setIndenting(false);
        }
        
        StringWriter out = new StringWriter();

        XMLSerializer serializer = new XMLSerializer(out, format);
        try {
			serializer.serialize(sourceDocument);
			return out.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
    }

	/**
	 * Serializes a DOM Element into a String.
	 * @param sourceDocument the Document to be serialized
	 * @param prettyPrint indicates, whether the String will be needed to transmit (not pretty printed) 
	 * or just shown locally (formatted with line breaks and indentation)
	 * @return String
	 */
	public static String getElementString(Element sourceElement, boolean prettyPrint) {
    	if (sourceElement == null) {
    		return null;
    	}
    	
        OutputFormat format = new OutputFormat(Method.XML, "UTF-8", prettyPrint);
        if (prettyPrint) {
        	format.setLineSeparator(System.getProperty("line.separator"));
        	format.setIndenting(true);
        	format.setLineWidth(80);
        	format.setIndent(4);
        } else {
        	format.setIndenting(false);
        }
        format.setOmitXMLDeclaration(true);
        
        StringWriter out = new StringWriter();

        XMLSerializer serializer = new XMLSerializer(out, format);
        try {
			serializer.serialize(sourceElement);
			String escaped = StringUtils.escapeForXML(out.toString());
			return escaped;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
    
    /**
     * Serializes a DOM Document into a File.
     * @param doc the Document to serialize
     * @param file the File to store the document in
     * @return boolean - true if everything went fine
     */
    public static boolean storeDocument(Document sourceDocument, String fileName) {
    	if ((sourceDocument == null) || (fileName == null)) {
    		return false;
    	}
    	
        OutputFormat format = new OutputFormat(sourceDocument);
        format.setLineSeparator(System.getProperty("line.separator"));
        format.setIndenting(true);
        format.setLineWidth(80);
        format.setIndent(4);
        
		try {
			FileWriter out = new FileWriter(fileName);
			XMLSerializer serializer = new XMLSerializer(out, format);
			serializer.serialize(sourceDocument);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
    }
    
    /**
     * Parses the File found under the given path into a DOM Document.
     * @param path the file path
     * @return Document - the parsed DOM Document
     */
	public static Document getDocumentFromPath(String path) {
		return getDocument(new File(path));
	}

    /**
     * Parses the File into a DOM Document.
     * @param file the file to parse
     * @return Document - the parsed DOM Document
     */
	public static Document getDocument(File file) {
		DocumentBuilder builder;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return builder.parse(file);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Creates an empty DOM Document.
	 * @return Document - the created document
	 */
	public static Document createEmptyDocument() {
        DocumentBuilder builder;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return builder.newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns the first direct child element for the given parent node, which
	 * matches the given tagName (probably faster than retrieving all children first).
	 * @param parent the parent node under which to search for the element
	 * @param tagName the tag name of the element to find
	 * @return Element - the first found Element or null if no such Element is present
	 */
	public static Element findFirstChildElement(Node parent, String tagName) {
		Node n = parent.getFirstChild();
		do {
			if ((n.getNodeType() == Node.ELEMENT_NODE) &&
					(n.getNodeName().equals(tagName))) {
				return (Element) n;
			}
			// Android Workaround
			try {
				n = n.getNextSibling();
			} catch (IndexOutOfBoundsException e) {
				n = null;
			}
		} while (n != null);
		return null;
	}
	
	/**
	 * Returns the content of the first child of the given Element
	 * @param element the Element
	 * @return String - content of the Text node
	 */
	public static String getElementText(Element element) {
		Node n = element.getFirstChild();
		if (n.getNodeType() == Node.TEXT_NODE) {
			return ((Text)n).getNodeValue();
		}
		return null;
	}
	
	/**
	 * Parses a String into a DOM Element. Already known namespace prefixes are added to a temporary root node beforehand.
	 * @param ownerDoc the document to create the element for
	 * @param str the string to parse
	 * @return Element - the DOM Element representation of the given XML string
	 */
	public static Element getElement(String str) {
		// xml declaration to be added
		String xmlHead = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		// known namespaces
		String tempRootStart = "<root xmlns:" + CEFXUtil.CEFX_PREFIX + "=\"" + CEFXUtil.CEFX_NAMESPACE + "\">";
		// TODO dynamically add more namespaces here, which occur in the currently edited document and are known after the document download
		String tempRootEnd = "</root>";
		str = xmlHead + tempRootStart + str + tempRootEnd;
		
		System.out.println(str);
        DocumentBuilder builder;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setNamespaceAware(true);		// IMPORTANT
			builder = factory.newDocumentBuilder();
			InputStream is = new ByteArrayInputStream(str.getBytes());
			Document newDoc = builder.parse(is);
			
			Node n = newDoc.getDocumentElement().getFirstChild();
			do {
				if ((n.getNodeType() == Node.ELEMENT_NODE)) {
					return (Element) n;
				}
				// Android Workaround
				try {
					n = n.getNextSibling();
				} catch (IndexOutOfBoundsException e) {
					n = null;
				}
			} while (n != null);
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
