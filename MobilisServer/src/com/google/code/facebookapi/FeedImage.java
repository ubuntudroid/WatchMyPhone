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

import java.net.URL;

/**
 * A simple Pair of URLs for an image appearing in a newsfeed/minifeed story and the optional destination URL for a click on that image.
 * 
 * @see IFacebookRestClient
 */
public class FeedImage extends Pair<Object,URL> implements IFeedImage {

	/**
	 * Constructor for a feed image with no associated hyperlink.
	 * 
	 * @param image
	 *            the URL of the image to display.
	 * 
	 * @deprecated a FeedImage must include a link url.
	 */
	@Deprecated
	public FeedImage( URL image ) {
		this( image, null );
	}

	/**
	 * Constructor for a feed image with an associated hyperlink.
	 * 
	 * @param image
	 *            the URL of the image to display.
	 * @param link
	 *            the url to link to when the image is clicked.
	 */
	public FeedImage( URL image, URL link ) {
		super( image, link );
		if ( null == image || null == link ) {
			throw new IllegalArgumentException( "both image and link URLs are required" );
		}
	}

	/**
	 * @return the URL of the image to display.
	 */
	public Object getImageUrl() {
		return getFirst();
	}

	/**
	 * @return the URL of the link that will be opened when the image is clicked.
	 */
	public URL getLinkUrl() {
		return getSecond();
	}

	/**
	 * @return the String representation of the image URL
	 */
	public String getImageUrlString() {
		return getImageUrl().toString();
	}
}
