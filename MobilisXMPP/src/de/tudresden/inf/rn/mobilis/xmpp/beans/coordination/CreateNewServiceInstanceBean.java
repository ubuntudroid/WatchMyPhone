package de.tudresden.inf.rn.mobilis.xmpp.beans.coordination;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.Mobilis;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

/**
 * This bean is sent to create a new instance of
 * a service on the MobilisServer.
 * @author Robert Lübke
 */
public class CreateNewServiceInstanceBean extends XMPPBean {

	private static final long serialVersionUID = 1L;
	public static final String NAMESPACE = Mobilis.NAMESPACE + "#services/CoordinatorService";
	public static final String CHILD_ELEMENT = "createNewServiceInstance";
	
	public String serviceNamespace, servicePassword, serviceName;
	public String jidOfNewService;
	
	/** Constructor for creating a new Service Instance on the MobilisServer; type=SET */
	public CreateNewServiceInstanceBean(String serviceNamespace, String servicePassword) {
		super();
		this.serviceNamespace=serviceNamespace;
		this.servicePassword=servicePassword;
		this.type=XMPPBean.TYPE_SET;
	}
		
	/** Constructor for type=ERROR */
	public CreateNewServiceInstanceBean(String errorType, String errorCondition, String errorText) {
		super(errorType, errorCondition, errorText);	
	}
	
	/** Constructor for empty bean */
	public CreateNewServiceInstanceBean() {
		super();
		this.type=XMPPBean.TYPE_RESULT;
	}
	
	/** Constructor for type=RESULT */
	public CreateNewServiceInstanceBean(String jidOfNewService) {
		super();
		this.jidOfNewService=jidOfNewService;
		this.type=XMPPBean.TYPE_RESULT;
	}
	
	@Override
	public CreateNewServiceInstanceBean clone() {
		CreateNewServiceInstanceBean twin = new CreateNewServiceInstanceBean(serviceNamespace, servicePassword);		
		twin.jidOfNewService = this.jidOfNewService;	
		
		twin = (CreateNewServiceInstanceBean) cloneBasicAttributes(twin);
		return twin;
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();
		
		if (this.serviceNamespace!=null)
			sb.append("<serviceNamespace>").append(serviceNamespace).append("</serviceNamespace>");
		if (this.servicePassword!=null)
			sb.append("<servicePassword>").append(servicePassword).append("</servicePassword>");
		if (this.jidOfNewService!=null)
			sb.append("<jidOfNewService>").append(jidOfNewService).append("</jidOfNewService>");
		if (this.serviceName!=null)
			sb.append("<serviceName>").append(serviceName).append("</serviceName>");		
		
		sb = appendErrorPayload(sb);		
		return sb.toString();
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String childElement = CreateNewServiceInstanceBean.CHILD_ELEMENT;
		
		boolean done = false;
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();
				} else if (tagName.equals("serviceNamespace")) {
					this.serviceNamespace = parser.nextText();		
				} else if (tagName.equals("servicePassword")) {
					this.servicePassword = parser.nextText();	
				} else if (tagName.equals("jidOfNewService")) {
					this.jidOfNewService = parser.nextText();	
				} else if (tagName.equals("serviceName")) {
					this.serviceName = parser.nextText();	
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
		return CreateNewServiceInstanceBean.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		return CreateNewServiceInstanceBean.NAMESPACE;
	}

	public String getServiceNamespace() {
		return serviceNamespace;
	}

	public void setServiceNamespace(String serviceNamespace) {
		this.serviceNamespace = serviceNamespace;
	}

	public String getServicePassword() {
		return servicePassword;
	}

	public void setServicePassword(String servicePassword) {
		this.servicePassword = servicePassword;
	}

	public String getJidOfNewService() {
		return jidOfNewService;
	}

	public void setJidOfNewService(String jidOfNewService) {
		this.jidOfNewService = jidOfNewService;
	}

	/**
	 * @return the name of the service which should be created. 
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * @param serviceName the name of the service which should be created. This should be a descriptive text of this special service instance.
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	
}
