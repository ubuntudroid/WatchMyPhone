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
package de.tudresden.inf.rn.mobilis.server.services;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.packet.Packet;

import de.tudresden.inf.rn.mobilis.server.MobilisManager;
import de.tudresden.inf.rn.mobilis.server.agents.MobilisAgent;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.coordination.CreateNewServiceInstanceBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.coordination.MobilisServiceDiscoveryBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.coordination.MobilisServiceInfo;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanFilterAdapter;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanIQAdapter;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanProviderAdapter;

/**
 * 
 * @author Robert Lübke
 *
 */
public class CoordinatorService extends MobilisService {

	private String serviceVersion = "1.0";
	private Map<String, Map<String, Object>> serviceSettings;
	/**
	 * List of all application-specific services, that the Coordinator created
	 * and now manages. 
	 */
	private List<AppSpecificService> appSpecificServicesList = null;
	/**
	 * Maintenance mode of the MobilisServer. In this mode, the Coordinator will not 
	 * respond to Service Discovery request and will not create new service instances.
	 */
	private boolean maintenanceMode = false;
	
	
	public CoordinatorService() {
		super();
		System.out.println("CoordinatorService created");
		serviceSettings = MobilisManager.getInstance().getSettings("services");		
		appSpecificServicesList=new ArrayList<AppSpecificService>();
		
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
	}
		
	@Override
	protected void registerPacketListener() {
		XMPPBean prototype1 = new CreateNewServiceInstanceBean();	
		XMPPBean prototype2 = new MobilisServiceDiscoveryBean();
		(new BeanProviderAdapter(prototype1)).addToProviderManager();
		(new BeanProviderAdapter(prototype2)).addToProviderManager();
		this.mAgent.getConnection().addPacketListener(this,
				new OrFilter(
					new BeanFilterAdapter(prototype1),
					new BeanFilterAdapter(prototype2)));
	}
	
    public void processPacket(Packet p) {
    	super.processPacket(p);
    	if (p instanceof BeanIQAdapter) {
    		XMPPBean b = ((BeanIQAdapter) p).getBean();
    		
    		if (b instanceof CreateNewServiceInstanceBean) {
    			CreateNewServiceInstanceBean bb = (CreateNewServiceInstanceBean) b;    			
    			if (b.getType() == XMPPBean.TYPE_SET)
    				this.inCreateServiceInstanceSet(bb);   				
    		} else if (b instanceof MobilisServiceDiscoveryBean) {
    			MobilisServiceDiscoveryBean bb = (MobilisServiceDiscoveryBean) b;    			
    			if (b.getType() == XMPPBean.TYPE_GET)
    				this.inMobilisServiceDiscoveryGet(bb); 
    		}
    	}
    }
    
    private void inMobilisServiceDiscoveryGet(MobilisServiceDiscoveryBean bean) {
    	XMPPConnection c = this.mAgent.getConnection();
    	String from = bean.getFrom();
		String to = bean.getTo();
			
		MobilisServiceDiscoveryBean beanAnswer=null;
		
		//TODO: Error Handling	
		if (maintenanceMode) {
			//The MobilisServer is currently in maintenance mode.
			beanAnswer = new MobilisServiceDiscoveryBean("wait", "unexpected-request", "The MobilisServer is currently in maintenance mode. Retry later.");			
		} else  if (bean.serviceNamespace==null && bean.serviceVersion==null) {
			//Empty request for all active services
			
			List<MobilisService> autostartServices = MobilisManager.getInstance().getAllServices();			
			beanAnswer = new MobilisServiceDiscoveryBean(null);			
			// Sort out all services with the wrong namespace and version
			for (MobilisService service : autostartServices) {
				if ( 	service.mAgent!=null
						&&
						(bean.serviceNamespace==null ||
						(bean.serviceNamespace!=null && bean.serviceNamespace.equals(service.getNamespace())))
						&&
						(bean.serviceVersion==null ||
						(bean.serviceVersion!=null && bean.serviceVersion.equals(service.getVersion())))	)
					beanAnswer.addDiscoveredService(service.getNamespace(), service.getVersion(), service.mAgent.getFullJid());
			}
						
			for (String ident : serviceSettings.keySet()) {
				Map<String, Object> settings = serviceSettings.get(ident);
				if (settings.containsKey("start") && settings.get("start").equals("ondemand")) {
					int count = 0;
					for (AppSpecificService ass : appSpecificServicesList)
						if (ident.equals(ass.getIdent()))
							count++;									
					beanAnswer.addDiscoveredService(MobilisManager.discoServicesNode+"/"+ident, count);
				}
			}
			
		} else {
			// Request for a special service with given Namespace or given Version
						
			List<MobilisService> allServices = MobilisManager.getInstance().getAllServices();
			allServices.addAll(appSpecificServicesList);						
			beanAnswer = new MobilisServiceDiscoveryBean(null);			
			// Sort out all services with the wrong namespace and version
			for (MobilisService service : allServices) {
				if ( 	(bean.serviceNamespace==null ||
						(bean.serviceNamespace!=null && bean.serviceNamespace.equals(service.getNamespace())))
						&&
						(bean.serviceVersion==null ||
						(bean.serviceVersion!=null && bean.serviceVersion.equals(service.getVersion())))	) {					
					MobilisServiceInfo serviceInfo = new MobilisServiceInfo();
					serviceInfo.setServiceNamespace(service.getNamespace());
					serviceInfo.setVersion(service.getVersion());
					serviceInfo.setJid(service.mAgent.getFullJid());
					if (service instanceof AppSpecificService)
						serviceInfo.setServiceName(((AppSpecificService) service).getServiceName());
					beanAnswer.addDiscoveredService(serviceInfo);
				}
			}		
		}		
		
		beanAnswer.setTo(from); beanAnswer.setFrom(to);
		beanAnswer.setId(bean.getId());
		
		c.sendPacket(new BeanIQAdapter(beanAnswer));
	}
    

	private void inCreateServiceInstanceSet(CreateNewServiceInstanceBean bean) {
    	XMPPConnection c = this.mAgent.getConnection();
    	String from = bean.getFrom();
		String to = bean.getTo();
			
		CreateNewServiceInstanceBean beanAnswer=null;
				
		if (maintenanceMode) {
			//The MobilisServer is currently in maintenance mode.
			beanAnswer = new CreateNewServiceInstanceBean("wait", "unexpected-request", "The MobilisServer is currently in maintenance mode. Retry later.");			
		} else if (bean.serviceNamespace==null) {
			//ServiceNamespace is null
			beanAnswer = new CreateNewServiceInstanceBean("modify", "not-acceptable", "The service namespace is not set.");
		} else {			
		String serviceName = bean.serviceNamespace;
		serviceName = serviceName.replaceFirst(MobilisManager.discoServicesNode+"/", "");	
		if (!serviceSettings.containsKey(serviceName)) {		
			//ServiceName is unknown
			beanAnswer = new CreateNewServiceInstanceBean("modify", "not-acceptable", "The given service namespace is unknown.");
		//TODO: Error Handling
//		} else if () {
//			
//		} else if() {
//			
		} else {
			//Create new instance of the service
			String className = MobilisManager.getInstance().getSettingString("services", serviceName, "type");
			
			AppSpecificService newService=null;									
			try {
				Constructor con = Class.forName(className).getConstructor(CoordinatorService.class, String.class, String.class);
				newService = (AppSpecificService) con.newInstance(new Object[]{this, bean.servicePassword, bean.serviceName});
			} catch(Exception e) {
				MobilisManager.getLogger().severe("Couldn't instantiate service: " + className + " because of " + e.getClass().getName() + ": " + e.getMessage());    
			}
									
			//Get an XMPP resource for the new agent, that is not already in use.
			String agentIdent = MobilisManager.getInstance().getSettingString("services", newService.getIdent(), "agent");
			int i=1;
			boolean alreadyTaken;
			do {
				alreadyTaken=false;
				for (AppSpecificService ass : appSpecificServicesList) {
					if (ass.mAgent.getResource().equalsIgnoreCase(agentIdent+i)) {
						alreadyTaken=true;
						i++;
						break;
					}						
				}					
			} while(alreadyTaken);
			
			String agentResource = agentIdent+i;
			
			//Create the new Agent with the generated XMPP resource.
			MobilisAgent agent = new MobilisAgent(agentIdent, true, agentResource);
			agent.registerService(newService);
				
			//Startup Agent and Service
			try {
				newService.mAgent = agent;
				agent.startup();
				appSpecificServicesList.add(newService);
				String jidOfNewService = newService.mAgent.getJid()+"/"+newService.mAgent.getResource();			
				beanAnswer = new CreateNewServiceInstanceBean(jidOfNewService);				
			} catch (Exception e) {
				e.printStackTrace();				
				try {
					//Try to Shutdown agent and service.
					newService.shutdown();
					agent.shutdown();
				} catch (Exception e1) { /*Do nothing if error occurs */ }				
				//Send Error IQ
				beanAnswer = new CreateNewServiceInstanceBean("wait", "internal-server-error", "Error starting up the new service.");
			}			
			
		}
		}
				
		beanAnswer.setTo(from); beanAnswer.setFrom(to);
		beanAnswer.setId(bean.getId());
		
		c.sendPacket(new BeanIQAdapter(beanAnswer));
	}
	
	/**
	 * Shuts down all managed application-specific services before it shuts
	 * down itself.
	 */
	@Override
	public void shutdown() throws Exception {
		for (AppSpecificService ass : appSpecificServicesList)
			if (ass!=null && ass.getAgent()!=null) ass.getAgent().shutdown();
		super.shutdown();
    }
	
	public boolean removeAppSpecificService(AppSpecificService service) {
		return appSpecificServicesList.remove(service);
	}
	
	/**
	 * @return true, if MobilisServer is in maintenance mode. false, otherwise.
	 */
	public boolean isInMaintenanceMode() {
		return maintenanceMode;
	}
	
	/**
	 * Set the MobilisServer into maintenance mode. In this mode, the Coordinator
	 * will not respond to Service Discovery request and will not create new service
	 * instances.
	 * @param maintenanceMode 
	 */
	public void setMaintenanceMode(boolean maintenanceMode) {
		this.maintenanceMode = maintenanceMode;
	}
	
		
	private String getVersionOfAppService(String ident) {
		for (AppSpecificService ass : appSpecificServicesList)		
			if (ass.getIdent().equals(ident))
				return ass.getVersion();		
		return null;
	}

}
