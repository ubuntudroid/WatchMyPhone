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
 * Enum for specifying categories in marketplace listings and API calls.
 */
public enum MarketListingCategory {
	/**
	 * Category to specify for a for-sale listing
	 */
	FORSALE("FORSALE"),
	/**
	 * Category to specify for a housing listing
	 */
	HOUSING("HOUSING"),
	/**
	 * Category to specify for a job posting
	 */
	JOBS("JOBS"),
	/**
	 * Category for any listing that doesn't fit in any of the other categories
	 */
	OTHER("OTHER"),
	/**
	 * Category for a listing advertising free goods/services
	 */
	FREE("FREE"),
	/**
	 * Category for a listing seeking items for sale
	 */
	FORSALE_WANTED("FORSALE_WANTED"),
	/**
	 * Category for a listing seeking housing
	 */
	HOUSING_WANTED("HOUSING_WANTED"),
	/**
	 * Category for a listing seeking employment
	 */
	JOBS_WANTED("JOBS_WANTED"),
	/**
	 * Category for a listing seeking anything that doesn't fit in any other category
	 */
	OTHER_WANTED("OTHER_WANTED");

	private String name;

	private MarketListingCategory( String name ) {
		this.name = name;
	}

	/**
	 * Get the name by which Facebook refers to this category. This can be used in API calls when talking to their servers.
	 * 
	 * @return the name Facebook has allocated to this category.
	 */
	public String getName() {
		return name;
	}
}
