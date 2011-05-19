package de.tudresden.inf.rn.mobilis.server.services.locpairs;

import java.util.TimerTask;

import de.tudresden.inf.rn.mobilis.server.services.locpairs.LocPairs;

/**
 * The Class RoundRestarter. It's an extension of the class TimerTask
 * to start a round.
 * 
 * @author Reik Mueller
 */
public class RoundRestarterTask extends TimerTask {

	private LocPairs game = null;
	
	/**
	 * Instantiates a new round restarter.
	 *
	 * @param game the game
	 */
	public RoundRestarterTask(LocPairs game) {
		this.game = game;
	}
	
	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
//		System.out.println("Roundstarter.run()");
		game.startRound();
//		this.cancel();
	}
}
