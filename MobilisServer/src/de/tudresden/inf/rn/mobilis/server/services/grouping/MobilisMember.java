package de.tudresden.inf.rn.mobilis.server.services.grouping;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


public class MobilisMember implements Serializable {

	private static final long serialVersionUID = 1L;

	private String jidWithoutResource;
	private String realName;
	private int age;
	private String email;
	private String homepage;
	private String city;
	
	private Set groups;
	
	// Constructor with all attributes
	public MobilisMember(String jidWithoutResource, String realName, int age, String email, String homepage, String city) {
		super();
		this.jidWithoutResource=jidWithoutResource;
		this.realName=realName;		
		this.age=age;		
		this.email=email;
		this.homepage=homepage;	
		this.city=city;
		this.groups = new HashSet();
	}

	// Constructor with no attributes
	public MobilisMember() {
		super();
		
	}
	
	public String toString() {
		return "jid:"+jidWithoutResource+" Name:"+realName+" age:"+age+" email:"+email+" homepage:"+homepage;		
	}
	
	// GETTER & SETTER:
	
	public String getJidWithoutResource() {
		return jidWithoutResource;
	}
	public void setJidWithoutResource(String jidWithoutResource) {
		this.jidWithoutResource=jidWithoutResource;
	}
	
	public String getRealName() {
		return realName;
	}
	public void setRealName(String realName) {
		this.realName=realName;
	}
	
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age=age;
	}
		
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email=email;
	}
	
	public String getHomepage() {
		return homepage;
	}
	public void setHomepage(String homepage) {
		this.homepage=homepage;
	}
	
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city=city;
	}
	public Set getGroups() {
		groups.size();
		return groups;
	}
	public void setGroups(Set groups) {
		this.groups=groups;
	}
	
	public boolean equals(Object o) {
		if (o==null) return false;
		if (!(o instanceof MobilisMember)) return false;
		return ((MobilisMember) o).getJidWithoutResource().equals(this.jidWithoutResource);
	}
	
}
