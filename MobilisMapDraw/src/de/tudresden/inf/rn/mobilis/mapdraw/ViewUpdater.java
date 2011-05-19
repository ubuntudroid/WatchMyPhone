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

import de.hdm.cefx.awareness.AwarenessController;
import de.hdm.cefx.awareness.AwarenessEvent;
import de.hdm.cefx.awareness.AwarenessWidget;
import de.hdm.cefx.concurrency.operations.Operation;
import de.hdm.cefx.concurrency.operations.OperationData;

public abstract class ViewUpdater implements AwarenessWidget {

	protected Operation operation;
	private AwarenessController ac;

	@Override
	public boolean hasInterestIn(AwarenessEvent event) {
		Object eventData = event.getEvent();
		if (eventData instanceof OperationData) {
			operation = ((OperationData) eventData).getOperation();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void init() {
		// possible to add / override code for initialization of this awareness widget ...
	}

	@Override
	public void setAwarenessController(AwarenessController ac) {
		this.ac = ac;
	}

	@Override
	public abstract void notifyOfAwarenessEvent(AwarenessEvent event);
}
