package de.tudresden.inf.rn.mobilis.server.services.context;

import org.jivesoftware.smackx.packet.PEPItem;

public class ContextItem {

	private String key, value;
	private int type;
	private long lastUpdate, expirationDate;
	
	public ContextItem(String key, String value, int type) {
		this.key = key;
		this.value = value;
		this.type = type;
	}
	
	public ContextItem(String key, String value, int type, long expirationDate) {
		this.key = key;
		this.value = value;
		this.type = type;
		this.expirationDate=expirationDate;
	}
	
	//Getter & Setter
	
	public String getKey() {
		return key;
	}
	public String getValue() {
		return value;
	}
	public int getType() {
		return type;
	}
	public long getLastUpdate() {
		return lastUpdate;
	}
	public long getExpirationDate() {
		return expirationDate;
	}
	
	public void setKey(String key) {
		lastUpdate = System.currentTimeMillis() / 1000;
		this.key = key;
	}
	public void setValue(String value) {
		lastUpdate = System.currentTimeMillis() / 1000;
		this.value = value;
	}
	public void setType(int type) {
		lastUpdate = System.currentTimeMillis() / 1000;
		this.type = type;
	}
	public void setExpirationDate(long expirationDate) {
		lastUpdate = System.currentTimeMillis() / 1000;
		this.expirationDate = expirationDate;
	}
	
	public String toString() {
		String result = key+"="+value+" (type:"+type+") ";
		if (expirationDate>0) result+="(expirationDate="+expirationDate+") ";
		if (lastUpdate>0) result+="(lastUpdate="+lastUpdate+") ";		
		return result;
	}
	
	
	
	
	
}
