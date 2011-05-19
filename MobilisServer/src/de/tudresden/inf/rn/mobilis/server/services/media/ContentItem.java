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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity @Table(name="mobilis_contentitem")
public class ContentItem implements Serializable {		

	private static final long serialVersionUID = 1L;
	
	@EmbeddedId
		public ContentItem.Identifier identifier;
	@Column(name="source")
		public String source;
	@Column(name="description")
		public String description;
	@Column(name="mimetype")
		public String mimetype;
	@Column(name="filename")
		public String filename;

	
	public static class Identifier implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		public Identifier() {}
		public Identifier(String repository, String uid) {
			this.repository = repository; this.uid = uid;
		}
		
		@Column(name="repository")
			public String repository;
		@Column(name="uid")
			public String uid;
		
		@Override
		public int hashCode() {
			return this.toString().hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			return (obj instanceof Identifier)
					&& ((ContentItem.Identifier)obj).repository.equals(this.repository)
					&& ((ContentItem.Identifier)obj).uid.equals(this.uid);
		}
		@Override
		public String toString() {
			return this.repository + "#" + this.uid;
		}
		
	}

	
}
