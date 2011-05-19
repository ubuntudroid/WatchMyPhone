package de.tudresden.inf.rn.mobilis.xmpp.beans.context;

import org.xmlpull.v1.XmlPullParser;
import de.tudresden.inf.rn.mobilis.xmpp.beans.Mobilis;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

/**
 * Bean used to authorize another user to subscribe to user's context. 
 * @author Robert Lübke
 */
public class AuthorizationBean extends XMPPBean {

	private static final long serialVersionUID = 1L;
	public static final String NAMESPACE = Mobilis.NAMESPACE + "#services/UserContextService";
	public static final String CHILD_ELEMENT = "authorization";
	
	public String userJidToAuthorize, pathToElement;
	
	/** Constructor for authorization request from server to client (publisher); type=GET */
	public AuthorizationBean(String userJidToAuthorize, String pathToElement) {
		super();		
		this.userJidToAuthorize=userJidToAuthorize;
		this.pathToElement=pathToElement;
		this.type=XMPPBean.TYPE_GET;
	}		
	
	/** Constructor for type=ERROR */
	public AuthorizationBean(String errorType, String errorCondition, String errorText) {
		super(errorType, errorCondition, errorText);	
	}
	
	/** Constructor for empty bean and type=RESULT */
	public AuthorizationBean() {
		super();
		this.type=XMPPBean.TYPE_RESULT;
	}
	
	@Override
	public AuthorizationBean clone() {
		AuthorizationBean twin = new AuthorizationBean(userJidToAuthorize, pathToElement);		
		twin = (AuthorizationBean) cloneBasicAttributes(twin);			
		return twin;
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();
		
		if (this.userJidToAuthorize!=null)
			sb.append("<usertoauthorize>").append(userJidToAuthorize).append("</usertoauthorize>");
		if (this.pathToElement!=null)
			sb.append("<path>").append(pathToElement).append("</path>");
				
		sb = appendErrorPayload(sb);		
		return sb.toString();
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String childElement = AuthorizationBean.CHILD_ELEMENT;
		
		boolean done = false;
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
				} else if (tagName.equals("usertoauthorize")) {
					this.userJidToAuthorize = parser.nextText();
				} else if (tagName.equals("path")) {					
					this.pathToElement = parser.nextText();
				} else if (tagName.equals("error")) {
					parser = parseErrorAttributes(parser);
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
		return AuthorizationBean.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		return AuthorizationBean.NAMESPACE;
	}
		
}
