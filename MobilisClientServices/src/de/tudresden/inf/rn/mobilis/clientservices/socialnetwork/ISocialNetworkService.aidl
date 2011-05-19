/**
 * Copyright (C) 2010 Technische Universität Dresden
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

package de.tudresden.inf.rn.mobilis.clientservices.socialnetwork;

import de.tudresden.inf.rn.mobilis.clientservices.socialnetwork.foursquare.Venue;
import de.tudresden.inf.rn.mobilis.clientservices.socialnetwork.qype.Place;

/**
 * @author Robert Lübke
 */
interface ISocialNetworkService {
   
    List<Venue> getFoursquareNearbyVenues(double longitude, double latitude, int limit);
    
	List<Venue> getFoursquareCheckinHistory(int limit);
	
	void setFoursquareUserCredentials(String userName, String userPassword);	
		
	/**
	 * Makes a request to the Qype-API and returns a list of nearby Qype Places.
	 *
	 * @param longitude Longitude of the location where to search.
	 * @param latitude Latitude of the location where to search.
	 * @param radius Radius in km within the search should be done. Possible values: 1..10.
	 * Any other value (for example 0) simply disables radius search.
	 * @param order order the results by distance (QypeManager.ORDER_DISTANCE) or
	 * rating (QypeManager.ORDER_RATING). Pass 0 for default order (distance). 
	 * @param searchTerm Search for nearby places matching this String. Pass 'null' to
	 * disable searching.	 
	 * @return a list of nearby Qype Places from the Qype-API.	 
	 */
	List<Place> getQypeNearbyPlacesWithSearch(double longitude, double latitude, int radius, int order, String searchTerm);
	
	/**
	 * Makes a request to the Qype-API and returns a list of nearby Qype Places.
	 *
	 * @param longitude Longitude of the location where to search.
	 * @param latitude Latitude of the location where to search.
	 * @param radius Radius in km within the search should be done. Possible values: 1..10.
	 * Any other value (for example 0) simply disables radius search.
	 * @param order order the results by distance (QypeManager.ORDER_DISTANCE) or
	 * rating (QypeManager.ORDER_RATING). Pass 0 for default order (distance).	 
	 * @param qypeCategoryId Only show nearby places in one or more categories
	 * (comma-separated). Pass 'null' to disable searching in categories.
	 * @return a list of nearby Qype Places.
	 */
	List<Place> getQypeNearbyPlaces(double longitude, double latitude, int radius, int order, String qypeCategoryId);
	
	/**
	 * Returns a list of all Qype Places within a rectangular region (bounding box),
	 * which is defined by two latitude-longitude pairs.
	 * 
	 * @param longitudeSW Longitudeof the left lower corner (south-west)
	 * @param latitudeSW Latitude of the left lower corner (south-west)
	 * @param longitudeNE Longitude of the right upper corner (north east)
	 * @param latitudeNE Latitude pair of the right upper corner (north east)
	 * @param order the results by rating (QypeManager.ORDER_RATING). Pass 0 for default order (unordered).
	 * @param qypeCategoryId Only show nearby places in one or more categories
	 * (comma-separated). This doesn't work together with the searchTerm parameter.
	 * Pass 'null' to disable searching in categories.	
	 * @return a list of all Qype Places within the given bounding box.
	 */
	List<Place> getQypeAllPlacesInBoundingBox(double longitudeSW, double latitudeSW, double longitudeNE, double latitudeNE, int order, String qypeCategoryId);
	
	/**
	 * Returns a list of all Qype Places within a rectangular region (bounding box),
	 * which is defined by two latitude-longitude pairs.
	 * 
	 * @param longitudeSW Longitudeof the left lower corner (south-west)
	 * @param latitudeSW Latitude of the left lower corner (south-west)
	 * @param longitudeNE Longitude of the right upper corner (north east)
	 * @param latitudeNE Latitude pair of the right upper corner (north east)
	 * @param order the results by rating (QypeManager.ORDER_RATING). Pass 0 for default order (unordered). 
	 * @param searchTerm Search for nearby places matching this String. Pass 'null' to
	 * disable searching.
	 * Pass 'null' to disable searching in categories.	
	 * @return a list of all Qype Places within the given bounding box.
	 */
	List<Place> getQypeAllPlacesInBoundingBoxWithSearch(double longitudeSW, double latitudeSW, double longitudeNE, double latitudeNE, int order, String searchTerm);
	
	
	void testLoginOAuth2();	
}