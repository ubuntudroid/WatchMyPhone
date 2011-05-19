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
package de.tudresden.inf.rn.mobilis.mapdraw;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import de.tudresden.inf.rn.mobilis.mapdraw.overlaymgr.ManagedOverlay;
import de.tudresden.inf.rn.mobilis.mapdraw.overlaymgr.ManagedOverlayGestureDetector;
import de.tudresden.inf.rn.mobilis.mapdraw.overlaymgr.ManagedOverlayItem;
import de.tudresden.inf.rn.mobilis.mapdraw.overlaymgr.OverlayManager;
import de.tudresden.inf.rn.mobilis.mapdraw.overlaymgr.ZoomEvent;

/**
 * A map overlay, which displays drawing shapes like lines etc.
 * @author Dirk Hering
 */
public class DrawingOverlay extends ManagedOverlay {

	private static final String TAG = "DrawingOverlay";
	private boolean rendersPaths;
	private DrawingObjectsStorage drawingObjects;

	protected DrawingOverlay(OverlayManager overlayMgr, String name, Drawable defaultMarker, int showAtLevel) {
		super(overlayMgr, name, defaultMarker, showAtLevel);
		overlayMgr.addOverlay(this, false, false);
		rendersPaths = true;
		drawingObjects = DrawingObjectsStorage.getInstance();
		initGestureListener();
	}

	private void initGestureListener() {
		setOnOverlayGestureListener(new ManagedOverlayGestureDetector.OnOverlayGestureListener() {
			@Override
			public boolean onZoom(ZoomEvent zoom, ManagedOverlay overlay) {
				return false;
			}

			@Override
			public boolean onDoubleTap(MotionEvent e, ManagedOverlay overlay, GeoPoint point, ManagedOverlayItem item) {
				//MapController ctrl = MapDrawExtension.this.mapView.getController();
				//ctrl.animateTo(point);
				//ctrl.zoomIn();
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
			public boolean onSweep(MotionEvent motionEvent, ManagedOverlay overlay) {
				return false;
			}

			@Override
			public boolean onPress(MotionEvent motionEvent, ManagedOverlay overlay) {
				return false;
			}
			
			@Override
			public boolean onRelease(MotionEvent motionEvent, ManagedOverlay overlay) {
				return false;
			}
		});
	}

	/**
	 * Overrides the draw method for this overlay to display the drawing shapes.
	 * @param canvas the canvas of the overlay to draw on
	 * @param mapView the underlying map view
	 * @param shadow if true, draw the shadow layer. If false, draw the overlay contents.
	 */
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow, true); // possibly needed at the end of this method
		
		// draw all completed paths in this map overlay when moving of the map is possible (drawing tool not selected)
		if (rendersPaths) {
			drawingObjects.draw(canvas);
		}
		
		/*
		LinkedList<PointF> points = new LinkedList<PointF>();
		points.add(new PointF(0,0));
		points.add(new PointF(mapView.getWidth() / 2, mapView.getHeight()));
		points.add(new PointF(mapView.getWidth(), 0));
		points.add(new PointF(30,100));
		points.add(new PointF(100,30));
		points.add(new PointF(50, 200));
		points.add(new PointF(210, 220));		
		canvas.drawPath(PathConverter.getPath(points, PathConverter.PATH_TYPE_CUBIC_BEZIER, 100), currentPaint);
		
		Paint cp = new Paint(currentPaint);
		int i = 0;
		for (PointF p : PathBuilder.controlPoints) {
			if (((i % 4) == 0) || ((i % 4) == 1)) cp.setARGB(255, 255, 0, 0);
			else cp.setARGB(255, 255, 128, 0);
			canvas.drawCircle(p.x, p.y, 1, cp);
			i++;
		}
		
		Paint sp = new Paint(currentPaint);
		sp.setARGB(255, 0, 255, 0);
		i = 0;
		for (PointF p : PathBuilder.samplingPoints) {
			canvas.drawCircle(p.x, p.y, 1, sp);
			i++;
		}*/
	}
	
	/**
	 * Prepares the overlay for drawing (e.g. populate() has to be called initially if there are no items to display)
	 */
	public void prepare() {
		populate();
	}
	
	public boolean isRendersPaths() {
		return rendersPaths;
	}

	public void setRendersPaths(boolean drawThisOverlay) {
		this.rendersPaths = drawThisOverlay;
	}
}
