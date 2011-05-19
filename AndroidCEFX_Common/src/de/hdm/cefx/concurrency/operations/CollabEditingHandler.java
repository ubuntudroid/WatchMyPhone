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
package de.hdm.cefx.concurrency.operations;

import jabberSrpc.MucRoomListener;
import de.hdm.cefx.client.net.CEFXSession;
import de.hdm.cefx.client.net.RemoteOperationExecutor;

public abstract class CollabEditingHandler implements OperationHandler, MucRoomListener {
	
	protected RemoteOperationExecutor executor;
	protected CEFXSession session;
	protected int clientID;
	
	public CollabEditingHandler(RemoteOperationExecutor executor, CEFXSession currentSession, int clientID) {
		this.executor = executor;
		this.session = currentSession;
		this.clientID = clientID;
	}
	
	public void setCurrentSession(CEFXSession currentSession) {
		session = currentSession;
	}

}
