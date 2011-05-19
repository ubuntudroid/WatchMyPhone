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

import java.util.LinkedList;
import java.util.List;

import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;

/**
 * Builds GeoPaths or normal Paths from lists of pixel coordinates.
 * @author Hering
 */
public class PathBuilder {

	private static final String TAG = "PathBuilder";
	public static final int PATH_TYPE_LINEAR = 1;
	public static final int PATH_TYPE_CUBIC_BEZIER = 2;
	public static LinkedList<PointF> controlPoints = new LinkedList<PointF>();
	public static LinkedList<Point> samplingPoints = new LinkedList<Point>();
	
	/**
	 * Returns a Path, drawable by an Android Canvas, either in form of connected lines (PATH_TYPE_LINEAR),
	 * or as a smooth bezier curve through the points (PATH_TYPE_CUBIC_BEZIER). Of course, the latter needs
	 * more computing resources.
	 * @param screenPoints the points to connect with a path
	 * @param pathType the type of the path (PATH_TYPE_LINEAR or PATH_TYPE_CUBIC_BEZIER)
	 * @param smoothness value range from 0..100 (sharp ... round), only affects bezier curves
	 * @return Path
	 */
	public static Path getPath(List<Point> screenPoints, int pathType, int smoothness) {
		boolean firstPoint = true;
		Path path = new Path();
		
		// debug
//		samplingPoints.addAll(screenPoints);
		
		switch(pathType) {
		
		case PATH_TYPE_LINEAR:
			
			for(Point p : screenPoints) {
				if (firstPoint) {
					path.moveTo(p.x, p.y);
					firstPoint = false;
				} else {
					path.lineTo(p.x, p.y);
				}
			}
			break;
			
		case PATH_TYPE_CUBIC_BEZIER:

			// cubic bezier is described by p0, p1, p2, p3
			// this curve goes through p0, p3, with p1 and p2 as control points
			
			// smoothness 0 = sharp, smoothness 100 = round
			float s = smoothness;
			if (s < 0) {
				s = 0;
			} else if (s > 100) {
				s = 100;
			}
			if (s > 0) {
				s = 100 / (s*3f);
			}

			int size = screenPoints.size();
			if (size < 2) {
				break;
			} else if (size == 2) {
				path.moveTo(screenPoints.get(0).x, screenPoints.get(0).y);
				path.lineTo(screenPoints.get(1).x, screenPoints.get(1).y);
			} else {

				for (int i=0; i <= size - 2; i++) {
					Point p1 = screenPoints.get(i);
					Point p2 = screenPoints.get(i+1);
					float controlDistance = s * pointDistanceApprox(p2.x, p2.y, p1.x, p1.y);
					if (i == 0) {
						// first point = first control point!
						Point p3 = screenPoints.get(i+2);
						float dist = min1(pointDistanceApprox(p3.x, p3.y, p1.x, p1.y));
						path.moveTo(p1.x, p1.y);
						path.cubicTo(
								p1.x, p1.y, 
								p2.x - (controlDistance * (p3.x - p1.x) / dist), p2.y - (controlDistance * (p3.y - p1.y) / dist), 
								p2.x, p2.y);
						// debug
//						controlPoints.add(new PointF(p2.x - controlDistance * (p3.x - p1.x) / dist, p2.y - controlDistance * (p3.y - p1.y) / dist));
					} else if (i == size - 2) {
						// last control point = last point!
						Point p0 = screenPoints.get(i-1);
						float dist = min1(pointDistanceApprox(p2.x, p2.y, p0.x, p0.y));
						path.cubicTo(
								p1.x + (controlDistance * (p2.x - p0.x) / dist), p1.y + (controlDistance * (p2.y - p0.y) / dist), 
								p2.x, p2.y, 
								p2.x, p2.y);
						// debug
//						controlPoints.add(new PointF(p1.x + controlDistance * (p2.x - p0.x) / dist, p1.y + controlDistance * (p2.y - p0.y) / dist));
					} else {
						// point with neighbors on the path -> 2 separate control points
						Point p0 = screenPoints.get(i-1);
						Point p3 = screenPoints.get(i+2);
						float dist1 = min1(pointDistanceApprox(p2.x, p2.y, p0.x, p0.y));
						float dist2 = min1(pointDistanceApprox(p3.x, p3.y, p1.x, p1.y));
						path.cubicTo(
								p1.x + (controlDistance * (p2.x - p0.x) / dist1), p1.y + (controlDistance * (p2.y - p0.y) / dist1), 
								p2.x - (controlDistance * (p3.x - p1.x) / dist2), p2.y - (controlDistance * (p3.y - p1.y) / dist2), 
								p2.x, p2.y);
						// debug
//						controlPoints.add(new PointF(p1.x + controlDistance * (p2.x - p0.x) / dist1, p1.y + controlDistance * (p2.y - p0.y) / dist1));
//						controlPoints.add(new PointF(p2.x - controlDistance * (p3.x - p1.x) / dist2, p2.y - controlDistance * (p3.y - p1.y) / dist2));
					}

				}
			}
			break;
		}
		return path;
	}

	/**
	 * Approximates the 2D distance of two points 
	 * (code from http://www.flipcode.com/archives/Fast_Approximate_Distance_Functions.shtml)
	 * @param x1 point1 x coordinate
	 * @param y1 point1 y coordinate
	 * @param x2 point2 x coordinate
	 * @param y2 point2 y coordinate
	 * @return int the approximated distance as a whole number
	 */
	public static int pointDistanceApprox(int x1, int y1, int x2, int y2) {

		int dx = x2 - x1;
		int dy = y2 - y1;
		int min, max;

		if ( dx < 0 ) dx = -dx;
		if ( dy < 0 ) dy = -dy;

		if ( dx < dy ) {
			min = dx;
			max = dy;
		} else {
			min = dy;
			max = dx;
		}

		// coefficients equivalent to ( 123/128 * max ) and ( 51/128 * min )
		return ((( max << 8 ) + ( max << 3 ) - ( max << 4 ) - ( max << 1 ) +
				( min << 7 ) - ( min << 5 ) + ( min << 3 ) - ( min << 1 )) >> 8 );
	}
	
	public static int min1(int number) {
		if (number == 0) return 1;
		else return number;
	}
	
	/**
	 * Computes the exact distance between two points - computationally intensive
	 * @param x1 point1 x coordinate
	 * @param y1 point1 y coordinate
	 * @param x2 point2 x coordinate
	 * @param y2 point2 y coordinate
	 * @return float the distance as a floating-point number
	 */
	public static float pointDistance(float x1, float y1, float x2, float y2) {
		return new Double(Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1))).floatValue();
	}
}
