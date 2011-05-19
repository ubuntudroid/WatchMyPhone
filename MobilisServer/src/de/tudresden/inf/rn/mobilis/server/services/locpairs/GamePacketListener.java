package de.tudresden.inf.rn.mobilis.server.services.locpairs;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import de.tudresden.inf.rn.mobilis.server.services.locpairs.LocPairs;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.GameInformationBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.JoinGameBean;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanIQAdapter;

/**
 * The listener interface for receiving gamePacket events.
 * The class that is interested in processing a gamePacket
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addGamePacketListener<code> method. When
 * the gamePacket event occurs, that object's appropriate
 * method is invoked.
 *
 * @author Reik Mueller
 * @see GamePacketEvent
 */
public class GamePacketListener implements PacketListener{
	
	private LocPairs game = null;
	
	/**
	 * Instantiates a new game packet listener.
	 *
	 * @param game the game
	 */
	public GamePacketListener(LocPairs game){
		this.game = game;
//		System.out.println("GamePacketListener erstellt");
	}
	
	/* (non-Javadoc)
	 * @see org.jivesoftware.smack.PacketListener#processPacket(org.jivesoftware.smack.packet.Packet)
	 */
	@Override
	public void processPacket(Packet packet) {
//		System.out.println("GPL: Packet empfangen: " + packet.toString());
		if(packet instanceof BeanIQAdapter){
			XMPPBean b = ((BeanIQAdapter)packet).getBean();
			
			if(b instanceof JoinGameBean){
				JoinGameBean bean = (JoinGameBean)b;
				if(bean.getType() == XMPPBean.TYPE_SET){
//					System.out.println("GPL: JoinGameBean vom typ SET von " + bean.getFrom() + " empfangen");
					if (bean.getPlayerID() != null && bean.getPlayerName() != null){
						if(!game.createPlayer(bean.getPlayerID(), bean.getPlayerName())){
							bean.setTo(bean.getFrom());
							bean.setFrom(game.getGameId());
							bean.setType(XMPPBean.TYPE_ERROR);
							game.getConnection().getConnection().sendPacket(new BeanIQAdapter(bean));
						}
					}else{
						System.out.println("Anmeldeversuch ohne jid oder playername!");
						// TODO 
					}
				}
			} else if(b instanceof GameInformationBean){
				GameInformationBean bean = (GameInformationBean)b;
				if(bean.getType() == XMPPBean.TYPE_SET){
//					System.out.println("GameInformationBean vom typ SET von " + bean.getFrom() + " empfangen");
					if(!game.returnGameInfomation(bean.getFrom())){
						bean.setTo(bean.getFrom());
						bean.setFrom(game.getGameId());
						bean.setType(XMPPBean.TYPE_ERROR);
						game.getConnection().getConnection().sendPacket(new BeanIQAdapter(bean));
					}
				}
			}
		}
	}
}
