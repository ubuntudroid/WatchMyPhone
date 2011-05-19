package de.tudresden.inf.rn.mobilis.server.services.locpairs;

import java.text.ParseException;
import java.util.Date;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import de.tudresden.inf.rn.mobilis.server.services.locpairs.Player;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.EndGameBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.GoThereBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.JoinGameBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.KeepAliveBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.PlayerUpdateBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.QuitBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.StartGameBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.StartRoundBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.UncoverCardBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.model.LocPairsDateFormat;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanIQAdapter;

/**
 * The Class PlayerPacketListener.
 * This class processes all packets an invokes the corresponding methods in
 * the Player class.
 * 
 * @author Reik Mueller
 *
 * @see PlayerPacketEvent
 */
public class PlayerPacketListener implements PacketListener {

	private Player player = null;

	/**
	 * Instantiates a new player packet listener.
	 * 
	 * @param player
	 *            the player
	 */
	public PlayerPacketListener(Player player) {
		this.player = player;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jivesoftware.smack.PacketListener#processPacket(org.jivesoftware.
	 * smack.packet.Packet)
	 */
	@Override
	public void processPacket(Packet packet) {
		// System.out.println("PlayerPacketListener Packet empfangen");
		if (!player.isStopped()) {

			if (packet instanceof BeanIQAdapter) {
				XMPPBean b = ((BeanIQAdapter) packet).getBean();

				if (b.getType() == XMPPBean.TYPE_RESULT) {
					player.acknowledgeBean(b);
				}
				player.keepAlive();
				// System.out.println("PPL: PlayerPacketListener Art: " +
				// b.getNamespace() + " Type: " + b.getType());

				if (b instanceof StartGameBean) {
					// System.out.println("PPL: StartGame Type: " +
					StartGameBean bean = (StartGameBean) b;
					if (bean.getType() == XMPPBean.TYPE_SET) {
						 System.out.println("PPL: StartGameBean vom typ SET empfangen von: "
						 + bean.getFrom());
					} else if (bean.getType() == XMPPBean.TYPE_RESULT) {
						// System.out
						// .println("PPL: StartGameBean vom typ RESULT empfangen von: "
						// + bean.getFrom());
						player.startGame();
						player.acknowledgeBean(bean);
					} else if (bean.getType() == XMPPBean.TYPE_ERROR) {
						System.out
								.println("PPL: StartGameBean vom typ ERROR empfangen");
					}

				} else if (b instanceof QuitBean) {
					// System.out.println("PPL: QuitGame Type: " + b.getType());
					QuitBean bean = (QuitBean) b;
					if (bean.getType() == XMPPBean.TYPE_SET) {
						System.out
								.println("PPL: QuitBean vom typ SET empfangen von: "
										+ bean.getFrom());
						if (this.player.quitGame()) {
							System.out.println("Quitbean result geschickt");
							this.player.sendAcknowledgement(bean);
						} else {
							this.player.sendError(bean, "");
						}
					} else if (bean.getType() == XMPPBean.TYPE_RESULT) {
						player.acknowledgeBean(bean);
						System.out
								.println("PPL: QuitBean vom typ RESULT empfangen");
					} else if (bean.getType() == XMPPBean.TYPE_ERROR) {
						player.acknowledgeBean(bean);
					}

				} else if (b instanceof GoThereBean) {
					// System.out.println("PPL: GoThere Type: " + b.getType());
					GoThereBean bean = (GoThereBean) b;
					if (bean.getType() == XMPPBean.TYPE_SET) {
						player.getTeamMate().goThere(bean);
					} else if (bean.getType() == XMPPBean.TYPE_RESULT) {
						player.acknowledgeBean(bean);
						player.getTeamMate().sendAcknowledgement(bean);
					} else if (bean.getType() == XMPPBean.TYPE_ERROR) {
						player.getTeamMate().sendError(bean,
								"konnte nicht verarbeitet werden");	
						player.acknowledgeBean(bean);
					}

				} else if (b instanceof StartRoundBean) {
					// System.out.println("PPL: StartRound Type: " +
					// b.getType());
					StartRoundBean bean = (StartRoundBean) b;
					if (bean.getType() == XMPPBean.TYPE_RESULT) {
						// System.out
						// .println("PPL: StartRoundBean vom typ RESULT empfangen von :"
						// + bean.getFrom());
						player.acknowledgeBean(bean);
					} else if (bean.getType() == XMPPBean.TYPE_ERROR) {
						System.out
								.println("PPL: StartRoundBean vom typ ERROR empfangen");
						player.acknowledgeBean(bean);
					}

				} else if (b instanceof UncoverCardBean) {
					// System.out.println("PPL: UncoverCard Type: " +
					// b.getType());
					UncoverCardBean bean = (UncoverCardBean) b;
					if (bean.getType() == XMPPBean.TYPE_SET) {
		//				System.out
		//						.println("PPL: UncoverCardBean vom typ SET empfangen von: "
		//								+ bean.getFrom());
						try {
							Date timeStamp = LocPairsDateFormat.getFormat()
									.parse(bean.getTimeStamp());
							// execute player.uncoverCard() if successful send
							// acknowledgment if not send errorIQ
							if (player.uncoverCard(bean.getBarCodeId(), bean
									.getNetworkFingerPrint(), timeStamp)) {
								player.sendAcknowledgement(bean);
							} else {
								player.sendError(bean, "");
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}

				} else if (b instanceof KeepAliveBean) {
					// System.out.println("PPL: KeepAlive Type: " +
					// b.getType());
					KeepAliveBean bean = (KeepAliveBean) b;
					if (bean.getType() == XMPPBean.TYPE_SET) {
						// System.out.println("PPL: KeepAliveBean vom typ SET empfangen "
						// + bean.getFrom());
						if (player.keepAlive())
							player.sendAcknowledgement(bean);
						player.setActualPosition(bean.getPosition());
					} else if (bean.getType() == XMPPBean.TYPE_GET) {
						System.out
								.println("PPL: KeepAliveBean vom typ GET empfangen");
					} else if (bean.getType() == XMPPBean.TYPE_RESULT) {
						System.out
								.println("PPL: KeepAliveBean vom typ RESULT empfangen");
					} else if (bean.getType() == XMPPBean.TYPE_ERROR) {
						System.out
								.println("PPL: KeepAliveBean vom typ ERROR empfangen");
					}

				} else if (b instanceof PlayerUpdateBean) {
					// System.out.println("PPL: PlayerUpdate Type: " +
					// b.getType());
					PlayerUpdateBean bean = (PlayerUpdateBean) b;
					if (bean.getType() == XMPPBean.TYPE_RESULT) {
						// System.out.println("PPL: PlayerUpdateBean vom typ RESULT empfangen");
					}
				} else if (b instanceof JoinGameBean) {
					// System.out.println("PPL: JoinGame von: " + b.getFrom());
					JoinGameBean bean = (JoinGameBean) b;
					if (bean.getType() == XMPPBean.TYPE_RESULT) {
						System.out
								.println("PPL: JoinGameBean vom typ RESULT empfangen ERROR");
					}
					if (bean.getType() == XMPPBean.TYPE_ERROR) {
						System.out
								.println("PPL: JoinGameBean vom typ Error empfangen ERROR");
					}
					if (bean.getType() == XMPPBean.TYPE_SET) {
						// System.out.println("PPL: JoinGameBean vom typ SET empfangen ERROR");
					}
					if (bean.getType() == XMPPBean.TYPE_GET) {
						// System.out.println("PPL: JoinGameBean vom typ GET empfangen ERROR");
					}

				} else if (b instanceof EndGameBean) {
					System.out.println("PPL: EndGame von: " + b.getFrom());
					JoinGameBean bean = (JoinGameBean) b;
					if (bean.getType() == XMPPBean.TYPE_RESULT) {
						player.acknowledgeBean(bean);
					}
				}
			}
		}
	}
}
