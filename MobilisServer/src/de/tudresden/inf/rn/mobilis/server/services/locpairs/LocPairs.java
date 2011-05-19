package de.tudresden.inf.rn.mobilis.server.services.locpairs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import de.tudresden.inf.rn.mobilis.server.services.AppSpecificService;
import de.tudresden.inf.rn.mobilis.server.services.CoordinatorService;
import de.tudresden.inf.rn.mobilis.server.services.locpairs.GamePacketFilter;
import de.tudresden.inf.rn.mobilis.server.services.locpairs.GamePacketListener;
import de.tudresden.inf.rn.mobilis.server.services.locpairs.LocPairsServerTime;
import de.tudresden.inf.rn.mobilis.server.services.locpairs.NetworkFingerprintDAO;
import de.tudresden.inf.rn.mobilis.server.services.locpairs.Player;
import de.tudresden.inf.rn.mobilis.server.services.locpairs.PlayerUpdateTask;
import de.tudresden.inf.rn.mobilis.server.services.locpairs.Round;
import de.tudresden.inf.rn.mobilis.server.services.locpairs.RoundRestarterTask;
import de.tudresden.inf.rn.mobilis.server.services.locpairs.Team;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.EndGameBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.GameInformationBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.EndGameBean.EndType;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.GeoPosition;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.NetworkFingerPrint;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanIQAdapter;

// TODO: Auto-generated Javadoc
/**
 * The Class LocPairs. The main Class, that holds and controls everything necessary. 
 * It contains the lobby and the game logic.
 * 
 * @author Reik Mueller
 */

public class LocPairs extends AppSpecificService {
	
	private int startcount = 4;
	
	private Map<String, String> settings = new HashMap<String, String>();
	private NetworkFingerprintDAO fingerPrintWriter = null;
	private HighScoreDAO highscoreDAO = new HighScoreDAO();
	private String gameId = null;
	private Collection<String> barcodes = new ArrayList<String>();
	private Collection<String> imageURLs = new ArrayList<String>();
	private Map<String, GeoPosition> barcodePositions = new HashMap<String, GeoPosition>();
	private Map<String, String> pairs = new HashMap<String, String>();
	private Connection connection = null;
	private List<Team> teams = new ArrayList<Team>();
	private Set<Player> playersWithoutTeam = new HashSet<Player>();
	private Set<Player> quitedPlayers = new HashSet<Player>();
	private int playerReady = 0;
	private int joinedPlayers = 0;
	private Timer timer = new Timer();
	private int discoveredPairs = 0;
	private Round round = new Round(this);
	private boolean isRunning = false;
	private Player opener = null;
	private TimerTask roundTask = null;
	private Long lobbyUse = new Long(0);

	/**
	 * Instantiates a new game.
	 *
	 * @param coordinator the coordinator
	 * @param password the password
	 */
	public LocPairs(CoordinatorService coordinator, String password, String serviceName) {
		super(coordinator, password, serviceName);
		init();
		System.out.println("LocPairsService (ServiceName="+serviceName+") started up.");
	}

	/**
	 * Sends the game information in return to an game information IQ back to
	 * the requesting client.
	 * 
	 * @param toJid
	 *            the jid where the result has to be send to
	 * @return true, if successful
	 */
	public boolean returnGameInfomation(String toJid) {
//		if (isRunning)return false;
		
		Collection<String> playernames = new ArrayList<String>();
			for (Player p : getPlayers().keySet()) {
			playernames.add(p.getName());
		}
		
		GameInformationBean bean = new GameInformationBean(opener.getName(),
				playernames, startcount);
		bean.setFrom(gameId);
		bean.setTo(toJid);
		bean.setType(XMPPBean.TYPE_RESULT);
		connection.getConnection().sendPacket(new BeanIQAdapter(bean));
		return true;
	}

	/**
	 * Creates the player and a team every time two players without a team are
	 * available.
	 * 
	 * @param jid
	 *            the XMPP-jid of the XMPPClient the player-object represents.
	 * @param name
	 *            the name the nick name of the player
	 * @return true, if successful. false, if the player is already added or
	 *         something went wrong.
	 */
	public boolean createPlayer(String jid, String name) {

		System.out.println("Game.ceatePlayer()");

		lobbyUse = LocPairsServerTime.getMilli();

		// Create new Player
		Player player = new Player(jid, name, this);
		if (opener == null) {
			opener = player;
		}
		// avoid double joins and joins when the game is already active
		if (hasPlayer(player) || startcount == 0 || isRunning) {
			System.out.println("Anmeldung verweigert (" + player.getName()
					+ "! startcount: " + startcount + " spiel läuft bereits: "
					+ isRunning + " bereits angemeldet: " + hasPlayer(player));
			return false;
		} else {
			// join the player
			joinedPlayers++;
			playersWithoutTeam.add(player);
			createTeam();
			
			player.joinGame();
			playerUpdate(false);

			System.out.println("teamsize: " + teams.size()
					+ " playerswithout team: " + playersWithoutTeam.toString()
					+ " quited players: " + quitedPlayers.toString());
			System.out.println("Alle Spieler: "
					+ getPlayers().keySet().toString());
			if (teams.size() == startcount / 2) {
				System.out
						.println("Game.createPlayer() startGame() wird aufgerufen");
				startGame();
			}
			return true;
		}
	}

	/**
	 * With this method the game becomes aware of the player that are ready to
	 * play. If all players have invoked this method the first round is started.
	 * 
	 * @param player
	 *            the player
	 * @return true, if successful
	 */
	public boolean playerReadyToPlay(Player player) {
		// check that the player, calling to be readyToPlay is in a team
		for (Team t : teams) {
			if (t.hasPlayer(player))
				playerReady++;
		}
		// when all players signaled to be ready the first round of the game
		// will be started
		if (startcount == playerReady) {
			return startRound();
		}
		return false;
	}

	/**
	 * Deletes a player, its team-mate and its team from the game and notifies
	 * the other players about the changes. If there are not enough teams left
	 * to go on with the game the game will be quited automatically and the
	 * remaining teams get the high-score.
	 * 
	 * @param player
	 *            the player
	 * @param reason
	 *            the reason
	 * @return true, if successful
	 */
	public boolean quitPlayer(Player player, String reason) {
		
		Player teamMate = player.getTeamMate();
		player.endGame(EndType.ENDBYTEAMMATEQUIT);

		// remove the team of the two players
		if (player.getTeam() != null) {
			teams.remove(player.getTeam());
		}

		if (isRunning) {
			// add the player to the quitedPlayers list (it remains in the system for further use)
			quitedPlayers.add(player);
			teamMate.endGame(EndType.ENDBYTEAMMATEQUIT);

			if (player.isActive() && teams.size() >= 2) startRound();
			if (teams.size() < 2) endGame();
			
		} else {
			player.setStopped(true);
			lobbyUse = LocPairsServerTime.getMilli();
			joinedPlayers--;
			if (teamMate != null) {
				playersWithoutTeam.add(teamMate);
				System.out.println("playerswithout team: "
						+ playersWithoutTeam.toString());
			} else {
				playersWithoutTeam.remove(player);
			}
		}
		createTeam();

		playerUpdate(false);
		System.out.println("Alle Spieler: " + getPlayers().keySet().toString());
		return true;
	}
	
	
	/**
	 * Delete player.
	 *
	 * @param player the player
	 * @return true, if successful
	 */
	public boolean deletePlayer(Player player) {
		quitedPlayers.remove(player);
		return true;
	}

	/**
	 * Signalizes the Game that an card has been uncovered. The uncovered card
	 * is notified to all players to be shown. If it is the second one the
	 * actual round will be stopped and a new round begins.
	 *
	 * @param barcode the barcode
	 * @param player the player
	 * @return true, if successful
	 */
	public boolean uncoverCard(String barcode, Player player) {
		System.out.println("Game.uncoverCard()");

		round.uncoverCard(barcode, player);
		// show the uncovered card to every player
		showCard();
		// Check whether a new round can be started
		if (round.isFinished()) {
			startRound();
		}
		return true;
	}

	/**
	 * Write network-fingerprint. It is used to persist the measured signal
	 * strengths to be analyzed later on.
	 *
	 * @param fingerprint the network-fingerprint
	 * @param barCode the bar code
	 * @return true, if successful persisted.
	 */
	public boolean writeNetworkFingerprint(NetworkFingerPrint fingerprint,
			String barCode) {
		GeoPosition position = barcodePositions.get(barCode);
		if (position == null)
			return false;
		fingerprint.setPosition(position);
		return fingerPrintWriter.addFingerprint(fingerprint);
	}

	/**
	 * Player update.
	 *
	 * @param resend the resend
	 * @return true, if successful
	 */
	public boolean playerUpdate(boolean resend) {
		for (Team team : teams) {
			for (Player player : team.getPlayers()) {
				player.sendPlayerUpdate(gameId, getPlayers());
			}
		}
		for (Player player : playersWithoutTeam) {
			player.sendPlayerUpdate(gameId, getPlayers());
		}
		if (resend) {
			this.timer.schedule(new PlayerUpdateTask(this),
					PlayerUpdateTask.delay);
		}
		return true;
	}

	private void init() {
		lobbyUse = LocPairsServerTime.getMilli();
		cleanUp();
		playerUpdate(true);
		// settings = this.getSettingStrings("locpairs");
		LocPairsServerTime.getTime();
		fingerPrintWriter = new NetworkFingerprintDAO();

		barcodes.add("INFE001");
		barcodes.add("INFE005");
		barcodes.add("INFE006");
		barcodes.add("INFE007");
		barcodes.add("INFE008");
		barcodes.add("INFE009");
		barcodes.add("INFE010");
		barcodes.add("INFE015");

		for (String s : barcodes) {
			barcodePositions.put(s, new GeoPosition(0, 0, 0));
		}

		imageURLs.add("memory01");
		imageURLs.add("memory02");
		imageURLs.add("memory03");
		imageURLs.add("memory04");

		pairs.put("INFE001", "memory01");
		pairs.put("INFE005", "memory01");
		pairs.put("INFE006", "memory02");
		pairs.put("INFE007", "memory02");
		pairs.put("INFE008", "memory03");
		pairs.put("INFE009", "memory03");
		pairs.put("INFE010", "memory04");
		pairs.put("INFE015", "memory04");

		barcodePositions
				.put("INFE001", new GeoPosition(51.025336, 13.72356, 0));
		barcodePositions.put("INFE005", new GeoPosition(51.0253, 13.723499, 0));
		barcodePositions.put("INFE006",
				new GeoPosition(51.025218, 13.723461, 0));
		barcodePositions.put("INFE007",
				new GeoPosition(51.025139, 13.723425, 0));
		barcodePositions.put("INFE008",
				new GeoPosition(51.025058, 13.723428, 0));
		barcodePositions.put("INFE009", new GeoPosition(51.0251, 13.723445, 0));
		barcodePositions.put("INFE010",
				new GeoPosition(51.025188, 13.723486, 0));
		barcodePositions.put("INFE015",
				new GeoPosition(51.025074, 13.723265, 0));
		// barcodePositions.put("INFE016", new GeoPosition(51.025095, 13.723173,
		// 0));
		// barcodePositions.put("INFE017", new GeoPosition(51.025092, 13.723173,
		// 0));
		/*
		 * barcodePositions.put("001", new GeoPosition(0, 0, 0)); 019 51.02509,
		 * 13.723171 barcodePositions.put("001", new GeoPosition(0, 0, 0)); 023
		 * 51.025102, 13.723117 barcodePositions.put("001", new GeoPosition(0,
		 * 0, 0));
		 * 
		 * 031 51.025099, 13.723135 barcodePositions.put("001", new
		 * GeoPosition(0, 0, 0)); 069 51.025122, 13.723017
		 * barcodePositions.put("001", new GeoPosition(0, 0, 0)); 067 51.025442,
		 * 13.722979 barcodePositions.put("001", new GeoPosition(0, 0, 0)); 065
		 * 51.025478, 13.722778 barcodePositions.put("001", new GeoPosition(0,
		 * 0, 0));
		 * 
		 * 054 51.025519, 13.722555 barcodePositions.put("001", new
		 * GeoPosition(0, 0, 0)); 053 51.025509, 13.722608
		 * barcodePositions.put("001", new GeoPosition(0, 0, 0)); 052 51.025551,
		 * 13.722284 barcodePositions.put("001", new GeoPosition(0, 0, 0)); 051
		 * 51.025628, 13.722316 barcodePositions.put("001", new GeoPosition(0,
		 * 0, 0)); 059 51.025703, 13.722348 barcodePositions.put("001", new
		 * GeoPosition(0, 0, 0));
		 * 
		 * 049 51.025668, 13.722375 barcodePositions.put("001", new
		 * GeoPosition(0, 0, 0)); 045 51.025791, 13.722586
		 * barcodePositions.put("001", new GeoPosition(0, 0, 0)); 047 51.025777,
		 * 13.722659 barcodePositions.put("001", new GeoPosition(0, 0, 0)); 048
		 * 51.02575, 13.722651 barcodePositions.put("001", new GeoPosition(0, 0,
		 * 0)); 044 51.025763, 13.722737 barcodePositions.put("001", new
		 * GeoPosition(0, 0, 0)); 043 51.025741, 13.722840
		 * barcodePositions.put("001", new GeoPosition(0, 0, 0)); 041 51.025734,
		 * 13.722906 barcodePositions.put("001", new GeoPosition(0, 0, 0)); 039
		 * 51.025722, 13.722963 barcodePositions.put("001", new GeoPosition(0,
		 * 0, 0));
		 * 
		 * 046 51.025757, 13.722652 042 51.025751, 13.722796 040 51.025722,
		 * 13.722963
		 */
	}

	@Override
	protected void registerPacketListener() {
		String username = null;
		String host = null;
		String password = null;
		for (String s : settings.keySet()) {
			if (s.equals("username"))
				username = settings.get(s);
			if (s.equals("password"))
				password = settings.get(s);
			if (s.equals("host"))
				host = settings.get(s);
		}
		if (password != null && username != null && host != null) {
			connection = new Connection(this, host, username, password);
		} else {
			connection = new Connection(this, "141.30.203.90", "server",
					"7Dj3S");
		}
		gameId = connection.getJid();
		GamePacketListener l = new GamePacketListener(this);
		connection.getConnection().addPacketListener(l,
				GamePacketFilter.getFilter());

	}

	private Team createTeam() {
		if (playersWithoutTeam.size() == 2) {
			// System.out.println("Teams werden erzeugt");
			int i = 0;
			Player player1 = null;
			Player player2 = null;
			for (Player playerT : playersWithoutTeam) {

				if (i == 1) {
					player2 = playerT;
					playersWithoutTeam.remove(playerT);
					playersWithoutTeam.remove(player1);
					break;
				}
				if (i == 0) {
					player1 = playerT;
					i++;
				}
			}

			System.out.println("Game.createTeam(Player1: " + player1.toString()
					+ " player2: " + player2.toString() + ")");
			Team team = new Team(player1, player2, "test", teams.size() + 1);
			player1.setTeam(team);
			player2.setTeam(team);
			teams.add(team);
			return team;
		}
		return null;
	}

	private boolean startGame() {
		isRunning = true;
		// remove quited players
		for (Player p : quitedPlayers) {
			p.setStopped(true);
		}
		quitedPlayers.clear();
		// remove players without team from the game
		for (Player p : playersWithoutTeam) {
			p.setStopped(true);
			p.endGame(EndType.ENDBYREGULAREND);
		}
		playersWithoutTeam.clear();

		System.out.println("Game.startGame()");
		// notify the players about the start of the game and send them
		// necessary informations
		for (Team t : teams) {
			for (Player p : t.getPlayers()) {
				p.startGame(barcodePositions, pairs);
			}
		}
		return true;
	}

	protected boolean startRound() {
		System.out.println("Game.startRound()");

		if (roundTask != null)
			roundTask.cancel();

		if (round.compareCards()) {
			discoveredPairs++;
			round.getPlayer1().getTeam().increaseScore();
		} else {
			Collections.sort(teams);
		}

		if (discoveredPairs < pairs.size() / 2) {
			System.out.println("Game.startRound() teams " + teams.toString());
			round.clear();
			int oldRoundNumber = round.getNumber();
			round.increaseNumber();
			round.setStartTime();

			int i = 0;
			for (Team team : teams) {
				if (i == 0) {
					team.setLastActiveRound(oldRoundNumber);
					round.setActiveTeam(team);

					for (Player player : team.getPlayers()) {
						player.setActive(true);
						player.startRound(round);
					}
				} else {
					for (Player player : team.getPlayers()) {
						player.setActive(false);
						player.startRound(round);
					}
				}
				i++;
			}
			roundTask = new RoundRestarterTask(this);
			timer.schedule(roundTask, new Long(round.getDuration()));
		} else {
			endGame();
		}
		System.out.println("teams neu sortiert: " + teams.toString());
		return true;
	}

	private boolean showCard() {
		for (Player p : getPlayers().keySet()) {
			if (round.getUncoveredCard2() != null) {
				p.showCard(round.getUncoveredCard2(), round.getPlayer2());
			} else {
				if (round.getUncoveredCard1() != null)
					p.showCard(round.getUncoveredCard1(), round.getPlayer1());
			}
		}
		return true;
	}

	private Map<Player, Team> getPlayers() {
		Map<Player, Team> players = new HashMap<Player, Team>();
		for (Team team : teams) {
			for (Player player : team.getPlayers()) {
				players.put(player, team);
			}
		}
		for (Player player : playersWithoutTeam) {
			players.put(player, null);
		}
		return players;
	}

	private void actualiseHighscore() {
		System.out.println("after update: "
				+ highscoreDAO.updateHighscore(teams).toString());
	}

	private void endGame() {
		if(roundTask != null) roundTask.cancel();
		actualiseHighscore();
		for (Player player : getPlayers().keySet()) {
			player.endGame(EndGameBean.EndType.ENDBYREGULAREND);
			player.setStopped(true);
		}
		fingerPrintWriter.close();
		try {
			shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
		teams.clear();
		round.clear();
		isRunning = true;
		joinedPlayers = startcount;
		playerReady = 0;
		timer.purge();
		discoveredPairs = 0;
		System.out.println("!!!!!!! GAME FINISHED !!!!!!!");
	}

	protected void cleanUp() {
		// General CleanUp
		Map<Player, Team> players = getPlayers();
		for (Player p : players.keySet()) {
			Calendar cal = (Calendar) Calendar.getInstance().clone();
			cal.setTime(p.getLastLifeSign());
			if ((LocPairsServerTime.getMilli() - cal.getTimeInMillis()) > CleanUpTask.quitOfflinePlayerDelay) {
				System.out.println("Der Speiler " + p.getJid()
						+ " wurde wegen time-out gelöscht!");
				p.quitGame();
			}
		}
		if (!isRunning) {
			// CleamUp process for the lobby
			// delete off line players that have no team
			for(Player p : playersWithoutTeam){
				Calendar cal = (Calendar) Calendar.getInstance().clone();
				cal.setTime(p.getLastLifeSign());
				if ((LocPairsServerTime.getMilli() - cal.getTimeInMillis()) > CleanUpTask.quitOfflinePlayerDelay) {
					System.out.println("Der Speiler " + p.getJid()
							+ " wurde wegen time-out gelöscht!");
					p.quitGame();
				}
			}
			// quit Lobby (the entire game) when unused
			if((LocPairsServerTime.getMilli() - lobbyUse) > CleanUpTask.quitLobbyDelay && playersWithoutTeam.size() == 0 && teams.size() == 0){
				endGame();
			}
			
		}
		CleanUpTask cleanUpTask = new CleanUpTask(this);
		timer.schedule(cleanUpTask, CleanUpTask.invocationFrequence);
		timer.purge();
	}

	/**
	 * Gets the game id.
	 * 
	 * @return the game id
	 */
	public String getGameId() {
		if (gameId == null)
			gameId = connection.getJid();
		return gameId;
	}

	/**
	 * Sets the game id.
	 * 
	 * @param gameId
	 *            the new game id
	 */
	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	/**
	 * Gets the barcodes.
	 * 
	 * @return the barcodes
	 */
	public Collection<String> getBarcodes() {
		return barcodes;
	}

	/**
	 * Sets the barcodes.
	 * 
	 * @param barcodes
	 *            the new barcodes
	 */
	public void setBarcodes(Collection<String> barcodes) {
		this.barcodes = barcodes;
	}

	/**
	 * Gets the image urls.
	 * 
	 * @return the image urls
	 */
	public Collection<String> getImageURLs() {
		return imageURLs;
	}

	/**
	 * Sets the image urls.
	 * 
	 * @param imageURLs
	 *            the new image ur ls
	 */
	public void setImageURLs(Collection<String> imageURLs) {
		this.imageURLs = imageURLs;
	}

	/**
	 * Gets the pairs.
	 * 
	 * @return the pairs
	 */
	public Map<String, String> getPairs() {
		return pairs;
	}

	/**
	 * Sets the pairs.
	 * 
	 * @param pairs
	 *            the pairs
	 */
	public void setPairs(Map<String, String> pairs) {
		this.pairs = pairs;
	}

	/**
	 * Gets the connection.
	 * 
	 * @return the connection
	 */
	public Connection getConnection() {
		return connection;
	}
	
	/**
	 * Gets the scores.
	 * 
	 * @return the scores
	 */
	public Map<Integer, Long> getScores() {
		Map<Integer, Long> scores = new HashMap<Integer, Long>();
		for (Team team : teams) {
			scores.put(new Integer(team.getNumber()), team.getScore());
		}
		return scores;
	}

	/**
	 * Gets the highscores. 50 in maximum
	 * 
	 * @return the highscores
	 */
	public Map<String, Long> getHighscores() {
		return highscoreDAO.getHighscore();
	}

	/**
	 * Checks for player.
	 * 
	 * @param player
	 *            the player
	 * @return true, if successful
	 */
	public boolean hasPlayer(Player player) {
		for (Player p : getPlayers().keySet()) {
			if (p.equals(player))
				return true;
		}
		return false;
	}

}
