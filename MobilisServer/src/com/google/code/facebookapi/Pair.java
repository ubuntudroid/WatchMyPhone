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

import java.io.Serializable;

/**
 * Simple data structure for grouping two values together. Required by some API calls.
 * 
 * @param <N>
 *            first element in the pair.
 * @param <V>
 *            second element in the pair.
 */
public class Pair<N,V> implements IPair<N,V>, Serializable {

	/**
	 * The first element in the pair.
	 */
	// FIXME: should be private
	public N first;
	/**
	 * The second element in the pair.
	 */
	// FIXME: should be private
	public V second;

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            the first value in the pair.
	 * @param value
	 *            the second value in the pair.
	 */
	public Pair( N name, V value ) {
		this.first = name;
		this.second = value;
	}

	/**
	 * Set the first element in the pair
	 * 
	 * @param first
	 *            the object to set
	 */
	public void setFirst( N first ) {
		this.first = first;
	}

	/**
	 * @return the first object in the pair
	 */
	public N getFirst() {
		return first;
	}

	/**
	 * Set the second element in the pair
	 * 
	 * @param second
	 *            the object to set
	 */
	public void setSecond( V second ) {
		this.second = second;
	}

	/**
	 * @return the second object in the pair
	 */
	public V getSecond() {
		return second;
	}

}
