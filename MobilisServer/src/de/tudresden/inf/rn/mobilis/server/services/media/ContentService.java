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
package de.tudresden.inf.rn.mobilis.server.services.media;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.logging.Level;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import de.tudresden.inf.rn.mobilis.server.MobilisManager;
import de.tudresden.inf.rn.mobilis.server.services.MobilisService;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.media.ContentDeleteBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.media.ContentItemInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.media.ContentRegisterBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.media.ContentTransferBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.media.ContentUnregisterBean;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanFilterAdapter;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanIQAdapter;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanProviderAdapter;

public class ContentService extends MobilisService implements FileTransferListener {
	
	private static final int FILETRANSFER_BLOCKSIZE = 4*1024;
	private ContentStore contentStore;
	private FileTransferManager fileTransferManager;
	
	public ContentService() {
		super();
		this.contentStore = new ContentStore(this.getSettingString("StorageFolder"));
	}
	
	@Override
	protected void registerPacketListener() {
		XMPPBean contentTransferPrototype = new ContentTransferBean();
		XMPPBean contentDeletePrototype = new ContentDeleteBean();
		XMPPBean contentRegisterPrototype = new ContentRegisterBean();
		XMPPBean contentUnregisterPrototype = new ContentUnregisterBean();
		(new BeanProviderAdapter(contentTransferPrototype)).addToProviderManager();
		(new BeanProviderAdapter(contentDeletePrototype)).addToProviderManager();
		(new BeanProviderAdapter(contentRegisterPrototype)).addToProviderManager();
		(new BeanProviderAdapter(contentUnregisterPrototype)).addToProviderManager();
		this.mAgent.getConnection().addPacketListener(this, new OrFilter(
				new OrFilter(
					new BeanFilterAdapter(contentTransferPrototype),
					new BeanFilterAdapter(contentDeletePrototype)
				), new OrFilter(
					new BeanFilterAdapter(contentRegisterPrototype),
					new BeanFilterAdapter(contentUnregisterPrototype)
				)
			));
		XMPPConnection c = this.mAgent.getConnection();
		FileTransferNegotiator.setServiceEnabled(c, true);
		this.fileTransferManager = new FileTransferManager(c);
		this.fileTransferManager.addFileTransferListener(this);
	}
	
    public void processPacket(Packet p) {
    	super.processPacket(p);
    	if (p instanceof BeanIQAdapter) {
    		XMPPBean b = ((BeanIQAdapter) p).getBean();
    		if (b instanceof ContentTransferBean) {
    			ContentTransferBean bb = (ContentTransferBean) b;
    			if (b.getType() == XMPPBean.TYPE_GET)
    				this.inContentTransferGet(bb);
    			else if (b.getType() == XMPPBean.TYPE_SET)
    				this.inContentTransferSet(bb);
       		} else if (b instanceof ContentDeleteBean) {
    			ContentDeleteBean bb = (ContentDeleteBean) b;
    			if (b.getType() == XMPPBean.TYPE_SET)
    				this.inContentDeleteSet(bb);
    		} else if (b instanceof ContentRegisterBean) {
    			ContentRegisterBean bb = (ContentRegisterBean) b;
    			if (b.getType() == XMPPBean.TYPE_SET)
    				this.inContentRegisterSet(bb);
    		} else if (b instanceof ContentUnregisterBean) {
    			ContentUnregisterBean bb = (ContentUnregisterBean) b;
    			if (b.getType() == XMPPBean.TYPE_SET)
    				this.inContentUnregisterSet(bb);
    		}
    	}
    }

	@Override
	public void fileTransferRequest(FileTransferRequest request) {
		String from        = request.getRequestor();
		String description = request.getDescription();
		// parse content description
		XmlPullParserFactory factory;
		ContentItemInfo contentDescription = null;
		ContentItem item = null;
		XmlPullParser parser;
		try {
			factory = XmlPullParserFactory.newInstance();
			parser = factory.newPullParser();
			parser.setInput(new StringReader(description));
			contentDescription = new ContentItemInfo(parser);
		} catch (Exception e) {
			MobilisManager.getLogger().log(Level.WARNING, this.getIdent() + " couldn't accept content item due to " + e.getClass().getName() + ": " + e.getMessage());
		}
		// security test
		if (contentDescription != null) {
			ContentItem.Identifier identifier = new ContentItem.Identifier(
					contentDescription.getRepository(),
					contentDescription.getUid());
			item = this.contentStore.acceptExpectedItem(
					identifier,
					from,
					request.getFileName(),
					request.getMimeType(),
					contentDescription.getDescription());
			if (item != null) {
				IncomingFileTransfer transfer = request.accept();
				ContentTransferBean beanToRepository = new ContentTransferBean();
				beanToRepository.setFrom(this.mAgent.getJid());
				beanToRepository.setTo(item.identifier.repository);
				beanToRepository.setUid(item.identifier.uid);
				beanToRepository.setRetrieveFrom(from);
				beanToRepository.setSendTo(this.mAgent.getJid());
				try {
					File file = this.contentStore.getItemFile(identifier);
					if (!file.exists() || file.canWrite()) {
						transfer.recieveFile(file);
						MobilisManager.getLogger().log(Level.INFO, this.getIdent() + " accepted content item " + identifier.toString() +  " (" + file.getAbsolutePath() + ")");
						beanToRepository.setType(XMPPBean.TYPE_RESULT);
						this.mAgent.getConnection().sendPacket(new BeanIQAdapter(beanToRepository));
					} else {
						MobilisManager.getLogger().log(Level.WARNING, this.getIdent() + " couldn't accept content item " + identifier.toString() + " because it couldn't be stored to the filesystem (" + file.getAbsolutePath() + ")");
						transfer.cancel();
						this.contentStore.deleteItem(item);
						this.contentStore.expectItem(item.identifier, item.source);
					}
				} catch (XMPPException e) {
					MobilisManager.getLogger().log(Level.WARNING, this.getIdent() + " couldn't accept content item " + identifier.toString() + " due to " + e.getClass().getName() + ": " + e.getMessage());
					transfer.cancel();
					this.contentStore.deleteItem(item); // rollback
					this.contentStore.expectItem(item.identifier, item.source);
				}
			} else {
				MobilisManager.getLogger().log(Level.WARNING, this.getIdent() + " couldn't accept content item " + identifier.toString() + " because it was not expected.");
				request.reject();
				return;
			}
		} else {
			MobilisManager.getLogger().log(Level.WARNING, this.getIdent() + " couldn't accept content item because a malformed request was issued.");
			request.reject();
			return;
		}
	}
    
	protected void inContentUnregisterSet(ContentUnregisterBean bean) {
		// Unregister a repository
		String from = bean.getFrom();
		String to   = bean.getTo();
    	ContentUnregisterBean beanAnswer = bean.clone();
    	beanAnswer.setFrom(to); beanAnswer.setTo(from);
		if (!this.contentStore.isRepositoryRegistered(from)) {
			// error: packet sender is not a registered repository
			MobilisManager.getLogger().log(Level.WARNING, this.getIdent() + " couldn't unregister repository: " + from
					+ " (Reason: packet sender is not a registered repository.)");
	    	beanAnswer.setType(XMPPBean.TYPE_ERROR);
		} else {
			// ok: unregister repository
			this.contentStore.unregister(from);
			MobilisManager.getLogger().log(Level.INFO, this.getIdent() + " unregistered repository: " + from);
	    	beanAnswer.setType(XMPPBean.TYPE_RESULT);
		}
    	this.mAgent.getConnection().sendPacket(new BeanIQAdapter(beanAnswer));
    }
    
	protected void inContentRegisterSet(ContentRegisterBean bean) {
		// Register a repository
		String from = bean.getFrom();
		String to   = bean.getTo();
		// prepare feedback
		ContentRegisterBean beanAnswer = bean.clone();
    	beanAnswer.setFrom(to); beanAnswer.setTo(from);
		if (this.contentStore.isRepositoryRegistered(from)) {
			// error: packet sender is already registered as repository
			MobilisManager.getLogger().log(Level.WARNING, this.getIdent() + " couldn't register repository: " + from + 
					" (Reason: packet sender is already registered as repository.)");
	    	beanAnswer.setType(XMPPBean.TYPE_ERROR);
		} else {
			// ok: register repository
			this.contentStore.register(from);
			MobilisManager.getLogger().log(Level.INFO, this.getIdent() + " registered repository: " + from);
	    	beanAnswer.setType(XMPPBean.TYPE_RESULT);
		}
    	this.mAgent.getConnection().sendPacket(new BeanIQAdapter(beanAnswer));
    }
    
	protected void inContentDeleteSet(ContentDeleteBean bean) {
		// Delete a file from the repository
		String uid  = bean.getUid();
		String from = bean.getFrom();
		String to   = bean.getTo();
		ContentItem.Identifier identifier = new ContentItem.Identifier(from, uid);
		// prepare feedback
		ContentDeleteBean beanAnswer = bean.clone();
		beanAnswer.setFrom(to); beanAnswer.setTo(from);
		if (!this.contentStore.isRepositoryRegistered(from)) {
			// error: packet sender is not a registered repository
			MobilisManager.getLogger().log(Level.WARNING, this.getIdent() + " couldn't delete content: " + from + "#" + uid
					+ " (Reason: packet sender is not a registered repository.)");
			beanAnswer.setType(XMPPBean.TYPE_ERROR);
		} else {
			// ok: delete content
			this.contentStore.deleteItem(identifier);
			MobilisManager.getLogger().log(Level.INFO, this.getIdent() + " deleted content: " + from + "#" + uid);
			beanAnswer.setType(XMPPBean.TYPE_RESULT);
		}
		this.mAgent.getConnection().sendPacket(new BeanIQAdapter(beanAnswer));
	}

	protected void inContentTransferSet(ContentTransferBean bean) {
		// Store a file into the repository
		String sendTo       = bean.getSendTo();
		String retrieveFrom = bean.getRetrieveFrom();
		String uid          = bean.getUid();
		String from  = bean.getFrom();
		String to    = bean.getTo();
		String me    = mAgent.getConnection().getUser();
		// prepare feedback
		ContentTransferBean beanAnswer = bean.clone();
		beanAnswer.setFrom(to); beanAnswer.setTo(from);
		if (!sendTo.equals(me)) {
			// error: can only expect files which are sent to me
			MobilisManager.getLogger().log(Level.WARNING, this.getIdent() + " couldn't transfer (set) content " + from + "#" + uid + " from " + retrieveFrom
					+ " (Reason: can only expect files which are sent to me)");
			beanAnswer.setType(XMPPBean.TYPE_ERROR);
			this.mAgent.getConnection().sendPacket(new BeanIQAdapter(beanAnswer));
		} else if (!this.contentStore.isRepositoryRegistered(from)) {
			// error: packet sender is not a registered repository
			MobilisManager.getLogger().log(Level.WARNING, this.getIdent() + " couldn't transfer (set) content " + from + "#" + uid + " from " + retrieveFrom
					+ " (Reason: packet sender is not a registered repository.)");
			beanAnswer.setType(XMPPBean.TYPE_ERROR);
			this.mAgent.getConnection().sendPacket(new BeanIQAdapter(beanAnswer));
		} else {
			this.contentStore.expectItem(from, uid, retrieveFrom);
			// notification of the source to send the file to the content service
			ContentTransferBean beanToSource = new ContentTransferBean();
			beanToSource.setType(XMPPBean.TYPE_GET);
			beanToSource.setFrom(me);
			beanToSource.setTo(retrieveFrom);
			beanToSource.setRetrieveFrom(retrieveFrom);
			beanToSource.setSendTo(me);
			beanToSource.setUid(uid);
			this.mAgent.getConnection().sendPacket(new BeanIQAdapter(beanToSource));
			// answer
			MobilisManager.getLogger().log(Level.INFO, this.getIdent() + " transfering (set) content " + from + "#" + uid + " from " + retrieveFrom);
			beanAnswer.setType(XMPPBean.TYPE_RESULT);
		}
			
	}

	protected void inContentTransferGet(ContentTransferBean bean) {
		// Get a file from the repository
		String sendTo       = bean.getSendTo();
		String retrieveFrom = bean.getRetrieveFrom();
		String uid          = bean.getUid();
		String from = bean.getFrom();
		String to   = bean.getTo();
		String me   = mAgent.getConnection().getUser();
		ContentItem.Identifier identifier = new ContentItem.Identifier(from, uid);
		ContentItem item = this.contentStore.findItem(identifier);
		// prepare feedback
		ContentTransferBean beanAnswer = bean.clone();
		beanAnswer.setFrom(to); beanAnswer.setTo(from);
		if (!retrieveFrom.equals(me)) {
			// error: content service is not intended to send the item 
			MobilisManager.getLogger().log(Level.WARNING, this.getIdent() + " couldn't transfer (get) content " + from + "#" + uid + " to " + sendTo
					+ " (Reason: content service is not intended to send the item.)");
			beanAnswer.setType(XMPPBean.TYPE_ERROR);
		} else if (item == null) {
			// error: item is not available
			MobilisManager.getLogger().log(Level.WARNING, this.getIdent() + " couldn't transfer (get) content " + from + "#" + uid + " to " + sendTo
					+ " (Reason: item is not available.)");
			beanAnswer.setType(XMPPBean.TYPE_ERROR);
		} else {
			// ok: initiate file transfer
			File file = this.contentStore.getItemFile(identifier);
			if (!file.exists() || !file.canRead()) {
				// error: item file not readable
				MobilisManager.getLogger().log(Level.WARNING, this.getIdent() + " couldn't transfer (get) content " + from + "#" + uid + " to " + sendTo
						+ " (Reason: item file is not available for reading.)");
				beanAnswer.setType(XMPPBean.TYPE_ERROR);
			} else {
				OutgoingFileTransfer transfer = this.fileTransferManager.createOutgoingFileTransfer(sendTo);
				boolean transferred = false;
				OutputStream output = null;
				InputStream input = null;
				try {
					output = transfer.sendFile(item.filename, file.length(), item.description);
					input = new FileInputStream(file);
					int actuallyRead = 0;
					byte[] buffer = new byte[ContentService.FILETRANSFER_BLOCKSIZE];
					do {
						actuallyRead = input.read(buffer, 0, ContentService.FILETRANSFER_BLOCKSIZE);
						if (actuallyRead != -1)
							output.write(buffer, 0, actuallyRead);
					} while (actuallyRead != -1);
					transferred = true;
				} catch (XMPPException e) {} catch (FileNotFoundException e) {} catch (IOException e) {}
				try {
					if (output != null) output.close();
					if (input != null) input.close();
				} catch (IOException e) {}
				if (!transferred) {
					// error: file wasn't transferred
					MobilisManager.getLogger().log(Level.WARNING, this.getIdent() + " couldn't transfer (get) content " + from + "#" + uid + " to " + sendTo
							+ " (Reason: file couldn't be transferred.)");
					beanAnswer.setType(XMPPBean.TYPE_ERROR);
				} else {
					// ok: we did everything
					MobilisManager.getLogger().log(Level.INFO, this.getIdent() + " transfering (get) content " + from + "#" + uid + " to " + sendTo);
					beanAnswer.setType(XMPPBean.TYPE_RESULT);
				}
			}
		}
		this.mAgent.getConnection().sendPacket(new BeanIQAdapter(beanAnswer));			
	}

}
