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
public class UpdateDeleteOperation extends UpdateOperations {

	private int textPos;
	private int length;
	private int undoTextPos;
	private int undoLength;

	public UpdateDeleteOperation() {
		textPos=0;
		length=0;
	}

	public UpdateDeleteOperation(int textPos,int length, int nodeType, NodePosition nodePosition, String attributName) {
		operation=UpdateOperations.DELETE;
		this.length=length;
		this.undoLength=length;
		this.textPos=textPos;
		this.undoTextPos=textPos;
		this.nodeType=nodeType;
		this.nodePosition=nodePosition;
		this.attributName=attributName;
	}

	public void undoTransformation() {
		textPos=undoTextPos;
		length=undoLength;
	}

	public boolean isReady() {
		if (textPos<0) return false;
		if (length<0) return false;
		return super.isReady();
	}

	public int getTextPos() {
		return textPos;
	}

	public void setTextPos(int textPos) {
		this.textPos = textPos;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}


}
