package de.tudresden.inf.rn.mobilis.server.services.grouping;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.jivesoftware.smackx.muc.MultiUserChat;


public class MobilisGroup implements Serializable {

	private static final long serialVersionUID = 1L;

	private String groupId;
	private String name;	
	private String description;
	private String address;
	private int longitude_e6;
	private int latitude_e6;
	private int visibilityRadius;
	private int visibilityLongitude_e6;
	private int visibilityLatitude_e6;
	private int joinRadius;
	private int joinLongitude_e6;
	private int joinLatitude_e6;
	private Date joinStartTime, joinEndTime, startTime, endTime;
	private String  privacy;
	private String link;
	private MobilisMember founder;
	
	private Set members;
	private String invitees;
	
	private MultiUserChat muc;
	
	
	// Constructor with all attributes
	public MobilisGroup(String name, String description, String address,
			int longitude_e6, int latitude_e6, int visbilityRadius,
			int visibilityLongitude_e6, int visibilityLatitude_e6,
			int joinRadius, int joinLongitude_e6, int joinLatitude_e6,
			Date joinStartTime, Date joinEndTime, Date startTime, Date endTime,
			String privacy, String link, MobilisMember founder) {
		super();
		this.name=name;
		this.description=description;
		this.address=address;
		this.longitude_e6=longitude_e6;
		this.latitude_e6=latitude_e6;
		this.visibilityRadius=visbilityRadius;
		this.visibilityLongitude_e6=visibilityLongitude_e6;
		this.visibilityLatitude_e6=visibilityLatitude_e6;
		this.joinRadius=joinRadius;
		this.joinLongitude_e6=joinLongitude_e6;
		this.joinLatitude_e6=joinLatitude_e6;
		this.joinStartTime=joinStartTime;
		this.joinEndTime=joinEndTime;
		this.startTime=startTime;
		this.endTime=endTime;
		this.privacy=privacy;
		this.link=link;
		this.founder=founder;
		this.members = new HashSet();
		this.invitees = ",";
	}

	// Constructor with no attributes
	public MobilisGroup() {
		super();
		this.members = new HashSet();
		this.invitees = ",";		
	}
	
	public String toString() {
		return "groupId: "+groupId+" Name: "+name+" Members: "+members.size();		
	}
	
	// GETTER & SETTER:
	
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId=groupId;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name=name;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description=description;
	}
		
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address=address;
	}
	
	public int getLongitude_e6() {
		return longitude_e6;
	}
	public void setLongitude_e6(int longitude_e6) {
		this.longitude_e6=longitude_e6;
	}
	
	public int getLatitude_e6() {
		return latitude_e6;
	}
	public void setLatitude_e6(int latitude_e6) {
		this.latitude_e6=latitude_e6;
	}	
	
	public int getVisibilityRadius() {
		return visibilityRadius;
	}
	public void setVisibilityRadius(int visibilityRadius) {
		this.visibilityRadius=visibilityRadius;
	}
	
	public int getVisibilityLongitude_e6() {
		return visibilityLongitude_e6;
	}
	public void setVisibilityLongitude_e6(int visibilityLongitude_e6) {
		this.visibilityLongitude_e6=visibilityLongitude_e6;
	}
	
	public int getVisibilityLatitude_e6() {
		return visibilityLatitude_e6;
	}
	public void setVisibilityLatitude_e6(int visibilityLatitude_e6) {
		this.visibilityLatitude_e6=visibilityLatitude_e6;
	}	
	
	public int getJoinRadius() {
		return joinRadius;
	}
	public void setJoinRadius(int joinRadius) {
		this.joinRadius=joinRadius;
	}
	
	public int getJoinLongitude_e6() {
		return joinLongitude_e6;
	}
	public void setJoinLongitude_e6(int joinLongitude_e6) {
		this.joinLongitude_e6=joinLongitude_e6;
	}
	
	public int getJoinLatitude_e6() {
		return joinLatitude_e6;
	}
	public void setJoinLatitude_e6(int joinLatitude_e6) {
		this.joinLatitude_e6=joinLatitude_e6;
	}	
	
	public Date getJoinStartTime() {
		return joinStartTime;
	}
	public void setJoinStartTime(Date joinStartTime) {
		this.joinStartTime=joinStartTime;
	}
	
	public Date getJoinEndTime() {
		return joinEndTime;
	}
	public void setJoinEndTime(Date joinEndTime) {
		this.joinEndTime=joinEndTime;
	}
	
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime=startTime;
	}
	
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime=endTime;
	}
	
	public String getPrivacy() {
		return privacy;
	}
	public void setPrivacy(String privacy) {
		this.privacy=privacy;
	}
	
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link=link;
	}
	
	public MobilisMember getFounder() {
		return founder;
	}
	public void setFounder(MobilisMember founder) {
		this.founder=founder;
	}
	
	public Set getMembers() {
		members.size();
		return members;
	}
	public void setMembers(Set members) {
		this.members=members;
	}
	
	public String getInvitees() {		
		return invitees;
	}
	public void setInvitees(String invitees) {
		this.invitees=invitees;
	}
	
	public boolean isInvited(String jid) {
		if (jid.equals("")) return false;
		StringTokenizer st = new StringTokenizer(invitees, ",");
		while (st.hasMoreTokens())
		  if (st.nextToken().equals(jid))
			  return true;		 
		return false;
	}
	
	public void addInvitee(String jid) {
		System.out.println("addInvitee. before: "+invitees);
		if (!isInvited(jid)) {
			invitees += jid + ",";
		}
		System.out.println("addInvitee. after: "+invitees);
	}
	
	public void removeInvitee(String jid) {
		System.out.println("removeInvitee. before - jid: "+jid);
		System.out.println("removeInvitee. before: "+invitees);
		if (isInvited(jid)) {
			invitees = invitees.replaceFirst(","+jid+",", ",");
		}
		System.out.println("removeInvitee. after: "+invitees);
	}
	
	public void setMUC(MultiUserChat muc) {
		this.muc=muc;
	}	
	public MultiUserChat getMUC() {
		return muc;
	}
	
}
