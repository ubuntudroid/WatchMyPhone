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
package de.tudresden.inf.rn.mobilis.android.util;

/**
 * Provides internally used constants for building namespaces, checking values, ...  
 * Constant strings should not be printed out on the screen, use the Android 
 * resource manager with res/values/strings.xml instead.
 * 
 * @author Dirk
 */
public class Const {

    // internal namespace prefixes
    public static final String MOBILIS_PREFIX        = "de.tudresden.inf.rn.mobilis.";
    public static final String MOBILIS_SERVER_PREFIX = "de.tudresden.inf.rn.mobilis.server.";
    public static final String ANDROID_PREFIX        = "de.tudresden.inf.rn.mobilis.android.";
    public static final String XMPP_PREFIX           = "de.tudresden.inf.rn.mobilis.xmpp.";
    public static final String INTENT_PREFIX         = "de.tudresden.inf.rn.mobilis.android.intent.";
    public static final String SAVED_PREFIX          = "de.tudresden.inf.rn.mobilis.android.savedinstance.";
    
    // temporary additional preference file for this application
    public static final String PREFERENCES           = "de.tudresden.inf.rn.mobilis.android_preferences";
    
    // name for the mobilis server as location provider
    public static final String MOBILIS_LOCATION_PROVIDER = "de.tudresden.inf.rn.mobilis.server.locationprovider";
    
    // internal network names, only for identification / checks / ... not for printing
    public static final String LOCALDB = "localdb";
    public static final String MOBILIS = "mobilis";
    public static final String FACEBOOK = "facebook";
    
}
