package de.tudresden.inf.rn.mobilis.server.services.locpairs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.FromContainsFilter;

import de.tudresden.inf.rn.mobilis.server.services.locpairs.LocPairs;
import de.tudresden.inf.rn.mobilis.server.services.locpairs.LocPairsServerTime;
import de.tudresden.inf.rn.mobilis.server.services.locpairs.Player;
import de.tudresden.inf.rn.mobilis.server.services.locpairs.PlayerPacketFilter;
import de.tudresden.inf.rn.mobilis.server.services.locpairs.PlayerPacketListener;
import de.tudresden.inf.rn.mobilis.server.services.locpairs.Round;
import de.tudresden.inf.rn.mobilis.server.services.locpairs.Team;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.EndGameBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.GoThereBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.JoinGameBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.KeepAliveBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.PlayerUpdateBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.QuitBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.ShowCardBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.StartGameBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.StartRoundBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.UncoverCardBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.GeoPosition;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.NetworkFingerPrint;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanIQAdapter;

/**
 * The Class Player represents a mobile client in the real world.
 * 
 * @author Reik Mueller
 */
public class Player {
	
	private String jid = null;
	private String name;
	private XMPPConnection connection;
	private LocPairs game;
	private String fromJID; 
	private Team team;
	private int consecutiveErrors = 0;
	private Date lastLifeSign = LocPairsServerTime.getTime();
	private boolean active = false;
	private boolean status = false;
	private Round actualRound = null;
	private GeoPosition actualPosition = new GeoPosition(0, 0, 0);
	private boolean ackReadyToPlay = false;
	private boolean stopped = false;
	private Collection<XMPPBean> unacknowledgedBeans = new ArrayList<XMPPBean>();
	
	
	/**
	 * Instantiates a new player.
	 *
	 * @param jid the jid
	 * @param name the name
	 * @param game the game
	 */
	public Player (String jid, String name, LocPairs game){
		this.jid = jid;
		this.name = name;
		this.game = game;
		fromJID = game.getGameId();
		connection = game.getConnection().getConnection();
		this.connection.addPacketListener(new PlayerPacketListener(this), new AndFilter( new FromContainsFilter(this.jid), PlayerPacketFilter.getFilter()));
	}
	
	public Player(String name){
		this.name = name;
	}
	
	/**
	 * Join game. Sends the result IQ to the mobile client that the client has joined successfully ; 
	 *
	 * @return true, if successful
	 */
	public boolean joinGame(){
		JoinGameBean resultBean = new JoinGameBean();
		resultBean.setTo(jid);
	    resultBean.setFrom(fromJID);
	    resultBean.setType(XMPPBean.TYPE_RESULT);
	    connection.sendPacket(new BeanIQAdapter(resultBean));
//	    System.out.println("JoinGameBean vom typ RESULT an " + jid + " gesendet");
	    return true;
	}
	
	/**
	 * Start game. Sends the startGameIQ to the client.
	 *
	 * @param barcodes the barcodes
	 * @param pictures the pictures
	 * @return true, if successful
	 */
	public boolean startGame(Map<String, GeoPosition> barcodes, Map<String, String> pictures) {
		System.out.println("Player.startGame() : " + name);
		StartGameBean bean = new StartGameBean(fromJID, barcodes, pictures);
		bean.setFrom(fromJID);
		bean.setTo(jid);
		bean.setType(XMPPBean.TYPE_SET);
		
		unacknowledgedBeans.add(bean);
		new BeanResenderTask(bean, this);
//		connection.sendPacket(new BeanIQAdapter(bean));
		return true;
	}
	
	/**
	 * Start game. Notifies the game that the player is ready to start.
	 *
	 * @return true, if successful
	 */
	public boolean startGame() {
		if(!ackReadyToPlay){
			ackReadyToPlay = true;
			System.out
			.println("PPL: StartGameBean vom typ RESULT empfangen von: " + jid);
			return game.playerReadyToPlay(this);
		}
		return false;
	}
	
	/**
	 * Start round.
	 *
	 * @param round the round
	 * @return true, if successful
	 */
	public boolean startRound(Round round) {
//		System.out.println("Player.startRound() Player: " + jid + "active: " + active);
		actualRound = round;
		StartRoundBean bean = new StartRoundBean(active, StartRoundBean.sdf.format(round.getStartTime()), round.getDuration() - 2000, game.getScores(), String.valueOf(round.getActiveTeam().getNumber()), round.getNumber());
		bean.setFrom(fromJID);
		bean.setTo(jid);
		bean.setType(XMPPBean.TYPE_SET);
//		connection.sendPacket(new BeanIQAdapter(bean));
		
		unacknowledgedBeans.add(bean);
		new BeanResenderTask(bean, this);
		return true;
	}
	
	/**
	 * Show card.
	 *
	 * @param barCodeId the bar code id
	 * @param pictureURL the picture url
	 * @param player the player
	 * @return true, if successful
	 */
	public boolean showCard(String barCodeId, Player player) {
		
		ShowCardBean bean = new ShowCardBean(barCodeId, player.getJid());
		bean.setFrom(fromJID);
		bean.setTo(jid);
		bean.setType(XMPPBean.TYPE_SET);
//		System.out.println("Player.showCard(): " + bean.getId());
//		connection.sendPacket(new BeanIQAdapter(bean));
		unacknowledgedBeans.add(bean);
		new BeanResenderTask(bean, this);
		return true;
	}
	
	/**
	 * Uncover card. Is invoked by the PlayerPacketListener if an uncoverCardIQ arrived.
	 *
	 * @param barCode the bar code
	 * @param fingerprint the detected network finger print
	 * @param timeStamp the time stamp
	 * @return true, if successful
	 */
	public boolean uncoverCard(String barCode, NetworkFingerPrint fingerprint, Date timeStamp){
		game.writeNetworkFingerprint(fingerprint, barCode);
		if(!active)return false;
		if(!game.uncoverCard(barCode, this))return false;
		return true;
	}
	
	/**
	 * Go there. Is invoked by the PlayerPacketListener if a goThereIQ arrived.
	 * It shows the the player where his team mate wants him/her to go.
	 *
	 * @param bean the bean
	 * @return true, if successful
	 */
	public boolean goThere(XMPPBean bean) {
		System.out.println("gothere von: " + bean.getFrom() + " nach: " + jid);
		bean.setFrom(fromJID);
		bean.setTo(jid);
		bean.setType(XMPPBean.TYPE_SET);
//		connection.sendPacket(new BeanIQAdapter(bean));
		unacknowledgedBeans.add(bean);
		new BeanResenderTask(bean, this);
		return true;
	}

	/**
	 * Keep alive. Is invoked by the PlayerPacketListener when a keepAliveIQ 
	 * arrives and signalizes that the client is online.
	 *
	 * @return true, if successful
	 */
	public boolean keepAlive() {
		Date date = LocPairsServerTime.getTime();
		this.lastLifeSign = date;
		return true;
	}

	/**
	 * Sends player update that contains the actualized position of each player.
	 *
	 * @param gameID the game id
	 * @param players the players
	 * @return true, if successful
	 */
	public boolean sendPlayerUpdate(String gameID, Map<Player, Team> players) {
//		System.out.println("Player.sendPlayerUpdate()");
		Map<String, Integer> playerAndTeam = new HashMap<String, Integer>();
		Map<String, String> playerjidAndPlayername = new HashMap<String, String>();
		Map<String, Boolean> playerjidAndPlayerstate = new HashMap<String, Boolean>();
		Map<String, GeoPosition> playerAndPosition = new HashMap<String, GeoPosition>();
		
		for(Player player : players.keySet()){
			String playerJid = player.getJid();
			if(players.get(player) != null){
				Team team = players.get(player);
				playerAndTeam.put(playerJid,team.getNumber());
			}else{
				playerAndTeam.put(playerJid, -1);
			}
			playerjidAndPlayername.put(playerJid, player.getName());
			playerjidAndPlayerstate.put(playerJid, player.getState());
			playerAndPosition.put(playerJid, player.getActualPosition());
		}
		
		PlayerUpdateBean bean = new PlayerUpdateBean(gameID, playerAndTeam, playerjidAndPlayername, playerjidAndPlayerstate, playerAndPosition);
		bean.setFrom(fromJID);
		bean.setTo(jid);
		bean.setType(XMPPBean.TYPE_SET);
		
		connection.sendPacket(new BeanIQAdapter(bean));

		unacknowledgedBeans.add(bean);
		new BeanResenderTask(bean, this);
		return true;
	}
	
	/**
	 * Quit game. Is invoked by the PlayerPacketListener when the player
	 * quits the game for reasons of his own.
	 *
	 * @return true, if successful
	 */
	public boolean quitGame() {
		return game.quitPlayer(this, "sieler gibt auf");		
	}
	
	/**
	 * Send acknowledgment is used to send acknowledgments of every sort of
	 * IQ. (the received bean is reused)
	 *
	 * @param bean the bean
	 * @return true, if successful
	 */
	public boolean sendAcknowledgement(XMPPBean bean) {
//		System.out.println("Player.sendAcknowledgment() " + bean.getNamespace());
		bean.setFrom(fromJID);
		bean.setTo(jid);
		bean.setType(XMPPBean.TYPE_RESULT);
		connection.sendPacket(new BeanIQAdapter(bean));
		return true;
	}
	
	/**
	 * Send error is used to send errors of every sort of
	 * IQ. (the received bean is reused)
	 *
	 * @param bean the bean
	 * @param error the error
	 * @return true, if successful
	 */
	public boolean sendError(XMPPBean bean, String error){
		System.out.println("Player.sendError() " + bean.getNamespace());
		bean.setFrom(fromJID);
		bean.setTo(jid);
		bean.setType(XMPPBean.TYPE_ERROR);
		bean.errorCondition = "a";
		bean.errorText = "error";
		bean.errorType = "a";
		
		connection.sendPacket(new BeanIQAdapter(bean));
		return true;
	}
	
	/**
	 * End game is invoked by the Game and signals the mobile client the end
	 * of the game. A reason is sent, too.
	 *
	 * @param type the type
	 * @return true, if successful
	 */
	public boolean endGame(EndGameBean.EndType type) {
		System.out.println("Player.endGame() Highscores: " + game.getHighscores().toString());
		EndGameBean bean = new EndGameBean(game.getScores(), game.getHighscores(), type);
		bean.setFrom(fromJID);
		bean.setTo(jid);
		bean.setType(XMPPBean.TYPE_SET);
//		connection.sendPacket(new BeanIQAdapter(bean));
		unacknowledgedBeans.add(bean);
		new BeanResenderTask(bean, this);
		return true;
	}
	
	/**
	 * Checks if a IQ has been acknowledged. 
	 *
	 * @param bean the bean
	 * @return true, if is acknowledged
	 */
	public boolean isAcknowledged(XMPPBean bean){
		for(XMPPBean b : unacknowledgedBeans){
			if(b.getId().equals(bean.getId()))return false;
		}
		return true;
	}
	
	/**
	 * AcknowledgeBean is used to signal the system that an IQ has been
	 * acknowledged.
	 *
	 * @param bean the bean
	 * @return true, if successful
	 */
	public boolean acknowledgeBean(XMPPBean bean){
//		System.out.println(unacknowledgedBeans.toString());
		XMPPBean hit = null;
		for(XMPPBean b : unacknowledgedBeans){
			if(b.getId().equals(bean.getId())){
				hit = b;
			}
		}
		if(hit != null)unacknowledgedBeans.remove(hit);
		return true;
	}
	
	/**
	 * BeanSendingError decides what to do, if an IQ has not been 
	 * acknowledged to often.
	 *
	 * @param bean the bean
	 * @return true, if successful
	 */
	public boolean beanSendingError(XMPPBean bean){
		// Hier wird entschieden ob die bean weiter gesendet werden soll (neuen Beanresender)
		// oder ob das Senden eingestellt wird.
		// Die Fehlerbearbeitung wird hier angestoﬂen
		if(bean instanceof StartGameBean){
			
		}else if(bean instanceof QuitBean){
			// nischt, es handelt sich eh nur um die best‰tigungsbean
			acknowledgeBean(bean);
		}else if(bean instanceof GoThereBean){
			// TODO Error bean an den Clienten, der die bean gesendet hat, dass der mitspieler nicht erreichbar ist
			getTeamMate().sendError(bean, "bean konnte nicht zugestellt werden");
			acknowledgeBean(bean);
		
		}else if(bean instanceof StartRoundBean){
			if(active){
				game.quitPlayer(this, "clientreagiert nicht");
				acknowledgeBean(bean);
			}else{
				// TODO Soll die Bean weiter gesendet werden?
			} 
		}else if(bean instanceof UncoverCardBean){
			// nischt es handelt sich eh nur um die best‰tigungsbean
			acknowledgeBean(bean);
		}else if(bean instanceof KeepAliveBean){
			// nischt es handelt sich eh nur um die best‰tigungsbean
			acknowledgeBean(bean);
		}else if(bean instanceof ShowCardBean){
			// TODO Wenn die neue Runde begonnen hat, pech, sonst bean weiter senden 
		}
		return false;
	}
	
	
	/**
	 * Equals. Checks whether a given player is the same this one. The creteria 
	 * is the jid.
	 *
	 * @param player the player
	 * @return true, if successful
	 */
	public boolean equals(Player player){
		return this.jid.equals(player.getJid());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return "Player - name: " + this.name + " jid: " + this.jid;
	}
	
	// Getters & Setters
	/**
	 * Gets the jid.
	 *
	 * @return the jid
	 */
	public String getJid() {
		return jid;
	}
	
	/**
	 * Gets the team mate.
	 *
	 * @return the team mate
	 */
	public Player getTeamMate(){
		if(team == null) return null;
		Collection<Player> players = team.getPlayers();
		for(Player p : players){
			if(!p.equals(this))return p; 
		}
		return null;
	}
	
	/**
	 * Sets the jid.
	 *
	 * @param jid the new jid
	 */
	public void setJid(String jid) {
		this.jid = jid;
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the connection.
	 *
	 * @return the connection
	 */
	public XMPPConnection getConnection() {
		return connection;
	}
	
	/**
	 * Sets the connection.
	 *
	 * @param connection the new connection
	 */
	public void setConnection(XMPPConnection connection) {
		this.connection = connection;
	}
	
	/**
	 * Gets the game.
	 *
	 * @return the game
	 */
	public LocPairs getGame() {
		return game;
	}
	
	/**
	 * Sets the game.
	 *
	 * @param game the new game
	 */
	public void setGame(LocPairs game) {
		this.game = game;
	}
	
	/**
	 * Gets the team.
	 *
	 * @return the team
	 */
	public Team getTeam() {
		return team;
	}
	
	/**
	 * Sets the team.
	 *
	 * @param team the new team
	 */
	public void setTeam(Team team) {
		this.team = team;
	}
	
	/**
	 * Sets the state.
	 *
	 * @param state the new state
	 */
	public void setState(boolean state) {
		this.status = state;
	}
	
	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	public boolean getState() {
		return status;
	}
	
	/**
	 * Gets the consecutive errors.
	 *
	 * @return the consecutive errors
	 */
	public int getConsecutiveErrors() {
		return consecutiveErrors;
	}
	
	/**
	 * Sets the consecutive errors.
	 *
	 * @param consecutiveErrors the new consecutive errors
	 */
	public void setConsecutiveErrors(int consecutiveErrors) {
		this.consecutiveErrors = consecutiveErrors;
	}
	
	/**
	 * Gets the last life sign.
	 *
	 * @return the last life sign
	 */
	public Date getLastLifeSign() {
		return lastLifeSign;
	}
	
	/**
	 * Sets the last life sign.
	 *
	 * @param lastLifeSign the new last life sign
	 */
	public void setLastLifeSign(Date lastLifeSign) {
		this.lastLifeSign = lastLifeSign;
	}
	
	/**
	 * Checks if is active.
	 *
	 * @return true, if is active
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * Sets the active.
	 *
	 * @param active the new active
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Gets the from jid.
	 *
	 * @return the from jid
	 */
	public String getFromJID() {
		return fromJID;
	}

	/**
	 * Sets the from jid.
	 *
	 * @param fromJID the new from jid
	 */
	public void setFromJID(String fromJID) {
		this.fromJID = fromJID;
	}

	/**
	 * Checks if is status.
	 *
	 * @return true, if is status
	 */
	public boolean isStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 *
	 * @param status the new status
	 */
	public void setStatus(boolean status) {
		this.status = status;
	}

	/**
	 * Gets the actual round.
	 *
	 * @return the actual round
	 */
	public Round getActualRound() {
		return actualRound;
	}

	/**
	 * Sets the actual round.
	 *
	 * @param actualRound the new actual round
	 */
	public void setActualRound(Round actualRound) {
		this.actualRound = actualRound;
	}

	/**
	 * Gets the actual position.
	 *
	 * @return the actual position
	 */
	public GeoPosition getActualPosition() {
		return actualPosition;
	}

	/**
	 * Sets the actual position.
	 *
	 * @param actualPosition the new actual position
	 */
	public void setActualPosition(GeoPosition actualPosition) {
//		System.out.println(name + ".setActualPosition() :" + actualPosition.toString());
		if(actualPosition != null)this.actualPosition = actualPosition;
	}

	public boolean isStopped() {
		return stopped;
	}

	public void setStopped(boolean stopped) {
		this.stopped = stopped;
	}
	
	
}
