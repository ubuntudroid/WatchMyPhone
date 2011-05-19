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
package de.tudresden.inf.rn.mobilis.server.services.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;

import de.tudresden.inf.rn.mobilis.server.MobilisManager;
import de.tudresden.inf.rn.mobilis.server.agents.MobilisAgent;
import de.tudresden.inf.rn.mobilis.server.services.MobilisService;
import de.tudresden.inf.rn.mobilis.xmpp.beans.Mobilis;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPUtil;
import de.tudresden.inf.rn.mobilis.xmpp.beans.context.AuthorizationBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.context.ContextItemInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.context.PubSubBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.context.PublishItemInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.context.SubscribeItemInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.context.UnsubscribeItemInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.context.UserContextInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.context.UserLocationInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.context.UserMoodInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.context.UserTuneInfo;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanFilterAdapter;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanIQAdapter;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanProviderAdapter;

/**
 * 
 * @author Robert Lübke
 *
 */

public class UserContextService extends MobilisService {

	private String uri;
	private String serviceVersion = "1.0";
	/**
	 * Map that manages all context information.
	 * Structure: [NodeOwnerJID, Node]
	 */
	private Map<String, ContextNode> userContextMap;
	/**
	 * Map that manages all subscriptions.
	 * Structure: [NodeOwnerJID], [NodeName, SubscriberList]
	 */
	private Map<String, Map<String,List<String>>> subscriptions;
	
//	private Map<String, PubSubBean> pendingBeanAnswers = new HashMap<String, PubSubBean>();
//	private Map<String, SubscribeItemInfo> pendingSubscriptions = new HashMap<String, SubscribeItemInfo>();
	private List<PendingSubscription> pendingSubscriptions = new ArrayList<PendingSubscription>();
	private Map<String, String> publisherJIDs = new HashMap<String, String>();
	
	public UserContextService() {
		super();
		this.userContextMap = new HashMap<String, ContextNode>();
		this.subscriptions = new HashMap<String, Map<String,List<String>>>();
//		MobilisManager.getInstance().getSettingString(
//				"services",
//				"UserContextService",
//				"key");				
	}
	
	public String getNode() {
		return super.getNode() + "#" + serviceVersion;
	}
	
	public void startup(MobilisAgent agent) throws Exception {
		super.startup(agent);
		this.uri="mobilis://"+mAgent.getConnection().getUser()+"#";
	}
		
	@Override
	protected void registerPacketListener() {
		XMPPBean authPrototype = new AuthorizationBean();		
		XMPPBean pubsubPrototype = new PubSubBean();
		(new BeanProviderAdapter(authPrototype)).addToProviderManager();
		(new BeanProviderAdapter(pubsubPrototype)).addToProviderManager();
		this.mAgent.getConnection().addPacketListener(this,
				new OrFilter(
						new PacketTypeFilter(Presence.class),
						new OrFilter(
								new BeanFilterAdapter(authPrototype),
								new BeanFilterAdapter(pubsubPrototype))
				));
	}
	
    public void processPacket(Packet p) {
    	super.processPacket(p);
//    	System.out.println("processPacket() ID: "+p.getPacketID());
    	
    	if (p instanceof Presence) {
    		Presence pr = (Presence) p;
    		if (pr.getType().equals(Presence.Type.unavailable)) {
    			String bareJID = XMPPUtil.jidWithoutRessource(pr.getFrom());
    			if (publisherJIDs.containsKey(bareJID)) {
    				if (publisherJIDs.get(bareJID).equals(pr.getFrom())) {
    					System.out.println("Presence unavailable --> remove "+pr.getFrom());
    					publisherJIDs.remove(bareJID);
    				}
    			}    					
    		}
    	} else if (p instanceof BeanIQAdapter) {
    		XMPPBean b = ((BeanIQAdapter) p).getBean();
    		
    		if (b instanceof AuthorizationBean) {
    			AuthorizationBean bb = (AuthorizationBean) b;    			
    			if (b.getType() == XMPPBean.TYPE_RESULT)
    				this.inAuthResult(bb);
    			else if (b.getType() == XMPPBean.TYPE_ERROR)
    				this.inAuthError(bb);
    			
    		}  else 
    			if (b instanceof PubSubBean) {
    			PubSubBean bb = (PubSubBean) b;  
    			if (bb.publish!=null)    			
    				this.inPublish(bb);
    			else if (bb.subscribe!=null)
    				this.inSubscribe(bb);
    			else if (bb.unsubscribe!=null)
    				this.inUnsubscribe(bb);
    		}    		
    	}
    }
    
    
	private void inPublish(PubSubBean bean) {
		
    	XMPPConnection c = this.mAgent.getConnection();
    	String from = bean.getFrom();
		String to = bean.getTo();
		
		String node= adjustNodeString(bean.publish.getNode());
		
		PubSubBean beanAnswer = null;
				
		if (node==null || node.equals("") || node.equals("root")) {
			//Error: no correct node given
			beanAnswer = new PubSubBean("modify", "not-acceptable", "You have to provide a valid node.");
		} else {
			
			String userJID = XMPPUtil.jidWithoutRessource(from);
			publisherJIDs.put(userJID, from);
			
			ContextNode rootNode;
			if (!userContextMap.containsKey(userJID)) {
				// User is unknown. Create root node for the new user.
				rootNode = new ContextNode("root");
				userContextMap.put(userJID, rootNode);
			} else {
				// User is already in the Map
				rootNode = userContextMap.get(userJID);
			}
						    		    	
	    	for (ContextItemInfo cii : bean.publish.getContextItemInfos()) {
	    		if (cii.getUserTuneInfo()!=null) {
	    			//USER TUNE Item
//	    			System.out.println("TUNE: "+cii.getUserTuneInfo().toXML());
	    			
	    			ContextNode cn = createOrFindSubNode(rootNode, node);
	    			UserTuneInfo uti = cii.getUserTuneInfo();
	    			
	    			if (uti.artist!=null) cn.addOrUpdateEntry("artist", uti.artist, Mobilis.USERCONTEXT_DATATYPE_STRING);
	    			if (uti.length>Integer.MIN_VALUE) cn.addOrUpdateEntry("length", String.valueOf(uti.length), Mobilis.USERCONTEXT_DATATYPE_INTEGER);
	    			if (uti.rating>Integer.MIN_VALUE) cn.addOrUpdateEntry("rating", String.valueOf(uti.rating), Mobilis.USERCONTEXT_DATATYPE_INTEGER);
	    			if (uti.source!=null) cn.addOrUpdateEntry("source", uti.source, Mobilis.USERCONTEXT_DATATYPE_STRING);
	    			if (uti.title!=null) cn.addOrUpdateEntry("title", uti.title, Mobilis.USERCONTEXT_DATATYPE_STRING);
	    			if (uti.track!=null) cn.addOrUpdateEntry("track", uti.track, Mobilis.USERCONTEXT_DATATYPE_STRING);
	    			if (uti.uri!=null) cn.addOrUpdateEntry("uri", uti.uri, Mobilis.USERCONTEXT_DATATYPE_STRING);
	    			
	    			informSubscribers(userJID, node, cn);

	    		} else if (cii.getUserLocationInfo()!=null) {
	    			//USER LOCATION Item
//	    			System.out.println("LOCATION: "+cii.getUserLocationInfo().toXML());
	    			
	    			ContextNode cn = createOrFindSubNode(rootNode, node);
	    			UserLocationInfo uli = cii.getUserLocationInfo();
	    			
	    			// Add or Update the ContextItems of the locationNode			
	    			boolean updated = false;
	    			if (uli.getAccuracy()>Double.MIN_VALUE)
	    				updated = cn.addOrUpdateEntry("accuracy", ""+uli.getAccuracy(), Mobilis.USERCONTEXT_DATATYPE_DOUBLE) || updated;
	    			if (uli.getAlt()>Double.MIN_VALUE)
	    				updated = cn.addOrUpdateEntry("alt", ""+uli.getAlt(), Mobilis.USERCONTEXT_DATATYPE_DOUBLE) || updated;
	    			if (uli.getArea()!=null)
	    				updated = cn.addOrUpdateEntry("area", ""+uli.getArea(), Mobilis.USERCONTEXT_DATATYPE_STRING) || updated;
	    			if (uli.getBearing()>Double.MIN_VALUE)
	    				updated = cn.addOrUpdateEntry("bearing", ""+uli.getBearing(), Mobilis.USERCONTEXT_DATATYPE_DOUBLE) || updated;
	    			if (uli.getBuilding()!=null)
	    				updated = cn.addOrUpdateEntry("building", ""+uli.getBuilding(), Mobilis.USERCONTEXT_DATATYPE_STRING) || updated;
	    			if (uli.getCountry()!=null)
	    				updated = cn.addOrUpdateEntry("country", ""+uli.getCountry(), Mobilis.USERCONTEXT_DATATYPE_STRING) || updated;
	    			if (uli.getCountrycode()!=null)
	    				updated = cn.addOrUpdateEntry("countrycode", ""+uli.getCountrycode(), Mobilis.USERCONTEXT_DATATYPE_STRING) || updated;
	    			if (uli.getDatum()!=null)
	    				updated = cn.addOrUpdateEntry("datum", ""+uli.getDatum(), Mobilis.USERCONTEXT_DATATYPE_STRING) || updated;
	    			if (uli.getDescription()!=null)
	    				updated = cn.addOrUpdateEntry("description", ""+uli.getDescription(), Mobilis.USERCONTEXT_DATATYPE_STRING) || updated;
	    			if (uli.getError()>Double.MIN_VALUE)
	    				updated = cn.addOrUpdateEntry("error", ""+uli.getError(), Mobilis.USERCONTEXT_DATATYPE_DOUBLE) || updated;
	    			if (uli.getFloor()!=null)
	    				updated = cn.addOrUpdateEntry("floor", ""+uli.getFloor(), Mobilis.USERCONTEXT_DATATYPE_STRING) || updated;
	    			if (uli.getLat()>Double.MIN_VALUE)
	    				updated = cn.addOrUpdateEntry("lat", ""+uli.getLat(), Mobilis.USERCONTEXT_DATATYPE_DOUBLE) || updated;
	    			if (uli.getLocality()!=null)
	    				updated = cn.addOrUpdateEntry("locality", ""+uli.getLocality(), Mobilis.USERCONTEXT_DATATYPE_STRING) || updated;
	    			if (uli.getLon()>Double.MIN_VALUE)
	    				updated = cn.addOrUpdateEntry("lon", ""+uli.getLon(), Mobilis.USERCONTEXT_DATATYPE_DOUBLE) || updated;
	    			if (uli.getPostalcode()!=null)
	    				updated = cn.addOrUpdateEntry("postalcode", ""+uli.getPostalcode(), Mobilis.USERCONTEXT_DATATYPE_STRING) || updated;
	    			if (uli.getRegion()!=null)
	    				updated = cn.addOrUpdateEntry("region", ""+uli.getRegion(), Mobilis.USERCONTEXT_DATATYPE_STRING) || updated;
	    			if (uli.getRoom()!=null)
	    				updated = cn.addOrUpdateEntry("room", ""+uli.getRoom(), Mobilis.USERCONTEXT_DATATYPE_STRING) || updated;
	    			if (uli.getSpeed()>Double.MIN_VALUE)
	    				updated = cn.addOrUpdateEntry("speed", ""+uli.getSpeed(), Mobilis.USERCONTEXT_DATATYPE_DOUBLE) || updated;
	    			if (uli.getStreet()!=null)
	    				updated = cn.addOrUpdateEntry("street", ""+uli.getStreet(), Mobilis.USERCONTEXT_DATATYPE_STRING) || updated;
	    			if (uli.getText()!=null)
	    				updated = cn.addOrUpdateEntry("text", ""+uli.getText(), Mobilis.USERCONTEXT_DATATYPE_STRING) || updated;
	    			if (uli.getTimestamp()!=null)
	    				updated = cn.addOrUpdateEntry("timestamp", ""+uli.getTimestamp(), Mobilis.USERCONTEXT_DATATYPE_STRING) || updated;
	    			if (uli.getUri()!=null)
	    				updated = cn.addOrUpdateEntry("uri", ""+uli.getUri(), Mobilis.USERCONTEXT_DATATYPE_STRING) || updated;	
	    			
	    			informSubscribers(userJID, node, cn);	    			
	    			
	    		} else if (cii.getUserMoodInfo()!=null) {
	    			//USER MOOD Item
//	    			System.out.println("MOOD: "+cii.getUserMoodInfo().toXML());
	    			
	    			ContextNode cn = createOrFindSubNode(rootNode, node);
	    			UserMoodInfo umi = cii.getUserMoodInfo();
	    			
	    			if (umi.getMoodElement()!=null)
	    				cn.addOrUpdateEntry("moodElement",
	    						umi.getMoodElement(),
	    						Mobilis.USERCONTEXT_DATATYPE_STRING);
	    			if (umi.getMoodDescription()!=null)
	    				cn.addOrUpdateEntry("moodDescription",
	    						umi.getMoodDescription(),
	    						Mobilis.USERCONTEXT_DATATYPE_STRING);
	    			
	    			informSubscribers(userJID, node, cn);	
	    			
	    		} else if (cii.getUserContextInfo()!=null) {
	    			//USER CONTEXT Item
//	    			System.out.println("CONTEXT: "+cii.getUserContextInfo().toXML());
	    			
	    			ContextNode cn = createOrFindSubNode(rootNode, node);
	    			UserContextInfo uci = cii.getUserContextInfo();	    			
	    			
	    			cn.addOrUpdateEntry(uci.getKey(), uci.getValue(), uci.getType());
	    			
	    			informSubscribers(userJID, node, cn);	    			
	    			
	    		}
	    	}
	    	
	    	//ContextTree is updated. Now we can prepare the answer bean with type RESULT.  	
	    	beanAnswer= new PubSubBean();  
	    	beanAnswer.publish = new PublishItemInfo();
	    	
			}
		
		beanAnswer.setTo(from); beanAnswer.setFrom(to);
		beanAnswer.setId(bean.getId());		
		c.sendPacket(new BeanIQAdapter(beanAnswer));
		
		printContextTree();
    		
	}
    
	/**
	 * Informs all subscribers of a special node, that the node was updated.
	 * @param ownerJID JID of the node owner
	 * @param nodePath the node path
	 * @param node the node itself.
	 */
    private void informSubscribers(String ownerJID, String nodePath, ContextNode node) {
    	XMPPConnection c = this.mAgent.getConnection();
    	    	    	
    	//Prepare the Message to be sent:    	
    	Message message = new Message();
    	message.setFrom(this.mAgent.getFullJid()); 
    	message.setType(Message.Type.normal);
    	String userNodeFormat = XMPPUtil.jidWithoutRessource(ownerJID)+"/"+nodePath;
    	PacketExtension pe = new MessagePayloadPacketExtension(node, userNodeFormat);   	
    	message.addExtension(pe);
    	
    	int i=0;
    	// Get the list of subscribers
    	if (subscriptions.containsKey(ownerJID)) {
    		Map<String, List<String>> usersSub = subscriptions.get(ownerJID);
    		if (usersSub.containsKey(nodePath)) {
    			List<String> subscriptionList = usersSub.get(nodePath);
    			for (String subscriberJID : subscriptionList) {
    				//Inform each subscriber with the message:
    				message.setTo(subscriberJID);
    				c.sendPacket(message);
    				i++;
    			}
    		}    		
    	}
    	System.out.println("Informed "+i+" subscribers.");		
	}
    
    /**
     * Informs a new Subscriber about current status of the node he subscribed to.
     * @param ownerJID JID of the node owner the subscriber subscribed to
     * @param nodePath the node path the subscriber subscribed to
     * @param newSubscriber the JID of the subscriber
     */
    private void informSubscriber(String ownerJID, String nodePath, String newSubscriber) {
    	XMPPConnection c = this.mAgent.getConnection();
    	    	    	
    	ContextNode node;
    	node = createOrFindSubNode(userContextMap.get(ownerJID), nodePath);
    	
    	//Prepare the Message to be sent:    	
    	Message message = new Message();
    	message.setFrom(this.mAgent.getFullJid()); 
    	message.setType(Message.Type.normal);
    	String userNodeFormat = XMPPUtil.jidWithoutRessource(ownerJID)+"/"+nodePath;
    	PacketExtension pe = new MessagePayloadPacketExtension(node, userNodeFormat);   	
    	message.addExtension(pe);
    	
		//Inform the new subscriber with the message:
		message.setTo(newSubscriber);
		c.sendPacket(message);
		
    	System.out.println("Informed the new subscriber about current status of the node.");		
	}
	

	/**
     * Deletes leading, ending and multiple slashes in a String
     * and adjust the name of a node for special XEPs.
     * @param node the String
     * @return the adjusted String representation of the node
     */
    private String adjustNodeString(String node) {		
		if (node!=null && !node.equals("")) {
			if (node.equals(UserTuneInfo.NAMESPACE)) node="tune";
			else if (node.equals(UserMoodInfo.NAMESPACE)) node="mood";
			else if (node.equals(UserLocationInfo.NAMESPACE)) node="location";
			else {
		    	while (node.contains("//")) {	node=node.replaceAll("//", "/"); }			
				if (node.startsWith("/")) {	node=node.substring(1); }
				if (node.endsWith("/")) {	node=node.substring(0, node.length()-1); }
			}
		}
		return node;
	}

	private void printContextTree() {
    	System.out.println("PRINTING CONTEXT TREE\n");
		for (String user : this.userContextMap.keySet()) {
			System.out.println("USER: "+user);
			System.out.println(ContextNode.toStringTree(this.userContextMap.get(user)));
		}
		
	}

	/**
     * Creates a new subnode or finds an existing subnode in the context tree of a user.
     * @param rootNode the root context node of the user
     * @param nodePath the path of the context node which should be created or found.
     * @return
     */
    private ContextNode createOrFindSubNode(ContextNode rootNode, String nodePath) {    	
		ContextNode currentNode = rootNode, nextSubNode;
		String n;				
		StringTokenizer st = new StringTokenizer(nodePath, "/");
		while (st.hasMoreTokens()) {
			n = st.nextToken();
//			System.out.println("Token: "+n);
			nextSubNode = currentNode.getDirectSubNode(n);
			if (nextSubNode==null) {
				//This subNode doesn't already exist. So we create it:
				nextSubNode = new ContextNode(n);
				currentNode.addSubNode(nextSubNode);					
			}
			currentNode=nextSubNode;
		}
		return currentNode;
		
    }

	private void inSubscribe(PubSubBean bean) {    	
    	XMPPConnection c = this.mAgent.getConnection();
    	String from = bean.getFrom();
		String to = bean.getTo();
    	
    	String node = adjustNodeString(bean.subscribe.getNode());
    	String user = XMPPUtil.jidWithoutRessource(bean.subscribe.getJid());
    	
    	System.out.println("Subscribe Request received. User="+user+" Node="+node);    	
//    	System.out.println("PubSubBean: "+bean.toXML()); 
    	
    	PubSubBean beanAnswer = null;
		
		if (node==null || node.equals("") || node.equals("root")) {
			//Error: no correct node given
			beanAnswer = new PubSubBean("modify", "not-acceptable", "You have to provide a valid node.");
		} else if(!userContextMap.containsKey(user)) {
			//Error: user not found in userContextMap
			beanAnswer = new PubSubBean("modify", "not-acceptable", "This user is unknown.");
		} else if(!userContextMap.get(user).hasSubNode(node)) {
			//Error: node not found
			System.out.println("userContextMap.get("+user+").hasSubNode("+node+") = "+userContextMap.get(user).hasSubNode(node));
			beanAnswer = new PubSubBean("modify", "not-acceptable", "This node is unknown.");
		} else if(!publisherJIDs.containsKey(user)) {
			//Error: user is offline. Full-JID is unknown.		
			beanAnswer = new PubSubBean("wait", "not-authorized", "User is offline. Authorization not possible.");
		} 

		if (beanAnswer!=null) {
			beanAnswer.setTo(from); beanAnswer.setFrom(to);
			beanAnswer.setId(bean.getId());
			c.sendPacket(new BeanIQAdapter(beanAnswer));	
		} else {

			beanAnswer = new PubSubBean();
			beanAnswer.subscribe = new SubscribeItemInfo(node, user);
			beanAnswer.setTo(from); beanAnswer.setFrom(to);
			beanAnswer.setId(bean.getId());
			
			AuthorizationBean authBean = new AuthorizationBean(from, node);
			authBean.setFrom(getAgent().getFullJid());
			
			//Send the authorization request to the FullJID the last publishing came from:
			authBean.setTo(publisherJIDs.get(user)); 
				
			PendingSubscription ps = new PendingSubscription(node, user, from, beanAnswer);
			pendingSubscriptions.add(ps);
			
			c.sendPacket(new BeanIQAdapter(authBean));			
		}		
    	
	}
	
	
	// Authorization denied
	private void inAuthError(AuthorizationBean bean) {
		XMPPConnection c = this.mAgent.getConnection();
		
		PendingSubscription ps=null;
		for (PendingSubscription i : pendingSubscriptions) {
			if (i.equals(new PendingSubscription(bean.pathToElement, bean.getFrom(), bean.userJidToAuthorize, null))) {
				ps=i;
				break;
			}
		}		
				
		if (ps!=null) {
			PubSubBean beanAnswer = ps.beanAnswer;
			PubSubBean beanAnswerError = new PubSubBean("auth", "not-authorized", "Your subscription request was denied.");
			
			beanAnswerError.setFrom(beanAnswer.getFrom());
			beanAnswerError.setTo(beanAnswer.getTo());
			beanAnswerError.setId(beanAnswer.getId());
						
			c.sendPacket(new BeanIQAdapter(beanAnswerError));
			
			pendingSubscriptions.remove(ps);
		} else {
			MobilisManager.getLogger().warning("Received an error AuthorizationBean that was not requested.");
		}
		
		
	}

	// Authorization approved
	private void inAuthResult(AuthorizationBean bean) {
		XMPPConnection c = this.mAgent.getConnection();
		
		PendingSubscription ps=null;
		PendingSubscription a = new PendingSubscription(bean.pathToElement, XMPPUtil.jidWithoutRessource(bean.getFrom()), bean.userJidToAuthorize, null);
		for (PendingSubscription i : pendingSubscriptions) {
			if (i.equals(a)) {
				ps=i;
				break;
			}
		}		
		
		if (ps!=null) {
			PubSubBean beanAnswer = ps.beanAnswer;	
			c.sendPacket(new BeanIQAdapter(beanAnswer));
			
			String nodeOwnerBareJid = XMPPUtil.jidWithoutRessource(ps.nodeOwner);
			String node = ps.node;
			
			if (!subscriptions.containsKey(nodeOwnerBareJid)) {
				Map<String, List<String>> map = new HashMap<String, List<String>>();
				List<String> subscriberList = new ArrayList<String>();
				subscriberList.add(beanAnswer.getTo());
				map.put(node, subscriberList);
				subscriptions.put(nodeOwnerBareJid, map);
			} else {
				Map<String, List<String>> map = subscriptions.get(nodeOwnerBareJid);
				List<String> subscriberList = null;
				if (map.containsKey(node)) {
					subscriberList = map.get(node);
				} else {
					subscriberList = new ArrayList<String>();
					map.put(node, subscriberList);
				}
				subscriberList.add(beanAnswer.getTo());						
			}
			//TODO: Send the current status of the node to the new subscriber.
			informSubscriber(ps.nodeOwner, ps.node, beanAnswer.getTo());					
			pendingSubscriptions.remove(ps);
		} else {
			MobilisManager.getLogger().warning("Received a result AuthorizationBean that was not requested. PacketID: "+bean.getId());
		}
		
	}
	
	
	private void inUnsubscribe(PubSubBean bean) {		
		XMPPConnection c = this.mAgent.getConnection();
    	String from = bean.getFrom();
		String to = bean.getTo();
    	
    	String node = adjustNodeString(bean.unsubscribe.getNode());
    	String user = XMPPUtil.jidWithoutRessource(bean.unsubscribe.getJid());
    	
    	System.out.println("Unsubscribe Request received. User="+user+" Node="+node);    	
//    	System.out.println("PubSubBean: "+bean.toXML()); 
    	
    	PubSubBean beanAnswer;
		
		if (node==null || node.equals("") || node.equals("root")) {
			//Error: no correct node given
			beanAnswer = new PubSubBean("modify", "not-acceptable", "You have to provide a valid node.");
		} else if(!userContextMap.containsKey(user)) {
			//Error: user not found in userContextMap
			beanAnswer = new PubSubBean("modify", "not-acceptable", "This user is unknown.");
		} else if(!userContextMap.get(user).hasSubNode(node)) {
			//Error: node not found
			System.out.println("userContextMap.get("+user+").hasSubNode("+node+") = "+userContextMap.get(user).hasSubNode(node));
			beanAnswer = new PubSubBean("modify", "not-acceptable", "This node is unknown.");
		} else {
				
			beanAnswer = new PubSubBean("modify", "not-acceptable", "You are not subscribed to that node.");
				
			String nodeOwnerBareJid = user;
				
			if (subscriptions.containsKey(nodeOwnerBareJid)) {
				Map<String, List<String>> map = subscriptions.get(nodeOwnerBareJid);
				if (map.containsKey(node)) {					
					List<String> subscriberList = map.get(node);
					boolean changed = false;					
					while (subscriberList.remove(from)) {
						changed=true;
					}
					if (changed)
						beanAnswer=new PubSubBean();
						beanAnswer.unsubscribe = new UnsubscribeItemInfo(node, user);
						System.out.println("--> JID "+from+" successfully unsubscribed.");
				}				
			}		
		}
		
		beanAnswer.setTo(from); beanAnswer.setFrom(to);
		beanAnswer.setId(bean.getId());
		c.sendPacket(new BeanIQAdapter(beanAnswer));	
	}
	
}
