package de.tudresden.inf.rn.mobilis.xmpp.beans.testing;

import org.xmlpull.v1.XmlPullParser;
import de.tudresden.inf.rn.mobilis.xmpp.beans.Mobilis;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

/**
 * Bean used to ping other Mobilis entities, similar to XEP-0199.
 * @author Robert Lübke
 */
public class MobilisPingBean extends XMPPBean {

	private static final long serialVersionUID = 1L;
	public static final String NAMESPACE = Mobilis.NAMESPACE + "#services/TestingService";
	public static final String CHILD_ELEMENT = "mobilisping";
	
		
	/** Constructor for a ping; type=GET */
	public MobilisPingBean() {
		super();		
		this.type=XMPPBean.TYPE_GET;
	}		
	
	/** Constructor for type=ERROR */
	public MobilisPingBean(String errorType, String errorCondition, String errorText) {
		super(errorType, errorCondition, errorText);	
	}
	
	@Override
	public MobilisPingBean clone() {
		MobilisPingBean twin = new MobilisPingBean();
		twin = (MobilisPingBean) cloneBasicAttributes(twin);			
		return twin;
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();						
		sb = appendErrorPayload(sb);		
		return sb.toString();
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		String childElement = MobilisPingBean.CHILD_ELEMENT;
		
		boolean done = false;
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				if (tagName.equals(childElement)) {
					parser.next();				
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
		return MobilisPingBean.CHILD_ELEMENT;
	}

	@Override
	public String getNamespace() {
		return MobilisPingBean.NAMESPACE;
	}
		
}
