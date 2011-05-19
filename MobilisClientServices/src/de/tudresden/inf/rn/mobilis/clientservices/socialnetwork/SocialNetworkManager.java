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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import android.util.AndroidException;
import android.util.Log;
import de.tudresden.inf.rn.mobilis.clientservices.AuthRemoteException;

/**
 * @author Robert Lübke
 */
public abstract class SocialNetworkManager {
	
	/** The TAG for the Log. */
	private final static String TAG = "SocialNetworkManager";
	
	//OAuth
	protected OAuthConsumer consumer=null;
	protected String userName, userPassword;
	/**
	 * @return the OAuth consumer key for the specific API.
	 */
	protected abstract String getConsumerKey();
	/**
	 * @return the OAuth consumer secret for the specific API.
	 */	
	protected abstract String getConsumerSecret();
	/**
	 * @return the OAuth Request Token URL for the specific API.
	 */	
	protected abstract String getRequestTokenURL();
	/**
	 * @return the OAuth Access Token URL for the specific API.
	 */	
	protected abstract String getAccessTokenURL();
	/**
	 * @return the OAuth Authorize URL for the specific API.
	 */	
	protected abstract String getAuthorizeURL();
	
	
		
	/**
	 * @return the OAuth authentification URL for the specific API.
	 */	
	protected abstract String getAuthentificationURL();
	
	
	public SocialNetworkManager() {		
	}
	
	public void setUserName(String userName) {
		this.userName=userName;
	}
	public void setUserPassword(String userPassword) {
		this.userPassword=userPassword;
	}
	
	protected InputStream makeRequestAndGetJSONData(String url) throws URISyntaxException, ClientProtocolException, IOException{
        DefaultHttpClient httpClient = new DefaultHttpClient();
        URI uri;
        InputStream data = null;        
        uri = new URI(url);
        HttpGet method = new HttpGet(uri);
        HttpResponse response = httpClient.execute(method);
        data = response.getEntity().getContent();               
        return data;
    }
	
	protected InputStream makeSignedRequestAndGetJSONData(String url){
		try {
			if (consumer==null) loginOAuth();
		} catch (Exception e) {
			// Error during authentification
			consumer=null;
			e.printStackTrace();
		}		
        DefaultHttpClient httpClient = new DefaultHttpClient();
        URI uri;
        InputStream data = null;
        try {
            uri = new URI(url);
            HttpGet method = new HttpGet(uri);
            
            // sign the request
            consumer.sign(method);
            
            HttpResponse response = httpClient.execute(method);
            data = response.getEntity().getContent();
        } catch (Exception e) {
            e.printStackTrace();
        }       
        return data;
    }	
	
	
	
	public void loginOAuth() throws OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException, ClientProtocolException, IOException, IllegalStateException, SAXException, ParserConfigurationException, FactoryConfigurationError, AndroidException {		
		// Build URL and create GET request
		String url = getAuthentificationURL();
		HttpGet reqLogin = new HttpGet(url);

		// Instantiate consumer with application key and secret
		consumer = new CommonsHttpOAuthConsumer(
				getConsumerKey(),
                getConsumerSecret());
		consumer.sign(reqLogin);

		// Login
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse resLogin = httpClient.execute(reqLogin);
		if (resLogin.getEntity() == null) {
			throw new AuthRemoteException();
		}

		// Parse response
		Document document =
			DocumentBuilderFactory
				.newInstance()
					.newDocumentBuilder()
						.parse(resLogin.getEntity().getContent());

		// Get access token
		Element eOAuthToken =
			(Element)document.getElementsByTagName("oauth_token").item(0);
		if (eOAuthToken == null) {
			throw new AuthRemoteException();
		}
		Node e = eOAuthToken.getFirstChild();
		String sOAuthToken =  e.getNodeValue();
		System.out.println("token: " +sOAuthToken);

		// Get access secret
		Element eOAuthTokenSecret =
			(Element)document.getElementsByTagName("oauth_token_secret").item(0);
		if (eOAuthTokenSecret == null) {
			throw new AuthRemoteException();
		}
		e = eOAuthTokenSecret.getFirstChild();
		String sOAuthTokenSecret =  e.getNodeValue();
		System.out.println("Secret: " +sOAuthTokenSecret);

		// Set access token and secret for further requests
		consumer.setTokenWithSecret(sOAuthToken, sOAuthTokenSecret);
	}
	
	
	public void loginOAuth2() throws OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException, IOException {
        OAuthConsumer consumer = new DefaultOAuthConsumer(getConsumerKey(),
        getConsumerSecret());

		OAuthProvider provider = new DefaultOAuthProvider(getRequestTokenURL(),
		        getAccessTokenURL(), getAuthorizeURL());
		
		Log.v(TAG,"Fetching request token ...");
		
		// we do not support callbacks, thus pass OOB
		String authUrl = provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND);
		
		Log.v(TAG,"Request token: " + consumer.getToken());
		Log.v(TAG,"Token secret: " + consumer.getTokenSecret());
		
		Log.v(TAG,"Now visit:\n" + authUrl + "\n... and grant this app authorization");
		Log.v(TAG,"Enter the PIN code and hit ENTER when you're done:");
		
//		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//		String pin = br.readLine();
		String pin="abc";
		
		System.out.println("Fetching access token ...");
		
		provider.retrieveAccessToken(consumer, pin);
		
		Log.v(TAG,"Access token: " + consumer.getToken());
		Log.v(TAG,"Token secret: " + consumer.getTokenSecret());
		
		URL url = new URL("http://twitter.com/statuses/mentions.xml");
		HttpURLConnection request = (HttpURLConnection) url.openConnection();
		
		consumer.sign(request);
		
		Log.v(TAG,"Sending request...");
		request.connect();
		
		Log.v(TAG,"Response: " + request.getResponseCode() + " "
		        + request.getResponseMessage());

	}
	
}
