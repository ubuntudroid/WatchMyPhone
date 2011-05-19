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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility class to assist in creating a templatized action for publishing to the minifeed/newsfeed, because the API call Facebook decided to add in order to do it is
 * ridiculously complex.
 */
public class TemplatizedAction {

	protected static Log log = LogFactory.getLog( TemplatizedAction.class );

	private String titleTemplate;
	private String bodyTemplate;
	private String bodyGeneral;
	private String targetIds;
	private JSONObject titleParams;
	private JSONObject bodyParams;
	private List<IPair<Object,URL>> pictures;
	private Long pageActorId;

	public static final String UID_TOKEN = "http://UID/";

	private TemplatizedAction() {
		// empty constructor not allowed, at a minimum the titleTemplate parameter is needed
	}

	/**
	 * Constructor
	 * 
	 * @param titleTemplate
	 *            the title-template to set.
	 */
	public TemplatizedAction( String titleTemplate ) {
		this.setTitleTemplate( titleTemplate );
		this.titleParams = new JSONObject();
		this.bodyParams = new JSONObject();
		this.bodyTemplate = null;
		this.bodyGeneral = null;
		this.targetIds = null;
		this.pictures = new ArrayList<IPair<Object,URL>>();
		this.pageActorId = null;
	}

	/**
	 * Constructor
	 * 
	 * @param titleTemplate
	 *            the title template to use
	 * @param bodyTemplate
	 *            the body template to use
	 */
	public TemplatizedAction( String titleTemplate, String bodyTemplate ) {
		this( titleTemplate );
		this.setBodyTemplate( bodyTemplate );
		this.bodyGeneral = null;
		this.targetIds = null;
	}

	/**
	 * Constructor
	 * 
	 * @param titleTemplate
	 *            the title template to use
	 * @param bodyTemplate
	 *            the body template to use
	 * @param bodyGeneral
	 *            the non-templatized body content to use
	 */
	public TemplatizedAction( String titleTemplate, String bodyTemplate, String bodyGeneral ) {
		this( titleTemplate, bodyTemplate );
		this.setBodyGeneral( bodyGeneral );
		this.targetIds = null;
	}

	/**
	 * Remove a picture from the list, this can be used to revise the list/free up space for alternate pictures.
	 * 
	 * @param index
	 *            the index to remove from.
	 */
	public void removePicture( int index ) {
		if ( ( this.pictures == null ) || ( index > this.pictures.size() ) ) {
			return;
		}
		this.pictures.remove( index );
	}

	/**
	 * Add a picture to be associated with this feed entry, along with a link to be associated with the picture (clicking on the picture in the feed will go to the
	 * specified link).<br />
	 * <br />
	 * Note that only 4 pictures may be present at any given time. Any pictures beyond this are ignored. Use removePicture if you need to change something after 4
	 * pictures have been added.
	 * 
	 * @param imageUid
	 *            the id of the image to display. This can be a picture id or a Facebook user-id.
	 * @param linkHref
	 *            the URL of the link to go to when the image is clicked.
	 */
	public void addPicture( Long imageUid, String linkHref ) {
		addPicture( Long.toString( imageUid ), linkHref );
	}

	/**
	 * Add a picture to be associated with this feed entry, along with a link to be associated with the picture (clicking on the picture in the feed will go to the
	 * specified link).<br />
	 * <br />
	 * Note that only 4 pictures may be present at any given time. Any pictures beyond this are ignored. Use removePicture if you need to change something after 4
	 * pictures have been added.
	 * 
	 * @param imageHref
	 *            the URL of the image to display in the feed.
	 * @param linkHref
	 *            the URL of the link to go to when the image is clicked.
	 */
	public void addPicture( String imageHref, String linkHref ) {
		if ( linkHref == null ) {
			this.addPicture( imageHref );
		}
		try {
			if ( !imageHref.startsWith( "http" ) ) {
				imageHref = UID_TOKEN + imageHref;
			}
			addPicture( new URL( imageHref ), new URL( linkHref ) );
		}
		catch ( Exception ex ) {
			log.warn( "Could not add entry for picture!", ex );
		}
	}

	/**
	 * Add a picture to be associated with this feed entry, the picture will not have an associated link.
	 * 
	 * Note that only 4 pictures may be present at any given time. Any pictures beyond this are ignored. Use removePicture if you need to change something after 4
	 * pictures have been added.
	 * 
	 * @param imageHref
	 *            the URL of the image to display in the feed.
	 */
	public void addPicture( String imageHref ) {
		try {
			if ( !imageHref.startsWith( "http" ) ) {
				imageHref = UID_TOKEN + imageHref;
			}
			addPicture( new URL( imageHref ), null );
		}
		catch ( Exception ex ) {
			log.warn( "Could not add entry for picture!", ex );
		}
	}

	private void addPicture( URL imageUrl, URL linkUrl ) {
		if ( this.pictures == null ) {
			this.pictures = new ArrayList<IPair<Object,URL>>();
		}
		if ( this.pictures.size() < 4 ) {
			this.pictures.add( new Pair<Object,URL>( imageUrl, linkUrl ) );
		}
	}

	/**
	 * Set the pictures to display all at once, if you feel like building the Collection<Pair<URL, URL>> on your own. Otherwise use the more convenient addPic() method
	 * instead.
	 * 
	 * @param pics
	 *            the pictures to set.
	 */
	public void setPictures( List<? extends IPair<Object,URL>> pics ) {
		if ( ( pics == null ) || ( pics.isEmpty() ) ) {
			this.pictures = null;
			return;
		}
		if ( pics.size() <= 4 ) {
			this.pictures = (List<IPair<Object,URL>>) pics;
		}
		if ( pics.size() > 4 ) {
			int count = 0;
			for ( IPair<Object,URL> pic : pics ) {
				this.pictures.add( pic );
				count++ ;
				if ( count == 4 ) {
					break;
				}
			}
		}
	}

	/**
	 * Get the list of pictures.
	 * 
	 * @return the list of pictures.
	 */
	public Collection<IPair<Object,URL>> getPictures() {
		if ( ( this.pictures == null ) || ( this.pictures.isEmpty() ) ) {
			return null;
		}
		return this.pictures;
	}

	/**
	 * Add a parameter value for the title template. It will be used to replace the corresponding token when the feed entry is rendered.
	 * 
	 * @param key
	 *            the name of the parameter/token.
	 * @param value
	 *            the value to set it to.
	 */
	public void addTitleParam( String key, String value ) {
		addParam( titleParams, key, value );
	}

	/**
	 * Add a parameter value for the body template. It will be used to replace the corresponding token when the feed entry is rendered.
	 * 
	 * @param key
	 *            the name of the parameter/token.
	 * @param value
	 *            the value to set it to.
	 */
	public void addBodyParam( String key, String value ) {
		addParam( bodyParams, key, value );
	}

	private void addParam( JSONObject map, String key, String value ) {
		if ( ( "actor".equals( key ) ) || ( "target".equals( key ) ) ) {
			throw new RuntimeException( key + " is a reserved token name, you cannot set it yourself!" );
		}
		try {
			map.put( key, value );
		}
		catch ( JSONException ex ) {
			log.warn( "JSONException for key=" + key + ", value=" + value + "!", ex );
		}
	}

	/**
	 * Get the title params as a JSON-formatted string.
	 * 
	 * @return the parameters for the templatized title tokens.
	 */
	public String getTitleParams() {
		return getJsonParams( titleParams );
	}

	/**
	 * Get the body params as a JSON-formatted string.
	 * 
	 * @return the parameters for the templatized body tokens.
	 */
	public String getBodyParams() {
		return getJsonParams( bodyParams );
	}

	private String getJsonParams( JSONObject params ) {
		if ( params.length() == 0 ) {
			return null;
		}
		return params.toString();
	}

	/**
	 * @return the bodyGeneral
	 */
	public String getBodyGeneral() {
		return bodyGeneral;
	}

	/**
	 * Set the general body content for this feed entry. This is optional, non-templatized markup, and is distinct from and unrelated to bodyTemplate. A feed entry can
	 * have both at once, either or, or neither.
	 * 
	 * @param bodyGeneral
	 *            non-templatized markup that will be displayed in this feed entry. When multiple entries are aggregated, only the bodyGeneral from (an arbitrarily
	 *            chosen) one of them will appear in the aggregate entry, meaning that any markup specified here must be generic enough that it can make sense in any
	 *            context.
	 */
	public void setBodyGeneral( String bodyGeneral ) {
		if ( "".equals( bodyGeneral ) ) {
			bodyGeneral = null;
		}
		this.bodyGeneral = bodyGeneral;
	}

	/**
	 * @return the bodyTemplate
	 */
	public String getBodyTemplate() {
		return bodyTemplate;
	}

	/**
	 * Set the body template for this feed entry. The body template is optinal.
	 * 
	 * @param bodyTemplate
	 *            templatized markup that will be used to compute the body section of this feed entry. Unlike titleTemplate, this markup *is not* required to utilize the
	 *            "{actor}" token, although you may choose to use any desired tokens in this section if you wish.
	 */
	public void setBodyTemplate( String bodyTemplate ) {
		if ( "".equals( bodyTemplate ) ) {
			bodyTemplate = null;
		}
		this.bodyTemplate = bodyTemplate;
	}

	/**
	 * @return the targetIds
	 */
	public String getTargetIds() {
		if ( "".equals( targetIds ) ) {
			targetIds = null;
		}
		return targetIds;
	}

	/**
	 * Set the target ids of friends who are associated with this action. This must be specified if you have used the "{target}" token in any of your markup.<br />
	 * <br />
	 * This method will clear out any previously added target ids. To append additional target ids to a previous list, use addTargetIds instead.
	 * 
	 * @param targetIds
	 *            a comma-seperated list of Facebook UID's representing any friends of the current user who are associated with the action being published (so if the
	 *            action is "Bob waves to Sally and Susie", Sally and Susie are the targets).
	 */
	public void setTargetIds( String targetIds ) {
		if ( "".equals( targetIds ) ) {
			targetIds = null;
		}
		this.targetIds = targetIds;
	}

	/**
	 * Append to the list of friends who are associated with this action.<br />
	 * <br />
	 * This method *will not* clear out any previously added target ids.
	 * 
	 * @param newIds
	 *            a comma-seperated list of Facebook UID's representing any friends of the current user who are associated with the action being published (so if the
	 *            action is "Bob waves to Sally and Susie", Sally and Susie are the targets).
	 */
	public void addTargetIds( String newIds ) {
		if ( this.targetIds == null ) {
			this.targetIds = "";
		}
		if ( !"".equals( this.targetIds ) ) {
			this.targetIds += ",";
		}
		this.targetIds += newIds;
	}

	/**
	 * Set the target ids of friends who are associated with this action. This must be specified if you have used the "{target}" token in any of your markup.<br />
	 * <br />
	 * This method will clear out any previously added target ids. To append additional target ids to a previous list, use addTargetIds instead.
	 * 
	 * @param facebookIds
	 *            a list of all the Facebook UID to specify as targets. The elements in the collection may only be of type Integer or type String.
	 */
	public void setTargetIds( Collection<Object> facebookIds ) {
		if ( facebookIds.isEmpty() ) {
			this.targetIds = null;
			return;
		}
		this.targetIds = "";
		for ( Object current : facebookIds ) {
			if ( !"".equals( this.targetIds ) ) {
				this.targetIds += ",";
			}
			this.targetIds += current;
		}
	}

	/**
	 * Append to the set of friends who are associated with this action. This must be specified if you have used the "{target}" token in any of your markup.<br />
	 * <br />
	 * This method *will not* clear out any previously added target ids.
	 * 
	 * @param facebookIds
	 *            a list of all the Facebook UID to specify as targets. The elements in the collection may only be of type Long or type String.
	 */
	public void addTargetIds( Collection<Object> facebookIds ) {
		if ( this.targetIds == null ) {
			this.targetIds = "";
		}
		for ( Object current : facebookIds ) {
			if ( !"".equals( this.targetIds ) ) {
				this.targetIds += ",";
			}
			this.targetIds += current;
		}
	}

	/**
	 * @return the titleTemplate
	 */
	public String getTitleTemplate() {
		return titleTemplate;
	}

	/**
	 * Set the title template for this feed entry. This is a required field, and the template must always contain the "{actor}" token.
	 * 
	 * @param titleTemplate
	 *            templatized markup to use as the title of this feed entry. It must contain the "{actor}" token.
	 */
	public void setTitleTemplate( String titleTemplate ) {
		if ( titleTemplate == null ) {
			throw new RuntimeException( "The title-template cannot be null!" );
		}
		if ( !titleTemplate.contains( "{actor}" ) && !titleTemplate.contains( "{*actor*}" ) ) {
			throw new RuntimeException( titleTemplate + " is an invalid template!  The title-template must contain the \"{actor}\" token." );
		}
		this.titleTemplate = titleTemplate;
	}

	/**
	 * @return the page actor-id
	 */
	public Long getPageActorId() {
		return pageActorId;
	}

	/**
	 * @param pageActorId
	 *            the page actor-id to set
	 */
	public void setPageActorId( Long pageActorId ) {
		this.pageActorId = pageActorId;
	}

}
