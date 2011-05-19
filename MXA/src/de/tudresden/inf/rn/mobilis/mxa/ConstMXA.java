/**
 * Copyright (C) 2009 Technische Universität Dresden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: http://mobilisplatform.sourceforge.net
 */

package de.tudresden.inf.rn.mobilis.mxa;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @author Istvan Koren, Robert Lübke.
 */
public class ConstMXA {

	// the enum values sent as action
	public static final int MSG_CONNECT = 10;
	public static final int MSG_DISCONNECT = 11;
	public static final int MSG_SEND_MESSAGE = 12;
	public static final int MSG_SEND_IQ = 13;
	public static final int MSG_SEND_PRESENCE = 14;
	public static final int MSG_SEND_FILE = 15;
	public static final int MSG_DISCOVER_ITEMS = 16;
	public static final int MSG_DISCOVER_INFO = 17;
	public static final int MSG_ACCEPT_FILE = 18;
	public static final int MSG_DENY_FILE = 19;
	// the enum values for status
	public static final int MSG_STATUS_REQUEST = 20;
	public static final int MSG_STATUS_SUCCESS = 21;
	public static final int MSG_STATUS_DELIVERED = 22;
	public static final int MSG_STATUS_ERROR = 23;
	// the additional enum values for IQ status
	public static final int MSG_STATUS_IQ_RESULT = 34;
	public static final int MSG_STATUS_IQ_ERROR = 35;
	// the enum values for callbacks
	public static final int MSG_CONN_CHANGED = 40;
	public static final int MSG_PRES_RECEIVED = 41;
	public static final int MSG_MSG_RECEIVED = 42;
	public static final int MSG_IQ_RECEIVED = 43;

	// ==========================================================
	// Intents
	// ==========================================================
	public static final String INTENT_PREFERENCES = "de.tudresden.inf.rn.mobilis.mxa.PREFERENCES";
	public static final String INTENT_SERVICEMONITOR = "de.tudresden.inf.rn.mobilis.mxa.SERVICEMONITOR";

	// ==========================================================
	// Broadcasts
	// ==========================================================
	
	public static final String BROADCAST_PRESENCE = "de.tudresden.inf.rn.mobilis.mxa.intent.PRESENCE";
	
	// ==========================================================
	// Message provider
	// ==========================================================

	public static final String MESSAGE_AUTHORITY = "de.tudresden.inf.rn.mobilis.mxa.provider.messages";
	
	// ==========================================================
	// Multi User Chat
	// ==========================================================
	
	public static final String MUC_IS_MODERATED = "ismoderated";
	public static final String MUC_IS_MEMEBERSONLY = "ismembersonly";
	public static final String MUC_IS_PASSWORDPROTECTED = "ispasswordprotected";
	public static final String MUC_IS_PERSISTENT = "ispersitent";
	public static final String MUC_DESCRIPTION = "description";
	public static final String MUC_SUBJECT = "subject";
	public static final String MUC_OCCUPANTSCOUNT = "occupantscount";
	

	/**
	 * Roster table
	 */
	public static final class MessageItems implements BaseColumns {
		// This class cannot be instantiated
		private MessageItems() {
		}

		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ MESSAGE_AUTHORITY + "/messageitems");

		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of
		 * message items.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mxa.message";

		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
		 * message item.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mxa.message";

		/**
		 * The sender
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String SENDER = "sender";

		/**
		 * The recipient
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String RECIPIENT = "recipient";

		/**
		 * The subject
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String SUBJECT = "subject";

		/**
		 * The body
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String BODY = "body";

		/**
		 * The timestamp for when the message was sent
		 * <P>
		 * Type: INTEGER (long from System.curentTimeMillis())
		 * </P>
		 */
		public static final String DATE_SENT = "date_sent";

		/**
		 * If the message was read (0 false, 1 true)
		 * <P>
		 * Type: INTEGER (no boolean available)
		 * </P>
		 */
		public static final String READ = "read";

		/**
		 * The type
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String TYPE = "type";

		/**
		 * The status
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String STATUS = "status";

		/**
		 * The default sort order for this table
		 */
		public static final String DEFAULT_SORT_ORDER = "";
	}

	// ==========================================================
	// Roster provider
	// ==========================================================

	public static final String ROSTER_AUTHORITY = "de.tudresden.inf.rn.mobilis.mxa.provider.roster";

	/**
	 * Roster table
	 */
	public static final class RosterItems implements BaseColumns {
		// This class cannot be instantiated
		private RosterItems() {
		}

		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ ROSTER_AUTHORITY + "/rosteritems");

		/**
		 * The MIME type of {@link #CONTENT_URI} providing a directory of roster
		 * items.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mxa.roster";

		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
		 * roster item.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mxa.roster";

		/**
		 * The XMPP ID
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String XMPP_ID = "xmpp_id";

		/**
		 * The name
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String NAME = "name";

		/**
		 * The presence mode
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String PRESENCE_MODE = "mode";

		/**
		 * The presence status
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String PRESENCE_STATUS = "status";

		/**
		 * The timestamp for when the item was last modified
		 * <P>
		 * Type: INTEGER (long from System.currentTimeMillis())
		 * </P>
		 */
		public static final String UPDATED_DATE = "updated";

		/**
		 * The default sort order for this table
		 */
		public static final String DEFAULT_SORT_ORDER = "CASE mode WHEN 'online' THEN 1 WHEN 'away' THEN 2 WHEN 'xa' THEN 3 WHEN 'dnd' THEN 4 WHEN 'offline' THEN 5 END, name";
	}
}
