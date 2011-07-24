package de.tudresden.inf.rn.mobilis.media.parcelables;

import de.tudresden.inf.rn.mobilis.xmpp.beans.ConditionInfo;
import android.os.Parcelable;
import android.os.Parcel;

public class ConditionParcel implements Parcelable {
	private static final long serialVersionUID = 1L;

	public static final int OP_EQ = 0;

	public static final int OP_NE = 1;

	public static final int OP_GT = 2;

	public static final int OP_LT = 3;

	public static final int OP_GE = 4;

	public static final int OP_LE = 5;

	public static final int OP_AND = 6;

	public static final int OP_OR = 7;

	public static final int OP_NOT = 8;

	public String key = null;
	public int op = ConditionInfo.OP_EQ;
	public String value = null;
	public ConditionParcel[] conditions = new ConditionParcel[0];
	public static final Creator<ConditionParcel> CREATOR = new Parcelable.Creator<ConditionParcel>() {
		public ConditionParcel createFromParcel(Parcel in) {
			return new ConditionParcel(in);
		}

		public ConditionParcel[] newArray(int size) {
			return new ConditionParcel[size];
		}
	};

	public ConditionParcel() {
	}

	public int describeContents() {
		return 0;
	}

	public ConditionParcel(Parcel in) {
		this.readFromParcel(in);
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
