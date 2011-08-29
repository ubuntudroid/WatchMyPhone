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
package de.hdm.cefx.concurrency.operations;

@SuppressWarnings("serial")
public class UpdateInsertOperation extends UpdateOperations {

	private int     textPos;
	private int     undoTextPos;
	private String  text;

	public UpdateInsertOperation() {
		textPos=0;
		text="";
	}

	public UpdateInsertOperation(String text,int textPos, int nodeType, NodePosition nodePosition, String attributName) {
		operation=UpdateOperations.INSERT;
		this.text=text;
		this.textPos=textPos;
		this.undoTextPos=textPos;
		this.nodeType=nodeType;
		this.nodePosition=nodePosition;
		this.attributName=attributName;
	}

	public void undoTransformation() {
		textPos=undoTextPos;
	}

	public boolean isReady() {
		if (textPos<0) return false;
		if (nodePosition==null) return false;
		return super.isReady();
	}

	public int getTextPos() {
		return textPos;
	}

	public void setTextPos(int textPos) {
		this.textPos = textPos;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
