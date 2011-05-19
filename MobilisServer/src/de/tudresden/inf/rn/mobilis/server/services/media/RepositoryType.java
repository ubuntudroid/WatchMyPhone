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

public enum RepositoryType {
	
	INTEGER, FLOAT, STRING, DECIMAL, TIMESTAMP;
	
	public String convertXmlToHql(String xml) {
		switch (this) {
		case INTEGER: case FLOAT: case TIMESTAMP: case STRING:
			return xml;
		default:
			return null;
		}
	}
	
	public String convertHqlToXml(String hql) {
		switch (this) {
		case INTEGER: case FLOAT: case TIMESTAMP: case STRING:
			return hql;
		default:
			return null;
		}
	}
	
	public String toHQL() {
		switch (this) {
			case INTEGER: return "cast(%s as integer)";
			case FLOAT: return "%s"; 
			case TIMESTAMP: return "%s";
			case STRING: default: return "%s";
		}
	}
	
	public static RepositoryType fromSettingString(String settingString) {
		if (settingString.equals("integer"))
			return RepositoryType.INTEGER;
		else if (settingString.equals("float"))
			return RepositoryType.FLOAT;
		else if (settingString.equals("timestamp"))
			return RepositoryType.TIMESTAMP;
		else
			return RepositoryType.STRING;
	}
	
	public static interface Resolver {
		public void addSlicing(String key, RepositoryType type);
		public RepositoryType getSlicingType(String key);
	}
	
}