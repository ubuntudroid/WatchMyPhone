/**
 * Copyright (C) 2009 Technische Universit�t Dresden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: http://mobilisplatform.sourceforge.net
 */

package de.tudresden.inf.rn.mobilis.mxa.services.pubsub;

import de.tudresden.inf.rn.mobilis.mxa.services.callbacks.ISubscribeCallback;

interface IPubSubService {

    /**
     * Subscribes to the given node on the given target. The callback will be
     * called as soon as a new event has been received.
     */
    void subscribe(ISubscribeCallback callback, String target, String node);
    
    /**
     * Removes the subscription to the given node on the given target.
     */
    void unsubscribe(ISubscribeCallback callback, String target, String node);
}