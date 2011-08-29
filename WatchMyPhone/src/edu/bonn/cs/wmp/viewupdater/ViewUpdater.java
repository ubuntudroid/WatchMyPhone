package edu.bonn.cs.wmp.viewupdater;

import de.hdm.cefx.awareness.AwarenessEvent;
import de.hdm.cefx.concurrency.operations.Operation;
import de.hdm.cefx.concurrency.operations.OperationData;

public abstract class ViewUpdater {
	protected Operation operation;

	public boolean hasInterestIn(AwarenessEvent event) {
		Object eventData = event.getEvent();
		if (eventData instanceof OperationData) {
			operation = ((OperationData) eventData).getOperation();
			return true;
		} else {
			return false;
		}
	}

	public abstract void notifyOfAwarenessEvent(AwarenessEvent event);
}
