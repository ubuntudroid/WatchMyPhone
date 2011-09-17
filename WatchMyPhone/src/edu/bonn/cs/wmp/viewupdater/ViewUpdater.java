package edu.bonn.cs.wmp.viewupdater;

import de.hdm.cefx.awareness.AwarenessEvent;
import de.hdm.cefx.concurrency.operations.Operation;
import de.hdm.cefx.concurrency.operations.OperationData;
import edu.bonn.cs.wmp.application.WMPApplication;
import edu.bonn.cs.wmp.views.WMPView;

/**
 * The view updater is responsible for informing a collaborative
 * view of a change (in the form of a {@link AwarenessEvent} in the local document copy of the Collaborative
 * Editing Framework.
 * 
 * @author Sven Bendel
 * @author Dirk Hering
 */
public abstract class ViewUpdater {
	protected Operation operation;

	/**
	 * This method is called by {@link WMPApplication} to check if this
	 * view updater is interested in a particular new {@link AwarenessEvent}.
	 * 
	 * This method is not really necessary for the functioning of the WMP concept,
	 * but was introduced to split up the decision making about the usefulness of a
	 * {@link AwarenessEvent} for a view updater and the actual processing. 
	 * @param event
	 * 			the {@link AwarenessEvent} to be propagated in case this view
	 * 			updater is interested in it
	 * @return
	 * 			<code>true</code>, if this view updater wants to get notified
	 * 			about this AwarenessEvent, <code>false<code> otherwise
	 */
	public boolean hasInterestIn(AwarenessEvent event) {
		Object eventData = event.getEvent();
		if (eventData instanceof OperationData) {
			operation = ((OperationData) eventData).getOperation();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method allows the processing of an incoming {@link AwarenessEvent}. It is
	 * called by {@link WMPApplication} on arrival of a new event if this view updater
	 * has returned <code>true</code> on the precedent call to {@link #hasInterestIn(AwarenessEvent)}. 
	 * @param event
	 * 			the new AwarenessEvent
	 */
	public abstract void notifyOfAwarenessEvent(AwarenessEvent event);
	
	/**
	 * @return the currently attached {@link WMPView}
	 */
	public abstract WMPView getWMPView();
}
