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
package de.tudresden.inf.rn.mobilis.mapdraw.overlaymgr;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;


import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Manages map overlays.
 * (Basis version by Christoph Widulle, under GNU General Public License)
 * 
 * @author Hering, Widulle
 *
 */
public class OverlayManager {

    ArrayList<ManagedOverlay> overlays = new ArrayList<ManagedOverlay>();
    MapView mapView;
    Context ctx;

    /**
     * Populates layers to the MapView.
     * Overlays not managed by OverlayManager will stay untouched.
     */
    public void populate() {
    	
        List<Overlay> mapoverlays = mapView.getOverlays();

        // get all unmanaged overlays from the map view
        ArrayList<Overlay> unmanagedOverlays = new ArrayList<Overlay>();
        int size = mapoverlays.size();
        for (int i = 0; i < size; i++) {
            Overlay overlay = mapoverlays.get(i);
            if (!(overlay instanceof ManagedOverlay)) {
                unmanagedOverlays.add(overlay);
            }
        }
        
        // get all managed overlays to add upon the unmanaged overlays
        ArrayList<ManagedOverlay> managedOverlays = new ArrayList<ManagedOverlay>();
        size = overlays.size();
        for (int i = 0; i < size; i++) {
            ManagedOverlay overlay = overlays.get(i);
            overlay.init();
            managedOverlays.add(overlay);
        }
        
        Collections.sort(managedOverlays);
        mapoverlays.clear();
        mapoverlays.addAll(unmanagedOverlays);
        mapoverlays.addAll(managedOverlays);
        
        mapView.invalidate();
    }

    public OverlayManager(Context ctx, MapView mapView) {
        this.mapView = mapView;
        this.ctx = ctx;
    }
    
    public void addOverlay(ManagedOverlay overlay, boolean checkForDuplicate, boolean refresh) {
    	if (overlay != null) {
    		if (checkForDuplicate) {
    			if (overlays.contains(overlay)) return;
    		}
    		overlays.add(overlay);
    		overlay.onVisibilityChanged();
    		if (refresh) populate();
    	}
    }

    public boolean removeOverlay(ManagedOverlay overlay, boolean refresh) {
        if (overlay != null) {
            overlays.remove(overlay);
            if (refresh) populate();
            return true;
        } else {
            return false;
        }
    }

    public boolean removeOverlay(String name, boolean refresh) {
        ManagedOverlay o = getOverlay(name);
        return removeOverlay(o, refresh);
    }

    public ManagedOverlay getOverlay(int i) {
        if (overlays.size() >= i) {
            return overlays.get(i);
        } else {
            return null;
        }
    }

    public MapView getMapView() {
        return mapView;
    }

    public ManagedOverlay getOverlay(String name) {
        for (int i = 0; i < overlays.size(); i++) {
            ManagedOverlay overlay = overlays.get(i);
            if (name.equals(overlay.getName()))
                return overlay;
        }
        return null;
    }

    protected Drawable createDefaultMarker() {
        Bitmap bitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint p = new Paint();
        p.setColor(Color.BLUE);
        p.setAlpha(50);
        p.setAntiAlias(true);
        canvas.drawCircle(8, 8, 8, p);
        BitmapDrawable bd = new BitmapDrawable(bitmap);
        bd.setBounds(0, 0, bd.getIntrinsicWidth(), bd.getIntrinsicHeight());
        return bd;
    }

}
