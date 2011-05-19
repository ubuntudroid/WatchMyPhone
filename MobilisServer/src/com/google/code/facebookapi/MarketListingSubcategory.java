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
 * Enum for specifying sub-categories in marketplace listings/API calls.
 */
public enum MarketListingSubcategory {
	/**
	 * Subcategory for listings involving books
	 */
	BOOKS("BOOKS"),
	/**
	 * Subcategory for listings involving furniture
	 */
	FURNITURE("FURNITURE"),
	/**
	 * Subcategory for listings involving event tickets
	 */
	TICKETS("TICKETS"),
	/**
	 * Subcategory for listings involving electronics
	 */
	ELECTRONICS("ELECTRONICS"),
	/**
	 * Subcategory for listings involving cars
	 */
	AUTOS("AUTOS"),
	/**
	 * Subcategory for listings involving things not specified by any of the other subcategories
	 */
	GENERAL("GENERAL"),
	/**
	 * Subcategory for listings involving rentals
	 */
	RENTALS("RENTALS"),
	/**
	 * Subcategory for listings involving sublets
	 */
	SUBLETS("SUBLETS"),
	/**
	 * Subcategory for listings involving real-estate
	 */
	REAL_ESTATE("REALESTATE"),
	/**
	 * Subcategory for listings seeking books
	 */
	BOOKS_WANTED("BOOKS_WANTED"),
	/**
	 * Subcategory for listings seeking furniture
	 */
	FURNITURE_WANTED("FURNITURE_WANTED"),
	/**
	 * Subcategory for listings seeking electronics
	 */
	ELECTRONICS_WANTED("ELECTRONICS_WANTED"),
	/**
	 * Subcategory for listings seeking cars
	 */
	AUTOS_WANTED("AUTOS_WANTED"),
	/**
	 * Subcategory for listings seeking things not specified by any of the other subcategories
	 */
	GENERAL_WANTED("GENERAL_WANTED"),
	/**
	 * Subcategory for listings seeking sublets
	 */
	SUBLETS_WANTED("SUBLETS_WANTED"),
	/**
	 * Subcategory for listings seeking real-estate
	 */
	REAL_ESTATE_WANTED("REALESTATE_WANTED");

	private String name;

	private MarketListingSubcategory( String name ) {
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
