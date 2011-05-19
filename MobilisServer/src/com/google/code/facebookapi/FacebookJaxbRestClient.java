/*
 * Copyright 2007, BigTribe Corporation. All rights reserved.
 *
 * This software is an unpublished work subject to a confidentiality agreement
 * and protected by copyright and trade secret law.  Unauthorized copying,
 * redistribution or other use of this work is prohibited.  All copies must
 * retain this copyright notice.  Any use or exploitation of this work without
 * authorization could subject the perpetrator to criminal and civil liability.
 * 
 * Redistribution and use in source and binary forms, with or without        
 * modification, are permitted provided that the following conditions        
 * are met:                                                                  
 *                                                                           
 * 1. Redistributions of source code must retain the above copyright         
 *    notice, this list of conditions and the following disclaimer.          
 * 2. Redistributions in binary form must reproduce the above copyright      
 *    notice, this list of conditions and the following disclaimer in the    
 *    documentation and/or other materials provided with the distribution.   
 *
 * The information in this software is subject to change without notice
 * and should not be construed as a commitment by BigTribe Corporation.
 *
 * The above copyright notice does not indicate actual or intended publication
 * of this source code.
 *
 * $Id: bigtribetemplates.xml 5524 2006-04-06 09:40:52 -0700 (Thu, 06 Apr 2006) greening $
 */
package com.google.code.facebookapi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.code.facebookapi.schema.FacebookApiException;
import com.google.code.facebookapi.schema.FriendsGetResponse;
import com.google.code.facebookapi.schema.Listing;
import com.google.code.facebookapi.schema.MarketplaceGetListingsResponse;
import com.google.code.facebookapi.schema.MarketplaceGetSubCategoriesResponse;
import com.google.code.facebookapi.schema.MarketplaceSearchResponse;
import com.google.code.facebookapi.schema.SessionInfo;

/**
 * A FacebookRestClient that JAXB response objects. This means results from calls to the Facebook API are returned as XML and transformed into JAXB Java objects.
 */
public class FacebookJaxbRestClient extends ExtensibleClient<Object> {

	protected static Log log = LogFactory.getLog( FacebookJaxbRestClient.class );

	// used so that executeBatch can return the correct types in its list, without killing efficiency.
	private static final Map<FacebookMethod,String> RETURN_TYPES;
	static {
		RETURN_TYPES = new HashMap<FacebookMethod,String>();
		Method[] candidates = FacebookJaxbRestClient.class.getMethods();
		// this loop is inefficient, but it only executes once per JVM, so it doesn't really matter
		for ( FacebookMethod method : EnumSet.allOf( FacebookMethod.class ) ) {
			String name = method.methodName();
			name = name.substring( name.indexOf( "." ) + 1 );
			name = name.replace( ".", "_" );
			for ( Method candidate : candidates ) {
				if ( candidate.getName().equalsIgnoreCase( name ) ) {
					String typeName = candidate.getReturnType().getName().toLowerCase();
					// possible types are Document, String, Boolean, Integer, Long, void
					if ( typeName.indexOf( "object" ) != -1 ) {
						RETURN_TYPES.put( method, "default" );
					} else if ( typeName.indexOf( "string" ) != -1 ) {
						RETURN_TYPES.put( method, "string" );
					} else if ( typeName.indexOf( "bool" ) != -1 ) {
						RETURN_TYPES.put( method, "bool" );
					} else if ( typeName.indexOf( "long" ) != -1 ) {
						RETURN_TYPES.put( method, "long" );
					} else if ( typeName.indexOf( "int" ) != -1 ) {
						RETURN_TYPES.put( method, "int" );
					} else if ( ( typeName.indexOf( "applicationpropertyset" ) != -1 ) || ( typeName.indexOf( "list" ) != -1 ) || ( typeName.indexOf( "url" ) != -1 )
							|| ( typeName.indexOf( "map" ) != -1 ) || ( typeName.indexOf( "object" ) != -1 ) ) {
						// we don't autobox these for now, the user can parse them on their own
						RETURN_TYPES.put( method, "default" );
					} else {
						RETURN_TYPES.put( method, "void" );
					}
					break;
				}
			}
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param apiKey
	 *            your Facebook API key
	 * @param secret
	 *            your 'secret' Facebook key
	 */
	public FacebookJaxbRestClient( String apiKey, String secret ) {
		super( apiKey, secret );
	}

	/**
	 * Constructor.
	 * 
	 * @param apiKey
	 *            your Facebook API key
	 * @param secret
	 *            your 'secret' Facebook key
	 * @param connectionTimeout
	 *            the connection timeout to apply when making API requests to Facebook, in milliseconds
	 */
	public FacebookJaxbRestClient( String apiKey, String secret, int connectionTimeout ) {
		super( apiKey, secret, connectionTimeout );
	}

	/**
	 * Constructor.
	 * 
	 * @param apiKey
	 *            your Facebook API key
	 * @param secret
	 *            your 'secret' Facebook key
	 * @param sessionKey
	 *            the session-id to use
	 */
	public FacebookJaxbRestClient( String apiKey, String secret, String sessionKey ) {
		super( apiKey, secret, sessionKey );
	}

	/**
	 * Constructor.
	 * 
	 * @param apiKey
	 *            your Facebook API key
	 * @param secret
	 *            your 'secret' Facebook key
	 * @param sessionKey
	 *            the session-id to use
	 * @param connectionTimeout
	 *            the connection timeout to apply when making API requests to Facebook, in milliseconds
	 */
	public FacebookJaxbRestClient( String apiKey, String secret, String sessionKey, int connectionTimeout ) {
		super( apiKey, secret, sessionKey, connectionTimeout );
	}


	/**
	 * Constructor.
	 * 
	 * @param serverAddr
	 *            the URL of the Facebook API server to use
	 * @param apiKey
	 *            your Facebook API key
	 * @param secret
	 *            your 'secret' Facebook key
	 * @param sessionKey
	 *            the session-id to use
	 * 
	 * @throws MalformedURLException
	 *             if you specify an invalid URL
	 */
	public FacebookJaxbRestClient( String serverAddr, String apiKey, String secret, String sessionKey ) throws MalformedURLException {
		super( serverAddr, apiKey, secret, sessionKey );
	}

	/**
	 * Constructor.
	 * 
	 * @param serverAddr
	 *            the URL of the Facebook API server to use
	 * @param apiKey
	 *            your Facebook API key
	 * @param secret
	 *            your 'secret' Facebook key
	 * @param sessionKey
	 *            the session-id to use
	 * @param connectionTimeout
	 *            the connection timeout to apply when making API requests to Facebook, in milliseconds
	 * 
	 * @throws MalformedURLException
	 *             if you specify an invalid URL
	 */
	public FacebookJaxbRestClient( String serverAddr, String apiKey, String secret, String sessionKey, int connectionTimeout ) throws MalformedURLException {
		super( serverAddr, apiKey, secret, sessionKey, connectionTimeout );
	}

	/**
	 * Constructor.
	 * 
	 * @param serverUrl
	 *            the URL of the Facebook API server to use
	 * @param apiKey
	 *            your Facebook API key
	 * @param secret
	 *            your 'secret' Facebook key
	 * @param sessionKey
	 *            the session-id to use
	 */
	public FacebookJaxbRestClient( URL serverUrl, String apiKey, String secret, String sessionKey ) {
		super( serverUrl, apiKey, secret, sessionKey );
	}

	/**
	 * Constructor.
	 * 
	 * @param serverUrl
	 *            the URL of the Facebook API server to use
	 * @param apiKey
	 *            your Facebook API key
	 * @param secret
	 *            your 'secret' Facebook key
	 * @param sessionKey
	 *            the session-id to use
	 * @param connectionTimeout
	 *            the connection timeout to apply when making API requests to Facebook, in milliseconds
	 */
	public FacebookJaxbRestClient( URL serverUrl, String apiKey, String secret, String sessionKey, int connectionTimeout ) {
		super( serverUrl, apiKey, secret, sessionKey, connectionTimeout, -1 );
	}

	/**
	 * Constructor.
	 * 
	 * @param serverUrl
	 *            the URL of the Facebook API server to use
	 * @param apiKey
	 *            your Facebook API key
	 * @param secret
	 *            your 'secret' Facebook key
	 * @param sessionKey
	 *            the session-id to use
	 * @param connectionTimeout
	 *            the connection timeout to apply when making API requests to Facebook, in milliseconds
	 * @param readTimeout
	 *            the read timeout to apply when making API requests to Facebook, in milliseconds
	 */
	public FacebookJaxbRestClient( URL serverUrl, String apiKey, String secret, String sessionKey, int connectionTimeout, int readTimeout ) {
		super( serverUrl, apiKey, secret, sessionKey, connectionTimeout, readTimeout );
	}

	/**
	 * The response format in which results to FacebookMethod calls are returned
	 * 
	 * @return the format: either XML, JSON, or null (API default)
	 */
	public String getResponseFormat() {
		return "xml";
	}

	private String parse() {
		String xml = this.rawResponse;
		if ( ( xml == null ) || ( "".equals( xml ) ) ) {
			return null;
		}
		if ( !xml.contains( "</" ) ) {
			return null;
		}
		xml = xml.substring( 0, xml.indexOf( "</" ) );
		xml = xml.substring( xml.lastIndexOf( ">" ) + 1 );
		return xml;
	}

	/**
	 * Extracts a String from a result consisting entirely of a String.
	 * 
	 * @param val
	 * @return the String
	 */
	public String extractString( Object val ) {
		return parse();
	}

	private FriendsGetResponse cacheFriendsList;

	@Override
	public FriendsGetResponse friends_get() throws FacebookException {
		if ( batchMode ) {
			return (FriendsGetResponse) super.friends_get();
		}
		if ( cacheFriendsList == null ) {
			cacheFriendsList = (FriendsGetResponse) super.friends_get();
		}
		return cacheFriendsList;
	}

	public FriendsGetResponse getCacheFriendsList() {
		return cacheFriendsList;
	}

	public void setCacheFriendsList( List<Long> ids ) {
		cacheFriendsList = toFriendsGetResponse( ids );
	}

	public static FriendsGetResponse toFriendsGetResponse( List<Long> ids ) {
		FriendsGetResponse out = new FriendsGetResponse();
		out.setList( true );
		out.getUid().addAll( ids );
		return out;
	}

	/**
	 * Call this function to retrieve the session information after your user has logged in.
	 * 
	 * @param authToken
	 *            the token returned by auth_createToken or passed back to your callback_url.
	 */
	public String auth_getSession( String authToken ) throws FacebookException {
		JAXBElement obj = (JAXBElement) callMethod( FacebookMethod.AUTH_GET_SESSION, newPair( "auth_token", authToken ) );
		SessionInfo d = (SessionInfo) obj.getValue();
		this.cacheSessionKey = d.getSessionKey();
		this.cacheUserId = d.getUid();
		this.cacheSessionExpires = (long) d.getExpires();
		if ( this._isDesktop ) {
			this.cacheSessionSecret = d.getSecret();
		}
		return this.cacheSessionKey;
	}

	/**
	 * Parses the result of an API call from XML into JAXB Objects.
	 * 
	 * @param data
	 *            an InputStream with the results of a request to the Facebook servers
	 * @param method
	 *            the method
	 * @return a JAXB Object
	 * @throws FacebookException
	 *             if <code>data</code> represents an error
	 * @throws IOException
	 *             if <code>data</code> is not readable
	 */
	protected Object parseCallResult( InputStream data, IFacebookMethod method ) throws FacebookException {
		log.debug( "Facebook response:  " + rawResponse );
		Object res = getResponsePOJO();
		if ( res instanceof FacebookApiException ) {
			FacebookApiException error = (FacebookApiException) res;
			int errorCode = error.getErrorCode();
			String message = error.getErrorMsg();
			throw new FacebookException( errorCode, message );
		} else if ( res instanceof JAXBElement ) {
			JAXBElement jbe = (JAXBElement) res;
			if ( FacebookApiException.class.equals( jbe.getDeclaredType() ) ) {
				FacebookApiException error = (FacebookApiException) jbe.getValue();
				int errorCode = error.getErrorCode();
				String message = error.getErrorMsg();
				throw new FacebookException( errorCode, message );
			}
		}
		return res;
	}

	/**
	 * Extracts a URL from a result that consists of a URL only. For JSON, that result is simply a String.
	 * 
	 * @param url
	 * @return the URL
	 */
	protected URL extractURL( Object url ) throws IOException {
		String result = parse();
		if ( result != null ) {
			return new URL( result );
		}
		return null;
	}

	/**
	 * Extracts an Integer from a result that consists of an Integer only.
	 * 
	 * @param val
	 * @return the Integer
	 */
	protected int extractInt( Object val ) {
		try {
			return Integer.parseInt( parse() );
		}
		catch ( Exception cce ) {
			return 0;
		}
	}

	/**
	 * Extracts a Boolean from a result that consists of a Boolean only.
	 * 
	 * @param val
	 * @return the Boolean
	 */
	protected boolean extractBoolean( Object val ) {
		String result = parse();
		if ( ( "1".equals( result ) ) || ( "true".equalsIgnoreCase( result ) ) ) {
			return true;
		}
		return false;
	}

	/**
	 * Extracts a Long from a result that consists of an Long only.
	 * 
	 * @param val
	 * @return the Integer
	 */
	protected Long extractLong( Object val ) {
		try {
			return Long.parseLong( parse() );
		}
		catch ( Exception cce ) {
			return 0l;
		}
	}

	public List<Listing> marketplace_getListings( List<Long> listingIds, List<Long> uids ) throws FacebookException {
		MarketplaceGetListingsResponse resp = (MarketplaceGetListingsResponse) marketplace_getListings( listingIds, uids );
		return resp.getListing();
	}

	public List<String> marketplace_getSubCategories() throws FacebookException {
		MarketplaceGetSubCategoriesResponse resp = (MarketplaceGetSubCategoriesResponse) marketplace_getSubCategories( null );
		return resp.getMarketplaceSubcategory();
	}

	public List<Listing> marketplace_search( MarketListingCategory category, MarketListingSubcategory subcategory, String searchTerm ) throws FacebookException {
		MarketplaceSearchResponse resp = (MarketplaceSearchResponse) marketplace_search( category.getName(), subcategory.getName(), searchTerm );
		return resp.getListing();
	}

	public String admin_getAppPropertiesAsString( Collection<ApplicationProperty> properties ) throws FacebookException {
		if ( this._isDesktop ) {
			// this method cannot be called from a desktop app
			throw new FacebookException( ErrorCode.GEN_PERMISSIONS_ERROR, "Desktop applications cannot use 'admin.getAppProperties'" );
		}
		JSONArray props = new JSONArray();
		for ( ApplicationProperty property : properties ) {
			props.put( property.getName() );
		}
		callMethod( FacebookMethod.ADMIN_GET_APP_PROPERTIES, newPair( "properties", props.toString() ) );
		return extractString( null );
	}

	/**
	 * Executes a batch of queries. You define the queries to execute by calling 'beginBatch' and then invoking the desired API methods that you want to execute as part
	 * of your batch as normal. Invoking this method will then execute the API calls you made in the interim as a single batch query.
	 * 
	 * @param serial
	 *            set to true, and your batch queries will always execute serially, in the same order in which your specified them. If set to false, the Facebook API
	 *            server may execute your queries in parallel and/or out of order in order to improve performance.
	 * 
	 * @return a list containing the results of the batch execution. The list will be ordered such that the first element corresponds to the result of the first query in
	 *         the batch, and the second element corresponds to the result of the second query, and so on. The types of the objects in the list will match the type
	 *         normally returned by the API call being invoked (so calling users_getLoggedInUser as part of a batch will place a Long in the list, and calling friends_get
	 *         will place a Document in the list, etc.).
	 * 
	 * The list may be empty, it will never be null.
	 * 
	 * @throws FacebookException
	 * @throws IOException
	 */
	public List<? extends Object> executeBatch( boolean serial ) throws FacebookException {
		this.batchMode = false;
		List<Object> result = new ArrayList<Object>();
		List<BatchQuery> buffer = new ArrayList<BatchQuery>();
		while ( !this.queries.isEmpty() ) {
			buffer.add( this.queries.remove( 0 ) );
			if ( ( buffer.size() == BATCH_LIMIT ) || ( this.queries.isEmpty() ) ) {
				// we can only actually batch up to 20 at once
				batch_run( encodeMethods( buffer ), serial );
				DocumentBuilder builder;
				Document doc;
				try {
					builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					doc = builder.parse( new ByteArrayInputStream( this.rawResponse.getBytes( "UTF-8" ) ) );
				}
				catch ( ParserConfigurationException ex ) {
					throw new RuntimeException( ex );
				}
				catch ( UnsupportedEncodingException ex ) {
					throw new RuntimeException( ex );
				}
				catch ( SAXException ex ) {
					throw new RuntimeException( ex );
				}
				catch ( IOException ex ) {
					throw new RuntimeException( ex );
				}
				NodeList responses = doc.getElementsByTagName( "batch_run_response_elt" );
				for ( int count = 0; count < responses.getLength(); count++ ) {
					String response = extractNodeString( responses.item( count ) );
					try {
						this.rawResponse = response;
						Object pojo = parseCallResult( null, null );
						String type = RETURN_TYPES.get( buffer.get( count ).getMethod() );
						// possible types are document, string, bool, int, long, void
						if ( type.equals( "default" ) ) {
							result.add( pojo );
						} else if ( type.equals( "string" ) ) {
							result.add( extractString( pojo ) );
						} else if ( type.equals( "bool" ) ) {
							result.add( extractBoolean( pojo ) );
						} else if ( type.equals( "int" ) ) {
							result.add( extractInt( pojo ) );
						} else if ( type.equals( "long" ) ) {
							result.add( (long) extractLong( pojo ) );
						} else {
							// void
							result.add( null );
						}
					}
					catch ( Exception e ) {
						if ( result.size() < count + 1 ) {
							result.add( null );
						}
					}
				}
				//End for loop
				
				if( buffer.size() == BATCH_LIMIT ) {
					log.debug("Clearing buffer for the next run.");
					buffer.clear();
				} else {
					log.trace( "No need to clear buffer, this is the final iteration of the batch" );
				}
			}
		}

		return result;
	}

	public static String extractNodeString( Node d ) {
		if ( d == null ) {
			return null;
		}
		return d.getFirstChild().getTextContent();
	}
}
