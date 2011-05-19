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

package de.hdm.cefx.client.net;

import org.w3c.dom.Element;

import de.hdm.cefx.util.CEFXUtil;
import de.hdm.cefx.util.XMLHelper;

public class InsertExtension extends OperationExtension {

	private Element element;

	public String getNamespace() {
		return super.getNamespace()+OperationExtension.INSERT;
	}

	public Element getElement() {
		return element;
	}

	public void setElement(Element element) {
		this.element = element;
	}

	private String Element2String() {
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        OutputStreamWriter steamWriter;
//		try {
//			steamWriter = new OutputStreamWriter(out,"UTF-8");
//	        XMLWriterImpl writer = new XMLWriterImpl();
//	        writer.setWriter(steamWriter);
//
//	        NamedNodeMap nnm=element.getAttributes();
//	        AttributesImpl a=new AttributesImpl();
//	        int ii;
//	        for (ii=0; ii<nnm.getLength(); ii++) {
//	        	Attr n=(Attr)nnm.item(ii);
//	        	String pre=n.getPrefix();
//	        	String name=n.getName();
//	        	if ((pre==null) || ("".equals(pre))) {
//	        		pre = DOM3Methods.lookupPrefix(n, n.getNamespaceURI());
//	        	}
////	        	if (pre!=null) {
////	        		name=pre+':'+name;
////	        	}
//	        	a.addAttribute(n.getNamespaceURI(),n.getLocalName(),name, "", n.getValue());
//	        }
//	        writer.startElement(null,null,element.getTagName(),a);
//
//	        writer.endElement("x");
//	        steamWriter.flush();
//	        return 	out.toString();
//		} catch (UnsupportedEncodingException e1) {
//			e1.printStackTrace();
//		} catch (SAXException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return "";
		
////		// add CEFX namespace to the element (needed for parsing it again on the other site)
////		element.setAttribute("xmlns:" + CEFXUtil.CEFX_PREFIX, CEFXUtil.CEFX_NAMESPACE);
////		
////		// serialize the Element
////		String elStr = XMLHelper.getElementString(element, false);
////		
////		return elStr;

		// CURRENTLY, THIS METHOD IS NOT USED - the serialized inserted element is transmitted in the message body (see OperationXMLTransformer)
		return XMLHelper.getElementString(element, false);
		
//		return "";	
	}

	public String toXML() {
		setContent(Element2String());
		return super.toXML();
	}

	public void setParameterValue(String parameter,String val) {
		super.setParameterValue(parameter, val);
	}
}
