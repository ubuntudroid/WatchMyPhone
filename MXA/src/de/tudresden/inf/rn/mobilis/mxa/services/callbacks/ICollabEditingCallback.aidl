package de.tudresden.inf.rn.mobilis.mxa.services.callbacks;

import de.tudresden.inf.rn.mobilis.media.parcelables.ParcelableAwarenessEvent;

interface ICollabEditingCallback {
	
	void onAwarenessEventReceived(in ParcelableAwarenessEvent event);
	
}