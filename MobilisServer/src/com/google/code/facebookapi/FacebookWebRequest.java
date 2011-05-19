package com.google.code.facebookapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

public class FacebookWebRequest<T> {

	protected static Log log = LogFactory.getLog( FacebookWebRequest.class );

	private HttpServletRequest request;
	private String apiKey;
	private String secret;
	private IFacebookRestClient<T> apiClient;
	private boolean valid;
	private Map<String,String> fbParams;

	private String sessionKey;
	private Long userId;
	private Long sessionExpires;

	@Deprecated
	private boolean appAdded;
	private boolean appUser;
	private boolean inCanvas;
	private boolean inIframe;
	private boolean inNewFacebook;
	private boolean inProfileTab;


	public static FacebookWebRequest<Document> newInstanceXml( HttpServletRequest request, String apiKey, String secret ) {
		return new FacebookWebRequest<Document>( request, apiKey, secret, new FacebookXmlRestClient( apiKey, secret ) );
	}

	public static FacebookWebRequest<Object> newInstanceJson( HttpServletRequest request, String apiKey, String secret ) {
		return new FacebookWebRequest<Object>( request, apiKey, secret, new FacebookJsonRestClient( apiKey, secret ) );
	}

	public static FacebookWebRequest<Object> newInstanceJaxb( HttpServletRequest request, String apiKey, String secret ) {
		return new FacebookWebRequest<Object>( request, apiKey, secret, new FacebookJaxbRestClient( apiKey, secret ) );
	}

	protected FacebookWebRequest( HttpServletRequest request, String apiKey, String secret, IFacebookRestClient<T> apiClient ) {
		this.request = request;
		this.apiKey = apiKey;
		this.secret = secret;
		this.apiClient = apiClient;
		this.fbParams = FacebookSignatureUtil.pulloutFbSigParams( request.getParameterMap() );
		this.valid = FacebookSignatureUtil.verifySignature( fbParams, secret );
		if ( valid ) {
			inNewFacebook = getFbParamBoolean( FacebookParam.IN_NEW_FACEBOOK );
			inProfileTab = getFbParamBoolean( FacebookParam.IN_PROFILE_TAB );
			{
				// caching of session key / logged in user
				// XXX: introduce concept of viewer/owner???
				if ( !inProfileTab ) {
					sessionKey = getFbParam( FacebookParam.SESSION_KEY );
					userId = getFbParamLong( FacebookParam.USER );
					Long canvas_user = getFbParamLong( FacebookParam.CANVAS_USER );
					if ( canvas_user != null ) {
						userId = canvas_user;
					}
				} else {
					sessionKey = getFbParam( FacebookParam.PROFILE_SESSION_KEY );
					userId = getFbParamLong( FacebookParam.PROFILE_USER );
				}
				sessionExpires = getFbParamLong( FacebookParam.EXPIRES );
				if ( sessionKey != null || userId != null ) {
					apiClient.setCacheSession( sessionKey, userId, sessionExpires );
				}
			}
			{
				// caching of friends
				String friends = getFbParam( FacebookParam.FRIENDS );
				if ( friends != null && !friends.equals( "" ) ) {
					List<Long> friendsList = new ArrayList<Long>();
					for ( String friend : friends.split( "," ) ) {
						friendsList.add( Long.parseLong( friend ) );
					}
					apiClient.setCacheFriendsList( friendsList );
				}
			}
			{
				// caching of the "added" value
				appAdded = getFbParamBoolean( FacebookParam.ADDED );
				apiClient.setCacheAppAdded( appAdded );
			}
			{
				// caching of the "added" value
				appUser = getFbParamBoolean( FacebookParam.ADDED );
				apiClient.setCacheAppUser( appUser );
			}
			{
				// other values from the request;
				inCanvas = getFbParamBoolean( FacebookParam.IN_CANVAS );
				inIframe = getFbParamBoolean( FacebookParam.IN_IFRAME ) || !inCanvas;
			}
		}
	}

	// ---- Parameter Helpers

	public String getFbParam( FacebookParam key ) {
		return fbParams.get( key.toString() );
	}

	public Long getFbParamLong( FacebookParam key ) {
		String t = getFbParam( key );
		if ( t != null ) {
			return Long.parseLong( t );
		}
		return null;
	}

	public boolean getFbParamBoolean( FacebookParam key ) {
		Long t = getFbParamLong( key );
		return t != null && t > 0;
	}

	public boolean fbParamEquals( FacebookParam key, String val ) {
		String param = getFbParam( key );
		return key.equals( param );
	}

	// ---- Getters

	public boolean isLoggedIn() {
		return sessionKey != null && userId != null;
	}

	public IFacebookRestClient<T> getApiClient() {
		return apiClient;
	}

	public String getApiKey() {
		return apiKey;
	}

	@Deprecated
	public boolean isAppAdded() {
		return appAdded;
	}

	public boolean isAppUser() {
		return appUser;
	}

	public Map<String,String> getFbParams() {
		return fbParams;
	}

	public boolean isInCanvas() {
		return inCanvas;
	}

	public boolean isInIframe() {
		return inIframe;
	}

	public Long getSessionExpires() {
		return sessionExpires;
	}

	public String getSessionKey() {
		return sessionKey;
	}

	public Long getUserId() {
		return userId;
	}

	public boolean isValid() {
		return valid;
	}

	public boolean isInNewFacebook() {
		return inNewFacebook;
	}

	public boolean isInProfileTab() {
		return inProfileTab;
	}

}
