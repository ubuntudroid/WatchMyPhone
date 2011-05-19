package de.tudresden.inf.rn.mobilis.xmpp.beans.context;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupItemInfo;

/**
 * Based on XEP-0163: Personal Eventing Protocol
 * Can contain a PublishItemInfo or a SubscribeItemInfo.
 * @author Robert Lübke
 */
public class PubSubBean extends XMPPBean {

	private static final long serialVersionUID = 1L;
	public static final String NAMESPACE = "http://jabber.org/protocol/pubsub";
	public static final String CHILD_ELEMENT = "pubsub";
	
	public SubscribeItemInfo subscribe;
	public UnsubscribeItemInfo unsubscribe;
	public PublishItemInfo publish;
		
	/** Constructor for publishing; type=SET */
	public PubSubBean(PublishItemInfo publish) {
		super();
		this.publish=publish;
		this.type=XMPPBean.TYPE_SET;
	}
	
	/** Constructor for subscribing; type=SET */
	public PubSubBean(SubscribeItemInfo subscribe) {
		super();
		this.subscribe=subscribe;
		this.type=XMPPBean.TYPE_SET;
	}
	
	/** Constructor for subscribing; type=SET */
	public PubSubBean(UnsubscribeItemInfo unsubscribe) {
		super();
		this.unsubscribe=unsubscribe;
		this.type=XMPPBean.TYPE_SET;
	}
			
	/** Constructor for type=ERROR */
	public PubSubBean(String errorType, String errorCondition, String errorText) {
		super(errorType, errorCondition, errorText);	
	}
	
	/** Constructor for empty bean and type=RESULT */
	public PubSubBean() {
		super();
		this.type=XMPPBean.TYPE_RESULT;
	}
	
	@Override
	public PubSubBean clone() {
		PubSubBean twin = new PubSubBean();
		if (this.publish!=null)
			twin.publish = this.publish.clone();
		if (this.subscribe!=null)
			twin.subscribe = this.subscribe.clone();
		if (this.unsubscribe!=null)
			twin.unsubscribe = this.unsubscribe.clone();
		twin = (PubSubBean) cloneBasicAttributes(twin);			
		return twin;
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();
		
		if (this.subscribe!=null)
			sb.append(subscribe.toXML());
		if (this.unsubscribe!=null)
			sb.append(unsubscribe.toXML());
		if (this.publish!=null)
			sb.append(publish.toXML());		
		
		sb = appendErrorPayload(sb);		
		return sb.toString();
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String childElement = PubSubBean.CHILD_ELEMENT;
		
		boolean done = false;
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
				} else if (tagName.equals(PublishItemInfo.CHILD_ELEMENT)) {
					this.publish = new PublishItemInfo();
					this.publish.fromXML(parser);
				} else if (tagName.equals(SubscribeItemInfo.CHILD_ELEMENT)) {
					this.subscribe = new SubscribeItemInfo();
					this.subscribe.fromXML(parser);
				} else if (tagName.equals(UnsubscribeItemInfo.CHILD_ELEMENT)) {
					this.unsubscribe = new UnsubscribeItemInfo();
					this.unsubscribe.fromXML(parser);
				} else if (tagName.equals("error")) {
					try {
						parser = parseErrorAttributes(parser);
					} catch (XmlPullParserException e1) {
						e1.printStackTrace();
					} catch (IOException e2) {
						e2.printStackTrace();
					}
				} else
					parser.next();
				break;
			case XmlPullParser.END_TAG:
				if (parser.getName().equals(childElement))
					done = true;
				else
					parser.next();
				break;
			case XmlPullParser.END_DOCUMENT:
				done = true;
				break;
			default:
				parser.next();
			}
		} while (!done);
			
	}

	@Override
	public String getChildElement() {
		return PubSubBean.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		return PubSubBean.NAMESPACE;
	}
	
}
