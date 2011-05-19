/*******************************************************************************
 * Copyright (C) 2010 Technische Universität Dresden
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
package de.tudresden.inf.rn.mobilis.server.services.grouping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.muc.MultiUserChat;

import de.tudresden.inf.rn.mobilis.server.HibernateUtil;
import de.tudresden.inf.rn.mobilis.server.MobilisManager;
import de.tudresden.inf.rn.mobilis.server.agents.MobilisAgent;
import de.tudresden.inf.rn.mobilis.server.services.AppSpecificService;
import de.tudresden.inf.rn.mobilis.server.services.CoordinatorService;
import de.tudresden.inf.rn.mobilis.server.services.MobilisService;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPUtil;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupCreateBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupDeleteBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupInfoBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupInviteBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupItemInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupJoinBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupLeaveBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupMemberInfoBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupQueryBean;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanFilterAdapter;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanIQAdapter;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanProviderAdapter;

/**
 * 
 * @author Robert Lübke
 *
 */
public class GroupingService extends MobilisService {

	private String serviceVersion = "1.1";
	private GroupManager groupManager;
	private static final String CONST_INVITE_ONLY = "Only invited people can join";
	private String uri;
	
	public GroupingService() {
		super();
		System.out.println("GroupingService created");
		this.groupManager = new GroupManager(this);
//		MobilisManager.getInstance().getSettingString(
//				"services",
//				"GroupingService",
//				"key");	
				
				
	}
	
	@Override
	public String getVersion() {
		return serviceVersion;
	}
	
	public String getNode() {
		return super.getNode() + "#" + serviceVersion;
	}
	
	public void startup(MobilisAgent agent) throws Exception {
		super.startup(agent);
		this.uri="mobilis://"+mAgent.getConnection().getUser()+"#";
	}
	
	
	private String uriToGroupId(String uri) {
		if (uri==null || uri.equals("")) return null;		
		String[] s = uri.split("#");
		if (s.length==2) {
			return s[1];
		}
		return null;
	}
	
	
	@Override
	protected void registerPacketListener() {
		XMPPBean groupCreatePrototype = new GroupCreateBean();
		XMPPBean groupDeletePrototype = new GroupDeleteBean();
		XMPPBean groupInfoPrototype = new GroupInfoBean();
		XMPPBean groupJoinPrototype = new GroupJoinBean();
		XMPPBean groupLeavePrototype = new GroupLeaveBean();
//		XMPPBean groupUpdatePrototype = new GroupUpdateBean();
		XMPPBean groupMemberInfoPrototype = new GroupMemberInfoBean();
		XMPPBean groupQueryPrototype = new GroupQueryBean();
		XMPPBean groupInvitePrototype = new GroupInviteBean();
		(new BeanProviderAdapter(groupCreatePrototype)).addToProviderManager();
		(new BeanProviderAdapter(groupDeletePrototype)).addToProviderManager();
		(new BeanProviderAdapter(groupInfoPrototype)).addToProviderManager();
		(new BeanProviderAdapter(groupJoinPrototype)).addToProviderManager();
		(new BeanProviderAdapter(groupLeavePrototype)).addToProviderManager();
//		(new BeanProviderAdapter(groupUpdatePrototype)).addToProviderManager();	
		(new BeanProviderAdapter(groupMemberInfoPrototype)).addToProviderManager();		
		(new BeanProviderAdapter(groupQueryPrototype)).addToProviderManager();	
		(new BeanProviderAdapter(groupInvitePrototype)).addToProviderManager();	
		this.mAgent.getConnection().addPacketListener(this,
				new OrFilter(
					new OrFilter(
						new OrFilter(
							new OrFilter(
								new BeanFilterAdapter(groupCreatePrototype),
								new BeanFilterAdapter(groupDeletePrototype)),
							new OrFilter(
								new BeanFilterAdapter(groupInfoPrototype),
								new BeanFilterAdapter(groupJoinPrototype)) ),
						new OrFilter(
							new BeanFilterAdapter(groupLeavePrototype),
							new BeanFilterAdapter(groupMemberInfoPrototype))),
					new OrFilter(
							new BeanFilterAdapter(groupInvitePrototype),
							new BeanFilterAdapter(groupQueryPrototype)) 		
			));
	}
	
    public void processPacket(Packet p) {
    	super.processPacket(p);
    	if (p instanceof BeanIQAdapter) {
    		XMPPBean b = ((BeanIQAdapter) p).getBean();
    		
    		if (b instanceof GroupCreateBean) {
    			GroupCreateBean bb = (GroupCreateBean) b;    			
    			if (b.getType() == XMPPBean.TYPE_SET)
    				this.inGroupCreateSet(bb);    				
    		} else if (b instanceof GroupDeleteBean) {
    			GroupDeleteBean bb = (GroupDeleteBean) b;    			
				if (b.getType() == XMPPBean.TYPE_SET)
					this.inGroupDeleteSet(bb);
    		} else if (b instanceof GroupInfoBean) {
    			GroupInfoBean bb = (GroupInfoBean) b;    			
				if (b.getType() == XMPPBean.TYPE_GET)
					this.inGroupInfoGet(bb); 
    		} else if (b instanceof GroupJoinBean) {
    			GroupJoinBean bb = (GroupJoinBean) b;    			
				if (b.getType() == XMPPBean.TYPE_SET)
					this.inGroupJoinSet(bb); 
    		} else if (b instanceof GroupLeaveBean) {
    			GroupLeaveBean bb = (GroupLeaveBean) b;
				if (b.getType() == XMPPBean.TYPE_SET)
					this.inGroupLeaveSet(bb); 
//    		} else if (b instanceof GroupUpdateBean) {
//    			GroupUpdateBean bb = (GroupUpdateBean) b;    			
//				if (b.getType() == XMPPBean.TYPE_SET)
//					this.inGroupUpdateSet(bb); 
    		} else if (b instanceof GroupMemberInfoBean) {
    			GroupMemberInfoBean bb = (GroupMemberInfoBean) b;    			
				if (b.getType() == XMPPBean.TYPE_SET)
					this.inGroupMemberInfoSet(bb);
				if (b.getType() == XMPPBean.TYPE_GET)
					this.inGroupMemberInfoGet(bb); 
    		} else if (b instanceof GroupQueryBean) {
    			GroupQueryBean bb = (GroupQueryBean) b;			
				if (b.getType() == XMPPBean.TYPE_GET)
					this.inGroupQueryGet(bb); 
    		} else if (b instanceof GroupInviteBean) {
    			GroupInviteBean bb = (GroupInviteBean) b;			
    			if (b.getType() == XMPPBean.TYPE_SET)
    				this.inGroupInviteSet(bb); 
    		}
    		
    	}
    }
    
    private void inGroupInviteSet(GroupInviteBean bean) {
    	XMPPConnection c = this.mAgent.getConnection();
    	String from = bean.getFrom();
		String to = bean.getTo();
		
		MobilisGroup group = groupManager.getGroup(uriToGroupId(bean.groupId));
		MobilisMember sender = groupManager.getMember(bean.getFrom());
		
		GroupInviteBean beanAnswer=null;		
		
		if (group==null) {
			//Group not found
			beanAnswer = new GroupInviteBean("modify", "not-acceptable", "This group id does not exist");
			beanAnswer.setType(XMPPBean.TYPE_ERROR);
		} else if (sender==null || !groupManager.isMember(sender.getJidWithoutResource(), group.getGroupId())) {
			//sender is not a member of the group
			beanAnswer = new GroupInviteBean("auth", "forbidden", "You are not a member of that group.");
			beanAnswer.setType(XMPPBean.TYPE_ERROR);
		//TODO: Invite Rights
//		} else if (!senderHasTheRightToInvite) {
//			//sender has not the right to invite others
//			beanAnswer = new GroupInviteBean("auth", "forbidden", "You don't have the required rights to invite others to that group.");
//			beanAnswer.setType(XMPPBean.TYPE_ERROR);
		} else {	
			//sender is member of the group has the right to invite
			beanAnswer = new GroupInviteBean();
			groupManager.addInviteesToGroup(bean.invitees, group);
			
			beanAnswer.setType(XMPPBean.TYPE_RESULT);
			
		}
		beanAnswer.setTo(from); beanAnswer.setFrom(to);
		beanAnswer.setId(bean.getId());
		
		c.sendPacket(new BeanIQAdapter(beanAnswer));
	}


	private void inGroupQueryGet(GroupQueryBean bean) {
    	XMPPConnection c = this.mAgent.getConnection();
    	String from = bean.getFrom();
		String to = bean.getTo();
		
		Collection<MobilisGroup> results = groupManager.getGroups(bean.getCondition());
		List<GroupItemInfo> itemInfos = new ArrayList<GroupItemInfo>(results.size());
		
		Date now = new Date();
		
		for (MobilisGroup g: results) {
			// Sort out all groups with startTime in the future
			if (g.getStartTime()!=null && now.before(g.getStartTime())) continue;
			// Sort out all groups with endTime in the past
			if (g.getEndTime()!=null && now.after(g.getEndTime())) continue;
			// Sort out all groups with not fullfilled visibility restriction
			if (g.getVisibilityRadius()>0 &&
					g.getVisibilityLatitude_e6()>Integer.MIN_VALUE && 
					g.getVisibilityLongitude_e6()>Integer.MIN_VALUE) {
				if (bean.userLongitude>Integer.MIN_VALUE && bean.userLatitude>Integer.MIN_VALUE) {
					long d = distance(g.getVisibilityLatitude_e6(), g.getVisibilityLongitude_e6(), bean.userLatitude, bean.userLongitude);
					if (d>g.getVisibilityRadius())
						//Sort out because user is not inside visibility radius 
						continue;
				} else {
					//Sort out because no user position was provided and visibility-restriction is active
					continue;
				}
			}				
			itemInfos.add(new GroupItemInfo(
					uri+g.getGroupId(),
					g.getName(),
					g.getLatitude_e6(),
					g.getLongitude_e6(),
					g.getMembers().size()));
		}
		GroupQueryBean beanAnswer = new GroupQueryBean();
		
		beanAnswer.setCondition(null);
		beanAnswer.getItems().clear();
		beanAnswer.getItems().addAll(itemInfos);
		
		beanAnswer.setTo(from); beanAnswer.setFrom(to);
		beanAnswer.setId(bean.getId());		
		beanAnswer.setType(XMPPBean.TYPE_RESULT);	
		
		c.sendPacket(new BeanIQAdapter(beanAnswer));
	}


	private void inGroupMemberInfoGet(GroupMemberInfoBean bean) {		
		XMPPConnection c = this.mAgent.getConnection();
    	String from = bean.getFrom();
		String to = bean.getTo();
		
		// Get the Member from the database
		MobilisMember m = this.groupManager.getMember(bean.jidWithoutResource);
		
		Map<String,String> groups = new HashMap<String, String>();
		
				
		GroupMemberInfoBean beanAnswer=null;
		if (m!=null) {
			// Prepare the answer bean and fill it with the content from the member
			Date now = new Date();
			if (m.getGroups()!=null) {
				MobilisGroup g;
				for (Object o : m.getGroups()) {
					g = ((MobilisGroup) o);
					// Sort out all groups with startTime in the future
					if (g.getStartTime()!=null && now.before(g.getStartTime())) continue;
					// Sort out all groups with endTime in the past
					if (g.getEndTime()!=null && now.after(g.getEndTime())) continue;
					groups.put(uri+g.getGroupId(), g.getName());
				}
			}			
			beanAnswer = new GroupMemberInfoBean(m.getRealName(), m.getCity(), m.getEmail(), m.getHomepage(), m.getAge(), m.getJidWithoutResource(), groups);
			beanAnswer.setType(XMPPBean.TYPE_RESULT);
		} else {
			// Member was not found			
			beanAnswer = new GroupMemberInfoBean("modify", "not-acceptable", "This member does not exist.");	
			beanAnswer.setType(XMPPBean.TYPE_ERROR);			
		}
		
		beanAnswer.setTo(from); beanAnswer.setFrom(to);
		beanAnswer.setId(bean.getId());	
		c.sendPacket(new BeanIQAdapter(beanAnswer));
	}
    
    
    /**
     * Sends a GET request for more information about the user
     * @param jidRecipient the jid of the user, we want more information about
     */
    public void outGroupMemberInfoGet(String jidRecipient) {
    	XMPPConnection c = this.mAgent.getConnection();
    	GroupMemberInfoBean beanToSend = new GroupMemberInfoBean();
    	beanToSend.setTo(jidRecipient);
    	beanToSend.setFrom(c.getUser());
		
    	beanToSend.setType(XMPPBean.TYPE_GET);
		
		c.sendPacket(new BeanIQAdapter(beanToSend));
    }

	private void inGroupMemberInfoSet(GroupMemberInfoBean bean) {			
		XMPPConnection c = this.mAgent.getConnection();
    	String from = bean.getFrom();
		String to = bean.getTo();
		
		this.groupManager.createOrUpdateMember(bean);
		
		GroupMemberInfoBean beanAnswer = new GroupMemberInfoBean();
		beanAnswer.setTo(from); beanAnswer.setFrom(to);
		beanAnswer.setId(bean.getId());
		
		beanAnswer.setType(XMPPBean.TYPE_RESULT);
			
		c.sendPacket(new BeanIQAdapter(beanAnswer));
	}


	private void inGroupLeaveSet(GroupLeaveBean bean) {
		XMPPConnection c = this.mAgent.getConnection();
    	String from = bean.getFrom();
		String to = bean.getTo();
		
		MobilisGroup group = groupManager.getGroup(uriToGroupId(bean.groupId));
		MobilisMember sender = groupManager.getMember(bean.getFrom());
		
		GroupLeaveBean beanAnswer=null;		
		
		if (group==null) {
			//Group not found
			beanAnswer = new GroupLeaveBean("modify", "not-acceptable", "This group id does not exist");
			beanAnswer.setType(XMPPBean.TYPE_ERROR);
		} else if (sender==null || !groupManager.isMember(sender.getJidWithoutResource(), group.getGroupId())) {
			//sender is not a member of the group
			beanAnswer = new GroupLeaveBean("modify", "not-acceptable", "You are not a member of that group.");
			beanAnswer.setType(XMPPBean.TYPE_ERROR);
		} else {	
			//sender is member of the group and can be removed now
			beanAnswer = new GroupLeaveBean();			
			boolean success = groupManager.removeMemberFromGroup(sender, group);
			if (success)
				beanAnswer.setType(XMPPBean.TYPE_RESULT);
			else
				beanAnswer.setType(XMPPBean.TYPE_ERROR);
		}		
		beanAnswer.setTo(from); beanAnswer.setFrom(to);
		beanAnswer.setId(bean.getId());			
		
		c.sendPacket(new BeanIQAdapter(beanAnswer));
		
	}


	private void inGroupJoinSet(GroupJoinBean bean) {
		XMPPConnection c = this.mAgent.getConnection();
    	String from = bean.getFrom();
		String to = bean.getTo();
		
		MobilisGroup group = groupManager.getGroup(uriToGroupId(bean.groupId));
		MobilisMember newMember = groupManager.getMember(bean.getFrom());				
		
		GroupJoinBean beanAnswer=null;		
		
		Date now = new Date();
		
		if (group==null) {
			//Group not found
			beanAnswer = new GroupJoinBean("modify", "not-acceptable", "This group id does not exist");
			beanAnswer.setType(XMPPBean.TYPE_ERROR);		
		} else if (group.getPrivacy().equals(CONST_INVITE_ONLY) && !group.isInvited(XMPPUtil.jidWithoutRessource(bean.getFrom()))) {
			//User has no invitation
			beanAnswer = new GroupJoinBean("auth", "forbidden", "This group requires an invitation from a member and you have none.");
			beanAnswer.setType(XMPPBean.TYPE_ERROR);
		} else if (group.getJoinStartTime()!=null && now.before(group.getJoinStartTime())) {
			//Start join time in the future
			beanAnswer = new GroupJoinBean("wait", "unexpected-request", "Start join date is still in the future.");
			beanAnswer.setType(XMPPBean.TYPE_ERROR);
		} else if (group.getJoinEndTime()!=null && now.after(group.getJoinEndTime())) {
			//End join time in the past
			beanAnswer = new GroupJoinBean("cancel", "unexpected-request", "End join date is already in the past.");
			beanAnswer.setType(XMPPBean.TYPE_ERROR);
		//JoinRadius Check
		} else if (group.getJoinRadius()>0 &&
				group.getJoinLatitude_e6()>Long.MIN_VALUE &&
				group.getJoinLongitude_e6()>Long.MIN_VALUE &&
				(bean.userLatitude<=Long.MIN_VALUE || bean.userLongitude<=Long.MIN_VALUE)) {
			//IQ did not include user position, but it is required
			beanAnswer = new GroupJoinBean("cancel", "not-acceptable", "You did not provide your geographical position.");
			beanAnswer.setType(XMPPBean.TYPE_ERROR);		
		} else if (group.getJoinRadius()>0 &&
				group.getJoinLatitude_e6()>Long.MIN_VALUE &&
				group.getJoinLongitude_e6()>Long.MIN_VALUE &&
				distance(bean.userLatitude, bean.userLongitude, group.getJoinLatitude_e6(), group.getJoinLongitude_e6())>group.getJoinRadius()) {
			//Not inside the joinRadius
			beanAnswer = new GroupJoinBean("modify", "not-acceptable", "You are not inside the join radius.");
			beanAnswer.setType(XMPPBean.TYPE_ERROR);			
		} else {
			//Group was found					
			if (newMember==null) {		
				// Member does not already exist in the database
				GroupMemberInfoBean b = new GroupMemberInfoBean();
				b.setFrom(from);
				newMember = groupManager.createOrUpdateMember(b);
			}
			String jid = newMember.getJidWithoutResource();
			String gid = group.getGroupId();
			if (groupManager.isMember(jid, gid)) {
				// 'newMember' is already a member of 'group'
				beanAnswer=new GroupJoinBean("cancel", "conflict", "You are already a member of that group.");
			} else {
				// 'newMember' is not already a member of 'group'
				beanAnswer = new GroupJoinBean();	
				boolean success = groupManager.addMemberToGroup(newMember, group);
				if (success)
					beanAnswer.setType(XMPPBean.TYPE_RESULT);
				else
					beanAnswer.setType(XMPPBean.TYPE_ERROR);
			}
			
		}		
		beanAnswer.setTo(from); beanAnswer.setFrom(to);
		beanAnswer.setId(bean.getId());			
		
		c.sendPacket(new BeanIQAdapter(beanAnswer));
	}


	private void inGroupInfoGet(GroupInfoBean bean) {
		XMPPConnection c = this.mAgent.getConnection();
    	String from = bean.getFrom();
		String to = bean.getTo();
		
		MobilisGroup group = groupManager.getGroup(uriToGroupId(bean.groupId));
		MobilisMember sender = groupManager.getMember(bean.getFrom());
		
		GroupInfoBean beanAnswer=null;
		
		if (group==null) {
			//Group not found
			beanAnswer = new GroupInfoBean("modify", "not-acceptable", "This group id does not exist");
			beanAnswer.setType(XMPPBean.TYPE_ERROR);
		} else {
			//Group found			
			Set<MobilisMember> memberSet = group.getMembers();						
			Map<String, String> memberMap = new HashMap<String, String>();
			for (MobilisMember m : memberSet)
				memberMap.put(m.getJidWithoutResource(), m.getRealName());			
			
			long joinStartTime=Long.MIN_VALUE, joinEndTime=Long.MIN_VALUE;
			long startTime=Long.MIN_VALUE, endTime=Long.MIN_VALUE;
			if (group.getJoinStartTime()!=null)
				joinStartTime = group.getJoinStartTime().getTime();
			if (group.getJoinEndTime()!=null)
				joinEndTime = group.getJoinEndTime().getTime();
			if (group.getStartTime()!=null)
				startTime = group.getStartTime().getTime();
			if (group.getEndTime()!=null)
				endTime = group.getEndTime().getTime();
			String founderName="";
			if (group.getFounder()!=null) {
				Session session = HibernateUtil.getSessionFactory().getCurrentSession();
				founderName = group.getFounder().getRealName();
			}
			beanAnswer = new GroupInfoBean(
					uri+group.getGroupId(),
					group.getName(),
					group.getDescription(),
					group.getAddress(),
					group.getLongitude_e6(), 
					group.getLatitude_e6(),
					group.getVisibilityRadius(),
					group.getVisibilityLongitude_e6(),
					group.getVisibilityLatitude_e6(),
					group.getJoinRadius(),
					group.getJoinLongitude_e6(),
					group.getJoinLatitude_e6(),
					joinStartTime,
					joinEndTime,
					startTime,
					endTime,
					group.getPrivacy(),
					group.getLink(),
					founderName,
					memberMap.size(),
					memberMap);			
			beanAnswer.setType(XMPPBean.TYPE_RESULT);
		}		
		beanAnswer.setTo(from); beanAnswer.setFrom(to);
		beanAnswer.setId(bean.getId());			
		
		c.sendPacket(new BeanIQAdapter(beanAnswer));
	}


	private void inGroupDeleteSet(GroupDeleteBean bean) {
		XMPPConnection c = this.mAgent.getConnection();
    	String from = bean.getFrom();
		String to = bean.getTo();
		
		MobilisGroup group = this.groupManager.getGroup(uriToGroupId(bean.groupId));
				
		GroupDeleteBean beanAnswer=null;
		if (group==null) {
			// Group does not exist
			beanAnswer = new GroupDeleteBean("modify", "not-acceptable", "This group id does not exist");
			beanAnswer.setType(XMPPBean.TYPE_ERROR);				
		} else  if (!group.getFounder().getJidWithoutResource().equals(XMPPUtil.jidWithoutRessource(bean.getFrom()))) {
			// Sender is not the founder of this group
			beanAnswer = new GroupDeleteBean("auth", "forbidden", "You are not the founder of this group. Deleting not allowed.");
			beanAnswer.setType(XMPPBean.TYPE_ERROR);
		} else {
			//Group exists and the sender is also allowed to delete the Group.
			
			Session session = HibernateUtil.getSessionFactory().getCurrentSession();       
	        session.beginTransaction();    
	        
	        session.delete(group);
	        
	        session.getTransaction().commit();
			
			beanAnswer = new GroupDeleteBean();
			beanAnswer.setType(XMPPBean.TYPE_RESULT);
		}
		
		beanAnswer.setTo(from); beanAnswer.setFrom(to);
		beanAnswer.setId(bean.getId());
		c.sendPacket(new BeanIQAdapter(beanAnswer));
	}


	private void inGroupCreateSet(GroupCreateBean bean) {
    	XMPPConnection c = this.mAgent.getConnection();
    	String from = bean.getFrom();
		String to = bean.getTo();
		
		// At first create the member if it doesn't already exist
		GroupMemberInfoBean gmib = new GroupMemberInfoBean();
		gmib.setFrom(from);
		MobilisMember member = this.groupManager.createOrUpdateMember(gmib);
		
		GroupCreateBean beanAnswer=null;
		
		if (bean.startTime>Long.MIN_VALUE && bean.endTime>Long.MIN_VALUE && bean.startTime>=bean.endTime) {
			// StartTime >= EndTime
			beanAnswer = new GroupCreateBean("modify", "not-acceptable", "Start date must be before End date.");
			beanAnswer.setType(XMPPBean.TYPE_ERROR);			
		} else if (bean.joinStartTime>Long.MIN_VALUE && bean.joinEndTime>Long.MIN_VALUE && bean.joinStartTime>=bean.joinEndTime) {
			// JoinStartTime >= JoinEndTime
			beanAnswer = new GroupCreateBean("modify", "not-acceptable", "Start join date must be before End join date.");
			beanAnswer.setType(XMPPBean.TYPE_ERROR);			
		} else if (bean.startTime>Long.MIN_VALUE && bean.joinStartTime>Long.MIN_VALUE && bean.startTime>bean.joinStartTime) {
			// StartTime > JoinStartTime
			beanAnswer = new GroupCreateBean("modify", "not-acceptable", "Start join date must be after Start date.");
			beanAnswer.setType(XMPPBean.TYPE_ERROR);
		} else if (bean.endTime>Long.MIN_VALUE && bean.joinEndTime>Long.MIN_VALUE && bean.endTime<bean.joinEndTime) {
			// EndTime < JoinEndTime
			beanAnswer = new GroupCreateBean("modify", "not-acceptable", "End date must be after End join date.");
			beanAnswer.setType(XMPPBean.TYPE_ERROR);
		} else if (bean.startTime>Long.MIN_VALUE && bean.joinEndTime>Long.MIN_VALUE && bean.startTime>bean.joinEndTime) {
			// StartTime > JoinEndTime
			beanAnswer = new GroupCreateBean("modify", "not-acceptable", "End join date must be after Start date.");
			beanAnswer.setType(XMPPBean.TYPE_ERROR);
		} else if (bean.joinStartTime>Long.MIN_VALUE && bean.endTime>Long.MIN_VALUE && bean.joinStartTime>bean.endTime) {
			// JoinStartTime > EndTime
			beanAnswer = new GroupCreateBean("modify", "not-acceptable", "End date must be after Start join date.");
			beanAnswer.setType(XMPPBean.TYPE_ERROR);
		} else if (bean.groupId==null) {
			// It's a Create Call
			// Create and store the group
			MobilisGroup group = this.groupManager.createAndStoreGroup(bean, member);
			//MUC
			if (c.isConnected()) {				
				String chatRoomServer = MobilisManager.getInstance().getSettingString("services", "GroupingService", "ChatRoomServer");
				MultiUserChat muc = new MultiUserChat(c, group.getGroupId()+"@"+chatRoomServer);
				try {
					muc.create("MobilisServer");					
					muc.changeSubject(group.getGroupId());
					Form oldForm = muc.getConfigurationForm();
					Form newForm = oldForm.createAnswerForm();
					
					for (Iterator<FormField> fields = oldForm.getFields(); fields.hasNext();) {
					    FormField field = (FormField) fields.next();
					    if (!FormField.TYPE_HIDDEN.equals(field.getType()) && field.getVariable() != null) {
					    	newForm.setDefaultAnswer(field.getVariable());
					    }
					}					
					newForm.setAnswer("muc#roomconfig_enablelogging", true);
					newForm.setAnswer("muc#roomconfig_membersonly", false);
					newForm.setAnswer("muc#roomconfig_passwordprotectedroom", false);
					newForm.setAnswer("muc#roomconfig_persistentroom", true);
					newForm.setAnswer("muc#roomconfig_publicroom", true);
					muc.sendConfigurationForm(newForm);
					//Attach the Multi User Chat to its Group
					group.setMUC(muc);					
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else System.out.println("Could not create Multi User Chat, because XMPPConnection is not connected.");
			beanAnswer = new GroupCreateBean(uri+group.getGroupId());		
			beanAnswer.setType(XMPPBean.TYPE_RESULT);
		} else {
			//It's an Update Call
			
			MobilisGroup group = this.groupManager.getGroup(uriToGroupId(bean.groupId));
			if (group==null) {
				// Group does not exist
				beanAnswer = new GroupCreateBean("modify", "not-acceptable", "This group id does not exist");
				beanAnswer.setType(XMPPBean.TYPE_ERROR);				
			} else  if (!group.getFounder().equals(member)) {
				// Sender is not the founder of this group
				beanAnswer = new GroupCreateBean("auth", "forbidden", "You are not the founder of this group. Updating not allowed.");
				beanAnswer.setType(XMPPBean.TYPE_ERROR);
			} else {
				
				groupManager.updateGroup(group, bean);
				
				beanAnswer = new GroupCreateBean();		
				beanAnswer.setType(XMPPBean.TYPE_RESULT);
			}
		}
		
		beanAnswer.setTo(from); beanAnswer.setFrom(to);
		beanAnswer.setId(bean.getId());
		c.sendPacket(new BeanIQAdapter(beanAnswer));		
	}
    
 
	 	
	/**
	 * Calculates the distance between two points(given the latitude/longitude of those points)
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return distance between the points
	 */
	private long distance(int latitude1, int longitude1, int latitude2, int longitude2) {
		double lat1=latitude1/1E6, lon1=longitude1/1E6;
		double lat2=latitude2/1E6, lon2=longitude2/1E6;
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = rad2deg(Math.acos(dist));
		dist = dist * 60 * 1000 * 1.1515 * 1.609344;
//		System.out.println("distance="+dist);
		return Math.round(dist);
	}

	/**
	 * Converts decimal degrees to radians
	 */
	private double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/**
	 * Converts radians to decimal degrees
	 */
	private double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}


}
