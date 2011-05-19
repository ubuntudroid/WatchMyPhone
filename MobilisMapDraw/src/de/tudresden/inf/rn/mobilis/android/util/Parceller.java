/*******************************************************************************
 * Copyright (C) 2011 Technische Universität Dresden
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
package de.tudresden.inf.rn.mobilis.android.util;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPIQ;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

/**
 * 
 * @author Benjamin Söllner
 *
 */
public class Parceller {

	private static Parceller instance;
	private Map<String,Map<String,XMPPBean>> prototypes
			= Collections.synchronizedMap(new HashMap<String,Map<String,XMPPBean>>());
	
	public static Parceller getInstance() {
		if (Parceller.instance == null) 
			Parceller.instance = new Parceller();
		return Parceller.instance;
	}

	private Parceller() {}
	
	public void registerXMPPBean(XMPPBean prototype) {
		String namespace    = prototype.getNamespace();
		String childElement = prototype.getChildElement(); 
		synchronized (this.prototypes) {
			if (!this.prototypes.keySet().contains(namespace))
				this.prototypes.put(namespace, Collections.synchronizedMap( new HashMap<String,XMPPBean>() ));
			this.prototypes.get(namespace).put(childElement, prototype);
		}
	}
	
	public void unregisterXMPPBean(XMPPBean prototype) {
		String namespace    = prototype.getNamespace();
		String childElement = prototype.getChildElement(); 
		synchronized (this.prototypes) {
			if (this.prototypes.containsKey(namespace)) {
				this.prototypes.get(namespace).remove(childElement);
				if (this.prototypes.get(namespace).size() > 0)
					this.prototypes.remove(namespace);
			}
		}
	}

	public XMPPIQ convertXMPPBeanToIQ(XMPPBean bean, boolean mergePayload) {
		int type = XMPPIQ.TYPE_GET;
		switch (bean.getType()) {
			case XMPPBean.TYPE_GET:    type = XMPPIQ.TYPE_GET; break;
			case XMPPBean.TYPE_SET:    type = XMPPIQ.TYPE_SET; break;
			case XMPPBean.TYPE_RESULT: type = XMPPIQ.TYPE_RESULT; break;
			case XMPPBean.TYPE_ERROR:  type = XMPPIQ.TYPE_ERROR; break;
		}
		XMPPIQ iq;
		if (mergePayload)
			iq = new XMPPIQ( bean.getFrom(), bean.getTo(), type, null, null, bean.toXML() );
		else
			iq = new XMPPIQ( bean.getFrom(), bean.getTo(), type,
					bean.getChildElement(), bean.getNamespace(), bean.payloadToXML() );
		iq.packetID = bean.getId();
		return iq;
	}
	
	public XMPPBean convertXMPPIQToBean(XMPPIQ iq) {
		final Map<String,Map<String,XMPPBean>> prototypes = this.prototypes;
		try {
			String childElement = iq.element;
			String namespace    = iq.namespace;
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(iq.payload));
			XMPPBean bean = null;
			synchronized (prototypes) {
				if ( namespace != null && prototypes.containsKey(namespace)
						&& prototypes.get(namespace).containsKey(childElement) ) {
					bean = prototypes.get(namespace).get(childElement).clone();
					bean.fromXML(parser);
					bean.setId(iq.packetID);
					bean.setFrom(iq.from);
					bean.setTo(iq.to);
					switch (iq.type) {
						case XMPPIQ.TYPE_GET: bean.setType(XMPPBean.TYPE_GET); break;
						case XMPPIQ.TYPE_SET: bean.setType(XMPPBean.TYPE_SET); break;
						case XMPPIQ.TYPE_RESULT: bean.setType(XMPPBean.TYPE_RESULT); break;
						case XMPPIQ.TYPE_ERROR: bean.setType(XMPPBean.TYPE_ERROR); break;
					}
					return bean;
				}
			}
		} catch (Exception e) {}
		return null;
	}
	
//	public RepositoryItemParcel convertRepositoryItemInfoToParcel(RepositoryItemInfo info) {
//		RepositoryItemParcel parcel = new RepositoryItemParcel();
//		parcel.content = info.getContent();
//		parcel.owner   = info.getOwner();
//		parcel.uid     = info.getUid();
//		parcel.slices.clear();
//		parcel.slices.putAll(info.getSlices());
//		return parcel;
//	}
//	
//	public RepositoryItemInfo convertRepositoryItemParcelToInfo(RepositoryItemParcel parcel) {
//		RepositoryItemInfo info = new RepositoryItemInfo();
//		info.setContent(parcel.content);
//		info.setOwner(parcel.owner);
//		info.setUid(parcel.uid);
//		info.getSlices().clear();
//		info.getSlices().putAll(parcel.slices);
//		return info;
//	}
//	
//	public ConditionParcel convertConditionInfoToParcel(ConditionInfo info) {
//		ConditionParcel parcel = new ConditionParcel();
//		parcel.key        = info.getKey();
//		parcel.op         = info.getOp();
//		parcel.value      = info.getValue();
//		parcel.conditions = new ConditionParcel[info.getConditions().size()];
//		int i = 0; for (ConditionInfo subinfo: info.getConditions()) {
//			parcel.conditions[i] = this.convertConditionInfoToParcel(subinfo);
//			i++;
//		}
//		return parcel;
//	}
//	
//	public ConditionInfo convertConditionParcelToInfo(ConditionParcel parcel) {
//		ConditionInfo info = new ConditionInfo();
//		info.setKey(parcel.key);
//		info.setOp(parcel.op);
//		info.setValue(parcel.value);
//		info.getConditions().clear();
//		for (int i = 0; i < parcel.conditions.length; i++)
//			info.getConditions().add(this.convertConditionParcelToInfo(parcel.conditions[i]));
//		return info;
//	}

}
