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
package de.hdm.cefx.awareness.events;

import de.hdm.cefx.awareness.AwarenessController;
import de.hdm.cefx.awareness.AwarenessEvent;


/**
 * 
 * The <code>EventPropagator</code> class is used to propagate the awareness
 * events to the AwarenessController.
 * 
 * @author Ansgar Gerlicher
 */
public class EventPropagator {

	public static final int SCOPE_INTERNAL = 0;

	public static final int SCOPE_EXTERNAL = 1;

	private static AwarenessController ac = null;

	/**
	 * Override of
	 * <code>propagateEvent(Object event, String eventType, String eventDescription, int scope, String source )</code>
	 * 
	 * @see #propagateEvent(Object event, String eventType, String
	 *      eventDescription, int scope, String source)
	 */
	public static void propagateEvent(Object event, String eventType, int scope, String source) {
		if (ac != null) {

			if (scope == SCOPE_INTERNAL) {
				AwarenessEvent awe = new AwarenessEvent(eventType, "", event, source);
				ac.awarenessEvent(awe);
			} else {
				AwarenessEvent awe = new AwarenessEvent(eventType, "", event, source);
				ac.propagateAwarenessEvent(awe);
			}

		}

	}

	/**
	 * In order to propagate an awareness event, the
	 * <code>propagateEvent(...)</code> method is used. The
	 * <code>propagateEvent(...)</code> method requires the following
	 * arguments to be passed to it:
	 * 
	 * @param event
	 *            this can be an object of any type and represents the original
	 *            event that occurred. For example a mouse event or a key event.
	 * @param eventType
	 *            a text that identifies the type of event. For example
	 *            “MOUSE_EVENT” or “KEYBOARD_EVENT”. The type argument will be
	 *            used to find the awareness widgets that are interested in this
	 *            event.
	 * @param eventDescription
	 *            a text that describes the event in further detail.
	 * @param scope
	 *            the scope of the event. Two scopes exist, the internal and the
	 *            external scope. The internal scope is for events that should
	 *            not be propagated to other clients but visualised to the user.
	 *            The external scope is for events that should be propagated to
	 *            other clients.
	 * @param source
	 *            a text that identifies the source of the event. This could be,
	 *            for example, the client's name or id.
	 */
	public static void propagateEvent(Object event, String eventType, String eventDescription, int scope, String source) {
		if (ac != null) {
			if (scope == SCOPE_INTERNAL) {
				AwarenessEvent awe = new AwarenessEvent(eventType, eventDescription, event, source);
				ac.awarenessEvent(awe);
			} else {
				AwarenessEvent awe = new AwarenessEvent(eventType, eventDescription, event, source);
				ac.propagateAwarenessEvent(awe);
			}

		}

	}

	/**
	 * Override of
	 * <code>propagateEvent(Object event, String eventType, String eventDescription, int scope, String source )</code>
	 * 
	 * @see #propagateEvent(Object event, String eventType, String
	 *      eventDescription, int scope, String source)
	 */
	public static void propagateEvent(Object event, int scope, String source) {
		if (ac != null) {
			if (scope == SCOPE_INTERNAL) {
				AwarenessEvent awe = new AwarenessEvent("", "", event, source);
				ac.awarenessEvent(awe);
			} else {
				AwarenessEvent awe = new AwarenessEvent("", "", event, source);
				ac.propagateAwarenessEvent(awe);
			}

		}

	}

	/**
	 * The AwarenessController provides the EventPropagator with a reference to
	 * it by calling the <code>setAwarenessController(...)</code> method. The
	 * EventPropagater uses this reference to forward events to the
	 * AwarenessController.
	 * 
	 * @param acimpl
	 *            a reference to the AwarenessController.
	 */
	public static void setAwarenessController(AwarenessController acimpl) {

		ac = acimpl;
	}
}
