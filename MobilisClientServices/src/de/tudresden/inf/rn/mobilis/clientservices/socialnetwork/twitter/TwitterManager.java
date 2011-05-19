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

package de.tudresden.inf.rn.mobilis.clientservices.socialnetwork.twitter;

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
public class TwitterManager extends SocialNetworkManager{
	
	/** The TAG for the Log. */
	private final static String TAG = "TwitterManager";
	
	//Const
	public static final String TWITTER_CONSUMERKEY		= "0QGNbwhiXod3jS5jynvY9A";
	public static final String TWITTER_CONSUMERSECRET	= "C35I3CMjadhBbK1O83TSmRymhzO5vzjrwoFvwqVPpXY";
	
	public static final String FOURSQUARE_VENUES_URL		= "http://api.foursquare.com/v1/venues.json";
	public static final String FOURSQUARE_VENUES_LATITUDE	= "geolat=";
	public static final String FOURSQUARE_VENUES_LONGITUDE	= "geolong=";
	public static final String FOURSQUARE_VENUES_LIMIT		= "l=";
	
	public static final String FOURSQUARE_HISTORY_URL		= "http://api.foursquare.com/v1/history.json";
	public static final String FOURSQUARE_HISTORY_LIMIT		= "l=";
	
	public TwitterManager() {	
	}
	
	@Override
	protected String getConsumerKey() {
		return TWITTER_CONSUMERKEY;
	}

	@Override
	protected String getConsumerSecret() {
		return TWITTER_CONSUMERSECRET;
	}
	
	@Override
	protected String getAuthentificationURL() {		
		return null;
	}

	@Override
	protected String getAccessTokenURL() {
		return "http://twitter.com/oauth/access_token";
	}

	@Override
	protected String getAuthorizeURL() {
		return "http://twitter.com/oauth/authorize";
	}

	@Override
	protected String getRequestTokenURL() {		
		return "http://twitter.com/oauth/request_token";
	}
	
	// ************************* //
	// Twitter functionality: //
	// ************************* //
	

	
	
}
