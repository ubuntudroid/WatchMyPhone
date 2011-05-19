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
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;

public class ManagedOverlayGestureDetector extends GestureDetector {

    public static final String LOG_TAG = "ManagedOverlayGestureDetector";

    private ManagedOverlay overlay;
    private OnOverlayGestureListener overlayOnGestureListener;
    private OnGestureListener onGestureListener;
    private ManagedGestureListener managedGestureListener;

    protected GeoPoint lastTapPoint = null;
    protected ManagedOverlayItem lastTappedOverlayItem;
    protected MotionEvent longPressMotionEvent;
    protected ZoomEvent zoomEvent;
    protected boolean inLongPress;
    protected boolean inLongPressMoved;
    protected boolean inMoving;
    protected MotionEvent movingMotionEvent;
    protected MotionEvent movingMotionEvent1;
    protected float movingV;
    protected float movingV1;

    public ManagedOverlayGestureDetector(ManagedGestureListener managedGestureListener, ManagedOverlay overlay, Handler handler) {
        this(null, managedGestureListener, overlay, handler);
    }

    public ManagedOverlayGestureDetector(ManagedGestureListener managedGestureListener, ManagedOverlay overlay) {
        this(null, managedGestureListener, overlay, null);
    }

    public ManagedOverlayGestureDetector(Context context, ManagedGestureListener managedGestureListener, ManagedOverlay overlay) {
        this(context, managedGestureListener, overlay, null);
    }

    public ManagedOverlayGestureDetector(Context context, ManagedGestureListener managedGestureListener, ManagedOverlay overlay, Handler handler) {
        super(context, managedGestureListener, handler);
        this.overlay = overlay;
        this.managedGestureListener = managedGestureListener;
        this.managedGestureListener.setDetector(this);
    }

    public boolean invokeZoomEvent(int lastZoomLevel, int zoomLevel) {
        if (overlayOnGestureListener != null || overlay.isLazyLoadEnabled) {
            ZoomEvent zoomEvent = new ZoomEvent();
            zoomEvent.setEventTime(System.currentTimeMillis());
            zoomEvent.setZoomLevel(zoomLevel);
            if (lastZoomLevel > zoomLevel)
                zoomEvent.setAction(ZoomEvent.ZOOM_OUT);
            else
                zoomEvent.setAction(ZoomEvent.ZOOM_IN);
            this.zoomEvent = zoomEvent;
            //overlay.zoomFinished = true;
            invokeZoomFinished();
        }
        return true;
    }

    protected void invokeZoomFinished() {
        if (this.getOverlayOnGestureListener() != null) {
            this.overlayOnGestureListener.onZoom(zoomEvent, this.overlay);
            this.resetState();
            this.overlay.invokeLazyLoad(1000);
        }
    }

    protected void invokeLongPressFinished() {
        if (this.getOverlayOnGestureListener() != null) {
            this.getOverlayOnGestureListener().onLongPressFinished(longPressMotionEvent, overlay, lastTapPoint, lastTappedOverlayItem);
            if(this.inLongPressMoved)
                overlay.invokeLazyLoad(0);
            this.resetState();
          
        }
    }

    public OnOverlayGestureListener getOverlayOnGestureListener() {
        return overlayOnGestureListener;
    }

    public void setOverlayOnGestureListener(OnOverlayGestureListener overlayOnGestureListener) {
        this.overlayOnGestureListener = overlayOnGestureListener;
    }

    public OnGestureListener getOnGestureListener() {
        return onGestureListener;
    }

    public void setOnGestureListener(OnGestureListener onGestureListener) {
        this.onGestureListener = onGestureListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
    	int action = motionEvent.getAction();
    	OnOverlayGestureListener gestureListener = overlayOnGestureListener; // accessing local variables is faster than accessing object fields
    	boolean handled = false;
    	if (gestureListener  != null) {
    	/*
    	if (inLongPress && action == MotionEvent.ACTION_MOVE) {
    		inLongPressMoved = true;
    		if (gestureListener != null) {
    			handled = gestureListener.onSweepInLongPress(motionEvent, overlay);
    		}
    	} else */
    	if (action == MotionEvent.ACTION_MOVE) {
    		handled = gestureListener.onSweep(motionEvent, overlay);
    	} else if (action == MotionEvent.ACTION_DOWN) {
    		handled = gestureListener.onPress(motionEvent, overlay);
    	} else if (action == MotionEvent.ACTION_UP) {
    			handled = gestureListener.onRelease(motionEvent, overlay);
    	} else if (inMoving && action == MotionEvent.ACTION_UP) { 
    			handled = gestureListener.onScrolled(movingMotionEvent, movingMotionEvent1, movingV, movingV1, overlay);
    			inMoving = false;
    			resetState();
    			overlay.invokeLazyLoad(0);
    	} else if (inLongPress && !inMoving && action == MotionEvent.ACTION_UP) {
    		overlay.longPressFinished = true;
    	}
    	}
    	if (!handled) {
    		return super.onTouchEvent(motionEvent);
    	} else {
    		return true;
    	}
    }

    protected void onTap(GeoPoint p) {
        this.lastTapPoint = p;
    }

    protected boolean onTap(int index) {
        this.lastTappedOverlayItem = overlay.getItem(index);
        return true;
    }

    protected boolean resetState() {
        if (isReset())
            return false;
        //this.lastTapPoint = null;
        this.lastTappedOverlayItem = null;
        this.inLongPress = false;
        this.inLongPressMoved = false;
        this.inMoving = false;
        this.zoomEvent = null;
        this.longPressMotionEvent = null;
        this.inLongPressMoved = false;
        return true;
    }

    protected boolean isReset() {
        return (this.lastTappedOverlayItem == null && this.lastTapPoint == null);
    }

    public static class ManagedGestureListener extends GestureDetector.SimpleOnGestureListener {
        public static final String LOG_TAG = "ManagedGestureListener";
        protected ManagedOverlay overlay;
        protected ManagedOverlayGestureDetector detector;

        public ManagedGestureListener(ManagedOverlay overlay) {
            super();
            this.overlay = overlay;
        }

        public void setDetector(ManagedOverlayGestureDetector detector) {
            this.detector = detector;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (!detector.inMoving) {
                detector.inLongPress = true;
                detector.longPressMotionEvent = e;
            }
            if (detector.getOnGestureListener() != null)
                detector.getOnGestureListener().onLongPress(e);
            super.onLongPress(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (detector.getOverlayOnGestureListener() != null)
                detector.getOverlayOnGestureListener().onSingleTap(e, overlay, detector.lastTapPoint, detector.lastTappedOverlayItem);
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (detector.getOnGestureListener() != null)
                detector.getOnGestureListener().onSingleTapUp(e);
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (detector.getOverlayOnGestureListener() != null) {
                detector.getOverlayOnGestureListener().onDoubleTap(e, overlay, detector.lastTapPoint, detector.lastTappedOverlayItem);
            }
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return super.onDoubleTapEvent(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            detector.resetState();
            if (detector.getOnGestureListener() != null)
                detector.getOnGestureListener().onDown(e);
            return super.onDown(e);
        }

        @Override
        public void onShowPress(MotionEvent e) {
            if (detector.getOnGestureListener() != null)
                detector.getOnGestureListener().onShowPress(e);
            if (detector.getOverlayOnGestureListener() != null)
                detector.getOverlayOnGestureListener().onLongPress(e, overlay);
            super.onShowPress(e);
        }

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            detector.inMoving = true;
            detector.inLongPress = false;
            detector.movingMotionEvent = motionEvent;
            detector.movingMotionEvent1 = motionEvent1;
            detector.movingV = v;
            detector.movingV1 = v1;
            if (detector.getOnGestureListener() != null)
                detector.getOnGestureListener().onScroll(motionEvent, motionEvent1, v, v1);
            return super.onScroll(motionEvent, motionEvent1, v, v1);
        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            if (detector.getOnGestureListener() != null)
                detector.getOnGestureListener().onFling(motionEvent, motionEvent1, v, v1);
            return super.onFling(motionEvent, motionEvent1, v, v1);
        }
    }

    public static interface OnOverlayGestureListener {

        public boolean onZoom(ZoomEvent zoom, ManagedOverlay overlay);

		public boolean onDoubleTap(MotionEvent e, ManagedOverlay overlay, GeoPoint point, ManagedOverlayItem item);

        public void onLongPress(MotionEvent e, ManagedOverlay overlay);

        public void onLongPressFinished(MotionEvent e, ManagedOverlay overlay, GeoPoint point, ManagedOverlayItem item);

        public boolean onScrolled(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY, ManagedOverlay overlay);

        public boolean onSingleTap(MotionEvent e, ManagedOverlay overlay, GeoPoint point, ManagedOverlayItem item);
        
        public boolean onSweep(MotionEvent e, ManagedOverlay overlay);
        
		public boolean onPress(MotionEvent motionEvent, ManagedOverlay overlay);
        
		public boolean onRelease(MotionEvent motionEvent, ManagedOverlay overlay);
    }
}
