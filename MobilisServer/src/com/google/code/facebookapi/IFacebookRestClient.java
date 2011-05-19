package com.google.code.facebookapi;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;

import org.json.JSONObject;

import com.google.code.facebookapi.schema.Listing;

/**
 * Generic interface for a FacebookRestClient, parameterized by output format. For continually updated documentation, please refer to the <a
 * href="http://wiki.developers.facebook.com/index.php/API">Developer Wiki</a>.
 */
public interface IFacebookRestClient<T> {

	public static final String TARGET_API_VERSION = "1.0";
	public static final String ERROR_TAG = "error_response";
	public static final String FB_SERVER = "api.facebook.com/restserver.php";
	public static final String SERVER_ADDR = "http://" + FB_SERVER;
	public static final String HTTPS_SERVER_ADDR = "https://" + FB_SERVER;

	public String getApiKey();
	public String getSecret();
	
	/**
	 * Check to see if the client is running in desktop-app mode
	 * 
	 * @return true if the app is running in desktop mode. false otherwise
	 */
	public boolean isDesktop();

	/**
	 * Set the client to run in desktop-app mode.
	 * 
	 * @param isDesktop
	 *            set to true to enable desktop mode set to false to disable desktop mode
	 */
	public void setIsDesktop( boolean isDesktop );

	public T getCacheFriendsList();

	public void setCacheFriendsList( List<Long> friendIds );

	@Deprecated
	public Boolean getCacheAppAdded();

	@Deprecated
	public void setCacheAppAdded( Boolean appAdded );

	@Deprecated
	public Boolean getCacheAppUser();

	public void setCacheAppUser( Boolean appUser );

	public void setCacheSession( String cacheSessionKey, Long cacheUserId, Long cacheSessionExpires );

	/**
	 * Sets the FBML for a user's profile, including the content for both the profile box and the profile actions.
	 * 
	 * @param userId
	 *            The user ID for the user whose profile you are updating, or the page ID in case of a Page. If this parameter is not specified, then it defaults to the
	 *            session user. Note: This parameter applies only to Web applications and is required by them only if the session_key is not specified. Facebook returns
	 *            an error if this parameter is passed by a desktop application.
	 * @param profileFbml
	 *            The FBML intended for the application profile box that appears on the Boxes tab on the user's profile.
	 * @param actionFbml
	 *            The FBML intended for the user's profile actions. A profile action is the link under the user's profile picture that allows a user to take an action
	 *            with your application. Note: This attribute is being deprecated when the new profile design launches in July 2008, as there are no third party profile
	 *            action links on the new profile.
	 * @param mobileFbml
	 *            The FBML intended for mobile devices.
	 * @param profileMain
	 *            The FBML intended for the narrow profile box on the Wall and Info tabs of the user's profile. Note: This attribute applies only to the new profile
	 *            design that launched July 2008.
	 * 
	 * @return a boolean indicating whether the FBML was successfully set
	 * 
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.setFBML">http://wiki.developers.facebook.com/index.php/Profile.setFBML</a>
	 */
	public boolean profile_setFBML( Long userId, String profileFbml, String actionFbml, String mobileFbml, String profileMain ) throws FacebookException;

	/**
	 * @see #profile_setFBML(Long, String, String, String, String)
	 * @deprecated trying to reduce superfluous methods, use {@link #profile_setFBML(Long, String, String, String, String)}
	 */
	@Deprecated
	public boolean profile_setFBML( Long userId, String profileFbml, String actionFbml, String mobileFbml ) throws FacebookException;

	/**
	 * Sets the FBML for a profile box on the logged-in user's profile.
	 * 
	 * @param fbmlMarkup
	 *            refer to the FBML documentation for a description of the markup and its role in various contexts
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.setFBML"> Developers wiki: Profile.setFbml</a>
	 * @deprecated trying to reduce superfluous methods, use {@link #profile_setFBML(Long, String, String, String, String)}
	 */
	@Deprecated
	public boolean profile_setProfileFBML( CharSequence fbmlMarkup ) throws FacebookException;

	/**
	 * Sets the FBML for profile actions for the logged-in user.
	 * 
	 * @param fbmlMarkup
	 *            refer to the FBML documentation for a description of the markup and its role in various contexts
	 * @return a boolean indicating whether the FBML was successfully set
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.setFBML"> Developers wiki: Profile.setFBML</a>
	 * @deprecated trying to reduce superfluous methods, use {@link #profile_setFBML(Long, String, String, String, String)}
	 */
	@Deprecated
	public boolean profile_setProfileActionFBML( CharSequence fbmlMarkup ) throws FacebookException;

	/**
	 * Sets the FBML for the logged-in user's profile on mobile devices.
	 * 
	 * @param fbmlMarkup
	 *            refer to the FBML documentation for a description of the markup and its role in various contexts
	 * @return a boolean indicating whether the FBML was successfully set
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.setFBML"> Developers wiki: Profile.setFBML</a>
	 * @deprecated trying to reduce superfluous methods, use {@link #profile_setFBML(Long, String, String, String, String)}
	 */
	@Deprecated
	public boolean profile_setMobileFBML( CharSequence fbmlMarkup ) throws FacebookException;

	/**
	 * Sets the FBML for a profile box on the user or page profile with ID <code>profileId</code>.
	 * 
	 * @param fbmlMarkup
	 *            refer to the FBML documentation for a description of the markup and its role in various contexts
	 * @param profileId
	 *            a page or user ID (null for the logged-in user)
	 * @return a boolean indicating whether the FBML was successfully set
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.setFBML"> Developers wiki: Profile.setFbml</a>
	 * @deprecated trying to reduce superfluous methods, use {@link #profile_setFBML(Long, String, String, String, String)}
	 */
	@Deprecated
	public boolean profile_setProfileFBML( CharSequence fbmlMarkup, Long profileId ) throws FacebookException;

	/**
	 * Sets the FBML for profile actions for the user or page profile with ID <code>profileId</code>.
	 * 
	 * @param fbmlMarkup
	 *            refer to the FBML documentation for a description of the markup and its role in various contexts
	 * @param profileId
	 *            a page or user ID (null for the logged-in user)
	 * @return a boolean indicating whether the FBML was successfully set
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.setFBML"> Developers wiki: Profile.setFBML</a>
	 * @deprecated trying to reduce superfluous methods, use {@link #profile_setFBML(Long, String, String, String, String)}
	 */
	@Deprecated
	public boolean profile_setProfileActionFBML( CharSequence fbmlMarkup, Long profileId ) throws FacebookException;

	/**
	 * Sets the FBML for the user or page profile with ID <code>profileId</code> on mobile devices.
	 * 
	 * @param fbmlMarkup
	 *            refer to the FBML documentation for a description of the markup and its role in various contexts
	 * @param profileId
	 *            a page or user ID (null for the logged-in user)
	 * @return a boolean indicating whether the FBML was successfully set
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.setFBML"> Developers wiki: Profile.setFBML</a>
	 * @deprecated trying to reduce superfluous methods, use {@link #profile_setFBML(Long, String, String, String, String)}
	 */
	@Deprecated
	public boolean profile_setMobileFBML( CharSequence fbmlMarkup, Long profileId ) throws FacebookException;

	/**
	 * Sets the FBML for the profile box and profile actions for the logged-in user. Refer to the FBML documentation for a description of the markup and its role in
	 * various contexts.
	 * 
	 * @param profileFbmlMarkup
	 *            the FBML for the profile box
	 * @param profileActionFbmlMarkup
	 *            the FBML for the profile actions
	 * @return a boolean indicating whether the FBML was successfully set
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.setFBML"> Developers wiki: Profile.setFBML</a>
	 * @deprecated trying to reduce superfluous methods, use {@link #profile_setFBML(Long, String, String, String, String)}
	 */
	@Deprecated
	public boolean profile_setFBML( CharSequence profileFbmlMarkup, CharSequence profileActionFbmlMarkup ) throws FacebookException;

	/**
	 * Sets the FBML for the profile box and profile actions for the user or page profile with ID <code>profileId</code>. Refer to the FBML documentation for a
	 * description of the markup and its role in various contexts.
	 * 
	 * @param profileFbmlMarkup
	 *            the FBML for the profile box
	 * @param profileActionFbmlMarkup
	 *            the FBML for the profile actions
	 * @param profileId
	 *            a page or user ID (null for the logged-in user)
	 * @return a boolean indicating whether the FBML was successfully set
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.setFBML"> Developers wiki: Profile.setFBML</a>
	 * @deprecated trying to reduce superfluous methods, use {@link #profile_setFBML(Long, String, String, String, String)}
	 */
	@Deprecated
	public boolean profile_setFBML( CharSequence profileFbmlMarkup, CharSequence profileActionFbmlMarkup, Long profileId ) throws FacebookException;

	/**
	 * Sets the FBML for the profile box, profile actions, and mobile devices for the user or page profile with ID <code>profileId</code>. Refer to the FBML
	 * documentation for a description of the markup and its role in various contexts.
	 * 
	 * @param profileFbmlMarkup
	 *            the FBML for the profile box
	 * @param profileActionFbmlMarkup
	 *            the FBML for the profile actions
	 * @param mobileFbmlMarkup
	 *            the FBML for mobile devices
	 * @param profileId
	 *            a page or user ID (null for the logged-in user)
	 * @return a boolean indicating whether the FBML was successfully set
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.setFBML"> Developers wiki: Profile.setFBML</a>
	 * @deprecated trying to reduce superfluous methods, use {@link #profile_setFBML(Long, String, String, String, String)}
	 */
	@Deprecated
	public boolean profile_setFBML( CharSequence profileFbmlMarkup, CharSequence profileActionFbmlMarkup, CharSequence mobileFbmlMarkup, Long profileId )
			throws FacebookException;

	/**
	 * Sets the FBML for the profile box, profile actions, and mobile devices for the current user. Refer to the FBML documentation for a description of the markup and
	 * its role in various contexts.
	 * 
	 * @param profileFbmlMarkup
	 *            the FBML for the profile box
	 * @param profileActionFbmlMarkup
	 *            the FBML for the profile actions
	 * @param mobileFbmlMarkup
	 *            the FBML for mobile devices
	 * @param profileId
	 *            a page or user ID (null for the logged-in user)
	 * @return a boolean indicating whether the FBML was successfully set
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.setFBML"> Developers wiki: Profile.setFBML</a>
	 * @deprecated trying to reduce superfluous methods, use {@link #profile_setFBML(Long, String, String, String, String)}
	 */
	@Deprecated
	public boolean profile_setFBML( CharSequence profileFbmlMarkup, CharSequence profileActionFbmlMarkup, CharSequence mobileFbmlMarkup ) throws FacebookException;

	/**
	 * Gets the FBML for the current user's profile box.
	 * 
	 * @return a T containing FBML markup
	 * 
	 * @see #profile_getFBML(int, Long)
	 */
	public T profile_getFBML() throws FacebookException;

	/**
	 * Gets the FBML for the user's profile box.
	 * 
	 * @param userId
	 *            The user whose profile FBML is to be fetched, or the page ID in case of a Page. If not specified, defaults to the session user.
	 * @return a T containing FBML markup
	 * 
	 * @see #profile_getFBML(int, Long)
	 */
	public T profile_getFBML( Long userId ) throws FacebookException;

	/**
	 * Gets the FBML for the current user's profile boxes.
	 * 
	 * @param type
	 *            The type of profile box to retrieve. Specify 1 for the original style (wide and narrow column boxes), 2 for profile_main box. (Default value is 1.)
	 * @return a T containing FBML markup
	 * 
	 * @see #profile_getFBML(int, Long)
	 */
	public T profile_getFBML( int type ) throws FacebookException;

	/**
	 * Gets the FBML for the user's profile boxes.
	 * 
	 * @param type
	 *            The type of profile box to retrieve. Specify 1 for the original style (wide and narrow column boxes), 2 for profile_main box. (Default value is 1.)
	 * @param userId
	 *            The user whose profile FBML is to be fetched, or the page ID in case of a Page. If not specified, defaults to the session user.
	 * @return a T containing FBML markup
	 * 
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Profile.getFBML">Profile.getFBML</a>
	 */
	public T profile_getFBML( int type, Long userId ) throws FacebookException;

	/**
	 * Recaches the referenced url.
	 * 
	 * @param url
	 *            string representing the URL to refresh
	 * @return boolean indicating whether the refresh succeeded
	 */
	public boolean fbml_refreshRefUrl( String url ) throws FacebookException;

	/**
	 * Recaches the referenced url.
	 * 
	 * @param url
	 *            the URL to refresh
	 * @return boolean indicating whether the refresh succeeded
	 */
	public boolean fbml_refreshRefUrl( URL url ) throws FacebookException;

	/**
	 * Recaches the image with the specified imageUrl.
	 * 
	 * @param imageUrl
	 *            String representing the image URL to refresh
	 * @return boolean indicating whether the refresh succeeded
	 */
	public boolean fbml_refreshImgSrc( String imageUrl ) throws FacebookException;

	/**
	 * Recaches the image with the specified imageUrl.
	 * 
	 * @param imageUrl
	 *            the image URL to refresh
	 * @return boolean indicating whether the refresh succeeded
	 */
	public boolean fbml_refreshImgSrc( URL imageUrl ) throws FacebookException;

	/**
	 * Publishes a Mini-Feed story describing an action taken by a user, and publishes aggregating News Feed stories to the friends of that user. Stories are identified
	 * as being combinable if they have matching templates and substituted values.
	 * 
	 * @param actorId
	 *            deprecated
	 * @param titleTemplate
	 *            markup (up to 60 chars, tags excluded) for the feed story's title section. Must include the token <code>{actor}</code>.
	 * @return whether the action story was successfully published; false in case of a permission error
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Feed.publishTemplatizedAction"> Developers Wiki: Feed.publishTemplatizedAction</a>
	 * @see <a href="http://developers.facebook.com/tools.php?feed"> Developers Resources: Feed Preview Console </a>
	 * @deprecated since 01/18/2008
	 */
	@Deprecated
	public boolean feed_publishTemplatizedAction( Long actorId, CharSequence titleTemplate ) throws FacebookException;

	/**
	 * Publishes a Mini-Feed story describing an action taken by the logged-in user, and publishes aggregating News Feed stories to their friends. Stories are identified
	 * as being combinable if they have matching templates and substituted values.
	 * 
	 * @param titleTemplate
	 *            markup (up to 60 chars, tags excluded) for the feed story's title section. Must include the token <code>{actor}</code>.
	 * @return whether the action story was successfully published; false in case of a permission error
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Feed.publishTemplatizedAction"> Developers Wiki: Feed.publishTemplatizedAction</a>
	 * @see <a href="http://developers.facebook.com/tools.php?feed"> Developers Resources: Feed Preview Console </a>
	 */
	@Deprecated
	public boolean feed_publishTemplatizedAction( CharSequence titleTemplate ) throws FacebookException;

	/**
	 * Publishes a Mini-Feed story describing an action taken by the logged-in user (or, if <code>pageActorId</code> is provided, page), and publishes aggregating News
	 * Feed stories to the user's friends/page's fans. Stories are identified as being combinable if they have matching templates and substituted values.
	 * 
	 * @param titleTemplate
	 *            markup (up to 60 chars, tags excluded) for the feed story's title section. Must include the token <code>{actor}</code>.
	 * @param pageActorId
	 *            (optional) the ID of the page into whose mini-feed the story is being published
	 * @return whether the action story was successfully published; false in case of a permission error
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Feed.publishTemplatizedAction"> Developers Wiki: Feed.publishTemplatizedAction</a>
	 * @see <a href="http://developers.facebook.com/tools.php?feed"> Developers Resources: Feed Preview Console </a>
	 */
	public boolean feed_publishTemplatizedAction( CharSequence titleTemplate, Long pageActorId ) throws FacebookException;

	/**
	 * Publishes a Mini-Feed story describing an action taken by the logged-in user (or, if <code>pageActorId</code> is provided, page), and publishes aggregating News
	 * Feed stories to the user's friends/page's fans. Stories are identified as being combinable if they have matching templates and substituted values.
	 * 
	 * @param titleTemplate
	 *            markup (up to 60 chars, tags excluded) for the feed story's title section. Must include the token <code>{actor}</code>.
	 * @param titleData
	 *            (optional) contains token-substitution mappings for tokens that appear in titleTemplate. Should not contain mappings for the <code>{actor}</code> or
	 *            <code>{target}</code> tokens. Required if tokens other than <code>{actor}</code> or <code>{target}</code> appear in the titleTemplate.
	 * @param bodyTemplate
	 *            (optional) markup to be displayed in the feed story's body section. can include tokens, of the form <code>{token}</code>, to be substituted using
	 *            bodyData.
	 * @param bodyData
	 *            (optional) contains token-substitution mappings for tokens that appear in bodyTemplate. Required if the bodyTemplate contains tokens other than
	 *            <code>{actor}</code> and <code>{target}</code>.
	 * @param bodyGeneral
	 *            (optional) additional body markup that is not aggregated. If multiple instances of this templated story are combined together, the markup in the
	 *            bodyGeneral of one of their stories may be displayed.
	 * @param targetIds
	 *            The user ids of friends of the actor, used for stories about a direct action between the actor and these targets of his/her action. Required if either
	 *            the titleTemplate or bodyTemplate includes the token <code>{target}</code>.
	 * @param images
	 *            (optional) additional body markup that is not aggregated. If multiple instances of this templated story are combined together, the markup in the
	 *            bodyGeneral of one of their stories may be displayed.
	 * @param pageActorId
	 *            (optional) the ID of the page into whose mini-feed the story is being published
	 * @return whether the action story was successfully published; false in case of a permission error
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Feed.publishTemplatizedAction"> Developers Wiki: Feed.publishTemplatizedAction</a>
	 * @see <a href="http://developers.facebook.com/tools.php?feed"> Developers Resources: Feed Preview Console </a>
	 */
	public boolean feed_publishTemplatizedAction( CharSequence titleTemplate, Map<String,CharSequence> titleData, CharSequence bodyTemplate,
			Map<String,CharSequence> bodyData, CharSequence bodyGeneral, Collection<Long> targetIds, Collection<? extends IPair<? extends Object,URL>> images,
			Long pageActorId ) throws FacebookException;

	/**
	 * Publishes a Mini-Feed story describing an action taken by a user, and publishes aggregating News Feed stories to the friends of that user. Stories are identified
	 * as being combinable if they have matching templates and substituted values.
	 * 
	 * @param actorId
	 *            the user into whose mini-feed the story is being published.
	 * @param titleTemplate
	 *            markup (up to 60 chars, tags excluded) for the feed story's title section. Must include the token <code>{actor}</code>.
	 * @param titleData
	 *            (optional) contains token-substitution mappings for tokens that appear in titleTemplate. Should not contain mappings for the <code>{actor}</code> or
	 *            <code>{target}</code> tokens. Required if tokens other than <code>{actor}</code> or <code>{target}</code> appear in the titleTemplate.
	 * @param bodyTemplate
	 *            (optional) markup to be displayed in the feed story's body section. can include tokens, of the form <code>{token}</code>, to be substituted using
	 *            bodyData.
	 * @param bodyData
	 *            (optional) contains token-substitution mappings for tokens that appear in bodyTemplate. Required if the bodyTemplate contains tokens other than
	 *            <code>{actor}</code> and <code>{target}</code>.
	 * @param bodyGeneral
	 *            (optional) additional body markup that is not aggregated. If multiple instances of this templated story are combined together, the markup in the
	 *            bodyGeneral of one of their stories may be displayed.
	 * @param targetIds
	 *            The user ids of friends of the actor, used for stories about a direct action between the actor and these targets of his/her action. Required if either
	 *            the titleTemplate or bodyTemplate includes the token <code>{target}</code>.
	 * @param images
	 *            (optional) additional body markup that is not aggregated. If multiple instances of this templated story are combined together, the markup in the
	 *            bodyGeneral of one of their stories may be displayed.
	 * @return whether the action story was successfully published; false in case of a permission error
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Feed.publishTemplatizedAction"> Developers Wiki: Feed.publishTemplatizedAction</a>
	 */
	@Deprecated
	public boolean feed_publishTemplatizedAction( Long actorId, CharSequence titleTemplate, Map<String,CharSequence> titleData, CharSequence bodyTemplate,
			Map<String,CharSequence> bodyData, CharSequence bodyGeneral, Collection<Long> targetIds, Collection<? extends IPair<? extends Object,URL>> images )
			throws FacebookException;

	/**
	 * Retrieves whether two users are friends.
	 * 
	 * @param userId1
	 * @param userId2
	 * @see http://wiki.developers.facebook.com/index.php/Friends.areFriends
	 */
	public T friends_areFriends( long userId1, long userId2 ) throws FacebookException;

	/**
	 * Retrieves whether pairs of users are friends. Returns whether the first user in <code>userIds1</code> is friends with the first user in <code>userIds2</code>,
	 * the second user in <code>userIds1</code> is friends with the second user in <code>userIds2</code>, etc.
	 * 
	 * @param userIds1
	 * @param userIds2
	 * @see http://wiki.developers.facebook.com/index.php/Friends.areFriends
	 */
	public T friends_areFriends( Collection<Long> userIds1, Collection<Long> userIds2 ) throws FacebookException;

	/**
	 * Retrieves the friends of the currently logged in user.
	 * 
	 * @see http://wiki.developers.facebook.com/index.php/Friends.get
	 */
	public T friends_get() throws FacebookException;

	/**
	 * Retrieves the friends uid.
	 * 
	 * @see http://wiki.developers.facebook.com/index.php/Friends.get
	 */
	public T friends_get( Long uid ) throws FacebookException;

	/**
	 * Retrieves the friends of the currently logged in user that are members of the friends list with ID <code>friendListId</code>.
	 * 
	 * @param friendListId
	 *            the friend list for which friends should be fetched. if <code>null</code>, all friends will be retrieved.
	 * @see http://wiki.developers.facebook.com/index.php/Friends.get
	 */
	public T friends_getList( Long friendListId ) throws FacebookException;

	/**
	 * Retrieves the friend lists of the currently logged in user.
	 * 
	 * @see http://wiki.developers.facebook.com/index.php/Friends.getLists
	 */
	public T friends_getLists() throws FacebookException;

	/**
	 * Retrieves the friends of the currently logged in user, who are also users of the calling application.
	 * 
	 * @return array of friends
	 */
	public T friends_getAppUsers() throws FacebookException;

	/**
	 * Retrieves the requested info fields for the requested set of users.
	 * 
	 * @param userIds
	 *            a collection of user IDs for which to fetch info
	 * @param fields
	 *            a set of ProfileFields
	 * @return a T consisting of a list of users, with each user element containing the requested fields.
	 * @see http://wiki.developers.facebook.com/index.php/Users.getInfo
	 */
	public T users_getInfo( Iterable<Long> userIds, Collection<ProfileField> fields ) throws FacebookException;

	/**
	 * Retrieves the requested info fields for the requested set of users.
	 * 
	 * @param userIds
	 *            a collection of user IDs for which to fetch info
	 * @param fields
	 *            a set of strings describing the info fields desired, such as "last_name", "sex"
	 * @return a T consisting of a list of users, with each user element containing the requested fields.
	 * @see http://wiki.developers.facebook.com/index.php/Users.getInfo
	 */
	public T users_getInfo( Iterable<Long> userIds, Set<CharSequence> fields ) throws FacebookException;

	/**
	 * Returns an array of user-specific information for each user identifier passed, limited by the view of the current user. The information you can get from this call
	 * is limited to: uid, first_name, last_name, name, timezone, birthday, sex, affiliations (regional type only)
	 * 
	 * @param userIds
	 *            a collection of user IDs for which to fetch info
	 * @param fields
	 *            a set of ProfileFields
	 * @return a T consisting of a list of users, with each user element containing the requested fields.
	 * @see http://wiki.developers.facebook.com/index.php/Users.getStandardInfo
	 */
	public T users_getStandardInfo( Iterable<Long> userIds, Collection<ProfileField> fields ) throws FacebookException;

	/**
	 * Returns an array of user-specific information for each user identifier passed, limited by the view of the current user. The information you can get from this call
	 * is limited to: uid, first_name, last_name, name, timezone, birthday, sex, affiliations (regional type only)
	 * 
	 * @param userIds
	 *            a collection of user IDs for which to fetch info
	 * @param fields
	 *            a set of strings describing the info fields desired, such as "last_name", "sex"
	 * @return a T consisting of a list of users, with each user element containing the requested fields.
	 * @see http://wiki.developers.facebook.com/index.php/Users.getStandardInfo
	 */
	public T users_getStandardInfo( Iterable<Long> userIds, Set<CharSequence> fields ) throws FacebookException;

	/**
	 * Retrieves the user ID of the user logged in to this API session
	 * 
	 * @return the Facebook user ID of the logged-in user
	 */
	public long users_getLoggedInUser() throws FacebookException;

	/**
	 * Retrieves an indicator of whether the logged-in user has added the application associated with the _apiKey.
	 * 
	 * @return boolean indicating whether the user has added the app
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Users.isAppAdded">Users.isAppAdded</a>
	 * @deprecated
	 */
	@Deprecated
	public boolean users_isAppAdded() throws FacebookException;

	/**
	 * Retrieves an indicator of whether the specified user has added the application associated with the _apiKey.
	 * 
	 * @param userId
	 *            the if of the user to check for.
	 * @return boolean indicating whether the user has added the app
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Users.isAppAdded">Users.isAppAdded</a>
	 * @deprecated
	 */
	@Deprecated
	public boolean users_isAppAdded( Long userId ) throws FacebookException;

	/**
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Users.isAppUser">Users.isAppUser</a>
	 * @see #users_isAppUser(Long)
	 */
	public boolean users_isAppUser() throws FacebookException;

	/**
	 * Returns whether the user (either the session user or user specified by uid) has authorized the calling application.
	 * 
	 * @param userId
	 *            The user ID of the user who may have authorized the application. If this parameter is not specified, then it defaults to the session user. Note: This
	 *            parameter applies only to Web applications and is required by them only if the session_key is not specified. Facebook ignores this parameter if it is
	 *            passed by a desktop application.
	 * 
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Users.isAppUser">Users.isAppUser</a>
	 */
	public boolean users_isAppUser( Long userId ) throws FacebookException;

	/**
	 * Sets the logged-in user's Facebook status. Requires the status_update extended permission.
	 * 
	 * @return whether the status was successfully set
	 * @see #users_hasAppPermission
	 * @see FacebookExtendedPerm#STATUS_UPDATE
	 * @see http://wiki.developers.facebook.com/index.php/Users.setStatus
	 */
	public boolean users_setStatus( String status ) throws FacebookException;

	/**
	 * Sets the spedified user's Facebook status. Requires the status_update extended permission.
	 * 
	 * @return whether the status was successfully set
	 * @see #users_hasAppPermission
	 * @see FacebookExtendedPerm#STATUS_UPDATE
	 * @see http://wiki.developers.facebook.com/index.php/Users.setStatus
	 */
	public boolean users_setStatus( String status, Long userId ) throws FacebookException;

	/**
	 * Set the user's profile status message. This requires that the user has granted the application the 'status_update' permission, otherwise the call will return an
	 * error. You can use 'users_hasAppPermission' to check to see if the user has granted your app the abbility to update their status.
	 * 
	 * @param newStatus
	 *            the new status message to set.
	 * @param clear
	 *            whether or not to clear the old status message.
	 * 
	 * @return true if the call succeeds false otherwise
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 * @see http://wiki.developers.facebook.com/index.php/Users.setStatus
	 */
	public boolean users_setStatus( String newStatus, boolean clear ) throws FacebookException;

	/**
	 * Set the user's profile status message. This requires that the user has granted the application the 'status_update' permission, otherwise the call will return an
	 * error. You can use 'users_hasAppPermission' to check to see if the user has granted your app the abbility to update their status.
	 * 
	 * @param newStatus
	 *            the new status message to set.
	 * @param clear
	 *            whether or not to clear the old status message.
	 * @param userId
	 *            the id of the user to set the status for.
	 * 
	 * @return true if the call succeeds false otherwise
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 * @see http://wiki.developers.facebook.com/index.php/Users.setStatus
	 */
	public boolean users_setStatus( String newStatus, boolean clear, Long userId ) throws FacebookException;

	/**
	 * Set the user's profile status message. This requires that the user has granted the application the 'status_update' permission, otherwise the call will return an
	 * error. You can use 'users_hasAppPermission' to check to see if the user has granted your app the abbility to update their status
	 * 
	 * @param newStatus
	 *            the new status message to set.
	 * @param clear
	 *            whether or not to clear the old status message.
	 * @param statusIncludesVerb
	 *            set to true if you do not want the Facebook Platform to automatically prepend "is " to your status message set to false if you want the "is " prepended
	 *            (default behavior)
	 * 
	 * @return true if the call succeeds false otherwise
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 * @see http://wiki.developers.facebook.com/index.php/Users.setStatus
	 */
	public boolean users_setStatus( String newStatus, boolean clear, boolean statusIncludesVerb ) throws FacebookException;

	/**
	 * Set the user's profile status message. This requires that the user has granted the application the 'status_update' permission, otherwise the call will return an
	 * error. You can use 'users_hasAppPermission' to check to see if the user has granted your app the abbility to update their status
	 * 
	 * @param newStatus
	 *            the new status message to set.
	 * @param clear
	 *            whether or not to clear the old status message.
	 * @param statusIncludesVerb
	 *            set to true if you do not want the Facebook Platform to automatically prepend "is " to your status message set to false if you want the "is " prepended
	 *            (default behavior)
	 * @param userId
	 *            the id of the user to set the status for.
	 * 
	 * @return true if the call succeeds false otherwise
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 * @see http://wiki.developers.facebook.com/index.php/Users.setStatus
	 */
	public boolean users_setStatus( String newStatus, boolean clear, boolean statusIncludesVerb, Long userId ) throws FacebookException;

	/**
	 * Clears the logged-in user's Facebook status. Requires the status_update extended permission.
	 * 
	 * @return whether the status was successfully cleared
	 * @see #users_hasAppPermission
	 * @see FacebookExtendedPerm#STATUS_UPDATE
	 * @see http://wiki.developers.facebook.com/index.php/Users.setStatus
	 */
	public boolean users_clearStatus() throws FacebookException;

	/**
	 * Used to retrieve photo objects using the search parameters (one or more of the parameters must be provided).
	 * 
	 * @param subjId
	 *            retrieve from photos associated with this user (optional).
	 * @param albumId
	 *            retrieve from photos from this album (optional)
	 * @param photoIds
	 *            retrieve from this list of photos (optional)
	 * 
	 * @return an T of photo objects.
	 */
	public T photos_get( Long subjId, Long albumId, Iterable<Long> photoIds ) throws FacebookException;

	/**
	 * Used to retrieve photo objects using the search parameters (one or more of the parameters must be provided).
	 * 
	 * @param subjId
	 *            retrieve from photos associated with this user (optional).
	 * @param photoIds
	 *            retrieve from this list of photos (optional)
	 * @return an T of photo objects.
	 * @see #photos_get(Long, Long, Collection)
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.get"> Developers Wiki: Photos.get</a>
	 */
	public T photos_get( Long subjId, Iterable<Long> photoIds ) throws FacebookException;

	/**
	 * Used to retrieve photo objects using the search parameters (one or more of the parameters must be provided).
	 * 
	 * @param subjId
	 *            retrieve from photos associated with this user (optional).
	 * @param albumId
	 *            retrieve from photos from this album (optional)
	 * @return an T of photo objects.
	 * @see #photos_get(Long, Long, Collection)
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.get"> Developers Wiki: Photos.get</a>
	 */
	public T photos_get( Long subjId, Long albumId ) throws FacebookException;

	/**
	 * Used to retrieve photo objects using the search parameters (one or more of the parameters must be provided).
	 * 
	 * @param photoIds
	 *            retrieve from this list of photos (optional)
	 * @return an T of photo objects.
	 * @see #photos_get(Long, Long, Collection)
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.get"> Developers Wiki: Photos.get</a>
	 */
	public T photos_get( Iterable<Long> photoIds ) throws FacebookException;

	/**
	 * Used to retrieve photo objects using the search parameters (one or more of the parameters must be provided).
	 * 
	 * @param subjId
	 *            retrieve from photos associated with this user (optional).
	 * @return an T of photo objects.
	 * @see #photos_get(Long, Long, Collection)
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.get"> Developers Wiki: Photos.get</a>
	 */
	public T photos_get( Long subjId ) throws FacebookException;

	/**
	 * Retrieves album metadata. Pass a user id and/or a list of album ids to specify the albums to be retrieved (at least one must be provided)
	 * 
	 * @param userId
	 *            (optional) the id of the albums' owner (optional)
	 * @param albumIds
	 *            (optional) the ids of albums whose metadata is to be retrieved
	 * @return album objects
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.getAlbums"> Developers Wiki: Photos.getAlbums</a>
	 */
	public T photos_getAlbums( Long userId, Iterable<Long> albumIds ) throws FacebookException;

	/**
	 * Retrieves album metadata for albums owned by a user.
	 * 
	 * @param userId
	 *            (optional) the id of the albums' owner (optional)
	 * @return album objects
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.getAlbums"> Developers Wiki: Photos.getAlbums</a>
	 */
	public T photos_getAlbums( Long userId ) throws FacebookException;

	/**
	 * Retrieves album metadata for a list of album IDs.
	 * 
	 * @param albumIds
	 *            the ids of albums whose metadata is to be retrieved
	 * @return album objects
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.getAlbums"> Developers Wiki: Photos.getAlbums</a>
	 */
	public T photos_getAlbums( Iterable<Long> albumIds ) throws FacebookException;

	/**
	 * Retrieves the tags for the given set of photos.
	 * 
	 * @param photoIds
	 *            The list of photos from which to extract photo tags.
	 * @return the created album
	 */
	public T photos_getTags( Iterable<Long> photoIds ) throws FacebookException;

	/**
	 * Creates an album.
	 * 
	 * @param albumName
	 *            The list of photos from which to extract photo tags.
	 * @return the created album
	 */
	public T photos_createAlbum( String albumName ) throws FacebookException;

	/**
	 * Creates an album.
	 * 
	 * @param name
	 *            The album name.
	 * @param location
	 *            The album location (optional).
	 * @param description
	 *            The album description (optional).
	 * @return an array of photo objects.
	 */
	public T photos_createAlbum( String name, String description, String location ) throws FacebookException;

	/**
	 * Creates an album.
	 * 
	 * @param albumName
	 *            The list of photos from which to extract photo tags.
	 * @param userId
	 *            the id of the user creating the album.
	 * @return the created album
	 */
	public T photos_createAlbum( String albumName, Long userId ) throws FacebookException;

	/**
	 * Creates an album.
	 * 
	 * @param name
	 *            The album name.
	 * @param location
	 *            The album location (optional).
	 * @param description
	 *            The album description (optional).
	 * @param userId
	 *            the id of the user creating the album.
	 * @return an array of photo objects.
	 */
	public T photos_createAlbum( String name, String description, String location, Long userId ) throws FacebookException;

	/**
	 * Adds several tags to a photo.
	 * 
	 * @param photoId
	 *            The photo id of the photo to be tagged.
	 * @param tags
	 *            A list of PhotoTags.
	 * @return a list of booleans indicating whether the tag was successfully added.
	 */
	public T photos_addTags( Long photoId, Iterable<PhotoTag> tags ) throws FacebookException;

	/**
	 * Adds a tag to a photo.
	 * 
	 * @param photoId
	 *            The photo id of the photo to be tagged.
	 * @param xPct
	 *            The horizontal position of the tag, as a percentage from 0 to 100, from the left of the photo.
	 * @param yPct
	 *            The vertical position of the tag, as a percentage from 0 to 100, from the top of the photo.
	 * @param taggedUserId
	 *            The list of photos from which to extract photo tags.
	 * @return whether the tag was successfully added.
	 */
	public boolean photos_addTag( Long photoId, Long taggedUserId, Double xPct, Double yPct ) throws FacebookException;

	/**
	 * Adds a tag to a photo.
	 * 
	 * @param photoId
	 *            The photo id of the photo to be tagged.
	 * @param xPct
	 *            The horizontal position of the tag, as a percentage from 0 to 100, from the left of the photo.
	 * @param yPct
	 *            The list of photos from which to extract photo tags.
	 * @param tagText
	 *            The text of the tag.
	 * @return whether the tag was successfully added.
	 */
	public boolean photos_addTag( Long photoId, CharSequence tagText, Double xPct, Double yPct ) throws FacebookException;

	/**
	 * Adds a tag to a photo.
	 * 
	 * @param photoId
	 *            The photo id of the photo to be tagged.
	 * @param xPct
	 *            The horizontal position of the tag, as a percentage from 0 to 100, from the left of the photo.
	 * @param yPct
	 *            The vertical position of the tag, as a percentage from 0 to 100, from the top of the photo.
	 * @param taggedUserId
	 *            The list of photos from which to extract photo tags.
	 * @param userId
	 *            the user tagging the photo.
	 * @return whether the tag was successfully added.
	 */
	public boolean photos_addTag( Long photoId, Long taggedUserId, Double xPct, Double yPct, Long userId ) throws FacebookException;

	/**
	 * Adds a tag to a photo.
	 * 
	 * @param photoId
	 *            The photo id of the photo to be tagged.
	 * @param xPct
	 *            The horizontal position of the tag, as a percentage from 0 to 100, from the left of the photo.
	 * @param yPct
	 *            The list of photos from which to extract photo tags.
	 * @param tagText
	 *            The text of the tag.
	 * @param userId
	 *            the user tagging the photo.
	 * @return whether the tag was successfully added.
	 */
	public boolean photos_addTag( Long photoId, CharSequence tagText, Double xPct, Double yPct, Long userId ) throws FacebookException;

	/**
	 * Uploads a photo to Facebook.
	 * 
	 * @param photo
	 *            an image file
	 * @return a T with the standard Facebook photo information
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.upload"> Developers wiki: Photos.upload</a>
	 */
	public T photos_upload( File photo ) throws FacebookException;

	/**
	 * Uploads a photo to Facebook.
	 * 
	 * @param photo
	 *            an image file
	 * @param caption
	 *            a description of the image contents
	 * @return a T with the standard Facebook photo information
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.upload"> Developers wiki: Photos.upload</a>
	 */
	public T photos_upload( File photo, String caption ) throws FacebookException;

	/**
	 * Uploads a photo to Facebook.
	 * 
	 * @param photo
	 *            an image file
	 * @param albumId
	 *            the album into which the photo should be uploaded
	 * @return a T with the standard Facebook photo information
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.upload"> Developers wiki: Photos.upload</a>
	 */
	public T photos_upload( File photo, Long albumId ) throws FacebookException;

	/**
	 * Uploads a photo to Facebook.
	 * 
	 * @param photo
	 *            an image file
	 * @param caption
	 *            a description of the image contents
	 * @param albumId
	 *            the album into which the photo should be uploaded
	 * @return a T with the standard Facebook photo information
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.upload"> Developers wiki: Photos.upload</a>
	 */
	public T photos_upload( File photo, String caption, Long albumId ) throws FacebookException;

	/**
	 * Uploads a photo to Facebook.
	 * 
	 * @param photo
	 *            an image file
	 * @param userId
	 *            the id of the user uploading the photo
	 * @return a T with the standard Facebook photo information
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.upload"> Developers wiki: Photos.upload</a>
	 */
	public T photos_upload( Long userId, File photo ) throws FacebookException;

	/**
	 * Uploads a photo to Facebook.
	 * 
	 * @param photo
	 *            an image file
	 * @param caption
	 *            a description of the image contents
	 * @param userId
	 *            the id of the user uploading the photo
	 * @return a T with the standard Facebook photo information
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.upload"> Developers wiki: Photos.upload</a>
	 */
	public T photos_upload( Long userId, File photo, String caption ) throws FacebookException;

	/**
	 * Uploads a photo to Facebook.
	 * 
	 * @param photo
	 *            an image file
	 * @param albumId
	 *            the album into which the photo should be uploaded
	 * @param userId
	 *            the id of the user uploading the photo
	 * @return a T with the standard Facebook photo information
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.upload"> Developers wiki: Photos.upload</a>
	 */
	public T photos_upload( Long userId, File photo, Long albumId ) throws FacebookException;

	/**
	 * Uploads a photo to Facebook.
	 * 
	 * @param photo
	 *            an image file
	 * @param caption
	 *            a description of the image contents
	 * @param albumId
	 *            the album into which the photo should be uploaded
	 * @param userId
	 *            the id of the user uploading the photo
	 * @return a T with the standard Facebook photo information
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.upload"> Developers wiki: Photos.upload</a>
	 */
	public T photos_upload( Long userId, File photo, String caption, Long albumId ) throws FacebookException;

	/**
	 * Uploads a photo to Facebook.
	 * 
	 * @param userId
	 *            the id of the user uploading the photo
	 * @param caption
	 *            a description of the image contents
	 * @param albumId
	 *            the album into which the photo should be uploaded
	 * @param fileName
	 * @param fileStream
	 * 
	 * @return a T with the standard Facebook photo information
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.upload"> Developers wiki: Photos.upload</a>
	 */
	public T photos_upload( Long userId, String caption, Long albumId, String fileName, InputStream fileStream ) throws FacebookException;

	/**
	 * Retrieves the groups associated with a user
	 * 
	 * @param userId
	 *            Optional: User associated with groups. A null parameter will default to the session user.
	 * @param groupIds
	 *            Optional: group ids to query. A null parameter will get all groups for the user.
	 * @return array of groups
	 */
	public T groups_get( Long userId, Collection<Long> groupIds ) throws FacebookException;

	/**
	 * Retrieves the membership list of a group
	 * 
	 * @param groupId
	 *            the group id
	 * @return a T containing four membership lists of 'members', 'admins', 'officers', and 'not_replied'
	 */
	public T groups_getMembers( Number groupId ) throws FacebookException;

	/**
	 * Retrieves the results of a Facebook Query Language query
	 * 
	 * @param query :
	 *            the FQL query statement
	 * @return varies depending on the FQL query
	 */
	public T fql_query( CharSequence query ) throws FacebookException;

	/**
	 * Retrieves the outstanding notifications for the session user.
	 * 
	 * @return a T containing notification count pairs for 'messages', 'pokes' and 'shares', a uid list of 'friend_requests', a gid list of 'group_invites', and an eid
	 *         list of 'event_invites'
	 */
	public T notifications_get() throws FacebookException;

	/**
	 * Send a notification message to the specified users.
	 * 
	 * @param recipientIds
	 *            the user ids to which the message is to be sent
	 * @param notification
	 *            the FBML to display on the notifications page
	 * @param email
	 *            the FBML to send to the specified users via email, or null if no email should be sent
	 * @return a URL, possibly null, to which the user should be redirected to finalize the sending of the email
	 * 
	 * @deprecated notifications.send can no longer be used for sending e-mails, use notifications.sendEmail intead when sending e-mail, or the alternate version of
	 *             notifications.send if all you want to send is a notification.
	 */
	@Deprecated
	public URL notifications_send( Collection<Long> recipientIds, CharSequence notification, CharSequence email ) throws FacebookException;

	/**
	 * Send a notification message to the specified users.
	 * 
	 * @param recipientIds
	 *            the user ids to which the message is to be sent.
	 * @param notification
	 *            the FBML to display on the notifications page.
	 */
	public void notifications_send( Collection<Long> recipientIds, CharSequence notification ) throws FacebookException;

	/**
	 * Call this function and store the result, using it to generate the appropriate login url and then to retrieve the session information.
	 * 
	 * @return an authentication token
	 * @see http://wiki.developers.facebook.com/index.php/Auth.createToken
	 */
	public String auth_createToken() throws FacebookException;

	/**
	 * Call this function to retrieve the session information after your user has logged in.
	 * 
	 * @param authToken
	 *            the token returned by auth_createToken or passed back to your callback_url.
	 * @see http://wiki.developers.facebook.com/index.php/Auth.getSession
	 */
	public String auth_getSession( String authToken ) throws FacebookException;

	/**
	 * Call this function to get the user ID.
	 * 
	 * @return The ID of the current session's user, or -1 if none.
	 */
	@Deprecated
	public long auth_getUserId( String authToken ) throws FacebookException;

	public String getCacheSessionKey();

	public Long getCacheUserId();

	public Long getCacheSessionExpires();

	public String getCacheSessionSecret();

	/**
	 * Create a marketplace listing. The create_listing extended permission is required.
	 * 
	 * @param showOnProfile
	 *            whether
	 * @return the id of the created listing
	 * @see #users_hasAppPermission
	 * @see FacebookExtendedPerm#MARKETPLACE
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.createListing"> Developers Wiki: marketplace.createListing</a>
	 * 
	 * @deprecated provided for legacy support only. Please use the version that takes a MarketListing instead.
	 */
	@Deprecated
	public Long marketplace_createListing( Boolean showOnProfile, MarketplaceListing attrs ) throws FacebookException;

	/**
	 * Modify a marketplace listing. The create_listing extended permission is required.
	 * 
	 * @return the id of the edited listing
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.createListing"> Developers Wiki: marketplace.createListing</a>
	 * 
	 * @deprecated provided for legacy support only. Please use the version that takes a MarketListing instead.
	 */
	@Deprecated
	public Long marketplace_editListing( Long listingId, Boolean showOnProfile, MarketplaceListing attrs ) throws FacebookException;

	/**
	 * Remove a marketplace listing. The create_listing extended permission is required.
	 * 
	 * @param listingId
	 *            the listing to be removed
	 * @return boolean indicating whether the listing was removed
	 * @see #users_hasAppPermission
	 * @see FacebookExtendedPerm#MARKETPLACE
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.removeListing"> Developers Wiki: marketplace.removeListing</a>
	 */
	public boolean marketplace_removeListing( Long listingId ) throws FacebookException;

	/**
	 * Remove a marketplace listing. The create_listing extended permission is required.
	 * 
	 * @param listingId
	 *            the listing to be removed
	 * @param userId
	 *            the id of the user removing the listing
	 * @return boolean indicating whether the listing was removed
	 * @see #users_hasAppPermission
	 * @see FacebookExtendedPerm#MARKETPLACE
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.removeListing"> Developers Wiki: marketplace.removeListing</a>
	 */
	public boolean marketplace_removeListing( Long listingId, Long userId ) throws FacebookException;

	/**
	 * Remove a marketplace listing. The create_listing extended permission is required.
	 * 
	 * @param listingId
	 *            the listing to be removed
	 * @param status
	 *            MARKETPLACE_STATUS_DEFAULT, MARKETPLACE_STATUS_SUCCESS, or MARKETPLACE_STATUS_NOT_SUCCESS
	 * @return boolean indicating whether the listing was removed
	 * @see #users_hasAppPermission
	 * @see FacebookExtendedPerm#MARKETPLACE
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.removeListing"> Developers Wiki: marketplace.removeListing</a>
	 * 
	 * @deprecated provided for legacy support only. Please use the version that takes a MarketListingStatus instead.
	 */
	@Deprecated
	public boolean marketplace_removeListing( Long listingId, CharSequence status ) throws FacebookException;

	/**
	 * Get the categories available in marketplace.
	 * 
	 * @return a T listing the marketplace categories
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.getCategories"> Developers Wiki: marketplace.getCategories</a>
	 */
	public List<String> marketplace_getCategories() throws FacebookException;

	/**
	 * Get the subcategories available for a category.
	 * 
	 * @param category
	 *            a category, e.g. "HOUSING"
	 * @return a T listing the marketplace sub-categories
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.getSubCategories"> Developers Wiki: marketplace.getSubCategories</a>
	 */
	public T marketplace_getSubCategories( CharSequence category ) throws FacebookException;

	/**
	 * Fetch marketplace listings, filtered by listing IDs and/or the posting users' IDs.
	 * 
	 * @param listingIds
	 *            listing identifiers (required if uids is null/empty)
	 * @param userIds
	 *            posting user identifiers (required if listingIds is null/empty)
	 * @return a T of marketplace listings
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.getListings"> Developers Wiki: marketplace.getListings</a>
	 */
	public T marketplace_getListings( Collection<Long> listingIds, Collection<Long> userIds ) throws FacebookException;

	/**
	 * Search for marketplace listings, optionally by category, subcategory, and/or query string.
	 * 
	 * @param category
	 *            the category of listings desired (optional except if subcategory is provided)
	 * @param subCategory
	 *            the subcategory of listings desired (optional)
	 * @param query
	 *            a query string (optional)
	 * @return a T of marketplace listings
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.search"> Developers Wiki: marketplace.search</a>
	 * 
	 * @deprecated provided for legacy support only. Please use the alternate version instead.
	 */
	@Deprecated
	public T marketplace_search( CharSequence category, CharSequence subCategory, CharSequence query ) throws FacebookException;

	/**
	 * Used to retrieve photo objects using the search parameters (one or more of the parameters must be provided).
	 * 
	 * @param albumId
	 *            retrieve from photos from this album (optional)
	 * @param photoIds
	 *            retrieve from this list of photos (optional)
	 * @return an T of photo objects.
	 * @see #photos_get(Integer, Long, Collection)
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.get"> Developers Wiki: Photos.get</a>
	 */
	public T photos_getByAlbum( Long albumId, Iterable<Long> photoIds ) throws FacebookException;

	/**
	 * Used to retrieve photo objects using the search parameters (one or more of the parameters must be provided).
	 * 
	 * @param albumId
	 *            retrieve from photos from this album (optional)
	 * @return an T of photo objects.
	 * @see #photos_get(Integer, Long, Collection)
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Photos.get"> Developers Wiki: Photos.get</a>
	 */
	public T photos_getByAlbum( Long albumId ) throws FacebookException;

	/**
	 * Get the categories available in marketplace.
	 * 
	 * @return a T listing the marketplace categories
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.getCategories"> Developers Wiki: marketplace.getCategories</a>
	 * 
	 * @deprecated use the version that returns a List<String> instead.
	 */
	@Deprecated
	public T marketplace_getCategoriesObject() throws FacebookException;

	/**
	 * Returns a string representation for the last API response recieved from Facebook, exactly as sent by the API server.
	 * 
	 * Note that calling this method consumes the data held in the internal buffer, and thus it may only be called once per API call.
	 * 
	 * @return a String representation of the last API response sent by Facebook
	 */
	public String getRawResponse();

	/**
	 * Returns a JAXB object of the type that corresponds to the last API call made on the client. Each Facebook Platform API call that returns a Document object has a
	 * JAXB response object associated with it. The naming convention is generally intuitive. For example, if you invoke the 'user_getInfo' API call, the associated JAXB
	 * response object is 'UsersGetInfoResponse'.<br />
	 * <br />
	 * An example of how to use this method:<br />
	 * <br />
	 * FacebookRestClient client = new FacebookRestClient("apiKey", "secretKey", "sessionId");<br />
	 * client.friends_get();<br />
	 * FriendsGetResponse response = (FriendsGetResponse)client.getResponsePOJO();<br />
	 * List<Long> friends = response.getUid(); <br />
	 * <br />
	 * This is particularly useful in the case of API calls that return a Document object, as working with the JAXB response object is generally much simple than trying
	 * to walk/parse the DOM by hand.<br />
	 * <br />
	 * This method can be safely called multiple times, though note that it will only return the response-object corresponding to the most recent Facebook Platform API
	 * call made.<br />
	 * <br />
	 * Note that you must cast the return value of this method to the correct type in order to do anything useful with it.
	 * 
	 * @return a JAXB POJO ("Plain Old Java Object") of the type that corresponds to the last API call made on the client. Note that you must cast this object to its
	 *         proper type before you will be able to do anything useful with it.
	 */
	public Object getResponsePOJO();

	/**
	 * Publishes a templatized action for the current user. The action will appear in their minifeed, and may appear in their friends' newsfeeds depending upon a number
	 * of different factors. When a template match exists between multiple distinct users (like "Bob recommends Bizou" and "Sally recommends Bizou"), the feed entries may
	 * be combined in the newfeed (to something like "Bob and sally recommend Bizou"). This happens automatically, and *only* if the template match between the two feed
	 * entries is identical.<br />
	 * <br />
	 * Feed entries are not aggregated for a single user (so "Bob recommends Bizou" and "Bob recommends Le Charm" *will not* become "Bob recommends Bizou and Le Charm").<br />
	 * <br />
	 * If the user's action involves one or more of their friends, list them in the 'targetIds' parameter. For example, if you have "Bob says hi to Sally and Susie", and
	 * Sally's UID is 1, and Susie's UID is 2, then pass a 'targetIds' paramters of "1,2". If you pass this parameter, you can use the "{target}" token in your templates.
	 * Probably it also makes it more likely that Sally and Susie will see the feed entry in their newsfeed, relative to any other friends Bob might have. It may be a
	 * good idea to always send a list of all the user's friends, and avoid using the "{target}" token, to maximize distribution of the story through the newsfeed.<br />
	 * <br />
	 * The only strictly required parameter is 'titleTemplate', which must contain the "{actor}" token somewhere inside of it. All other parameters, options, and tokens
	 * are optional, and my be set to null if being omitted.<br />
	 * <br />
	 * Not that stories will only be aggregated if *all* templates match and *all* template parameters match, so if two entries have the same templateTitle and titleData,
	 * but a different bodyTemplate, they will not aggregate. Probably it's better to use bodyGeneral instead of bodyTemplate, for the extra flexibility it provides.<br />
	 * <br />
	 * <br />
	 * Note that this method is replacing 'feed_publishActionOfUser', which has been deprecated by Facebook. For specific details, visit
	 * http://wiki.developers.facebook.com/index.php/Feed.publishTemplatizedAction
	 * 
	 * 
	 * @param action
	 *            a TemplatizedAction instance that represents the feed data to publish
	 * 
	 * @return a Document representing the XML response returned from the Facebook API server.
	 * 
	 * @throws FacebookException
	 *             if any number of bad things happen
	 */
	public boolean feed_PublishTemplatizedAction( TemplatizedAction action ) throws FacebookException;

	/**
	 * Lookup a single preference value for the current user.
	 * 
	 * @param prefId
	 *            the id of the preference to lookup. This should be an integer value from 0-200.
	 * 
	 * @return The value of that preference, or null if it is not yet set.
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 */
	public String data_getUserPreference( int prefId ) throws FacebookException;

	/**
	 * Get a map containing all preference values set for the current user.
	 * 
	 * @return a map of preference values, keyed by preference id. The map will contain all preferences that have been set for the current user. If there are no
	 *         preferences currently set, the map will be empty. The map returned will never be null.
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 */
	public T data_getUserPreferences() throws FacebookException;

	/**
	 * Set a user-preference value. The value can be any string up to 127 characters in length, while the preference id can only be an integer between 0 and 200. Any
	 * preference set applies only to the current user of the application.
	 * 
	 * To clear a user-preference, specify null as the value parameter. The values of "0" and "" will be stored as user-preferences with a literal value of "0" and ""
	 * respectively.
	 * 
	 * @param prefId
	 *            the id of the preference to set, an integer between 0 and 200.
	 * @param value
	 *            the value to store, a String of up to 127 characters in length.
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 */
	public void data_setUserPreference( int prefId, String value ) throws FacebookException;

	/**
	 * Set multiple user-preferences values. The values can be strings up to 127 characters in length, while the preference id can only be an integer between 0 and 200.
	 * Any preferences set apply only to the current user of the application.
	 * 
	 * To clear a user-preference, specify null as its value in the map. The values of "0" and "" will be stored as user-preferences with a literal value of "0" and ""
	 * respectively.
	 * 
	 * @param values
	 *            the values to store, specified in a map. The keys should be preference-id values from 0-200, and the values should be strings of up to 127 characters in
	 *            length.
	 * @param replace
	 *            set to true if you want to remove any pre-existing preferences before writing the new ones set to false if you want the new preferences to be merged
	 *            with any pre-existing preferences
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 */
	public void data_setUserPreferences( Map<Integer,String> values, boolean replace ) throws FacebookException;

	/**
	 * An object type is like a "table" in SQL terminology, or a "class" in object-oriented programming concepts. Each object type has a unique human-readable "name" that
	 * will be used to identify itself throughout the API. Each object type also has a list of properties that one has to define individually. Each property is like a
	 * "column" in an SQL table, or a "data member" in an object class.
	 * 
	 * @param name
	 * @throws FacebookException
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Data.createObjectType"> Developers Wiki: Data.createObjectType</a>
	 */
	public void data_createObjectType( String name ) throws FacebookException;

	/**
	 * Remove a previously defined object type. This will also delete ALL objects of this type. This deletion is NOT reversible.
	 * 
	 * @param objectType
	 * @throws FacebookException
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Data.createObjectType"> Developers Wiki: Data.dropObjectType</a>
	 */
	public void data_dropObjectType( String objectType ) throws FacebookException;

	public void data_renameObjectType( String name, String newName ) throws FacebookException;

	public void data_defineObjectProperty( String objectType, String propertyName, PropertyType propertyType ) throws FacebookException;

	public void data_undefineObjectProperty( String objectType, String propertyName ) throws FacebookException;

	public void data_renameObjectProperty( String objectType, String propertyName, String newPropertyName ) throws FacebookException;

	public T data_getObjectTypes() throws FacebookException;

	public T data_getObjectType( String objectType ) throws FacebookException;



	/**
	 * @see #users_hasAppPermission(Permission,Long)
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Users.hasAppPermission">Users.hasAppPermission</a>
	 */
	public boolean users_hasAppPermission( Permission perm ) throws FacebookException;

	/**
	 * Checks whether the user has opted in to an extended application permission.
	 * 
	 * @param perm
	 *            String identifier for the extended permission that is being checked for. Must be one of email, offline_access, status_update, photo_upload,
	 *            create_listing, create_event, rsvp_event, sms.
	 * @param userId
	 *            The user ID of the user whose permissions you are checking. If this parameter is not specified, then it defaults to the session user. Note: This
	 *            parameter applies only to Web applications and is required by them only if the session_key is not specified. Facebook ignores this parameter if it is
	 *            passed by a desktop application.
	 * 
	 * @return true if the user has granted the application the specified permission false otherwise
	 * 
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Users.hasAppPermission">Users.hasAppPermission</a>
	 */
	public boolean users_hasAppPermission( Permission perm, Long userId ) throws FacebookException;

	/**
	 * Publishes a templatized action for the current user. The action will appear in their minifeed, and may appear in their friends' newsfeeds depending upon a number
	 * of different factors. When a template match exists between multiple distinct users (like "Bob recommends Bizou" and "Sally recommends Bizou"), the feed entries may
	 * be combined in the newfeed (to something like "Bob and sally recommend Bizou"). This happens automatically, and *only* if the template match between the two feed
	 * entries is identical.<br />
	 * <br />
	 * Feed entries are not aggregated for a single user (so "Bob recommends Bizou" and "Bob recommends Le Charm" *will not* become "Bob recommends Bizou and Le Charm").<br />
	 * <br />
	 * If the user's action involves one or more of their friends, list them in the 'targetIds' parameter. For example, if you have "Bob says hi to Sally and Susie", and
	 * Sally's UID is 1, and Susie's UID is 2, then pass a 'targetIds' paramters of "1,2". If you pass this parameter, you can use the "{target}" token in your templates.
	 * Probably it also makes it more likely that Sally and Susie will see the feed entry in their newsfeed, relative to any other friends Bob might have. It may be a
	 * good idea to always send a list of all the user's friends, and avoid using the "{target}" token, to maximize distribution of the story through the newsfeed.<br />
	 * <br />
	 * The only strictly required parameter is 'titleTemplate', which must contain the "{actor}" token somewhere inside of it. All other parameters, options, and tokens
	 * are optional, and my be set to null if being omitted.<br />
	 * <br />
	 * Not that stories will only be aggregated if *all* templates match and *all* template parameters match, so if two entries have the same templateTitle and titleData,
	 * but a different bodyTemplate, they will not aggregate. Probably it's better to use bodyGeneral instead of bodyTemplate, for the extra flexibility it provides.<br />
	 * <br />
	 * <br />
	 * Note that this method is replacing 'feed_publishActionOfUser', which has been deprecated by Facebook. For specific details, visit
	 * http://wiki.developers.facebook.com/index.php/Feed.publishTemplatizedAction
	 * 
	 * 
	 * @param titleTemplate
	 *            the template for the title of the feed entry, this must contain the "(actor}" token. Any other tokens are optional, i.e. "{actor} recommends {place}".
	 * @param titleData
	 *            JSON-formatted values for any tokens used in titleTemplate, with the exception of "{actor}" and "{target}", which Facebook populates automatically, i.e.
	 *            "{place: "<a href='http://www.bizou.com'>Bizou</a>"}".
	 * @param bodyTemplate
	 *            the template for the body of the feed entry, works the same as 'titleTemplate', but is not required to contain the "{actor}" token.
	 * @param bodyData
	 *            works the same as titleData
	 * @param bodyGeneral
	 *            non-templatized content for the body, may contain markup, may not contain tokens.
	 * @param pictures
	 *            a list of up to 4 images to display, with optional hyperlinks for each one.
	 * @param targetIds
	 *            a comma-seperated list of the UID's of any friend(s) who are involved in this feed action (if there are any), this specifies the value of the "{target}"
	 *            token. If you use this token in any of your templates, you must specify a value for this parameter.
	 * 
	 * @return a Document representing the XML response returned from the Facebook API server.
	 * 
	 * @throws FacebookException
	 *             if any number of bad things happen
	 */
	@Deprecated
	public boolean feed_publishTemplatizedAction( String titleTemplate, String titleData, String bodyTemplate, String bodyData, String bodyGeneral,
			Collection<? extends IPair<? extends Object,URL>> pictures, String targetIds ) throws FacebookException;

	/**
	 * Associates the specified FBML markup with the specified handle/id. The markup can then be referenced using the fb:ref FBML tag, to allow a given snippet to be
	 * reused easily across multiple users, and also to allow the application to update the fbml for multiple users more easily without having to make a seperate call for
	 * each user, by just changing the FBML markup that is associated with the handle/id.
	 * 
	 * This method cannot be called by desktop apps.
	 * 
	 * @param handle
	 *            the id to associate the specified markup with. Put this in fb:ref FBML tags to reference your markup.
	 * @param markup
	 *            the FBML markup to store.
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 */
	public boolean fbml_setRefHandle( String handle, String markup ) throws FacebookException;

	/**
	 * Publishes a Mini-Feed story describing an action taken by a user, and publishes aggregating News Feed stories to the friends of that user. Stories are identified
	 * as being combinable if they have matching templates and substituted values.
	 * 
	 * @param actorId
	 *            the user into whose mini-feed the story is being published.
	 * @param titleTemplate
	 *            markup (up to 60 chars, tags excluded) for the feed story's title section. Must include the token <code>{actor}</code>.
	 * @param titleData
	 *            (optional) contains token-substitution mappings for tokens that appear in titleTemplate. Should not contain mappings for the <code>{actor}</code> or
	 *            <code>{target}</code> tokens. Required if tokens other than <code>{actor}</code> or <code>{target}</code> appear in the titleTemplate.
	 * @param bodyTemplate
	 *            (optional) markup to be displayed in the feed story's body section. can include tokens, of the form <code>{token}</code>, to be substituted using
	 *            bodyData.
	 * @param bodyData
	 *            (optional) contains token-substitution mappings for tokens that appear in bodyTemplate. Required if the bodyTemplate contains tokens other than
	 *            <code>{actor}</code> and <code>{target}</code>.
	 * @param bodyGeneral
	 *            (optional) additional body markup that is not aggregated. If multiple instances of this templated story are combined together, the markup in the
	 *            bodyGeneral of one of their stories may be displayed.
	 * @param targetIds
	 *            The user ids of friends of the actor, used for stories about a direct action between the actor and these targets of his/her action. Required if either
	 *            the titleTemplate or bodyTemplate includes the token <code>{target}</code>.
	 * @param images
	 *            (optional) additional body markup that is not aggregated. If multiple instances of this templated story are combined together, the markup in the
	 *            bodyGeneral of one of their stories may be displayed.
	 * @return whether the action story was successfully published; false in case of a permission error
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Feed.publishTemplatizedAction"> Developers Wiki: Feed.publishTemplatizedAction</a>
	 * @see <a href="http://developers.facebook.com/tools.php?feed"> Developers Resources: Feed Preview Console </a>
	 * 
	 * @deprecated use the version that specified the actorId as a Long instead. UID's *are not ever to be* expressed as Integers.
	 */
	@Deprecated
	public boolean feed_publishTemplatizedAction( Integer actorId, CharSequence titleTemplate, Map<String,CharSequence> titleData, CharSequence bodyTemplate,
			Map<String,CharSequence> bodyData, CharSequence bodyGeneral, Collection<Long> targetIds, Collection<? extends IPair<? extends Object,URL>> images )
			throws FacebookException;

	/**
	 * Create a new marketplace listing, or modify an existing one.
	 * 
	 * @param listingId
	 *            the id of the listing to modify, set to 0 (or null) to create a new listing.
	 * @param showOnProfile
	 *            set to true to show the listing on the user's profile (Facebook appears to ignore this setting).
	 * @param attributes
	 *            JSON-encoded attributes for this listing.
	 * 
	 * @return the id of the listing created (or modified).
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 */
	public Long marketplace_createListing( Long listingId, boolean showOnProfile, String attributes ) throws FacebookException;

	/**
	 * Create a new marketplace listing, or modify an existing one.
	 * 
	 * @param listingId
	 *            the id of the listing to modify, set to 0 (or null) to create a new listing.
	 * @param showOnProfile
	 *            set to true to show the listing on the user's profile, set to false to prevent the listing from being shown on the profile.
	 * @param listing
	 *            the listing to publish.
	 * 
	 * @return the id of the listing created (or modified).
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 */
	public Long marketplace_createListing( Long listingId, boolean showOnProfile, MarketListing listing ) throws FacebookException;

	/**
	 * Create a new marketplace listing.
	 * 
	 * @param showOnProfile
	 *            set to true to show the listing on the user's profile, set to false to prevent the listing from being shown on the profile.
	 * @param listing
	 *            the listing to publish.
	 * 
	 * @return the id of the listing created (or modified).
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 */
	public Long marketplace_createListing( boolean showOnProfile, MarketListing listing ) throws FacebookException;

	/**
	 * Create a new marketplace listing, or modify an existing one.
	 * 
	 * @param listingId
	 *            the id of the listing to modify, set to 0 (or null) to create a new listing.
	 * @param showOnProfile
	 *            set to true to show the listing on the user's profile (Facebook appears to ignore this setting).
	 * @param attributes
	 *            JSON-encoded attributes for this listing.
	 * @param userId
	 *            the id of the user to create the listing for.
	 * 
	 * @return the id of the listing created (or modified).
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 */
	public Long marketplace_createListing( Long listingId, boolean showOnProfile, String attributes, Long userId ) throws FacebookException;

	/**
	 * Create a new marketplace listing, or modify an existing one.
	 * 
	 * @param listingId
	 *            the id of the listing to modify, set to 0 (or null) to create a new listing.
	 * @param showOnProfile
	 *            set to true to show the listing on the user's profile, set to false to prevent the listing from being shown on the profile.
	 * @param listing
	 *            the listing to publish.
	 * @param userId
	 *            the id of the user to create the listing for.
	 * 
	 * @return the id of the listing created (or modified).
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 */
	public Long marketplace_createListing( Long listingId, boolean showOnProfile, MarketListing listing, Long userId ) throws FacebookException;

	/**
	 * Create a new marketplace listing.
	 * 
	 * @param showOnProfile
	 *            set to true to show the listing on the user's profile, set to false to prevent the listing from being shown on the profile.
	 * @param listing
	 *            the listing to publish.
	 * @param userId
	 *            the id of the user to create the listing for.
	 * 
	 * @return the id of the listing created (or modified).
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 */
	public Long marketplace_createListing( boolean showOnProfile, MarketListing listing, Long userId ) throws FacebookException;

	/**
	 * Return a list of all valid Marketplace subcategories.
	 * 
	 * @return a list of marketplace subcategories allowed by Facebook.
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 */
	public List<String> marketplace_getSubCategories() throws FacebookException;

	/**
	 * Retrieve listings from the marketplace. The listings can be filtered by listing-id or user-id (or both).
	 * 
	 * @param listingIds
	 *            the ids of listings to filter by, only listings matching the specified ids will be returned.
	 * @param uids
	 *            the ids of users to filter by, only listings submitted by those users will be returned.
	 * 
	 * @return A list of marketplace listings that meet the specified filter criteria.
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 */
	public List<Listing> marketplace_getListings( List<Long> listingIds, List<Long> uids ) throws FacebookException;

	/**
	 * Search the marketplace listings by category, subcategory, and keyword.
	 * 
	 * @param category
	 *            the category to search in, optional (unless subcategory is specified).
	 * @param subcategory
	 *            the subcategory to search in, optional.
	 * @param searchTerm
	 *            the keyword to search for, optional.
	 * 
	 * @return a list of marketplace entries that match the specified search parameters.
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 */
	public List<Listing> marketplace_search( MarketListingCategory category, MarketListingSubcategory subcategory, String searchTerm ) throws FacebookException;

	/**
	 * Remove a listing from the marketplace by id.
	 * 
	 * @param listingId
	 *            the id of the listing to remove.
	 * @param status
	 *            the status to apply when removing the listing. Should be one of MarketListingStatus.SUCCESS or MarketListingStatus.NOT_SUCCESS.
	 * 
	 * @return true if the listing was successfully removed false otherwise
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 */
	public boolean marketplace_removeListing( Long listingId, MarketListingStatus status ) throws FacebookException;

	/**
	 * Remove a listing from the marketplace by id.
	 * 
	 * @param listingId
	 *            the id of the listing to remove.
	 * @param status
	 *            the status to apply when removing the listing. Should be one of MarketListingStatus.SUCCESS or MarketListingStatus.NOT_SUCCESS.
	 * @param userId
	 *            the id of the user removing the listing.
	 * 
	 * @return true if the listing was successfully removed false otherwise
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 */
	public boolean marketplace_removeListing( Long listingId, MarketListingStatus status, Long userId ) throws FacebookException;

	/**
	 * Modify a marketplace listing
	 * 
	 * @param listingId
	 *            identifies the listing to be modified
	 * @param showOnProfile
	 *            whether the listing can be shown on the user's profile
	 * @param attrs
	 *            the properties of the listing
	 * @return the id of the edited listing
	 * @see MarketplaceListing
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Marketplace.createListing"> Developers Wiki: marketplace.createListing</a>
	 */
	public Long marketplace_editListing( Long listingId, Boolean showOnProfile, MarketListing attrs ) throws FacebookException;

	/**
	 * Retrieves the requested profile fields for the Facebook Pages with the given <code>pageIds</code>. Can be called for pages that have added the application
	 * without establishing a session.
	 * 
	 * @param pageIds
	 *            the page IDs
	 * @param fields
	 *            a set of page profile fields
	 * @return a T consisting of a list of pages, with each page element containing the requested fields.
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.getInfo"> Developers Wiki: Pages.getInfo</a>
	 */
	public T pages_getInfo( Collection<Long> pageIds, EnumSet<PageProfileField> fields ) throws FacebookException;

	/**
	 * Retrieves the requested profile fields for the Facebook Pages with the given <code>pageIds</code>. Can be called for pages that have added the application
	 * without establishing a session.
	 * 
	 * @param pageIds
	 *            the page IDs
	 * @param fields
	 *            a set of page profile fields
	 * @return a T consisting of a list of pages, with each page element containing the requested fields.
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.getInfo"> Developers Wiki: Pages.getInfo</a>
	 */
	public T pages_getInfo( Collection<Long> pageIds, Set<CharSequence> fields ) throws FacebookException;

	/**
	 * Retrieves the requested profile fields for the Facebook Pages of the user with the given <code>userId</code>.
	 * 
	 * @param userId
	 *            the ID of a user about whose pages to fetch info
	 * @param fields
	 *            a set of PageProfileFields
	 * @return a T consisting of a list of pages, with each page element containing the requested fields.
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.getInfo"> Developers Wiki: Pages.getInfo</a>
	 */
	public T pages_getInfo( Long userId, EnumSet<PageProfileField> fields ) throws FacebookException;

	/**
	 * Retrieves the requested profile fields for the Facebook Pages of the user with the given <code>userId</code>.
	 * 
	 * @param userId
	 *            the ID of a user about whose pages to fetch info
	 * @param fields
	 *            a set of page profile fields
	 * @return a T consisting of a list of pages, with each page element containing the requested fields.
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.getInfo"> Developers Wiki: Pages.getInfo</a>
	 */
	public T pages_getInfo( Long userId, Set<CharSequence> fields ) throws FacebookException;

	/**
	 * Checks whether a page has added the application
	 * 
	 * @param pageId
	 *            the ID of the page
	 * @return true if the page has added the application
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.isAppAdded"> Developers Wiki: Pages.isAppAdded</a>
	 */
	public boolean pages_isAppAdded( Long pageId ) throws FacebookException;

	/**
	 * Checks whether a user is a fan of the page with the given <code>pageId</code>.
	 * 
	 * @param pageId
	 *            the ID of the page
	 * @param userId
	 *            the ID of the user (defaults to the logged-in user if null)
	 * @return true if the user is a fan of the page
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.isFan"> Developers Wiki: Pages.isFan</a>
	 */
	public boolean pages_isFan( Long pageId, Long userId ) throws FacebookException;

	/**
	 * Checks whether the logged-in user is a fan of the page with the given <code>pageId</code>.
	 * 
	 * @param pageId
	 *            the ID of the page
	 * @return true if the logged-in user is a fan of the page
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.isFan"> Developers Wiki: Pages.isFan</a>
	 */
	public boolean pages_isFan( Long pageId ) throws FacebookException;

	/**
	 * Checks whether the logged-in user for this session is an admin of the page with the given <code>pageId</code>.
	 * 
	 * @param pageId
	 *            the ID of the page
	 * @return true if the logged-in user is an admin
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Pages.isAdmin"> Developers Wiki: Pages.isAdmin</a>
	 */
	public boolean pages_isAdmin( Long pageId ) throws FacebookException;

	/**
	 * Send an e-mail to the currently logged-in user. The e-mail content can be specified as either plaintext or FBML. In either case, only a limited subset of markup is
	 * supported (only tags that result in text and links are allowed).
	 * 
	 * You must include at least one of either the fbml or email parameters, but you do not ever need to specify both at once (the other can be null, or ""). If you
	 * specify both a text version and a fbml version of your e-mail, the text version will be used.
	 * 
	 * @param subject
	 *            the subject of the email message.
	 * @param email
	 *            a plaintext version of the email to send.
	 * @param fbml
	 *            an FBML version of the email to send, the fbml parameter is a stripped-down set of FBML that allows only tags that result in text, links and linebreaks.
	 * 
	 * @return a list of user-ids specifying which users were successfully emailed.
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 */
	public T notifications_sendEmailToCurrentUser( String subject, String email, String fbml ) throws FacebookException;

	/**
	 * Send an e-mail to a set of app-users. You can only e-mail users who have already added your application. The e-mail content can be specified as either plaintext or
	 * FBML. In either case, only a limited subset of markup is supported (only tags that result in text and links are allowed).
	 * 
	 * You must include at least one of either the fbml or email parameters, but you do not ever need to specify both at once (the other can be null, or ""). If you
	 * specify both a text version and a fbml version of your e-mail, the text version will be used.
	 * 
	 * @param recipients
	 *            the uid's of the users to send to.
	 * @param subject
	 *            the subject of the email message.
	 * @param email
	 *            a plaintext version of the email to send.
	 * @param fbml
	 *            an FBML version of the email to send, the fbml parameter is a stripped-down set of FBML that allows only tags that result in text, links and linebreaks.
	 * 
	 * @return a list of user-ids specifying which users were successfully emailed.
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 */
	public T notifications_sendEmail( Collection<Long> recipients, CharSequence subject, CharSequence email, CharSequence fbml ) throws FacebookException;

	/**
	 * Send an e-mail to the currently logged-in user. The e-mail must be specified as plaintext, and can contain a limited subset of HTML tags (specifically, only tags
	 * that result in text and links).
	 * 
	 * @param subject
	 *            the subject of the email message.
	 * @param email
	 *            a plaintext version of the email to send.
	 * 
	 * @return a list of user-ids specifying which users were successfully emailed.
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 */
	public T notifications_sendTextEmailToCurrentUser( String subject, String email ) throws FacebookException;

	/**
	 * Send an e-mail to a set of app-users. You can only e-mail users who have already added your application. The e-mail content can be specified as either plaintext or
	 * FBML. In either case, only a limited subset of markup is supported (only tags that result in text and links are allowed).
	 * 
	 * @param recipients
	 *            the uid's of the users to send to.
	 * @param subject
	 *            the subject of the email message.
	 * @param email
	 *            a plaintext version of the email to send.
	 * 
	 * @return a list of user-ids specifying which users were successfully emailed.
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 */
	public T notifications_sendTextEmail( Collection<Long> recipients, String subject, String email ) throws FacebookException;

	/**
	 * Send an e-mail to the currently logged-in user. The e-mail must be specified as fbml, and can contain a limited subset of FBML tags (specifically, only tags that
	 * result in text and links).
	 * 
	 * @param subject
	 *            the subject of the email message.
	 * @param fbml
	 *            the FBML version of the email to send, the fbml parameter is a stripped-down set of FBML that allows only tags that result in text, links and
	 *            linebreaks.
	 * 
	 * @return a list of user-ids specifying which users were successfully emailed.
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 */
	public T notifications_sendFbmlEmailToCurrentUser( String subject, String fbml ) throws FacebookException;

	/**
	 * Send an e-mail to a set of app-users. You can only e-mail users who have already added your application. The e-mail content can be specified as either plaintext or
	 * FBML. In either case, only a limited subset of markup is supported (only tags that result in text and links are allowed).
	 * 
	 * @param recipients
	 *            the uid's of the users to send to.
	 * @param subject
	 *            the subject of the email message.
	 * @param fbml
	 *            the FBML version of the email to send, the fbml parameter is a stripped-down set of FBML that allows only tags that result in text, links and
	 *            linebreaks.
	 * 
	 * @return a list of user-ids specifying which users were successfully emailed.
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 */
	public T notifications_sendFbmlEmail( Collection<Long> recipients, String subject, String fbml ) throws FacebookException;

	/**
	 * Send a notification message to the logged-in user.
	 * 
	 * @param notification
	 *            the FBML to be displayed on the notifications page; only a stripped-down set of FBML tags that result in text and links is allowed
	 * @return a URL, possibly null, to which the user should be redirected to finalize the sending of the email
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Notifications.send"> Developers Wiki: notifications.send</a>
	 */
	public void notifications_send( CharSequence notification ) throws FacebookException;

	/**
	 * Sends a notification email to the specified users, who must have added your application. You can send five (5) emails to a user per day. Requires a session key for
	 * desktop applications, which may only send email to the person whose session it is. This method does not require a session for Web applications.
	 * 
	 * @param recipientIds
	 *            up to 100 user ids to which the message is to be sent
	 * @param subject
	 *            the subject of the notification email (optional)
	 * @param fbml
	 *            markup to be sent to the specified users via email; only a stripped-down set of FBML that allows only tags that result in text, links and linebreaks is
	 *            allowed
	 * @return a comma-separated list of the IDs of the users to whom the email was successfully sent
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Notifications.send"> Developers Wiki: notifications.sendEmail</a>
	 * 
	 * @deprecated provided for legacy support only, please use one of the alternate notifications_sendEmail calls.
	 */
	@Deprecated
	public String notifications_sendEmail( Collection<Long> recipientIds, CharSequence subject, CharSequence fbml ) throws FacebookException;

	/**
	 * Sends a notification email to the specified users, who must have added your application. You can send five (5) emails to a user per day. Requires a session key for
	 * desktop applications, which may only send email to the person whose session it is. This method does not require a session for Web applications.
	 * 
	 * @param recipientIds
	 *            up to 100 user ids to which the message is to be sent
	 * @param subject
	 *            the subject of the notification email (optional)
	 * @param text
	 *            the plain text to send to the specified users via email
	 * @return a comma-separated list of the IDs of the users to whom the email was successfully sent
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Notifications.sendEmail"> Developers Wiki: notifications.sendEmail</a>
	 * 
	 * @deprecated provided for legacy support only, please use one of the alternate notifications_sendEmail calls.
	 */
	@Deprecated
	public String notifications_sendEmailPlain( Collection<Long> recipientIds, CharSequence subject, CharSequence text ) throws FacebookException;

	/**
	 * Sends a notification email to the specified users, who must have added your application. You can send five (5) emails to a user per day. Requires a session key for
	 * desktop applications, which may only send email to the person whose session it is. This method does not require a session for Web applications. Either
	 * <code>fbml</code> or <code>text</code> must be specified.
	 * 
	 * @param recipientIds
	 *            up to 100 user ids to which the message is to be sent
	 * @param subject
	 *            the subject of the notification email (optional)
	 * @param fbml
	 *            markup to be sent to the specified users via email; only a stripped-down set of FBML tags that result in text, links and linebreaks is allowed
	 * @param text
	 *            the plain text to send to the specified users via email
	 * @return a comma-separated list of the IDs of the users to whom the email was successfully sent
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Notifications.sendEmail"> Developers Wiki: notifications.sendEmail</a>
	 * 
	 * @deprecated provided for legacy support only, please use one of the alternate notifications_sendEmail calls.
	 */
	@Deprecated
	public String notifications_sendEmailStr( Collection<Long> recipientIds, CharSequence subject, CharSequence fbml, CharSequence text ) throws FacebookException;

	/**
	 * Set application properties. The properties are used by Facebook to describe the configuration of your application.
	 * 
	 * This method cannot be called by desktop apps.
	 * 
	 * @param properties
	 *            a Map containing the properties to set.
	 * 
	 * @return true if the properties are set successfully false otherwise
	 * 
	 * @throws FacebookException
	 */
	public boolean admin_setAppProperties( Map<ApplicationProperty,String> properties ) throws FacebookException;

	/**
	 * Retrieve application properties. The properties are used by Facebook to describe the configuration of your application.
	 * 
	 * This method cannot be called by desktop apps.
	 * 
	 * @param properties
	 *            a collection indicating the properties you are interested in retrieving.
	 * 
	 * @return a JSONObject that maps ApplicationProperty names to their corresponding values.
	 * 
	 * @throws FacebookException
	 * 
	 * @deprecated use admin_getAppPropertiesMap() instead
	 */
	@Deprecated
	public JSONObject admin_getAppProperties( Collection<ApplicationProperty> properties ) throws FacebookException;

	/**
	 * Retrieve application properties. The properties are used by Facebook to describe the configuration of your application.
	 * 
	 * This method cannot be called by desktop apps.
	 * 
	 * @param properties
	 *            a collection indicating the properties you are interested in retrieving.
	 * 
	 * @return a mapping of ApplicationProperty's to the corresponding values that are set for those properties. Properties are represented as strings, so properties that
	 *         are of boolean type will have a value of "true" when true, and "false" when false. The properties returned will never be null, an unset property is
	 *         represented by an empty string.
	 * 
	 * @throws FacebookException
	 */
	public Map<ApplicationProperty,String> admin_getAppPropertiesMap( Collection<ApplicationProperty> properties ) throws FacebookException;

	/**
	 * Retrieve application properties. The properties are used by Facebook to describe the configuration of your application.
	 * 
	 * This method cannot be called by desktop apps.
	 * 
	 * @param properties
	 *            a collection indicating the properties you are interested in retrieving.
	 * 
	 * @return a JSON-encoded string containing the properties. It is your responsibility to parse the string. Details can be found at
	 *         http://wiki.developers.facebook.com/index.php/Admin.getAppProperties
	 * 
	 * @throws FacebookException
	 */
	public String admin_getAppPropertiesAsString( Collection<ApplicationProperty> properties ) throws FacebookException;

	/**
	 * Get all cookies for the currently logged-in user.
	 * 
	 * @return all cookies for the current user.
	 * 
	 * @throws FacebookException
	 */
	public T data_getCookies() throws FacebookException;

	/**
	 * Get all cookies for the specified user.
	 * 
	 * @param userId
	 *            the id of the user to get the cookies for.
	 * 
	 * @return all cookies for the specified user.
	 * 
	 * @throws FacebookException
	 */
	public T data_getCookies( Long userId ) throws FacebookException;

	/**
	 * Get a specific cookie for the currently logged-in user.
	 * 
	 * @param name
	 *            the name of the cookie to retrieve.
	 * 
	 * @return the specified cookie for the current user.
	 * 
	 * @throws FacebookException
	 */
	public T data_getCookies( String name ) throws FacebookException;

	/**
	 * Get a specific cookie for the specified user.
	 * 
	 * @param userId
	 *            the id of the user to get the cookies for.
	 * @param name
	 *            the name of the cookie to retrieve.
	 * 
	 * @return the specified cookie for the specified user.
	 * 
	 * @throws FacebookException
	 */
	public T data_getCookies( Long userId, CharSequence name ) throws FacebookException;

	/**
	 * Set a cookie for the current user. It will use the default expiry (never), and the default path ("/").
	 * 
	 * @param name
	 *            the name of the cookie to set
	 * @param value
	 *            the value of the cookie
	 * 
	 * @return true if the cookie is set successfully, false otherwise.
	 * 
	 * @throws FacebookException
	 */
	public boolean data_setCookie( String name, String value ) throws FacebookException;

	/**
	 * Set a cookie for the current user, under the specified path. It will use the default expiry (never).
	 * 
	 * @param name
	 *            the name of the cookie to set
	 * @param value
	 *            the value of the cookie
	 * @param path
	 *            the path relative to the application's callback URL, with which the cookie should be associated. (default is "/")
	 * 
	 * @return true if the cookie is set successfully, false otherwise.
	 * 
	 * @throws FacebookException
	 */
	public boolean data_setCookie( String name, String value, String path ) throws FacebookException;

	/**
	 * Set a cookie for the specified user. The cookie will use the default expiry (never) and the default path ("/").
	 * 
	 * @param userId
	 *            the id of the user to set the cookie for.
	 * @param name
	 *            the name of the cookie to set
	 * @param value
	 *            the value of the cookie
	 * 
	 * @return true if the cookie is set successfully, false otherwise.
	 * 
	 * @throws FacebookException
	 */
	public boolean data_setCookie( Long userId, CharSequence name, CharSequence value ) throws FacebookException;

	/**
	 * Set a cookie for the specified user, with the specified path. The cookie will use the default expiry (never).
	 * 
	 * @param userId
	 *            the id of the user to set the cookie for.
	 * @param name
	 *            the name of the cookie to set
	 * @param value
	 *            the value of the cookie
	 * @param path
	 *            the path relative to the application's callback URL, with which the cookie should be associated. (default is "/")
	 * 
	 * @return true if the cookie is set successfully, false otherwise.
	 * 
	 * @throws FacebookException
	 */
	public boolean data_setCookie( Long userId, CharSequence name, CharSequence value, CharSequence path ) throws FacebookException;

	/**
	 * Set a cookie for the current user, with the specified expiration date. It will use the default path ("/").
	 * 
	 * @param name
	 *            the name of the cookie to set
	 * @param value
	 *            the value of the cookie
	 * @param expires
	 *            the timestamp at which the cookie expires
	 * 
	 * @return true if the cookie is set successfully, false otherwise.
	 * 
	 * @throws FacebookException
	 */
	public boolean data_setCookie( String name, String value, Long expires ) throws FacebookException;

	/**
	 * Set a cookie for the current user, with the specified expiration date and path.
	 * 
	 * @param name
	 *            the name of the cookie to set
	 * @param value
	 *            the value of the cookie
	 * @param expires
	 *            the timestamp at which the cookie expires
	 * @param path
	 *            the path relative to the application's callback URL, with which the cookie should be associated. (default is "/")
	 * 
	 * @return true if the cookie is set successfully, false otherwise.
	 * 
	 * @throws FacebookException
	 */
	public boolean data_setCookie( String name, String value, Long expires, String path ) throws FacebookException;

	/**
	 * Set a cookie for the specified user, with the specified expiration date. The cookie will use the default path ("/").
	 * 
	 * @param userId
	 *            the id of the user to set the cookie for.
	 * @param name
	 *            the name of the cookie to set
	 * @param value
	 *            the value of the cookie
	 * @param expires
	 *            the timestamp at which the cookie expires
	 * 
	 * @return true if the cookie is set successfully, false otherwise.
	 * 
	 * @throws FacebookException
	 */
	public boolean data_setCookie( Long userId, CharSequence name, CharSequence value, Long expires ) throws FacebookException;

	/**
	 * Set a cookie for the specified user, with the specified expiration date and path.
	 * 
	 * @param userId
	 *            the id of the user to set the cookie for.
	 * @param name
	 *            the name of the cookie to set
	 * @param value
	 *            the value of the cookie
	 * @param expires
	 *            the timestamp at which the cookie expires
	 * @param path
	 *            the path relative to the application's callback URL, with which the cookie should be associated. (default is "/")
	 * 
	 * @return true if the cookie is set successfully, false otherwise.
	 * 
	 * @throws FacebookException
	 */
	public boolean data_setCookie( Long userId, CharSequence name, CharSequence value, Long expires, CharSequence path ) throws FacebookException;

	/**
	 * Create object in Data Store
	 * 
	 * @param objectType
	 *            Specifies which type of new object to create.
	 * @param properties
	 *            Optional - Name-value pairs of properties this new object has
	 * @throws FacebookException
	 * @return 64-bit integer: Numeric identifier (fbid) of newly created object.
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Data.createObject"> Developers Wiki: Data.createObject</a>
	 */
	public long data_createObject( String objectType, Map<String,String> properties ) throws FacebookException;

	/**
	 * Update properties of an existing object in Data Store
	 * 
	 * @param objectId
	 *            Numeric identifier (fbid) of the object to modify.
	 * @param properties
	 *            Name-value pairs of new properties.
	 * @param replace
	 *            True if replace all existing properties; false to merge into existing ones.
	 * @throws FacebookException
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Data.updateObject"> Developers Wiki: Data.updateObject</a>
	 */
	public void data_updateObject( long objectId, Map<String,String> properties, boolean replace ) throws FacebookException;

	/**
	 * Delete object in Data Store
	 * 
	 * @param objectId
	 *            Numeric identifier (fbid) of the object to delete.
	 * @throws FacebookException
	 * @see #data_deleteObjects
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Data.deleteObject"> Developers Wiki: Data.deleteObject</a>
	 */
	public void data_deleteObject( long objectId ) throws FacebookException;

	/**
	 * Delete multiple objects in Data Store
	 * 
	 * WARNING: This method seems to fail when it comes across the first object that it can't find. It may be more reliable to iterate through your list of objects to
	 * delete and call deleteObject individually (although, of course, less efficient).
	 * 
	 * @param objectIds
	 *            A list of 64-bit integers that are numeric identifiers (fbids) of objects to delete.
	 * @throws FacebookException
	 * @see #data_deleteObject
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Data.deleteObjects"> Developers Wiki: Data.deleteObjects</a>
	 */
	public void data_deleteObjects( Collection<Long> objectIds ) throws FacebookException;

	public T data_getObject( long objectId ) throws FacebookException;

	public T data_getObjects( Collection<Long> objectIds ) throws FacebookException;

	public T data_getObjectProperty( long objectId, String propertyName ) throws FacebookException;

	public void data_setObjectProperty( long objectId, String propertyName, String value ) throws FacebookException;

	/**
	 * 
	 * @param associationName
	 *            Name of forward association to create. This name needs to be unique among all object types and associations defined for this application. This name also
	 *            needs to be a valid identifier, which is no longer than 32 characters, starting with a letter (a-z) and consisting of only small letters (a-z), numbers
	 *            (0-9) and/or underscores.
	 * @param associationType
	 * @param associationInfo1
	 * @param associationInfo2
	 * @param inverseName
	 *            Optional - name of backward association, if it is two-way asymmetric. This name needs to be unique among all object types and associations defined for
	 *            this application. This name also needs to be a valid identifier, which is no longer than 32 characters, starting with a letter (a-z) and consisting of
	 *            only small letters (a-z), numbers (0-9) and/or underscores.
	 * @throws FacebookException
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Data.defineAssociation"> Developers Wiki: Data.defineAssociation</a>
	 */
	public void data_defineAssociation( String associationName, AssociationType associationType, AssociationInfo associationInfo1, AssociationInfo associationInfo2,
			String inverseName ) throws FacebookException;

	/**
	 * Remove a previously defined association. This will also delete this type of associations established between objects. This deletion is not reversible.
	 * 
	 * @param name
	 * @throws FacebookException
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Data.undefineAssociation"> Developers Wiki: Data.undefineAssociation</a>
	 */
	public void data_undefineAssociation( String name ) throws FacebookException;

	/**
	 * Rename a previously defined association. Note that, any renaming here only affects one direction. To change names and aliases for another direction, rename with
	 * the name of that direction of association.
	 * 
	 * @param name
	 * @param newName
	 * @param newAlias1
	 * @param newAlias2
	 * 
	 * @throws FacebookException
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Data.renameAssociation"> Developers Wiki: Data.renameAssociation</a>
	 */
	public void data_renameAssociation( String name, String newName, String newAlias1, String newAlias2 ) throws FacebookException;

	public T data_getAssociationDefinition( String associationName ) throws FacebookException;

	public T data_getAssociationDefinitions() throws FacebookException;


	/**
	 * Create an association between two objects
	 * 
	 * @param associationName
	 *            Name of the association to set.
	 * @param object1Id
	 *            Object identifier 1.
	 * @param object2Id
	 *            Object identifier 2.
	 * @param data
	 *            Optional (can be null) - An arbitrary data (max. 255 characters) to store with this association.
	 * @param associationTime
	 *            Optional (can be null) - Default to association creation time. A timestamp to store with this association. This timestamp is represented as number of
	 *            seconds since the Unix Epoch (January 1 1970 00:00:00 GMT). )
	 * @throws FacebookException
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Data.setAssociation"> Developers Wiki: Data.setAssociation</a>
	 */
	public void data_setAssociation( String associationName, long object1Id, long object2Id, String data, Date associationTime ) throws FacebookException;

	/**
	 * Removes an association between two object identifiers. Note that, the order of these two identifiers matters, unless this is a symmetric two-way association.
	 * 
	 * @param associationName
	 *            Name of the association.
	 * @param object1Id
	 *            Object identifier 1.
	 * @param object2Id
	 *            Object identifier 2.
	 * @throws FacebookException
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Data.removeAssociation"> Developers Wiki: Data.removeAssociation</a>
	 */
	public void data_removeAssociation( String associationName, long object1Id, long object2Id ) throws FacebookException;

	/**
	 * The name of this function may be misleading, but it actually removes associations between any other objects and a specified object. Those other associated objects
	 * will NOT be removed or deleted. Only the associations will be broken and deleted.
	 * 
	 * @param associationName
	 *            Name of the association.
	 * @param objectId
	 *            Object identifier.
	 * @throws FacebookException
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Data.removeAssociatedObjects"> Developers Wiki: Data.removeAssociatedObjects</a>
	 */
	public void data_removeAssociatedObjects( String associationName, long objectId ) throws FacebookException;

	/**
	 * Returns count of object ids that are associated with specified object. This function takes constant time to return the count, regardless how many objects are
	 * associated.
	 * 
	 * @param associationName
	 *            Name of the association.
	 * @param objectId
	 *            Object identifier.
	 * @throws FacebookException
	 * @return int64 object count
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Data.getAssociatedObjectCount"> Developers Wiki: Data.getAssociatedObjectCount</a>
	 */
	public long data_getAssociatedObjectCount( String associationName, long objectId ) throws FacebookException;

	/**
	 * Sets several property values for an application. The properties available are analogous to the ones editable via the Facebook Developer application. A session is
	 * not required to use this method.
	 * 
	 * This method cannot be called by desktop apps.
	 * 
	 * @param properties
	 *            an ApplicationPropertySet that is translated into a single JSON String.
	 * @return a boolean indicating whether the properties were successfully set
	 * @see http://wiki.developers.facebook.com/index.php/Admin.setAppProperties
	 */
	public boolean admin_setAppProperties( ApplicationPropertySet properties ) throws FacebookException;

	/**
	 * Gets property values previously set for an application on either the Facebook Developer application or the with the <code>admin.setAppProperties</code> call. A
	 * session is not required to use this method.
	 * 
	 * This method cannot be called by desktop apps.
	 * 
	 * @param properties
	 *            an enumeration of the properties to get
	 * @return an ApplicationPropertySet
	 * @see ApplicationProperty
	 * @see http://wiki.developers.facebook.com/index.php/Admin.getAppProperties
	 */
	public ApplicationPropertySet admin_getAppPropertiesAsSet( EnumSet<ApplicationProperty> properties ) throws FacebookException;

	/**
	 * Starts a batch of queries. Any API calls made after invoking 'beginBatch' will be deferred until the next time you call 'executeBatch', at which time they will be
	 * processed as a batch query. All API calls made in the interim will return null as their result.
	 */
	public void beginBatch();

	/**
	 * Executes a batch of queries. It is your responsibility to encode the method feed correctly. It is not recommended that you call this method directly. Instead use
	 * 'beginBatch' and 'executeBatch', which will take care of the hard parts for you.
	 * 
	 * @param methods
	 *            A JSON encoded array of strings. Each element in the array should contain the full parameters for a method, including method name, sig, etc. Currently,
	 *            there is a maximum limit of 15 elements in the array.
	 * @param serial
	 *            An optional parameter to indicate whether the methods in the method_feed must be executed in order. The default value is false.
	 * 
	 * @return a result containing the response to each individual query in the batch.
	 */
	public T batch_run( String methods, boolean serial ) throws FacebookException;

	/**
	 * Executes a batch of queries. You define the queries to execute by calling 'beginBatch' and then invoking the desired API methods that you want to execute as part
	 * of your batch as normal. Invoking this method will then execute the API calls you made in the interim as a single batch query.
	 * 
	 * @param serial
	 *            set to true, and your batch queries will always execute serially, in the same order in which your specified them. If set to false, the Facebook API
	 *            server may execute your queries in parallel and/or out of order in order to improve performance.
	 * 
	 * @return a list containing the results of the batch execution. The list will be ordered such that the first element corresponds to the result of the first query in
	 *         the batch, and the second element corresponds to the result of the second query, and so on. The types of the objects in the list will match the type
	 *         normally returned by the API call being invoked (so calling users_getLoggedInUser as part of a batch will place a Long in the list, and calling friends_get
	 *         will place a Document in the list, etc.).
	 * 
	 * The list may be empty, it will never be null.
	 * 
	 * @throws FacebookException
	 */
	public List<? extends Object> executeBatch( boolean serial ) throws FacebookException;

	/**
	 * Gets the public information about the specified application. Only one of the 3 parameters needs to be specified.
	 * 
	 * @param applicationId
	 *            the id of the application to get the info for.
	 * @param applicationKey
	 *            the public API key of the application to get the info for.
	 * @param applicationCanvas
	 *            the canvas-page name of the application to get the info for.
	 * 
	 * @return the public information for the specified application
	 * @see http://wiki.developers.facebook.com/index.php/Application.getPublicInfo
	 */
	public T application_getPublicInfo( Long applicationId, String applicationKey, String applicationCanvas ) throws FacebookException;

	/**
	 * Gets the public information about the specified application, by application id.
	 * 
	 * @param applicationId
	 *            the id of the application to get the info for.
	 * 
	 * @return the public information for the specified application
	 * @see http://wiki.developers.facebook.com/index.php/Application.getPublicInfo
	 */
	public T application_getPublicInfoById( Long applicationId ) throws FacebookException;

	/**
	 * Gets the public information about the specified application, by API key.
	 * 
	 * @param applicationKey
	 *            the public API key of the application to get the info for.
	 * 
	 * @return the public information for the specified application
	 * @see http://wiki.developers.facebook.com/index.php/Application.getPublicInfo
	 */
	public T application_getPublicInfoByApiKey( String applicationKey ) throws FacebookException;

	/**
	 * Gets the public information about the specified application, by canvas-page name.
	 * 
	 * @param applicationCanvas
	 *            the canvas-page name of the application to get the info for.
	 * 
	 * @return the public information for the specified application
	 * @see http://wiki.developers.facebook.com/index.php/Application.getPublicInfo
	 */
	public T application_getPublicInfoByCanvasName( String applicationCanvas ) throws FacebookException;

	/**
	 * Get your application's current allocation of the specified type of request (i.e. the number of requests that it is currently allowed to send per user per day).
	 * 
	 * @param allocationType
	 *            the type of request to check the allocation for. Currently: "notifications_per_day" and "requests_per_day", "emails_per_day",
	 *            "email_disable_message_location"
	 * 
	 * @return the number of the specified type of requests that the application is permitted to send per user per day.
	 * @see http://wiki.developers.facebook.com/index.php/Admin.getAllocation
	 */
	public int admin_getAllocation( String allocationType ) throws FacebookException;

	/**
	 * Get your application's current allocation of the specified type of request (i.e. the number of requests that it is currently allowed to send per user per day).
	 * 
	 * @param allocationType
	 *            the type of request to check the allocation for. Currently: "notifications_per_day" and "requests_per_day", "emails_per_day",
	 *            "email_disable_message_location"
	 * 
	 * @return the number of the specified type of requests that the application is permitted to send per user per day.
	 * @see http://wiki.developers.facebook.com/index.php/Admin.getAllocation
	 */
	public int admin_getAllocation( AllocationType allocationType ) throws FacebookException;

	/**
	 * Get your application's current allocation for invites/requests (i.e. the total number of invites/requests that it is allowed to send per user, per day).
	 * 
	 * @return the number of invites/requests that the application is permitted to send per user per day.
	 * @see http://wiki.developers.facebook.com/index.php/Admin.getAllocation
	 */
	@Deprecated
	public int admin_getRequestAllocation() throws FacebookException;

	/**
	 * Get your application's current allocation for notifications (i.e. the total number of notifications that it is allowed to send per user, per day).
	 * 
	 * @return the number of notifications that the application is permitted to send per user per day.
	 * @see http://wiki.developers.facebook.com/index.php/Admin.getAllocation
	 */
	@Deprecated
	public int admin_getNotificationAllocation() throws FacebookException;

	/**
	 * Retrieve the daily metrics for the current application.
	 * 
	 * @param metrics
	 *            a set specifying the specific metrics to retrieve
	 * @param start
	 *            the starting date to retrieve data for (range must not exceed 30 days)
	 * @param end
	 *            the ending to to retrive data for (range must not exceed 30 days)
	 * 
	 * @return daily metrics for your app, for each day in the specified range
	 * 
	 * @throws FacebookException
	 * @see http://wiki.developers.facebook.com/index.php/Admin.getDailyMetrics
	 */
	@Deprecated
	public T admin_getDailyMetrics( Set<Metric> metrics, Date start, Date end ) throws FacebookException;

	/**
	 * Retrieve metrics for the current application.
	 * 
	 * @param metrics
	 *            a set specifying the specific metrics to retrieve
	 * @param start
	 *            the starting date to retrieve data for (range must not exceed 30 days)
	 * @param end
	 *            the ending to to retrive data for (range must not exceed 30 days)
	 * @param period
	 *            a number specifying the desired period to group the metrics by, in seconds, Facebook currently only supports Metric.PERIOD_DAY, Metric.PERIOD_WEEK, and
	 *            Metric.PERIOD_MONTH
	 * 
	 * @return daily metrics for your app, for each day in the specified range
	 * 
	 * @throws FacebookException
	 * @see http://wiki.developers.facebook.com/index.php/Admin.getMetrics
	 */
	public T admin_getMetrics( Set<Metric> metrics, Date start, Date end, long period ) throws FacebookException;

	/**
	 * Retrieve the daily metrics for the current application.
	 * 
	 * @param metrics
	 *            a set specifying the specific metrics to retrieve
	 * @param start
	 *            the starting date to retrieve data for (range must not exceed 30 days), the accepted unit of time is milliseconds, NOT seconds
	 * @param end
	 *            the ending to to retrive data for (range must not exceed 30 days), the accepted unit of time is milliseconds, NOT seconds
	 * 
	 * @return daily metrics for your app, for each day in the specified range
	 * 
	 * @throws FacebookException
	 * @see http://wiki.developers.facebook.com/index.php/Admin.getDailyMetrics
	 */
	@Deprecated
	public T admin_getDailyMetrics( Set<Metric> metrics, long start, long end ) throws FacebookException;

	/**
	 * Retrieve the daily metrics for the current application.
	 * 
	 * @param metrics
	 *            a set specifying the specific metrics to retrieve
	 * @param start
	 *            the starting date to retrieve data for (range must not exceed 30 days), the accepted unit of time is milliseconds, NOT seconds
	 * @param end
	 *            the ending to to retrive data for (range must not exceed 30 days), the accepted unit of time is milliseconds, NOT seconds
	 * @param period
	 *            a number specifying the desired period to group the metrics by, in seconds, Facebook currently only supports Metric.PERIOD_DAY, Metric.PERIOD_WEEK, and
	 *            Metric.PERIOD_MONTH
	 * 
	 * @return daily metrics for your app, for each day in the specified range
	 * 
	 * @throws FacebookException
	 * @see http://wiki.developers.facebook.com/index.php/Admin.getMetrics
	 */
	public T admin_getMetrics( Set<Metric> metrics, long start, long end, long period ) throws FacebookException;

	/**
	 * Grant permission to an external app to make API calls on behalf of the current application.
	 * 
	 * @param apiKey
	 *            the API-key of the application to grant permission to.
	 * @param methods
	 *            the API methods to allow the other application to call. If the set is empty or null, permission is granted for all API methods.
	 * 
	 * @return true if the operation succeeds false otherwise
	 */
	public boolean permissions_grantApiAccess( String apiKey, Set<FacebookMethod> methods ) throws FacebookException;

	/**
	 * Grant permission to an external app to make API calls on behalf of the current application. Access is granted to the full set of allowed API methods.
	 * 
	 * @param apiKey
	 *            the API-key of the application to grant permission to.
	 * 
	 * @return true if the operation succeeds false otherwise
	 */
	public boolean permissions_grantFullApiAccess( String apiKey ) throws FacebookException;

	/**
	 * Check to see what permissions have been granted to current app by the specified external application.
	 * 
	 * For example:
	 * 
	 * Application A grants permission on users.getInfo to Application B, Applicatio B can then call permissions_checkAvailableApiAccess(A) and will recieve
	 * "users.getInfo" as a result.
	 * 
	 * @param apiKey
	 *            the API key of the application to check for permissions from.
	 * 
	 * @return a list of all API methods that the specified application has permission to use.
	 */
	public T permissions_checkAvailableApiAccess( String apiKey ) throws FacebookException;

	/**
	 * Revokes the specified application's permission to call API methods on behalf of the current app.
	 * 
	 * @param apiKey
	 *            the API key of the application to remove permissions for.
	 * 
	 * @return true if the operation succeeds false otherwise
	 */
	public boolean permissions_revokeApiAccess( String apiKey ) throws FacebookException;

	/**
	 * Check to see what permissions have been granted to specified external application by the current application.
	 * 
	 * For example:
	 * 
	 * Application A grants permission on users.getInfo to Application B, Applicatio A can then call permissions_checkGrantedApiAccess(B) and will recieve "users.getInfo"
	 * as a result.
	 * 
	 * @param apiKey
	 *            the API key of the application to check permissions for.
	 * 
	 * @return a list of all API methods that the specified application has permission to use.
	 */
	public T permissions_checkGrantedApiAccess( String apiKey ) throws FacebookException;

	/**
	 * Expires the curently active session.
	 * 
	 * @return true if the call succeeds false otherwise
	 * 
	 * @throws FacebookException
	 */
	public boolean auth_expireSession() throws FacebookException;

	/**
	 * If this method is called for the logged in user, then no further API calls can be made on that user's behalf until the user decides to authorize the application
	 * again.
	 * 
	 * @return true if the call succeeds false otherwise
	 * 
	 * @throws FacebookException
	 */
	public boolean auth_revokeAuthorization() throws FacebookException;

	/**
	 * Begins permissions mode, and allows the current application to begin making requests on behalf of the application associated with the specified API key.
	 * 
	 * This method must be invoked prior to making an API request on behalf of another application. When you are done, be sure to call endPermissionsMode().
	 * 
	 * @param apiKey
	 *            the API key of the application to being making requests for.
	 */
	public void beginPermissionsMode( String apiKey );

	/**
	 * Terminates permissions mode. After calling this, the current application will be unable to make requests on behalf of another app, until beginPermissionsMode is
	 * called again.
	 */
	public void endPermissionsMode();

	/**
	 * Get the JAXB context that is being used by the client.
	 * 
	 * @return the JAXB context object.
	 */
	public JAXBContext getJaxbContext();

	/**
	 * Set the JAXB context that the client will use.
	 * 
	 * @param context
	 *            the context to use.
	 */
	public void setJaxbContext( JAXBContext context );

	/**
	 * Generate a key for the current session that can be used to authenticate client-side components.
	 * 
	 * @return the key.
	 */
	public String auth_promoteSession() throws FacebookException;

	/**
	 * Registers a feed template.
	 * 
	 * See: http://wiki.developers.facebook.com/index.php/Feed.registerTemplateBundle
	 * 
	 * @param template
	 *            the template to store
	 * 
	 * @return the id which Facebook assigns to your template
	 */
	public Long feed_registerTemplateBundle( String template ) throws FacebookException;

	/**
	 * Registers a feed template.
	 * 
	 * See: http://wiki.developers.facebook.com/index.php/Feed.registerTemplateBundle
	 * 
	 * @param templates
	 *            the templates to store
	 * 
	 * @return the id which Facebook assigns to your template
	 */
	public Long feed_registerTemplateBundle( Collection<String> templates ) throws FacebookException;

	/**
	 * Registers a feed template.
	 * 
	 * See: http://wiki.developers.facebook.com/index.php/Feed.registerTemplateBundle
	 * 
	 * @param template
	 *            the template to store.
	 * @param shortTemplate
	 *            the short template to store.
	 * @param longTemplate
	 *            the long template to store.
	 * 
	 * @return the id which Facebook assigns to your template
	 * 
	 * @deprecated Facebook has greatly modified the syntax required for the 'shortTemplate' and 'longTemplate' parameters. As such this method will now ignore those
	 *             parameters. You are encouraged to use one of the alternate versions.
	 */
	@Deprecated
	public Long feed_registerTemplateBundle( String template, String shortTemplate, String longTemplate ) throws FacebookException;

	/**
	 * Registers a feed template.
	 * 
	 * See: http://wiki.developers.facebook.com/index.php/Feed.registerTemplateBundle
	 * 
	 * @param template
	 *            the template to store.
	 * @param shortTemplate
	 *            the short template to store.
	 * @param longTemplate
	 *            the long template to store.
	 * @param actionLinks
	 *            the action links to store
	 * 
	 * @return the id which Facebook assigns to your template
	 */
	public Long feed_registerTemplateBundle( Collection<String> templates, Collection<BundleStoryTemplate> shortTemplates, BundleStoryTemplate longTemplate,
			List<BundleActionLink> actionLinks ) throws FacebookException;

	/**
	 * Registers a feed template.
	 * 
	 * See: http://wiki.developers.facebook.com/index.php/Feed.registerTemplateBundle
	 * 
	 * @param template
	 *            the template to store.
	 * @param shortTemplate
	 *            the short template to store.
	 * @param longTemplate
	 *            the long template to store.
	 * 
	 * @return the id which Facebook assigns to your template
	 */
	public Long feed_registerTemplateBundle( Collection<String> templates, Collection<BundleStoryTemplate> shortTemplates, BundleStoryTemplate longTemplate )
			throws FacebookException;

	/**
	 * Get a list of all registered template bundles for your application.
	 * 
	 * @return a list describing all registered feed templates.
	 * 
	 * @throws FacebookException
	 */
	public T feed_getRegisteredTemplateBundles() throws FacebookException;

	/**
	 * Retrieve a template bundle by id.
	 * 
	 * @param id
	 *            the id to retrieve.
	 * 
	 * @return the specified template bundle definition.
	 * @throws FacebookException
	 */
	public T feed_getRegisteredTemplateBundleByID( Long id ) throws FacebookException;

	/**
	 * Publishes a user action to the feed.
	 * 
	 * See: http://wiki.developers.facebook.com/index.php/Feed.publishUserAction
	 * 
	 * @param bundleId
	 *            the template bundle-id to use to render the feed.
	 * 
	 * @return true if the call succeeds false otherwise
	 * 
	 * @throws FacebookException
	 */
	public Boolean feed_publishUserAction( Long bundleId ) throws FacebookException;

	/**
	 * Publishes a user action to the feed.
	 * 
	 * See: http://wiki.developers.facebook.com/index.php/Feed.publishUserAction
	 * 
	 * @param bundleId
	 *            the template bundle-id to use to render the feed.
	 * @param templateData
	 *            a map of name-value pairs to substitute into the template being rendered.
	 * @param targetIds
	 *            the ids of individuals that are the target of this action.
	 * @param bodyGeneral
	 *            additional markup to include in the feed story.
	 * 
	 * @return true if the call succeeds false otherwise
	 * 
	 * @throws FacebookException
	 */
	public Boolean feed_publishUserAction( Long bundleId, Map<String,String> templateData, List<Long> targetIds, String bodyGeneral ) throws FacebookException;

	/**
	 * Get the specified user's application-info section.
	 * 
	 * @param userId
	 *            the id of the user to get the info section for.
	 * 
	 * @return the user's application-info section.
	 * 
	 * @throws FacebookException
	 */
	public T profile_getInfo( Long userId ) throws FacebookException;

	/**
	 * Get the options associated with the specified field for an application info section.
	 * 
	 * @param field
	 *            the field to get the options for.
	 * 
	 * @return the options associated with the specified field for an application info section.
	 * 
	 * @throws FacebookException
	 */
	public T profile_getInfoOptions( String field ) throws FacebookException;

	/**
	 * Configures an application info section that the specified user can install on the Info tab of her profile.
	 * 
	 * See: http://wiki.developers.facebook.com/index.php/Profile.setInfo
	 * 
	 * @param userId
	 *            the user to set the info section for.
	 * @param title
	 *            the title to use for the section.
	 * @param textOnly
	 *            set to true if your info fields are text only. set to false for thumbnail mode.
	 * @param fields
	 *            the fields to set.
	 * 
	 * @throws FacebookException
	 */
	public void profile_setInfo( Long userId, String title, boolean textOnly, List<ProfileInfoField> fields ) throws FacebookException;

	/**
	 * Specifies the objects for a field for an application info section. These options populate the typeahead for a thumbnail.
	 * 
	 * See: http://wiki.developers.facebook.com/index.php/Profile.setInfoOptions
	 * 
	 * @param field
	 *            the field to set.
	 * 
	 * @throws FacebookException
	 */
	public void profile_setInfoOptions( ProfileInfoField field ) throws FacebookException;

	/**
	 * Adds several tags to a photo.
	 * 
	 * @param photoId
	 *            The photo id of the photo to be tagged.
	 * @param tags
	 *            A list of PhotoTags.
	 * @param userId
	 *            the id of the user adding the tags.
	 * 
	 * @return a list of booleans indicating whether the tag was successfully added.
	 */
	public T photos_addTags( Long photoId, Iterable<PhotoTag> tags, Long userId ) throws FacebookException;

	/**
	 * Override the default Facebook API server used for making requests. Can be used to tell the client to run against the
	 * 
	 * @param newUrl
	 *            the new URL to use, for example: "http://api.facebook.com/restserver.php"
	 * @throws MalformedURLException
	 */
	public void setServerUrl( String newUrl );

	public URL getDefaultServerUrl();

	public void setDefaultServerUrl( URL url );

	/**
	 * Sends a message using the LiveMessage API. Note that for the message to be recieved by the recipent, you must set up a FBJS handler function. See
	 * http://wiki.developers.facebook.com/index.php/LiveMessage for details.
	 * 
	 * @param recipient
	 *            the id of the user to send the message to.
	 * @param eventName
	 *            the name associated with the FBJS handler you want to recieve your message.
	 * @param message
	 *            the JSON-object to send, the object will be passed to the FBJS handler that you have mapped to 'eventName'. See
	 *            http://wiki.developers.facebook.com/index.php/LiveMessage for details.
	 * 
	 * @return true if the message is sent, false otherwise
	 * 
	 * @throws FacebookException
	 */
	public Boolean liveMessage_send( Long recipient, String eventName, JSONObject message ) throws FacebookException;

	/**
	 * Sends a notification.
	 * 
	 * @param recipientIds
	 *            the ids of the users to send the notification to.
	 * @param notification
	 *            the notification to send.
	 * @param announcement
	 *            set to 'true' to send an "announcement" notification, otherwise set to false to send a "general" notification.
	 * 
	 * @throws FacebookException
	 * @see http://wiki.developers.facebook.com/index.php/Notifications.send
	 */
	public void notifications_send( Collection<Long> recipientIds, String notification, boolean isAppToUser ) throws FacebookException;

	/**
	 * Deactivates the specified template bundle.
	 * 
	 * @param bundleId
	 *            the id of the bundle to deactivate.
	 * 
	 * @return true if the call succeeds, false otherwise.
	 * 
	 * @throws FacebookException
	 */
	public boolean feed_deactivateTemplateBundleByID( Long bundleId ) throws FacebookException;

	/**
	 * Publishes a user action to the feed.
	 * 
	 * See: http://wiki.developers.facebook.com/index.php/Feed.publishUserAction
	 * 
	 * @param bundleId
	 *            the template bundle-id to use to render the feed.
	 * @param templateData
	 *            a map of name-value pairs to substitute into the template being rendered.
	 * @param images
	 *            the images to associate with this feed entry
	 * @param targetIds
	 *            the ids of individuals that are the target of this action.
	 * @param bodyGeneral
	 *            additional markup to include in the feed story.
	 * @param storySize
	 *            story size to use. valid values are 1, 2 or 4.
	 * 
	 * @return true if the call succeeds false otherwise
	 * 
	 * @throws FacebookException
	 */
	public Boolean feed_publishUserAction( Long bundleId, Map<String,String> templateData, List<IFeedImage> images, List<Long> targetIds, String bodyGeneral,
			int storySize ) throws FacebookException;


	// ========== EVENTS ==========

	/**
	 * Returns all visible events according to the filters specified.
	 * 
	 * @param userId
	 *            Filter by events associated with a user with this uid.
	 * @param eventIds
	 *            Filter by this list of event IDs. This is a comma-separated list of event IDs.
	 * @param startTime
	 *            Filter with this UTC as lower bound. A missing or zero parameter indicates no lower bound.
	 * @param endTime
	 *            Filter with this UTC as upper bound. A missing or zero parameter indicates no upper bound.
	 * 
	 * @see http://wiki.developers.facebook.com/index.php/Events.get
	 */
	public T events_get( Long userId, Collection<Long> eventIds, Long startTime, Long endTime ) throws FacebookException;

	/**
	 * Returns all visible events according to the filters specified.
	 * 
	 * @param userId
	 *            Filter by events associated with a user with this uid.
	 * @param eventIds
	 *            Filter by this list of event IDs. This is a comma-separated list of event IDs.
	 * @param startTime
	 *            Filter with this UTC as lower bound. A missing or zero parameter indicates no lower bound.
	 * @param endTime
	 *            Filter with this UTC as upper bound. A missing or zero parameter indicates no upper bound.
	 * @param rsvp_status
	 *            Filter by this RSVP status. The RSVP status should be one of the following strings: attending, unsure, declined, not_replied
	 * 
	 * @see http://wiki.developers.facebook.com/index.php/Events.get
	 */
	public T events_get( Long userId, Collection<Long> eventIds, Long startTime, Long endTime, String rsvp_status ) throws FacebookException;

	/**
	 * Retrieves the membership list of an event
	 * 
	 * @param eventId
	 *            The event ID.
	 * @return T consisting of four membership lists corresponding to RSVP status, with keys 'attending', 'unsure', 'declined', and 'not_replied'
	 * @see http://wiki.developers.facebook.com/index.php/Events.getMembers
	 */
	public T events_getMembers( Long eventId ) throws FacebookException;

	/**
	 * Creates an event on behalf of the user if the application has an active session key for that user; otherwise it creates an event on behalf of the application.
	 * 
	 * @param event_info
	 *            The event information. You must pass the following parameters:
	 *            <ul>
	 *            <li>name</li>
	 *            <li>category</li>
	 *            <li>subcategory</li>
	 *            <li>host</li>
	 *            <li>location</li>
	 *            <li>city</li>
	 *            <li>start_time (seconds since epoch)</li>
	 *            <li>end_time (seconds since epoch)</li>
	 *            </ul>
	 *            Optionally, you can pass the following parameters in the event_info array:
	 *            <ul>
	 *            <li>street</li>
	 *            <li>phone</li>
	 *            <li>email</li>
	 *            <li>page_id</li>
	 *            <li>description</li>
	 *            <li>privacy_type</li>
	 *            <li>tagline</li>
	 *            </ul>
	 * 
	 * @see http://wiki.developers.facebook.com/index.php/Events.create
	 */
	public Long events_create( Map<String,String> event_info ) throws FacebookException;

	/**
	 * Edits the details of an existing event. The application must be an admin of the event. An application is the admin of an event if the application created the event
	 * on behalf of a user (with that user's active session) or if it is the creator of the event itself (that is, the event was created without an active user session).
	 * 
	 * @param eid
	 *            The event ID.
	 * @param event_infoThe
	 *            event information. The "name" cannot be edited. The following parameters in the event_info array are required and can only be edited, not deleted:
	 *            <ul>
	 *            <li>category</li>
	 *            <li>subcategory</li>
	 *            <li>host</li>
	 *            <li>location</li>
	 *            <li>start_time (seconds since epoch)</li>
	 *            <li>end_time (seconds since epoch)</li>
	 *            <li>street</li>
	 *            <li>phone</li>
	 *            <li>email</li>
	 *            <li>host_id</li>
	 *            <li>description</li>
	 *            <li>privacy_type</li>
	 *            <li>tagline</li>
	 *            </ul>
	 * 
	 * @see http://wiki.developers.facebook.com/index.php/Events.edit
	 */
	public boolean events_edit( Long eid, Map<String,String> event_info ) throws FacebookException;

	/**
	 * Cancels an event. The application must be an admin of the event. An application is the admin of an event if the application created the event on behalf of a user
	 * (with that user's active session) or if it is the creator of the event itself (that is, the event was created without an active user session).
	 * 
	 * @param eid
	 *            The event ID.
	 * @param cancel_message
	 *            The message sent explaining why the event was canceled. You can pass an empty string if you don't want to provide an explanation.
	 * 
	 * @see http://wiki.developers.facebook.com/index.php/Events.cancel
	 */
	public boolean events_cancel( Long eid, String cancel_message ) throws FacebookException;

	/**
	 * Sets a user's RSVP status for an event. An application can set a user's RSVP status only if the following are all true:
	 * <ul>
	 * <li>The application is an admin for the event.</li>
	 * <li>The application has an active session for the user.</li>
	 * <li>The active user has granted the application the rsvp_event extended permission.</li>
	 * </ul>
	 * 
	 * @param eid
	 *            The event ID.
	 * @param rsvp_status
	 *            The user's RSVP status. Specify attending, unsure, or declined.
	 * 
	 * @see http://wiki.developers.facebook.com/index.php/Events.rsvp
	 */
	public boolean events_rsvp( Long eid, String rsvp_status ) throws FacebookException;


	// ========== MOBILE ==========

	/**
	 * Check to see if the application is permitted to send SMS messages to the current application user.
	 * 
	 * @return true if the application is presently able to send SMS messages to the current user false otherwise
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 */
	public boolean sms_canSend() throws FacebookException;

	/**
	 * Check to see if the application is permitted to send SMS messages to the specified user.
	 * 
	 * @param userId
	 *            the UID of the user to check permissions for
	 * 
	 * @return true if the application is presently able to send SMS messages to the specified user false otherwise
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 */
	public boolean sms_canSend( Long userId ) throws FacebookException;

	/**
	 * Send an SMS message to the current application user.
	 * 
	 * @param message
	 *            the message to send.
	 * @param smsSessionId
	 *            the SMS session id to use, note that that is distinct from the user's facebook session id. It is used to allow applications to keep track of individual
	 *            SMS conversations/threads for a single user. Specify null if you do not want/need to use a session for the current message.
	 * @param makeNewSession
	 *            set to true to request that Facebook allocate a new SMS session id for this message. The allocated id will be returned as the result of this API call.
	 *            You should only set this to true if you are passing a null 'smsSessionId' value. Otherwise you already have a SMS session id, and do not need a new one.
	 * 
	 * @return an integer specifying the value of the session id alocated by Facebook, if one was requested. If a new session id was not requested, this method will
	 *         return null.
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 */
	public Integer sms_send( String message, Integer smsSessionId, boolean makeNewSession ) throws FacebookException;

	/**
	 * Send an SMS message to the specified user.
	 * 
	 * @param userId
	 *            the id of the user to send the message to.
	 * @param message
	 *            the message to send.
	 * @param smsSessionId
	 *            the SMS session id to use, note that that is distinct from the user's facebook session id. It is used to allow applications to keep track of individual
	 *            SMS conversations/threads for a single user. Specify null if you do not want/need to use a session for the current message.
	 * @param makeNewSession
	 *            set to true to request that Facebook allocate a new SMS session id for this message. The allocated id will be returned as the result of this API call.
	 *            You should only set this to true if you are passing a null 'smsSessionId' value. Otherwise you already have a SMS session id, and do not need a new one.
	 * 
	 * @return an integer specifying the value of the session id alocated by Facebook, if one was requested. If a new session id was not requested, this method will
	 *         return null.
	 * 
	 * @throws FacebookException
	 *             if an error happens when executing the API call.
	 */
	public Integer sms_send( Long userId, String message, Integer smsSessionId, boolean makeNewSession ) throws FacebookException;

	/**
	 * Sends a message via SMS to the user identified by <code>userId</code>, with the expectation that the user will reply. The SMS extended permission is required
	 * for success. The returned mobile session ID can be stored and used in {@link #sms_sendResponse} when the user replies.
	 * 
	 * @param userId
	 *            a user ID
	 * @param message
	 *            the message to be sent via SMS
	 * @return a mobile session ID (can be used in {@link #sms_sendResponse})
	 * @throws FacebookException
	 *             in case of error, e.g. SMS is not enabled
	 * @see FacebookExtendedPerm#SMS
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Mobile#Application_generated_messages"> Developers Wiki: Mobile: Application Generated Messages</a>
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Mobile#Workflow"> Developers Wiki: Mobile: Workflow</a>
	 */
	public int sms_sendMessageWithSession( Long userId, CharSequence message ) throws FacebookException;

	/**
	 * Sends a message via SMS to the user identified by <code>userId</code>. The SMS extended permission is required for success.
	 * 
	 * @param userId
	 *            a user ID
	 * @param message
	 *            the message to be sent via SMS
	 * @throws FacebookException
	 *             in case of error
	 * @see FacebookExtendedPerm#SMS
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Mobile#Application_generated_messages"> Developers Wiki: Mobile: Application Generated Messages</a>
	 * @see <a href="http://wiki.developers.facebook.com/index.php/Mobile#Workflow"> Developers Wiki: Mobile: Workflow</a>
	 */
	public void sms_sendMessage( Long userId, CharSequence message ) throws FacebookException;

	// ========== CONNECT ==========

	/**
	 * This method is used to create an association between an external user account and a Facebook user account. This method takes an array of account data, including a
	 * required email_hash and optional account data. For each connected account, if the user exists, the information is added to the set of the user's connected
	 * accounts. If the user has already authorized the site, the connected account is added in the confirmed state. If the user has not yet authorized the site, the
	 * connected account is added in the pending state.
	 * 
	 * @param accounts
	 *            An array of up to 1,000 arrays, or "maps," where each map represent a connected account. Each map can have the following properties:
	 *            <ul>
	 *            <li>email_hash: The public email hash of remote account. This property is required. Compute the email_hash property as follows:
	 *            <ol>
	 *            <li>Normalize the email address. Trim leading and trailing whitespace, and convert all characters to lowercase.</li>
	 *            <li>Compute the CRC32 value for the normalized email address and use the unsigned integer representation of this value. (Note that some implementations
	 *            return signed integers, in which case you will need to convert that result to an unsigned integer.)</li>
	 *            <li>Compute the MD5 value for the normalized email address and use the hex representation of this value (using lowercase for A through F).</li>
	 *            <li>Combine these two value with an underscore.</li>
	 *            </ol>
	 *            For example, the address mary@example.com converts to 4228600737_c96da02bba97aedfd26136e980ae3761. </li>
	 *            <li>account_id: The user's account ID on the Facebook Connect site. This property is optional. If you specify the account_id property, then you must
	 *            also set a Connect Preview URL in your application's settings in order to generate a full user URL. The Connect Preview URL contains an account_id
	 *            parameter, such as http://www.example.com/profile.php?user=account_id. </li>
	 *            <li>account_url: The URL to the user's account on the Facebook Connect site. This property is optional. If you specify the account_url property, that
	 *            URL will be used directly. </li>
	 *            </ul>
	 *            Facebook recommends that you specify at least one of either the account_id or the account_url properties.
	 * 
	 * @return This method returns an array of email hashes that have been successfully registered. If any email hashes are missing, we recommend that you try registering
	 *         them again later.
	 * @see http://wiki.developers.facebook.com/index.php/Connect.registerUsers
	 */
	public T connect_registerUsers( Collection<Map<String,String>> accounts ) throws FacebookException;

	/**
	 * This method allows a site to unregister a previously registered account (using connect.registerUsers). You should call this method if the user deletes his account
	 * on your site.
	 * 
	 * @param email_hashes
	 *            An array of email_hashes to unregister.
	 * @return This method returns an array of unregistered email hashes. If any email hashes are missing, we recommend that you try unregistering the account again
	 *         later.
	 * @see http://wiki.developers.facebook.com/index.php/Connect.unregisterUsers
	 */
	public T connect_unregisterUsers( Collection<String> email_hashes ) throws FacebookException;

	/**
	 * This method returns the number of friends of the current user who have accounts on your site, but have not yet connected their accounts. Also see
	 * fb:unconnected-friends-count. You can use the response from this call to determine whether or not to display a link allowing the user to invite their friends to
	 * connect as well.
	 * 
	 * @return This method returns an int that indicates the number of users who have not yet connected their accounts.
	 * @see http://wiki.developers.facebook.com/index.php/Connect.getUnconnectedFriendsCount
	 */
	public int connect_getUnconnectedFriendsCount() throws FacebookException;

}
