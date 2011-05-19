package com.google.code.facebookapi;

import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

public class XMLTestUtils {

	public static void print(Node dom) {
		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer t = factory.newTransformer();
			StringWriter sw = new StringWriter();
			t.transform(new DOMSource(dom), new StreamResult(sw));
			System.out.println(sw.toString());
		} catch(Exception ex) {
			System.out.println("Could not print XML document");
		}
	}
	
}
