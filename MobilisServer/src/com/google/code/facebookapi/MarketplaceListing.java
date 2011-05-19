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

import java.util.Map;

import org.json.JSONObject;

/**
 * Facebook foolishly decided to copy this project's idea of having a utility class for creating marketplace listings, without bothering to implemenent a solution that
 * was compatible with the pre-existing version. The open-source project got here first, and Facebook should have respected that, but they didn't. Hence we have a useless
 * extra class so that people who are using the official API can still easily migrate to this project.<br />
 * <br />
 * This class is only provided to preserve drop-in compatibility with the latest "official" version of the Facebook API, and generally should not be used in other cases.
 * It is important to avoid use of this class, as that will help to discourage Facebook from repeating their actions in the future.<br />
 * <br />
 * 
 * @deprecated provided for legacy support only. Please use MarketListing instead.
 */
@Deprecated
public class MarketplaceListing extends MarketListing {

	/**
	 * Constructor.
	 * 
	 * @param category
	 * @param subCategory
	 * @param title
	 * @param description
	 * 
	 * @deprecated provided for legacy support only. Please use MarketListing instead.
	 */
	@Deprecated
	public MarketplaceListing( String category, String subCategory, String title, String description ) {
		super( title, description, null, null );
		this.setAttribute( "category", category );
		this.setAttribute( "subcategory", subCategory );
	}

	/**
	 * Constructor.
	 * 
	 * @param category
	 * @param subCategory
	 * @param title
	 * @param description
	 * @param extraAttributes
	 * 
	 * @deprecated provided for legacy support only. Please use MarketListing instead.
	 */
	@Deprecated
	public MarketplaceListing( String category, String subCategory, String title, String description, Map<CharSequence,CharSequence> extraAttributes ) {
		this( category, subCategory, title, description );
		if ( null != extraAttributes ) {
			for ( CharSequence key : extraAttributes.keySet() ) {
				super.setAttribute( key.toString(), extraAttributes.get( key ).toString() );
			}
		}
	}

	/**
	 * Does the same thing as 'setAttribute'.
	 * 
	 * @param attr
	 * @param value
	 * 
	 * @deprecated provided for legacy support only. Please use MarketListing instead.
	 */
	@Deprecated
	public void putAttribute( CharSequence attr, CharSequence value ) {
		super.setAttribute( attr.toString(), value.toString() );
	}

	/**
	 * Return a JSON representation of this object
	 * 
	 * @return JSONObject
	 * 
	 * @deprecated provided for legacy support only. Please use MarketListing instead.
	 */
	@Deprecated
	public JSONObject jsonify() {
		return super.attribs;
	}
}
