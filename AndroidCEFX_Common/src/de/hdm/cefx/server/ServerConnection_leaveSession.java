package de.hdm.cefx.server;

import java.io.Serializable;

import de.hdm.cefx.client.CEFXClient;

public class ServerConnection_leaveSession implements Serializable {
	public String sessionName;
	public CEFXClient client;
}
