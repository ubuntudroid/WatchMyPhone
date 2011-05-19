package com.google.code.facebookapi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

/**
 * Utility class to handle authorization and authentication of requests. Objects of this class are meant to be created for every request. They are stateless and are not
 * supposed to be kept in the session.
 */
public class FacebookWebappHelper<T> {

	protected static Log log = LogFactory.getLog( FacebookWebappHelper.class );

	private HttpServletRequest request;

	private HttpServletResponse response;

	protected IFacebookRestClient<T> apiClient;

	protected String apiKey;

	protected String secret;

	protected Map<String,String> fbParams;

	protected Long user;

	private static String FACEBOOK_URL_PATTERN = "^https?://([^/]*\\.)?facebook\\.com(:\\d+)?/.*";

	/** @deprecated please use different constructor, or factory methods */
	@Deprecated
	public FacebookWebappHelper( HttpServletRequest request, HttpServletResponse response, String apiKey, String secret ) {
		this( request, response, apiKey, secret, (IFacebookRestClient<T>) new FacebookXmlRestClient( apiKey, secret ) );
	}

	public static FacebookWebappHelper<Document> newInstanceXml( HttpServletRequest request, HttpServletResponse response, String apiKey, String secret ) {
		return new FacebookWebappHelper<Document>( request, response, apiKey, secret, new FacebookXmlRestClient( apiKey, secret ) );
	}

	public static FacebookWebappHelper<Object> newInstanceJson( HttpServletRequest request, HttpServletResponse response, String apiKey, String secret ) {
		return new FacebookWebappHelper<Object>( request, response, apiKey, secret, new FacebookJsonRestClient( apiKey, secret ) );
	}

	public static FacebookWebappHelper<Object> newInstanceJaxb( HttpServletRequest request, HttpServletResponse response, String apiKey, String secret ) {
		return new FacebookWebappHelper<Object>( request, response, apiKey, secret, new FacebookJaxbRestClient( apiKey, secret ) );
	}

	public FacebookWebappHelper( HttpServletRequest request, HttpServletResponse response, String apiKey, String secret, IFacebookRestClient<T> apiClient ) {
		this.request = request;
		this.response = response;
		this.apiKey = apiKey;
		this.secret = secret;
		this.apiClient = apiClient;
		validateFbParams();
		{
			// caching of session key / logged in user
			String userS = fbParams.get( FacebookParam.USER.getSignatureName() );
			String canvasUserS = fbParams.get( FacebookParam.CANVAS_USER.getSignatureName() );
			String sessionKey = fbParams.get( FacebookParam.SESSION_KEY.getSignatureName() );
			String expiresS = fbParams.get( FacebookParam.EXPIRES.getSignatureName() );
			if ( userS != null && sessionKey != null && expiresS != null ) {
				apiClient.setCacheSession( sessionKey, Long.parseLong( userS ), Long.parseLong( expiresS ) );
			} else if ( canvasUserS != null ) {
				apiClient.setCacheSession( null, Long.parseLong( canvasUserS ), null );
			}
		}
		{
			// caching of friends
			String friends = fbParams.get( "friends" );
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
			String added = fbParams.get( "added" );
			if ( added != null ) {
				apiClient.setCacheAppAdded( added.equals( "1" ) );
			}
		}
	}

	/**
	 * Returns the internal FacebookRestClient object.
	 */
	public IFacebookRestClient<T> getFacebookRestClient() {
		return apiClient;
	}

	/**
	 * Synonym for {@link #getFacebookRestClient()}
	 */
	public IFacebookRestClient<T> get_api_client() {
		return apiClient;
	}

	/**
	 * Returns the secret key used to initialize this object.
	 */
	public String getSecret() {
		return secret;
	}

	/**
	 * Returns the api key used to initialize this object.
	 */
	public String getApiKey() {
		return apiKey;
	}

	private void validateFbParams() {
		// first we analyze the request parameters
		fbParams = getValidFbParams( _getRequestParams(), 48 * 3600, FacebookParam.SIGNATURE.toString() );
		if ( fbParams != null && !fbParams.isEmpty() ) {
			// this comment block is copied from the official php client,
			// it explains a lot :)
			//
			// If we got any fb_params passed in at all, then either:
			// - they included an fb_user / fb_session_key, which we should assume to be correct
			// - they didn't include an fb_user / fb_session_key, which means the
			// user doesn't have a valid session and if we want to get one we'll
			// need to use require_login(). (Calling set_user with null values
			// for user/session_key will work properly.)
			// - Note that we should *not* use our cookies in this scenario, since they may be referring to the wrong user.
			//

			// parsing the user, session, and expiry info
			String tmpSt = fbParams.get( FacebookParam.USER.getSignatureName() );
			Long user_id = tmpSt != null ? Long.valueOf( tmpSt ) : null;
			String session_key = fbParams.get( FacebookParam.SESSION_KEY.getSignatureName() );
			tmpSt = fbParams.get( FacebookParam.EXPIRES.getSignatureName() );
			Long expires = tmpSt != null ? Long.valueOf( tmpSt ) : null;
			setUser( user_id, session_key, expires );
		} else {
			// fallback to checking cookies
			Map<String,String> cookieParams = _getCookiesParams();
			fbParams = getValidFbParams( cookieParams, null, this.apiKey );
			if ( fbParams != null && !fbParams.isEmpty() ) {
				// parsing the user and session
				String tmpSt = fbParams.get( FacebookParam.USER.getSignatureName() );
				Long user_id = tmpSt != null ? Long.valueOf( tmpSt ) : null;
				String session_key = fbParams.get( FacebookParam.SESSION_KEY.getSignatureName() );
				setUser( user_id, session_key, null );
			}
			// finally we check the auth_token for a round-trip from the
			// facebook login page
			else if ( request.getParameter( "auth_token" ) != null ) {
				try {
					doGetSession( request.getParameter( "auth_token" ) );
					setUser( apiClient.getCacheUserId(), apiClient.getCacheSessionKey(), apiClient.getCacheSessionExpires() );
				}
				catch ( Exception ex ) {
					// if auth_token is stale (browser url doesn't change,
					// server is restarted), then auth_getSession throws
					// an exception. This happens a lot during development. To
					// recover, we do nothing. Then when requireLogin or
					// requireAdd
					// kick in, a new auth_token is created by redirecting the
					// user.
					// e.printStackTrace(System.err);
					log.warn( "possible issue (might be ignorable): " + ex.getMessage(), ex );
				}
			}
		}
	}

	public String doGetSession( String authToken ) {
		try {
			return apiClient.auth_getSession( authToken );
		}
		catch ( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	/**
	 * Sets the user. This method also saves the user and session information in the HttpSession
	 * 
	 * @param user_id
	 * @param session_key
	 * @param expires
	 */
	private void setUser( Long user_id, String session_key, Long expires ) {
		// place the data in the session for future requests that may not have the facebook parameters
		if ( !inFbCanvas() ) {
			Map<String,String> cookiesInfo = _getCookiesParams();
			String cookieUser = cookiesInfo.get( this.apiKey + "_user" );
			if ( cookieUser == null || !cookieUser.equals( user_id + "" ) ) {
				// map of parameters, but without the api_key prefix
				Map<String,String> cookies = new TreeMap<String,String>();
				cookies.put( "user", user_id + "" );
				cookies.put( "session_key", session_key );
				String sig = generateSig( cookies, this.secret );
				int age = 0;
				if ( expires != null ) {
					age = (int) ( expires.longValue() - ( System.currentTimeMillis() / 1000 ) );
				}
				for ( Map.Entry<String,String> entry : cookies.entrySet() ) {
					addCookie( this.apiKey + "_" + entry.getKey(), entry.getValue(), age );
				}
				addCookie( this.apiKey, sig, age );
			}
		}
		this.user = user_id;
	}

	private void addCookie( String key, String value, int age ) {
		Cookie cookie = new Cookie( key, value );
		if ( age > 0 ) {
			cookie.setMaxAge( age );
		}
		cookie.setPath( request.getContextPath() );
		response.addCookie( cookie );
	}

	private Map<String,String> getValidFbParams( Map<String,String> params, Integer timeout, String namespace ) {
		if ( namespace == null ) {
			namespace = "fb_sig";
		}
		String prefix = namespace + "_";
		int prefix_len = prefix.length();
		Map<String,String> fb_params = new HashMap<String,String>();
		for ( Entry<String,String> requestParam : params.entrySet() ) {
			if ( requestParam.getKey().indexOf( prefix ) == 0 ) {
				fb_params.put( requestParam.getKey().substring( prefix_len ), requestParam.getValue() );
			}
		}
		if ( timeout != null ) {
			if ( !fb_params.containsKey( FacebookParam.TIME.getSignatureName() ) ) {
				return Collections.emptyMap();
			}
			String tmpTime = fb_params.get( FacebookParam.TIME.getSignatureName() );
			if ( tmpTime.indexOf( '.' ) > 0 )
				tmpTime = tmpTime.substring( 0, tmpTime.indexOf( '.' ) );
			long time = Long.parseLong( tmpTime );
			if ( System.currentTimeMillis() / 1000 - time > timeout ) {
				return Collections.emptyMap();
			}
		}
		if ( !params.containsKey( namespace ) || !verifySignature( fb_params, params.get( namespace ) ) ) {
			return Collections.emptyMap();
		}
		return fb_params;
	}

	public void redirect( String url ) {
		try {
			// fbml redirect
			if ( inFbCanvas() ) {
				String out = "<fb:redirect url=\"" + url + "\"/>";
				response.getWriter().print( out );
				response.flushBuffer();
			}
			// javascript "frame-bypassing" redirect
			else if ( url.matches( FACEBOOK_URL_PATTERN ) ) {
				String out = "<script type=\"text/javascript\">\ntop.location.href = \"" + url + "\";\n</script>";
				response.getWriter().print( out );
				response.flushBuffer();
			} else {
				// last fallback
				response.sendRedirect( url );
			}
		}
		catch ( IOException e ) {
			throw new RuntimeException( e );
		}
	}

	/**
	 * Returns true if the application is in a frame or a canvas.
	 */
	public boolean inFrame() {
		return fbParams.containsKey( FacebookParam.IN_CANVAS.getSignatureName() ) || fbParams.containsKey( FacebookParam.IN_IFRAME.getSignatureName() );
	}

	/**
	 * Returns true if the application is in a canvas.
	 */
	public boolean inFbCanvas() {
		return fbParams.containsKey( FacebookParam.IN_CANVAS.getSignatureName() );
	}

	@Deprecated
	public boolean isAdded() {
		return "1".equals( fbParams.get( FacebookParam.ADDED.getSignatureName() ) );
	}
	
	/**
	 * Returns true if UNINSTALL param exists and is 1.
	 */
	public boolean isUninstall() {
		return "1".equals( fbParams.get( FacebookParam.UNINSTALL.getSignatureName() ) );
	}


	public boolean isLogin() {
		return getUser() != null;
	}

	/**
	 * Synonym for {@link #getUser()}
	 */
	public Long get_loggedin_user() {
		return getUser();
	}

	/**
	 * Returns the user id of the logged in user associated with this object
	 */
	public Long getUser() {
		return this.user;
	}

	/**
	 * Returns the url of the currently requested page
	 */
	private String currentUrl() {
		String url = request.getScheme() + "://" + request.getServerName();
		int port = request.getServerPort();
		if ( port != 80 ) {
			url += ":" + port;
		}
		url += request.getRequestURI();
		return url;
	}

	/**
	 * Forces the user to log in to this application. If the user hasn't logged in yet, this method issues a url redirect.
	 * 
	 * @param next
	 *            the value for the 'next' request paramater that is appended to facebook's login screen.
	 * @return true if the user hasn't logged in yet and a redirect was issued.
	 */
	public boolean requireLogin( String next ) {
		if ( isLogin() ) {
			return false;
		}
		redirect( getLoginUrl( next, inFrame() ) );
		return true;
	}

	/**
	 * Forces the user to add this application. If the user hasn't added it yet, this method issues a url redirect.
	 * 
	 * @param next
	 *            the value for the 'next' request paramater that is appended to facebook's add screen.
	 * @return true if the user hasn't added the application yet and a redirect was issued.
	 */
	@Deprecated
	public boolean requireAdd( String next ) {
		if ( getUser() != null && isAdded() ) {
			return false;
		}
		redirect( getAddUrl( next ) );
		return true;
	}

	/**
	 * Forces the application to be in a frame. If it is not in a frame, this method issues a url redirect.
	 * 
	 * @param next
	 *            the value for the 'next' request paramater that is appended to facebook's login screen.
	 * @return true if a redirect was issued, false otherwise.
	 */
	public boolean requireFrame( String next ) {
		if ( !inFrame() ) {
			redirect( getLoginUrl( next, true ) );
			return true;
		}
		return false;
	}

	/**
	 * Returns the url that facebook uses to prompt the user to login to this application.
	 * 
	 * @param next
	 *            indicates the page to which facebook should redirect the user has logged in.
	 */
	public String getLoginUrl( String next, boolean canvas ) {
		String url = getFacebookUrl( null ) + "/login.php?v=1.0&api_key=" + apiKey;
		try {
			url += next != null ? "&next=" + URLEncoder.encode( next, "UTF-8" ) : "";
		}
		catch ( UnsupportedEncodingException e ) {
			throw new RuntimeException( e );
		}
		url += canvas ? "&canvas=true" : "";
		return url;
	}

	/**
	 * Returns the url that facebook uses to prompt the user to add this application.
	 * 
	 * @param next
	 *            indicates the page to which facebook should redirect the user after the application is added.
	 */
	public String getAddUrl( String next ) {
		String url = getFacebookUrl( null ) + "/add.php?api_key=" + apiKey;
		try {
			url += next != null ? "&next=" + URLEncoder.encode( next, "UTF-8" ) : "";
		}
		catch ( UnsupportedEncodingException e ) {
			throw new RuntimeException( e );
		}
		return url;
	}

	/**
	 * Returns a url to a facebook sub-domain
	 * 
	 * @param subDomain
	 */
	public static String getFacebookUrl( String subDomain ) {
		if ( subDomain == null || subDomain.equals( "" ) ) {
			subDomain = "www";
		}
		return "http://" + subDomain + ".facebook.com";
	}

	public static String generateSig( Map<String,String> params, String secret ) {
		SortedSet<String> keys = new TreeSet<String>( params.keySet() );
		// make sure that the signature paramater is not included
		keys.remove( FacebookParam.SIGNATURE.toString() );
		String str = "";
		for ( String key : keys ) {
			str += key + "=" + params.get( key );
		}
		str += secret;
		try {
			MessageDigest md = MessageDigest.getInstance( "MD5" );
			md.update( str.getBytes( "UTF-8" ) );
			StringBuilder result = new StringBuilder();
			for ( byte b : md.digest() ) {
				result.append( Integer.toHexString( ( b & 0xf0 ) >>> 4 ) );
				result.append( Integer.toHexString( b & 0x0f ) );
			}
			return result.toString();
		}
		catch ( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	/**
	 * Verifies that the signature of the parameters is valid
	 * 
	 * @param params
	 *            a map of the parameters. Typically these are the request parameters that start with "fb_sig"
	 * @param expected_sig
	 *            the expected signature
	 */
	public boolean verifySignature( Map<String,String> params, String expected_sig ) {
		return generateSig( params, secret ).equals( expected_sig );
	}

	/**
	 * returns a String->String map of the request parameters. It doesn't matter if the request method is GET or POST.
	 */
	private Map<String,String> _getRequestParams() {
		Map<String,String> results = new HashMap<String,String>();
		Map<String,String[]> map = request.getParameterMap();
		for ( Entry<String,String[]> entry : map.entrySet() ) {
			results.put( entry.getKey(), entry.getValue()[0] );
		}
		return results;
	}

	private Map<String,String> _getCookiesParams() {
		Map<String,String> results = new HashMap<String,String>();
		Cookie[] cookies = request.getCookies();
		if ( cookies != null ) {
			for ( Cookie cookie : cookies ) {
				results.put( cookie.getName(), cookie.getValue() );
			}
		}
		return results;
	}

}
