/*******************************************************************************
 * Copyright (C) 2010 Ansgar Gerlicher
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
 * Stuttgart, Hochschule der Medien: http://www.mi.hdm-stuttgart.de/mmb/
 * Collaborative Editing Framework or XML:
 * http://sourceforge.net/projects/cefx/
 * 
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: http://mobilisplatform.sourceforge.net
 ******************************************************************************/
/**
 * This sourcecode is part of the Collaborative Editing Framework for XML (CEFX).
 * @author Michael Voigt
 */
package jabberSrpc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.provider.BytestreamsProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.IBBProviders;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.search.UserSearch;

public class JabberClient {

	private XMPPConnection jabberConnection;
	private Roster         roster;

	private String         userName;
	private String         password;
	private String         host;
	private String         resource;
	private String         resourceSuffix; //zB cefx-server oder cefx-client
	private int            port=5222;
	private String serviceName;

	private String         targetJID;

	private String         packetIDPrefix;
	private int            packetNumber;

	private boolean        connected;

	private static int              MAX_QUEUE_COUNT=10000;
	private static int              MAX_PACKET_SIZE=5000;
	private LinkedList<IQ>          responseQueue;
	private Hashtable<String,LinkedList<RPCPacket>> unfinishedPacketQueue;
	private PacketProcessor         packetProcessor;

	private Hashtable<String,StubData>  stubs;
	private static JabberClient     instance=null;

	private int           uniqueThreadID;

	private Hashtable<String,MucRoomData> mucRooms;
	
	private boolean usesProvidedConnection;
	private XMPPConnection providedXMPPConnection;
	
	private class MucRoomData {
		private String         mucRoomName;
		private MultiUserChat  mucRoom;
		private MucRoomListener mucRoomHandler;
	}

	public static JabberClient getInstance() {
		if (instance==null) {
			instance=new JabberClient();
		}
		return instance;
	}

	private JabberClient() {
		responseQueue=new LinkedList<IQ>();
		stubs=new Hashtable<String,StubData>();
		unfinishedPacketQueue=new Hashtable<String,LinkedList<RPCPacket>>();
		registerProvider();
		resource="";
		connected=false;
		generateUniqueResourceString();
		uniqueThreadID=0;
		usesProvidedConnection = false;

		mucRooms=new Hashtable<String,MucRoomData>();
	}

	public int getUniqueThreadID() {
		uniqueThreadID=uniqueThreadID+1;
		return uniqueThreadID;
	}

	private void registerProvider() {
        //RPCProvider p=new RPCProvider();
        //ProviderManager.getInstance().addIQProvider("query", "jabber:iq:rpc", p);
		ProviderManager pm = ProviderManager.getInstance();
		configureProviderManager(pm);
        packetIDPrefix=Integer.toHexString( ((int)(Math.random()*((float)0xFFFFFF))) )+"-";
        packetNumber=0;
	}

	private String uniquePacketID() {
		packetNumber=packetNumber+1;
		return packetIDPrefix+Integer.toString(packetNumber);
	}

	public boolean connect() {
		
		if (connected) return true;
		
		if (usesProvidedConnection) {
			
			jabberConnection = providedXMPPConnection;
			String fullUserName = jabberConnection.getUser();
			userName = fullUserName.substring(0, fullUserName.indexOf("@"));
			host = jabberConnection.getHost();
			port = jabberConnection.getPort();
			serviceName = jabberConnection.getServiceName();
			resource = fullUserName.substring(fullUserName.lastIndexOf("/")+1);
			resourceSuffix = "";
			
		} else {
			
			// Hack for ServiceDiscoveryManager: this class has a static initializer which
			// is not called in the right moment. That is why we repeat its code here.
			XMPPConnection.addConnectionCreationListener(new ConnectionCreationListener() {
				@Override
				public void connectionCreated(XMPPConnection connection) {
					new ServiceDiscoveryManager(connection);
				}
			});
			
			ConnectionConfiguration config = new ConnectionConfiguration(host, port);
			//config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
			jabberConnection = new XMPPConnection(config);
			try {
				jabberConnection.connect();
				jabberConnection.login(userName,password,resource+resourceSuffix);

			} catch (XMPPException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		roster=jabberConnection.getRoster();
		
		/*
		//set up xmpp connection
		ConnectionConfiguration config = new ConnectionConfiguration(
				host, port, serviceName);
		jabberConnection = new XMPPConnection(config);

		// connect and login to XMPP server
		try {
			jabberConnection.connect();
			//jabberConnection.login(userName, password);
			jabberConnection.login(userName, password, resource+resourceSuffix);
			roster=jabberConnection.getRoster();
			
		} catch (XMPPException e) {
			e.printStackTrace();
			return false;
		}
		*/

		System.out.println(getJID()+" connected");

        Presence presence = new Presence(Presence.Type.available);
        jabberConnection.sendPacket(presence);

	    packetProcessor=new PacketProcessor();
	    packetProcessor.start();

		PacketFilter filter = new PacketTypeFilter(RPCPacket.class);
		PacketListener listener = new PacketListener() {
	        public void processPacket(Packet packet) {
	        	if (packet==null) return;

	        	System.out.println("Received RPC IQ from: " + userName + ", at time: " + System.currentTimeMillis());

	        	XMPPError error = packet.getError();
	        	if (error != null) {
	        		System.out.println("Received error message: " +  error.toString());
	        		System.out.println(packet.toXML());
	        	}
	        	
	        	IQ iqp=(IQ)packet;
	        	processIQPacket(iqp);
	        }
	    };
	    jabberConnection.addPacketListener(listener, filter);

	    connected=true;
        return true;
	}

	/**
	 * WORKAROUND for Android only! The necessary configuration files for Smack
	 * library are not included in Android's apk-Package.
	 * 
	 * @param pm
	 *            A ProviderManager instance.
	 */
	private void configureProviderManager(ProviderManager pm) {

		// Private Data Storage
		pm.addIQProvider("query", "jabber:iq:private",
				new PrivateDataManager.PrivateDataIQProvider());

		// Time
		try {
			pm.addIQProvider("query", "jabber:iq:time", Class
					.forName("org.jivesoftware.smackx.packet.Time"));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		// Roster Exchange
		pm.addExtensionProvider("x", "jabber:x:roster",
				new RosterExchangeProvider());

		// Message Events
		pm.addExtensionProvider("x", "jabber:x:event",
				new MessageEventProvider());

		// Chat State
		pm.addExtensionProvider("active",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("composing",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("paused",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("inactive",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("gone",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		// XHTML
		pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",
				new XHTMLExtensionProvider());

		// Group Chat Invitations
		pm.addExtensionProvider("x", "jabber:x:conference",
				new GroupChatInvitation.Provider());

		// Service Discovery # Items
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
				new DiscoverItemsProvider());

		// Service Discovery # Info
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
				new DiscoverInfoProvider());

		// Data Forms
		pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());

		// MUC User
		pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user",
				new MUCUserProvider());

		// MUC Admin
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin",
				new MUCAdminProvider());

		// MUC Owner
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner",
				new MUCOwnerProvider());

		// Delayed Delivery
		pm.addExtensionProvider("x", "jabber:x:delay",
				new DelayInformationProvider());

		// Version
		try {
			pm.addIQProvider("query", "jabber:iq:version", Class
					.forName("org.jivesoftware.smackx.packet.Version"));
		} catch (ClassNotFoundException e) {
			// Not sure what's happening here.
		}

		// VCard
		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());

		// Offline Message Requests
		pm.addIQProvider("offline", "http://jabber.org/protocol/offline",
				new OfflineMessageRequest.Provider());

		// Offline Message Indicator
		pm.addExtensionProvider("offline",
				"http://jabber.org/protocol/offline",
				new OfflineMessageInfo.Provider());

		// Last Activity
		pm
				.addIQProvider("query", "jabber:iq:last",
						new LastActivity.Provider());

		// User Search
		pm
				.addIQProvider("query", "jabber:iq:search",
						new UserSearch.Provider());

		// SharedGroupsInfo
		pm.addIQProvider("sharedgroup",
				"http://www.jivesoftware.org/protocol/sharedgroup",
				new SharedGroupsInfo.Provider());

		// JEP-33: Extended Stanza Addressing
		pm.addExtensionProvider("addresses",
				"http://jabber.org/protocol/address",
				new MultipleAddressesProvider());

		// FileTransfer
		pm.addIQProvider("si", "http://jabber.org/protocol/si",
				new StreamInitiationProvider());
		pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams",
				new BytestreamsProvider());
		pm.addIQProvider("open", "http://jabber.org/protocol/ibb",
				new IBBProviders.Open());
		pm.addIQProvider("close", "http://jabber.org/protocol/ibb",
				new IBBProviders.Close());
		pm.addExtensionProvider("data", "http://jabber.org/protocol/ibb",
				new IBBProviders.Data());

		// Privacy
		pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
		
		// RPC
        //RPCProvider p=new RPCProvider();
        //ProviderManager.getInstance().addIQProvider("query", "jabber:iq:rpc", p);
		pm.addIQProvider("query", "jabber:iq:rpc", new RPCProvider());
	}
	
	public boolean isUserAvailable(String user) {
		return roster.getPresence(user).isAvailable();
	}

	public Vector<String> getRosterJIDs() {
		Vector<String> result=new Vector<String>();
		Collection<RosterEntry> c=roster.getEntries();

		for (RosterEntry e : c) {
			String user=e.getUser();
			Iterator<Presence> p=roster.getPresences(user);
			while (p.hasNext()) {
				Presence presence=p.next();
				if (presence.getType()==Presence.Type.available) {
					result.add(presence.getFrom());
				}
			}

		}

		return result;
	}

	public boolean isConnected() {
		return connected;
	}

	private synchronized void processIQPacket(IQ iqp) {
		
		if (iqp instanceof RPCPacket) {

			RPCPacket p=(RPCPacket)iqp;
			String id=iqp.getPacketID();
			String data=p.getStringData();
			LinkedList<RPCPacket> list;

			System.out.println("Processing RPC IQ - method name: " + p.getMethodName());

			if ((data.trim().endsWith("#")) || (data.length()==0)) {
				if (data.trim().endsWith("#")) {
					data=data.substring(0,data.length()-1);
					p.setStringData(data);
				}
				if (unfinishedPacketQueue.containsKey(id)) { //last part?

					list=unfinishedPacketQueue.get(id);
					list.addLast(p);
					int size=0;
					for ( Iterator<RPCPacket> i=list.iterator(); i.hasNext(); ) {
						RPCPacket tmp=i.next();
						size=size+tmp.getStringData().length();
					}
					StringBuffer buffer=new StringBuffer(size);
					for ( Iterator<RPCPacket> i=list.iterator(); i.hasNext(); ) {
						RPCPacket tmp=i.next();
						buffer.append(tmp.getStringData());
					}
					p.setStringData(buffer.toString());
					unfinishedPacketQueue.remove(id);
				}
				if (iqp.getType()==IQ.Type.SET) {
					packetProcessor.addRequest(p);
				}
				if (iqp.getType()==IQ.Type.RESULT) {
					if (responseQueue.size() == MAX_QUEUE_COUNT) {
						responseQueue.removeLast();
					}
					responseQueue.addFirst(p);
					this.notifyAll();
				}
			} else {
				if (unfinishedPacketQueue.containsKey(id)) { //first part?
					list=unfinishedPacketQueue.get(id);
					list.addLast(p);
				} else {
					list=new LinkedList<RPCPacket>();
					list.addLast(p);
					unfinishedPacketQueue.put(id, list);
				}

			}
		}
	}

	public String getJID() {
		String userid=userName+"@"+serviceName;
		if (resource == null) resource = "";
		if (resourceSuffix == null) resourceSuffix = "";
		if ((resource.length()>0) || (resourceSuffix.length()>0)) {
			userid=userid+"/"+resource+resourceSuffix;
		}
		return userid;
	}

	public byte[] Object2ByteArray(Object o) {
		if (o==null) {
			byte[] dummy=new byte[0];
			return dummy;
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			new ObjectOutputStream(baos).writeObject(o);
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] data=baos.toByteArray();
		return data;
	}

	private RPCPacket createRequestPacket(String method,String data,String target,String id) {
		RPCPacket packet=new RPCPacket();
		packet.setMethodName(method);
		packet.setStringData(data);
		packet.createRequest(getJID(),target,id);
		return packet;
	}

	private String sendPackets(String method,Object o,String target,RPCPacket packet) {
		String packetID=uniquePacketID();

		if (o!=null) {
			String data=Base64.encode(Object2ByteArray(o))+'#';
//System.out.println("sendPackets - sendPacket SIZE: "+data.length()+" (from: "+userName+")");
			int offset=0;
			while (offset<data.length()) {
				RPCPacket sendPacket;

				int r=data.length()-offset;
				int l=MAX_PACKET_SIZE;
				if (r<MAX_PACKET_SIZE) {
					l=r;
				}
				String tmp=data.substring(offset, offset+l);
				offset=offset+l;
	//System.out.println("sendPackets - sendPacket: "+tmp.length()+" ("+userName+")");

				if (packet==null) {
					sendPacket=createRequestPacket(method,tmp,target,packetID);
				} else {
					sendPacket=packet.createResponseFromRequest(tmp);
				}
				//System.out.println("Packet to send:" + sendPacket.toXML());
				jabberConnection.sendPacket(sendPacket);
			}
		} else {
//System.out.println("sendPackets - sendPacket SIZE: 0 (from: "+userName+")");
			RPCPacket sendPacket;
			if (packet==null) {
				sendPacket=createRequestPacket(method,"",target,packetID);
			} else {
				sendPacket=packet.createResponseFromRequest("");
			}
			//System.out.println("Packet to send:" + sendPacket.toXML());
			jabberConnection.sendPacket(sendPacket);
		}
		return packetID;
	}

	public synchronized Object callB(String method,Object o,String target,long timeOut) {
		String packetID=sendPackets(method,o,target,null);

		IQ        iqp=null;
		Object    resultObject=null;

		long startTime=System.currentTimeMillis();
		long waitTime=timeOut;
		do {
			iqp=getNextResponsePacket(target,packetID);
			if (iqp!=null) { break; }
			if (waitTime<=0) { break; }
			try {
				wait(waitTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			long tmp=System.currentTimeMillis();
			waitTime=waitTime-(tmp-startTime);
			startTime=tmp;
		} while (true);

		if (iqp!=null) {
			resultObject=((RPCPacket)iqp).getObject();
		}

		return resultObject;
	}

	private synchronized IQ getNextResponsePacket(String from, String id) {
		if (responseQueue.size()==0) return null;

		IQ packet=null;

		for ( Iterator<IQ> i=responseQueue.iterator(); i.hasNext(); ) {
			packet=i.next();
			if (!packet.getPacketID().equals(id)) {
				packet=null;
				continue;
			}
			if (!packet.getFrom().equals(from)) {
				packet=null;
				continue;
			}
			i.remove();
			break;
		}

		return packet;
	}

	public synchronized void callNB(String method,Object o,String target) {
		sendPackets(method,o,target,null);
	}

	public void disconnect() {
		jabberConnection.disconnect();
	}

	public void generateUniqueResourceString() {
		resource=Integer.toHexString( ((int)(Math.random()*((float)0xFFFF))) )+"-";
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getTargetJID() {
		return targetJID;
	}

	public void setTargetJID(String targetJID) {
		this.targetJID = targetJID;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getResourceSuffix() {
		return resourceSuffix;
	}

	public void setResourceSuffix(String resourceSuffix) {
		this.resourceSuffix = resourceSuffix;
	}


	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	
	public String getServiceName() {
		return serviceName;
	}
	
	//name = "Class.Method"
	public boolean registerMethod(String className,String methodName,Stub stub,String threadID) {
		String comb=className+"."+methodName;
		if (threadID!=null) {
			comb=comb+'@'+threadID;
		}
		if (stubs.containsKey(comb)) return false;
		boolean type=false;

		java.lang.reflect.Method method;
		try {
			method = stub.getClass().getMethod(methodName, new Class[]{Object.class});
			if (method.getReturnType()==void.class) {
				type=true;
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		stubs.put(comb, new StubData(methodName,stub,type));
		return true;
	}

	class PacketProcessor extends Thread {

		private LinkedList<IQ> requestQueue;

		public PacketProcessor() {
			requestQueue=new LinkedList<IQ>();
		}

		public synchronized void addRequest(IQ request) {
System.out.println("##################### PacketProcessor add: "+((RPCPacket)request).getMethodName()+"   "+userName);
			if (requestQueue.size() == MAX_QUEUE_COUNT) {
            	requestQueue.removeLast();
            }
            requestQueue.addFirst(request);
            this.notifyAll();
		}

		private synchronized IQ getNext() {
System.out.println("##################### PacketProcessor getNext "+userName);
			return pollFirstFromRequestQueue();
		}
		
		private IQ pollFirstFromRequestQueue() {
			if (requestQueue.size()==0) return null;
			return requestQueue.removeFirst();
		}

		private Object callMethod(RPCPacket request) {
			String name=request.getMethodName(); //name = "Class.Method"
			String methodName=((StubData)stubs.get(name)).getMethod();
			Stub   stub=((StubData)stubs.get(name)).getStub();

			try {
				java.lang.reflect.Method method=stub.getClass().getMethod(methodName, new Class[]{Object.class});
				return method.invoke(stub, request.getObject());
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			return null;
		}

		private void callVoidMethod(RPCPacket request) {
			String name=request.getMethodName(); //name = "Class.Method"
			String methodName=((StubData)stubs.get(name)).getMethod();
			Stub   stub=((StubData)stubs.get(name)).getStub();

			try {
				java.lang.reflect.Method method=stub.getClass().getMethod(methodName, new Class[]{Object.class});
				method.invoke(stub, request.getObject());
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}


		public synchronized void run() {
			while (true) {
				if (requestQueue.size()==0) {
					try {
						wait();
						System.out.println("Jabber PacketProcessor continues ...");
						//sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				while (requestQueue.size()>0) {
					RPCPacket request=(RPCPacket)getNext();
System.out.println("##################### PacketProcessor run "+request.getMethodName()+"   "+userName);
					if (!stubs.containsKey(request.getMethodName())) continue;

					if (stubs.get(request.getMethodName()).isReturnTypeVoid()==true) {
						callVoidMethod(request);
					} else {
						Object o=callMethod(request);
						sendPackets("",o,"",request);
					}
				}

			}
		}

	}

	class StubData {
		private String method;
		private Stub   stub;
		private boolean returnTypeVoid;

		public StubData(String m, Stub s,boolean returnTypeVoid) {
			method=m;
			stub=s;
			this.returnTypeVoid=returnTypeVoid;
		}

		String  getMethod() { return method; }
		Stub    getStub()   { return stub; }
		boolean isReturnTypeVoid() { return returnTypeVoid; }
	}

	public String genUniqueMucRoomName(String add) {
		String mucRoomName="";
		mucRoomName=Integer.toHexString( ((int)(Math.random()*((float)0xFFFFFF))) );
		if (add!=null) {
			mucRoomName=mucRoomName+add;
		}
		mucRoomName=mucRoomName+"@conference."+host;
		return mucRoomName;
//TODO check if name is unique
	}

	private void addMucRoomListener(MucRoomData data) {
	    MucRoomMessageListener l=new MucRoomMessageListener();
	    l.setHandler(data.mucRoomHandler);
	    data.mucRoom.addMessageListener(l);

	    MucRoomPresenceListener lp=new MucRoomPresenceListener();
	    lp.setHandler(data.mucRoomHandler);
	    data.mucRoom.addParticipantListener(lp);
	}

	public boolean createMucRoom(String name,MucRoomListener handler) {
		MultiUserChat m=new MultiUserChat(jabberConnection,name);
		try {
			m.create(userName);
			m.sendConfigurationForm(new Form(Form.TYPE_SUBMIT));

			MucRoomData data=new MucRoomData();
			data.mucRoom=m;
			data.mucRoomName=name;
			data.mucRoomHandler=handler;
			mucRooms.put(name, data);
			addMucRoomListener(data);
			return true;
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean joinMucRoom(String MUCUserName,String roomName,MucRoomListener handler) {
		if ((roomName==null) || ("".equals(roomName))) return false;
		MucRoomData data=new MucRoomData();
		data.mucRoomHandler=handler;
		data.mucRoomName=roomName;
		data.mucRoom=new MultiUserChat(jabberConnection,roomName);
		try {
			data.mucRoom.join(MUCUserName);
			addMucRoomListener(data);
			mucRooms.put(roomName, data);
		} catch (XMPPException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean isMucRoomJoined(String name) {
		if (!mucRooms.containsKey(name)) return false;
		MultiUserChat m=mucRooms.get(name).mucRoom;
		if (m==null) return false;
		return m.isJoined();
	}

	public void sendMucRoomMessage(String name,Message msg) {
		if (!mucRooms.containsKey(name)) return;
		MultiUserChat m=mucRooms.get(name).mucRoom;
		if (m==null) return;
		try {
//			System.out.println(msg.toXML());
			System.out.println("Sending MUC message " + msg.toXML());
			m.sendMessage(msg);
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}

	public void sendPacket(Packet packet) {
		jabberConnection.sendPacket(packet);
	}
	
	public void deleteMucRoom(String name) {
		if (!mucRooms.containsKey(name)) return;
		MultiUserChat m=mucRooms.get(name).mucRoom;
		if (m==null) return;
		try {
			m.destroy("", null);
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}

	public void leaveMucRoom(String name) {
		if (!mucRooms.containsKey(name)) return;
		MultiUserChat m=mucRooms.get(name).mucRoom;
		if (m==null) return;
		m.leave();
	}

	public void addExtensionProvider(String element, String namespace, Object provider) {
		ProviderManager.getInstance().addExtensionProvider(element, namespace, provider);
	}

	public boolean usesProvidedConnection() {
		return usesProvidedConnection;
	}

	public void setUsesProvidedConnection(boolean usesProvidedConnection) {
		this.usesProvidedConnection = usesProvidedConnection;
	}

	public void setProvidedXMPPConnection(XMPPConnection providedXMPPConnection) {
		this.providedXMPPConnection = providedXMPPConnection;
	}

	public XMPPConnection getJabberConnection() {
		return jabberConnection;
	}
}

