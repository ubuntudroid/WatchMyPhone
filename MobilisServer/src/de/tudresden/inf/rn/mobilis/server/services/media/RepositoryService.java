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
package de.tudresden.inf.rn.mobilis.server.services.media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverItems;

import de.tudresden.inf.rn.mobilis.server.HibernateUtil;
import de.tudresden.inf.rn.mobilis.server.MobilisManager;
import de.tudresden.inf.rn.mobilis.server.services.MobilisService;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPUtil;
import de.tudresden.inf.rn.mobilis.xmpp.beans.media.ContentDeleteBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.media.ContentRegisterBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.media.ContentTransferBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.media.RepositoryDeleteBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.media.RepositoryItemInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.media.RepositoryQueryBean;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanExchanger;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanFilterAdapter;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanIQAdapter;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanProviderAdapter;

public class RepositoryService extends MobilisService {
	
	private RepositoryCube repositoryCube = new RepositoryCube();
	private String contentBroker = null;

	public RepositoryService() {
		super();
		this.initRepositoryCube();
	}
	
    protected void initRepositoryCube() {
		Map<String,String> slices = this.getSettingStrings("Slices");
		this.repositoryCube = new RepositoryCube();
    	if (slices != null)
    		for (String slice: slices.keySet())
    			this.repositoryCube.addSlicing(slice, RepositoryType.fromSettingString(
    					slices.get(slice) ));
    }
    
	@Override
	protected void registerPacketListener() {
		XMPPBean repositoryQueryPrototype = new RepositoryQueryBean();
		XMPPBean repositoryDeletePrototype = new RepositoryDeleteBean();
		XMPPBean contentTransferPrototype = new ContentTransferBean();
		(new BeanProviderAdapter(repositoryQueryPrototype)).addToProviderManager();
		(new BeanProviderAdapter(repositoryDeletePrototype)).addToProviderManager();
		(new BeanProviderAdapter(contentTransferPrototype)).addToProviderManager();
		this.mAgent.getConnection().addPacketListener(this, new OrFilter(
				new OrFilter(
					new BeanFilterAdapter(repositoryQueryPrototype),
					new BeanFilterAdapter(repositoryDeletePrototype)
				),
				new BeanFilterAdapter(contentTransferPrototype)
			));
		this.initContentBroker();
		if (!this.outContentRegisterSet()) this.contentBroker = null;
	}
	
    protected String findContentBroker(String jid, ServiceDiscoveryManager discoverer) {
    	if (jid != null) try {
    			Iterator<DiscoverItems.Item> agentItems
    					= discoverer.discoverItems(jid).getItems();
	    		while (agentItems.hasNext()) {
	    			DiscoverItems.Item agentItem = agentItems.next();
	   				String agentJid = this.findContentBroker(agentItem.getEntityID(), discoverer);
	   				if (agentJid != null) return agentJid;
	    		}
	    		Iterator<DiscoverItems.Item> serviceItems
	    				= discoverer.discoverItems(jid, MobilisManager.discoServicesNode).getItems();
	    		while (serviceItems.hasNext()) {
	    			DiscoverItems.Item serviceItem = serviceItems.next();
	    			if (serviceItem.getNode().equals(MobilisManager.discoServicesNode+"/ContentService"))
	    				return serviceItem.getEntityID();
	    		}
    		} catch (XMPPException e) { }
    	return null;
    }
    
    protected void initContentBroker() {
    	ServiceDiscoveryManager discoverer = ServiceDiscoveryManager.getInstanceFor(this.mAgent.getConnection());
    	this.contentBroker = this.findContentBroker(this.getSettingString("ContentBroker"), discoverer);
    }
	
    public void processPacket(Packet p) {
    	super.processPacket(p);
    	if (p instanceof BeanIQAdapter) {
    		XMPPBean b = ((BeanIQAdapter) p).getBean();
    		if (b instanceof RepositoryDeleteBean) {
    			RepositoryDeleteBean bb = (RepositoryDeleteBean) b;
    			if (b.getType() == XMPPBean.TYPE_SET)
    				this.inRepositoryDeleteSet(bb);
    		} else if (b instanceof RepositoryQueryBean) {
    			RepositoryQueryBean bb = (RepositoryQueryBean) b;
    			if (b.getType() == XMPPBean.TYPE_SET)
    				this.inRepositoryQuerySet(bb);
    			else if (b.getType() == XMPPBean.TYPE_GET)
    				this.inRepositoryQueryGet(bb);
    		} else if (b instanceof ContentTransferBean) {
    			ContentTransferBean bb = (ContentTransferBean) b;
    			if (b.getType() == XMPPBean.TYPE_RESULT)
    				this.inContentTransferResult(bb);
    			else if (b.getType() == XMPPBean.TYPE_GET)
    				this.inContentTransferGet(bb);
    		}
    	}
    }

	protected boolean outContentTransferSet(String contentBroker, String uid, String owner) {
    	XMPPConnection c = this.mAgent.getConnection(); 
    	String me = c.getUser();
    	if (contentBroker != null) {
    		ContentTransferBean contentBean = new ContentTransferBean();
    		contentBean.setFrom(me);
    		contentBean.setTo(contentBroker);
    		contentBean.setType(XMPPBean.TYPE_SET);
    		contentBean.setUid(uid);
    		contentBean.setRetrieveFrom(owner);
    		contentBean.setSendTo(contentBroker);
    		c.sendPacket(new BeanIQAdapter(contentBean));
    		return true;
    	} else 
    		return false;
    }
    
    protected boolean outContentDeleteSet(String contentBroker, String uid) {
    	XMPPConnection c = this.mAgent.getConnection();
    	String me = c.getUser();
    	if (contentBroker != null) {
    		ContentDeleteBean contentBean = new ContentDeleteBean();
    		contentBean.setFrom(me);
    		contentBean.setTo(contentBroker);
    		contentBean.setType(XMPPBean.TYPE_SET);
    		contentBean.setUid(uid);
    		c.sendPacket(new BeanIQAdapter(contentBean));
    		return true;
    	} else
    		return false;
    }
    
    protected boolean outContentRegisterSet() {
    	XMPPConnection c = this.mAgent.getConnection(); 
    	String contentBroker = this.contentBroker;
    	String me = c.getUser();
    	if (this.contentBroker != null) {
    		ContentRegisterBean beanOut = new ContentRegisterBean();
    		beanOut.setFrom(me);
    		beanOut.setTo(contentBroker);
    		beanOut.setType(XMPPBean.TYPE_SET);
    		BeanExchanger<ContentRegisterBean> exchanger = new BeanExchanger<ContentRegisterBean>(c);
    		ContentRegisterBean beanIn = exchanger.exchange(beanOut);
    		if (beanIn.getFrom().equals(contentBroker) && beanIn.getTo().equals(me)
    				&& beanIn.getType() == XMPPBean.TYPE_RESULT)
    			return true;
    		else 
    			return false;
    	} else
    		return false;
    }
    
	protected void inRepositoryQuerySet(RepositoryQueryBean bean) {
		List<RepositoryItemInfo> itemInfos = bean.getItems();
		String from = bean.getFrom();
		String fromWithoutRessource = XMPPUtil.jidWithoutRessource(from);
		String to = bean.getTo();
		RepositoryQueryBean beanAnswer = new RepositoryQueryBean();
		beanAnswer.setType(XMPPBean.TYPE_RESULT);
		beanAnswer.setId(bean.getId());
		beanAnswer.setFrom(to);
		beanAnswer.setTo(from);
		beanAnswer.setCondition(null);
		Session session = HibernateUtil.getSession();
		for (RepositoryItemInfo itemInfo: itemInfos) {
			RepositoryItem item = this.repositoryCube.getItem(itemInfo);
			if (item == null) {
				item = new RepositoryItem();
				item.content = fromWithoutRessource;
			}
			item.fromInfo(itemInfo, this.repositoryCube, false);
			
			item.owner = fromWithoutRessource;
			item.slices.put("owner", fromWithoutRessource);
			String uid = (String)session.save(item);
			RepositoryItemInfo itemInfoAnswer = new RepositoryItemInfo();
			itemInfoAnswer.setContent(this.contentBroker);
			itemInfoAnswer.setOwner(from);
			itemInfoAnswer.setUid(uid);
			beanAnswer.getItems().add(itemInfoAnswer);
			session.flush();
			this.outContentTransferSet(this.contentBroker, uid, from);
		}
		this.mAgent.getConnection().sendPacket(new BeanIQAdapter(beanAnswer));
	}
	
	protected void inRepositoryQueryGet(RepositoryQueryBean bean) {
		Collection<RepositoryItem> items   = this.repositoryCube.getItems(bean.getCondition());
		List<RepositoryItemInfo> itemInfos = new ArrayList<RepositoryItemInfo>(items.size());
		for (RepositoryItem item: items)
			itemInfos.add(item.toInfo(this.repositoryCube));
		RepositoryQueryBean beanAnswer = bean.clone(); 
		beanAnswer.setType(XMPPBean.TYPE_RESULT);
		beanAnswer.setFrom(bean.getTo());
		beanAnswer.setTo(bean.getFrom());
		beanAnswer.setCondition(null);
		beanAnswer.getItems().clear();
		beanAnswer.getItems().addAll(itemInfos);
		this.mAgent.getConnection().sendPacket(new BeanIQAdapter(beanAnswer));
	}
	
	protected void inRepositoryDeleteSet(RepositoryDeleteBean bean) {
		List<RepositoryItemInfo> itemInfos = bean.getItems();
		String from = bean.getFrom();
		String to = bean.getTo();
		RepositoryQueryBean beanAnswer = new RepositoryQueryBean();
		beanAnswer.setType(XMPPBean.TYPE_RESULT);
		beanAnswer.setId(bean.getId());
		beanAnswer.setFrom(to);
		beanAnswer.setTo(from);
		beanAnswer.setCondition(null);
		Session session = HibernateUtil.getSession();
		for (RepositoryItemInfo itemInfo: itemInfos) {
			String uid = itemInfo.getUid(); 
			if (uid != null) {
				RepositoryItem item = this.repositoryCube.getItem(itemInfo);
				if (item != null && XMPPUtil.similarJid(item.owner, from)) {
					RepositoryItemInfo itemInfoAnswer = new RepositoryItemInfo();
					session.delete(item);
					itemInfoAnswer.setUid(uid);
					beanAnswer.getItems().add(itemInfoAnswer);
					if (item.content.equals(this.contentBroker))
						this.outContentDeleteSet(this.contentBroker, uid);
				}
			}
		}
		session.flush();
		this.mAgent.getConnection().sendPacket(new BeanIQAdapter(beanAnswer));
	}
	
    private void inContentTransferGet(ContentTransferBean bean) {
    	XMPPConnection c = this.mAgent.getConnection(); 
    	String me = c.getUser();
    	String uid = bean.getUid();
		String from = bean.getFrom();
		String to = bean.getTo();
		String retrieveFrom = bean.getRetrieveFrom();
		String sendTo = bean.getSendTo();
		RepositoryItem item = this.repositoryCube.getItem(uid);
		ContentTransferBean beanAnswer = bean.clone();
		beanAnswer.setTo(from); beanAnswer.setFrom(to);
		if (item != null && sendTo.equals(from) && retrieveFrom.equals(item.content)) {
			ContentTransferBean beanToContent = bean.clone();
			beanToContent.setTo(item.content);
			beanToContent.setFrom(me);
			beanAnswer.setType(XMPPBean.TYPE_RESULT);
			c.sendPacket(new BeanIQAdapter(beanToContent));
		} else
			beanAnswer.setType(XMPPBean.TYPE_ERROR);
		c.sendPacket(new BeanIQAdapter(beanAnswer));
	}
	
	protected void inContentTransferResult(ContentTransferBean bean) {
		String uid = bean.getUid();
		String content = bean.getFrom();
		Session session = HibernateUtil.getSession();
		if (uid != null) {
			RepositoryItem item = this.repositoryCube.getItem(uid);
			if (item != null && content.equals(this.contentBroker)) {
				item.content = content;
				session.save(item);
				session.flush();
			}
		} else ; // TODO
	}
	
}
