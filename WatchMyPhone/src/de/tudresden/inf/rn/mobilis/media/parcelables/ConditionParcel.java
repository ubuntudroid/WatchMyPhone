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
import de.tudresden.inf.rn.mobilis.xmpp.beans.ConditionInfo;

public class ConditionParcel implements Parcelable {

	private static final long serialVersionUID = 1L;

	public static final int OP_EQ = 0;
	public static final int OP_NE = 1;
	public static final int OP_GT = 2;
	public static final int OP_LT = 3;
	public static final int OP_GE = 4;
	public static final int OP_LE = 5;
	public static final int OP_AND = 6;
	public static final int OP_OR  = 7;
	public static final int OP_NOT = 8; 

	public String key = null;
	public int op = ConditionInfo.OP_EQ;
	public String value = null;
	public ConditionParcel[] conditions = new ConditionParcel[0];

	
	public static final Parcelable.Creator<ConditionParcel> CREATOR
				= new Parcelable.Creator<ConditionParcel>() {
		public ConditionParcel createFromParcel(Parcel in) {
			return new ConditionParcel(in);
		}
		public ConditionParcel[] newArray(int size) {
			return new ConditionParcel[size];
		}
	};
	
	public ConditionParcel() {}
	
	public ConditionParcel(Parcel in) {
		this.readFromParcel(in);
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeString(this.key);
		out.writeInt(this.op);
		out.writeString(this.value);
		out.writeParcelableArray(this.conditions, flags);
	}
	
	public void readFromParcel(Parcel in) {
		this.key = in.readString();
		this.op = in.readInt();
		this.value = in.readString();
		this.conditions = (ConditionParcel[]) in.readParcelableArray(null);
	}

}
