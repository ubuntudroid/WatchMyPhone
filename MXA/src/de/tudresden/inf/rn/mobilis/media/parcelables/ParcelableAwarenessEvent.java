package de.tudresden.inf.rn.mobilis.media.parcelables;

import java.io.Serializable;

import org.jivesoftware.smackx.pubsub.NodeType;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;
import de.hdm.cefx.awareness.AwarenessEvent;
import de.hdm.cefx.concurrency.operations.InsertOperationImpl;
import de.hdm.cefx.concurrency.operations.NodePosition;
import de.hdm.cefx.concurrency.operations.OperationData;
import de.hdm.cefx.concurrency.operations.UpdateInsertOperation;
import de.hdm.cefx.concurrency.operations.UpdateOperationImpl;
import de.hdm.cefx.exceptions.NodePositionException;

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
		
		// workaround for non-parcelable InsertOperationImpl
		if (event.getEvent() instanceof OperationData) {
			OperationData opData = (OperationData) event.getEvent();
			if (opData.getOperation() instanceof InsertOperationImpl) {
				InsertOperationImpl insOp = (InsertOperationImpl) opData.getOperation();
				if (insOp.getInsertNode() instanceof Element
						&& insOp.getInsertNode().getFirstChild() instanceof Text) {
					final Text textNode = (Text) insOp.getInsertNode()
							.getFirstChild().cloneNode(true);
					UpdateInsertOperation upIns;
					try {
						/*
						 *  most of these fields just get some dummy values as we just need
						 *  them for the constructor
						 */
						upIns = new UpdateInsertOperation(textNode.getData(), 0, Node.TEXT_NODE, new NodePosition("dummy", null, NodePosition.INSERT_BEFORE), "");
						UpdateOperationImpl upOp = new UpdateOperationImpl(upIns, insOp.getStateVector(), insOp.getClientName(), insOp.getClientId());
						opData.setOperation(upOp);
						setEvent(opData);
					} catch (DOMException e) {
						// should never be fired due to the nature of our intitialization values
						e.printStackTrace();
					} catch (NodePositionException e) {
						// should never be fired due to the nature of our intitialization values
						e.printStackTrace();
					}
				}
			}
		}
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
