package de.tudresden.inf.rn.mobilis.media.parcelables;

import android.os.Parcelable;
import de.tudresden.inf.rn.mobilis.media.ConstMMedia;
import de.tudresden.inf.rn.mobilis.mxa.services.parcelable.FileTransfer;
import android.os.Parcel;

public class TransferParcel implements Parcelable {
	public FileTransfer xmppFile = null;
	public int id = 0;
	public int state = ConstMMedia.enumeration.STATE_STANDBY;
	public int direction = ConstMMedia.enumeration.DIRECTION_OUT;
	public int blocksTransferred = 0;
	public long bytesTransferred = 0;
	public static final Creator<TransferParcel> CREATOR = new Parcelable.Creator<TransferParcel>() {
		public TransferParcel createFromParcel(Parcel in) {
			return new TransferParcel(in);
		}

		public TransferParcel[] newArray(int size) {
			return new TransferParcel[size];
		}
	};

	public TransferParcel() {
	}

	public TransferParcel(int id, int state, int direction,
			int blocksTransferred, long bytesTransferred) {
		this.id = id;
		this.state = state;
		this.direction = direction;
		this.blocksTransferred = blocksTransferred;
		this.bytesTransferred = bytesTransferred;
	}

	public int describeContents() {
		return 0;
	}

	public TransferParcel(Parcel in) {
		this.readFromParcel(in);
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
