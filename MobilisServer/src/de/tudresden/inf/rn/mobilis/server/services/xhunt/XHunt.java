/*******************************************************************************
 * Copyright (C) 2010 Technische Universit�t Dresden
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: http://mobilisplatform.sourceforge.net
 ******************************************************************************/
package de.tudresden.inf.rn.mobilis.server.services.xhunt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;

import de.tudresden.inf.rn.mobilis.server.agents.MobilisAgent;
import de.tudresden.inf.rn.mobilis.server.services.AppSpecificService;
import de.tudresden.inf.rn.mobilis.server.services.CoordinatorService;
import de.tudresden.inf.rn.mobilis.server.services.xhunt.helper.SqlHelper;
import de.tudresden.inf.rn.mobilis.server.services.xhunt.services.IQListener;
import de.tudresden.inf.rn.mobilis.server.services.xhunt.services.MessageService;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.GameOverBean;


public class XHunt extends AppSpecificService {
	
	private Connection mConnection;
	private Game mGame;
	private Settings mSettings;
	private DateFormat mDateFormatter;
	private SqlHelper mSqlHelper;
	/* The list of participants, who do not play, but spectate at the game. */
	private Set<String> spectators = new HashSet<String>();
	
	/**
	 * Initializes the life cycle
	 */
	public XHunt(CoordinatorService coordinator, String password, String serviceName) {
		super(coordinator, password, serviceName);
		
		mDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
		log("XHunt()");		
	}
	
	public void log(String str){		 
		System.out.println("[" + mDateFormatter.format(System.currentTimeMillis()) + "] " + str);
	}
	
	private void logToFile(String str){
		FileWriter fw;
		File file = new File("xhunt.log");
		
		try {
			fw = new FileWriter(file, true);
			
			BufferedWriter bw = new BufferedWriter(fw); 
			
			bw.write(str); 
			bw.newLine();
			
			bw.close(); 
		} catch (IOException e) {
			System.err.println("ERROR while writing to logfile: " + file.getAbsolutePath());
			e.printStackTrace();
		} 
	}
	
	public void startup(MobilisAgent agent) throws Exception {
		super.startup(agent);
		log("XHunt.startUp()");
		mConnection = new Connection(this);
		log("XMPP Connection: " + mConnection.toString());
		
		mSettings = new Settings(getAgent());
		
		mSqlHelper = new SqlHelper(this);
		//TODO: read data from hibernate.xml
		mSqlHelper.setSqlConnectionData("127.0.0.1", "3306", "mobilis_server", "mobilis", "mobilis");
		
		startGame();
	}
	

	/**
	 * Starts/Restarts the game
	 */
	public void startGame(){
		log("XHunt.startGame()");
		if(mConnection.isConnected()){
			try {
				mGame = new Game(this);
			} catch (XMPPException e) {
				if(e.getXMPPError() != null){
					int errorcode = e.getXMPPError().getCode();
					String errormessage = e.getXMPPError().getMessage();
					log(errorcode + " - " + errormessage);
				}else{
					log("Unknown Error while connecting to the XMPP-Server");
				}
			} catch (Exception e) {
				log("Another stupid error");
			}			
		}else{
			try {
				this.shutdown();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void shutdown() throws Exception {
		if (mGame != null){
			try {
				GameOverBean bean = new GameOverBean("Server was shut down");
				mConnection.sendXMPPBean(bean, mGame.getPlayers().keySet(), XMPPBean.TYPE_SET);
				
				mSqlHelper.disconnect();
				mGame.closeMultiUserChat();
			} catch (XMPPException e) {
				if(e.getXMPPError() != null){
					int errorcode = e.getXMPPError().getCode();
					String errormessage = e.getXMPPError().getMessage();
					log(errorcode + " - " + errormessage);
				}else{
					log("Unknown Error while shut down XHunt Service: " + getAgent().getFullJid());
				}
			}
		}
		
		log(getAgent().getFullJid() + " is shutting down.");
		super.shutdown();
	}

	public Connection getConnection() {
		return mConnection;
	}

	public void setActGame(Game actGame) {
		this.mGame = actGame;
	}

	public Game getActGame() {
		return mGame;
	}

	public Settings getSettings() {
		return mSettings;
	}
	
	public SqlHelper getSqlHelper(){
		return this.mSqlHelper;
	}


	public void setSettings(Settings mSettings) {
		this.mSettings = mSettings;
	}
	
	/**
	 * Get all participants, who do not play, but spectate at the game.
	 * @return the set of spectator JIDs
	 */	
	public Set<String> getSpectators() {
		return spectators;
	}
	
	/**
	 * Add a new entry to the list of participants, who do not play,
	 * but spectate at the game.
	 * @param spectatorJID the full XMPP-ID (JID; with resource) of the spectator
	 * @return true if the set of spectators did not already contain the specified element
	 */			
	public boolean addSpectator(String spectatorJID) {
		return spectators.add(spectatorJID);
	}
	
	/**
	 * Remove an entry from the list of participants, who do not play,
	 * but spectate at the game.
	 * @param spectatorJID the full XMPP-ID (JID; with resource) of the spectator
	 * @return true if this set of spectators contained the specified element
	 */
	public boolean removeSpectator(String spectatorJID) {
		return spectators.remove(spectatorJID);
	}
	
	@Override
	protected void registerPacketListener() {
		log("START XHunt.registerPacketListener()");
		MessageService mesServ = new MessageService(this);
		log("MessageService created XHunt.registerPacketListener()");
		PacketTypeFilter mesFil = new PacketTypeFilter(Message.class);		
		getAgent().getConnection().addPacketListener(mesServ, mesFil);
		log("3 XHunt.registerPacketListener()");
		
		IQListener iqServ = new IQListener(this);
		PacketTypeFilter locFil = new PacketTypeFilter(IQ.class);		
		getAgent().getConnection().addPacketListener(iqServ, locFil);	
		log("END XHunt.registerPacketListener()");
	}
}
