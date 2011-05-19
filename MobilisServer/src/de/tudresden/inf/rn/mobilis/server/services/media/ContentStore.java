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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;

import de.tudresden.inf.rn.mobilis.server.HibernateUtil;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPUtil;


public class ContentStore {
	
	private Set<String> registeredRepositories
			= Collections.synchronizedSet(new HashSet<String>());
	private String storageFolder;
	private Map<ContentItem.Identifier,String> expectedItems
			= Collections.synchronizedMap(new HashMap<ContentItem.Identifier,String>());
	
	public ContentStore(String storageFolder) {
		this.storageFolder = storageFolder;
	}
		
	public boolean isRepositoryRegistered(String repository) {
		return this.registeredRepositories.contains(repository);
	}
	
	public void register(String repository) {
		this.registeredRepositories.add(repository);
	}
	
	public void unregister(String repository) {
		this.registeredRepositories.remove(repository);
	}
		
	public void expectItem(String repository, String uid, String source) {
		ContentItem.Identifier identifier = new ContentItem.Identifier(repository, uid);
		this.expectItem(identifier, source);
	}
	
	public void expectItem(ContentItem.Identifier identifier, String source) {
		this.expectedItems.put(identifier, source);
	}
		
	public ContentItem findItem(ContentItem.Identifier identifier) {
		Session session = HibernateUtil.getSession();
		Object o = session.get(ContentItem.class, identifier);
		if (o != null && (o instanceof ContentItem))
			return (ContentItem)o;
		else
			return null;
	}
	
	public ContentItem acceptExpectedItem(ContentItem.Identifier identifier, String source,
			String filename, String mimetype, String description) {
		if (this.expectedItems.containsKey(identifier)
				&& XMPPUtil.similarJid(this.expectedItems.get(identifier), source)) {
			Session session = HibernateUtil.getSession();
			ContentItem item = this.findItem(identifier);
			if (item == null) {
				item = new ContentItem();
				item.identifier = identifier;
			}
			this.expectedItems.remove(identifier);
			item.filename = filename;
			item.mimetype = mimetype;
			item.description = description;
			item.source = source;
			session.save(item);
			session.flush();
			return item;
		} else
			return null;
	}
	
	public File getItemFile(ContentItem.Identifier key) {
		return new File(
				this.storageFolder + File.separatorChar + key.toString().replace('/', '$')
			); 
	}
			
	public void deleteItem(ContentItem.Identifier identifier) {
		ContentItem item = this.findItem(identifier);
		this.deleteItem(item);
	}
	
	public void deleteItem(ContentItem item) {
		Session session = HibernateUtil.getSession();
		this.getItemFile(item.identifier).delete();
		session.delete(item);
		session.flush();
	}

}
