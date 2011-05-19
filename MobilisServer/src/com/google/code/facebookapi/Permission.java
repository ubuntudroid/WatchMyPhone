package com.google.code.facebookapi;

/**
 * Enum for managing the different permission-types used by Facebook. These are opt-in permissions that the user must explicitly grant, and can only be requested one at a
 * time. To request that a user grant you a permission, direct them to a URL of the form:
 * 
 * http://www.facebook.com/authorize.php?api_key=[YOUR_API_KEY]&v=1.0&ext_perm=[PERMISSION NAME]
 * 
 * You can query to see if the user has granted your application a given permission using the 'users.hasAppPermission' API call.
 * 
 * @see <a
 *      href="http://wiki.developers.facebook.com/index.php/Extended_application_permission">http://wiki.developers.facebook.com/index.php/Extended_application_permission</a>
 * @see <a href=""></a>
 * @see <a href="http://wiki.developers.facebook.com/index.php/Users.hasAppPermission">http://wiki.developers.facebook.com/index.php/Users.hasAppPermission</a>
 */
public enum Permission {
	/**
	 * This permission allows an application to send email to its user. This permission can be obtained only through the fb:prompt-permission tag or the promptpermission
	 * attribute. When the user accepts, you can send him an email via Notifications.sendEmail or directly to the proxied_email FQL field.
	 */
	EMAIL("email"),
	/**
	 * This permission grants an application access to user data when the user is offline or doesn't have an active session. This permission can be obtained only through
	 * the fb:prompt-permission tag or the promptpermission attribute.
	 */
	OFFLINE_ACCESS("offline_access"),
	/** This extended permission grants access to the users.setStatus method. */
	STATUS_UPDATE("status_update"),
	/**
	 * This permission relaxes requirements on the photos.upload and photos.addTag methods. If the user grants this permission, photos uploaded by the application will
	 * bypass the pending state and the user will not have to manually approve the photos each time.
	 */
	PHOTO_UPLOAD("photo_upload"),
	/**
	 * This permission allows an app to create and modify listings for a user via the marketplace.removeListing and marketplace.createListing methods. The user will not
	 * need to approve each listing individually.
	 */
	CREATE_LISTING("create_listing"),
	/** This permission allows an app to create and modify events for a user via the events.create, events.edit and events.cancel methods. */
	CREATE_EVENT("create_event"),
	/** This permission allows an app to RSVP to an event on behalf of a user via the events.rsvp method. */
	RSVP_EVENT("rsvp_event"),
	/** This permissions allows a mobile application to send messages to the user and respond to messages from the user via text message. */
	SMS_SEND("sms"),
	/**
	 * @deprecated {@link #CREATE_LISTING}
	 */
	@Deprecated
	MARKETPLACE_CREATE("create_listing");

	/**
	 * The unchanging part of the URL to use when authorizing permissions.
	 */
	public static final String PERM_AUTHORIZE_ADDR = "http://www.facebook.com/authorize.php";

	private String name;

	private Permission( String name ) {
		this.name = name;
	}

	/**
	 * Gets the name by which Facebook refers to this permission. The name is what is sent in API calls and other requests to Facebook to specify the desired premission.
	 * 
	 * @return the Facebook name given to this permission.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Compute the URL to which to send the user to request the extended permission.
	 * 
	 * @param apiKey
	 *            your application's API key.
	 * @param permission
	 *            the permission you want the grant URL for.
	 * 
	 * @return a String that specifies the URL to direct users to in order to grant this permission to the application.
	 */
	public static String authorizationUrl( String apiKey, Permission permission ) {
		return authorizationUrl( apiKey, permission.getName() );
	}

	private static String authorizationUrl( String apiKey, CharSequence permission ) {
		return String.format( "%s?api_key=%s&v=1.0&ext_perm=%s", PERM_AUTHORIZE_ADDR, apiKey, permission );
	}

}
