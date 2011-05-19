package de.tudresden.inf.rn.mobilis.server.services.context;

import de.tudresden.inf.rn.mobilis.xmpp.beans.context.PubSubBean;

public class PendingSubscription {

	public String node;
	public String nodeOwner;
	public String userSubscriber;
	public PubSubBean beanAnswer;
	
	public PendingSubscription(String node, String nodeOwner, String userSubscriber, PubSubBean beanAnswer) {
		this.node=node;
		this.nodeOwner=nodeOwner;
		this.userSubscriber=userSubscriber;
		this.beanAnswer=beanAnswer;
	}
	
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof PendingSubscription) {
			PendingSubscription ps = (PendingSubscription) o;
			if (ps.node.equals(this.node) && ps.nodeOwner.equals(this.nodeOwner) && ps.userSubscriber.equals(this.userSubscriber))
				return true;
		}		
		return false;		
	}
	
}
