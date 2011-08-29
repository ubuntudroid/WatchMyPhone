package de.hdm.cefx.awareness;

/**
 * This interface acts as a hub for CEFX classes which want to access
 * the MXA-CollabEditingService.
 * @author Sven Bendel
 *
 */
public interface CEFXtoMobilisHub {
	
	public void sendCEFXAwarenessEventToMyself(AwarenessEvent event);
	
	public void onAwarenessEventReceived(AwarenessEvent event);
}
