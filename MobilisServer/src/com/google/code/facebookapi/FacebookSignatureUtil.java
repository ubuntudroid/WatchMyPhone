/*
 +---------------------------------------------------------------------------+
 | Facebook Development Platform Java Client                                 |
 +---------------------------------------------------------------------------+
 | Copyright (c) 2007 Facebook, Inc.                                         |
 | All rights reserved.                                                      |
 |                                                                           |
 | Redistribution and use in source and binary forms, with or without        |
 | modification, are permitted provided that the following conditions        |
 | are met:                                                                  |
 |                                                                           |
 | 1. Redistributions of source code must retain the above copyright         |
 |    notice, this list of conditions and the following disclaimer.          |
 | 2. Redistributions in binary form must reproduce the above copyright      |
 |    notice, this list of conditions and the following disclaimer in the    |
 |    documentation and/or other materials provided with the distribution.   |
 |                                                                           |
 | THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR      |
 | IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES |
 | OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.   |
 | IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,          |
 | INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT  |
 | NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, |
 | DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY     |
 | THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT       |
 | (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF  |
 | THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.         |
 +---------------------------------------------------------------------------+
 | For help with this library, contact developers-help@facebook.com          |
 +---------------------------------------------------------------------------+
 */
package com.google.code.facebookapi;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.CRC32;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility for managing Facebook-specific parameters, specifically those related to session/login aspects.
 */
public final class FacebookSignatureUtil {

	/**
	 * Compares two key=value style keys, only performing the comparison up to the first "=".
	 */
	private static final Comparator<String> KEY_COMPARATOR = new Comparator<String>() {
		public int compare( String s1, String s2 ) {
			int minLength = Math.min( s1.length(), s2.length() );
			for ( int i = 0; i < minLength; i++ ) {
				char c1 = s1.charAt( i );
				char c2 = s2.charAt( i );
				if ( c1 == '=' ) {
					if ( c2 == '=' ) {
						return 0;
					}

					return -1;
				}

				if ( c2 == '=' ) {
					return 1;
				}

				if ( c1 != c2 ) {
					return c1 - c2;
				}
			}

			return s1.length() - s2.length();
		}
	};

	protected static Log log = LogFactory.getLog( FacebookSignatureUtil.class );

	public static Map<String,String> pulloutFbSigParams( Map<String,String[]> reqParams ) {
		Map<String,String> result = new TreeMap<String,String>();
		for ( Map.Entry<String,String[]> entry : reqParams.entrySet() ) {
			String key = entry.getKey();
			String[] values = entry.getValue();
			if ( values.length > 0 && FacebookParam.isInNamespace( key ) ) {
				result.put( key, values[0] );
			}
		}
		return result;
	}

	/**
	 * Out of the passed in <code>reqParams</code>, extracts the parameters that are in the FacebookParam namespace and returns them.
	 * 
	 * @param reqParams
	 *            A map of request parameters to their values. Values are arrays of strings, as returned by ServletRequest.getParameterMap(). Only the first element in a
	 *            given array is significant.
	 * @return a boolean indicating whether the calculated signature matched the expected signature
	 */
	public static Map<String,String> extractFacebookParamsFromArray( Map<CharSequence,CharSequence[]> reqParams ) {
		if ( null == reqParams ) {
			return null;
		}
		Map<String,String> result = new TreeMap<String,String>();
		for ( Map.Entry<CharSequence,CharSequence[]> entry : reqParams.entrySet() ) {
			String key = entry.getKey().toString();
			CharSequence[] values = entry.getValue();
			if ( values.length > 0 && FacebookParam.isInNamespace( key ) ) {
				result.put( key, toString( values[0] ) );
			}
		}
		return result;
	}

	public static String toString( CharSequence cs ) {
		if ( cs != null ) {
			return cs.toString();
		}
		return null;
	}

	/**
	 * Out of the passed in <code>reqParams</code>, extracts the parameters that are in the FacebookParam namespace and returns them.
	 * 
	 * @param reqParams
	 *            A map of request parameters to their values. Values are arrays of strings, as returned by ServletRequest.getParameterMap(). Only the first element in a
	 *            given array is significant.
	 * @return a boolean indicating whether the calculated signature matched the expected signature
	 */
	// Facebook likes to refer to everything as a CharSequence, even when referencing Objects defined by an external API that explicitly
	// specifies the use of String, and *not* CharSequence.
	public static Map<String,String> extractFacebookParamsFromStandardsCompliantArray( Map<String,String[]> reqParams ) {
		if ( null == reqParams ) {
			return null;
		}
		Map<String,String> result = new TreeMap<String,String>();
		for ( Map.Entry<String,String[]> entry : reqParams.entrySet() ) {
			String key = entry.getKey();
			if ( FacebookParam.isInNamespace( key ) ) {
				String[] value = entry.getValue();
				if ( value.length > 0 ) {
					result.put( key, value[0] );
				}
			}
		}
		return result;
	}

	/**
	 * Out of the passed in <code>reqParams</code>, extracts the parameters that are in the FacebookParam namespace and returns them.
	 * 
	 * @param reqParams
	 *            a map of request parameters to their values
	 * @return a boolean indicating whether the calculated signature matched the expected signature
	 */
	public static Map<String,CharSequence> extractFacebookNamespaceParams( Map<CharSequence,CharSequence> reqParams ) {
		if ( null == reqParams ) {
			return null;
		}
		Map<String,CharSequence> result = new TreeMap<String,CharSequence>();
		for ( Map.Entry<CharSequence,CharSequence> entry : reqParams.entrySet() ) {
			String key = entry.getKey().toString();
			if ( FacebookParam.isInNamespace( key ) ) {
				result.put( key, entry.getValue() );
			}
		}
		return result;
	}

	/**
	 * Out of the passed in <code>reqParams</code>, extracts the parameters that are known FacebookParams and returns them.
	 * 
	 * @param reqParams
	 *            a map of request parameters to their values
	 * @return a map suitable for being passed to verify signature
	 */
	public static EnumMap<FacebookParam,CharSequence> extractFacebookParams( Map<CharSequence,CharSequence> reqParams ) {
		if ( null == reqParams )
			return null;

		EnumMap<FacebookParam,CharSequence> result = new EnumMap<FacebookParam,CharSequence>( FacebookParam.class );
		for ( Map.Entry<CharSequence,CharSequence> entry : reqParams.entrySet() ) {
			FacebookParam matchingFacebookParam = FacebookParam.get( entry.getKey().toString() );
			if ( null != matchingFacebookParam ) {
				result.put( matchingFacebookParam, entry.getValue() );
			}
		}
		return result;
	}

	/**
	 * Verifies that a signature received matches the expected value. Removes FacebookParam.SIGNATURE from params if present.
	 * 
	 * @param params
	 *            a map of parameters and their values, such as one obtained from extractFacebookParams; expected to the expected signature as the FacebookParam.SIGNATURE
	 *            parameter
	 * @param secret
	 * @return a boolean indicating whether the calculated signature matched the expected signature
	 */
	public static boolean verifySignature( EnumMap<FacebookParam,String> params, String secret ) {
		if ( null == params || params.isEmpty() )
			return false;
		CharSequence sigParam = params.remove( FacebookParam.SIGNATURE );
		return ( null == sigParam ) ? false : verifySignature( params, secret, sigParam.toString() );
	}

	/**
	 * Verifies that a signature received matches the expected value.
	 * 
	 * @param params
	 *            a map of parameters and their values, such as one obtained from extractFacebookParams
	 * @param secret
	 *            the developers 'secret' API key
	 * @param expected
	 *            the expected resulting value of computing the MD5 sum of the 'sig' params and the 'secret' key
	 * @return a boolean indicating whether the calculated signature matched the expected signature
	 */
	public static boolean verifySignature( EnumMap<FacebookParam,String> params, String secret, String expected ) {
		assert ! ( null == secret || "".equals( secret ) );
		if ( null == params || params.isEmpty() )
			return false;
		if ( null == expected || "".equals( expected ) ) {
			return false;
		}
		params.remove( FacebookParam.SIGNATURE );
		List<String> sigParams = convertFacebookParams( params.entrySet() );
		return verifySignature( sigParams, secret, expected );
	}

	/**
	 * Verifies that a signature received matches the expected value. Removes FacebookParam.SIGNATURE from params if present.
	 * 
	 * @param params
	 *            a map of parameters and their values, such as one obtained from extractFacebookNamespaceParams; expected to contain the signature as the
	 *            FacebookParam.SIGNATURE parameter
	 * @param secret
	 *            the developers 'secret' API key
	 * @return a boolean indicating whether the calculated signature matched the expected signature
	 */
	public static boolean verifySignature( Map<String,String> params, String secret ) {
		if ( null == params || params.isEmpty() ) {
			return false;
		}
		CharSequence sigParam = params.remove( FacebookParam.SIGNATURE.toString() );
		return ( null == sigParam ) ? false : verifySignature( params, secret, sigParam.toString() );
	}

	/**
	 * Verifies that a signature received matches the expected value. This method will perform any necessary conversion of the parameter map passed to it (should the map
	 * be immutable, etc.), meaning that you may safely call it without doing any manual preprocessing of the parameters first.
	 * 
	 * @param requestParams
	 *            A map of request parameters to their values, as returned by ServletRequest.getParameterMap(), for example.
	 * @param secret
	 *            the developers 'secret' API key
	 * @param expected
	 *            the expected resulting value of computing the MD5 sum of the 'sig' params and the 'secret' key
	 * 
	 * @return a boolean indicating whether the calculated signature matched the expected signature
	 */
	public static boolean autoVerifySignature( Map<String,String[]> requestParams, String secret, String expected ) {
		Map<String,String> convertedMap = extractFacebookParamsFromStandardsCompliantArray( requestParams );
		return verifySignature( convertedMap, secret, expected );
	}

	/**
	 * Verifies that a signature received matches the expected value. This method will perform any necessary conversion of the parameter map passed to it (should the map
	 * be immutable, etc.), meaning that you may safely call it without doing any manual preprocessing of the parameters first.
	 * 
	 * @param requestParams
	 *            A map of request parameters to their values, as returned by ServletRequest.getParameterMap(), for example.
	 * @param secret
	 *            the developers 'secret' API key
	 * 
	 * @return a boolean indicating whether the calculated signature matched the expected signature
	 */
	public static boolean autoVerifySignature( Map<String,String[]> requestParams, String secret ) {
		String[] expectedParms = requestParams.get( "fb_sig" );

		// Make sure we aren't missing the signature
		if ( expectedParms == null || ( expectedParms.length == 0 ) ) {
			return false;
		}

		String expected = expectedParms[0];
		return autoVerifySignature( requestParams, secret, expected );
	}

	/**
	 * Verifies that a signature received matches the expected value.
	 * 
	 * @param params
	 *            a map of parameters and their values, such as one obtained from extractFacebookNamespaceParams
	 * @param secret
	 *            the developers 'secret' API key
	 * @param expected
	 *            the expected resulting value of computing the MD5 sum of the 'sig' params and the 'secret' key
	 * @return a boolean indicating whether the calculated signature matched the expected signature
	 */
	public static boolean verifySignature( Map<String,String> params, String secret, String expected ) {
		assert ! ( null == secret || "".equals( secret ) );
		if ( null == params || params.isEmpty() )
			return false;
		if ( null == expected || "".equals( expected ) ) {
			return false;
		}
		params.remove( FacebookParam.SIGNATURE.toString() );
		List<String> sigParams = convert( params.entrySet() );
		return verifySignature( sigParams, secret, expected );
	}

	private static boolean verifySignature( List<String> sigParams, String secret, String expected ) {
		if ( null == expected || "".equals( expected ) ) {
			return false;
		}
		String signature = generateSignature( sigParams, secret );
		return expected.equals( signature );
	}

	/**
	 * Converts a Map of key-value pairs into the form expected by generateSignature
	 * 
	 * @param entries
	 *            a collection of Map.Entry's, such as can be obtained using myMap.entrySet()
	 * @return a List suitable for being passed to generateSignature
	 */
	public static List<String> convert( Collection<Map.Entry<String,String>> entries ) {
		List<String> result = new ArrayList<String>( entries.size() );
		for ( Map.Entry<String,? extends CharSequence> entry : entries ) {
			result.add( FacebookParam.stripSignaturePrefix( entry.getKey() ) + "=" + entry.getValue() );
		}
		return result;
	}

	/**
	 * Converts a Map of key-value pairs into the form expected by generateSignature
	 * 
	 * @param entries
	 *            a collection of Map.Entry's, such as can be obtained using myMap.entrySet()
	 * @return a List suitable for being passed to generateSignature
	 */
	public static List<String> convertFacebookParams( Collection<Map.Entry<FacebookParam,String>> entries ) {
		List<String> result = new ArrayList<String>( entries.size() );
		for ( Map.Entry<FacebookParam,String> entry : entries ) {
			result.add( entry.getKey().getSignatureName() + "=" + entry.getValue() );
		}
		return result;
	}

	/**
	 * Calculates the signature for the given set of params using the supplied secret
	 * 
	 * @param params
	 *            Strings of the form "key=value"
	 * @param secret
	 * @return the signature
	 */
	public static String generateSignature( List<String> params, String secret ) {
		StringBuilder buffer = new StringBuilder();
		Collections.sort( params, KEY_COMPARATOR );
		for ( String param : params ) {
			buffer.append( param );
		}
		buffer.append( secret );
		return generateMD5( buffer.toString() );
	}

	public static String generateMD5( String value ) {
		try {
			MessageDigest md = MessageDigest.getInstance( "MD5" );
			byte[] bytes;
			try {
				bytes = value.getBytes( "UTF-8" );
			}
			catch ( UnsupportedEncodingException e1 ) {
				bytes = value.getBytes();
			}
			StringBuilder result = new StringBuilder();
			for ( byte b : md.digest( bytes ) ) {
				result.append( Integer.toHexString( ( b & 0xf0 ) >>> 4 ) );
				result.append( Integer.toHexString( b & 0x0f ) );
			}
			return result.toString();
		}
		catch ( NoSuchAlgorithmException ex ) {
			throw new RuntimeException( ex );
		}
	}

	/**
	 * <ol>
	 * <li>Normalize the email address. Trim leading and trailing whitespace, and convert all characters to lowercase.</li>
	 * <li>Compute the CRC32 value for the normalized email address and use the unsigned integer representation of this value. (Note that some implementations return
	 * signed integers, in which case you will need to convert that result to an unsigned integer.)</li>
	 * <li>Compute the MD5 value for the normalized email address and use the hex representation of this value (using lowercase for A through F).</li>
	 * <li>Combine these two value with an underscore.</li>
	 * </ol>
	 * For example, the address mary@example.com converts to 4228600737_c96da02bba97aedfd26136e980ae3761.
	 * 
	 * @param email
	 * @return email_hash
	 * @see IFacebookRestClient#connect_registerUsers(Collection)
	 */
	public static String generateEmailHash( String email ) {
		email = email.trim().toLowerCase();
		CRC32 crc = new CRC32();
		crc.update( email.getBytes() );
		String md5 = generateMD5( email );
		return crc.getValue() + "_" + md5;
	}

}
