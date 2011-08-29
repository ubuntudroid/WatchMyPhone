package de.tudresden.inf.rn.mobilis.media.parcelables;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;
import de.hdm.cefx.awareness.AwarenessEvent;

/**
 * This class acts as a wrapper for CEFX-AwarenessEvent objects. It
 * converts them into Parcelables so that they can be sent to other
 * processes via intents or IPC.
 * @author Sven Bendel
 *
 */
@SuppressWarnings("serial")
public class ParcelableAwarenessEvent extends AwarenessEvent implements
		Parcelable {

	public static final Parcelable.Creator<ParcelableAwarenessEvent> CREATOR = new
			Parcelable.Creator<ParcelableAwarenessEvent>() {
	    public ParcelableAwarenessEvent createFromParcel(Parcel in) {
	        return new ParcelableAwarenessEvent(in);
	    }
	
	    public ParcelableAwarenessEvent[] newArray(int size) {
	        return new ParcelableAwarenessEvent[size];
	    }
	};

	
	public ParcelableAwarenessEvent(String type, String description, Object obj,
			String source) {
		super(type, description, obj, source);
	}
	
	public ParcelableAwarenessEvent(Parcel in) {
		super("", "", null, "");
		this.readFromParcel(in);
	}
	
	public ParcelableAwarenessEvent(AwarenessEvent event) {
		super(event.getType(), event.getDescription(), event.getEvent(), event.getEventSource());
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.type);
		dest.writeString(this.description);
		dest.writeSerializable((Serializable) this.event);
		dest.writeString(this.eventSource);
	}
	
	public synchronized void readFromParcel(Parcel p) {
		this.type = p.readString();
		this.description = p.readString();
		this.event = p.readSerializable();
		this.eventSource = p.readString();
	}
}
