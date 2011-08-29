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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import de.hdm.cefx.CEFXController;
import de.hdm.cefx.awareness.events.EventPropagator;

/**
 * 
 * Implementation of AwarenessController.
 * 
 * @author Ansgar Gerlicher
 */
public class AwarenessControllerImpl implements AwarenessController {
	private final Logger LOG = Logger.getLogger(AwarenessControllerImpl.class.getName());

	/**
	 * Set of registered AwarenessWidgets.
	 */
	protected Set<AwarenessWidget> widgets;

	/**
	 * Reference to the CEFXController.
	 */
	protected CEFXController cefx;

	/**
	 * Set of awareness event types the registered widgets are interested in.
	 */
	private Set<String> interestingEvents;

	private CEFXtoMobilisHub collabEditingService;

	/**
	 * Constructor.
	 */
	public AwarenessControllerImpl() {
		// listeners = new ArrayList<AwarenessListener>();
		widgets = new HashSet<AwarenessWidget>();
		interestingEvents = new HashSet<String>();
		EventPropagator.setAwarenessController(this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hdm.cefx.awareness.AwarenessController#registerWidget(de.hdm.cefx.awareness.AwarenessWidget)
	 */
	public void registerWidget(AwarenessWidget widget) {
		widgets.add(widget);
		widget.setAwarenessController(this);
		widget.init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hdm.cefx.awareness.AwarenessController#setCEFXController(de.hdm.cefx.CEFXController)
	 */
	public void setCEFXController(CEFXController impl) {
		cefx = impl;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hdm.cefx.awareness.AwarenessController#awarenessEvent(de.hdm.cefx.awareness.AwarenessEvent)
	 */
	public void awarenessEvent(AwarenessEvent event) {
		LOG.info("AwarenessController received Awareness Event: " + event);
//		for (AwarenessWidget widget : widgets) {
//
//			if (widget.hasInterestIn(event)) {
//				widget.notifyOfAwarenessEvent(event);
//			}
//		}
//		collabEditingService.sendCEFXAwarenessEventToMyself(event);
		collabEditingService.onAwarenessEventReceived(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hdm.cefx.awareness.AwarenessController#propagateAwarenessEvent(de.hdm.cefx.awareness.AwarenessEvent)
	 */
	public void propagateAwarenessEvent(AwarenessEvent event) {

		// see if a widget exists, that is interested in the event.
		// If there is one, then propagate the event

		if (event.getEventSource() == null) {
			// if the source was not given, the set the source to the client
			// name
			event.setEventSource(cefx.getClient().getName());
		}

		if (interestingEvents.contains(event.getType())) {
			cefx.getNetworkController().propagateAwarenessEvent(event);
			return;
		}

		boolean interestedWidgetExists = false;
		for (AwarenessWidget widget : widgets) {

			if (widget.hasInterestIn(event)) {
				interestedWidgetExists = true;
				interestingEvents.add(event.getType());
			}
		}
		if (interestedWidgetExists) {
			cefx.getNetworkController().propagateAwarenessEvent(event);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hdm.cefx.awareness.AwarenessController#getCEFXController()
	 */
	public CEFXController getCEFXController() {

		return cefx;
	}

	public void setCollabEditingService(CEFXtoMobilisHub collabEditingService) {
		this.collabEditingService = collabEditingService;
	}

}
