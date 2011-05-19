package de.tudresden.inf.rn.mobilis.server.services.xhunt.model;


public class Ticket {

	private int mId;
	private String mName;
	private String mIcon;
	private boolean mIsSuperior;
	
	public Ticket() {}
	
	public Ticket(int id, String name) {
		this.mId = id;
		this.mName = name;
	}

	public Ticket(int id, String name, String icon, boolean isSuperior) {
		this.mId = id;
		this.mName = name;
		this.mIcon = icon;
		this.mIsSuperior = isSuperior;
	}
	
	public int getId() {
		return mId;
	}
	
	public boolean isSuperior() {
		return mIsSuperior;
	}	
	
	public String getIcon(){
		return mIcon;
	}
	
	public String getName() {
		return mName;
	}
	
	public void setIcon(String icon){
		mIcon = icon;
	}

	public void setId(int mId) {
		this.mId = mId;
	}

	public void setName(String mName) {
		this.mName = mName;
	}
	
	public void setSuperior(boolean mIsSuperior) {
		this.mIsSuperior = mIsSuperior;
	}

	@Override
	public String toString() {
		return "Ticket [mId=" + mId + ", mName=" + mName + ", mIsSuperior=" + mIsSuperior + "]";
	}

	
}
