package edu.bonn.cs.wmp.viewupdater;

import de.hdm.cefx.awareness.AwarenessWidget;
import de.hdm.cefx.concurrency.operations.Operation;
import de.hdm.cefx.concurrency.operations.OperationData;
import de.hdm.cefx.awareness.AwarenessController;
import de.hdm.cefx.awareness.AwarenessEvent;

public abstract class ViewUpdater implements AwarenessWidget {
	protected Operation operation;
	protected AwarenessController ac;

	@Override
	public void init() {
	}

	@Override
	public boolean hasInterestIn(AwarenessEvent event) {
		Object eventData = event.getEvent();
		if (eventData instanceof OperationData) {
			operation = ((OperationData) eventData).getOperation();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void setAwarenessController(AwarenessController ac) {
		this.ac = ac;
	}

	@Override
	public abstract void notifyOfAwarenessEvent(AwarenessEvent event);
}
