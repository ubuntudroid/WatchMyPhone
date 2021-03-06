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
package de.tudresden.inf.rn.mobilis.mapdraw;


import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.maps.MapView;

import de.tudresden.inf.rn.mobilis.mapdraw.overlaymgr.ManagedOverlay;
import de.tudresden.inf.rn.mobilis.mapdraw.overlaymgr.OverlayManager;

public class BackgroundOverlay extends ManagedOverlay {
	
	private static final String TAG = "BackgroundOverlay";
	
	public BackgroundOverlay(OverlayManager overlayManager, String name, Drawable defaultMarker, int showAtLevel) {
		super(overlayManager, name, defaultMarker, showAtLevel);
		overlayManager.addOverlay(this, false, false);
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
		Log.i(TAG, "draw background");
		canvas.drawRGB(255, 255, 255);
	}

	
}
