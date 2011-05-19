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
package de.tudresden.inf.rn.mobilis.android.services;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.GroupChatInvitation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import de.tudresden.inf.rn.mobilis.android.util.Const;

public class ChattingService implements
		PacketListener {

    private BroadcastReceiver ir;
    
    public void initIntentReceiver() {
        ir = new IntentReceiver();
        Context context = SessionService.getInstance().getContext();
        context.registerReceiver(ir, new IntentFilter(Const.INTENT_PREFIX + "servicecall.groupchat"));
    }

    public void unregisterIntentReceiver() {
        SessionService.getInstance().getContext().unregisterReceiver(ir);
    }
    
    private class IntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action
                    .equals(Const.INTENT_PREFIX + "servicecall.groupchat")) {
                String message = intent.getStringExtra(Const.INTENT_PREFIX + "servicecall.groupchat.message");
                sendMessage("", message);
            }
        }        
    }
    
	public void initialize(XMPPConnection conn) {
	    // TODO Auto-generated method stub
	}

	/**
	 * 
	 * @param type
	 *            The message type (e.g. 'groupchat' or 'chat')
	 * @param sender
	 *            The jid of the sender
	 * @param message
	 */
	public void processIncomingMessage(Message.Type type, String sender,
			String message) {
		appCallbackIncomingMessage(sender, message);
	}

	/**
	 * Notifies the view of new chat messages by using an intent.
	 */
	private void appCallbackIncomingMessage(String from, String message) {
		Intent i = new Intent(
				Const.INTENT_PREFIX + "callback.groupchat");
		i
				.putExtra(
						Const.INTENT_PREFIX + "callback.groupchat.message",
						message);
		i
				.putExtra(
						Const.INTENT_PREFIX + "callback.groupchat.from",
						from);
		SessionService.getInstance().getContext().sendBroadcast(i);
	}

	/**
	 * Sends a message.
	 * 
	 * @param type
	 * @param recipient
	 *            The jid of the recipient, may be a MultiUserChat or a single
	 *            user.
	 * @param message
	 */
	public void sendMessage(String recipient, String message) {
		callGroupChatMessage(recipient, message);
	}

	/**
	 * BSL wrapper for message sending.
	 * 
	 * @param message
	 */
	private void callGroupChatMessage(String recipient, String message) {
		// TODO look up if recipient is MUC or single user.
		try {
			SessionService.getInstance().getGroupManagementService().getMuc()
					.sendMessage(message);
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void processPacket(Packet packet) {
		if (packet instanceof Message) {
			Message m = (Message) packet;
			processIncomingMessage(Message.Type.groupchat, StringUtils
					.parseResource(m.getFrom()), m.getBody());
		} else if (packet.getExtension(GroupChatInvitation.ELEMENT_NAME,
				GroupChatInvitation.NAMESPACE) != null) {
			GroupChatInvitation gci = (GroupChatInvitation) packet
					.getExtension(GroupChatInvitation.ELEMENT_NAME,
							GroupChatInvitation.NAMESPACE);
			gci.getRoomAddress();
		}
	}
}
