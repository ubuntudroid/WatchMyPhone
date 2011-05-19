package de.tudresden.inf.rn.mobilis.server.services.locpairs;

import java.util.Timer;
import java.util.TimerTask;

import de.tudresden.inf.rn.mobilis.server.services.locpairs.BeanResenderTask;
import de.tudresden.inf.rn.mobilis.server.services.locpairs.Player;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanIQAdapter;

/**
 * The Class BeanResenderTask.
 * This class is used to periodically resent IQs and invokes 
 * the error handling when no acknowledgments are received.
 * 
 * @author Reik Mueller
 */
public class BeanResenderTask extends TimerTask {

	private XMPPBean bean = null;
	private Player player = null;
	private int repetition;
	private int maxRepetition = 5;
//	private boolean firstSent = false;
	private static Timer timer = new Timer();
	private static Long resendDelay = new Long("4000");


	private BeanResenderTask(XMPPBean bean, Player player, int repetition) {
		this.bean = bean;
		this.player = player;
		this.repetition = repetition;
//		this.firstSent = firstSend;

//		if(!firstSent)sendFirstBean();
	}

	/**
	 * Instantiates a new bean resender.
	 *
	 * @param bean the bean
	 * @param player the player
	 */
	public BeanResenderTask(XMPPBean bean, Player player) {
		this.bean = bean;
		this.player = player;
		this.repetition = 0;

		sendFirstBean();
	}
	
	private void sendFirstBean(){
//		System.out.println("ARG");
		player.getConnection().sendPacket(new BeanIQAdapter(bean));
		timer.schedule(new BeanResenderTask(bean, player, repetition + 1),
				resendDelay);
		
	}
	
	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		
		if (!player.isAcknowledged(bean)) {
			System.out.println("BRT " + bean.getId() + " von: " + player.getName() + " NS: " + bean.getNamespace() +  " rep: " + repetition);
//			System.out.println("UGH");
			if (repetition < maxRepetition) {
				player.getConnection().sendPacket(new BeanIQAdapter(bean));
				timer.schedule(new BeanResenderTask(bean, player, repetition + 1),
						resendDelay);
			} else {
				// invoke error handling
				player.beanSendingError(bean);
			}
		}
//		timer.purge();
		this.cancel();
	}	
}
