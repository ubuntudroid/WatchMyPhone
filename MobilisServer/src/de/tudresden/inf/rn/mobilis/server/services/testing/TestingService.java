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
package de.tudresden.inf.rn.mobilis.server.services.testing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.packet.Packet;
import de.tudresden.inf.rn.mobilis.server.agents.MobilisAgent;
import de.tudresden.inf.rn.mobilis.server.services.MobilisService;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.testing.MobilisPingBean;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanFilterAdapter;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanIQAdapter;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanProviderAdapter;

/**
 * A MobilisService for testing purposes.
 * @author Robert Lübke
 *
 */

public class TestingService extends MobilisService {

	private String uri;
	private String serviceVersion = "1.0";
	private DateFormat mDateFormatterDetailed, mDateFormatterNormal;
	private String logFileName;
	private List<String> usersToPing = new ArrayList<String>();
	private PingTimerTask pingTT = new PingTimerTask();
	private NoResponseTimerTask noResponseTT = new NoResponseTimerTask();
	private Map<XMPPBean, Long> unacknowledgedBeans = new HashMap<XMPPBean, Long>();
		
	public TestingService() {
		super();
		//FIXME:
		usersToPing.add("client1@mobilis.inf.tu-dresden.de/MXA");
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
		mDateFormatterDetailed = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
		mDateFormatterNormal = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		logFileName = mDateFormatterNormal.format(System.currentTimeMillis()) +
				"_" + this.getAgent().getResource() + ".csv";
		Timer t = new Timer();
		t.schedule(pingTT, 2*1000, 5*1000);
		t.schedule(noResponseTT, 5*1000, 3*1000);
	}
		
	@Override
	protected void registerPacketListener() {
//		XMPPBean pepPrototype = new PersonalEventingProtocolBean();		
		XMPPBean pingPrototype = new MobilisPingBean();
//		(new BeanProviderAdapter(pepPrototype)).addToProviderManager();
		(new BeanProviderAdapter(pingPrototype)).addToProviderManager();
		this.mAgent.getConnection().addPacketListener(this,
					new BeanFilterAdapter(pingPrototype)
				);
	}
	
    public void processPacket(Packet p) {
    	super.processPacket(p);
    	if (p instanceof BeanIQAdapter) {
    		XMPPBean b = ((BeanIQAdapter) p).getBean();
    		
//    		if (b instanceof PersonalEventingProtocolBean) {
//    			PersonalEventingProtocolBean bb = (PersonalEventingProtocolBean) b;    			
//    			if (b.getType() == XMPPBean.TYPE_SET)
//    				this.inPEPSet(bb);   				
//    		}  else 
    			if (b instanceof MobilisPingBean) {
    				MobilisPingBean bb = (MobilisPingBean) b;  
    			if (bb.getType()==XMPPBean.TYPE_GET)
    				this.inPingGet(bb);
    			else if (bb.getType()==XMPPBean.TYPE_RESULT)
    				this.inPingResult(bb);
    		}    		
    	}
    }

    /**
     * 
     * @param bean
     */
    private void inPingResult(MobilisPingBean bean) {    	    	
    	synchronized(unacknowledgedBeans) {
	    	for (XMPPBean b : unacknowledgedBeans.keySet()) {    	
	    		if (b.getId().equals(bean.getId())) {
	    			long pingResult = System.currentTimeMillis()-unacknowledgedBeans.get(b);
	    			logToFile("Ping Result = "+pingResult+";"+bean.getFrom()+";"+bean.getId());
	    			unacknowledgedBeans.remove(b);
	    			return;
	    		}
	    	}    	
	    	logToFile("ERROR: Pong with no Ping!;"+bean.getFrom()+";"+bean.getId());
    	}
	}

	/**
     * Responds to the incoming ping with a pong.
     * @param bean
     */
	private void inPingGet(MobilisPingBean bean) {	
		System.out.println("inPing from "+bean.getFrom()+". Sending Pong.");
		logToFile("inPing. Sending Pong.;"+bean.getFrom()+";"+bean.getId());
		XMPPConnection c = this.mAgent.getConnection();
    	String from = bean.getFrom();
		String to = bean.getTo();
		
		MobilisPingBean beanAnswer = new MobilisPingBean();
		beanAnswer.setType(XMPPBean.TYPE_RESULT);
		
		beanAnswer.setTo(from); beanAnswer.setFrom(to);
		beanAnswer.setId(bean.getId());		
		c.sendPacket(new BeanIQAdapter(beanAnswer));
		
	}
	
	/**
	 * Send a MobilisPing with type GET to an XMPP user.
	 * @param fullJID Full JID (XMPP-ID) of the user to send the ping to.
	 */
	private void outPingGet(String fullJID) {
		MobilisPingBean bean = new MobilisPingBean();
		bean.setType(XMPPBean.TYPE_GET);
		
		bean.setTo(fullJID);
		bean.setFrom(getAgent().getFullJid());
		
		logToFile("Sending Ping.;"+bean.getTo()+";"+bean.getId());
		synchronized(unacknowledgedBeans) {
			unacknowledgedBeans.put(bean, System.currentTimeMillis());
		}		
		this.mAgent.getConnection().sendPacket(new BeanIQAdapter(bean));
	}
    
	
	private void logToFile(String str){
		FileWriter fw;		
		File file = new File("log/"+logFileName);
	
		try {			
			if (!file.exists()) {
//				System.out.println("Log file does not exist. Creating it now...");
				file.createNewFile();
				System.out.println("New log file created: "+logFileName);
			} else {
//				System.out.println("Log file already exists.");
			}
				
			fw = new FileWriter(file, true);
			
			BufferedWriter bw = new BufferedWriter(fw); 
			
			bw.write(mDateFormatterDetailed.format(System.currentTimeMillis())+";"+str);
			bw.newLine();
			
			bw.close(); 
		} catch (IOException e) {
			System.err.println("ERROR while writing to logfile: " + file.getAbsolutePath());
			e.printStackTrace();
		} 
	}
	
	
	
    private class PingTimerTask extends TimerTask {
		@Override
		public void run() {			
			for (String user : usersToPing)
				outPingGet(user);
		}    	
    }
    
    private class NoResponseTimerTask extends TimerTask {
		@Override
		public void run() {	
			Set<XMPPBean> removeableBeans = new HashSet<XMPPBean>();
			synchronized(unacknowledgedBeans) {
				for (XMPPBean bean : unacknowledgedBeans.keySet()) {
					long elapsedTime = System.currentTimeMillis() - unacknowledgedBeans.get(bean);
					if (elapsedTime>15*1000) {
						logToFile("No Response to Ping Request. PACKET LOSS?!;"+bean.getFrom()+";"+bean.getId());
						removeableBeans.add(bean);
					}
				}			
				for (XMPPBean b : removeableBeans)
					unacknowledgedBeans.remove(b);
			}						
		}    	
    }

}
