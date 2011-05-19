package de.tudresden.inf.rn.mobilis.server.services.locpairs;

import java.util.TimerTask;

import de.tudresden.inf.rn.mobilis.server.services.locpairs.LocPairs;

/**
 * The Class CleanUpTask. 
 * This Class is used to invoke the cleanup process of the game.
 * It defines all times to control this process. 
 *
 * @author Reik Mueller
 */
public class CleanUpTask extends TimerTask {

	private LocPairs game = null;
	
	/** The Constant invocationFrequence. */
	public static final long invocationFrequence = 10000;
	
	/** The Constant quitLobbyDelay. */
	public static final long quitLobbyDelay = 60000;
	
	/** The Constant quitOfflinePlayerDelay. */
	public static final long quitOfflinePlayerDelay = 30000;
	
	
	/**
	 * Instantiates a new cleanup task.
	 *
	 * @param game the game
	 */
	public CleanUpTask(LocPairs game) {
		this.game = game;
	}
	
	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		game.cleanUp();
		this.cancel();
	}
}