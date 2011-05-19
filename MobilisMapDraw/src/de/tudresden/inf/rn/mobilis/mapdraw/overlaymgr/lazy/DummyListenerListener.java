/*******************************************************************************
 * Copyright (C) 2011 Technische Universität Dresden
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
package de.tudresden.inf.rn.mobilis.mapdraw.overlaymgr.lazy;

import android.view.MotionEvent;
import com.google.android.maps.GeoPoint;

import de.tudresden.inf.rn.mobilis.mapdraw.overlaymgr.ManagedOverlay;
import de.tudresden.inf.rn.mobilis.mapdraw.overlaymgr.ManagedOverlayGestureDetector;
import de.tudresden.inf.rn.mobilis.mapdraw.overlaymgr.ManagedOverlayItem;
import de.tudresden.inf.rn.mobilis.mapdraw.overlaymgr.ZoomEvent;

public class DummyListenerListener implements ManagedOverlayGestureDetector.OnOverlayGestureListener{
    @Override
    public boolean onZoom(ZoomEvent zoom, ManagedOverlay overlay) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e, ManagedOverlay overlay, GeoPoint point, ManagedOverlayItem item) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e, ManagedOverlay overlay) {

    }

    @Override
    public void onLongPressFinished(MotionEvent e, ManagedOverlay overlay, GeoPoint point, ManagedOverlayItem item) {

    }

    @Override
    public boolean onScrolled(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY, ManagedOverlay overlay) {
        return false;
    }

    @Override
    public boolean onSingleTap(MotionEvent e, ManagedOverlay overlay, GeoPoint point, ManagedOverlayItem item) {
        return false;
    }

	@Override
	public boolean onSweep(MotionEvent e, ManagedOverlay overlay) {
		return false;
	}

	@Override
	public boolean onPress(MotionEvent motionEvent, ManagedOverlay overlay) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean onRelease(MotionEvent motionEvent, ManagedOverlay overlay) {
		// TODO Auto-generated method stub
		return false;
	}
}
