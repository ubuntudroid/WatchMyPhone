/*******************************************************************************
 * Copyright (C) 2010 Technische Universität Dresden
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: http://mobilisplatform.sourceforge.net
 ******************************************************************************/
package de.tudresden.inf.rn.mobilis.media;

import de.tudresden.inf.rn.mobilis.mxa.ConstMXA;
import de.tudresden.inf.rn.mobilis.xmpp.beans.Mobilis;

public final class ConstMMedia {
		
	public static final class intent {
		private intent() {}
		public static final String SEND           = "de.tudresden.inf.rn.mobilis.media.intent.SEND";
		public static final String SEND_TO_JID    = "de.tudresden.inf.rn.mobilis.media.intent.SEND_TO_JID";
		public static final String SEND_TO_REP    = "de.tudresden.inf.rn.mobilis.media.intent.SEND_TO_REP";
		public static final String CHECK_TRANSFER = "de.tudresden.inf.rn.mobilis.media.intent.CHECK_TRANSFER";
		public static final String DISPLAY_REPOSITORYITEM = "de.tudresden.inf.rn.mobilis.media.intent.DISPLAY_REPOSITORYITEM";
		public static final String PREF_XMPP      = ConstMXA.INTENT_PREFERENCES;
		
		public static class extra {
			private extra() {}
			// for SEND_TO_JID, SEND_TO_REP
			public static final String STR_TO          = "STR_TO";
			public static final String STR_DESCRIPTION = "STR_DESCRIPTION";
			public static final String STRA_PATHS      = "STRA_PATHS";
			// for SEND_TO_REP
			public static final String BDLA_SLICES     = "BNDL_SLICES";
			public static final String STRA_REPOSITORYITEMS_UIDS = "STRA_REPOSITORYITEM_UIDS";
			// for DISPLAY_REPOSITORYITEM
			public static final String PAR_REPOSITORYITEM = "PAR_REPOSITORYITEM";
			// for DISPLAY_REPOSITORYITEM, optionally SEND
			public static final String STR_REPOSITORY     = "STR_REPOSITORY";
			// for optionally SEND
			public static final String STR_REPOSITORYITEM_UID = "STR_REPOSITORYITEM_UID";
			// for any subactivity (intern)
			public static final String PAR_PARENTMESSENGER = "PAR_PARENTMESSENGER";
			public static final String INT_CHILDARG2       = "INT_CHILDARG2";
		}
	}
	
	public static final class enumeration {
		private enumeration() {}
		public static final int STATE_REQUESTED = 0;
		public static final int STATE_STANDBY = 1; 
		public static final int STATE_INITIATED = 2;
		public static final int STATE_NEGOTIATED = 3;
		public static final int STATE_INPROGRESS = 4;
		public static final int STATE_FINISHED = 5;
		public static final int STATE_FAILED = 6;
		
		public static final int DIRECTION_OUT = 1;
		public static final int DIRECTION_IN = 2;
		public static final int DIRECTION_BOTH = 3;
		
	}
	
	public static final class message {
		private message() {}
		
		// for TransferService
		public static final int WHAT_TRANSFERSTATECHANGED = 0;
		// for RepositoryService
		public static final int WHAT_REPOSITORY_QUERY = 1;
		public static final int WHAT_REPOSITORY_DELETE = 2;
		public static final int WHAT_REPOSITORY_DISCOVER = 3;
		public static final int WHAT_REPOSITORY_TRANSFER = 4;
		// for RepositoryActivity
		public static final int WHAT_SUBACTIVITY_REGISTER = 5;
		public static final int WHAT_SUBACTIVITY_UNREGISTER = 6;
		public static final int WHAT_SUBACTIVITY_SHOW = 7;
		public static final int WHAT_SUBACTIVITY_HIDE = 8;
		public static final int WHAT_SUBACTIVITY_OUTDATE = 9;
		public static final int WHAT_SUBACTIVITY_UPDATE = 10;
		public static final int WHAT_SUBACTIVITY_DISPLAY = 11;
		
		public static final int ARG1_SUCCESS = 0;
		public static final int ARG1_ERROR = 1;
		public static final int ARG1_INFO = 2;
		
		public static class data {
			private data() {}
			// for WHAT_TRANSFERSTATECHANGED
			public static final String PAR_TRANSFER        = "PAR_TRANSFER";
			// for WHAT_REPOSITORY_QUERY, WHAT_REPOSITORY_DELETE, WHAT_SUBACTIVITY_UPDATE 
			public static final String PARA_REPOSITORYITEM = "PARA_REPOSITORYITEM";
			// for WHAT_REPOSITORY_DISCOVER
			public static final String STRL_REPOSITORIES   = "STRL_REPOSITORIES";
			// for WHAT_SUBACTIVITY_REGISTER
			public static final String PAR_CHILDMESSENGER  = "PAR_CHILDMESSENGER";
			// for WHAT_SUBACTIVITY_UPDATE
			public static final String PAR_CONDITION       = "PAR_CONDITION";
			// for WHAT_SUBACTIVITY_DISPLAY
			public static final String PAR_REPOSITORYITEM  = "PAR_REPOSITORYITEM";
		}		
	}
	
	public static final class database {
		private database() {}
		public static final String SLICE_TITLE        = "title";
		public static final String SLICE_DESCRIPTION  = "description";
		public static final String SLICE_TAKEN        = "taken";
		public static final String SLICE_LATITUDE_E6  = "latitude_e6";
		public static final String SLICE_LONGITUDE_E6 = "longitude_e6";
		public static final String SLICE_OWNER        = "owner";
	}
	
	public static final class namespace {
		private namespace() {}
		public static final String SERVICES = Mobilis.NAMESPACE + "#services";
		public static final String REPOSITORY_SERVICE = SERVICES + "/RepositoryService";
	}
}
