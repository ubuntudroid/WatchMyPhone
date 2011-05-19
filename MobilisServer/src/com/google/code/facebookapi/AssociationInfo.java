package com.google.code.facebookapi;

/**
 * Describes one of two legs of an association. It gives the association a name
 * and optionally specifies its type and uniqueness constraints.
 * 
 * @see <a href="http://wiki.developers.facebook.com/index.php/Data.defineAssociation"> Developers Wiki: Data.defineAssociation</a>
 * @author david.j.boden
 */
public class AssociationInfo {
	private String alias; // specified in constructor
	private String objectType; // default to null
	private boolean unique = false; // default to false

	public AssociationInfo(String alias) {
		this.alias = alias;
	}

	public AssociationInfo(String alias, String objectType, boolean unique) {
		this.alias = alias;
		this.objectType = objectType;
		this.unique = unique;
	}

	/**
	 * This alias needs to be a valid identifier, which is no longer than 32
	 * characters, starting with a letter (a-z) and consisting of only small
	 * letters (a-z), numbers (0-9) and/or underscores.
	 * 
	 * @return
	 */
	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	/**
	 * Optional - object type of object identifier. Name it after the table that
	 * it's "foreign keying" into.
	 * 
	 * @return
	 */
	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	/**
	 * Optional - Default to false. Whether each unique object identifier can
	 * only appear once in all associations of this type.
	 * 
	 * @return
	 */
	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

}