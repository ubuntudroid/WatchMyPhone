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
 * Copyright 2007 Ansgar Gerlicher.
 * @author Ansgar Gerlicher
 * @author Michael Voigt
 */
package de.hdm.cefx.client;

import java.io.Serializable;

/**
 *
 * The CEFXClient contains all information relevant to connecting and
 * identifying the client. The CEFXClient is used to identify each client within
 * an editing session and provide all other clients and the server with the
 * information that is necessary for connecting to it. A CEFXClient object is
 * transmitted over the network. Thus it must implement the
 * java.io.Serializable1 interface, allowing it to serialize a client object
 * into a byte stream, ready for network transport. A clients name, id,
 * hostname, port and connection name are configured in the CEFX network
 * properties file when installing CEFX on a client or server computer and are
 * retrieved from it when the CEFXClient is initialised.
 *
 * @author Ansgar Gerlicher
 */
public interface CEFXClient extends Serializable {
	/**
	 * The method <code>getConnectionString()</code> returns a String object
	 * containing a Unified Resource Identifier (URI). The URI is a compact
	 * string of characters used to identify or name a resource over a network.
	 * The CEFXClient URI consists of the hostname, the port and the connection
	 * name and looks similar to this: <code>//10.21.0.31:3451/CEFXClient</code>
	 * A hostname is the unique name by which a network attached device is known
	 * on a network. In this case the hostname can either be an internet
	 * hostname as defined by the Domain Name System1 (DNS) or an IP address.
	 * The port in this case represents a TCP/IP port on the clients machine and
	 * is separated from the hostname by a colon in the URI.
	 *
	 * @return returns the connection URI.
	 */
	public String getConnectionString();

	/**
	 * The connection name is the name of the client object as it is registered
	 * with the RMI registry service. In this case a client is registered as
	 * CEFXClient with the RMI registry. A server is registered as CEFXServer.
	 *
	 * @return the name of the client as it is registered with the RMI
	 *         registry.
	 */
	public String getConnectionName();

	/**
	 * The method <code>getName()</code> returns a String object containing
	 * the name of the client as it is defined at each client site. The first
	 * client in an editing session for example is named Client1, the second
	 * client is called Client2 and so on.
	 *
	 * @return returns the name of the client.
	 */
	public String getName();

	/**
	 * The port at which the client can be accessed.
	 *
	 * @return returns the TCP/IP port at which the client can be found.
	 */
	public String getPort();

	/**
	 * Returns the hostname of the client's machine.
	 *
	 * @return the hostname of the client.
	 */
	public String getHostName();

	/**
	 * The method getID() returns a String object containing the unique id of
	 * the client.
	 *
	 * @return returns the client's id.
	 */
	public int getID();

	public void setID(int id);

	public String getThreadID();

	public int getCounter();

	public void setCounter(int counter);

	public void init();
	
	public void setConnectionString(String connectionString);
	
}
