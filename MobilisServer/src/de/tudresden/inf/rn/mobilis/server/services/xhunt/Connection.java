package de.tudresden.inf.rn.mobilis.server.services.xhunt;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.muc.MultiUserChat;

import de.tudresden.inf.rn.mobilis.server.agents.MobilisAgent;
import de.tudresden.inf.rn.mobilis.server.services.xhunt.model.XHuntPlayer;
import de.tudresden.inf.rn.mobilis.server.services.xhunt.state.GameStateGameOver;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.AreasBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.CancelStartTimerBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.CreateGameBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.DepartureDataBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.GameDetailsBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.GameOverBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.JoinGameBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.LocationBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.OpenGamesBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.PlayerExitBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.PlayersBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.RoundStatusBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.SnapshotBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.StartRoundBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.TargetBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.UpdatePlayerBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.xhunt.UsedTicketsBean;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanIQAdapter;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanProviderAdapter;

public class Connection {
	
	private FileTransferManager mFileTransferManager;
	private MobilisAgent mMobilisAgent;
	private XHunt mController;
	
	private long mResultBeansTimeoutMillis = 15 * 1000;
	private Timer mDelayedResultBeansTimer;
	private int mLimitForDelayedPeriods = 3;
	
	// Timeout in seconds
	private int mFiletransferTimeout = 15;
	
	private boolean mIsFiletransferActive = false;
	
	private Map<String,Map<String,XMPPBean>> beanPrototypes
		= Collections.synchronizedMap(new HashMap<String,Map<String,XMPPBean>>());
	
	private ConcurrentHashMap<String, BeanTimePair> mWatingForResultBeans 
		= new ConcurrentHashMap<String, Connection.BeanTimePair>();
	
	private ArrayList<String> mUnavailablePlayers;
	
	
	public Connection(XHunt controller) {
		this.mMobilisAgent = controller.getAgent();
		this.mController = controller;
		this.mUnavailablePlayers = new ArrayList<String>();
		
		mFileTransferManager = new FileTransferManager(mMobilisAgent.getConnection());
		FileTransferNegotiator.setServiceEnabled(mMobilisAgent.getConnection(), true);
		
		registerXMPPExtensions();
	}
	
	public String beanToString(XMPPBean bean){
		String str = "XMPPBean: [NS="
			+ bean.getNamespace()
			+ " id=" + bean.getId()
			+ " from=" + bean.getFrom()
			+ " to=" + bean.getTo()
			+ " type=" + bean.getType()
			+ " payload=" + bean.payloadToXML();
		
		if(bean.errorCondition != null)
			str += " errorCondition=" + bean.errorCondition;
		if(bean.errorText != null)
			str += " errorText=" + bean.errorText;
		if(bean.errorType != null)
			str += " errorType=" + bean.errorType;
		
		str += "]";
		
		return str;
	}
	
	public void checkForDelayedResultBeans(){
		if(!mController.getActGame().isGameOpen())
			stopDelayedResultBeansTimer();
		
		long currentTime = System.currentTimeMillis();
		mController.log("check WaitingBeans; size: " + mWatingForResultBeans.size());
		
		if(mWatingForResultBeans.size() > 0){
			printWaitingBeanMap();
		}
		
		ArrayList<String> removeWaitingBeansIds = new ArrayList<String>(); 
		
		for(Map.Entry<String, BeanTimePair> entry : mWatingForResultBeans.entrySet()){
			// if player is unavailable or no more in game, do not send beans to him
			if(entry.getValue().DeleteFromWaitings
					|| mController.getActGame()
						.getPlayerByJid(entry.getValue().Bean.getTo()) == null){ 
				removeWaitingBeansIds.add(entry.getKey());
				continue;
			}
			
			if((entry.getValue().TimeStamp + mResultBeansTimeoutMillis) < currentTime){				
				if(entry.getValue().DelayedPeriods < mLimitForDelayedPeriods){
					if(!mIsFiletransferActive)
						entry.getValue().DelayedPeriods++;
					
					/*if(entry.getValue().Bean.getNamespace().equals(SnapshotBean.NAMESPACE))
						sendSnapshot(entry.getValue());
					else
						sendDelayedBean(entry.getValue());*/
					
					mController.log(entry.getValue().DelayedPeriods + ". delay of " + entry.getKey());
				}
				else{
					if(entry.getValue().Bean.getNamespace().equals(SnapshotBean.NAMESPACE)){
//						if(mUnavailablePlayers.contains(entry.getValue().Bean.getFrom()))
							handlePlayerUnavailable(entry.getValue().Bean.getTo());
						/*else{
							entry.getValue().DelayedPeriods++;
						}*/
					}
					else
						handlePlayerNotReplies(entry.getValue().Bean.getTo());
				}
			}
		}
		
		for(String waitingBeanId : removeWaitingBeansIds)
			mWatingForResultBeans.remove(waitingBeanId);
	}
	
	public MultiUserChat createMultiUserChat(String roomName){
		return new MultiUserChat(mMobilisAgent.getConnection(), roomName);
	}
	
	public FileTransferManager getFileTransferManager(){
		return this.mFileTransferManager;
	}
	
	public XMPPBean getRegisteredBeanByNamespace(String namespace){
		try{
			return this.beanPrototypes.get(namespace).values().iterator().hasNext()
				? this.beanPrototypes.get(namespace).values().iterator().next() : null;
		}
		catch(NullPointerException e){
			mController.log("ERROR: Cannot find namespace '" + namespace + "' in list of bean prototypes!");
			return null;
		}
	}
	
	public void handlePlayerNotReplies(String playerJid){
		mController.log("Buddy doesn't reply: " + playerJid);
		
		mUnavailablePlayers.add(playerJid);
		
		for(BeanTimePair pair : mWatingForResultBeans.values()){
			if(pair.Bean.getTo().equals(playerJid))
				pair.DeleteFromWaitings = true;
		}
		
		sendSnapshot(playerJid);		
	}
	
	private void handlePlayerUnavailable(String playerJid){
		mController.log("Buddy unavailable: " + playerJid);
		
		sendBean(new PlayerExitBean(playerJid));
		
		for(BeanTimePair pair : mWatingForResultBeans.values()){
			if(pair.Bean.getTo().equals(playerJid))
				pair.DeleteFromWaitings = true;
		}
		
		XHuntPlayer unavailablePlayer = mController.getActGame().getPlayerByJid(playerJid);
		
		if(unavailablePlayer.isMrx()){
			mController.getActGame().setGameIsOpen(false);
			mController.getActGame().removePlayerByJid(playerJid);
			
			// Switch to GameOver
			mController.getActGame().setGameState(new GameStateGameOver(mController, mController.getActGame()));
			mController.log("Status changed to GameStateGameOver");
			
			sendXMPPBean(
					new GameOverBean("MrX is no more available!"),
					mController.getActGame().getAgentsJids(),
					XMPPBean.TYPE_SET
			);
		}
		else{			
			mController.getActGame().removePlayerByJid(playerJid);
			
			mController.getActGame().getGameState().sendPlayersBean("Player " + unavailablePlayer.getName()
					+ " is no more available");
		}		

	}
	
	public boolean isConnected(){
		return mMobilisAgent.getConnection() != null
			? mMobilisAgent.getConnection().isConnected()
			: false;
	}
	
	public void printWaitingBeanMap(){
		mController.log("            WaitingBeans: " + mWatingForResultBeans.size());
		
		for(Map.Entry<String, BeanTimePair> entry : mWatingForResultBeans.entrySet()){
			mController.log("WaitingBean: [" 
					+ " id=" + entry.getKey()
					+ " timestamp=" + entry.getValue().TimeStamp
					+ " delayedPeriod=" + entry.getValue().DelayedPeriods
					+ " delete=" + entry.getValue().DeleteFromWaitings
					+ " " + beanToString(entry.getValue().Bean));
		}
	}
	
	private void registerXMPPExtensions(){		
		registerXMPPBean(new AreasBean());
		registerXMPPBean(new CancelStartTimerBean());
		registerXMPPBean(new CreateGameBean());
		registerXMPPBean(new DepartureDataBean());
		registerXMPPBean(new GameDetailsBean());
		registerXMPPBean(new GameOverBean());
		registerXMPPBean(new JoinGameBean());
		registerXMPPBean(new AreasBean());
		registerXMPPBean(new LocationBean());
		registerXMPPBean(new OpenGamesBean());
		registerXMPPBean(new OpenGamesBean());
		registerXMPPBean(new PlayerExitBean());
		registerXMPPBean(new PlayersBean());
		registerXMPPBean(new RoundStatusBean());
		registerXMPPBean(new SnapshotBean());
		registerXMPPBean(new StartRoundBean());
		registerXMPPBean(new TargetBean());
		registerXMPPBean(new UpdatePlayerBean());
		registerXMPPBean(new UsedTicketsBean());
	}
	
	private void registerXMPPBean(XMPPBean prototype) {
		
		(new BeanProviderAdapter(prototype)).addToProviderManager();
		
		synchronized (this.beanPrototypes) {
			if (!this.beanPrototypes.keySet().contains(prototype.getNamespace()))
				this.beanPrototypes.put(prototype.getNamespace(), 
						Collections.synchronizedMap( new HashMap<String,XMPPBean>() ));
			
			this.beanPrototypes.get(prototype.getNamespace())
				.put(prototype.getChildElement(), prototype);
		}
	}
	
	private void sendBean(XMPPBean bean){
		if(!mIsFiletransferActive
				&& mMobilisAgent != null
				&& mMobilisAgent.getConnection() != null
				&& mMobilisAgent.getConnection().isConnected())
			mMobilisAgent.getConnection().sendPacket(new BeanIQAdapter(bean));	
	}
	
	private void sendDelayedBean(BeanTimePair beanTimePair){
		beanTimePair.TimeStamp = System.currentTimeMillis();		
		
		mWatingForResultBeans.put(beanTimePair.Bean.getId(), beanTimePair);
		sendBean(beanTimePair.Bean.clone());			
	}
	
	private void sendSnapshot(String toJid){
		SnapshotBean snapshotBean = mController.getActGame().createSnapshotBean(toJid);
		snapshotBean.setTo(toJid);
		snapshotBean.setType(XMPPBean.TYPE_SET);
		
		mController.log("Snapshot: " + beanToString(snapshotBean));
		
		sendXMPPBean(snapshotBean);
	}
	
	private void sendSnapshot(BeanTimePair pair){
		SnapshotBean newSnapshotBean = mController.getActGame().createSnapshotBean(pair.Bean.getTo());
		newSnapshotBean.setTo(pair.Bean.getTo());
		newSnapshotBean.setType(pair.Bean.getType());
		newSnapshotBean.setId(pair.Bean.getId());
		
		pair.TimeStamp = System.currentTimeMillis();
		pair.Bean = newSnapshotBean.clone();
		mWatingForResultBeans.put(newSnapshotBean.getId(), pair);
		sendBean(newSnapshotBean);
	}
	
	public boolean sendXMPPBean(XMPPBean bean){
		bean.setFrom(mMobilisAgent.getFullJid());
		
		mController.log("sendIQ: " + beanToString(bean));
		
		//if player is not available, do not send any iq beside snapshot
		if(!mUnavailablePlayers.contains(bean.getTo())
				|| bean.getNamespace().equals(SnapshotBean.NAMESPACE)){
			// just wait for beans of type get or set
			if(bean.getType() == XMPPBean.TYPE_SET
					|| bean.getType() == XMPPBean.TYPE_GET){
				
				XMPPBean clone = bean.clone();
				mWatingForResultBeans.put(bean.getId(), new BeanTimePair(clone, System.currentTimeMillis()));
			}
			
			sendBean(bean);
			
			return true;
		}
		else
			return false;		
	}
	
	public boolean sendXMPPBean(XMPPBean bean, String to, int type){
		bean.setTo(to);
		bean.setType(type);
		
		return this.sendXMPPBean(bean);
	}
	
	public boolean sendXMPPBean(XMPPBean bean, Set<String> players, int type){
		
		bean.setType(type);
		int counter = 0;
		
		for(String playerJid : players){
			XMPPBean clone = bean.clone();
			clone.setTo(playerJid);
			clone.setId(bean.getId() + "_" + counter);
			
			this.sendXMPPBean(clone);
			counter++;
		}
		
		return true;
	}
	
	public boolean sendXMPPBeanError(XMPPBean resultBean, XMPPBean fromBean){
		resultBean.setTo(fromBean.getFrom());
		resultBean.setType(XMPPBean.TYPE_ERROR);
		resultBean.setId(fromBean.getId());
		
		return this.sendXMPPBean(resultBean);
	}
	
	public boolean sendXMPPBeanResult(XMPPBean resultBean, XMPPBean fromBean){
		resultBean.setTo(fromBean.getFrom());
		resultBean.setType(XMPPBean.TYPE_RESULT);
		resultBean.setId(fromBean.getId());
		
		return this.sendXMPPBean(resultBean);
	}
	
	public void startDelayedResultBeansTimer(){
		mDelayedResultBeansTimer = new Timer();
		mDelayedResultBeansTimer.schedule(
			new TimerTask() {
				public void run() {
					checkForDelayedResultBeans();
		        }
		}, mResultBeansTimeoutMillis, mResultBeansTimeoutMillis);
	}
	
	public void stopDelayedResultBeansTimer(){
		if(mDelayedResultBeansTimer != null)
			mDelayedResultBeansTimer.cancel();
	}
	
	public boolean transmitFile(File file, String fileDesc, String toJid){
		boolean transferSuccessful = false;
		mIsFiletransferActive = true;
		OutgoingFileTransfer transfer = mFileTransferManager.createOutgoingFileTransfer(toJid);
        
		if(file.exists()) {
			mController.log("Start transmitting file: " + file.getAbsolutePath()
					+ " to: " + toJid);
	        try {
	        	int counter = 0;
	        	transfer.sendFile(file, fileDesc);
	        	
	        	while(!transfer.isDone()) {
	        		if(counter == mFiletransferTimeout){
	        			mController.log("ERROR: Filetransfer canceled. No Response!");
	        			break;
	        		}
	        		counter++;
	        		
	        		try {
	        			Thread.sleep(1000);
	        		} catch (InterruptedException e1) {
	        			mController.log("ERROR: Thread interrupted while transmitting file: " + file.getName());
	        		}
	        	}
	        	
	        	transferSuccessful = transfer.isDone();
	        } catch (XMPPException e) {
	        	mController.log("FileTransfer throws XMPPException:");
	        	e.printStackTrace();
	        }
		}
		mIsFiletransferActive = false;
		mController.log("FileTransfer successful?: " + transferSuccessful);
		
		return transferSuccessful;
	}
	
	public boolean verifyIncomingBean(XMPPBean inBean){
		boolean isBeanAccepted = true;
		
		mController.log("incomingIQ: " + beanToString(inBean));
		
		if(inBean.getType() == XMPPBean.TYPE_RESULT){
			isBeanAccepted = (mWatingForResultBeans.remove(inBean.getId()) != null);
			
			if(mUnavailablePlayers.contains(inBean.getFrom())){
					//&& inBean.getNamespace() == SnapshotBean.NAMESPACE)
				mUnavailablePlayers.remove(inBean.getFrom());
				isBeanAccepted = true;
			}
		}
		
		return isBeanAccepted;
	}
	
	
	private class BeanTimePair {
		public XMPPBean Bean;
		public long TimeStamp;
		public int DelayedPeriods = 0;
		public boolean DeleteFromWaitings = false;
		
		public BeanTimePair(XMPPBean bean, long timeStamp){
			this.Bean = bean;
			this.TimeStamp = timeStamp;
		}
	}

}
