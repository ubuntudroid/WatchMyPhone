package de.tudresden.inf.rn.mobilis.server.services.locpairs;

import java.util.TimerTask;

import de.tudresden.inf.rn.mobilis.server.services.locpairs.LocPairs;

/**
 * The Class PlayerUpdateTimerTask. It's an extension of the class TimerTask
 * to start the method Game.playerUpdate().
 * 
 * @author Reik Mueller
 */
public class PlayerUpdateTask extends TimerTask {

	private LocPairs game = null;
	
	/** The Constant frequency describes the time span between 
	 * 	the two invocations of Game.playerUpdate().
	 */
	public static final long delay = 10000;
	
	/**
	 * Instantiates a new PlayerUpdateTimerTask.
	 *
	 * @param game the game
	 */
	public PlayerUpdateTask(LocPairs game) {
		this.game = game;
	}
	
	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
//		System.out.println("Roundstarter.run()");
		game.playerUpdate(true);
		this.cancel();
	}
}
