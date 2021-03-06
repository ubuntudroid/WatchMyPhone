/*******************************************************************************
 * Copyright (C) 2011 Technische Universitšt Dresden
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
package de.tudresden.inf.rn.mobilis.mapdraw.overlaymgr;

public class ManagedOverlayException extends Exception{

    public ManagedOverlayException() {
    }

    public ManagedOverlayException(String s) {
        super(s);
    }

    public ManagedOverlayException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ManagedOverlayException(Throwable throwable) {
        super(throwable);
    }
}
