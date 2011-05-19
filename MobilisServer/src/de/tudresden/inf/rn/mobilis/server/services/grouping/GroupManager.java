package de.tudresden.inf.rn.mobilis.server.services.grouping;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.hibernate.Session;

import de.tudresden.inf.rn.mobilis.server.HibernateUtil;
import de.tudresden.inf.rn.mobilis.server.services.media.RepositoryItem;
import de.tudresden.inf.rn.mobilis.server.services.media.RepositoryType;
import de.tudresden.inf.rn.mobilis.xmpp.beans.ConditionInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPUtil;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupCreateBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupMemberInfoBean;

public class GroupManager {
	
	private GroupingService groupingService;
	
	public GroupManager(GroupingService groupingService) {
		this.groupingService=groupingService;
	}

	
	public MobilisGroup createAndStoreGroup(GroupCreateBean bean, MobilisMember founder) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();

//		String founderName = founder.getRealName();
//		if (founderName==null) founderName = founder.getJidWithoutResource();
		
		Date dateJoinStartTime=null, dateJoinEndTime=null,
			dateStartTime=null, dateEndTime=null;		
		if (bean.joinStartTime>Long.MIN_VALUE)
			dateJoinStartTime = new Date(bean.joinStartTime);
		if (bean.joinEndTime>Long.MIN_VALUE)
			dateJoinEndTime = new Date(bean.joinEndTime);
		if (bean.startTime>Long.MIN_VALUE)
			dateStartTime = new Date(bean.startTime);
		if (bean.endTime>Long.MIN_VALUE)
			dateEndTime = new Date(bean.endTime);
		
        session.beginTransaction();
                
        MobilisGroup newGroup = new MobilisGroup(bean.name,
        		bean.description, bean.address,
        		bean.longitude_e6, bean.latitude_e6, bean.visibilityRadius,
        		bean.visibilityLongitude_e6, bean.visibilityLatitude_e6,
        		bean.joinRadius, bean.joinLongitude_e6, bean.joinLatitude_e6,
        		dateJoinStartTime, dateJoinEndTime,
        		dateStartTime, dateEndTime, bean.privacy,
        		bean.link, founder);
        // Let the founder also join the group.
        newGroup.getMembers().add(founder);
        founder.getGroups().add(newGroup);
              
        session.save(newGroup);
        session.update(founder);
        session.getTransaction().commit();
    
        return newGroup;
	}	
			
	/**
	 * Add all other xmpp users to the list of invitees of a group.
	 * @param invitees the Set of JIDs of the invited friends
	 * @param group the group, which the friends are invited to. 
	 */
	public void addInviteesToGroup(Set<String> invitees, MobilisGroup group) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();        
		
		for (String jid : invitees)
			group.addInvitee(XMPPUtil.jidWithoutRessource(jid));
		
		session.update(group);
        session.getTransaction().commit();		
	}
	
	/**
	 * 
	 * @param jid the Jabber ID of the MobilisMember
	 * @return the MobilisMember object with the given Jabber ID or null if no MobilisMember with that jid was found in the database.
	 */
	public MobilisMember getMember(String jid) {
		jid=XMPPUtil.jidWithoutRessource(jid);
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();        
        Object o = session.get(MobilisMember.class, jid);        
        session.getTransaction().commit();                
        if (o!=null) {
        	//System.out.println("GroupManager - getMember --> Member with jid="+jid+" found");
        	return (MobilisMember) o;
        } else {
        	//System.out.println("GroupManager - getMember --> Member with jid="+jid+" NOT found");        	
        }
        return null;
	}
	
	/**
	 * 
	 * @param groupId the identifier of the group
	 * @return the MobilisGroup object with the given group id or null if no MobilisGroup with that group id was found in the database.
	 */
	public MobilisGroup getGroup(String groupId) {		
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();        
        Object o = session.get(MobilisGroup.class, groupId);        
        session.getTransaction().commit();        
        if (o!=null) {
        	//System.out.println("GroupManager - getGroup --> Member with groupId="+groupId+" found");
        	return (MobilisGroup) o;
        } else {
        	//System.out.println("GroupManager - getGroup --> Member with groupId="+groupId+" NOT found");        	
        }
        return null;
	}
	
	/**
	 * Checks whether a MobilisMember is a member of MobilisGroup, or not.
	 * @param jid JID of the MobilisMember
	 * @param groupId ID of the Group
	 * @return true, when the the MobilisMember is a member of the MobilisGroup. false, otherwise.
	 */
	public boolean isMember(String jid, String groupId) {		
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        
		MobilisGroup group = (MobilisGroup) session.get(MobilisGroup.class, groupId);
		boolean result = false;
		if (group!=null)
			for (Object o: group.getMembers())
				if (((MobilisMember) o).getJidWithoutResource().equals(jid)) {
					result = true;
					break;
				}				
		//boolean result = session.createQuery("select GROUP_ID from mobilisgroups where GROUP_ID='"+groupId+"' and MEMBER_ID='"+jid+"'").list().isEmpty();        
        session.getTransaction().commit();       
        return result;
	}
	
	/**
	 * 
	 * @param member the MobilisMember who wants to join the group
	 * @param group the MobilisGroup, which should be joined
	 * @return true, if adding member to group was successful, false otherwise.
	 */
	public boolean addMemberToGroup(MobilisMember member, MobilisGroup group) {		
		if (member!=null && group!=null) {
	        Session session = HibernateUtil.getSessionFactory().getCurrentSession();       
	        session.beginTransaction();        
	        boolean success1 = group.getMembers().add(member);
	        boolean success2 = member.getGroups().add(group);
	        group.removeInvitee(member.getJidWithoutResource());
	        session.update(group);
	        session.update(member);
	        session.getTransaction().commit();
	        return (success1 && success2);
		}
		return false;
    }
	/**
	 * 
	 * @param member the MobilisMember who should be removed from the group.
	 * @param group the MobilisGroup, which the member should be removed from.
	 * @return true, if removing member from group was successful, false otherwise.
	 */
	public boolean removeMemberFromGroup(MobilisMember member, MobilisGroup group) {		
		if (member!=null && group!=null) {
			boolean success1=false, success2=false;
	        Session session = HibernateUtil.getSessionFactory().getCurrentSession();       
	        session.beginTransaction();     
	        Set members = group.getMembers();
	        for (Object o : members)
	        	if (((MobilisMember) o).getJidWithoutResource().equals(member.getJidWithoutResource())) {
	        		members.remove(o);
	        		success1 = true;
	        		break;
	        	}
	        Set groups = member.getGroups();
	        for (Object o : groups)	 
	        	if (((MobilisGroup) o).getGroupId().equals(group.getGroupId())) {
	        		groups.remove(o);
	        		success2 = true;
	        		break;
	        	}
	        if (members.size()<1)
	        	session.delete(group);
	         else
	        	 session.update(group);
	        session.update(member);
	        session.getTransaction().commit();
	        return (success1 && success2);
		}
		return false;
    }
	
	/**
	 * 
	 * @param bean
	 * @return the newly created or updated MobilisMember.
	 */
	public MobilisMember createOrUpdateMember(GroupMemberInfoBean bean) {
		
		MobilisMember theMember = getMember(bean.getFrom());
		
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		
		if (theMember==null) {
			// Create new MobilisMember:
//			System.out.println("Create new MobilisMember");
			theMember = new MobilisMember(XMPPUtil.jidWithoutRessource(bean.getFrom()), bean.realName, bean.age, bean.email, bean.homepage, bean.city);
			session.save(theMember);
			// Send a GET request for more information about the newly created user
			groupingService.outGroupMemberInfoGet(bean.getFrom());
			
		} else {
			// Update the existing MobilisMember
//			System.out.println("Update the existing MobilisMember");						
			if (bean.age>Integer.MIN_VALUE)
				theMember.setAge(bean.age);
			if (bean.city!=null)
				theMember.setCity(bean.city);
			if (bean.email!=null)
				theMember.setEmail(bean.email);
			if (bean.homepage!=null)
				theMember.setHomepage(bean.homepage);
			if (bean.realName!=null)
				theMember.setRealName(bean.realName);
			session.update(theMember);
		}				
	    session.getTransaction().commit();
	    return theMember;
	}
	
	public void updateGroup(MobilisGroup group, GroupCreateBean bean) {		
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		
		if (bean.name!=null)
			group.setName(bean.name);
		if (bean.description!=null)
			group.setDescription(bean.description);
		if (bean.address!=null)
			group.setAddress(bean.address);	
		if (bean.latitude_e6>Integer.MIN_VALUE)
			group.setLatitude_e6(bean.latitude_e6);
		if (bean.longitude_e6>Integer.MIN_VALUE)
			group.setLongitude_e6(bean.longitude_e6);
		if (bean.visibilityRadius>Integer.MIN_VALUE)
			group.setVisibilityRadius(bean.visibilityRadius);
		if (bean.visibilityLongitude_e6>Integer.MIN_VALUE)
			group.setVisibilityLongitude_e6(bean.visibilityLongitude_e6);
		if (bean.visibilityLatitude_e6>Integer.MIN_VALUE)
			group.setVisibilityLatitude_e6(bean.visibilityLatitude_e6);
		if (bean.joinRadius>Integer.MIN_VALUE)
			group.setJoinRadius(bean.joinRadius);
		if (bean.joinLongitude_e6>Integer.MIN_VALUE)
			group.setJoinLongitude_e6(bean.joinLongitude_e6);
		if (bean.joinLatitude_e6>Integer.MIN_VALUE)
			group.setJoinLatitude_e6(bean.joinLatitude_e6);
		if (bean.startTime>Long.MIN_VALUE)
			group.setStartTime(new Date(bean.startTime));
		if (bean.endTime>Long.MIN_VALUE)
			group.setEndTime(new Date(bean.endTime));
		if (bean.joinEndTime>Long.MIN_VALUE)
			group.setJoinEndTime(new Date(bean.joinEndTime));
		if (bean.joinStartTime>Long.MIN_VALUE)
			group.setJoinStartTime(new Date(bean.joinStartTime));
		if (bean.privacy!=null && !bean.privacy.equals(""))
			group.setPrivacy(bean.privacy);
		if (bean.link!=null)
			group.setLink(bean.link);
				
		session.update(group);
		session.getTransaction().commit();
		
	}	
	
	private Map<String,String> generateKeyLookup(ConditionInfo condition) {
		Map<String, String> keyLookup = new TreeMap<String,String>();		
		keyLookup.put("latitude_e6", "latitude_e6");
		keyLookup.put("longitude_e6", "longitude_e6");		
		return keyLookup;
	}	
	
	private String generateHQLStatement(ConditionInfo condition) {
		Map<String, String> keyLookup = this.generateKeyLookup(condition);
		StringBuilder sb = new StringBuilder()
				.append("from ").append(MobilisGroup.class.getCanonicalName()).append(" it");
		if (condition != null)
			sb.append(" where ").append(condition.toHQL(keyLookup));
		return sb.toString();
	}

	public Collection<MobilisGroup> getGroups(ConditionInfo condition) {
		String q = this.generateHQLStatement(condition);
		Session s = (Session) HibernateUtil.getSession();
		s.beginTransaction();	
		Collection<MobilisGroup> result = (Collection<MobilisGroup>) s.createQuery(q).list();
		
		for (MobilisGroup g : result) {
			s.refresh(g);
		}
		s.getTransaction().commit();		
		return result;
	}

	
}
