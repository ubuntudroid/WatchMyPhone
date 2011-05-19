/**
 * Copyright (C) 2010 Technische Universit�t Dresden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: http://mobilisplatform.sourceforge.net
 */

package de.tudresden.inf.rn.mobilis.xmpp.beans;

/**
 * @author Benjamin S�llner, Robert L�bke
 */
public class Mobilis {

	private Mobilis() {}
	
	public static final String NAMESPACE				= "http://mobilis.inf.tu-dresden.de";
	
	public static final String NAMESPACE_ERROR_STANZA 	= "urn:ietf:params:xml:ns:xmpp-stanzas";
	
	public static final int USERCONTEXT_DATATYPE_UNKNOWN	= 0;
	public static final int USERCONTEXT_DATATYPE_STRING		= 1;
	public static final int USERCONTEXT_DATATYPE_INTEGER	= 2;
	public static final int USERCONTEXT_DATATYPE_DOUBLE		= 3;
	public static final int USERCONTEXT_DATATYPE_LONG		= 4;
	public static final int USERCONTEXT_DATATYPE_BOOLEAN 	= 5;
	
	
}
