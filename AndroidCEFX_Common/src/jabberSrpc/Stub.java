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
package jabberSrpc;


public class Stub {

	long         timeOut=300000;
	String       target;
	String       className;
	String       threadID;
	JabberClient jabberClient;

	public Stub(JabberClient client,String cName) {
		jabberClient=client;
		className=cName;
		threadID=null;
	}

	protected Object sendRPC(String methodName,Object o,String targetJID,boolean waitForResponse) {
		if (targetJID==null) return null;
		Object resultObject=null;

		if (!jabberClient.isConnected()) return null;
		String call=className+"."+methodName;
		if (threadID!=null) {
			call=call+'@'+threadID;
		}
		if (waitForResponse) {
			resultObject=jabberClient.callB(call, o, targetJID, timeOut);
		} else {
			jabberClient.callNB(call, o, targetJID);
		}

		return resultObject;
	}

	protected Object sendRPC(String methodName,Object o,boolean waitForResponse) {
		return sendRPC(methodName,o, target,waitForResponse);
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public long getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

	public String getThreadID() {
		return threadID;
	}

	public void setThreadID(String threadID) {
		this.threadID = threadID;
	}

}
