/*******************************************************************************
 * Copyright (C) 2010 Ansgar Gerlicher
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
 * Stuttgart, Hochschule der Medien: http://www.mi.hdm-stuttgart.de/mmb/
 * Collaborative Editing Framework or XML:
 * http://sourceforge.net/projects/cefx/
 * 
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: http://mobilisplatform.sourceforge.net
 ******************************************************************************/
/**
 * This sourcecode is part of the Collaborative Editing Framework for XML (CEFX).
 * @author Michael Voigt
 */
package de.hdm.cefx.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;


public class StringRegistry {

	private Hashtable<String, Hashtable<String, String>> values;
	private static StringRegistry instance=null;

	private StringRegistry() {
		values=new Hashtable<String, Hashtable<String, String>>();
	}

	public static StringRegistry getInstance() {
		if (instance==null) {
			instance=new StringRegistry();
		}
		return instance;
	}

	public void readRegistryFile(String name) {
		File file=new File(name);
		if (!file.exists()) {
			return;
		}

		BufferedReader in;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(name)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		try {
			Hashtable<String, String> table=null;
			String line=null;
			do {
				line=in.readLine();
				if (line==null) {
					continue;
				}
				line.trim();
				if (line.length()==0) {
					continue;
				}
				if (line.startsWith("#")) {
					table=new Hashtable<String, String>();
					String key=line.substring(1, line.length());
					values.put(key, table);
				} else {
					if (table!=null) {
						String[] strings=line.split("\t");
						if (strings.length!=2) {
							continue;
						}
						String key=strings[0];
						String val=strings[1];
						if ((key.length()>0) && (val.length()>0)) {
							table.put(key, val);
						}
					}
				}

			} while (line!=null);
			in.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized String getValue(String group, String key) {
		if (!values.containsKey(group)) {
			return null;
		}
		Hashtable<String, String> params=values.get(group);
		if (!params.containsKey(key)) {
			return null;
		}
		return params.get(key);
	}

}
