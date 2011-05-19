/**
 * Copyright (C) 2009 Technische Universität Dresden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: http://mobilisplatform.sourceforge.net
 */

package de.tudresden.inf.rn.mobilis.mxa.services.xmpp;

import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.pubsub.EventElement;
import org.jivesoftware.smackx.pubsub.ItemsExtension;

/**
 * Filters incoming XEP-0060 Publish-Subscrive events by the given target and
 * node.
 * @author Istvan Koren
 */
public class SubscriptionFilter implements PacketFilter {

	private String target;
	private String node;

	public SubscriptionFilter(String target, String node) {
		this.target = target;
		this.node = node;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jivesoftware.smack.filter.PacketFilter#accept(org.jivesoftware.smack
	 * .packet.Packet)
	 */
	@Override
	public boolean accept(Packet packet) {
		EventElement ee = (EventElement) packet.getExtension("event",
				"http://jabber.org/protocol/pubsub#event");
		if (ee != null) {
			ItemsExtension ie = (ItemsExtension) ee.getExtensions().get(0);
			if ((packet.getFrom().equals(target))
					&& (ie.getNode().equals(node))) {
				return true;
			}
		}
		return false;
	}
}
