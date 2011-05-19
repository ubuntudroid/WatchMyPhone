package de.tudresden.inf.rn.mobilis.server.services.locpairs;

import java.util.Date;
import java.util.Map;

import de.tudresden.inf.rn.mobilis.server.services.locpairs.LocPairs;
import de.tudresden.inf.rn.mobilis.server.services.locpairs.LocPairsServerTime;
import de.tudresden.inf.rn.mobilis.server.services.locpairs.Player;

/**
 * The Class Round represents a round in the game locpairs with its start time,
 * the roundnumber and the uncovered cards and the player uncovered these cards
 * 
 * @author Reik Mueller
 */
public class Round {

	private Date startTime = null;
	private int duration = 60000;
	private int number = 0;
	private String card1BarCode = null;
	private Player player1 = null;
	private String card2BarCode = null;
	private Player player2 = null;
	private Team activeTeam = null;
	private LocPairs game = null;

	public Team getActiveTeam() {
		return activeTeam;
	}

	public void setActiveTeam(Team activeTeam) {
		this.activeTeam = activeTeam;
	}

	/**
	 * Instantiates a new round.
	 * 
	 * @param observer
	 *            the observer
	 */
	public Round(LocPairs game) {
		this.game = game;
	}

	/**
	 * ClearBarcodes. Sets the uncovered cards to null.
	 */
	public void clear() {
		card1BarCode = null;
		card2BarCode = null;
		player1 = null;
		player2 = null;
		activeTeam = null;
		startTime = null;
	}

	/**
	 * Increase number. increases the round number
	 */
	public void increaseNumber() {
		number++;
	}

	/**
	 * Gets the number.
	 * 
	 * @return the number
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * Sets the number.
	 * 
	 * @param number
	 *            the new number
	 */
	public void setNumber(int number) {
		this.number = number;
	}

	/**
	 * Gets the uncovered card1.
	 * 
	 * @return the uncovered card1
	 */
	public String getUncoveredCard1() {
		return card1BarCode;
	}

	/**
	 * Gets the uncovered card2.
	 * 
	 * @return the uncovered card2
	 */
	public String getUncoveredCard2() {
		return card2BarCode;
	}

	/**
	 * Uncover card.
	 * 
	 * @param uncoveredCard
	 *            the uncovered card
	 */
	public void uncoverCard(String uncoveredCard, Player player) {
		System.out.println("Round.uncoverCard");
		if (card1BarCode == null) {
			card1BarCode = uncoveredCard;
			player1 = player;
			player.setActive(false);
		} else {
			card2BarCode = uncoveredCard;
			player2 = player;
			player.setActive(false);

		}
	}

	/**
	 * Compare cards.
	 * 
	 * @return true, if successful
	 */
	public boolean compareCards() {
		boolean isEqual = false;
		if (card1BarCode != null && card2BarCode != null) {
			if (card1BarCode.equals(card2BarCode))return false;
			Map<String, String> pairs = game.getPairs();
			if (pairs.get(card1BarCode).equals(pairs.get(card2BarCode))){
				isEqual = true;
			}else{
				isEqual = false;;
			}
		}
//		System.out.println("Round.compareCards() " + card1BarCode + ":" + card2BarCode + " -> " + isEqual);
		return isEqual;
	}

	/**
	 * Sets the start time.
	 * 
	 * @param startTime
	 *            the new start time
	 */
	public void setStartTime() {
		this.startTime = LocPairsServerTime.getTime();
	}

	/**
	 * Sets the duration.
	 * 
	 * @param duration
	 *            the new duration
	 */
	public void setDuration(int duration) {
		this.duration = duration;
	}

	/**
	 * Gets the scores.
	 * 
	 * @return the scores
	 */

	/**
	 * Gets the start time.
	 * 
	 * @return the start time
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * Gets the duration.
	 * 
	 * @return the duration
	 */
	public int getDuration() {
		return duration;
	}

	/**
	 * Checks if the round is finished. A round ended when two Cards were
	 * uncovered.
	 * 
	 * @return true, if is finished
	 */
	public boolean isFinished() {
		if (card1BarCode != null && card2BarCode != null)
			return true;
		return false;
	}

	/**
	 * Gets the player that uncovered the first card.
	 * 
	 * @return the player1
	 */
	public Player getPlayer1() {
		return player1;
	}

	/**
	 * Gets the player that uncovered the second card.
	 * 
	 * @return the player2
	 */
	public Player getPlayer2() {
		return player2;
	}

}
