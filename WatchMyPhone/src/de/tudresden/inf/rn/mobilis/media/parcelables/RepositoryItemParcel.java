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
package de.tudresden.inf.rn.mobilis.media.parcelables;

import java.util.HashMap;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;

public class RepositoryItemParcel implements Parcelable {

	private static final long serialVersionUID = 1L;
	
	public static final Parcelable.Creator<RepositoryItemParcel> CREATOR
			= new Parcelable.Creator<RepositoryItemParcel>() {
		public RepositoryItemParcel createFromParcel(Parcel in) {
			return new RepositoryItemParcel(in);
		}
		public RepositoryItemParcel[] newArray(int size) {
			return new RepositoryItemParcel[size];
		}
	};
	
	public String content;
	public String owner;
	public String uid;
	public Map<String,String> slices
			= new HashMap<String,String>();
	
	public RepositoryItemParcel() {}
	
	public RepositoryItemParcel(Parcel in) {
		this.readFromParcel(in);
	}
	
	private void readFromParcel(Parcel in) {
		this.content = in.readString();
		this.owner = in.readString();
		this.uid = in.readString();
		final int n = in.readInt();
		for (int i = 0; i < n; i++)
			this.slices.put(in.readString(), in.readString());
	}
	
	public void writeToParcel(Parcel out, int flags) {
		Map<String,String> slices = this.slices;
		out.writeString(this.content);
		out.writeString(this.owner);
		out.writeString(this.uid);
		out.writeInt(slices.size());
		for (String key: slices.keySet()) {
			out.writeString(key);
			out.writeString(slices.get(key));
		}
	}
	
	public int describeContents() {
		return 0;
	}
	
}
