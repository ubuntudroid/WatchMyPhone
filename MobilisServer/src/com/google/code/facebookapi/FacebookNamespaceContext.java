package com.google.code.facebookapi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

/**
 * This NamespaceContext provides the namespaces used by Facebook.
 * 
 * Use xpath.setNamespaceContext(new FacebookNamespaceContext()) on your
 * javax.xml.xpath.XPath object so that you can evaluate XPath expressions using
 * prefixes like:
 * 
 * xpath.evaluate("fbapi:fql_query_response", document);
 * 
 * @author david.j.boden
 */
public class FacebookNamespaceContext implements NamespaceContext {

	private static final int NUMBER_OF_NAMESPACES_HINT = 2;
	private static Map<String, String> prefixToNamespace = new HashMap<String, String>(
			NUMBER_OF_NAMESPACES_HINT);
	private static Map<String, String> namespaceToPrefix = new HashMap<String, String>(
			NUMBER_OF_NAMESPACES_HINT);
	static {
		add("fb", "http://apps.facebook.com/ns/1.0");
		add("fbapi", "http://api.facebook.com/1.0/");
	}

	private static void add(String prefix, String namespace) {
		prefixToNamespace.put(prefix, namespace);
		namespaceToPrefix.put(namespace, prefix);
	}

	public String getNamespaceURI(String prefix) {
		return prefixToNamespace.get(prefix);
	}

	public String getPrefix(String namespaceURI) {
		return namespaceToPrefix.get(namespaceURI);
	}

	public Iterator<String> getPrefixes(String namespaceURI) {
		return Collections.singletonList(namespaceToPrefix.get(namespaceURI))
				.iterator();
	}

}