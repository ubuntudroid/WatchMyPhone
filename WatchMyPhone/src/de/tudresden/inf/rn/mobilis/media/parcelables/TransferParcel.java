/*******************************************************************************
 * Copyright (C) 2010 Technische Universit�t Dresden
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
package de.tudresden.inf.rn.mobilis.media.parcelables;

import android.os.Parcel;
import android.os.Parcelable;
import de.tudresden.inf.rn.mobilis.media.ConstMMedia;
import de.tudresden.inf.rn.mobilis.mxa.services.parcelable.FileTransfer;

public class TransferParcel implements Parcelable {

	public FileTransfer xmppFile = null;
	public int id = 0;
	public int state = ConstMMedia.enumeration.STATE_STANDBY;
	public int direction = ConstMMedia.enumeration.DIRECTION_OUT;
	public int blocksTransferred = 0;
	public long bytesTransferred = 0;
	
	public static final Parcelable.Creator<TransferParcel> CREATOR
			= new Parcelable.Creator<TransferParcel>() {
		public TransferParcel createFromParcel(Parcel in) {
			return new TransferParcel(in);
		}
		public TransferParcel[] newArray(int size) {
			return new TransferParcel[size];
		}
	};
	
	public TransferParcel() {	}
	
	public TransferParcel(Parcel in) {
		this.readFromParcel(in);
	}
	
	public TransferParcel(int id, int state, int direction, int blocksTransferred, long bytesTransferred) {
		this.id = id;
		this.state = state;
		this.direction = direction;
		this.blocksTransferred = blocksTransferred;
		this.bytesTransferred = bytesTransferred;
	}
	
	public int describeContents() {
		return 0;
	}
	
	private void readFromParcel(Parcel in) {
		this.id = in.readInt();
		this.state = in.readInt();
		this.blocksTransferred = in.readInt();
		this.bytesTransferred = in.readLong();
		this.xmppFile = in.readParcelable(null);
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(this.id);
		out.writeInt(this.state);
		out.writeInt(this.blocksTransferred);
		out.writeLong(this.bytesTransferred);
		out.writeParcelable(this.xmppFile, flags);
	}

}
