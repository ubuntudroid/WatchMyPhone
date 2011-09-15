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
 * Copyright 2007 Ansgar Gerlicher.
 * @author Ansgar Gerlicher
 */
package de.hdm.cefx.concurrency.operations;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

/**
 * The StateVecor class implements the interfaces Clonable and Serializable
 * which is required in order to be able to send a copy of a StateVector over
 * the network.
 *
 * @author Ansgar Gerlicher
 *
 */
@SuppressWarnings("serial")
public class StateVector implements Cloneable, Serializable {

	private HashMap<Integer, Integer> states;

	/**
	 * Class constructor.
	 */
	public StateVector() {
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		states = map;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#clone()
	 */
	@SuppressWarnings("unchecked")
	public Object clone() {
		StateVector sv = new StateVector();
		synchronized (this) {

			for (Integer key : states.keySet()) {
				sv.states.put(key, states.get(key));
			}
		}

		return sv;
	}

	/**
	 * Retrieves the state vector count for the given client id.
	 *
	 * @param clientidentifier
	 *            the id of the client.
	 * @return the count of executed operations at this clients site.
	 */
	public int getState(int clientidentifier) {
		Integer state = states.get(new Integer(clientidentifier));
		if (state == null)
			return -1;
		else
			return state.intValue();
	}

	/**
	 * Sets the state vector count for the given client id to the given value.
	 *
	 * @param clientidentifier
	 *            the client id.
	 * @param state
	 *            the count value.
	 */
	public void setState(int clientidentifier, int state) {
		states.put(new Integer(clientidentifier), new Integer(state));
	}

	/**
	 * Returns the number of clients that are counted in this state vector.
	 *
	 * @return number of clients.
	 */
	public int size() {

		return states.size();
	}

	/**
	 * Clears the state vector.
	 */
	public void clear() {
		states.clear();
	}

	/**
	 * Checks if a certain client id is contained in the state vector.
	 *
	 * @param key
	 *            the id of the client.
	 * @return true if the client operations count is contained in this state
	 *         vector.
	 */
	public boolean containsKey(Object key) {
		return states.containsKey(key);
	}

	/**
	 * Checks if a certain count value exists in this state vector.
	 *
	 * @param value
	 *            count.
	 * @return true if the value exists.
	 */
	public boolean containsValue(Object value) {
		return states.containsValue(value);
	}

	/**
	 * Retrieves the set of entries of this state vector.
	 *
	 * @return the entry set of this state vector.
	 */
	public Set<Entry<Integer, Integer>> entrySet() {
		return states.entrySet();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		return states.equals(o);
	}

	/**
	 * Retrieves the value count for the given client id.
	 *
	 * @param key
	 *            client id.
	 * @return count value.
	 */
	public Integer get(Object key) {
		return states.get(key);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return states.hashCode();
	}

	/**
	 * Checks if the state vector is empty.
	 *
	 * @return true if it is empty.
	 */
	public boolean isEmpty() {
		return states.isEmpty();
	}

	/**
	 * Returns all client ids in this state vector.
	 *
	 * @return set of client ids.
	 */
	public Set<Integer> keySet() {
		return states.keySet();
	}

	/**
	 * Adds a new client and count value to this state vector.
	 *
	 * @param key
	 *            the client id.
	 * @param value
	 *            the count value.
	 * @return previous count value if it already existed.
	 */
	public Integer put(Integer key, Integer value) {
		return states.put(key, value);
	}

	/**
	 * Copies all of the mappings from the specified map to this map These
	 * mappings will replace any mappings that this map had for any of the keys
	 * currently in the specified map.
	 *
	 * @param m
	 *            mappings to be stored in this map.
	 * @throws NullPointerException
	 *             if the specified map is null.
	 */
	public void putAll(HashMap<? extends Integer, ? extends Integer> m) {
		states.putAll(m);
	}

	/**
	 * Removes the specified client from the state vector.
	 *
	 * @param key
	 *            client id.
	 * @return previous count value for this client id.
	 */
	public Integer remove(Object key) {
		return states.remove(key);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return states.toString();
	}

	/**
	 * Returns all count values for all clients in this state vector.
	 *
	 * @return the count values in a collection.
	 */
	public Collection<Integer> values() {
		return states.values();
	}

	/**
	 * Returns the sum of all count values in this state vector.
	 *
	 * @return the sum of all state counts.
	 */
	public int getSumOfStates() {
		int sum = 0;
		Iterator iter = values().iterator();
		while (iter.hasNext()) {
			Integer element = (Integer) iter.next();
			sum += element.intValue();
		}
		return sum;
	}

	/**
	 * Resets this state vector.
	 */
/*	public void reset() {
		Collection<Integer> values = this.states.keySet();
		for (Integer integer : values) {
			this.setState(integer, 0);
		}

	}*/

}
