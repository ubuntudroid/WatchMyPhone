package de.tudresden.inf.rn.mobilis.mxa.services.collabedit;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class DocumentFactory {
	public DocumentFactory() {
	}

	public static Document createDocument(String uri) throws SAXException,
			IOException {
		Document d = null;
		DocumentBuilder docB = null;
		try {
			docB = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		if (docB != null) {
			File file = new File(uri);
			d = docB.parse(file);
		}
		return d;
	}

}
