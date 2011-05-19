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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.graphics.Color;
import android.graphics.Paint;

import com.google.android.maps.GeoPoint;

import de.hdm.cefx.awareness.AwarenessEvent;
import de.hdm.cefx.concurrency.operations.InsertOperationImpl;
import de.hdm.cefx.util.CEFXUtil;
import de.hdm.cefx.util.XMLHelper;
import de.tudresden.inf.rn.mobilis.android.services.CollabEditingService;
import de.tudresden.inf.rn.mobilis.android.services.SessionService;

public class MapDrawViewUpdater extends ViewUpdater {
	
	private MapDrawExtension mapDrawView;
	
	public MapDrawViewUpdater(MapDrawExtension mapDraw) {
		mapDrawView = mapDraw;
	}
	
	@Override
	public void notifyOfAwarenessEvent(AwarenessEvent event) {
		
		CollabEditingService collabEditingService = SessionService.getInstance().getCollabEditingService();
		
		// process INSERT operations
		if (operation instanceof InsertOperationImpl) {
			
			// Operation has stored a copy of the inserted node
			InsertOperationImpl ins = (InsertOperationImpl) operation;
			String nodeID = CEFXUtil.getNodeId(ins.getInsertNode());
			// needed to retrieve the node from the local document by its ID
			Element newElement = collabEditingService.getElementForId(nodeID);
			
			// process inserted KML Placemarks
			if (newElement.getNodeName().equals("Placemark")) {
				createAndStoreGeoPath(newElement);
			}
		}
		
		// process DOCUMENT_LOADED event
//		mapDrawViewUpdater.readKMLDocument(collabEditingService.getDocument());
		
		// Measuring Feedthrough
//		Monitoring.get().setFromRemote(true);
//		System.out.println("Redraw Remote");
		
		mapDrawView.redraw();
	}

	private int getAndroidColorInt(String kmlColor) {
		int alpha = Integer.parseInt(kmlColor.substring(0, 2), 16);
		int blue = Integer.parseInt(kmlColor.substring(2, 4), 16);
		int green = Integer.parseInt(kmlColor.substring(4, 6), 16);
		int red = Integer.parseInt(kmlColor.substring(6, 8), 16);
		return Color.argb(alpha, red, green, blue);
	}

	public void readKMLDocument(Document document) {
		NodeList nodes = document.getElementsByTagName("Placemark");
		for (int i = 0; i < nodes.getLength(); i++) {
			createAndStoreGeoPath((Element)nodes.item(i));
		}
		mapDrawView.redraw();
	}

	private void createAndStoreGeoPath(Element placemark) {
		
		// process LineStrings -> GeoPaths
		Element lineString = XMLHelper.findFirstChildElement(placemark, "LineString");
		Element coordinates = XMLHelper.findFirstChildElement(lineString, "coordinates");
		ArrayList<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
		String[] coords = XMLHelper.getElementText(coordinates).split(" ");
		for (int i = 0; i < coords.length; i++) {
			String[] coord = coords[i].split(",");
			int lon = new Double(Double.parseDouble(coord[0]) * 1E6).intValue();
			int lat = new Double(Double.parseDouble(coord[1]) * 1E6).intValue();
			geoPoints.add(new GeoPoint(lat, lon));
		}
		
		NodeList nodes = placemark.getElementsByTagName("color");
		String kmlColor = XMLHelper.getElementText((Element) nodes.item(0));
		int color = getAndroidColorInt(kmlColor);
		nodes = placemark.getElementsByTagName("width");
		float width = Float.valueOf(XMLHelper.getElementText((Element) nodes.item(0)));
		Paint paint = new Paint();
		paint.setColor(color);
		paint.setStrokeWidth(width);
		paint.setAntiAlias(false);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		
		GeoPath geoPath = new GeoPath(geoPoints, paint);
		DrawingObjectsStorage.getInstance().addDrawingObject(geoPath);
	}
	
}
