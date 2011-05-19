package com.google.code.facebookapi;

/**
 * A listing of all allocation types used by the Admin.getAllocation method call.
 * 
 * @see http://wiki.developers.facebook.com/index.php/Admin.getAllocation
 */
public enum AllocationType {

	NOTIFICATIONS_PER_DAY("notifications_per_day"),
	ANNOUNCEMENT_NOTIFICATIONS_PER_WEEK("announcement_notifications_per_week"),
	REQUESTS_PER_DAY("requests_per_day"),
	EMAILS_PER_DAY("emails_per_day"),
	EMAIL_DISABLE_MESSAGE_LOCATION("email_disable_message_location");

	private String name;

	private AllocationType( String name ) {
		this.name = name;
	}

	/**
	 * Get the name by which Facebook refers to this metric.
	 * 
	 * @return the Facebook-supplied name of this metric.
	 */
	public String getName() {
		return this.name;
	}

}
