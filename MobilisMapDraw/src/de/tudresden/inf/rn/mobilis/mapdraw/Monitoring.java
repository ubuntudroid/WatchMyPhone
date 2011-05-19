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
package de.tudresden.inf.rn.mobilis.mapdraw;

import jabberSrpc.JabberClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;

import de.tudresden.inf.rn.mobilis.android.services.SessionService;
import de.tudresden.inf.rn.mobilis.xmpp.packet.MonitoringIQ;

import android.content.Context;

public class Monitoring {

	private static Monitoring instance;
	private OutputStreamWriter out;
	private FileOutputStream fOut;
	private MonitoringIQHandler monHandler;
	private long startTime;
	private long timeSpan;
	private boolean stopPosted;
	public int valueCounter;
	private int valueCounterMax;
	private boolean fromRemote;
	
	private class MonitoringIQHandler implements PacketListener {
		
		private XMPPConnection xmpp;
		
		public MonitoringIQHandler() {
			xmpp = JabberClient.getInstance().getJabberConnection();
		}
		
		@Override
		public void processPacket(Packet p) {
			if (p instanceof MonitoringIQ) {
				MonitoringIQ monIQ = (MonitoringIQ) p;
				if (monIQ.getType()==IQ.Type.GET) {								
					String statusMsg = monIQ.getStatusMsg();
					if (statusMsg.equals(MonitoringIQ.PING)) {
						String from = monIQ.getFrom();
						monIQ.setType(IQ.Type.RESULT);
						monIQ.setFrom(monIQ.getTo());
						monIQ.setTo(from);
						xmpp.sendPacket(monIQ);
					} else if (statusMsg.equals(MonitoringIQ.START_TIMER)) {
						System.out.println("Start Remote IQ");
						startTimer();
						if (valueCounter == 0) {
							System.out.println("Init File");
							init("feedthrough.txt", null);
						}
					}
				}
			}
		}
	}
	
	private Monitoring() {
		monHandler = new MonitoringIQHandler();
		stopPosted = false;
		valueCounter = 0;
		valueCounterMax = 50;
		fromRemote = false;
	}
	
	public static Monitoring get() {
		if (instance == null) {
			instance = new Monitoring();
		}
		return instance;
	}
	
	public long ping(String toJID, String monitoringFileName) {
		XMPPConnection xmpp = JabberClient.getInstance().getJabberConnection();
		if (monitoringFileName != null) {
			init(monitoringFileName, SessionService.getInstance().getContext());
		}
		
		MonitoringIQ monIQ = new MonitoringIQ();
		monIQ.setStatusMsg(MonitoringIQ.PING);
		monIQ.setType(IQ.Type.GET);
		monIQ.setTo(toJID);
		PacketCollector collector = xmpp.createPacketCollector(
				new PacketIDFilter(monIQ.getPacketID()));

		long ping = 0;
		xmpp.sendPacket(monIQ);
		long t1 = System.currentTimeMillis();
		IQ result = (IQ)collector.nextResult(3000);
		long t2 = System.currentTimeMillis();
		if (result != null) {
			ping = t2 - t1;
			write(Long.toString(ping));
		} else {
			write("TIMEOUT");
		}
		newLine();
		return ping;
	}
	
	public MonitoringIQHandler getMonitoringIQHandler() {
		return monHandler;
	}
	
	public void init(String monitoringFileName, Context ctx) {
		fOut = null;
		out = null;
		stopPosted = false;
		try {
			//fOut = ctx.openFileOutput(Environment.getExternalStorageDirectory() + "/" + monitoringFileName, Context.MODE_APPEND);
			File dir = new File("/sdcard/AppData/MobilisMapDraw/");
			if (!dir.exists()) {
				dir.mkdirs();
			}			
			File file = new File("/sdcard/AppData/MobilisMapDraw/" + monitoringFileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			OutputStream fOut = new FileOutputStream(file); 
			out = new OutputStreamWriter(fOut);
		}
		catch (Exception e) {      
			e.printStackTrace();
		}
	}
	
	public void write(String text) {
		if (out != null) {
			try {
				out.write(text);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public long writeCurrentTime() {
		long sysTime = System.currentTimeMillis();
		write(Long.toString(sysTime));
		return sysTime;
	}
	
	public void newLine() {
		if (out != null) {
			try {
				out.write("\r\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void stop() {
		stopPosted = false;
		valueCounter = 0;
		fromRemote = false;
		try {
			out.flush();
			out.close();
			out = null;
			fOut = null;
		}
		catch (Exception e) {      
			e.printStackTrace();
		}
	}

	public void startTimer() {
		startTime = System.currentTimeMillis();
		stopPosted = false;
	}
	
	public long endTimer(boolean writeToCurrentFile) {
		timeSpan = System.currentTimeMillis() - startTime;
		valueCounter++;
		System.out.println("End Timer: " + timeSpan + " - valueCounter: " + valueCounter);
		startTime = 0;
		if (writeToCurrentFile) {
			try {
				System.out.println("Write to File");
				out.write(Long.toString(timeSpan));
				out.write("\r\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (fromRemote) fromRemote = false;
		if ((stopPosted) || (valueCounter == valueCounterMax)) {
			stop();
		}
		return timeSpan;
	}
	
	public boolean isTimerStarted() {
		return startTime != 0;
	}

	public void postStop() {
		if (startTime != 0) {
			stopPosted = true;
		} else {
			stop();
		}
	}

	public void setFromRemote(boolean b) {
		fromRemote = b;
	}
	
	public boolean isFromRemote() {
		return fromRemote;
	}
}
