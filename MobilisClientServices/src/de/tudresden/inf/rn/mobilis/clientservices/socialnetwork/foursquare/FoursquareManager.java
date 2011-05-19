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

package de.tudresden.inf.rn.mobilis.clientservices.socialnetwork.foursquare;

import java.io.IOException;
import java.io.InputStream;
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
public class FoursquareManager extends SocialNetworkManager{
	
	/** The TAG for the Log. */
	private final static String TAG = "FoursquareManager";
	
	//Const
	public static final String FOURSQUARE_CONSUMERKEY		= "TRVV42DLXBFYKSQQOAFIDIXKMLDFMXWNGYHJUCTRA3GK5RNA";
	public static final String FOURSQUARE_CONSUMERSECRET	= "5NX3HAIQCX0HZZYXGNFGHMD3TA3MVXMYTBHJMH5OBF1FQWCV";
	
	public static final String FOURSQUARE_VENUES_URL		= "http://api.foursquare.com/v1/venues.json";
	public static final String FOURSQUARE_VENUES_LATITUDE	= "geolat=";
	public static final String FOURSQUARE_VENUES_LONGITUDE	= "geolong=";
	public static final String FOURSQUARE_VENUES_LIMIT		= "l=";
	
	public static final String FOURSQUARE_HISTORY_URL		= "http://api.foursquare.com/v1/history.json";
	public static final String FOURSQUARE_HISTORY_LIMIT		= "l=";
	
	public FoursquareManager() {	
	}
	
	@Override
	protected String getConsumerKey() {
		return FOURSQUARE_CONSUMERKEY;
	}

	@Override
	protected String getConsumerSecret() {
		return FOURSQUARE_CONSUMERSECRET;
	}
	
	@Override
	protected String getAuthentificationURL() {		
		String url= "http://api.foursquare.com/v1/authexchange" +
			"?fs_username=" + userName + "&fs_password=" + userPassword;
		return url;
	}
	
	// ************************* //
	// Foursquare functionality: //
	// ************************* //
	
	public List<Venue> getNearbyVenues(double longitude, double latitude, int limit) throws ClientProtocolException, URISyntaxException, IOException {
		String url= FOURSQUARE_VENUES_URL + "?"
				+ FOURSQUARE_VENUES_LATITUDE
				+ latitude + "&"
				+ FOURSQUARE_VENUES_LONGITUDE
				+ longitude;		
		if (limit>0) url += "&" + FOURSQUARE_VENUES_LIMIT + limit;
		Log.i(TAG, url);
		
		List<Venue> foursquareVenues = null;
        Gson gson = new Gson();
        InputStream input = makeRequestAndGetJSONData(url);
        if (input!=null) {
	        Reader r = new InputStreamReader(input);	        
	        Group g = gson.fromJson(r, Group.class);
	        foursquareVenues = new ArrayList<Venue>();
	        if (g!=null)
		        for (Venues venues : g.getGroups()) { 
		        	if (venues!=null)
				        for(Venue v : venues.getVenues()){
				            Log.i("VENUES", v.getName() + " - " + v.getId());
				            foursquareVenues.add(v);
				        }
		        }
        }
        return foursquareVenues;	        
        
	}	
	
	
	public List<Venue> getFoursquareCheckinHistory(int limit) {
		String url= FOURSQUARE_HISTORY_URL;
		if (limit>0) url += "?" + FOURSQUARE_HISTORY_LIMIT + limit;
		Log.i(TAG, url);
		
		try {	        
	        Gson gson = new Gson();	        
	        Reader r = new InputStreamReader(makeSignedRequestAndGetJSONData(url));
	        Checkins c = gson.fromJson(r, Checkins.class);
	        List<Venue> foursquareVenues = new ArrayList<Venue>();	        
	        for (Checkin checkin : c.getCheckins()) {   
		        Venue v = checkin.getVenue();
		        Log.i("HISTORY-VENUE", v.getName() + " - " + v.getId());
		        foursquareVenues.add(v);
	        }
	        return foursquareVenues;	        
        } catch(Exception ex) {  
        	
        	ex.printStackTrace();       	        	
        	return null;
        }
	}

	@Override
	protected String getAccessTokenURL() {		
		return "http://foursquare.com/oauth/access_token";
	}

	@Override
	protected String getAuthorizeURL() {
		return "http://foursquare.com/oauth/authorize";
	}

	@Override
	protected String getRequestTokenURL() {		
		return "http://foursquare.com/oauth/request_token";
	}

	
	
}
