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

import jabberSrpc.JabberClient;

import java.util.Random;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.IQ;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.google.android.maps.MapView;

import de.hdm.cefx.awareness.AwarenessController;
import de.tudresden.inf.rn.mobilis.android.services.CollabEditingService;
import de.tudresden.inf.rn.mobilis.android.services.SessionService;
import de.tudresden.inf.rn.mobilis.mapdraw.overlaymgr.ManagedOverlay;
import de.tudresden.inf.rn.mobilis.mapdraw.overlaymgr.OverlayManager;
import de.tudresden.inf.rn.mobilis.xmpp.packet.MonitoringIQ;

/**
 * The map extension allows the user to draw items (e.g. lines) on map overlays and therefore it provides drawing UI controls 
 * (extra buttons) on the map. This component comprises a layer architecture for listening to user interface events and rendering
 * drawing objects. It also has a data model updater (MapDrawKMLModelUpdater) to take over the users drawings into the xml model and for
 * the opposite direction, a MapDrawViewUpdater (AndroidCEFX AwarenessWidget) translates data model changes into new DrawingObjects to be 
 * displayed on the map.
 * @author Dirk Hering
 */
public class MapDrawExtension {

	private static final String TAG = "MapDraw";
	
	private Context context;
	private MapView mapView;
	private View toolButtons;
	private OverlayManager overlayManager;
	private DrawingOverlay drawingOverlay;
	private BackgroundOverlay backgroundOverlay;
	private CachingLayer cachingLayer;
	private PrerenderingLayer prerenderingLayer;
	private boolean panningEnabled;
	private boolean drawingEnabled;
	private boolean cachingOnDrawing;
	private PointCollector pointCollector;
	private Paint currentPaint;
	private DrawingObjectsStorage drawingObjects;
	private MapDrawKMLModelUpdater modelUpdater;
	private MapDrawViewUpdater mapDrawViewUpdater;
	
	//services
	private CollabEditingService collabEditingService;
	
	/**
	 * The listener for motion events like press, move, release
	 */
	private OnTouchListener touchListener = new OnTouchListener() {
		
		// actions taken after user interaction with the map extension, depending on currently activated drawing tool
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {

			int action = event.getAction();
			
			if (action == MotionEvent.ACTION_MOVE) {
				
				if (!panningEnabled) {
					if (drawingEnabled) {
						// add the current point to the prerendered path
						//Log.i(TAG, "gesture consumed");
						pointCollector.addPoint(event.getX(), event.getY(), true);
						Path prerenderedPath = pointCollector.getCurrentPath(PathBuilder.PATH_TYPE_LINEAR, 0);
						prerenderingLayer.setPrerenderedPath(prerenderedPath);
						prerenderingLayer.invalidate();
						return true; // sweep event got already handled, do not pass it to the underlying map (panning disabled)
					} else {
						// do something when panning only is disabled ...
						return true; // move event got already handled, do not pass it to the underlying map (panning disabled)
					}
				} else {
					return false;
				}
				
			} else if (action == MotionEvent.ACTION_DOWN) {
				
				if (drawingEnabled) {
					// begin a new path
					pointCollector.addPoint(event.getX(), event.getY(), true);
					prerenderingLayer.setCurrentPaint(currentPaint);
					return true;
				} else {
					return false;
				}
				
			} else if (action == MotionEvent.ACTION_UP) {
				
				if (drawingEnabled) {
					PointCollector pc = pointCollector;
					if (pc.containsMultiplePoints()) {
						
						// invokes the final path calculation and adds it to the drawing objects
						pc.finishPath(true, false);
						GeoPath geoPath = new GeoPath(pc.getCurrentGeoPoints(mapView), currentPaint);
						drawingObjects.addDrawingObject(geoPath);
						
						// updates the underlying data model (inserts node in DOM Document)
						modelUpdater.addGeoPath(geoPath);
						
						// redrawing
						prerenderingLayer.clear();
						redraw();
					}
					pc.clear();
				}
				return false;
			}
			
			// pass through any other event
			return false;
		}
	};
	
	public MapDrawExtension(Context context, MapView mapView) {
		this.mapView = mapView;
		this.context = context;
		
		collabEditingService = SessionService.getInstance().getCollabEditingService();
		mapDrawViewUpdater = new MapDrawViewUpdater(this);
		AwarenessController ac = collabEditingService.getAwarenessController();
		ac.registerWidget(mapDrawViewUpdater);
		modelUpdater = new MapDrawKMLModelUpdater();
		
		pointCollector = new PointCollector();
		currentPaint = PaintFactory.getDefaultPaint();
		drawingObjects = DrawingObjectsStorage.getInstance();
		drawingObjects.setMapView(mapView);
		
		addDrawingControls(mapView);
		
		overlayManager = new OverlayManager(context, mapView);
		Drawable itemPic = context.getResources().getDrawable(R.drawable.ic_maps_indicator_current_position);
		backgroundOverlay = new BackgroundOverlay(overlayManager, "backgroundOverlay", itemPic, 1);
		drawingOverlay = new DrawingOverlay(overlayManager, "drawingOverlay", itemPic, 2);
		//prerenderingOverlay = new PrerenderingOverlay(overlayManager, "PrerenderingOverlay", itemPic, 3);

		//OverlayItem item = new OverlayItem(new GeoPoint(19240000,-99120000), "", "");
		//drawingOverlay.addItem(item);
		//for (int i = 0; i < 40; i = i + 3) {
		//	managedOverlay.createItem(GeoHelper.geopoint[i], "Item" + i);
		//}
	}
	
	/**
	 * Adds a layer for consuming gesture events in drawing mode, an additional prerendering layer and drawing tool buttons.
	 * @param mapView the MapView for displaying the buttons upon
	 */
	private void addDrawingControls(MapView mapView) {

		// control layers: buttons -> gestureConsumer -> prerendering -> caching -> mapview: drawingOverlay -> backGroundOverlay -> map
		
		FrameLayout controlLayers = new FrameLayout(context);
		MapView.LayoutParams controlLayersLayoutParams = new MapView.LayoutParams(
				LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT,
				0, 0, MapView.LayoutParams.TOP_LEFT);
		
		// add caching layer
		cachingLayer = new CachingLayer(context);
		cachingLayer.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		controlLayers.addView(cachingLayer);
		
		// add prerendering layer
		prerenderingLayer = new PrerenderingLayer(context);
		prerenderingLayer.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		controlLayers.addView(prerenderingLayer);
		
		// add gesture consumer layer
		View gestureConsumer = new View(context);
		gestureConsumer.setOnTouchListener(touchListener);
		gestureConsumer.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		controlLayers.addView(gestureConsumer);
		
		// add drawing tool buttons
		toolButtons = View.inflate(context, R.layout.tool_buttons, controlLayers);
		toolButtons.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		mapView.addView(controlLayers, controlLayersLayoutParams);
		
		
		// retrieving the button for toggling drawing mode
		CheckBox btnDraw = (CheckBox) toolButtons.findViewById(R.id.btn_draw);
		btnDraw.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setDrawingEnabled(isChecked);
				setCachingOnDrawing(isChecked);
			}
		});
		
		Button btnClear = (Button) toolButtons.findViewById(R.id.btn_clear);
		btnClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				clearDrawings();
				getMapView().invalidate();
			}
		});
		
		CheckBox btnWhite = (CheckBox) toolButtons.findViewById(R.id.btn_white);
		btnWhite.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					setOverlayVisible(backgroundOverlay, true, true);
				} else {
					setOverlayVisible(backgroundOverlay, false, true);
				}
			}
		});
		
		Button btnCEFX = (Button) toolButtons.findViewById(R.id.btn_test_cefx);
		btnCEFX.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (collabEditingService.isConnected()) {
					CollabEditingTester cet = new CollabEditingTester(collabEditingService);
					cet.test2();
					mapDrawViewUpdater.readKMLDocument(collabEditingService.getDocument());
				}
			}
		});
		
		final MapView map = mapView;
		Button btnPing = (Button) toolButtons.findViewById(R.id.btn_ping);
		btnPing.setVisibility(View.GONE);
		btnPing.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (collabEditingService.isConnected()) {
					
					// Testing Ping
//					String monFile = "pings.txt";
//					Monitoring mon = Monitoring.get();
//					mon.init(monFile, context);
//					for (int i = 0; i < 100; i++) {
//						mon.ping("cefxu1@wpc/MobilisClient", null);
//						try {
//							Thread.sleep(2000);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//					}
//					mon.stop();
					
					
//					// Testing Feedback
//					String monFile = "feedback.txt";
//					Monitoring mon = Monitoring.get();
//					mon.init(monFile, context);
//					
//					PointCollector pc = pointCollector;
//					int width = map.getWidth();
//					int height = map.getHeight();
//					Random random = new Random(12345);
//					
//					for (int i = 0; i < 40; i++) {
//						for (int j = 0; j < 6; j++) {
//							pc.addPoint(random.nextInt(width), random.nextInt(height), false);
//						}
//						pc.finishPath(false, false);
//
//						mon.startTimer();
//						GeoPath geoPath = new GeoPath(pc.getCurrentGeoPoints(map), currentPaint);
//						drawingObjects.addDrawingObject(geoPath);
//						
//						// updates the underlying data model (inserts node in DOM Document)
//						modelUpdater.addGeoPath(geoPath);
//						
//						// clearing and redrawing
//						pc.clear();
//						prerenderingLayer.clear();
//						redraw();
//						mon.endTimer(true);
//						
//						try {
//							Thread.sleep(2000);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//					}
//					mon.postStop();
					
					
					
					// Testing feedback locally, feedthrough at remote site
					String monFile = "feedback.txt";
					Monitoring mon = Monitoring.get();
					mon.init(monFile, context);
					
					PointCollector pc = pointCollector;
					int width = map.getWidth();
					int height = map.getHeight();
					Random random = new Random(12345);
					
					for (int i = 0; i < 50; i++) {
						for (int j = 0; j < 6; j++) {
							pc.addPoint(random.nextInt(width), random.nextInt(height), false);
						}
						pc.finishPath(false, false);

						// start local feedback measurement
						mon.startTimer();
						// inform remote site about processing begin
						XMPPConnection xmpp = JabberClient.getInstance().getJabberConnection();
						MonitoringIQ monIQ = new MonitoringIQ();
						monIQ.setStatusMsg(MonitoringIQ.START_TIMER);
						monIQ.setType(IQ.Type.GET);
						monIQ.setTo("cefxu1@wpc/MobilisClient");
						xmpp.sendPacket(monIQ);
						
						GeoPath geoPath = new GeoPath(pc.getCurrentGeoPoints(map), currentPaint);
						drawingObjects.addDrawingObject(geoPath);
						
						// updates the underlying data model (inserts node in DOM Document)
						modelUpdater.addGeoPath(geoPath);
						
						// clearing and redrawing
						pc.clear();
						prerenderingLayer.clear();
						redraw();
						mon.endTimer(true);
						
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					mon.postStop();
					
				}
			}
		});
	}

	/**
	 * Invalidates and redraws the caching layer and overlays
	 */
	public void redraw() {
		if (cachingOnDrawing) {
			cachingLayer.postInvalidate(); // redraw it
		} else {
			mapView.postInvalidate(); // redraws all maps overlays
		}
	}
	
	/**
	 * Returns the underlying MapView.
	 * @return MapView
	 */
	public MapView getMapView() {
		return mapView;
	}

	/**
	 * Convenience method for displaying the drawing controls and the drawings on the map.
	 */
	public void show() {
		setControlsVisible(true);
		setOverlayVisible(backgroundOverlay, false, false);
		setOverlayVisible(drawingOverlay, true, false);
		//setOverlayVisible(prerenderingOverlay, true, false);
		overlayManager.populate();
	}

	/**
	 * Defines if the drawing controls should be currently shown.
	 * @param visible if true, the drawing buttons are shown on the map
	 */
	private void setControlsVisible(boolean visible) {
		if (visible) toolButtons.setVisibility(View.VISIBLE);
		else toolButtons.setVisibility(View.INVISIBLE);
	}

	/**
	 * Defines if overlay should be currently shown.
	 * @param visible if true, the overlay is shown on the map
	 */
	private void setOverlayVisible(ManagedOverlay overlay, boolean visible, boolean refresh) {
		if (visible) {
			overlayManager.addOverlay(overlay, true, refresh);
		} else {
			overlayManager.removeOverlay(overlay, refresh);
		}
	}

	public Paint getCurrentPaint() {
		return currentPaint;
	}

	public void setCurrentPaint(Paint currentPaint) {
		this.currentPaint = currentPaint;
	}
	
	public void clearDrawings() {
		drawingObjects.clear();
		prerenderingLayer.clear();
		prerenderingLayer.invalidate();
		cachingLayer.invalidate();
		overlayManager.populate();
	}
	
	/**
	 * Defines if the panning of the underlying map should be allowed or not.
	 * @param enabled if true, the panning of the underlying map is enabled
	 */
	public void setPanningEnabled(boolean enabled) {
		panningEnabled = enabled;
	}
	
	public boolean isPanningEnabled() {
		return panningEnabled;
	}

	/**
	 * Enables or disables drawing on the map.
	 * @param drawingEnabled if true, drawing is allowed and panning will be disabled
	 */
	public void setDrawingEnabled(boolean drawingEnabled) {
		this.drawingEnabled = drawingEnabled;
		setPanningEnabled(!drawingEnabled);
	}

	public boolean isDrawingEnabled() {
		return drawingEnabled;
	}
	
	public boolean isCachingOnPrerendering() {
		return cachingOnDrawing;
	}

	public void setCachingOnDrawing(boolean cachingOnPrerendering) {
		this.cachingOnDrawing = cachingOnPrerendering;
		drawingOverlay.setRendersPaths(!cachingOnPrerendering);
		cachingLayer.setRendersPaths(cachingOnPrerendering);
		// refill or clear the drawing cache
		cachingLayer.invalidate();
	}
}
