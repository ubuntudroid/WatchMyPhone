package de.tudresden.inf.rn.mobilis.media.parcelables;

import android.os.Parcelable;
import java.util.HashMap;
import java.util.Map;
import android.os.Parcel;

public class RepositoryItemParcel implements Parcelable {
	private static final long serialVersionUID = 1L;

	public static final Creator<RepositoryItemParcel> CREATOR = new Parcelable.Creator<RepositoryItemParcel>() {
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
	public Map<String, String> slices = new HashMap<String, String>();

	public RepositoryItemParcel() {
	}

	public int describeContents() {
		return 0;
	}

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
		Map<String, String> slices = this.slices;
		out.writeString(this.content);
		out.writeString(this.owner);
		out.writeString(this.uid);
		out.writeInt(slices.size());
		for (String key : slices.keySet()) {
			out.writeString(key);
			out.writeString(slices.get(key));
		}
	}

}
