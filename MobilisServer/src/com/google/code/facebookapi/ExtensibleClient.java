package com.google.code.facebookapi;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Base class for interacting with the Facebook Application Programming Interface (API). Most Facebook API methods map directly to function calls of this class. <br/>
 * Instances of FacebookRestClient should be initialized via calls to {@link #auth_createToken}, followed by {@link #auth_getSession}. <br/> For continually updated
 * documentation, please refer to the <a href="http://wiki.developers.facebook.com/index.php/API"> Developer Wiki</a>.
 */
public abstract class ExtensibleClient<T> implements IFacebookRestClient<T> {

	protected static Log log = LogFactory.getLog( ExtensibleClient.class );
	
	public static final int BATCH_LIMIT = 20;

	public static URL SERVER_URL = null;
	public static URL HTTPS_SERVER_URL = null;
	static {
		try {
			SERVER_URL = new URL( SERVER_ADDR );
			HTTPS_SERVER_URL = new URL( HTTPS_SERVER_ADDR );
		}
		catch ( MalformedURLException ex ) {
			log.error( "MalformedURLException: " + ex.getMessage(), ex );
		}
	}

	protected static JAXBContext JAXB_CONTEXT;

	public static void initJaxbSupport() {
		if ( JAXB_CONTEXT == null ) {
			try {
				JAXB_CONTEXT = JAXBContext.newInstance( "com.google.code.facebookapi.schema" );
			}
			catch ( JAXBException ex ) {
				log.error( "MalformedURLException: " + ex.getMessage(), ex );
			}
		}
	}

	protected URL _serverUrl;
	protected int _timeout;
	protected int _readTimeout;

	protected final String _apiKey;
	protected final String _secret;
	protected boolean _isDesktop;

	protected String cacheSessionKey;
	protected Long cacheUserId;
	protected Long cacheSessionExpires;
	/** filled in when session is established only used for desktop apps */
	protected String cacheSessionSecret;

	protected String rawResponse;
	protected boolean batchMode;
	protected List<BatchQuery> queries;

	protected String permissionsApiKey = null;


	protected static final String CRLF = "\r\n";
	protected static final String PREF = "--";
	protected static final int UPLOAD_BUFFER_SIZE = 1024;


	public static final String MARKETPLACE_STATUS_DEFAULT = "DEFAULT";
	public static final String MARKETPLACE_STATUS_NOT_SUCCESS = "NOT_SUCCESS";
	public static final String MARKETPLACE_STATUS_SUCCESS = "SUCCESS";

	protected ExtensibleClient( String apiKey, String secret ) {
		this( SERVER_URL, apiKey, secret, null );
	}

	protected ExtensibleClient( String apiKey, String secret, int timeout ) {
		this( SERVER_URL, apiKey, secret, null, timeout );
	}

	protected ExtensibleClient( String apiKey, String secret, String sessionKey ) {
		this( SERVER_URL, apiKey, secret, sessionKey );
	}

	protected ExtensibleClient( String apiKey, String secret, String sessionKey, int connectionTimeout ) {
		this( SERVER_URL, apiKey, secret, sessionKey, connectionTimeout );
	}

	protected ExtensibleClient( String serverAddr, String apiKey, String secret, String sessionKey ) throws MalformedURLException {
		this( new URL( serverAddr ), apiKey, secret, sessionKey );
	}

	protected ExtensibleClient( String serverAddr, String apiKey, String secret, String sessionKey, int connectionTimeout ) throws MalformedURLException {
		this( new URL( serverAddr ), apiKey, secret, sessionKey, connectionTimeout );
	}

	protected ExtensibleClient( URL serverUrl, String apiKey, String secret, String sessionKey, int timeout ) {
		this( serverUrl, apiKey, secret, sessionKey );
		_timeout = timeout;
	}

	protected ExtensibleClient( URL serverUrl, String apiKey, String secret, String sessionKey, int timeout, int readTimeout ) {
		this( serverUrl, apiKey, secret, sessionKey );
		_timeout = timeout;
		_readTimeout = readTimeout;
	}

	protected ExtensibleClient( URL serverUrl, String apiKey, String secret, String sessionKey ) {
		this.cacheSessionKey = sessionKey;
		this._apiKey = apiKey;
		this._secret = secret;
		this._serverUrl = ( null != serverUrl ) ? serverUrl : SERVER_URL;
		this._timeout = -1;
		this._readTimeout = -1;
		this.batchMode = false;
		this.queries = new ArrayList<BatchQuery>();
		if ( this instanceof FacebookJaxbRestClient ) {
			initJaxbSupport();
		}
	}

	public String getApiKey() {
		return _apiKey;
	}
	
	public String getSecret() {
		return _secret;
	}

	public void beginPermissionsMode( String apiKey ) {
		this.permissionsApiKey = apiKey;
	}

	public void endPermissionsMode() {
		this.permissionsApiKey = null;
	}

	public JAXBContext getJaxbContext() {
		return JAXB_CONTEXT;
	}

	public void setJaxbContext( JAXBContext context ) {
		JAXB_CONTEXT = context;
	}

	/**
	 * The response format in which results to FacebookMethod calls are returned
	 * 
	 * @return the format: either XML, JSON, or null (API default)
	 */
	public abstract String getResponseFormat();

	/**
	 * Gets the session-token used by Facebook to authenticate a desktop application. If your application does not run in desktop mode, than this field is not relevent to
	 * you.
	 * 
	 * @return the desktop-app session token.
	 */
	public String getSessionSecret() {
		return cacheSessionSecret;
	}

	/**
	 * Allows the session-token to be manually overridden when running a desktop application. If your application does not run in desktop mode, then setting this field
	 * will have no effect. If you set an incorrect value here, your application will probably fail to run.
	 * 
	 * @param key
	 *            the new value to set. Incorrect values may cause your application to fail to run.
	 */
	public void setSessionSecret( String key ) {
		cacheSessionSecret = key;
	}

	private static CharSequence delimit( Iterable iterable ) {
		if ( iterable == null ) {
			return null;
		}
		StringBuilder buffer = new StringBuilder();
		boolean empty = true;
		boolean notFirst = false;
		for ( Object item : iterable ) {
			if ( notFirst ) {
				buffer.append( "," );
			} else {
				empty = false;
				notFirst = true;
			}
			buffer.append( item.toString() );
		}
		if ( empty ) {
			return null;
		}
		return buffer;
	}

	@Deprecated
	protected Boolean cacheAppAdded; // to save making the users.isAppAdded api call, this will get prepopulated on canvas pages

	@Deprecated
	public Boolean getCacheAppAdded() {
		return cacheAppAdded;
	}

	@Deprecated
	public void setCacheAppAdded( Boolean cacheAppAdded ) {
		this.cacheAppAdded = cacheAppAdded;
	}

	protected Boolean cacheAppUser; // to save making the users.isAppAdded api call, this will get prepopulated on canvas pages

	public Boolean getCacheAppUser() {
		return cacheAppUser;
	}

	public void setCacheAppUser( Boolean cacheAppUser ) {
		this.cacheAppUser = cacheAppUser;
	}

	public String getCacheSessionSecret() {
		return cacheSessionSecret;
	}

	public void setCacheSessionSecret( String cacheSessionSecret ) {
		this.cacheSessionSecret = cacheSessionSecret;
	}

	public void setCacheSession( String cacheSessionKey, Long cacheUserId, Long cacheSessionExpires ) {
		setCacheSessionKey( cacheSessionKey );
		setCacheUserId( cacheUserId );
		setCacheSessionExpires( cacheSessionExpires );
	}

	public Long getCacheSessionExpires() {
		return cacheSessionExpires;
	}

	public void setCacheSessionExpires( Long cacheSessionExpires ) {
		this.cacheSessionExpires = cacheSessionExpires;
	}

	public String getCacheSessionKey() {
		return cacheSessionKey;
	}

	public void setCacheSessionKey( String cacheSessionKey ) {
		this.cacheSessionKey = cacheSessionKey;
	}

	public Long getCacheUserId() {
		return cacheUserId;
	}

	public void setCacheUserId( Long cacheUserId ) {
		this.cacheUserId = cacheUserId;
	}

	public T friends_areFriends( long userId1, long userId2 ) throws FacebookException {
		return callMethod( FacebookMethod.FRIENDS_ARE_FRIENDS, newPair( "uids1", userId1 ), newPair( "uids2", userId2 ) );
	}

	public T friends_areFriends( Collection<Long> userIds1, Collection<Long> userIds2 ) throws FacebookException {
		if ( userIds1 == null || userIds2 == null || userIds1.isEmpty() || userIds2.isEmpty() ) {
			throw new IllegalArgumentException( "Collections passed to friends_areFriends should not be null or empty" );
		}
		if ( userIds1.size() != userIds2.size() ) {
			throw new IllegalArgumentException( String.format( "Collections should be same size: got userIds1: %d elts; userIds2: %d elts", userIds1.size(), userIds2
					.size() ) );
		}
		return callMethod( FacebookMethod.FRIENDS_ARE_FRIENDS, newPair( "uids1", delimit( userIds1 ) ), newPair( "uids2", delimit( userIds2 ) ) );
	}

	public boolean fbml_refreshRefUrl( String url ) throws FacebookException {
		try {
			return fbml_refreshRefUrl( new URL( url ) );
		}
		catch ( MalformedURLException ex ) {
			throw runtimeException( ex );
		}
	}

	/**
	 * Adds image parameters
	 * 
	 * @param params
	 * @param images
	 */
	protected void handleFeedImages( List<Pair<String,CharSequence>> params, Collection<? extends IPair<? extends Object,URL>> images ) {
		if ( images != null && images.size() > 4 ) {
			throw new IllegalArgumentException( "At most four images are allowed, got " + images.size() );
		}
		if ( null != images && !images.isEmpty() ) {
			int image_count = 0;
			for ( IPair<? extends Object,URL> image : images ) {
				++image_count;
				assert null != image.getFirst() : "Image URL must be provided";
				String name = "image_" + image_count;
				params.add( newPair( name, image.getFirst() ) );
				if ( null != image.getSecond() ) {
					params.add( newPair( name + "_link", image.getSecond() ) );
				}
			}
		}
	}

	/**
	 * Call this function to retrieve the session information after your user has logged in.
	 * 
	 * @param authToken
	 *            the token returned by auth_createToken or passed back to your callback_url.
	 */
	public abstract String auth_getSession( String authToken ) throws FacebookException;

	@Deprecated
	public boolean feed_publishTemplatizedAction( Long actorId, CharSequence titleTemplate ) throws FacebookException {
		return feed_publishTemplatizedAction( actorId, titleTemplate, null, null, null, null, null, null );
	}

	/**
	 * Publishes a Mini-Feed story describing an action taken by a user, and publishes aggregating News Feed stories to the friends of that user. Stories are identified
	 * as being combinable if they have matching templates and substituted values.
	 * 
	 * @param actorId
	 *            the user into whose mini-feed the story is being published.
	 * @param titleTemplate
	 *            markup (up to 60 chars, tags excluded) for the feed story's title section. Must include the token <code>{actor}</code>.
	 * @param titleData
	 *            (optional) contains token-substitution mappings for tokens that appear in titleTemplate. Should not contain mappings for the <code>{actor}</code> or
	 *            <code>{target}</code> tokens. Required if tokens other than <code>{actor}</code> or <code>{target}</code> appear in the titleTemplate.
	 * @param bodyTemplate
	 *            (optional) markup to be displayed in the feed story's body section. can include tokens, of the form <code>{token}</code>, to be substituted using
	 *            bodyData.
	 * @param bodyData
	 *            (optional) contains token-substitution mappings for tokens that appear in bodyTemplate. Required if the bodyTemplate contains tokens other than
	 *            <code>{actor}</code> and <code>{target}</code>.
	 * @param bodyGeneral
	 *            (optional) additional body markup that is not aggregated. If multiple instances of this templated story are combined together, the markup in the
	 *            bodyGeneral of one of their stories may be displayed.
	 * @param targetIds
	 *            The user ids of friends of the actor, used for stories about a direct action between the actor and these targets of his/her action. Required if either
	 *            the titleTemplate or bodyTemplate includes the token <code>{target}</code>.
	 * @param images
	 *            (optional) additional body markup that is not aggregated. If multiple instances of this templated story are combined together, the markup in the
	 *            bodyGeneral of one of their stories may be displayed.
	 * @return whether the action story was successfully published; false in case of a permission error
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Feed.publishTemplatizedAction"> Developers Wiki: Feed.publishTemplatizedAction</a>
	 */
	public boolean feed_publishTemplatizedAction( Long actorId, CharSequence titleTemplate, Map<String,CharSequence> titleData, CharSequence bodyTemplate,
			Map<String,CharSequence> bodyData, CharSequence bodyGeneral, Collection<Long> targetIds, Collection<? extends IPair<? extends Object,URL>> images )
			throws FacebookException {
		return feed_publishTemplatizedAction( titleTemplate, titleData, bodyTemplate, bodyData, bodyGeneral, targetIds, images, null );
	}

	/**
	 * @deprecated Use the version that takes a Long for the actorId paramter.
	 */
	@Deprecated
	public boolean feed_publishTemplatizedAction( Integer actorId, CharSequence titleTemplate, Map<String,CharSequence> titleData, CharSequence bodyTemplate,
			Map<String,CharSequence> bodyData, CharSequence bodyGeneral, Collection<Long> targetIds, Collection<? extends IPair<? extends Object,URL>> images )
			throws FacebookException {
		return feed_publishTemplatizedAction( (long) ( actorId.intValue() ), titleTemplate, titleData, bodyTemplate, bodyData, bodyGeneral, targetIds, images );
	}

	public T groups_getMembers( Number groupId ) throws FacebookException {
		assert ( null != groupId );
		return callMethod( FacebookMethod.GROUPS_GET_MEMBERS, newPair( "gid", groupId ) );
	}

	private static String encode( CharSequence target ) {
		if ( target == null ) {
			return "";
		}
		String result = target.toString();
		try {
			result = URLEncoder.encode( result, "UTF8" );
		}
		catch ( UnsupportedEncodingException ex ) {
			throw runtimeException( ex );
		}
		return result;
	}

	public T friends_getAppUsers() throws FacebookException {
		return callMethod( FacebookMethod.FRIENDS_GET_APP_USERS );
	}

	public T fql_query( CharSequence query ) throws FacebookException {
		assert ( null != query );
		return callMethod( FacebookMethod.FQL_QUERY, newPair( "query", query ) );
	}

	private String generateSignature( List<String> params, boolean requiresSession ) {
		String secret = ( isDesktop() && requiresSession ) ? cacheSessionSecret : _secret;
		//String secret = _secret;
		return FacebookSignatureUtil.generateSignature( params, secret );
	}

	public T groups_get( Long userId, Collection<Long> groupIds ) throws FacebookException {
		boolean hasGroups = ( null != groupIds && !groupIds.isEmpty() );
		if ( null != userId ) {
			return hasGroups ? callMethod( FacebookMethod.GROUPS_GET, newPair( "uid", userId ), newPair( "gids", delimit( groupIds ) ) ) : callMethod(
					FacebookMethod.GROUPS_GET, newPair( "uid", userId ) );
		} else {
			return hasGroups ? callMethod( FacebookMethod.GROUPS_GET, newPair( "gids", delimit( groupIds ) ) ) : callMethod( FacebookMethod.GROUPS_GET );
		}
	}

	/**
	 * Call the specified method, with the given parameters, and return a DOM tree with the results.
	 * 
	 * @param method
	 *            the fieldName of the method
	 * @param paramPairs
	 *            a list of arguments to the method
	 * @throws Exception
	 *             with a description of any errors given to us by the server.
	 */
	protected T callMethod( IFacebookMethod method, Pair<String,CharSequence>... paramPairs ) throws FacebookException {
		return callMethod( method, Arrays.asList( paramPairs ), null, null );
	}

	/**
	 * Call the specified method, with the given parameters, and return a DOM tree with the results.
	 * 
	 * @param method
	 *            the fieldName of the method
	 * @param paramPairs
	 *            a list of arguments to the method
	 * @throws Exception
	 *             with a description of any errors given to us by the server.
	 */
	protected T callMethod( IFacebookMethod method, Collection<Pair<String,CharSequence>> paramPairs ) throws FacebookException {
		return callMethod( method, paramPairs, null, null );
	}

	protected T callMethod( IFacebookMethod method, Collection<Pair<String,CharSequence>> paramPairs, String fileName, InputStream fileStream ) throws FacebookException {
		rawResponse = null;
		Map<String,String> params = new TreeMap<String,String>();
		if ( permissionsApiKey != null ) {
			params.put( "call_as_apikey", permissionsApiKey );
		}
		params.put( "method", method.methodName() );
		params.put( "api_key", _apiKey );
		params.put( "v", TARGET_API_VERSION );

		String format = getResponseFormat();
		if ( null != format ) {
			params.put( "format", format );
		}

		params.put( "call_id", Long.toString( System.currentTimeMillis() ) );
		boolean includeSession = !method.requiresNoSession() && cacheSessionKey != null;
		if ( includeSession ) {
			params.put( "session_key", cacheSessionKey );
		}

		CharSequence oldVal;
		for ( Pair<String,CharSequence> p : paramPairs ) {
			oldVal = params.put( p.first, FacebookSignatureUtil.toString( p.second ) );
			if ( oldVal != null ) {
				log.warn( String.format( "For parameter %s, overwrote old value %s with new value %s.", p.first, oldVal, p.second ) );
			}
		}

		assert ( !params.containsKey( "sig" ) );
		String signature = generateSignature( FacebookSignatureUtil.convert( params.entrySet() ), includeSession );
		params.put( "sig", signature );

		if ( batchMode ) {
			// if we are running in batch mode, don't actually execute the query now, just add it to the list
			boolean addToBatch = true;
			if ( method.methodName().equals( FacebookMethod.USERS_GET_LOGGED_IN_USER.methodName() ) ) {
				Exception trace = new Exception();
				StackTraceElement[] traceElems = trace.getStackTrace();
				int index = 0;
				for ( StackTraceElement elem : traceElems ) {
					if ( elem.getMethodName().indexOf( "_" ) != -1 ) {
						StackTraceElement caller = traceElems[index + 1];
						if ( ( caller.getClassName().equals( ExtensibleClient.class.getName() ) ) && ( !caller.getMethodName().startsWith( "auth_" ) ) ) {
							addToBatch = false;
						}
						break;
					}
					index++ ;
				}
			}
			if ( addToBatch ) {
				queries.add( new BatchQuery( method, params ) );
			}
			return null;
		}

		boolean doHttps = isDesktop() && FacebookMethod.AUTH_GET_SESSION.equals( method );
		boolean doEncode = true;
		try {
			rawResponse = method.takesFile() ? postFileRequest( method, params, fileName, fileStream ) : postRequest( method, params, doHttps );
			return parseCallResult( new ByteArrayInputStream( rawResponse.getBytes( "UTF-8" ) ), method );
		}
		catch ( IOException ex ) {
			throw runtimeException( ex );
		}
	}

	private String postRequest( IFacebookMethod method, Map<String,String> params, boolean doHttps ) throws IOException {
		URL serverUrl = ( doHttps ) ? HTTPS_SERVER_URL : _serverUrl;
		CharSequence paramString = ( null == params ) ? "" : delimit( params.entrySet(), "&", "=", true );
		if ( log.isDebugEnabled() ) {
			log.debug( method.methodName() + " POST: " + serverUrl.toString() + "?" + paramString );
		}

		HttpURLConnection conn = null;
		OutputStream out = null;
		InputStream in = null;
		try {
			conn = (HttpURLConnection) serverUrl.openConnection();
			if ( _timeout != -1 ) {
				conn.setConnectTimeout( _timeout );
			}
			if ( _readTimeout != -1 ) {
				conn.setReadTimeout( _readTimeout );
			}
			conn.setRequestMethod( "POST" );
			conn.setDoOutput( true );
			conn.connect();
			out = conn.getOutputStream();
			out.write( paramString.toString().getBytes( "UTF-8" ) );
			in = conn.getInputStream();
			return getResponse( method, in );
		}
		finally {
			close( in );
			close( out );
			disconnect( conn );
		}
	}

	/**
	 * Helper function for posting a request that includes raw file data, eg {@link #photos_upload}.
	 * 
	 * @param methodName
	 *            the name of the method
	 * @param params
	 *            request parameters (not including the file)
	 * @param doEncode
	 *            whether to UTF8-encode the parameters
	 * @return an InputStream with the request response
	 * @see #photos_upload
	 */

	protected String postFileRequest( IFacebookMethod method, Map<String,String> params, String fileName, InputStream fileStream ) throws IOException {
		HttpURLConnection con = null;
		OutputStream urlOut = null;
		InputStream in = null;
		try {
			String boundary = Long.toString( System.currentTimeMillis(), 16 );
			con = (HttpURLConnection) _serverUrl.openConnection();
			if ( _timeout != -1 ) {
				con.setConnectTimeout( _timeout );
			}
			if ( _readTimeout != -1 ) {
				con.setReadTimeout( _readTimeout );
			}
			con.setDoInput( true );
			con.setDoOutput( true );
			con.setUseCaches( false );
			con.setRequestProperty( "Content-Type", "multipart/form-data; boundary=" + boundary );
			con.setRequestProperty( "MIME-version", "1.0" );

			urlOut = con.getOutputStream();
			DataOutputStream out = new DataOutputStream( urlOut );

			for ( Map.Entry<String,String> entry : params.entrySet() ) {
				out.writeBytes( PREF + boundary + CRLF );

				out.writeBytes( "Content-Type: text/plain;charset=utf-8" + CRLF );
				// out.writeBytes( "Content-Transfer-Encoding: application/x-www-form-urlencoded" + CRLF );

				// out.writeBytes( "Content-Type: text/plain;charset=utf-8" + CRLF );
				// out.writeBytes( "Content-Transfer-Encoding: quoted-printable" + CRLF );

				out.writeBytes( "Content-disposition: form-data; name=\"" + entry.getKey() + "\"" + CRLF );
				out.writeBytes( CRLF );
				byte[] valueBytes = entry.getValue().toString().getBytes( "UTF-8" );
				out.write( valueBytes );
				out.writeBytes( CRLF );
			}

			out.writeBytes( PREF + boundary + CRLF );
			out.writeBytes( "Content-Type: image" + CRLF );
			out.writeBytes( "Content-disposition: form-data; filename=\"" + fileName + "\"" + CRLF );
			// out.writeBytes("Content-Transfer-Encoding: binary" + CRLF); // not necessary

			// Write the file
			out.writeBytes( CRLF );
			byte buf[] = new byte[UPLOAD_BUFFER_SIZE];
			int len = 0;
			while ( len >= 0 ) {
				out.write( buf, 0, len );
				len = fileStream.read( buf );
			}

			out.writeBytes( CRLF + PREF + boundary + PREF + CRLF );
			out.flush();
			in = con.getInputStream();
			return getResponse( method, in );
		}
		finally {
			close( urlOut );
			close( in );
			disconnect( con );
		}
	}

	private String getResponse( IFacebookMethod method, InputStream data ) throws IOException {
		Reader in = new BufferedReader( new InputStreamReader( data, "UTF-8" ) );
		StringBuilder buffer = new StringBuilder();
		char[] buf = new char[1000];
		int l = 0;
		while ( l >= 0 ) {
			buffer.append( buf, 0, l );
			l = in.read( buf );
		}
		return buffer.toString();
	}

	/**
	 * Parses the result of an API call into a T.
	 * 
	 * @param data
	 *            an InputStream with the results of a request to the Facebook servers
	 * @param method
	 *            the method called
	 * @throws FacebookException
	 *             if <code>data</code> represents an error
	 * @throws IOException
	 *             if <code>data</code> is not readable
	 * @return a T
	 */
	protected abstract T parseCallResult( InputStream data, IFacebookMethod method ) throws FacebookException, IOException;

	public boolean fbml_refreshRefUrl( URL url ) throws FacebookException {
		return extractBoolean( callMethod( FacebookMethod.FBML_REFRESH_REF_URL, newPair( "url", url ) ) );
	}

	public T notifications_get() throws FacebookException {
		return callMethod( FacebookMethod.NOTIFICATIONS_GET );
	}

	public T users_getStandardInfo( Iterable<Long> userIds, Collection<ProfileField> fields ) throws FacebookException {
		assert ( userIds != null );
		assert ( fields != null );
		assert ( !fields.isEmpty() );
		return callMethod( FacebookMethod.USERS_GET_STANDARD_INFO, newPair( "uids", delimit( userIds ) ), newPair( "fields", delimit( fields ) ) );
	}

	public T users_getStandardInfo( Iterable<Long> userIds, Set<CharSequence> fields ) throws FacebookException {
		assert ( userIds != null );
		assert ( fields != null );
		assert ( !fields.isEmpty() );
		return callMethod( FacebookMethod.USERS_GET_STANDARD_INFO, newPair( "uids", delimit( userIds ) ), newPair( "fields", delimit( fields ) ) );
	}

	public T users_getInfo( Iterable<Long> userIds, Collection<ProfileField> fields ) throws FacebookException {
		assert ( userIds != null );
		assert ( fields != null );
		assert ( !fields.isEmpty() );
		return callMethod( FacebookMethod.USERS_GET_INFO, newPair( "uids", delimit( userIds ) ), newPair( "fields", delimit( fields ) ) );
	}

	public T users_getInfo( Iterable<Long> userIds, Set<CharSequence> fields ) throws FacebookException {
		assert ( userIds != null );
		assert ( fields != null );
		assert ( !fields.isEmpty() );
		return callMethod( FacebookMethod.USERS_GET_INFO, newPair( "uids", delimit( userIds ) ), newPair( "fields", delimit( fields ) ) );
	}

	/**
	 * Retrieves the user ID of the user logged in to this API session
	 * 
	 * @return the Facebook user ID of the logged-in user
	 */
	public long users_getLoggedInUser() throws FacebookException {
		if ( cacheUserId == null || cacheUserId == -1 || batchMode ) {
			cacheUserId = extractLong( callMethod( FacebookMethod.USERS_GET_LOGGED_IN_USER ) );
		}
		return cacheUserId;
	}

	/**
	 * Call this function to get the user ID.
	 * 
	 * @return The ID of the current session's user, or -1 if none.
	 * @deprecated please call auth_getSession(authToken), then you can call users_getLoggedInUser(), or getCacheUserId()
	 */
	@Deprecated
	public long auth_getUserId( String authToken ) throws FacebookException {
		/*
		 * Get the session information if we don't have it; this will populate the user ID as well.
		 */
		if ( null == cacheSessionKey ) {
			auth_getSession( authToken );
		}
		return users_getLoggedInUser();
	}

	public boolean isDesktop() {
		return _isDesktop;
	}

	@Deprecated
	public boolean users_isAppAdded() throws FacebookException {
		if ( cacheAppAdded == null ) {
			cacheAppAdded = extractBoolean( callMethod( FacebookMethod.USERS_IS_APP_ADDED ) );
		}
		return cacheAppAdded;
	}

	public boolean users_isAppUser() throws FacebookException {
		if ( cacheAppUser == null ) {
			cacheAppUser = extractBoolean( callMethod( FacebookMethod.USERS_IS_APP_USER ) );
		}
		return cacheAppUser;
	}

	public boolean users_isAppUser( Long userId ) throws FacebookException {
		if ( userId != null ) {
			return extractBoolean( callMethod( FacebookMethod.USERS_IS_APP_USER_NOSESSION, newPair( "uid", userId ) ) );
		} else {
			return extractBoolean( callMethod( FacebookMethod.USERS_IS_APP_USER ) );
		}
	}

	public boolean users_setStatus( String status ) throws FacebookException {
		return users_setStatus( status, false, false );
	}

	public boolean users_clearStatus() throws FacebookException {
		return users_setStatus( null, true );
	}

	@Deprecated
	public URL notifications_send( Collection<Long> recipientIds, CharSequence notification, CharSequence email ) throws FacebookException {
		notifications_send( recipientIds, notification );
		return null;
	}

	public boolean fbml_refreshImgSrc( String imageUrl ) throws FacebookException {
		try {
			return fbml_refreshImgSrc( new URL( imageUrl ) );
		}
		catch ( MalformedURLException ex ) {
			throw runtimeException( ex );
		}
	}

	public void setIsDesktop( boolean isDesktop ) {
		this._isDesktop = isDesktop;
	}

	protected static CharSequence delimit( Collection<Map.Entry<String,String>> entries, String delimiter, String equals, boolean doEncode ) {
		if ( entries == null || entries.isEmpty() ) {
			return null;
		}
		StringBuilder buffer = new StringBuilder();
		boolean notFirst = false;
		for ( Map.Entry<String,String> entry : entries ) {
			if ( notFirst ) {
				buffer.append( delimiter );
			} else {
				notFirst = true;
			}
			CharSequence value = entry.getValue();
			buffer.append( entry.getKey() ).append( equals ).append( doEncode ? encode( value ) : value );
		}
		return buffer;
	}

	public boolean fbml_refreshImgSrc( URL imageUrl ) throws FacebookException {
		return extractBoolean( callMethod( FacebookMethod.FBML_REFRESH_IMG_SRC, newPair( "url", imageUrl ) ) );
	}

	public T friends_get() throws FacebookException {
		return callMethod( FacebookMethod.FRIENDS_GET );
	}

	public T friends_get( Long uid ) throws FacebookException {
		if ( uid != null ) {
			return callMethod( FacebookMethod.FRIENDS_GET_NOSESSION, newPair( "uid", uid ) );
		} else {
			return callMethod( FacebookMethod.FRIENDS_GET );
		}
	}

	public String auth_createToken() throws FacebookException {
		T d = callMethod( FacebookMethod.AUTH_CREATE_TOKEN );
		return extractString( d );
	}

	/**
	 * Create a marketplace listing
	 * 
	 * @param showOnProfile
	 *            whether the listing can be shown on the user's profile
	 * @param attrs
	 *            the properties of the listing
	 * @return the id of the created listing
	 * @see MarketplaceListing
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.createListing"> Developers Wiki: marketplace.createListing</a>
	 */
	@Deprecated
	public Long marketplace_createListing( Boolean showOnProfile, MarketplaceListing attrs ) throws FacebookException {
		T result = callMethod( FacebookMethod.MARKETPLACE_CREATE_LISTING, newPair( "show_on_profile", showOnProfile ? "1" : "0" ), newPair( "listing_id", "0" ), newPair(
				"listing_attrs", attrs.jsonify() ) );
		return extractLong( result );
	}

	/**
	 * Modify a marketplace listing
	 * 
	 * @param listingId
	 *            identifies the listing to be modified
	 * @param showOnProfile
	 *            whether the listing can be shown on the user's profile
	 * @param attrs
	 *            the properties of the listing
	 * @return the id of the edited listing
	 * @see MarketplaceListing
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.createListing"> Developers Wiki: marketplace.createListing</a>
	 */
	@Deprecated
	public Long marketplace_editListing( Long listingId, Boolean showOnProfile, MarketplaceListing attrs ) throws FacebookException {
		T result = callMethod( FacebookMethod.MARKETPLACE_CREATE_LISTING, newPair( "show_on_profile", showOnProfile ? "1" : "0" ), newPair( "listing_id", listingId ),
				newPair( "listing_attrs", attrs.jsonify() ) );
		return extractLong( result );
	}

	/**
	 * Remove a marketplace listing
	 * 
	 * @param listingId
	 *            the listing to be removed
	 * @return boolean indicating whether the listing was removed
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.removeListing"> Developers Wiki: marketplace.removeListing</a>
	 */
	public boolean marketplace_removeListing( Long listingId ) throws FacebookException {
		return marketplace_removeListing( listingId, MARKETPLACE_STATUS_DEFAULT );
	}

	/**
	 * Remove a marketplace listing
	 * 
	 * @param listingId
	 *            the listing to be removed
	 * @param status
	 *            MARKETPLACE_STATUS_DEFAULT, MARKETPLACE_STATUS_SUCCESS, or MARKETPLACE_STATUS_NOT_SUCCESS
	 * @return boolean indicating whether the listing was removed
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.removeListing"> Developers Wiki: marketplace.removeListing</a>
	 */
	@Deprecated
	public boolean marketplace_removeListing( Long listingId, CharSequence status ) throws FacebookException {
		assert MARKETPLACE_STATUS_DEFAULT.equals( status ) || MARKETPLACE_STATUS_SUCCESS.equals( status ) || MARKETPLACE_STATUS_NOT_SUCCESS.equals( status ) : "Invalid status: "
				+ status;
		T result = callMethod( FacebookMethod.MARKETPLACE_REMOVE_LISTING, newPair( "listing_id", listingId ), newPair( "status", status ) );
		return extractBoolean( result );
	}

	/**
	 * Get the categories available in marketplace.
	 * 
	 * @return a T listing the marketplace categories
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.getCategories"> Developers Wiki: marketplace.getCategories</a>
	 */
	@Deprecated
	public List<String> marketplace_getCategories() throws FacebookException {
		T temp = callMethod( FacebookMethod.MARKETPLACE_GET_CATEGORIES );
		if ( temp == null ) {
			return null;
		}
		List<String> results = new ArrayList<String>();
		if ( temp instanceof Document ) {
			Document d = (Document) temp;
			NodeList cats = d.getElementsByTagName( "marketplace_category" );
			for ( int count = 0; count < cats.getLength(); count++ ) {
				results.add( cats.item( count ).getFirstChild().getTextContent() );
			}
		} else {
			JSONObject j = (JSONObject) temp;
			Iterator it = j.keys();
			while ( it.hasNext() ) {
				try {
					results.add( j.get( (String) it.next() ).toString() );
				}
				catch ( Exception ex ) {
					runtimeException( ex );
				}
			}
		}
		return results;
	}

	/**
	 * Get the subcategories available for a category.
	 * 
	 * @param category
	 *            a category, e.g. "HOUSING"
	 * @return a T listing the marketplace sub-categories
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.getSubCategories"> Developers Wiki: marketplace.getSubCategories</a>
	 */
	public T marketplace_getSubCategories( CharSequence category ) throws FacebookException {
		return callMethod( FacebookMethod.MARKETPLACE_GET_SUBCATEGORIES, newPair( "category", category ) );
	}

	/**
	 * Fetch marketplace listings, filtered by listing IDs and/or the posting users' IDs.
	 * 
	 * @param listingIds
	 *            listing identifiers (required if uids is null/empty)
	 * @param userIds
	 *            posting user identifiers (required if listingIds is null/empty)
	 * @return a T of marketplace listings
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.getListings"> Developers Wiki: marketplace.getListings</a>
	 */
	public T marketplace_getListings( Collection<Long> listingIds, Collection<Long> userIds ) throws FacebookException {
		List<Pair<String,CharSequence>> params = new ArrayList<Pair<String,CharSequence>>( 2 );
		if ( null != listingIds && !listingIds.isEmpty() ) {
			params.add( newPair( "listing_ids", delimit( listingIds ) ) );
		}
		if ( null != userIds && !userIds.isEmpty() ) {
			params.add( newPair( "uids", delimit( userIds ) ) );
		}
		assert !params.isEmpty() : "Either listingIds or userIds should be provided";
		return callMethod( FacebookMethod.MARKETPLACE_GET_LISTINGS, params );
	}

	/**
	 * Search for marketplace listings, optionally by category, subcategory, and/or query string.
	 * 
	 * @param category
	 *            the category of listings desired (optional except if subcategory is provided)
	 * @param subCategory
	 *            the subcategory of listings desired (optional)
	 * @param query
	 *            a query string (optional)
	 * @return a T of marketplace listings
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.search"> Developers Wiki: marketplace.search</a>
	 */
	@Deprecated
	public T marketplace_search( CharSequence category, CharSequence subCategory, CharSequence query ) throws FacebookException {
		List<Pair<String,CharSequence>> params = new ArrayList<Pair<String,CharSequence>>( 3 );
		boolean hasCategory = addParamIfNotBlank( "category", category, params );
		if ( hasCategory ) {
			addParamIfNotBlank( "subcategory", subCategory, params );
		}
		addParamIfNotBlank( "query", query, params );
		return callMethod( FacebookMethod.MARKETPLACE_SEARCH, params );
	}

	/**
	 * Get the categories available in marketplace.
	 * 
	 * @return a T listing the marketplace categories
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.getCategories"> Developers Wiki: marketplace.getCategories</a>
	 */
	public T marketplace_getCategoriesObject() throws FacebookException {
		return callMethod( FacebookMethod.MARKETPLACE_GET_CATEGORIES );
	}

	public String getRawResponse() {
		return rawResponse;
	}

	public Object getResponsePOJO() {
		if ( rawResponse == null ) {
			return null;
		}
		if ( JAXB_CONTEXT == null ) {
			return null;
		}
		if ( ( getResponseFormat() != null ) && ( !"xml".equalsIgnoreCase( getResponseFormat() ) ) ) {
			// JAXB will not work with JSON
			throw new RuntimeException( "You can only generate a response POJO when using XML formatted API responses! JSON users go elsewhere!" );
		}
		try {
			Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller();
			return unmarshaller.unmarshal( new ByteArrayInputStream( rawResponse.getBytes( "UTF-8" ) ) );
		}
		catch ( Exception ex ) {
			throw runtimeException( ex );
		}
	}

	public boolean feed_PublishTemplatizedAction( TemplatizedAction action ) throws FacebookException {
		return templatizedFeedHandler( action.getTitleTemplate(), action.getTitleParams(), action.getBodyTemplate(), action.getBodyParams(), action.getBodyGeneral(),
				action.getPictures(), action.getTargetIds(), action.getPageActorId() );
	}

	public boolean feed_publishTemplatizedAction( String titleTemplate, String titleData, String bodyTemplate, String bodyData, String bodyGeneral,
			Collection<? extends IPair<? extends Object,URL>> pictures, String targetIds ) throws FacebookException {
		return templatizedFeedHandler( titleTemplate, titleData, bodyTemplate, bodyData, bodyGeneral, pictures, targetIds, null );
	}

	protected boolean templatizedFeedHandler( String titleTemplate, String titleData, String bodyTemplate, String bodyData, String bodyGeneral,
			Collection<? extends IPair<? extends Object,URL>> pictures, String targetIds, Long pageId ) throws FacebookException {
		assert ( pictures == null || pictures.size() <= 4 );

		List<Pair<String,CharSequence>> params = new ArrayList<Pair<String,CharSequence>>( 15 );

		// these are always required parameters
		addParam( "title_template", titleTemplate, params );

		// these are optional parameters
		addParamIfNotBlank( "title_data", titleData, params );
		boolean hasBody = addParamIfNotBlank( "body_template", bodyTemplate, params );
		if ( hasBody ) {
			addParamIfNotBlank( "body_data", bodyData, params );
		}
		addParamIfNotBlank( "body_general", bodyGeneral, params );
		if ( pictures != null ) {
			int count = 1;
			for ( IPair picture : pictures ) {
				String url = picture.getFirst().toString();
				if ( url.startsWith( TemplatizedAction.UID_TOKEN ) ) {
					url = url.substring( TemplatizedAction.UID_TOKEN.length() );
				}
				addParam( "image_" + count, url, params );
				if ( picture.getSecond() != null ) {
					addParam( "image_" + count + "_link", picture.getSecond().toString(), params );
				}
				count++ ;
			}
		}
		addParamIfNotBlank( "target_ids", targetIds, params );
		addParamIfNotBlank( "page_actor_id", pageId, params );
		return extractBoolean( callMethod( FacebookMethod.FEED_PUBLISH_TEMPLATIZED_ACTION, params ) );
	}

	public boolean users_hasAppPermission( Permission perm ) throws FacebookException {
		return users_hasAppPermission( perm, null );
	}

	public boolean users_hasAppPermission( Permission perm, Long userId ) throws FacebookException {
		if ( userId != null ) {
			return extractBoolean( callMethod( FacebookMethod.USERS_HAS_APP_PERMISSION_NOSESSION, newPair( "ext_perm", perm.getName() ), newPair( "uid", userId ) ) );
		} else {
			return extractBoolean( callMethod( FacebookMethod.USERS_HAS_APP_PERMISSION, newPair( "ext_perm", perm.getName() ) ) );
		}
	}


	public Long marketplace_createListing( Long listingId, boolean showOnProfile, String attributes ) throws FacebookException {
		T result = callMethod( FacebookMethod.MARKETPLACE_CREATE_LISTING, newPair( "show_on_profile", showOnProfile ? "1" : "0" ), newPair( "listing_id", "0" ), newPair(
				"listing_attrs", attributes ) );
		return extractLong( result );
	}

	public Long marketplace_createListing( Long listingId, boolean showOnProfile, MarketListing listing ) throws FacebookException {
		return marketplace_createListing( listingId, showOnProfile, listing.getAttribs() );
	}

	public Long marketplace_createListing( boolean showOnProfile, MarketListing listing ) throws FacebookException {
		return marketplace_createListing( null, showOnProfile, listing.getAttribs() );
	}

	public boolean marketplace_removeListing( Long listingId, MarketListingStatus status ) throws FacebookException {
		return marketplace_removeListing( listingId, status.getName() );
	}

	public Long marketplace_editListing( Long listingId, Boolean showOnProfile, MarketListing attrs ) throws FacebookException {
		T result = callMethod( FacebookMethod.MARKETPLACE_CREATE_LISTING, newPair( "show_on_profile", showOnProfile ? "1" : "0" ), newPair( "listing_id", listingId ),
				newPair( "listing_attrs", attrs.getAttribs() ) );
		return extractLong( result );
	}

	public boolean users_setStatus( String newStatus, boolean clear ) throws FacebookException {
		return users_setStatus( newStatus, clear, false );
	}

	/**
	 * Retrieves the requested profile fields for the Facebook Pages with the given <code>pageIds</code>. Can be called for pages that have added the application
	 * without establishing a session.
	 * 
	 * @param pageIds
	 *            the page IDs
	 * @param fields
	 *            a set of page profile fields
	 * @return a T consisting of a list of pages, with each page element containing the requested fields.
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.getInfo"> Developers Wiki: Pages.getInfo</a>
	 */
	public T pages_getInfo( Collection<Long> pageIds, EnumSet<PageProfileField> fields ) throws FacebookException {
		if ( pageIds == null || pageIds.isEmpty() ) {
			throw new IllegalArgumentException( "pageIds cannot be empty or null" );
		}
		if ( fields == null || fields.isEmpty() ) {
			throw new IllegalArgumentException( "fields cannot be empty or null" );
		}
		IFacebookMethod method = ( null == cacheSessionKey ) ? FacebookMethod.PAGES_GET_INFO_NOSESSION : FacebookMethod.PAGES_GET_INFO;
		return callMethod( method, newPair( "page_ids", delimit( pageIds ) ), newPair( "fields", delimit( fields ) ) );
	}

	/**
	 * Retrieves the requested profile fields for the Facebook Pages with the given <code>pageIds</code>. Can be called for pages that have added the application
	 * without establishing a session.
	 * 
	 * @param pageIds
	 *            the page IDs
	 * @param fields
	 *            a set of page profile fields
	 * @return a T consisting of a list of pages, with each page element containing the requested fields.
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.getInfo"> Developers Wiki: Pages.getInfo</a>
	 */
	public T pages_getInfo( Collection<Long> pageIds, Set<CharSequence> fields ) throws FacebookException {
		if ( pageIds == null || pageIds.isEmpty() ) {
			throw new IllegalArgumentException( "pageIds cannot be empty or null" );
		}
		if ( fields == null || fields.isEmpty() ) {
			throw new IllegalArgumentException( "fields cannot be empty or null" );
		}
		IFacebookMethod method = null == cacheSessionKey ? FacebookMethod.PAGES_GET_INFO_NOSESSION : FacebookMethod.PAGES_GET_INFO;
		return callMethod( method, newPair( "page_ids", delimit( pageIds ) ), newPair( "fields", delimit( fields ) ) );
	}

	/**
	 * Retrieves the requested profile fields for the Facebook Pages of the user with the given <code>userId</code>.
	 * 
	 * @param userId
	 *            the ID of a user about whose pages to fetch info (defaulted to the logged-in user)
	 * @param fields
	 *            a set of PageProfileFields
	 * @return a T consisting of a list of pages, with each page element containing the requested fields.
	 * @see http://wiki.developers.facebook.com/index.php/Pages.getInfo
	 */
	public T pages_getInfo( Long userId, EnumSet<PageProfileField> fields ) throws FacebookException {
		if ( fields == null || fields.isEmpty() ) {
			throw new IllegalArgumentException( "fields cannot be empty or null" );
		}
		if ( userId == null ) {
			userId = cacheUserId;
		}
		if ( userId == null ) {
			return callMethod( FacebookMethod.PAGES_GET_INFO, newPair( "fields", delimit( fields ) ) );
		}
		return callMethod( FacebookMethod.PAGES_GET_INFO, newPair( "uid", userId ), newPair( "fields", delimit( fields ) ) );
	}

	/**
	 * Retrieves the requested profile fields for the Facebook Pages of the user with the given <code>userId</code>.
	 * 
	 * @param userId
	 *            the ID of a user about whose pages to fetch info (defaulted to the logged-in user)
	 * @param fields
	 *            a set of page profile fields
	 * @return a T consisting of a list of pages, with each page element containing the requested fields.
	 * @see http://wiki.developers.facebook.com/index.php/Pages.getInfo
	 */
	public T pages_getInfo( Long userId, Set<CharSequence> fields ) throws FacebookException {
		if ( fields == null || fields.isEmpty() ) {
			throw new IllegalArgumentException( "fields cannot be empty or null" );
		}
		if ( userId == null ) {
			userId = cacheUserId;
		}
		if ( userId == null ) {
			return callMethod( FacebookMethod.PAGES_GET_INFO, newPair( "fields", delimit( fields ) ) );
		}
		return callMethod( FacebookMethod.PAGES_GET_INFO, newPair( "uid", userId ), newPair( "fields", delimit( fields ) ) );
	}

	/**
	 * Checks whether a page has added the application
	 * 
	 * @param pageId
	 *            the ID of the page
	 * @return true if the page has added the application
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.isAppAdded"> Developers Wiki: Pages.isAppAdded</a>
	 */
	public boolean pages_isAppAdded( Long pageId ) throws FacebookException {
		return extractBoolean( callMethod( FacebookMethod.PAGES_IS_APP_ADDED, newPair( "page_id", pageId ) ) );
	}

	/**
	 * Checks whether a user is a fan of the page with the given <code>pageId</code>.
	 * 
	 * @param pageId
	 *            the ID of the page
	 * @param userId
	 *            the ID of the user (defaults to the logged-in user if null)
	 * @return true if the user is a fan of the page
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.isFan"> Developers Wiki: Pages.isFan</a>
	 */
	public boolean pages_isFan( Long pageId, Long userId ) throws FacebookException {
		return extractBoolean( callMethod( FacebookMethod.PAGES_IS_FAN, newPair( "page_id", pageId ), newPair( "uid", userId ) ) );
	}

	/**
	 * Checks whether the logged-in user is a fan of the page with the given <code>pageId</code>.
	 * 
	 * @param pageId
	 *            the ID of the page
	 * @return true if the logged-in user is a fan of the page
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.isFan"> Developers Wiki: Pages.isFan</a>
	 */
	public boolean pages_isFan( Long pageId ) throws FacebookException {
		return extractBoolean( callMethod( FacebookMethod.PAGES_IS_FAN, newPair( "page_id", pageId ) ) );
	}

	/**
	 * Checks whether the logged-in user for this session is an admin of the page with the given <code>pageId</code>.
	 * 
	 * @param pageId
	 *            the ID of the page
	 * @return true if the logged-in user is an admin
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.isAdmin"> Developers Wiki: Pages.isAdmin</a>
	 */
	public boolean pages_isAdmin( Long pageId ) throws FacebookException {
		return extractBoolean( callMethod( FacebookMethod.PAGES_IS_ADMIN, newPair( "page_id", pageId ) ) );
	}

	/**
	 * Associates a "<code>handle</code>" with FBML markup so that the handle can be used within the <a
	 * href="http://wiki.developers.facebook.com/index.php/Fb:ref">fb:ref</a> FBML tag. A handle is unique within an application and allows an application to publish
	 * identical FBML to many user profiles and do subsequent updates without having to republish FBML for each user.
	 * 
	 * @param handle -
	 *            a string, unique within the application, that
	 * @param fbmlMarkup -
	 *            refer to the FBML documentation for a description of the markup and its role in various contexts
	 * @return a boolean indicating whether the FBML was successfully set
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Fbml.setRefHandle"> Developers Wiki: Fbml.setRefHandle</a>
	 */
	public boolean fbml_setRefHandle( String handle, String fbmlMarkup ) throws FacebookException {
		if ( _isDesktop ) {
			// this method cannot be called from a desktop app
			throw new FacebookException( ErrorCode.GEN_PERMISSIONS_ERROR, "Desktop applications cannot use 'fbml_setReftHandle'" );
		}
		return extractBoolean( callMethod( FacebookMethod.FBML_SET_REF_HANDLE, newPair( "handle", handle ), newPair( "fbml", fbmlMarkup ) ) );

	}

	public void notifications_send( Collection<Long> recipientIds, CharSequence notification ) throws FacebookException {
		notifications_send( recipientIds, notification.toString(), false );
	}

	public boolean users_setStatus( String newStatus, boolean clear, boolean statusIncludesVerb ) throws FacebookException {
		Collection<Pair<String,CharSequence>> params = new ArrayList<Pair<String,CharSequence>>();
		if ( newStatus != null ) {
			params.add( newPair( "status", newStatus ) );
		}
		if ( clear ) {
			params.add( newPair( "clear", "true" ) );
		}
		if ( statusIncludesVerb ) {
			params.add( newPair( "status_includes_verb", "true" ) );
		}
		return extractBoolean( callMethod( FacebookMethod.USERS_SET_STATUS, params ) );
	}

	/**
	 * Send a notification message to the logged-in user.
	 * 
	 * @param notification
	 *            the FBML to be displayed on the notifications page; only a stripped-down set of FBML tags that result in text and links is allowed
	 * @return a URL, possibly null, to which the user should be redirected to finalize the sending of the email
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Notifications.sendEmail"> Developers Wiki: notifications.send</a>
	 */
	public void notifications_send( CharSequence notification ) throws FacebookException {
		Long currentUser = users_getLoggedInUser();
		Collection<Long> coll = new ArrayList<Long>();
		coll.add( currentUser );
		notifications_send( coll, notification );
	}

	public T data_getCookies() throws FacebookException {
		return data_getCookies( users_getLoggedInUser(), null );
	}

	public T data_getCookies( Long userId ) throws FacebookException {
		return data_getCookies( userId, null );
	}

	public T data_getCookies( String name ) throws FacebookException {
		return data_getCookies( users_getLoggedInUser(), name );
	}

	public T data_getCookies( Long userId, CharSequence name ) throws FacebookException {
		if ( name == null ) {
			return callMethod( FacebookMethod.DATA_GET_COOKIES, newPair( "uid", userId ) );
		} else {
			return callMethod( FacebookMethod.DATA_GET_COOKIES, newPair( "uid", userId ), newPair( "name", name ) );
		}
	}

	public boolean data_setCookie( String name, String value ) throws FacebookException {
		return data_setCookie( users_getLoggedInUser(), name, value, null, null );
	}

	public boolean data_setCookie( String name, String value, String path ) throws FacebookException {
		return data_setCookie( users_getLoggedInUser(), name, value, null, path );
	}

	public boolean data_setCookie( Long userId, CharSequence name, CharSequence value ) throws FacebookException {
		return data_setCookie( userId, name, value, null, null );
	}

	public boolean data_setCookie( Long userId, CharSequence name, CharSequence value, CharSequence path ) throws FacebookException {
		return data_setCookie( userId, name, value, null, path );
	}

	public boolean data_setCookie( String name, String value, Long expires ) throws FacebookException {
		return data_setCookie( users_getLoggedInUser(), name, value, expires, null );
	}

	public boolean data_setCookie( String name, String value, Long expires, String path ) throws FacebookException {
		return data_setCookie( users_getLoggedInUser(), name, value, expires, path );
	}

	public boolean data_setCookie( Long userId, CharSequence name, CharSequence value, Long expires ) throws FacebookException {
		return data_setCookie( userId, name, value, expires, null );
	}

	public boolean data_setCookie( Long userId, CharSequence name, CharSequence value, Long expires, CharSequence path ) throws FacebookException {
		if ( ( name == null ) || ( "".equals( name ) ) ) {
			throw new FacebookException( ErrorCode.GEN_INVALID_PARAMETER, "The cookie name cannot be null or empty!" );
		}
		if ( value == null ) {
			value = "";
		}
		List<Pair<String,CharSequence>> params = new ArrayList<Pair<String,CharSequence>>( 5 );
		addParam( "uid", userId, params );
		addParam( "name", name, params );
		addParam( "value", value, params );
		addParamIfNotBlankZero( "expires", expires, params );
		addParamIfNotBlank( "path", path, params );
		T doc = callMethod( FacebookMethod.DATA_SET_COOKIE, params );
		return extractBoolean( doc );
	}

	public String data_getUserPreference( int prefId ) throws FacebookException {
		return extractString( callMethod( FacebookMethod.DATA_GET_USER_PREFERENCE, newPair( "pref_id", prefId ) ) );
	}

	public T data_getUserPreferences() throws FacebookException {
		return callMethod( FacebookMethod.DATA_GET_USER_PREFERENCES );
	}

	public void data_setUserPreference( int prefId, String value ) throws FacebookException {
		if ( value != null && value.length() > 128 ) {
			throw new FacebookException(
					ErrorCode.GEN_INVALID_PARAMETER,
					String
							.format(
									"Attempt to set a preference which hold a maximum of 128 characters to a value with %d characters. The Facebook API silently truncates this value to 128 characters which can lead to unpredictable results. If you want the truncation behaviour, please truncate the string in your Java code.",
									value.length() ) );
		}
		callMethod( FacebookMethod.DATA_SET_USER_PREFERENCE, newPair( "pref_id", prefId ), newPair( "value", value ) );
	}

	public void data_setUserPreferences( Map<Integer,String> values, boolean replace ) throws FacebookException {
		JSONObject prefs = new JSONObject();
		for ( Integer key : values.keySet() ) {
			try {
				prefs.put( key.toString(), values.get( key ) );
			}
			catch ( JSONException ex ) {
				throw runtimeException( ex );
			}
		}

		callMethod( FacebookMethod.DATA_SET_USER_PREFERENCES, newPair( "values", prefs.toString() ), newPairTF( "replace", replace ) );
	}

	public long data_createObject( String objectType, Map<String,String> properties ) throws FacebookException {
		return extractLong( callMethod( FacebookMethod.DATA_CREATE_OBJECT, newPair( "obj_type", objectType ), newPair( "properties", toJson( properties ) ) ) );
	}

	public void data_updateObject( long objectId, Map<String,String> properties, boolean replace ) throws FacebookException {
		callMethod( FacebookMethod.DATA_UPDATE_OBJECT, newPair( "obj_id", String.valueOf( objectId ) ), newPair( "properties", toJson( properties ) ), newPairTF(
				"replace", replace ) );
	}

	public void data_deleteObject( long objectId ) throws FacebookException {
		callMethod( FacebookMethod.DATA_DELETE_OBJECT, newPair( "obj_id", objectId ) );
	}

	public void data_deleteObjects( Collection<Long> objectIds ) throws FacebookException {
		callMethod( FacebookMethod.DATA_DELETE_OBJECTS, newPair( "obj_ids", delimit( objectIds ) ) );
	}

	public T data_getObject( long objectId ) throws FacebookException {
		return callMethod( FacebookMethod.DATA_GET_OBJECT, newPair( "obj_id", objectId ) );
	}

	public T data_getObjects( Collection<Long> objectIds ) throws FacebookException {
		return callMethod( FacebookMethod.DATA_GET_OBJECTS, newPair( "obj_ids", delimit( objectIds ) ) );
	}

	public T data_getObjectProperty( long objectId, String propertyName ) throws FacebookException {
		return callMethod( FacebookMethod.DATA_GET_OBJECT_PROPERTY, newPair( "obj_id", objectId ), newPair( "prop_name", propertyName ) );
	}

	public void data_setObjectProperty( long objectId, String propertyName, String value ) throws FacebookException {
		callMethod( FacebookMethod.DATA_SET_OBJECT_PROPERTY, newPair( "obj_id", objectId ), newPair( "prop_name", propertyName ), newPair( "value", value ) );
	}

	public void data_createObjectType( String name ) throws FacebookException {
		callMethod( FacebookMethod.DATA_CREATE_OBJECT_TYPE, newPair( "name", name ) );
	}

	public void data_dropObjectType( String objectType ) throws FacebookException {
		callMethod( FacebookMethod.DATA_DROP_OBJECT_TYPE, newPair( "obj_type", objectType ) );
	}

	public void data_renameObjectType( String objectType, String newName ) throws FacebookException {
		callMethod( FacebookMethod.DATA_RENAME_OBJECT_TYPE, newPair( "obj_type", objectType ), newPair( "new_name", newName ) );

	}

	public void data_defineObjectProperty( String objectType, String propertyName, PropertyType propertyType ) throws FacebookException {
		callMethod( FacebookMethod.DATA_DEFINE_OBJECT_PROPERTY, newPair( "obj_type", objectType ), newPair( "prop_name", propertyName ), newPair( "prop_type",
				propertyType.getValue() ) );
	}

	public void data_undefineObjectProperty( String objectType, String propertyName ) throws FacebookException {
		callMethod( FacebookMethod.DATA_UNDEFINE_OBJECT_PROPERTY, newPair( "obj_type", objectType ), newPair( "prop_name", propertyName ) );
	}

	public void data_renameObjectProperty( String objectType, String propertyName, String newPropertyName ) throws FacebookException {
		callMethod( FacebookMethod.DATA_RENAME_OBJECT_PROPERTY, newPair( "obj_type", objectType ), newPair( "prop_name", propertyName ), newPair( "new_name",
				newPropertyName ) );
	}

	public T data_getObjectTypes() throws FacebookException {
		return callMethod( FacebookMethod.DATA_GET_OBJECT_TYPES );
	}

	public T data_getObjectType( String objectType ) throws FacebookException {
		return callMethod( FacebookMethod.DATA_GET_OBJECT_TYPE, newPair( "obj_type", objectType ) );
	}

	public void data_defineAssociation( String associationName, AssociationType associationType, AssociationInfo associationInfo1, AssociationInfo associationInfo2,
			String inverseName ) throws FacebookException {
		List<Pair<String,CharSequence>> params = new ArrayList<Pair<String,CharSequence>>( 5 );
		addParam( "name", associationName, params );
		addParam( "assoc_type", associationType.getValue(), params );
		JSONObject assocInfo1 = new JSONObject();
		try {
			assocInfo1.put( "alias", associationInfo1.getAlias() );
			assocInfo1.put( "object_type", associationInfo1.getObjectType() );
			assocInfo1.put( "unique", associationInfo1.isUnique() );
		}
		catch ( JSONException ex ) {
			throw runtimeException( ex );
		}
		addParam( "assoc_info1", assocInfo1.toString(), params );
		JSONObject assocInfo2 = new JSONObject();
		try {
			assocInfo2.put( "alias", associationInfo2.getAlias() );
			assocInfo2.put( "object_type", associationInfo2.getObjectType() );
			assocInfo2.put( "unique", associationInfo2.isUnique() );
		}
		catch ( JSONException ex ) {
			throw runtimeException( ex );
		}
		addParam( "assoc_info2", assocInfo2, params );
		addParamIfNotBlank( "inverse", inverseName, params );

		callMethod( FacebookMethod.DATA_DEFINE_ASSOCIATION, params );
	}

	public void data_undefineAssociation( String name ) throws FacebookException {
		callMethod( FacebookMethod.DATA_UNDEFINE_ASSOCIATION, newPair( "name", name ) );
	}

	public void data_renameAssociation( String name, String newName, String newAlias1, String newAlias2 ) throws FacebookException {
		List<Pair<String,CharSequence>> params = new ArrayList<Pair<String,CharSequence>>( 4 );
		addParam( "name", name, params );
		addParam( "new_name", newName, params );
		addParamIfNotBlank( "new_alias1", newAlias1, params );
		addParamIfNotBlank( "new_alias2", newAlias2, params );

		callMethod( FacebookMethod.DATA_RENAME_ASSOCIATION, params );
	}

	public T data_getAssociationDefinition( String name ) throws FacebookException {
		return callMethod( FacebookMethod.DATA_GET_ASSOCIATION_DEFINITION, newPair( "name", name ) );
	}

	public T data_getAssociationDefinitions() throws FacebookException {
		return callMethod( FacebookMethod.DATA_GET_ASSOCIATION_DEFINITIONS );
	}


	public void data_setAssociation( String associationName, long object1Id, long object2Id, String data, Date associationTime ) throws FacebookException {
		List<Pair<String,CharSequence>> params = new ArrayList<Pair<String,CharSequence>>( 5 );
		addParam( "name", associationName, params );
		addParam( "obj_id1", object1Id, params );
		addParam( "obj_id2", object2Id, params );
		addParamIfNotBlank( "data", data, params );
		addParamSecondsIfNotBlank( "assoc_time", associationTime, params );
		callMethod( FacebookMethod.DATA_SET_ASSOCIATION, params );
	}

	public void data_removeAssociation( String associationName, long object1Id, long object2Id ) throws FacebookException {
		callMethod( FacebookMethod.DATA_REMOVE_ASSOCIATION, newPair( "name", associationName ), newPair( "obj_id1", object1Id ), newPair( "obj_id2", object2Id ) );
	}

	public void data_removeAssociatedObjects( String associationName, long objectId ) throws FacebookException {
		callMethod( FacebookMethod.DATA_REMOVE_ASSOCIATED_OBJECTS, newPair( "name", associationName ), newPair( "obj_id", objectId ) );
	}

	public long data_getAssociatedObjectCount( String associationName, long objectId ) throws FacebookException {
		return extractLong( callMethod( FacebookMethod.DATA_GET_ASSOCIATED_OBJECT_COUNT, newPair( "name", associationName ), newPair( "obj_id", objectId ) ) );
	}

	public boolean admin_setAppProperties( Map<ApplicationProperty,String> properties ) throws FacebookException {
		if ( _isDesktop ) {
			// this method cannot be called from a desktop app
			throw new FacebookException( ErrorCode.GEN_PERMISSIONS_ERROR, "Desktop applications cannot use 'admin_setAppProperties'" );
		}

		if ( ( properties == null ) || ( properties.isEmpty() ) ) {
			// nothing to do
			return true;
		}

		// Facebook is nonspecific about how they want the parameters encoded in JSON, so we make two attempts
		JSONObject encoding1 = new JSONObject();
		JSONArray encoding2 = new JSONArray();
		for ( ApplicationProperty property : properties.keySet() ) {
			JSONObject temp = new JSONObject();
			if ( property.getType().equals( "string" ) ) {
				// simple case, just treat it as a literal string
				try {
					encoding1.put( property.getName(), properties.get( property ) );
					temp.put( property.getName(), properties.get( property ) );
					encoding2.put( temp );
				}
				catch ( JSONException ex ) {
					throw runtimeException( ex );
				}
			} else {
				// we need to parse a boolean value
				String val = properties.get( property );
				if ( ( val == null ) || ( val.equals( "" ) ) || ( val.equalsIgnoreCase( "false" ) ) || ( val.equals( "0" ) ) ) {
					// false
					val = "0";
				} else {
					// true
					val = "1";
				}
				try {
					encoding1.put( property.getName(), val );
					temp.put( property.getName(), val );
					encoding2.put( temp );
				}
				catch ( JSONException ex ) {
					throw runtimeException( ex );
				}
			}
		}

		// now we've built our JSON-encoded parameter, so attempt to set the properties
		try {
			// first assume that Facebook is sensible enough to be able to undestand an associative array
			T d = callMethod( FacebookMethod.ADMIN_SET_APP_PROPERTIES, newPair( "properties", encoding1 ) );
			return extractBoolean( d );
		}
		catch ( FacebookException e ) {
			// if that didn't work, try the more convoluted encoding (which matches what they send back in response to admin_getAppProperties calls)
			T d = callMethod( FacebookMethod.ADMIN_SET_APP_PROPERTIES, newPair( "properties", encoding2 ) );
			return extractBoolean( d );
		}
	}

	/**
	 * @deprecated use admin_getAppPropertiesMap() instead
	 */
	@Deprecated
	public JSONObject admin_getAppProperties( Collection<ApplicationProperty> properties ) throws FacebookException {
		String json = admin_getAppPropertiesAsString( properties );
		if ( json == null ) {
			return null;
		}
		try {
			if ( json.matches( "\\{.*\\}" ) ) {
				return new JSONObject( json );
			} else {
				JSONArray temp = new JSONArray( json );
				JSONObject result = new JSONObject();
				for ( int count = 0; count < temp.length(); count++ ) {
					JSONObject obj = (JSONObject) temp.get( count );
					Iterator it = obj.keys();
					while ( it.hasNext() ) {
						String next = (String) it.next();
						result.put( next, obj.get( next ) );
					}
				}
				return result;
			}
		}
		catch ( Exception e ) {
			// response failed to parse
			throw new FacebookException( ErrorCode.GEN_SERVICE_ERROR, "Failed to parse server response:  " + json );
		}
	}

	public Map<ApplicationProperty,String> admin_getAppPropertiesMap( Collection<ApplicationProperty> properties ) throws FacebookException {
		Map<ApplicationProperty,String> result = new LinkedHashMap<ApplicationProperty,String>();
		String json = admin_getAppPropertiesAsString( properties );
		if ( json == null ) {
			return null;
		}
		if ( json.matches( "\\{.*\\}" ) ) {
			json = json.substring( 1, json.lastIndexOf( "}" ) );
		} else {
			json = json.substring( 1, json.lastIndexOf( "]" ) );
		}
		String[] parts = json.split( "\\," );
		for ( String part : parts ) {
			parseFragment( part, result );
		}

		return result;
	}

	static Map<ApplicationProperty,String> parseProperties( String json ) {
		Map<ApplicationProperty,String> result = new TreeMap<ApplicationProperty,String>();
		if ( json == null ) {
			return null;
		}
		if ( json.matches( "\\{.*\\}" ) ) {
			json = json.substring( 1, json.lastIndexOf( "}" ) );
		} else {
			json = json.substring( 1, json.lastIndexOf( "]" ) );
		}
		String[] parts = json.split( "\\," );
		for ( String part : parts ) {
			parseFragment( part, result );
		}
		return result;
	}

	private static void parseFragment( String fragment, Map<ApplicationProperty,String> result ) {
		if ( fragment.startsWith( "{" ) ) {
			fragment = fragment.substring( 1, fragment.lastIndexOf( "}" ) );
		}
		String keyString = fragment.substring( 1 );
		keyString = keyString.substring( 0, keyString.indexOf( '"' ) );
		ApplicationProperty key = ApplicationProperty.getPropertyForString( keyString );
		String value = fragment.substring( fragment.indexOf( ":" ) + 1 ).replaceAll( "\\\\", "" ); // strip escape characters
		if ( key.getType().equals( "string" ) ) {
			result.put( key, value.substring( 1, value.lastIndexOf( '"' ) ) );
		} else {
			if ( value.equals( "1" ) ) {
				result.put( key, "true" );
			} else {
				result.put( key, "false" );
			}
		}
	}

	public boolean feed_publishTemplatizedAction( CharSequence titleTemplate ) throws FacebookException {
		return feed_publishTemplatizedAction( titleTemplate, null );
	}

	public boolean feed_publishTemplatizedAction( CharSequence titleTemplate, Long pageActorId ) throws FacebookException {
		return feed_publishTemplatizedAction( titleTemplate, null, null, null, null, null, null, pageActorId );
	}

	public boolean feed_publishTemplatizedAction( CharSequence titleTemplate, Map<String,CharSequence> titleData, CharSequence bodyTemplate,
			Map<String,CharSequence> bodyData, CharSequence bodyGeneral, Collection<Long> targetIds, Collection<? extends IPair<? extends Object,URL>> images,
			Long pageActorId ) throws FacebookException {
		assert null != titleTemplate && !"".equals( titleTemplate );

		List<Pair<String,CharSequence>> params = new ArrayList<Pair<String,CharSequence>>( 15 );

		params.add( newPair( "title_template", titleTemplate ) );
		if ( null != titleData && !titleData.isEmpty() ) {
			JSONObject titleDataJson = new JSONObject();
			try {
				for ( String key : titleData.keySet() ) {
					titleDataJson.put( key, titleData.get( key ) );
				}
			}
			catch ( Exception ex ) {
				throw runtimeException( ex );
			}
			params.add( newPair( "title_data", titleDataJson ) );
		}

		if ( null != bodyTemplate && !"".equals( bodyTemplate ) ) {
			params.add( newPair( "body_template", bodyTemplate ) );
			if ( null != bodyData && !bodyData.isEmpty() ) {
				JSONObject bodyDataJson = new JSONObject();
				try {
					for ( String key : bodyData.keySet() ) {
						bodyDataJson.put( key, bodyData.get( key ) );
					}
				}
				catch ( Exception ex ) {
					throw runtimeException( ex );
				}
				params.add( newPair( "body_data", bodyDataJson ) );
			}
		}

		if ( null != bodyTemplate && !"".equals( bodyTemplate ) ) {
			params.add( newPair( "body_template", bodyTemplate ) );
		}

		if ( null != targetIds && !targetIds.isEmpty() ) {
			params.add( newPair( "target_ids", delimit( targetIds ) ) );
		}

		if ( bodyGeneral != null ) {
			params.add( newPair( "body_general", bodyGeneral ) );
		}

		if ( pageActorId != null ) {
			params.add( newPair( "page_actor_id", pageActorId ) );
		}

		handleFeedImages( params, images );

		return extractBoolean( callMethod( FacebookMethod.FEED_PUBLISH_TEMPLATIZED_ACTION, params ) );
	}

	public T friends_getList( Long friendListId ) throws FacebookException {
		if ( null != friendListId && 0L <= friendListId ) {
			throw new IllegalArgumentException( "given invalid friendListId " + friendListId );
		}
		return callMethod( FacebookMethod.FRIENDS_GET, newPair( "flid", friendListId ) );
	}

	public T friends_getLists() throws FacebookException {
		return callMethod( FacebookMethod.FRIENDS_GET_LISTS );
	}

	/**
	 * Sets several property values for an application. The properties available are analogous to the ones editable via the Facebook Developer application. A session is
	 * not required to use this method.
	 * 
	 * @param properties
	 *            an ApplicationPropertySet that is translated into a single JSON String.
	 * @return a boolean indicating whether the properties were successfully set
	 */
	public boolean admin_setAppProperties( ApplicationPropertySet properties ) throws FacebookException {
		if ( _isDesktop ) {
			// this method cannot be called from a desktop app
			throw new FacebookException( ErrorCode.GEN_PERMISSIONS_ERROR, "Desktop applications cannot use 'admin_setAppProperties'" );
		}
		if ( null == properties || properties.isEmpty() ) {
			throw new IllegalArgumentException( "expecting a non-empty set of application properties" );
		}
		return extractBoolean( callMethod( FacebookMethod.ADMIN_SET_APP_PROPERTIES, newPair( "properties", properties.toJsonString() ) ) );
	}

	/**
	 * Gets property values previously set for an application on either the Facebook Developer application or the with the <code>admin.setAppProperties</code> call. A
	 * session is not required to use this method.
	 * 
	 * @param properties
	 *            an enumeration of the properties to get
	 * @return an ApplicationPropertySet
	 * @see ApplicationProperty
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Admin.getAppProperties"> Developers Wiki: Admin.getAppProperties</a>
	 */
	public ApplicationPropertySet admin_getAppPropertiesAsSet( EnumSet<ApplicationProperty> properties ) throws FacebookException {
		String propJson = admin_getAppPropertiesAsString( properties );
		return new ApplicationPropertySet( propJson );
	}

	public void beginBatch() {
		batchMode = true;
		queries = new ArrayList<BatchQuery>();
	}

	protected String encodeMethods( List<BatchQuery> queries ) throws FacebookException {
		JSONArray result = new JSONArray();
		for ( BatchQuery query : queries ) {
			if ( query.getMethod().takesFile() ) {
				throw new FacebookException( ErrorCode.GEN_INVALID_PARAMETER, "File upload API calls cannot be batched:  " + query.getMethod().methodName() );
			}
			result.put( delimit( query.getParams().entrySet(), "&", "=", true ) );
		}
		return result.toString();
	}

	public T batch_run( String methods, boolean serial ) throws FacebookException {
		if ( !serial ) {
			return callMethod( FacebookMethod.BATCH_RUN, newPair( "method_feed", methods ) );
		} else {
			return callMethod( FacebookMethod.BATCH_RUN, newPair( "method_feed", methods ), newPair( "serial_only", "1" ) );
		}
	}

	public T application_getPublicInfo( Long applicationId, String applicationKey, String applicationCanvas ) throws FacebookException {
		Pair<String,CharSequence> pair = null;
		if ( ( applicationId != null ) && ( applicationId > 0 ) ) {
			pair = newPair( "application_id", applicationId );
		} else if ( ( applicationKey != null ) && ( !"".equals( applicationKey ) ) ) {
			pair = newPair( "application_api_key", applicationKey );
		} else if ( ( applicationCanvas != null ) && ( !"".equals( applicationCanvas ) ) ) {
			pair = newPair( "application_canvas_name", applicationCanvas );
		} else {
			// we need at least one of them to be valid
			throw new FacebookException( ErrorCode.GEN_INVALID_PARAMETER, "You must specify at least/most one of {applicationId, applicationKey, applicationCanvas}" );
		}
		return callMethod( FacebookMethod.APPLICATION_GET_PUBLIC_INFO, pair );
	}

	public T application_getPublicInfoById( Long applicationId ) throws FacebookException {
		return application_getPublicInfo( applicationId, null, null );
	}

	public T application_getPublicInfoByApiKey( String applicationKey ) throws FacebookException {
		return application_getPublicInfo( null, applicationKey, null );
	}

	public T application_getPublicInfoByCanvasName( String applicationCanvas ) throws FacebookException {
		return application_getPublicInfo( null, null, applicationCanvas );
	}

	public int admin_getAllocation( String allocationType ) throws FacebookException {
		return extractInt( callMethod( FacebookMethod.ADMIN_GET_ALLOCATION, newPair( "integration_point_name", allocationType ) ) );
	}

	public int admin_getAllocation( AllocationType allocationType ) throws FacebookException {
		return admin_getAllocation( allocationType.getName() );
	}

	@Deprecated
	public int admin_getNotificationAllocation() throws FacebookException {
		return admin_getAllocation( "notifications_per_day" );
	}

	@Deprecated
	public int admin_getRequestAllocation() throws FacebookException {
		return admin_getAllocation( "requests_per_day" );
	}

	@Deprecated
	public T admin_getDailyMetrics( Set<Metric> metrics, Date start, Date end ) throws FacebookException {
		return admin_getDailyMetrics( metrics, start.getTime(), end.getTime() );
	}

	@Deprecated
	public T admin_getDailyMetrics( Set<Metric> metrics, long start, long end ) throws FacebookException {
		int size = 2 + ( ( metrics != null ) ? metrics.size() : 0 );
		List<Pair<String,CharSequence>> params = new ArrayList<Pair<String,CharSequence>>( size );
		if ( metrics != null ) {
			metrics.remove( Metric.ACTIVE_USERS );
			if ( !metrics.isEmpty() ) {
				JSONArray metricsJson = new JSONArray();
				for ( Metric metric : metrics ) {
					metricsJson.put( metric.getName() );
				}
				params.add( newPair( "metrics", metricsJson ) );
			}
		}
		params.add( newPair( "start_date", ( start / 1000 ) ) );
		params.add( newPair( "end_date", ( end / 1000 ) ) );
		return callMethod( FacebookMethod.ADMIN_GET_DAILY_METRICS, params );
	}

	public T permissions_checkGrantedApiAccess( String apiKey ) throws FacebookException {
		return callMethod( FacebookMethod.PERM_CHECK_GRANTED_API_ACCESS, newPair( "permissions_apikey", apiKey ) );
	}

	public T permissions_checkAvailableApiAccess( String apiKey ) throws FacebookException {
		return callMethod( FacebookMethod.PERM_CHECK_AVAILABLE_API_ACCESS, newPair( "permissions_apikey", apiKey ) );
	}

	public boolean permissions_grantApiAccess( String apiKey, Set<FacebookMethod> methods ) throws FacebookException {
		List<Pair<String,CharSequence>> params = new ArrayList<Pair<String,CharSequence>>();
		if ( ( methods != null ) && ( !methods.isEmpty() ) ) {
			JSONArray methodsJson = new JSONArray();
			for ( FacebookMethod method : methods ) {
				methodsJson.put( method.methodName() );
			}
			params.add( newPair( "method_arr", methodsJson ) );
		}
		params.add( newPair( "permissions_apikey", apiKey ) );
		return extractBoolean( callMethod( FacebookMethod.PERM_GRANT_API_ACCESS, params ) );
	}

	public boolean permissions_grantFullApiAccess( String apiKey ) throws FacebookException {
		return permissions_grantApiAccess( apiKey, null );
	}

	public boolean permissions_revokeApiAccess( String apiKey ) throws FacebookException {
		return extractBoolean( callMethod( FacebookMethod.PERM_REVOKE_API_ACCESS, newPair( "permissions_apikey", apiKey ) ) );
	}

	public String auth_promoteSession() throws FacebookException {
		return extractString( callMethod( FacebookMethod.AUTH_PROMOTE_SESSION ) );
	}

	public boolean auth_revokeAuthorization() throws FacebookException {
		return extractBoolean( callMethod( FacebookMethod.AUTH_REVOKE_AUTHORIZATION ) );
	}

	public boolean auth_expireSession() throws FacebookException {
		return extractBoolean( callMethod( FacebookMethod.AUTH_EXPIRE_SESSION ) );
	}

	public Long marketplace_createListing( Long listingId, boolean showOnProfile, String attributes, Long userId ) throws FacebookException {
		if ( listingId == null ) {
			listingId = 0l;
		}
		MarketListing test = new MarketListing( attributes );
		if ( !test.verify() ) {
			throw new FacebookException( ErrorCode.GEN_INVALID_PARAMETER, "The specified listing is invalid!" );
		}
		List<Pair<String,CharSequence>> params = new ArrayList<Pair<String,CharSequence>>( 4 );
		params.add( newPair( "listing_id", listingId ) );
		if ( showOnProfile ) {
			params.add( newPair( "show_on_profile", "true" ) );
		}
		params.add( newPair( "listing_attrs", attributes ) );
		params.add( newPair( "uid", listingId ) );
		return extractLong( callMethod( FacebookMethod.MARKET_CREATE_LISTING_NOSESSION, params ) );
	}

	public Long marketplace_createListing( Long listingId, boolean showOnProfile, MarketListing listing, Long userId ) throws FacebookException {
		return marketplace_createListing( listingId, showOnProfile, listing.getAttribs(), userId );
	}

	public Long marketplace_createListing( boolean showOnProfile, MarketListing listing, Long userId ) throws FacebookException {
		return marketplace_createListing( 0l, showOnProfile, listing.getAttribs(), userId );
	}

	public boolean marketplace_removeListing( Long listingId, Long userId ) throws FacebookException {
		return marketplace_removeListing( listingId, MarketListingStatus.DEFAULT, userId );
	}

	public boolean marketplace_removeListing( Long listingId, MarketListingStatus status, Long userId ) throws FacebookException {
		if ( status == null ) {
			status = MarketListingStatus.DEFAULT;
		}
		if ( listingId == null ) {
			return false;
		}
		return extractBoolean( callMethod( FacebookMethod.MARKET_REMOVE_LISTING_NOSESSION, newPair( "listing_id", listingId ), newPair( "status", status.getName() ),
				newPair( "uid", userId ) ) );
	}

	@Deprecated
	public boolean users_isAppAdded( Long userId ) throws FacebookException {
		return extractBoolean( callMethod( FacebookMethod.USERS_IS_APP_ADDED_NOSESSION, newPair( "uid", userId ) ) );
	}

	public boolean users_setStatus( String status, Long userId ) throws FacebookException {
		return users_setStatus( status, false, userId );
	}

	public boolean users_setStatus( String newStatus, boolean clear, Long userId ) throws FacebookException {
		return users_setStatus( newStatus, clear, false, userId );
	}

	public boolean users_setStatus( String newStatus, boolean clear, boolean statusIncludesVerb, Long userId ) throws FacebookException {
		Collection<Pair<String,CharSequence>> params = new ArrayList<Pair<String,CharSequence>>();
		if ( newStatus != null ) {
			params.add( newPair( "status", newStatus ) );
		}
		if ( clear ) {
			params.add( newPair( "clear", "true" ) );
		}
		if ( statusIncludesVerb ) {
			params.add( newPair( "status_includes_verb", "true" ) );
		}
		params.add( newPair( "uid", userId ) );
		return extractBoolean( callMethod( FacebookMethod.USERS_SET_STATUS_NOSESSION, params ) );
	}

	public T feed_getRegisteredTemplateBundleByID( Long id ) throws FacebookException {
		return callMethod( FacebookMethod.FEED_GET_TEMPLATE_BY_ID, newPair( "template_bundle_id", id ) );
	}

	public T feed_getRegisteredTemplateBundles() throws FacebookException {
		return callMethod( FacebookMethod.FEED_GET_TEMPLATES );
	}

	public Boolean feed_publishUserAction( Long bundleId ) throws FacebookException {
		return feed_publishUserAction( bundleId, null, null, null );
	}

	public Boolean feed_publishUserAction( Long bundleId, Map<String,String> templateData, List<Long> targetIds, String bodyGeneral ) throws FacebookException {
		return feed_publishUserAction( bundleId, templateData, null, targetIds, bodyGeneral, 0 );
	}

	public Long feed_registerTemplateBundle( String template ) throws FacebookException {
		List<String> temp = new ArrayList<String>();
		temp.add( template );
		return feed_registerTemplateBundle( temp );
	}

	public Long feed_registerTemplateBundle( Collection<String> templates ) throws FacebookException {
		return feed_registerTemplateBundle( templates, null, null );
	}

	public Long feed_registerTemplateBundle( Collection<String> templates, Collection<BundleStoryTemplate> shortTemplates, BundleStoryTemplate longTemplate )
			throws FacebookException {
		return feed_registerTemplateBundle( templates, shortTemplates, longTemplate, null );
	}

	public Long feed_registerTemplateBundle( Collection<String> templates, Collection<BundleStoryTemplate> shortTemplates, BundleStoryTemplate longTemplate,
			List<BundleActionLink> actionLinks ) throws FacebookException {
		Collection<Pair<String,CharSequence>> params = new ArrayList<Pair<String,CharSequence>>();
		JSONArray templateArray = new JSONArray();
		for ( String template : templates ) {
			templateArray.put( template );
		}
		params.add( newPair( "one_line_story_templates", templateArray ) );
		if ( shortTemplates != null && !shortTemplates.isEmpty() ) {
			JSONArray shortArray = new JSONArray();
			for ( BundleStoryTemplate template : shortTemplates ) {
				shortArray.put( template.toJson() );
			}
			params.add( newPair( "short_story_templates", shortArray ) );
		}
		if ( longTemplate != null ) {
			params.add( newPair( "full_story_template", longTemplate.toJsonString() ) );
		}

		if ( actionLinks != null && !actionLinks.isEmpty() ) {
			JSONArray actionLinkArray = new JSONArray();
			for ( BundleActionLink actionLink : actionLinks ) {
				actionLinkArray.put( actionLink.toJson() );
			}
			params.add( newPair( "action_links", actionLinkArray ) );
		}

		return extractLong( callMethod( FacebookMethod.FEED_REGISTER_TEMPLATE, params ) );
	}

	@Deprecated
	public Long feed_registerTemplateBundle( String template, String shortTemplate, String longTemplate ) throws FacebookException {
		List<String> templates = new ArrayList<String>();
		templates.add( template );
		return feed_registerTemplateBundle( templates, null, null );
	}

	public T profile_getFBML() throws FacebookException {
		return callMethod( FacebookMethod.PROFILE_GET_FBML );
	}

	public T profile_getFBML( Long userId ) throws FacebookException {
		if ( userId != null ) {
			return callMethod( FacebookMethod.PROFILE_GET_FBML_NOSESSION, newPair( "uid", userId ) );
		} else {
			return callMethod( FacebookMethod.PROFILE_GET_FBML );
		}
	}

	public T profile_getFBML( int type ) throws FacebookException {
		return callMethod( FacebookMethod.PROFILE_GET_FBML, newPair( "type", type ) );
	}

	public T profile_getFBML( int type, Long userId ) throws FacebookException {
		if ( userId != null ) {
			return callMethod( FacebookMethod.PROFILE_GET_FBML_NOSESSION, newPair( "type", type ), newPair( "uid", userId ) );
		} else {
			return callMethod( FacebookMethod.PROFILE_GET_FBML, newPair( "type", type ) );
		}
	}

	public T profile_getInfo( Long userId ) throws FacebookException {
		return callMethod( FacebookMethod.PROFILE_GET_INFO, newPair( "uid", userId ) );
	}

	public T profile_getInfoOptions( String field ) throws FacebookException {
		return callMethod( FacebookMethod.PROFILE_GET_INFO_OPTIONS, newPair( "field", field ) );
	}

	public void profile_setInfo( Long userId, String title, boolean textOnly, List<ProfileInfoField> fields ) throws FacebookException {
		Collection<Pair<String,CharSequence>> params = new ArrayList<Pair<String,CharSequence>>();
		JSONArray json = new JSONArray();
		params.add( newPair( "uid", userId ) );
		params.add( newPair( "title", title ) );
		if ( textOnly ) {
			params.add( newPair( "type", "1" ) );
		} else {
			params.add( newPair( "type", "5" ) );
		}
		for ( ProfileInfoField field : fields ) {
			try {
				JSONObject innerJSON = new JSONObject();
				JSONArray fieldItems = new JSONArray();
				innerJSON.put( "field", field.getFieldName() );
				for ( ProfileFieldItem item : field.getItems() ) {
					JSONObject itemJSON = new JSONObject();
					for ( String key : item.getMap().keySet() ) {
						itemJSON.put( key, item.getMap().get( key ) );
					}
					fieldItems.put( itemJSON );
				}

				innerJSON.put( "items", fieldItems );
				json.put( innerJSON );
			}
			catch ( Exception ex ) {
				throw runtimeException( ex );
			}
		}
		params.add( newPair( "info_fields", json ) );
		callMethod( FacebookMethod.PROFILE_SET_INFO, params );
	}

	public void profile_setInfoOptions( ProfileInfoField field ) throws FacebookException {
		JSONArray json = new JSONArray();
		for ( ProfileFieldItem item : field.getItems() ) {
			JSONObject itemJSON = new JSONObject();
			for ( String key : item.getMap().keySet() ) {
				try {
					itemJSON.put( key, item.getMap().get( key ) );
				}
				catch ( Exception ex ) {
					throw runtimeException( ex );
				}
			}
			json.put( itemJSON );
		}
		Collection<Pair<String,CharSequence>> params = new ArrayList<Pair<String,CharSequence>>( 2 );
		addParam( "field", field.getFieldName(), params );
		addParam( "options", json.toString(), params );
		callMethod( FacebookMethod.PROFILE_SET_INFO_OPTIONS, params );
	}

	public boolean profile_setFBML( CharSequence profileFbmlMarkup, CharSequence profileActionFbmlMarkup ) throws FacebookException {
		return profile_setFBML( null, toString( profileFbmlMarkup ), toString( profileActionFbmlMarkup ), null, null );
	}

	public boolean profile_setFBML( CharSequence profileFbmlMarkup, CharSequence profileActionFbmlMarkup, Long profileId ) throws FacebookException {
		return profile_setFBML( profileId, toString( profileFbmlMarkup ), toString( profileActionFbmlMarkup ), null, null );
	}

	public boolean profile_setFBML( CharSequence profileFbmlMarkup, CharSequence profileActionFbmlMarkup, CharSequence mobileFbmlMarkup ) throws FacebookException {
		return profile_setFBML( null, toString( profileFbmlMarkup ), toString( profileActionFbmlMarkup ), toString( mobileFbmlMarkup ), null );
	}

	public boolean profile_setFBML( CharSequence profileFbmlMarkup, CharSequence profileActionFbmlMarkup, CharSequence mobileFbmlMarkup, Long profileId )
			throws FacebookException {
		return profile_setFBML( profileId, toString( profileFbmlMarkup ), toString( profileActionFbmlMarkup ), toString( mobileFbmlMarkup ), null );
	}

	public boolean profile_setMobileFBML( CharSequence fbmlMarkup ) throws FacebookException {
		return profile_setFBML( null, null, null, toString( fbmlMarkup ), null );
	}

	public boolean profile_setMobileFBML( CharSequence fbmlMarkup, Long profileId ) throws FacebookException {
		return profile_setFBML( profileId, null, null, toString( fbmlMarkup ), null );
	}

	public boolean profile_setProfileActionFBML( CharSequence fbmlMarkup ) throws FacebookException {
		return profile_setFBML( null, null, toString( fbmlMarkup ), null, null );
	}

	public boolean profile_setProfileActionFBML( CharSequence fbmlMarkup, Long profileId ) throws FacebookException {
		return profile_setFBML( profileId, null, toString( fbmlMarkup ), null, null );
	}

	public boolean profile_setProfileFBML( CharSequence fbmlMarkup ) throws FacebookException {
		return profile_setFBML( null, toString( fbmlMarkup ), null, null, null );
	}

	public boolean profile_setProfileFBML( CharSequence fbmlMarkup, Long profileId ) throws FacebookException {
		return profile_setFBML( profileId, toString( fbmlMarkup ), null, null, null );
	}

	public boolean profile_setFBML( Long userId, String profileFbml, String actionFbml, String mobileFbml ) throws FacebookException {
		return profile_setFBML( userId, profileFbml, actionFbml, mobileFbml, null );
	}

	public boolean profile_setFBML( Long userId, String profileFbml, String actionFbml, String mobileFbml, String profileMain ) throws FacebookException {
		Collection<Pair<String,CharSequence>> params = new ArrayList<Pair<String,CharSequence>>( 5 );
		addParamIfNotBlank( "uid", userId, params );
		addParamIfNotBlank( "profile", profileFbml, params );
		addParamIfNotBlank( "profile_action", actionFbml, params );
		addParamIfNotBlank( "mobile_fbml", mobileFbml, params );
		addParamIfNotBlank( "profile_main", profileMain, params );
		FacebookMethod method = ( isDesktop() || userId == null ) ? FacebookMethod.PROFILE_SET_FBML : FacebookMethod.PROFILE_SET_FBML_NOSESSION;
		return extractBoolean( callMethod( method, params ) );
	}



	public void setServerUrl( String newUrl ) {
		String base = newUrl;
		if ( base.startsWith( "http" ) ) {
			base = base.substring( base.indexOf( "://" ) + 3 );
		}
		try {
			String url = "http://" + base;
			_serverUrl = new URL( url );
			setDefaultServerUrl( _serverUrl );
		}
		catch ( MalformedURLException ex ) {
			throw runtimeException( ex );
		}
	}

	public URL getDefaultServerUrl() {
		return SERVER_URL;
	}

	public void setDefaultServerUrl( URL newUrl ) {
		SERVER_URL = newUrl;
	}

	public Boolean liveMessage_send( Long recipient, String eventName, JSONObject message ) throws FacebookException {
		return extractBoolean( callMethod( FacebookMethod.LIVEMESSAGE_SEND, newPair( "recipient", recipient ), newPair( "event_name", eventName ), newPair( "message",
				message ) ) );
	}

	public T admin_getMetrics( Set<Metric> metrics, Date start, Date end, long period ) throws FacebookException {
		return admin_getMetrics( metrics, start.getTime(), end.getTime(), period );
	}

	public T admin_getMetrics( Set<Metric> metrics, long start, long end, long period ) throws FacebookException {
		List<Pair<String,CharSequence>> params = new ArrayList<Pair<String,CharSequence>>();
		if ( metrics != null && !metrics.isEmpty() ) {
			JSONArray metricsJson = new JSONArray();
			for ( Metric metric : metrics ) {
				metricsJson.put( metric.getName() );
			}
			addParam( "metrics", metricsJson, params );
		}
		addParam( "start_time", start / 1000, params );
		addParam( "end_time", end / 1000, params );
		addParam( "period", period, params );
		return callMethod( FacebookMethod.ADMIN_GET_METRICS, params );
	}

	public boolean feed_deactivateTemplateBundleByID( Long bundleId ) throws FacebookException {
		return extractBoolean( callMethod( FacebookMethod.FEED_DEACTIVATE_TEMPLATE_BUNDLE, newPair( "template_bundle_id", bundleId ) ) );
	}

	public void notifications_send( Collection<Long> recipientIds, String notification, boolean isAppToUser ) throws FacebookException {
		if ( null == notification || "".equals( notification ) ) {
			throw new FacebookException( ErrorCode.GEN_INVALID_PARAMETER, "You cannot send an empty notification!" );
		}
		Pair<String,CharSequence> type = newPair( "type", isAppToUser ? "app_to_user" : "user_to_user" );
		if ( ( recipientIds != null ) && ( !recipientIds.isEmpty() ) ) {
			callMethod( FacebookMethod.NOTIFICATIONS_SEND, newPair( "to_ids", delimit( recipientIds ) ), newPair( "notification", notification ), type );
		} else {
			callMethod( FacebookMethod.NOTIFICATIONS_SEND, newPair( "notification", notification ), type );
		}
	}

	public Boolean feed_publishUserAction( Long bundleId, Map<String,String> templateData, List<IFeedImage> images, List<Long> targetIds, String bodyGeneral,
			int storySize ) throws FacebookException {

		// validate maximum of 4 images
		if ( images != null && images.size() > 4 ) {
			throw new IllegalArgumentException( "Maximum of 4 images allowed per feed item." );
		}

		Collection<Pair<String,CharSequence>> params = new ArrayList<Pair<String,CharSequence>>();
		params.add( newPair( "template_bundle_id", bundleId ) );

		if ( targetIds != null && !targetIds.isEmpty() ) {
			params.add( newPair( "target_ids", delimit( targetIds ) ) );
		}

		if ( bodyGeneral != null && !"".equals( bodyGeneral ) ) {
			params.add( newPair( "body_general", bodyGeneral ) );
		}

		if ( storySize == 1 || storySize == 2 || storySize == 4 ) {
			params.add( newPair( "story_size", storySize ) );
		}

		JSONObject jsonTemplateData = new JSONObject();
		if ( templateData != null && !templateData.isEmpty() ) {
			for ( String key : templateData.keySet() ) {
				try {
					jsonTemplateData.put( key, templateData.get( key ) );
				}
				catch ( Exception exception ) {
					throw runtimeException( exception );
				}
			}
		}

		/*
		 * Associate images to "images" label in the form of:
		 * 
		 * "images":[{"src":"http:\/\/www.facebook.com\/images\/image1.gif", "href":"http:\/\/www.facebook.com"}, {"src":"http:\/\/www.facebook.com\/images\/image2.gif",
		 * "href":"http:\/\/www.facebook.com"}]
		 */
		if ( images != null && !images.isEmpty() ) {

			try {
				// create images array
				JSONArray jsonArray = new JSONArray();
				for ( int i = 0; i < images.size(); i++ ) {
					IFeedImage image = images.get( i );
					JSONObject jsonImage = new JSONObject();
					jsonImage.put( "src", image.getImageUrlString() );
					jsonImage.put( "href", image.getLinkUrl().toExternalForm() );
					jsonArray.put( i, jsonImage );
				}

				// associate to key label
				jsonTemplateData.put( "images", jsonArray );
			}
			catch ( Exception exception ) {
				throw runtimeException( exception );
			}
		}

		// associate to param
		if ( jsonTemplateData.length() > 0 ) {
			params.add( newPair( "template_data", jsonTemplateData ) );
		}

		return extractBoolean( callMethod( FacebookMethod.FEED_PUBLISH_USER_ACTION, params ) );
	}


	// ========== HELPERS ==========

	protected static void disconnect( HttpURLConnection conn ) {
		if ( conn != null ) {
			conn.disconnect();
		}
	}

	protected static void close( Closeable c ) {
		if ( c != null ) {
			try {
				c.close();
			}
			catch ( IOException ex ) {
				log.warn( "Trouble closing connection", ex );
			}
		}
	}

	protected static RuntimeException runtimeException( Exception ex ) {
		if ( ! ( ex instanceof RuntimeException ) ) {
			return new RuntimeException( ex );
		}
		return (RuntimeException) ex;
	}

	/**
	 * Prints out the DOM tree.
	 * 
	 * @param n
	 *            the parent node to start printing from
	 * @param prefix
	 *            string to append to output, should not be null
	 */
	protected static void printDom( Node n, String prefix, StringBuilder sb ) {
		String outString = prefix;
		if ( n.getNodeType() == Node.TEXT_NODE ) {
			outString += "'" + n.getTextContent().trim() + "'";
		} else {
			outString += n.getNodeName();
		}
		sb.append( outString );
		sb.append( "\n" );
		NodeList children = n.getChildNodes();
		int length = children.getLength();
		for ( int i = 0; i < length; i++ ) {
			printDom( children.item( i ), prefix + "  ", sb );
		}
	}

	protected static Pair<String,CharSequence> newPair( String name, Object value ) {
		return new Pair<String,CharSequence>( name, String.valueOf( value ) );
	}

	@Deprecated
	protected static Pair<String,CharSequence> newPair( String name, boolean value ) {
		return newPair10( name, value );
	}

	protected static Pair<String,CharSequence> newPair10( String name, boolean value ) {
		return newPair( name, value ? "1" : "0" );
	}

	protected static Pair<String,CharSequence> newPairTF( String name, boolean value ) {
		return newPair( name, value ? "true" : "false" );
	}

	protected static Pair<String,CharSequence> newPair( String name, CharSequence value ) {
		return new Pair<String,CharSequence>( name, value );
	}

	protected static Pair<String,CharSequence> newPair( String name, Long value ) {
		return new Pair<String,CharSequence>( name, Long.toString( value ) );
	}

	protected static Pair<String,CharSequence> newPair( String name, Integer value ) {
		return new Pair<String,CharSequence>( name, Integer.toString( value ) );
	}

	protected static boolean addParam( String name, Long value, Collection<Pair<String,CharSequence>> params ) {
		params.add( newPair( name, value ) );
		return true;
	}

	protected static boolean addParamIfNotBlank( String name, Long value, Collection<Pair<String,CharSequence>> params ) {
		if ( value != null ) {
			return addParam( name, value, params );
		}
		return false;
	}

	protected static boolean addParamSecondsIfNotBlank( String name, Date value, Collection<Pair<String,CharSequence>> params ) {
		if ( value != null ) {
			return addParam( name, value.getTime() / 1000, params );
		}
		return false;
	}

	protected static boolean addParamIfNotBlankZero( String name, Long value, Collection<Pair<String,CharSequence>> params ) {
		if ( value != null && value.longValue() != 0 ) {
			return addParam( name, value, params );
		}
		return false;
	}

	protected static boolean addParamDelimitIfNotBlankEmpty( String name, Iterable value, Collection<Pair<String,CharSequence>> params ) {
		return addParamIfNotBlank( name, delimit( value ), params );
	}

	protected static boolean addParam( String name, Object value, Collection<Pair<String,CharSequence>> params ) {
		params.add( newPair( name, value ) );
		return true;
	}

	protected static boolean addParam( String name, CharSequence value, Collection<Pair<String,CharSequence>> params ) {
		params.add( newPair( name, value ) );
		return true;
	}

	protected static boolean addParamIfNotBlank( String name, CharSequence value, Collection<Pair<String,CharSequence>> params ) {
		if ( ( value != null ) && ( !"".equals( value ) ) ) {
			params.add( newPair( name, value ) );
			return true;
		}
		return false;
	}

	protected static String toString( CharSequence cs ) {
		return cs == null ? null : cs.toString();
	}

	protected static JSONObject toJson( Map<String,String> map ) {
		return new JSONObject( map );
	}

	protected static JSONArray toJsonListOfStrings( Collection<String> list ) {
		return new JSONArray( list );
	}

	protected static JSONArray toJsonListOfMaps( Collection<Map<String,String>> listOfMaps ) {
		JSONArray out = new JSONArray();
		for ( Map<String,String> map : listOfMaps ) {
			out.put( toJson( map ) );
		}
		return out;
	}

	/**
	 * Extracts a Boolean from a result that consists of a Boolean only.
	 * 
	 * @param result
	 * @return the Boolean
	 */
	protected boolean extractBoolean( T result ) {
		if ( result == null ) {
			return false;
		}
		return 1 == extractInt( result );
	}

	/**
	 * Extracts an Long from a result that consists of an Long only.
	 * 
	 * @param result
	 * @return the Long
	 */
	protected abstract int extractInt( T result );

	/**
	 * Extracts an Long from a result that consists of a Long only.
	 * 
	 * @param result
	 * @return the Long
	 */
	protected abstract Long extractLong( T result );

	/**
	 * Extracts a URL from a result that consists of a URL only.
	 * 
	 * @param result
	 * @return the URL
	 */
	protected abstract URL extractURL( T result ) throws IOException;

	/**
	 * Extracts a String from a T consisting entirely of a String.
	 * 
	 * @param result
	 * @return the String
	 */
	protected abstract String extractString( T result );

	// ========== EVENTS ==========

	public T events_get( Long userId, Collection<Long> eventIds, Long startTime, Long endTime ) throws FacebookException {
		return events_get( userId, eventIds, startTime, endTime, null );
	}

	public T events_get( Long userId, Collection<Long> eventIds, Long startTime, Long endTime, String rsvp_status ) throws FacebookException {
		List<Pair<String,CharSequence>> params = new ArrayList<Pair<String,CharSequence>>( 4 );
		addParamIfNotBlankZero( "uid", userId, params );
		addParamDelimitIfNotBlankEmpty( "eids", eventIds, params );
		addParamIfNotBlankZero( "start_time", startTime, params );
		addParamIfNotBlankZero( "end_time", endTime, params );
		return callMethod( FacebookMethod.EVENTS_GET, params );
	}

	public T events_getMembers( Long eventId ) throws FacebookException {
		return callMethod( FacebookMethod.EVENTS_GET_MEMBERS, newPair( "eid", eventId ) );
	}

	public Long events_create( Map<String,String> event_info ) throws FacebookException {
		return extractLong( callMethod( FacebookMethod.EVENTS_GET_CREATE, newPair( "event_info", new JSONObject( event_info ) ) ) );
	}

	public boolean events_edit( Long eid, Map<String,String> event_info ) throws FacebookException {
		return extractBoolean( callMethod( FacebookMethod.EVENTS_GET_EDIT, newPair( "eid", eid ), newPair( "event_info", new JSONObject( event_info ) ) ) );
	}

	public boolean events_cancel( Long eid, String cancel_message ) throws FacebookException {
		return extractBoolean( callMethod( FacebookMethod.EVENTS_GET_CANCEL, newPair( "eid", eid ), newPair( "cancel_message", cancel_message ) ) );
	}

	public boolean events_rsvp( Long eid, String rsvp_status ) throws FacebookException {
		return extractBoolean( callMethod( FacebookMethod.EVENTS_GET_RSVP, newPair( "eid", eid ), newPair( "rsvp_status", rsvp_status ) ) );
	}


	// ========== MOBILE ==========

	public boolean sms_canSend() throws FacebookException {
		return sms_canSend( users_getLoggedInUser() );
	}

	public boolean sms_canSend( Long userId ) throws FacebookException {
		return extractBoolean( callMethod( FacebookMethod.SMS_CAN_SEND, newPair( "uid", userId ) ) );
	}

	public Integer sms_send( String message, Integer smsSessionId, boolean makeNewSession ) throws FacebookException {
		return sms_send( users_getLoggedInUser(), message, smsSessionId, makeNewSession );
	}

	public Integer sms_send( Long userId, String message, Integer smsSessionId, boolean makeNewSession ) throws FacebookException {
		if ( smsSessionId != null && smsSessionId != 0 ) {
			return extractInt( callMethod( FacebookMethod.SMS_SEND_MESSAGE, newPair( "uid", userId ), newPair( "message", message ),
					newPair( "session_id", smsSessionId ), newPair( "req_session", makeNewSession ) ) );
		}
		return extractInt( callMethod( FacebookMethod.SMS_SEND_MESSAGE, newPair( "uid", userId ), newPair( "message", message ), newPair( "req_session", makeNewSession ) ) );
	}

	public void sms_sendMessage( Long userId, CharSequence message ) throws FacebookException {
		sms_send( userId, message.toString(), null, false );
	}

	public int sms_sendMessageWithSession( Long userId, CharSequence message ) throws FacebookException {
		return sms_send( userId, message.toString(), null, true );
	}


	// ========== CONNECT ==========

	public T connect_registerUsers( Collection<Map<String,String>> accounts ) throws FacebookException {
		return callMethod( FacebookMethod.CONNECT_REGISTER_USERS, newPair( "accounts", toJsonListOfMaps( accounts ) ) );
	}

	public T connect_unregisterUsers( Collection<String> email_hashes ) throws FacebookException {
		return callMethod( FacebookMethod.CONNECT_UNREGISTER_USERS, newPair( "accounts", toJsonListOfStrings( email_hashes ) ) );
	}

	public int connect_getUnconnectedFriendsCount() throws FacebookException {
		return extractInt( callMethod( FacebookMethod.CONNECT_GET_UNCONNECTED_FRIENDS_COUNT ) );
	}

	// ========== PHOTOS ==========

	public T photos_get( Iterable<Long> photoIds ) throws FacebookException {
		return photos_get( null /* subjId */, null /* albumId */, photoIds );
	}

	public T photos_get( Long subjId, Long albumId ) throws FacebookException {
		return photos_get( subjId, albumId, null /* photoIds */);
	}

	public T photos_get( Long subjId, Iterable<Long> photoIds ) throws FacebookException {
		return photos_get( subjId, null /* albumId */, photoIds );
	}

	public T photos_get( Long subjId ) throws FacebookException {
		return photos_get( subjId, null /* albumId */, null /* photoIds */);
	}

	public T photos_get( Long subjId, Long albumId, Iterable<Long> photoIds ) throws FacebookException {
		List<Pair<String,CharSequence>> params = new ArrayList<Pair<String,CharSequence>>( 3 );
		addParamIfNotBlankZero( "subj_id", subjId, params );
		addParamIfNotBlankZero( "aid", albumId, params );
		addParamDelimitIfNotBlankEmpty( "pids", photoIds, params );
		if ( params.isEmpty() ) {
			throw new IllegalArgumentException( "At least one of photoIds, albumId, or subjId must be provided" );
		}
		return callMethod( FacebookMethod.PHOTOS_GET, params );
	}

	public T photos_getTags( Iterable<Long> photoIds ) throws FacebookException {
		return callMethod( FacebookMethod.PHOTOS_GET_TAGS, newPair( "pids", delimit( photoIds ) ) );
	}

	public boolean photos_addTag( Long photoId, CharSequence tagText, Double xPct, Double yPct ) throws FacebookException {
		return photos_addTag( photoId, xPct, yPct, null, tagText );
	}

	private boolean photos_addTag( Long photoId, Double xPct, Double yPct, Long taggedUserId, CharSequence tagText ) throws FacebookException {
		assert ( null != photoId && !photoId.equals( 0 ) );
		assert ( null != taggedUserId || null != tagText );
		assert ( null != xPct && xPct >= 0 && xPct <= 100 );
		assert ( null != yPct && yPct >= 0 && yPct <= 100 );
		Pair<String,CharSequence> tagData;
		if ( taggedUserId != null ) {
			tagData = newPair( "tag_uid", taggedUserId );
		} else {
			tagData = newPair( "tag_text", tagText );
		}
		T d = callMethod( FacebookMethod.PHOTOS_ADD_TAG, newPair( "pid", photoId ), tagData, newPair( "x", xPct ), newPair( "y", yPct ) );
		return extractBoolean( d );
	}

	public T photos_createAlbum( String albumName ) throws FacebookException {
		return photos_createAlbum( albumName, null /* description */, null /* location */);
	}

	public boolean photos_addTag( Long photoId, Long taggedUserId, Double xPct, Double yPct ) throws FacebookException {
		return photos_addTag( photoId, xPct, yPct, taggedUserId, null );
	}

	public T photos_addTags( Long photoId, Iterable<PhotoTag> tags ) throws FacebookException {
		JSONArray jsonTags = new JSONArray();
		for ( PhotoTag tag : tags ) {
			jsonTags.put( tag.jsonify() );
		}
		return callMethod( FacebookMethod.PHOTOS_ADD_TAG, newPair( "pid", photoId ), newPair( "tags", jsonTags ) );
	}

	public T photos_createAlbum( String name, String description, String location ) throws FacebookException {
		assert ( null != name && !"".equals( name ) );
		List<Pair<String,CharSequence>> params = new ArrayList<Pair<String,CharSequence>>( 3 );
		params.add( newPair( "name", name ) );
		if ( null != description ) {
			params.add( newPair( "description", description ) );
		}
		if ( null != location ) {
			params.add( newPair( "location", location ) );
		}
		return callMethod( FacebookMethod.PHOTOS_CREATE_ALBUM, params );
	}

	public T photos_getAlbums( Iterable<Long> albumIds ) throws FacebookException {
		return photos_getAlbums( null /* userId */, albumIds );
	}

	public T photos_getAlbums( Long userId ) throws FacebookException {
		return photos_getAlbums( userId, null /* albumIds */);
	}

	public T photos_getAlbums( Long userId, Iterable<Long> albumIds ) throws FacebookException {
		List<Pair<String,CharSequence>> params = new ArrayList<Pair<String,CharSequence>>( 2 );
		addParamIfNotBlankZero( "uid", userId, params );
		addParamDelimitIfNotBlankEmpty( "aids", albumIds, params );
		if ( params.isEmpty() ) {
			throw new FacebookException( ErrorCode.GEN_INVALID_PARAMETER, "Atleast one of userId or albumIds is required." );
		}
		return callMethod( FacebookMethod.PHOTOS_GET_ALBUMS, params );
	}

	public T photos_getByAlbum( Long albumId, Iterable<Long> photoIds ) throws FacebookException {
		return photos_get( null /* subjId */, albumId, photoIds );
	}

	public T photos_getByAlbum( Long albumId ) throws FacebookException {
		return photos_get( null /* subjId */, albumId, null /* photoIds */);
	}

	private boolean photos_addTag( Long photoId, Double xPct, Double yPct, Long taggedUserId, CharSequence tagText, Long userId ) throws FacebookException {
		assert ( null != photoId && !photoId.equals( 0 ) );
		assert ( null != taggedUserId || null != tagText );
		assert ( null != xPct && xPct >= 0 && xPct <= 100 );
		assert ( null != yPct && yPct >= 0 && yPct <= 100 );
		List<Pair<String,CharSequence>> params = new ArrayList<Pair<String,CharSequence>>( 5 );
		if ( taggedUserId != null ) {
			params.add( newPair( "tag_uid", taggedUserId ) );
		} else {
			params.add( newPair( "tag_text", tagText ) );
		}
		params.add( newPair( "x", xPct ) );
		params.add( newPair( "y", yPct ) );
		params.add( newPair( "pid", photoId ) );
		params.add( newPair( "owner_uid", userId ) );
		return extractBoolean( callMethod( FacebookMethod.PHOTOS_ADD_TAG_NOSESSION, params ) );
	}

	public boolean photos_addTag( Long photoId, Long taggedUserId, Double pct, Double pct2, Long userId ) throws FacebookException {
		return photos_addTag( photoId, pct, pct2, taggedUserId, null, userId );
	}

	public boolean photos_addTag( Long photoId, CharSequence tagText, Double pct, Double pct2, Long userId ) throws FacebookException {
		return photos_addTag( photoId, pct, pct2, null, tagText );
	}

	public T photos_createAlbum( String albumName, Long userId ) throws FacebookException {
		return photos_createAlbum( albumName, null, null, userId );
	}

	public T photos_createAlbum( String name, String description, String location, Long userId ) throws FacebookException {
		assert ( null != name && !"".equals( name ) );
		List<Pair<String,CharSequence>> params = new ArrayList<Pair<String,CharSequence>>( 4 );
		params.add( newPair( "name", name ) );
		if ( null != description ) {
			params.add( newPair( "description", description ) );
		}
		if ( null != location ) {
			params.add( newPair( "location", location ) );
		}
		params.add( newPair( "uid", userId ) );
		return callMethod( FacebookMethod.PHOTOS_CREATE_ALBUM_NOSESSION, params );
	}

	public T photos_addTags( Long photoId, Iterable<PhotoTag> tags, Long userId ) throws FacebookException {
		String tagStr = null;
		try {
			JSONArray jsonTags = new JSONArray();
			for ( PhotoTag tag : tags ) {
				jsonTags.put( tag.jsonify() );
			}
			tagStr = jsonTags.toString();
		}
		catch ( Exception ex ) {
			throw runtimeException( ex );
		}
		return callMethod( FacebookMethod.PHOTOS_ADD_TAG_NOSESSION, newPair( "pid", photoId ), newPair( "tags", tagStr ), newPair( "uid", userId ) );
	}

	public T photos_upload( File photo ) throws FacebookException {
		return photos_upload( photo, null /* caption */, null /* albumId */);
	}

	public T photos_upload( File photo, String caption ) throws FacebookException {
		return photos_upload( photo, caption, null /* albumId */);
	}

	public T photos_upload( File photo, Long albumId ) throws FacebookException {
		return photos_upload( photo, null /* caption */, albumId );
	}

	public T photos_upload( File photo, String caption, Long albumId ) throws FacebookException {
		return photos_upload( null, photo, caption, albumId );
	}

	public T photos_upload( Long userId, File photo ) throws FacebookException {
		return photos_upload( userId, photo, null, null );
	}

	public T photos_upload( Long userId, File photo, String caption ) throws FacebookException {
		return photos_upload( userId, photo, caption, null );
	}

	public T photos_upload( Long userId, File photo, Long albumId ) throws FacebookException {
		return photos_upload( userId, photo, null, albumId );
	}

	public T photos_upload( Long userId, File photo, String caption, Long albumId ) throws FacebookException {
		try {
			FileInputStream fileInputStream = new FileInputStream( photo );
			BufferedInputStream fileStream = new BufferedInputStream( fileInputStream );
			try {
				return photos_upload( userId, caption, albumId, photo.getName(), fileStream );
			}
			finally {
				close( fileStream );
				close( fileInputStream );
			}
		}
		catch ( IOException ex ) {
			throw runtimeException( ex );
		}
	}

	public T photos_upload( Long userId, String caption, Long albumId, String fileName, InputStream fileStream ) throws FacebookException {
		List<Pair<String,CharSequence>> params = new ArrayList<Pair<String,CharSequence>>( 3 );
		addParamIfNotBlankZero( "aid", albumId, params );
		addParamIfNotBlank( "caption", caption, params );
		boolean uid = addParamIfNotBlankZero( "uid", userId, params );
		FacebookMethod method = uid ? FacebookMethod.PHOTOS_UPLOAD_NOSESSION : FacebookMethod.PHOTOS_UPLOAD;
		return callMethod( method, params, fileName, fileStream );
	}

	// ========== SEND EMAIL ==========

	public T notifications_sendEmail( Collection<Long> recipients, CharSequence subject, CharSequence text, CharSequence fbml ) throws FacebookException {
		// this method requires a session only if we're dealing with a desktop app
		FacebookMethod method = isDesktop() ? FacebookMethod.NOTIFICATIONS_SEND_EMAIL_SESSION : FacebookMethod.NOTIFICATIONS_SEND_EMAIL_NOSESSION;
		List<Pair<String,CharSequence>> params = new ArrayList<Pair<String,CharSequence>>( 4 );
		addParam( "recipients", delimit( recipients ), params );
		addParam( "subject", subject, params );
		addParamIfNotBlank( "text", text, params );
		addParamIfNotBlank( "fbml", fbml, params );
		return callMethod( method, params );
	}

	public T notifications_sendFbmlEmail( Collection<Long> recipients, String subject, String fbml ) throws FacebookException {
		return notifications_sendEmail( recipients, subject, null, fbml );
	}

	public T notifications_sendTextEmail( Collection<Long> recipients, String subject, String email ) throws FacebookException {
		return notifications_sendEmail( recipients, subject, email, null );
	}

	public T notifications_sendEmailToCurrentUser( String subject, String email, String fbml ) throws FacebookException {
		return notifications_sendEmail( Arrays.asList( users_getLoggedInUser() ), subject, email, fbml );
	}

	public T notifications_sendFbmlEmailToCurrentUser( String subject, String fbml ) throws FacebookException {
		return notifications_sendEmailToCurrentUser( subject, null, fbml );
	}

	public T notifications_sendTextEmailToCurrentUser( String subject, String email ) throws FacebookException {
		return notifications_sendEmailToCurrentUser( subject, email, null );
	}

	@Deprecated
	public String notifications_sendEmailStr( Collection<Long> recipients, CharSequence subject, CharSequence fbml, CharSequence text ) throws FacebookException {
		return extractString( notifications_sendEmail( recipients, subject, text, fbml ) );
	}

	@Deprecated
	public String notifications_sendEmail( Collection<Long> recipients, CharSequence subject, CharSequence fbml ) throws FacebookException {
		return notifications_sendEmailStr( recipients, subject, fbml, null );
	}

	@Deprecated
	public String notifications_sendEmailPlain( Collection<Long> recipients, CharSequence subject, CharSequence text ) throws FacebookException {
		return notifications_sendEmailStr( recipients, subject, null, text );
	}

}
