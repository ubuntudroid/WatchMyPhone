package com.google.code.facebookapi;

import java.util.Map;
import java.util.TreeMap;

/**
 * A data structure for managing the profile field-item objects required by the profile.setInfo and profile.setInfoOptions API calls. Each field-item must specify a label
 * and a link URL, and may optionally include a description, a sublabel, and an image URL.
 */
public class ProfileFieldItem {

	private Map<String,String> properties;

	/**
	 * Constructor, creates a new ProfileFieldItem with the specified label and link.
	 * 
	 * @param label
	 *            the label to use.
	 * @param url
	 *            the link to apply to the label.
	 */
	public ProfileFieldItem( String label, String url ) {
		properties = new TreeMap<String,String>();
		properties.put( "label", label );
		properties.put( "link", url );
	}

	/**
	 * @return the label.
	 */
	public String getLabel() {
		return properties.get( "label" );
	}

	/**
	 * @return the link url.
	 */
	public String getUrl() {
		return properties.get( "link" );
	}

	/**
	 * @return the description.
	 */
	public String getDescription() {
		return properties.get( "description" );
	}

	/**
	 * @return the image url.
	 */
	public String getImageUrl() {
		return properties.get( "image" );
	}

	/**
	 * @return the sublabel for this item.
	 */
	public String getSublabel() {
		return properties.get( "sublabel" );
	}

	/**
	 * @param description
	 *            the description to set.
	 */
	public void setDescription( String description ) {
		properties.put( "description", description );
	}

	/**
	 * @param imageUrl
	 *            the image URL to use.
	 */
	public void setImageUrl( String imageUrl ) {
		properties.put( "image", imageUrl );
	}

	/**
	 * @param sublabel
	 *            the sublabel to use.
	 */
	public void setSublabel( String sublabel ) {
		properties.put( "sublabel", sublabel );
	}

	// package-level access is intentional
	Map<String,String> getMap() throws FacebookException {
		if ( "".equals( getLabel() ) || getLabel() == null || "".equals( getUrl() ) || getUrl() == null ) {
			throw new FacebookException( ErrorCode.GEN_INVALID_PARAMETER, "Field items must include both a label and a link URL." );
		}
		return properties;
	}

}
