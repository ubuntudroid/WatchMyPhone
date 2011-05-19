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

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import com.google.android.maps.GeoPoint;

public class GeoPath extends DrawingObject {

	private ArrayList<GeoPoint> geoPoints;
	
	public GeoPath(ArrayList<GeoPoint> geoPoints, Paint paint) {
		super(paint);
		this.geoPoints = geoPoints;
	}
	
	@Override
	public void draw(Canvas canvas) {
		// TODO enable / disable bezier curve rendering in options
		Path path = PathBuilder.getPath(
				GeoHelper.getScreenPoints(geoPoints, DrawingObjectsStorage.getInstance().getMapView()), 
				PathBuilder.PATH_TYPE_LINEAR, 100);
		canvas.drawPath(path, paint);
	}

	public ArrayList<GeoPoint> getGeoPoints() {
		return geoPoints;
	}
}
