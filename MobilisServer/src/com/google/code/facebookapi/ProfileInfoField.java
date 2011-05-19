package com.google.code.facebookapi;

import java.util.ArrayList;
import java.util.List;

/**
 * A data structure for managing the profile info fields objects required by the profile.setInfo and profile.setInfoOptions API calls. Each field is identified by name,
 * and may contain any number of field items. Each field-item must specify a label and a link URL, and may optionally include a description, a sublabel, and an image URL.
 * 
 * @author aroth
 */
public class ProfileInfoField {
	String fieldName;
	List<ProfileFieldItem> items;

	/**
	 * Constructor, constructs a new profile field with the specified name.
	 * 
	 * @param name
	 *            the name to use for this field.
	 */
	public ProfileInfoField( String name ) {
		this.fieldName = name;
		this.items = new ArrayList<ProfileFieldItem>();
	}

	/**
	 * @return the name of thi field
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * Get the items that currently comprise this field.
	 * 
	 * @return the items that comprise this field.
	 */
	public List<ProfileFieldItem> getItems() {
		return items;
	}

	/**
	 * Set the items associated with this profile field.
	 * 
	 * @param items
	 *            the items to set.
	 */
	public void setItems( List<ProfileFieldItem> items ) {
		this.items = items;
	}

	/**
	 * Add an item to this ProfileInfoField.
	 * 
	 * @param item
	 *            the item to add.
	 */
	public void addItem( ProfileFieldItem item ) {
		this.items.add( item );
	}
}
