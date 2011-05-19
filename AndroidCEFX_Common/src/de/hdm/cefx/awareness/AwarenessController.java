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

import de.hdm.cefx.CEFXController;

/**
 * 
 * The AwarenessController is initialised by the CEFXController. The
 * CEFXController also initialises the AwarnessWidgets and registers them with
 * the AwarenessController.
 * 
 * @author Ansgar Gerlicher
 */
public interface AwarenessController {

	/**
	 * The CEFXController also initialises the AwarnessWidgets and registers
	 * them with the AwarenessController. This is done by using the
	 * <code>registerWidget(...)</code> method.
	 * 
	 * @param widget
	 *            the widget to be registered with the AwarnessController.
	 */
	public void registerWidget(AwarenessWidget widget);

	/**
	 * The CEFXController provides the AwarenessController with a reference to
	 * it by calling the <code>setCEFXController(...)</code> method. The
	 * AwarnessController uses this reference to retrieve a reference to the
	 * NetworkController in order to send awareness events over the network.
	 * 
	 * @param impl
	 *            a reference to the CEFXController.
	 */
	public void setCEFXController(CEFXController impl);

	/**
	 * The <code>getController(...)</code> method returns the reference to the
	 * CEFXController.
	 * 
	 * @return a reference to the CEFXController.
	 */
	public CEFXController getCEFXController();

	/**
	 * When an event is to be forwarded to an awareness widget, the
	 * <code>awarenessEvent(...)</code> method of the AwarenessController must
	 * be called.
	 * 
	 * @param event
	 *            the AwarenessEvent to forward to an awareness widget.
	 */
	public void awarenessEvent(AwarenessEvent event);

	/**
	 * The method <code>propagateAwarenessEvent(...)</code> propagates an
	 * event to the other clients in a session. Before an event is visualised or
	 * propagated, it is checked, if any widget exists that is interested in
	 * such an event. If no widget is interested, the event is dropped.
	 * 
	 * @param event
	 *            the AwarnessEvent to be propagated to the other clients.
	 */
	public void propagateAwarenessEvent(AwarenessEvent event);

}
