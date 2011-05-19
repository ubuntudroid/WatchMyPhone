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

import java.util.EnumSet;

/**
 * Enumaration that maps API method names to the (maximal) number of parameters that each method will send.
 * 
 * There are arguably better ways to do this.
 */
public enum FacebookMethod implements IFacebookMethod, CharSequence {

	// Authentication
	AUTH_CREATE_TOKEN("facebook.auth.createToken"),
	AUTH_GET_SESSION("facebook.auth.getSession"),
	AUTH_EXPIRE_SESSION("facebook.auth.expireSession"),
	AUTH_REVOKE_AUTHORIZATION("facebook.auth.revokeAuthorization"),
	AUTH_PROMOTE_SESSION("facebook.auth.promoteSession"),

	CONNECT_REGISTER_USERS("facebook.connect.registerUsers"),
	CONNECT_UNREGISTER_USERS("facebook.connect.unregisterUsers"),
	CONNECT_GET_UNCONNECTED_FRIENDS_COUNT("facebook.connect.getUnconnectedFriendsCount"),

	// FQL Query
	FQL_QUERY("facebook.fql.query"),

	// Events
	EVENTS_GET("facebook.events.get"),
	EVENTS_GET_MEMBERS("facebook.events.getMembers"),
	EVENTS_GET_CREATE("facebook.events.create"),
	EVENTS_GET_EDIT("facebook.events.edit"),
	EVENTS_GET_CANCEL("facebook.events.cancel"),
	EVENTS_GET_RSVP("facebook.events.rsvp"),

	// Friends
	FRIENDS_GET_APP_USERS("facebook.friends.getAppUsers"),
	@Deprecated
	FRIENDS_GET_REQUESTS("facebook.friends.getRequests"), // deprectaed/unofficial
	FRIENDS_ARE_FRIENDS("facebook.friends.areFriends"),
	FRIENDS_GET("facebook.friends.get"),
	FRIENDS_GET_NOSESSION("facebook.friends.get"),
	FRIENDS_GET_LISTS("facebook.friends.getLists"),
	// Users
	USERS_GET_INFO("facebook.users.getInfo"),
	USERS_GET_STANDARD_INFO("facebook.users.getStandardInfo"),
	USERS_GET_LOGGED_IN_USER("facebook.users.getLoggedInUser"),
	@Deprecated
	USERS_IS_APP_ADDED("facebook.users.isAppAdded"),
	@Deprecated
	USERS_IS_APP_ADDED_NOSESSION("facebook.users.isAppAdded"),
	USERS_IS_APP_USER("facebook.users.isAppUser"),
	USERS_IS_APP_USER_NOSESSION("facebook.users.isAppUser"),
	USERS_HAS_APP_PERMISSION("facebook.users.hasAppPermission"),
	USERS_HAS_APP_PERMISSION_NOSESSION("facebook.users.hasAppPermission"),
	USERS_SET_STATUS("facebook.users.setStatus"),
	USERS_SET_STATUS_NOSESSION("facebook.users.setStatus"),
	// Photos
	PHOTOS_GET("facebook.photos.get"),
	PHOTOS_GET_ALBUMS("facebook.photos.getAlbums"),
	PHOTOS_GET_TAGS("facebook.photos.getTags"),
	// PhotoUploads
	PHOTOS_CREATE_ALBUM("facebook.photos.createAlbum"),
	PHOTOS_CREATE_ALBUM_NOSESSION("facebook.photos.createAlbum"),
	PHOTOS_ADD_TAG("facebook.photos.addTag"),
	PHOTOS_ADD_TAG_NOSESSION("facebook.photos.addTag"),
	PHOTOS_UPLOAD("facebook.photos.upload"),
	PHOTOS_UPLOAD_NOSESSION("facebook.photos.upload"),
	// Notifications
	NOTIFICATIONS_GET("facebook.notifications.get"),
	NOTIFICATIONS_SEND("facebook.notifications.send"),
	@Deprecated
	NOTIFICATIONS_SEND_REQUEST("facebook.notifications.sendRequest"),
	NOTIFICATIONS_SEND_EMAIL_SESSION("facebook.notifications.sendEmail"),
	NOTIFICATIONS_SEND_EMAIL_NOSESSION("facebook.notifications.sendEmail"),
	// Groups
	GROUPS_GET("facebook.groups.get"),
	GROUPS_GET_MEMBERS("facebook.groups.getMembers"),
	// Profile
	PROFILE_SET_FBML("facebook.profile.setFBML"),
	PROFILE_SET_FBML_NOSESSION("facebook.profile.setFBML"),
	PROFILE_GET_FBML("facebook.profile.getFBML"),
	PROFILE_GET_FBML_NOSESSION("facebook.profile.getFBML"),
	PROFILE_SET_INFO("facebook.profile.setInfo"),
	PROFILE_SET_INFO_OPTIONS("facebook.profile.setInfoOptions"),
	PROFILE_GET_INFO("facebook.profile.getInfo"),
	PROFILE_GET_INFO_OPTIONS("facebook.profile.getInfoOptions"),
	// FBML
	FBML_REFRESH_REF_URL("facebook.fbml.refreshRefUrl"),
	FBML_REFRESH_IMG_SRC("facebook.fbml.refreshImgSrc"),
	FBML_SET_REF_HANDLE("facebook.fbml.setRefHandle"),
	// Feed
	FEED_PUBLISH_TEMPLATIZED_ACTION("facebook.feed.publishTemplatizedAction"),
	FEED_REGISTER_TEMPLATE("facebook.feed.registerTemplateBundle"),
	FEED_GET_TEMPLATES("facebook.feed.getRegisteredTemplateBundles"),
	FEED_GET_TEMPLATE_BY_ID("facebook.feed.getRegisteredTemplateBundleByID"),
	FEED_PUBLISH_USER_ACTION("facebook.feed.publishUserAction"),
	FEED_DEACTIVATE_TEMPLATE_BUNDLE("facebook.feed.deactivateTemplateBundleByID"),
	// Marketplace
	MARKET_CREATE_LISTING("facebook.marketplace.createListing"),
	MARKET_CREATE_LISTING_NOSESSION("facebook.marketplace.createListing"),
	MARKET_GET_CATEGORIES("facebook.marketplace.getCategories"),
	MARKET_GET_SUBCATEGORIES("facebook.marketplace.getSubCategories"),
	MARKET_GET_LISTINGS("facebook.marketplace.getListings"),
	MARKET_REMOVE_LISTING("facebook.marketplace.removeListing"),
	MARKET_REMOVE_LISTING_NOSESSION("facebook.marketplace.removeListing"),
	MARKET_SEARCH("facebook.marketplace.search"),
	/**
	 * @deprecated provided for legacy support only. Please use MARKET_GET_CATEGORIES instead.
	 */
	@Deprecated
	MARKETPLACE_GET_CATEGORIES("facebook.marketplace.getCategories"),
	/**
	 * @deprecated provided for legacy support only. Please use MARKET_GET_SUBCATEGORIES instead.
	 */
	@Deprecated
	MARKETPLACE_GET_SUBCATEGORIES("facebook.marketplace.getSubCategories"),
	/**
	 * @deprecated provided for legacy support only. Please use MARKET_GET_LISTINGS instead.
	 */
	@Deprecated
	MARKETPLACE_GET_LISTINGS("facebook.marketplace.getListings"),
	/**
	 * @deprecated provided for legacy support only. Please use MARKET_CREATE_LISTING instead.
	 */
	@Deprecated
	MARKETPLACE_CREATE_LISTING("facebook.marketplace.createListing"),
	/**
	 * @deprecated provided for legacy support only. Please use MARKET_SEARCH instead.
	 */
	@Deprecated
	MARKETPLACE_SEARCH("facebook.marketplace.search"),
	/**
	 * @deprecated provided for legacy support only. Please use MARKET_REMOVE_LISTING instead.
	 */
	@Deprecated
	MARKETPLACE_REMOVE_LISTING("facebook.marketplace.removeListing"),

	// Data
	DATA_SET_COOKIE("facebook.data.setCookie"),
	DATA_GET_COOKIES("facebook.data.getCookies"),
	DATA_SET_USER_PREFERENCE("facebook.data.setUserPreference"),
	DATA_SET_USER_PREFERENCES("facebook.data.setUserPreferences"),
	DATA_GET_USER_PREFERENCE("facebook.data.getUserPreference"),
	DATA_GET_USER_PREFERENCES("facebook.data.getUserPreferences"),
	
	DATA_CREATE_OBJECT_TYPE("facebook.data.createObjectType"),
	DATA_DROP_OBJECT_TYPE("facebook.data.dropObjectType"),
	DATA_RENAME_OBJECT_TYPE("facebook.data.renameObjectType"),
	DATA_DEFINE_OBJECT_PROPERTY("facebook.data.defineObjectProperty"),
	DATA_UNDEFINE_OBJECT_PROPERTY("facebook.data.undefineObjectProperty"),
	DATA_RENAME_OBJECT_PROPERTY("facebook.data.renameObjectProperty"),
	DATA_GET_OBJECT_TYPES("facebook.data.getObjectTypes"),
	DATA_GET_OBJECT_TYPE("facebook.data.getObjectType"),

	DATA_CREATE_OBJECT("facebook.data.createObject"),
	DATA_UPDATE_OBJECT("facebook.data.updateObject"),
	DATA_DELETE_OBJECT("facebook.data.deleteObject"),
	DATA_DELETE_OBJECTS("facebook.data.deleteObjects"),
	DATA_GET_OBJECT("facebook.data.getObject"),
	DATA_GET_OBJECTS("facebook.data.getObjects"),
	DATA_GET_OBJECT_PROPERTY("facebook.data.getObjectProperty"),
	DATA_SET_OBJECT_PROPERTY("facebook.data.setObjectProperty"),
	DATA_SET_ASSOCIATION("facebook.data.setAssociation"),
	DATA_REMOVE_ASSOCIATION("facebook.data.removeAssociation"),
	DATA_REMOVE_ASSOCIATED_OBJECTS("facebook.data.removeAssociatedObjects"),
	DATA_GET_ASSOCIATED_OBJECT_COUNT("facebook.data.getAssociatedObjectCount"),
	
	DATA_DEFINE_ASSOCIATION("facebook.data.defineAssociation"),
	DATA_UNDEFINE_ASSOCIATION("facebook.data.undefineAssociation"),
	DATA_RENAME_ASSOCIATION("facebook.data.renameAssociation"),
	DATA_GET_ASSOCIATION_DEFINITION("facebook.data.getAssociationDefinition"),
	DATA_GET_ASSOCIATION_DEFINITIONS("facebook.data.getAssociationDefinitions"),	
	

	// SMS - Mobile
	SMS_CAN_SEND("facebook.sms.canSend"),
	/**
	 * @deprecated use SMS_SEND_MESSAGE instead.
	 */
	@Deprecated
	SMS_SEND("facebook.sms.send"),
	SMS_SEND_MESSAGE("facebook.sms.send"),
	// Facebook Pages
	PAGES_IS_APP_ADDED("facebook.pages.isAppAdded"),
	PAGES_IS_ADMIN("facebook.pages.isAdmin"),
	PAGES_IS_FAN("facebook.pages.isFan"),
	PAGES_GET_INFO("facebook.pages.getInfo"),
	PAGES_GET_INFO_NOSESSION("facebook.pages.getInfo"),

	// Admin
	ADMIN_GET_APP_PROPERTIES("facebook.admin.getAppProperties"),
	ADMIN_SET_APP_PROPERTIES("facebook.admin.setAppProperties"),
	ADMIN_GET_ALLOCATION("facebook.admin.getAllocation"),
	@Deprecated
	ADMIN_GET_DAILY_METRICS("facebook.admin.getDailyMetrics"),
	ADMIN_GET_METRICS("facebook.admin.getMetrics"),

	// Permissions
	PERM_GRANT_API_ACCESS("facebook.permissions.grantApiAccess"),
	PERM_CHECK_AVAILABLE_API_ACCESS("facebook.permissions.checkAvailableApiAccess"),
	PERM_REVOKE_API_ACCESS("facebook.permissions.revokeApiAccess"),
	PERM_CHECK_GRANTED_API_ACCESS("facebook.permissions.checkGrantedApiAccess"),

	// Application
	APPLICATION_GET_PUBLIC_INFO("facebook.application.getPublicInfo"),

	// LiveMessage
	LIVEMESSAGE_SEND("facebook.livemessage.send"),

	// Batch
	BATCH_RUN("facebook.batch.run");

	private static final EnumSet<FacebookMethod> listSessionNone;
	private static final EnumSet<FacebookMethod> listTakesFile;

	static {
		listSessionNone = EnumSet.of( USERS_IS_APP_ADDED_NOSESSION, USERS_IS_APP_USER_NOSESSION, PROFILE_SET_FBML_NOSESSION, PROFILE_GET_FBML_NOSESSION,
				USERS_SET_STATUS_NOSESSION, MARKET_CREATE_LISTING_NOSESSION, MARKET_REMOVE_LISTING_NOSESSION, PHOTOS_ADD_TAG_NOSESSION, PHOTOS_CREATE_ALBUM_NOSESSION,
				PHOTOS_UPLOAD_NOSESSION, USERS_HAS_APP_PERMISSION_NOSESSION, PAGES_GET_INFO_NOSESSION, SMS_SEND, SMS_SEND_MESSAGE, FBML_REFRESH_IMG_SRC,
				FBML_REFRESH_REF_URL, FBML_SET_REF_HANDLE, CONNECT_GET_UNCONNECTED_FRIENDS_COUNT, CONNECT_REGISTER_USERS, CONNECT_UNREGISTER_USERS,
				NOTIFICATIONS_SEND_EMAIL_NOSESSION );
		listTakesFile = EnumSet.of( PHOTOS_UPLOAD, PHOTOS_UPLOAD_NOSESSION );
	}



	private String methodName;

	FacebookMethod( String name ) {
		assert ( name != null && 0 != name.length() );
		this.methodName = name;
	}

	/**
	 * Get the Facebook method name
	 * 
	 * @return the Facebook method name
	 */
	public String methodName() {
		return this.methodName;
	}

	public boolean requiresNoSession() {
		return listSessionNone.contains( this );
	}

	/**
	 * @return true if this API call requires a file-stream as a parameter false otherwise
	 */
	public boolean takesFile() {
		return listTakesFile.contains( this );
	}

	/* Implementing CharSequence */
	public char charAt( int index ) {
		return this.methodName.charAt( index );
	}

	public int length() {
		return this.methodName.length();
	}

	public CharSequence subSequence( int start, int end ) {
		return this.methodName.subSequence( start, end );
	}

	public String toString() {
		return this.methodName;
	}

}
