package de.tudresden.inf.rn.mobilis.server.services.locpairs;

import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.PacketFilter;

import de.tudresden.inf.rn.mobilis.server.services.locpairs.GamePacketFilter;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.locpairs.*;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanFilterAdapter;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanProviderAdapter;

/**
 * The Class GamePacketFilter. It filters incoming packets and only passes IQs of the type
 * "joinGame" to the associated filter. It adds all necessary BeanPrototypes to the 
 * ProviderManager of SMACK.
 * 
 * @author Reik Mueller
 */
public class GamePacketFilter{
	
	private static XMPPBean joinGameBeanPrototype = new JoinGameBean();
	private static XMPPBean gameInformationBeanPrototype = new GameInformationBean();
	@SuppressWarnings("unused")
	private static final GamePacketFilter instance = new GamePacketFilter();
	
	private GamePacketFilter(){
		(new BeanProviderAdapter(new GameInformationBean())).addToProviderManager();
		(new BeanProviderAdapter(new JoinGameBean())).addToProviderManager();
	}
	
	/**
	 * Gets the filter.
	 *
	 * @return the filter
	 */
	public static PacketFilter getFilter(){
		return  new OrFilter(	new BeanFilterAdapter(joinGameBeanPrototype),
								new BeanFilterAdapter(gameInformationBeanPrototype));
	}
	
}
