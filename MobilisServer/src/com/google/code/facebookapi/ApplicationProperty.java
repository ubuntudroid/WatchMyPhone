package com.google.code.facebookapi;

import java.util.HashMap;
import java.util.Map;

/**
 * An enumeration for managing the different application properties that Facebook supports. These properties can be managed using the admin.* API calls. For more details,
 * 
 * @see http://wiki.developers.facebook.com/index.php/ApplicationProperties
 */
public enum ApplicationProperty {
	/** This is the URL to your application's About page. About pages are now Facebook Pages. */
	ABOUT_URL("about_url", "string"),
	/** The name of your application. */
	APPLICATION_NAME("application_name", "string"),
	/** The URL. Facebook pings after a user authorizes your application. The URL cannot be longer than 100 characters. */
	AUTHORIZE_URL("authorize_url", "string"),
	/** Your application's callback URL. The callback URL cannot be longer than 100 characters. */
	CALLBACK_URL("callback_url", "string"),
	/** The canvas name of your application. (this is a read-only property that cannot be set) */
	CANVAS_NAME("canvas_name", "string"),
	/** The URL to your site's Facebook Connect logo. (this is a read-only property that cannot be set) */
	CONNECT_LOGO_URL("connect_logo_url", "string"),
	/** This is the URL to your application from its bookmark on the Applications menu. */
	DASHBOARD_URL("dashboard_url", "string"),
	/**
	 * The default FBML code that defines what, if anything, appears in the user's profile actions when he or she adds your application. (Deprecated with the new
	 * profile.)
	 * 
	 * @deprecated
	 */
	@Deprecated
	DEFAULT_ACTION_URL("default_fbml", "string"),
	/** Indicates whether your application appears in the wide (1) or narrow (0) column of a user's Facebook profile. (Default value is 1.) */
	DEFAULT_COLUMN("default_column", "bool"),
	/** The default FBML code that appears in the user's profile box when he or she adds your application. */
	DEFAULT_FBML("default_fbml", "string"),
	/** The description of your application. */
	DESCRIPTION("description", "string"),
	/** Indicates whether your application is Web-based (0) or gets installed on a user's desktop (1). (Default value is 1.) */
	DESKTOP("desktop", "bool"),
	/** Indicates whether developer mode is enabled (1) or disabled (0). Only developers can install applications in developer mode. (default value is 1) */
	DEV_MODE("dev_mode", "bool"),
	/**
	 * The URL to the Edit link displayed on your application's profile box. An admin of a Facebook Page that has added your application can follow the URL to edit the
	 * application settings for the Page.
	 */
	EDIT_URL("edit_url", "string"),
	/**
	 * The email address associated with the application; the email address Facebook uses to contact you about your application. (default value is your Facebook email
	 * address.)
	 */
	EMAIL("email", "string"),
	/** The URL to your application's help page */
	HELP_URL("help_url", "string"),
	/** The URL to your application's icon. (this is a read-only property that cannot be set) */
	ICON_URL("icon_url", "string"),
	/**
	 * The URL to ping if a user changes the content of your application info section. Your application can get new content for the section, and set it with
	 * profile.setInfo.
	 */
	INFO_CHANGED_URL("info_changed_url", "string"),
	/** Indicates whether a user can (1) or cannot (0) install your application. (Default value is 1) */
	INSTALLABLE("installable", "bool"),
	/** For Web-based applications, these are the IP addresses of your servers that can access Facebook's servers and serve information to your application. */
	IP_LIST("ip_list", "string"),
	/** Indicates whether your application can run on a mobile device (1) or not (0). (Default value is 1.) */
	IS_MOBILE("is_mobile", "bool"),
	/** A URL for your application's logo, as shown in product directory and search listings. (this is a read-only property that cannot be set) */
	LOGO_URL("logo_url", "string"),
	/** For applications that can create attachments, this is the label for the action that creates the attachment. It cannot be more than 20 characters. */
	MESSAGE_ACTION("message_action", "string"),
	/** For applications that can create attachments, this is the URL where you store the attachment's content. */
	MESSAGE_URL("message_url", "string"),
	/** The URL where a user gets redirected after authorizing your application. If not set, the user will continue on to the page that they were originally going to. */
	POST_AUTHORIZE_REDIRECT_URL("post_authorize_redirect_url", "string"),
	/** The URL where a user gets redirected after installing your application on a Facebook Page. The post-install URL cannot be longer than 100 characters. */
	POST_INSTALL_URL("post_install_url", "string"),
	/** A preloaded FQL query. */
	PRELOAD_FQL("preload_fql", "string"),
	/** The URL to your application's privacy terms. */
	PRIVACY_URL("privacy_url", "string"),
	/** Indicates whether you want to disable (1) or enable (0) News Feed and Mini-Feed stories when a user installs your application. (default value is 1) */
	PRIVATE_INSTALL("private_install", "bool"),
	/** The URL from where Facebook fetches the content for an application tab. The URL should be relative to your canvas page URL at Facebook. */
	PROFILE_TAB_URL("profile_tab_url", "string"),
	/** The label for the link on a friend's profile to an application's Publisher content. */
	PUBLISH_ACTION("publish_action", "string"),
	/** The label for the link on user's own profile to an application's Publisher content. */
	PUBLISH_SELF_ACTION("publish_self_action", "string"),
	/** The URL from which to fetch your application content for use in the Publisher when the user is publishing on her own profile. */
	PUBLISH_SELF_URL("publish_self_url", "string"),
	/** The URL from which to fetch your application content for use in the Publisher when the user is publishing on a friend's profile. */
	PUBLISH_URL("publish_url", "string"),
	/** ?? */
	SEE_ALL_URL("see_all_url", "string"),
	/** The default label for an application tab when a user first adds it to his or her profile. */
	TAB_DEFAULT_NAME("tab_default_name", "string"),
	/** The URL to your application's Terms of Service. */
	TOS_URL("tos_url", "string"),
	/** The URL that Facebook pings after a user removes your application. */
	UNINSTALL_URL("uninstall_url", "string"),
	/** Indicates whether you render your application with FBML (0) or in an iframe (1). (Default value is 1.) */
	USE_IFRAME("use_iframe", "bool"),
	/** Indicates whether to use the profile's full canvas width, which is for backwards compatibility. */
	WIDE_MODE("wide_mode", "bool");


	/**
	 * A map of property names to their associated ApplicationProperty value
	 */
	protected static final Map<String,ApplicationProperty> PROP_TABLE;
	static {
		PROP_TABLE = new HashMap<String,ApplicationProperty>();
		for ( ApplicationProperty prop : ApplicationProperty.values() ) {
			PROP_TABLE.put( prop.getName(), prop );
		}
	}

	private String name;
	private String type;

	private ApplicationProperty( String name, String type ) {
		this.name = name;
		this.type = type;
	}

	/**
	 * Gets the name by which Facebook refers to this property. The name is what is sent in API calls and other requests to Facebook to specify the desired property.
	 * 
	 * @return the Facebook name given to this property.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the type which Facebook assigns to this property. The returned value will be "string" for string-typed properties, and "bool" for boolean typed properties.
	 * 
	 * @return the type Facebook gives to this property.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Lookup an ApplicationProperty value by its name.
	 * 
	 * @param propName
	 *            the name to lookup
	 * @return the ApplicationProperty value that corresponds to the specified name, or null if the name cannot be found/is not valid.
	 */
	public static ApplicationProperty getPropertyForString( String propName ) {
		return PROP_TABLE.get( propName );
	}

	public static ApplicationProperty getProperty( String name ) {
		return getPropertyForString( name );
	}

	public String propertyName() {
		return this.getName();
	}

	public String toString() {
		return this.getName();
	}

	public boolean isBooleanProperty() {
		return "bool".equals( this.type );
	}

	public boolean isStringProperty() {
		return "string".equals( this.type );
	}

	/**
	 * Returns true if this field has a particular name.
	 */
	public boolean isName( String name ) {
		return toString().equals( name );
	}

}
