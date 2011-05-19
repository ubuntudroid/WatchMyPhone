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

package de.tudresden.inf.rn.mobilis.clientservices.socialnetwork.qype;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.http.client.ClientProtocolException;

import android.util.Log;

import com.google.gson.Gson;

import de.tudresden.inf.rn.mobilis.clientservices.socialnetwork.SocialNetworkManager;
import de.tudresden.inf.rn.mobilis.clientservices.socialnetwork.foursquare.Group;
import de.tudresden.inf.rn.mobilis.clientservices.socialnetwork.foursquare.Venue;
import de.tudresden.inf.rn.mobilis.clientservices.socialnetwork.foursquare.Venues;

/**
 * 
 * @author Robert Lübke
 *
 */
public class QypeManager extends SocialNetworkManager{
	
	/** The TAG for the Log. */
	private final static String TAG = "QypeManager";
	
	//Const
	public static final int ORDER_DISTANCE				= 1;
	public static final int ORDER_RATING				= 2;
	
	private static final String QYPE_CONSUMERKEY		= "7AaOjeoMw7DyDqskHc4rbQ";
	private static final String QYPE_CONSUMERSECRET	= "rQCNApFViqdDCMLSlg1RN9227ymko2JSM2f18YYTQmA";
	
	private static final String QYPE_PLACES_URL_1		= "http://api.qype.com/v1/positions/";
	private static final String QYPE_PLACES_URL_2		= "/places.json";
	private static final String QYPE_BOUNDINGBOX_URL_1 	= "http://api.qype.com/v1/bounding_boxes/";
	private static final String QYPE_BOUNDINGBOX_URL_2	= "/places.json";
	private static final String QYPE_ATTR_CONSUMERKEY	= "consumer_key=";
	private static final String QYPE_ATTR_ORDER			= "order=";
	private static final String QYPE_ATTR_RADIUS		= "radius=";
	private static final String QYPE_ATTR_SEARCH		= "show=";
	private static final String QYPE_ATTR_CATEGORY		= "in_category=";
	
	
	public QypeManager() {	
	}
	
	@Override
	protected String getConsumerKey() {
		return QYPE_CONSUMERKEY;
	}

	@Override
	protected String getConsumerSecret() {
		return QYPE_CONSUMERSECRET;
	}
	
	@Override
	protected String getAuthentificationURL() {		
		return null;
	}

	@Override
	protected String getAccessTokenURL() {
		return "http://api.qype.com/oauth/access_token";
	}

	@Override
	protected String getAuthorizeURL() {
		return "http://www.qype.com/mobile/authorize";
	}

	@Override
	protected String getRequestTokenURL() {		
		return "http://api.qype.com/oauth/request_token";
	}
	
	// ************************* //
	// Qype functionality:       //
	// ************************* //

	/**
	 * Returns a list of nearby Qype Places.
	 * @param longitude Longitude of the location where to search.
	 * @param latitude Latitude of the location where to search.
	 * @param radius Radius in km within the search should be done. Possible values: 1..10.
	 * Any other value (for example 0) simply disables radius search.
	 * @param order order the results by distance (QypeManager.ORDER_DISTANCE) or
	 * rating (QypeManager.ORDER_RATING). Pass 0 for default order (distance). 
	 * @param searchTerm Search for nearby places matching this String. Pass 'null' to
	 * disable searching.
	 * @param qypeCategoryId Only show nearby places in one or more categories
	 * (comma-separated). This doesn't work together with the searchTerm parameter.
	 * Pass 'null' to disable searching in categories.
	 * @return a list of nearby Qype Places.
	 * @throws ClientProtocolException
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public List<Place> getNearbyPlaces(double longitude, double latitude, int radius ,int order, String searchTerm, String qypeCategoryId) throws ClientProtocolException, URISyntaxException, IOException {		
		String url= QYPE_PLACES_URL_1 + latitude + "," + longitude
			+ QYPE_PLACES_URL_2 + "?" + QYPE_ATTR_CONSUMERKEY
			+ QYPE_CONSUMERKEY;
		if (order == ORDER_RATING)
			url += "&" + QYPE_ATTR_ORDER + "rating";
		else
			url += "&" + QYPE_ATTR_ORDER + "distance";
		if (searchTerm!=null)
			url += "&" + QYPE_ATTR_SEARCH + searchTerm;
		else if (qypeCategoryId!=null)
			url += "&" + QYPE_ATTR_CATEGORY + qypeCategoryId;
		if (radius>0 && radius<=10)
			url += "&" + QYPE_ATTR_RADIUS + radius;	
		
		Log.v(TAG, url);
		
		List<Place> qypePlaces = null;
		Gson gson = new Gson();
		InputStream input = makeRequestAndGetJSONData(url);
		if (input!=null) {
		    Reader reader = new InputStreamReader(input);	        
		    Results results = gson.fromJson(reader, Results.class);
		    qypePlaces = new ArrayList<Place>();
		    if (results!=null)
		        for (Result r : results.getResults())
		        	if (r.getPlace()!=null) {
		        		Place p = r.getPlace();
			            Log.v("PLACES", p.getTitle() + " - " + p.getPoint());
			            String point = p.getPoint();
			            StringTokenizer st = new StringTokenizer(point, ",");
			            double lat = Double.parseDouble(st.nextToken());	
			            double lon = Double.parseDouble(st.nextToken());
			            p.setLatitutde(lat);
			            p.setLongitude(lon);
			            qypePlaces.add(p);				       
		        }
		}
		return qypePlaces;		
	}
	
	
	/**
	 * Returns a list of all Qype Places within a rectangular region (bounding box),
	 * which is defined by two latitude-longitude pairs.
	 * 
	 * @param longitudeSW Longitudeof the left lower corner (south-west)
	 * @param latitudeSW Latitude of the left lower corner (south-west)
	 * @param longitudeNE Longitude of the right upper corner (north east)
	 * @param latitudeNE Latitude pair of the right upper corner (north east)
	 * @param order the results by distance (QypeManager.ORDER_DISTANCE) or
	 * rating (QypeManager.ORDER_RATING). Pass 0 for default order (distance). 
	 * @param searchTerm Search for nearby places matching this String. Pass 'null' to
	 * disable searching.
	 * @param qypeCategoryId Only show nearby places in one or more categories
	 * (comma-separated). This doesn't work together with the searchTerm parameter.
	 * Pass 'null' to disable searching in categories.	
	 * @return a list of all Qype Places within the given bounding box.
	 * @throws ClientProtocolException
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public List<Place> getAllPlacesInBoundingBox(double longitudeSW, double latitudeSW, double longitudeNE, double latitudeNE, int order, String searchTerm, String qypeCategoryId) throws ClientProtocolException, URISyntaxException, IOException {		
		String url= QYPE_BOUNDINGBOX_URL_1 + latitudeSW + "," + longitudeSW
			+ "," + latitudeNE + "," + longitudeNE
			+ QYPE_BOUNDINGBOX_URL_2 + "?" + QYPE_ATTR_CONSUMERKEY
			+ QYPE_CONSUMERKEY;
		if (order == ORDER_RATING)
			url += "&" + QYPE_ATTR_ORDER + "rating";		
		if (searchTerm!=null)
			url += "&" + QYPE_ATTR_SEARCH + searchTerm;
		else if (qypeCategoryId!=null)
			url += "&" + QYPE_ATTR_CATEGORY + qypeCategoryId;
				
		Log.v(TAG, url);
		
		List<Place> qypePlaces = null;
		Gson gson = new Gson();
		InputStream input = makeRequestAndGetJSONData(url);
		if (input!=null) {
		    Reader reader = new InputStreamReader(input);	        
		    Results results = gson.fromJson(reader, Results.class);
		    qypePlaces = new ArrayList<Place>();
		    if (results!=null)
		        for (Result r : results.getResults())
		        	if (r.getPlace()!=null) {
		        		Place p = r.getPlace();
			            Log.v("PLACES", p.getTitle() + " - " + p.getPoint());
			            String point = p.getPoint();
			            StringTokenizer st = new StringTokenizer(point, ",");
			            double lat = Double.parseDouble(st.nextToken());	
			            double lon = Double.parseDouble(st.nextToken());
			            p.setLatitutde(lat);
			            p.setLongitude(lon);
			            qypePlaces.add(p);				       
		        }
		}
		return qypePlaces;		
	}
	

	
	
}
