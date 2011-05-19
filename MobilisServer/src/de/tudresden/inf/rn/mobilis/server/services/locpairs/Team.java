package de.tudresden.inf.rn.mobilis.server.services.locpairs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import de.tudresden.inf.rn.mobilis.server.services.locpairs.Player;
import de.tudresden.inf.rn.mobilis.server.services.locpairs.Team;


/**
 * The Class Team represents a two player team. It manages the score.
 * 
 * @author Reik Mueller
 */

@Entity
public class Team implements Comparable<Team>{
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private int dbID = 0;
	@Transient
	private String id = null;
	private String name = null;
	private Long score = new Long(0);
	@Transient
	private Player player1 = null;
	@Transient
	private Player player2 = null;
	@Transient
	private int number;
	@Transient
	private int lastActiveRound = -1;
	
	public class TeamComparator implements Comparator<Team>{

		@Override
		public int compare(Team arg0, Team arg1) {
			if(arg0.getLastActiveRound() < arg1.lastActiveRound)return 1;
			if (arg0.getLastActiveRound() == arg1.lastActiveRound){
				if(arg0.getNumber() < arg1.number)return 1;
				if(arg0.getNumber() > arg1.number)return -1;
			}
			if (arg0.getLastActiveRound() > arg1.lastActiveRound)return -1;
			return 0;
		}
		
	}
	
	/**
	 * Instantiates a new team.
	 *
	 * @param player1 the player1
	 * @param player2 the player2
	 * @param id the id
	 * @param number the number
	 */
	public Team(Player player1, Player player2, String id, int number){
		this.player1 = player1;
		this.player2 = player2;
		this.id = id;
		this.number = number;
		this.name = this.player1.getName() + " + " + this.player2.getName();
	}
	
	public Team(){}
	
	/**
	 * Checks whether a specific player is member of this team.
	 *
	 * @param player the player
	 * @return true, if successful
	 */
	public boolean hasPlayer(Player player){
		if(player.equals(player1))return true;
		if(player.equals(player2))return true;
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return "Team - nr: " + this.number /*+ " lR: " + lastActiveRound*/ + " sc: " + score;
	}
	
	/**
	 * Gets the team number.
	 *
	 * @return the number
	 */
	public int getNumber() {
		return number;
	}
	
	/**
	 * Sets the team number.
	 *
	 * @param number the new number
	 */
	public void setNumber(int number) {
		this.number = number;
	}
	
	/**
	 * Gets the last round the team was active playing (uncovering cards).
	 *
	 * @return the last active round
	 */
	public int getLastActiveRound() {
		return lastActiveRound;
	}
	
	/**
	 * Sets the last round the team was active playing (uncovering cards).
	 *
	 * @param lastActiveRound the new last active round
	 */
	public void setLastActiveRound(int lastActiveRound) {
		//if(!gotPair)
		this.lastActiveRound = lastActiveRound;
	}
	
	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Gets the name. The name is build of the two names of the team members.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns all players in the team.
	 *
	 * @return the players
	 */
	public Collection<Player> getPlayers() {
		ArrayList<Player> players = new ArrayList<Player>();
		players.add(player1);
		players.add(player2);
		return players;
	}
	
	/**
	 * Sets the the first player.
	 *
	 * @param player1 the new player1
	 */
	public void setPlayer1(Player player1) {
		this.player1 = player1;
	}
	
	/**
	 * Sets the second player.
	 *
	 * @param player2 the new player2
	 */
	public void setPlayer2(Player player2) {
		this.player2 = player2;
	}
	
	/**
	 * Gets the score.
	 *
	 * @return the score
	 */
	public Long getScore() {
		return score;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Team arg0) {
		// Es wird erst nach letzter aktiver spielrunde und dann nach teamnummer sortiert
//		System.out.println("Team.compareTo() Team" + this.number + ".lastRound: " + this.lastActiveRound + "  Team" + arg0.getNumber() + ".lastRound: " + arg0.getLastActiveRound());
		if(arg0.getLastActiveRound() < this.lastActiveRound)return 1;
		if (arg0.getLastActiveRound() == this.lastActiveRound){
			if(arg0.getNumber() < this.number)return 1;
			if(arg0.getNumber() > this.number)return -1;
		}
		if (arg0.getLastActiveRound() > this.lastActiveRound)return -1;
		return 0;
	}
	
	/**
	 * Equals. the criteria is the id (jid)
	 *
	 * @param team the team
	 * @return true, if successful
	 */
	public boolean equals(Team team) {
		if(team.getId().equals(this.id))return true;
		return false;
	}
	
	/**
	 * Increases the team score.
	 */
	public void increaseScore(){
		System.out.println("Team " + name + " increaseScore()");
		score ++;
	}

	public int getDbID() {
		return dbID;
	}

	public void setDbID(int id1) {
		this.dbID = id1;
	}
	
}
