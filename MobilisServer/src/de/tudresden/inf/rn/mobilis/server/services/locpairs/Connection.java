package de.tudresden.inf.rn.mobilis.server.services.locpairs;

import org.jivesoftware.smack.XMPPConnection;


/**
 * The Class Connection.
 * 
 * @author Reik Mueller
 */
public class Connection {
	
	private XMPPConnection connection = null;
	private String jid = null;
	
	public Connection(LocPairs game, String host, String username, String password){
		jid = game.getAgent().getFullJid();
		connection = game.getAgent().getConnection();
//		System.out.println(jid);
	}

	/**
	 * Gets the jid.
	 *
	 * @return the jid
	 */
	public String getJid() {
		return jid;
	}

	/**
	 * Gets the connection.
	 *
	 * @return the connection
	 */
	public XMPPConnection getConnection() {
		return connection;
	}
}
