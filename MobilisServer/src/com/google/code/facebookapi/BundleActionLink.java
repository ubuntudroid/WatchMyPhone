package com.google.code.facebookapi;

import java.io.Serializable;

import org.json.JSONObject;

/**
 * A simple data structure for storing a story-template action link, used in the feed_registerTemplateBundle API call.
 * 
 * @see {@link http://wiki.developers.facebook.com/index.php/Action_Links}
 */
public class BundleActionLink implements Serializable {

	private String text;
	private String href;

	/**
	 * Constructor. If you use this version, you must make sure you set both the 'text' and 'href' fields before trying to submit your template, otherwise it will not
	 * serialize correctly.
	 */
	public BundleActionLink() {
		// empty
	}

	/**
	 * Constructor.
	 * 
	 * @param text
	 *            the text to display for the action.
	 * @param href
	 *            the action link (may include tokens).
	 */
	public BundleActionLink( String text, String href ) {
		this.text = text;
		this.href = href;
	}

	/**
	 * @return a JSON representation of this template.
	 */
	public JSONObject toJson() {
		JSONObject result = new JSONObject();
		if ( ( text == null ) || ( href == null ) || ( "".equals( text ) ) || ( "".equals( href ) ) ) {
			return result;
		}


		try {
			result.put( "text", text );
			result.put( "href", href );
		}
		catch ( Exception ignored ) {
			// ignore
		}
		return result;
	}

	/**
	 * @return a JSON-encoded String representation of this template. The resulting String is appropriate for passing to the Facebook API server.
	 */
	public String toJsonString() {
		return this.toJson().toString();
	}

	/**
	 * Get the text to display for the action.
	 */
	public final String getText() {
		return text;
	}

	/**
	 * Set the text to display for the action.
	 */
	public final void setText( String text ) {
		this.text = text;
	}

	/**
	 * Get the action link (may include tokens).
	 */
	public final String getHref() {
		return href;
	}

	/**
	 * Set the action link (may include tokens).
	 */
	public final void setHref( String href ) {
		this.href = href;
	}

}
