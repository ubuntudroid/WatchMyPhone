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
import java.util.List;

import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;

public class GeoHelper {

	public static ArrayList<GeoPoint> getGeoPoints(List<Point> screenPoints, MapView mapView) {
		ArrayList<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
		Projection projection = mapView.getProjection();
		for (Point p : screenPoints) {
			geoPoints.add(projection.fromPixels(p.x, p.y));
		}
		return geoPoints;
	}
	
	public static ArrayList<Point> getScreenPoints(List<GeoPoint> geoPoints, MapView mapView) {
		ArrayList<Point> screenPoints = new ArrayList<Point>();
		Projection projection = mapView.getProjection();
		for (GeoPoint p : geoPoints) {
			screenPoints.add(projection.toPixels(p, null));
		}
		return screenPoints;
	}
	
}
