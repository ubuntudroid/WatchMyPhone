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

import java.util.ArrayList;
import java.util.LinkedList;

import android.graphics.Path;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

/**
 * A class to build a GeoPath from a feed of screen points. Points laying too close get filtered out.
 * @author Hering
 */
public class PointCollector {
	
	private static final String TAG = "PointCollector";

	/**
	 * Minimum quadratic distance needed between the last point and the new point to be added to the path
	 */
	public static final int MIN_QUAD_DISTANCE = 100;
	private LinkedList<Point> tempScreenPoints;
	private Point lastAddedPoint;
	private Point lastCheckedPoint;

	public PointCollector() {
		tempScreenPoints = new LinkedList<Point>();
	}
	
	public void addPoint(float x, float y, boolean checkThisPoint) {
		Point newPoint = new Point(((int)(x + 0.5f)),((int)(y + 0.5f)));
		addPoint(newPoint, checkThisPoint);
	}
	
	public void addPoint(Point newPoint, boolean checkPoint) {
		if ((!checkPoint) || (lastAddedPoint == null) || (isMinimumDistance(newPoint))) {
			tempScreenPoints.add(newPoint);
			lastAddedPoint = newPoint;
		}
	}
	
	/**
	 * Checks for the quadratic distance between two points.
	 * @param point1
	 * @param point2
	 * @return true, if the points are at least MIN_DISTANCE away from each other, false otherwise
	 */
	private boolean isMinimumDistance(Point newPoint) {
		lastCheckedPoint = newPoint;
		Point lastPoint = lastAddedPoint;
		if (lastPoint != null) {
			float dx = newPoint.x - lastPoint.x;
			float dy = newPoint.y - lastPoint.y;
			return (((dx*dx)+(dy*dy)) >= MIN_QUAD_DISTANCE);
		} else {
			return true;
		}
	}
	
	/**
	 * Finishes collecting points for the path and returns it.
	 * @param checkLastPoint if true, the last checked point is taken into the path, if it isn't already there
	 * @param optimise if true, in a final step the curves are optimised, resulting in fewer points
	 * @return Path the completed path
	 */
	public void finishPath(boolean checkLastPoint, boolean optimise) {
		if (checkLastPoint) {
			// the last checked point may have been filtered out before, but the path will finish here,
			// so it should be included anyway
			if (lastCheckedPoint != lastAddedPoint) {
				addPoint(lastCheckedPoint, false);
			}
		}
		if (optimise) {
			// insert optimisation here ...
			// temporary collected points are stored in a LinkedList to perform deletions efficiently (faster than ArrayList in this case)
		}
	}
	
	public Path getCurrentPath(int pathType, int smoothness) {
		return PathBuilder.getPath(tempScreenPoints, pathType, smoothness);
	}

	public ArrayList<GeoPoint> getCurrentGeoPoints(MapView mapView) {
		return GeoHelper.getGeoPoints(tempScreenPoints, mapView);
	}
	
	public boolean containsMultiplePoints() {
		return (tempScreenPoints.size() > 1);
	}

	public void clear() {
		tempScreenPoints.clear();
		lastAddedPoint = null;
	}
}
