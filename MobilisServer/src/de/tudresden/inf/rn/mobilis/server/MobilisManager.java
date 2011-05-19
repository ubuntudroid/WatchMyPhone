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
package de.tudresden.inf.rn.mobilis.server;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.DiscoverItems.Item;

import de.tudresden.inf.rn.mobilis.server.agents.MobilisAgent;
import de.tudresden.inf.rn.mobilis.server.services.MobilisService;
import de.tudresden.inf.rn.mobilis.xmpp.beans.Mobilis;

public class MobilisManager {

	public final static String discoNamespace = Mobilis.NAMESPACE;	
	public final static String discoServicesNode = discoNamespace + "#services";
//	// all services
//	for (String serviceName : MobilisManager.getInstance().getServices().keySet()) {
//		MobilisService service = MobilisManager.getInstance().getService(serviceName);
//		Item item = new DiscoverItems.Item(service.getAgent().getConnection().getUser());
//		item.setName(service.getName());
//		item.setNode(service.getNode());
//		items.add(item);
//	}
	
	// the MobilisManager singleton instance
	private static MobilisManager instance;

	public static synchronized MobilisManager getInstance() {
		if (instance == null) {
			instance = new MobilisManager();
		}
		return instance;
	}

	public static Logger getLogger() {
		return Logger.getLogger("de.tudresden.inf.rn.mobilis");
	}
	
	public static Logger getHibernateLogger() {
		return Logger.getLogger("org.hibernate");
	}

	// container for configuration
	private final Map<String, Map<String, Map<String, Object>>> mConfiguration = 
		Collections.synchronizedMap(new HashMap<String, Map<String, Map<String, Object>>>());
	
	private final Map<String, MobilisAgent> mAgents = Collections.synchronizedMap(new HashMap<String, MobilisAgent>());

	private final Map<String, MobilisService> mServices = Collections.synchronizedMap(new HashMap<String, MobilisService>());

	private final Set<MobilisView> mServerViews = Collections.synchronizedSet(new HashSet<MobilisView>());

	private Boolean mStarted = false;

	private MobilisManager() {
		// setup customized logger for GUI and console output
		getLogger().setUseParentHandlers(false);
		getLogger().setLevel(Level.ALL);
		getLogger().addHandler(new Handler() {
			public void close() throws SecurityException {}
			public void flush() {}
			public void publish(LogRecord record) {
				synchronized(mServerViews) {
					for (MobilisView view : mServerViews) {
						try {
							view.showLogMessage(record.getLevel(), record.getMessage());
						} catch (Exception e) {
							// TODO Auto-generated catch block
						}
					}
				}
			}
		});
		getHibernateLogger().setUseParentHandlers(false);
		getHibernateLogger().setLevel(Level.INFO);
		getHibernateLogger().addHandler(new Handler() {
			public void close() throws SecurityException {}
			public void flush() {}
			public void publish(LogRecord record) {
				synchronized(mServerViews) {
					for (MobilisView view : mServerViews) {
						try {
							view.showLogMessage(record.getLevel(), "Hibernate: " + record.getMessage());
						} catch (Exception e) {
							// TODO Auto-generated catch block
						}
					}
				}
			}
		});
		
		//getLogger().addHandler(new ConsoleHandler());
		for (Handler handler : getLogger().getHandlers()) {
			try {
				handler.setLevel(Level.ALL);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
			}
		}
	}
	
	public MobilisAgent getAgent(String ident) {
		return getAgent(ident, "MobilisAgent");
	}
	
	public MobilisAgent getAgent(String ident, String classname) {
		MobilisAgent singleton = null;
		
		String packagename = getClass().getPackage().getName();
		if (!classname.startsWith(packagename)) {
			classname = packagename + ".agents." + classname;
		}

		synchronized(mAgents) {
			singleton = mAgents.get(ident);
		}

		if (singleton != null) {
			return singleton;
		}

		try {
			Class<?> agentClass = Class.forName(classname);
			Constructor<?> constructor = agentClass.getConstructor( new Class[]{ String.class } );
			singleton = (MobilisAgent) constructor.newInstance( new Object[]{ ident } );
		} catch (Exception e) {
			getLogger().severe("Couldn't instantiate agent: " + ident + " (" + classname + ")");
		}

		synchronized(mAgents) {
			mAgents.put(ident, singleton);
		}

		return singleton;
	}
	
	public List<Item> getNodeItems() {
    	List<Item> items = new LinkedList<Item>();
    	for (MobilisAgent agent: this.mAgents.values())
    		items.add( new Item(agent.getJid()) );
    	return items;
    }

	public MobilisService getService(String classname) {
		MobilisService singleton;

		String packagename = getClass().getPackage().getName();
		if (!classname.startsWith(packagename)) {
			classname = packagename + ".services." + classname;
		}

		synchronized(mServices) {
			singleton = mServices.get(classname);
		}

		if (singleton != null) {
			return singleton;
		}
		
		try {
			singleton = (MobilisService) Class.forName(classname).newInstance();
		} catch(Exception e) {
			getLogger().severe("Couldn't instantiate service: " + classname + " because of " + e.getClass().getName() + ": " + e.getMessage());    
		}

		synchronized(mServices) {
			if (singleton != null) mServices.put(classname, singleton);
		}

		return singleton;
	}
	
	/**
	 * @returna a list of all MobilisServices, that are registered at the MobilisManager
	 */
	public ArrayList<MobilisService> getAllServices() {
		return new ArrayList<MobilisService>(mServices.values());
	}

	// getter/setter related

	public boolean isStarted() {
		return mStarted;
	}

	public Map<String, Map<String, Object>> getSettings(String container) {
		synchronized(mConfiguration) {
			return mConfiguration.get(container);
		}
	}

	public Map<String, Object> getSettings(String container, String ident) {
		Map<String, Object> result = null;
		synchronized(mConfiguration) {
			if (mConfiguration.containsKey(container)) {
				result = mConfiguration.get(container).get(ident);
			}
		}
		return result;
	}

	public String getSettingString(String container, String ident, String key) {
		Object resultObject = null;
		synchronized(mConfiguration) {
			if (mConfiguration.containsKey(container)) {
				if (mConfiguration.get(container).containsKey(ident)) {
					resultObject = mConfiguration.get(container).get(ident).get(key);
				}
			}
		}
		return (resultObject instanceof String ? (String) resultObject : null);
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,String> getSettingStrings(String container, String ident, String key) {
		Object resultObject = null;
		synchronized(mConfiguration) {
			if (mConfiguration.containsKey(container)) {
				if (mConfiguration.get(container).containsKey(ident)) {
					resultObject = mConfiguration.get(container).get(ident).get(key);
				}
			}
		}
		return (resultObject instanceof Map<?,?> ? (Map<String,String>) resultObject : null);
	}

	public void registerServerView(MobilisView serverView) {
		synchronized(mServerViews) {
			mServerViews.add(serverView);
		}
	}

	public void unregisterServerView(MobilisView serverView) {
		synchronized(mServerViews) {
			mServerViews.remove(serverView);
		}
	}

	private void loadConfiguration() {
		try {
			String filename = "src/META-INF/MobilisSettings.xml";
			loadConfigurationFromFile(filename);
			getLogger().config("Mobilis Manager read settings file: " + filename);
		} catch (Exception e) {
			getLogger().severe("Mobilis Manager could not read settings file.");
		}
	}

	@SuppressWarnings("unchecked")
	private void loadConfigurationFromFile(String filename) throws Exception {
		Map<String, Map<String, Map<String, Object>>> settings = new HashMap<String, Map<String, Map<String, Object>>>();      
		Document doc = null;
		try {
			SAXBuilder b = new SAXBuilder(false);  // validierenden Parser nutzen
			File file = new File(filename);
			doc = b.build(file);
			Element root = doc.getRootElement();
			Map<? extends String, ? extends Map<String, Map<String, Object>>> mappedChildren = (Map<? extends String, ? extends Map<String, Map<String, Object>>>) getMappedChildren(root);
			settings.putAll(mappedChildren);
			
			getLogger().finest("Mobilis Manager settings: " + settings.toString());
		} catch (IOException e) {
			getLogger().severe("Mobilis Manager could not read settings file: " + e.getMessage());
			throw new Exception();
		} catch (JDOMException j) {    // nur eine Ausnahme fuer alle Situationen
			getLogger().severe("Mobilis Manager could not read settings.");
			throw new Exception();
		}

		synchronized(mConfiguration) {
			mConfiguration.clear();
			mConfiguration.putAll(settings);
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getMappedChildren(Element elem) {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> childResult = new HashMap<String, Object>();
		Iterator<Element> iter = elem.getChildren().iterator();
		String elemName = elem.getName();
		if (elemName.equals("setting")) {
			while (iter.hasNext()) {
				Element child = iter.next();
				result.put(child.getAttribute("name").getValue(), child.getContent(0).getValue());
			}
		} else if (elemName.equals("settings")) {
			while (iter.hasNext()) {
				Element child = iter.next();
				if (child.getChildren().size() > 0) {
					result.put(child.getAttribute("name").getValue(), getMappedChildren(child));
				} else 
					result.put(child.getAttribute("name").getValue(), child.getContent(0).getValue());
			}
		} else if (elemName.equals("service") || elemName.equals("network")
				|| elemName.equals("agent")) {
			while (iter.hasNext()) {
				Element child = iter.next();
				childResult.putAll(getMappedChildren(child));
			}
			if (elemName.equals("service")) {
				childResult.put("agent", elem.getAttribute("agent").getValue());				
			}
			if (elemName.equals("service") || elemName.equals("agent")) {
				childResult.put("start", elem.getAttribute("start").getValue());			
			}
			childResult.put("description", elem.getAttribute("description").getValue());
			childResult.put("type", elem.getAttribute("type").getValue());			
			result.put(elem.getAttribute("name").getValue(), childResult);
		} else if (elemName.equals("config")) {
			while (iter.hasNext()) {
				Element child = iter.next();
				result.putAll(getMappedChildren(child));
			}
		} else {
			while (iter.hasNext()) {
				Element child = iter.next();
				childResult.putAll(getMappedChildren(child));
			}
			result.put(elemName, childResult);
		}
		return result;
	}

	private void setupAgents() {	
		// bootstrap agent instances from configuration
		for (String agentName : getSettings("agents").keySet()) {
			String className = null;
			try {
				className = getSettingString("agents", agentName, "type");
				String startType = getSettingString("agents", agentName, "start");
				if (startType.equalsIgnoreCase("auto"))
					getAgent(agentName, className);
			} catch (Exception e) {
				getLogger().severe("Couldn't setup agent: " + agentName);
			}
		}
	}

	private void setupServices() {	
		// bootstrap service instances from configuration
		for (String serviceName : getSettings("services").keySet()) {
			String className = null;
			MobilisService service = null;
			try {
				className = getSettingString("services", serviceName, "type");
				String startType = getSettingString("services", serviceName, "start");
				if (startType.equalsIgnoreCase("auto"))
					service = getService(className);
			} catch (Exception e) {
				getLogger().severe("Couldn't setup service: " + serviceName);
			}
			
			// register service with agent
			try {
				String agentIdent = getSettingString("services", serviceName, "agent");
				String startType = getSettingString("services", serviceName, "start");
				if (startType.equalsIgnoreCase("auto"))
					getAgent(agentIdent).registerService(service);
			} catch (Exception e) {
				getLogger().severe("Couldn't register service: " + serviceName);
			}
		}
	}

	public void startup() {
		synchronized(mStarted) {
			if (!mStarted) {
				loadConfiguration();
				setupAgents();
				setupServices();
				synchronized(mAgents) {
					for (String key : mAgents.keySet()) {
						try {
							String startType = getSettingString("agents", mAgents.get(key).getIdent(), "start");
							if (startType.equalsIgnoreCase("auto"))
								mAgents.get(key).startup();
						} catch (XMPPException e) {
							getLogger().severe("Couldn't startup agent: " + key + " because " + e.getMessage());
						}
					}
				}
				mStarted = true;
				synchronized(mServerViews) {
					for (MobilisView view : mServerViews) {
						try {
							view.setStarted(mStarted);
						} catch (Exception e) {
							// TODO Auto-generated catch block
						}
					}
				}
				getLogger().info("Mobilis Manager started up.");
			} else {
				getLogger().info("Mobilis Manager is already up.");
			}
		}
	}

	public void shutdown() {
		synchronized(mStarted) {
			if (mStarted) {
				synchronized(mAgents) {
					for (String key : mAgents.keySet()) {
						try {
							mAgents.get(key).shutdown();
						} catch (XMPPException e) {
							getLogger().severe("Couldn't shutdown agent: " + key);
						}
					}
					mAgents.clear();
				}
				synchronized(mServices) {
					mServices.clear();
				}
				synchronized(mConfiguration) {
					mConfiguration.clear();
				}
				mStarted = false;
				synchronized(mServerViews) {
					for (MobilisView view : mServerViews) {
						try {
							view.setStarted(mStarted);
						} catch (Exception e) {
							// TODO Auto-generated catch block
						}
					}
				}
				getLogger().info("Mobilis Manager shut down.");
			} else {
				getLogger().info("Mobilis Manager is already down.");
			}
		}
	}
}
