/*******************************************************************************
 * Copyright (C) 2010 Technische Universität Dresden
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

package de.tudresden.inf.rn.mobilis.clientservices.socialnetwork.gowalla;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import android.util.Log;

import com.google.gson.Gson;

import de.tudresden.inf.rn.mobilis.clientservices.socialnetwork.SocialNetworkManager;

/**
 * 
 * @author Robert Lübke
 *
 */
public class GowallaManager extends SocialNetworkManager{
	
	/** The TAG for the Log. */
	private final static String TAG = "Gowallaanager";
	
	//Const
	public static final String GOWALLA_CONSUMERKEY		= "078b4a166fcd4885b4c11de2ff073500";
	public static final String GOWALLA_CONSUMERSECRET	= "edeeab189ae7490e91c728abbb569366";
	
	public static final String FOURSQUARE_VENUES_URL		= "http://api.foursquare.com/v1/venues.json";
	public static final String FOURSQUARE_VENUES_LATITUDE	= "geolat=";
	public static final String FOURSQUARE_VENUES_LONGITUDE	= "geolong=";
	public static final String FOURSQUARE_VENUES_LIMIT		= "l=";
	
	public static final String FOURSQUARE_HISTORY_URL		= "http://api.foursquare.com/v1/history.json";
	public static final String FOURSQUARE_HISTORY_LIMIT		= "l=";
	
	public GowallaManager() {	
	}
	
	@Override
	protected String getConsumerKey() {
		return GOWALLA_CONSUMERKEY;
	}

	@Override
	protected String getConsumerSecret() {
		return GOWALLA_CONSUMERSECRET;
	}
	
	@Override
	protected String getAuthentificationURL() {		
		return "";
	}

	@Override
	protected String getAccessTokenURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getAuthorizeURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getRequestTokenURL() {
		// TODO Auto-generated method stub
		return null;
	}
	
	// ************************* //
	// Qype functionality: //
	// ************************* //
	

	
	
}
