/*
 * Copyright 2007, BigTribe Corporation. All rights reserved.
 *
 * This software is an unpublished work subject to a confidentiality agreement
 * and protected by copyright and trade secret law.  Unauthorized copying,
 * redistribution or other use of this work is prohibited.  All copies must
 * retain this copyright notice.  Any use or exploitation of this work without
 * authorization could subject the perpetrator to criminal and civil liability.
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

/**
 * This interface provides backwards compatibility between Pair<URL, URL> and IFeedImage instances used in feed.* API calls, so that code that was written using the
 * former method is not forced to be rewritten in order to conform to the latter.
 * 
 * @param <N>
 *            The type of the first object in the pair.
 * @param <V>
 *            The type of the second object in the pair.
 */
public interface IPair<N,V> {

	public void setFirst( N first );

	public N getFirst();

	public void setSecond( V second );

	public V getSecond();

}
