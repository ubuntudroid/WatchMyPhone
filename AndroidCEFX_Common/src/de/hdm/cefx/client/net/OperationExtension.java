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

import java.util.Arrays;
import java.util.Hashtable;

import org.jivesoftware.smack.packet.PacketExtension;

import de.hdm.cefx.concurrency.operations.StateVector;

public class OperationExtension implements PacketExtension {

	public static final String INSERT="ins";
	public static final String DELETE="del";
	public static final String UPDATE_DELETE="ud";
	public static final String UPDATE_INSERT="ui";
	public static final String UPDATE_SET="us";

	public static final String NAMESPACE="jabber:x:cefx#";

	public static final String TEXT_TYPE="txt";
	public static final String ATTR_TYPE="attr";

	public static final String ATTRNAME_PARENT="p";
	public static final String ATTRNAME_FIXNODE="fn";
	public static final String ATTRNAME_STATEVECTOR="sv";
	public static final String ATTRNAME_NODETYPE="nt";
	public static final String ATTRNAME_ATTRNAME="an";
	public static final String ATTRNAME_POSITION="pos";
	public static final String ATTRNAME_LENGTH="len";
	public static final String ATTRNAME_BEFOREAFTER="ba";
	public static final String ATTRNAME_CLIENTID="ci";
	public static final String ATTRNAME_DOMLEVEL="dl";

	protected Hashtable<String, String> attr;

	private String            content;
	private StateVector       stateVector;
	private String            parentID;
	private String            fixNodeID;
	private String            type;
	private String            attrName;
	private int               beforeAfter;
	private int               cliendID;

	public OperationExtension() {
		attr=new Hashtable<String, String>();
		content="";
		type=null;
		fixNodeID=null;
		parentID="";
		attrName=null;
		beforeAfter=-1;
		stateVector=null;
	}

	public String getElementName() {
		return "x";
	}

	public String getNamespace() {
//TODO duch anderen Namensraum ersetzen
		return NAMESPACE;
	}

	private String stateVector2String() {
		String result="";

		Integer[] keys=(Integer[])stateVector.keySet().toArray(new Integer[0]);
		Arrays.sort(keys);
		int ii;
		int key=-1;
		int lastKey=-1;
		int val;
		for (ii=0; ii<keys.length; ii++) {
			lastKey=key;
			key=keys[ii];
			val=stateVector.getState(keys[ii]);
			boolean insertID=false;
			if (ii==0) {
				if (key!=0) {
					insertID=true;
				}
			} else {
				if (key!=lastKey+1) {
					insertID=true;
				}
			}
			String tmp="";
			if (ii>0) {
				tmp=",";
			}
			tmp=tmp+Integer.toHexString(val);
			if (insertID) {
				tmp=tmp+":"+Integer.toHexString(key);
			}
			result=result+tmp;
		}

		return result;
	}

	public void setStateVector(String text) {
		if (stateVector==null) {
			stateVector=new StateVector();
		}
		stateVector.clear();
		String[] t=text.split(",");
		int id=-1;
		int val;
		int ii;
		for (ii=0; ii<t.length; ii++) {
			id=id+1;
			String tmp=new String(t[ii]);
			if (tmp.contains(":")) {
				String[] s=tmp.split(":");
				if (s.length>=2) {
					id=Integer.parseInt(s[1],16);
				}
				tmp=s[0];
			}
			val=Integer.parseInt(tmp, 16);
			stateVector.put(id, val);
		}
	}

	/**
	 * Returns a String of the form attribName="attribValue"
	 * @param attrName the name of the attribute
	 * @param attrValue the value of the attribute
	 * @return String
	 */
	public static String getAS(String attrName, String attrValue) {
		return attrName + "=\"" + attrValue + "\" ";
	}
	
	public String toXML() {
		
		StringBuilder builder = new StringBuilder();
		builder.append("<x ");
		builder.append(getAS("xmlns", getNamespace()));
		builder.append(getAS(ATTRNAME_PARENT, parentID));
		builder.append(getAS(ATTRNAME_CLIENTID, Integer.toHexString(cliendID)));
		builder.append(getAS(ATTRNAME_STATEVECTOR, stateVector2String()));
		if (beforeAfter!=-1) builder.append(getAS(ATTRNAME_BEFOREAFTER, Integer.toHexString(beforeAfter)));
		if ((type!=null) && (type.length()>0)) builder.append(getAS(ATTRNAME_NODETYPE, type));
		if ((attrName!=null) && (attrName.length()>0)) builder.append(getAS(ATTRNAME_ATTRNAME, attrName));
		builder.append(getAS(ATTRNAME_FIXNODE, fixNodeID));
		builder.append(">");
		builder.append(content); // currently in message body! XXX: commented in by sven
		builder.append("</x>");
		
		return builder.toString();
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public StateVector getStateVector() {
		return stateVector;
	}

	public void setStateVector(StateVector stateVector) {
		this.stateVector = stateVector;
	}

	public String getParentID() {
		return parentID;
	}

	public void setParentID(String parentID) {
		this.parentID = parentID;
	}

	public String getFixNodeID() {
		return fixNodeID;
	}

	public void setFixNodeID(String fixNodeID) {
		this.fixNodeID = fixNodeID;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAttrName() {
		return attrName;
	}

	public void setAttrName(String attrName) {
		this.attrName = attrName;
	}

	public int getBeforeAfter() {
		return beforeAfter;
	}

	public void setBeforeAfter(int beforeAfter) {
		this.beforeAfter = beforeAfter;
	}

	public int getCliendID() {
		return cliendID;
	}

	public void setCliendID(int cliendID) {
		this.cliendID = cliendID;
	}

	public void setParameterValue(String parameter,String val) {
		if (parameter.equals(ATTRNAME_STATEVECTOR)) {
			setStateVector(val);
		} else if (parameter.equals(ATTRNAME_PARENT)) {
			setParentID(val);
		} else if (parameter.equals(ATTRNAME_FIXNODE)) {
			setFixNodeID(val);
		} else if (parameter.equals(ATTRNAME_NODETYPE)) {
			setType(val);
		} else if (parameter.equals(ATTRNAME_ATTRNAME)) {
			setAttrName(val);
		} else if (parameter.equals(ATTRNAME_BEFOREAFTER)) {
			setBeforeAfter(Integer.parseInt(val, 16));
		} else if (parameter.equals(ATTRNAME_CLIENTID)) {
			setCliendID(Integer.parseInt(val, 16));
		}
	}
}
