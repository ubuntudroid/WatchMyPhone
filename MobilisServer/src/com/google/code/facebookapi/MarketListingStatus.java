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

/**
 * Simple enum for specifying whether or not a marketplace listing was successful or not.
 */
public enum MarketListingStatus {
	/**
	 * The listing was successful.
	 */
	SUCCESS("SUCCESS"),
	/**
	 * "Default" status (ask Facebook what this should mean, because they don't specify).
	 */
	DEFAULT("DEFAULT"),
	/**
	 * The listing was not successful.
	 */
	NOT_SUCCESS("NOT_SUCCESS");

	private String name;

	private MarketListingStatus( String name ) {
		this.name = name;
	}

	/**
	 * Get the name given by Facebook to this status code.
	 * 
	 * @return the name, as supplied by Facebook.
	 */
	public String getName() {
		return name;
	}
}
