package de.tudresden.inf.rn.mobilis.xmpp.beans;

public class Credential {

    private String networkName;
    private String userId;
    private String password;
    private boolean autoConnect;
    
    public Credential(String networkName, String userId, String password, boolean autoConnect) {
        this.networkName = networkName;
        this.userId = userId;
        this.password = password;
        this.autoConnect = autoConnect;
    }

    public String getNetworkName() {
        return networkName;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAutoConnect() {
        return autoConnect;
    }

    public void setAutoConnect(boolean autoConnect) {
        this.autoConnect = autoConnect;
    }
    
    @Override
    public String toString() {
        String displayedNetworkName = networkName;
        if (networkName.toLowerCase().equals("mobilis")) displayedNetworkName = "Mobilis";
        if (networkName.toLowerCase().equals("facebook")) displayedNetworkName = "Facebook";
        return displayedNetworkName + " - " + userId;
    }
}
