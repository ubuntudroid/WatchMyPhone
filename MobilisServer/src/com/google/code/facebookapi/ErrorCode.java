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
 * Static class for organizing the error codes used by the Facebook API.
 * 
 * Full details are here:  http://wiki.developers.facebook.com/index.php/Error_codes
 */
public class ErrorCode {
    /**
     * An unspecified error occured.
     */
    public static final Integer GEN_UNKNOWN_ERROR = 1;
    /**
     * API service is temporarily down.
     */
    public static final Integer GEN_SERVICE_ERROR = 2;
    /**
     * Unknown API method specified
     */
    public static final Integer GEN_UNKNOWN_METHOD = 3;
    /**
     * Too many API calls have been made
     */
    public static final Integer GEN_TOO_MANY_CALLS = 4;
    /**
     * The API call was made from a machine with a blocked IP
     */
    public static final Integer GEN_BAD_IP = 5;
    /**
     * An invalid API server URL was specified
     */
    public static final Integer GEN_WRONG_API_SERVER = 6;
    /**
     * A specified API parameter was invalid, or a required parameter was missing
     */
    public static final Integer GEN_INVALID_PARAMETER = 100;
    /**
     * Specified user not valid
     */
    public static final Integer PHOTO_INVALID_USER_ID = 110;
    /**
     * Specified album not valid
     */
    public static final Integer PHOTO_INVALID_ALBUM_ID = 120;
    /**
     * Specified photo not valid
     */
    public static final Integer PHOTO_INVALID_PHOTO_ID = 121;
    /**
     * Feed priority value not valid
     */
    public static final Integer FEED_INVALID_PRIORITY = 130;
    /**
     * The requested action generated a permissions error
     */
    public static final Integer GEN_PERMISSIONS_ERROR = 200;
    /**
     * Desktop app tried to set FBML for an invalid user
     */
    public static final Integer FBML_DESKTOP_FBML_RESTRICTED = 240;
    /**
     * Updating a user's status requires extended permissions
     */
    public static final Integer PERM_REQUIRED_STATUS = 250;
    /**
     * Editing existing photos requires extended permissions
     */
    public static final Integer PERM_REQUIRED_PHOTOS = 260;
    /**
     * The specified album is full
     */
    public static final Integer PHOTOS_ALBUM_FULL = 321;
    /**
     * The specified photo tag target is invalid
     */
    public static final Integer PHOTOS_INVALID_TAG_SUBJECT = 322;
    /**
     * The specified photo cannot be tagged
     */
    public static final Integer PHOTOS_TAG_NOT_ALLOWED = 323;
    /**
     * The photo file is invalid or missing
     */
    public static final Integer PHOTOS_BAD_IMAGE = 324;
    /**
     * Too many photos are pending
     */
    public static final Integer PHOTOS_TOO_MANY_PHOTOS = 325;
    /**
     * Too many tags are pending
     */
    public static final Integer PHOTOS_TOO_MANY_TAGS = 326;
    /**
     * Failed to set FBML parkup
     */
    public static final Integer FBML_MARKUP_NOT_SET = 330;
    /**
     * Feed publication limit reached
     */
    public static final Integer FEED_LIMIT_REACHED = 340;
    /**
     * Feed action limit reached
     */
    public static final Integer FEED_ACTION_LIMIT_REACHED = 341;
    /**
     * Feed title contains too many links
     */
    public static final Integer FEED_TOO_MANY_LINKS = 342;
    /**
     * Feed title is too long
     */
    public static final Integer FEED_TITLE_LENGTH_EXCEEDED = 343;
    /**
     * Too many fb:userLink tags in title, or like points to an invalid user
     */
    public static final Integer FEED_INCORRECT_USERLINK = 344;
    /**
     * Feed title is blank
     */
    public static final Integer FEED_BLANK_TITLE = 345;
    /**
     * Feed body is too long
     */
    public static final Integer FEED_BODY_LENGTH_EXCEEDED = 346;
    /**
     * Could not find photo to include in feed
     */
    public static final Integer FEED_PHOTO_NOT_FOUND = 347;
    /**
     * Specified photo URL for feed is invalid
     */
    public static final Integer FEED_PHOTO_LINK_INVALID = 348;
    /** 
     * Session key specified has passed its expiration time  
     */
    public static final int SESSION_TIMED_OUT = 450;
    /** 
     * Session key specified cannot be used to call this method 
     */
    public static final int SESSION_METHOD_NOT_ALLOWED = 451;
    /** 
     * Session key invalid. This could be because the session key has an 
     * incorrect format, or because the user has revoked this session  
     */
    public static final int SESSION_INVALID = 452;
    /** 
     * A session key is required for calling this method 
     */
    public static final int SESSION_REQUIRED = 453;
    /** 
     * A session key must be specified when request is signed with a session secret 
     */
    public static final int SESSION_REQUIRED_FOR_SECRET = 454;
    /** 
     * A session secret is not permitted to be used with this type of session key
     */
    public static final int SESSION_CANNOT_USE_SESSION_SECRET = 455;
    /**
     * An unknown error occured when processing FQL
     */
    public static final Integer FQL_UNKNOWN_ERROR = 600;
    /**
     * FQL query fails to parse
     */
    public static final Integer FQL_PARSE_ERROR = 601;
    /**
     * Field referenced in FQL was not found
     */
    public static final Integer FQL_FIELD_NOT_FOUND = 602;
    /**
     * Table referenced in FQL was not found
     */
    public static final Integer FQL_TABLE_NOT_FOUND = 603;
    /**
     * FQL query cannot be indexed
     */
    public static final Integer FQL_NOT_INDEXABLE = 604;
    /**
     * The requested FQL function was not found
     */
    public static final Integer FQL_INVALID_FUNCTION = 605;
    /**
     * The FQL query includes an invalid parameter
     */
    public static final Integer FQL_INVALID_PARAMETER = 606;
    /**
     * Unknown error, please try the request again
     */
    public static final Integer GEN_REF_SET_FAILED = 700;
}
