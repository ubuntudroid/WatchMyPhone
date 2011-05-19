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

package de.hdm.cefx.awareness;

/**
 * The AwarenessWidget interface defines the methods that must be implemented by
 * an awareness widget.
 * 
 * @author Ansgar Gerlicher
 * 
 */
public interface AwarenessWidget {
	/**
	 * The <code>notifyOfAwarenessEvent(...)</code> method is called by the
	 * framework when the corresponding event (e.g. key event) has occurred and
	 * should contain the code that presents the event in some way to the user.
	 * 
	 * @param event
	 *            the AwarenessEvent that this widget is interested in.
	 */
	public void notifyOfAwarenessEvent(AwarenessEvent event);

	/**
	 * The method <code>hasInterestIn</code> is called just before the widget
	 * is notified of an event and should contain code that checks if the given
	 * event is relevant to the widget. For example a widget that will notify
	 * the user of key strokes may not be interested in mouse events or vice
	 * versa.
	 * 
	 * @param event 
	 *            the AwarenessEvent that may be of interest.
	 * @return true if the AwarenessWidget is interested in the event.
	 */
	public boolean hasInterestIn(AwarenessEvent event);

	/**
	 * When a widget is registered with the AC, it is provided with a reference
	 * to it by a call to the <code>setAwarenessController(...)</code> method.
	 * 
	 * @param ac 
	 *            a reference to the AwarenessController.
	 */
	public void setAwarenessController(AwarenessController ac);

	/**
	 * The AwarenessController initialises the widget by calling its
	 * <code>init()</code> method. When initialised, a widget usually displays
	 * some sort of window or dialogue to the user.
	 */
	public void init();
}
