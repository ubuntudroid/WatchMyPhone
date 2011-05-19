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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.AndroidException;
import de.tudresden.inf.rn.mobilis.clientservices.AuthRemoteException;
import de.tudresden.inf.rn.mobilis.clientservices.socialnetwork.foursquare.FoursquareManager;
import de.tudresden.inf.rn.mobilis.clientservices.socialnetwork.foursquare.Venue;
import de.tudresden.inf.rn.mobilis.clientservices.socialnetwork.qype.Place;
import de.tudresden.inf.rn.mobilis.clientservices.socialnetwork.qype.QypeManager;
import de.tudresden.inf.rn.mobilis.clientservices.socialnetwork.twitter.TwitterManager;

/**
 * @author Robert Lübke
 */
public class SocialNetworkService extends Service {

	private static final String TAG = "SocialNetworkService";
	
	private FoursquareManager mFoursquareManager;	
	private TwitterManager mTwitterManager;
	private QypeManager mQypeManager;
	
	public SocialNetworkService() {		
		mFoursquareManager = new FoursquareManager();
		mTwitterManager = new TwitterManager();
		mQypeManager = new QypeManager();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent i) {
		return mBinder;
	}

	private final ISocialNetworkService.Stub mBinder = new ISocialNetworkService.Stub() {
		
		@Override
		public List<Venue> getFoursquareNearbyVenues(double longitude,
				double latitude, int limit) throws RemoteException {
			List<Venue> venues = null;
			try {
				venues = mFoursquareManager.getNearbyVenues(longitude, latitude, limit);
			} catch (Exception e) {				
				e.printStackTrace();
				throw new RemoteException();
			}
			if (venues==null) venues = new ArrayList<Venue>();
			return venues;
		}
		
		@Override
		public List<Venue> getFoursquareCheckinHistory(int limit) throws RemoteException {						
			return mFoursquareManager.getFoursquareCheckinHistory(limit);
		}

		@Override
		public void setFoursquareUserCredentials(String userName,
				String userPassword) throws RemoteException {
			mFoursquareManager.setUserName(userName);
			mFoursquareManager.setUserPassword(userPassword);
			try {
				mFoursquareManager.loginOAuth();
			} catch (AuthRemoteException e) {				
				e.printStackTrace();							
				throw new AuthRemoteException();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException();
			}
		}

		@Override
		public void testLoginOAuth2() throws RemoteException {
			try {
				//mTwitterManager.loginOAuth2();
				mQypeManager.loginOAuth2();
			} catch (OAuthMessageSignerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OAuthNotAuthorizedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OAuthExpectationFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OAuthCommunicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		@Override
		public List<Place> getQypeNearbyPlaces(double longitude,
				double latitude, int radius, int order, String qypeCategoryId)
				throws RemoteException {
			List<Place> places = null;
			try {
				places = mQypeManager.getNearbyPlaces(longitude, latitude, radius, order, null, qypeCategoryId);
			} catch (Exception e) {				
				e.printStackTrace();
				throw new RemoteException();
			}
			if (places==null) places = new ArrayList<Place>();
			return places;			
		}

		@Override
		public List<Place> getQypeNearbyPlacesWithSearch(double longitude,
				double latitude, int radius, int order, String searchTerm)
				throws RemoteException {
			List<Place> places = null;
			try {
				places = mQypeManager.getNearbyPlaces(longitude, latitude, radius, order, searchTerm, null);
			} catch (Exception e) {				
				e.printStackTrace();
				throw new RemoteException();
			}
			if (places==null) places = new ArrayList<Place>();
			return places;			
		}

		@Override
		public List<Place> getQypeAllPlacesInBoundingBox(double longitudeSW,
				double latitudeSW, double longitudeNE, double latitudeNE,
				int order, String qypeCategoryId) throws RemoteException {
			List<Place> places = null;
			try {
				places = mQypeManager.getAllPlacesInBoundingBox(longitudeSW, latitudeSW, longitudeNE, latitudeNE, order, null, qypeCategoryId);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException();
			}
			if (places==null) places = new ArrayList<Place>();
			return places;			
		}

		@Override
		public List<Place> getQypeAllPlacesInBoundingBoxWithSearch(
				double longitudeSW, double latitudeSW, double longitudeNE,
				double latitudeNE, int order, String searchTerm)
				throws RemoteException {
			List<Place> places = null;
			try {
				places = mQypeManager.getAllPlacesInBoundingBox(longitudeSW, latitudeSW, longitudeNE, latitudeNE, order, searchTerm, null);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException();
			}
			if (places==null) places = new ArrayList<Place>();
			return places;
		}
				
	};

	
}
