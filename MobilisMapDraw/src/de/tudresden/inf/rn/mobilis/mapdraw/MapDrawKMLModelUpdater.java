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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import android.graphics.Color;

import com.google.android.maps.GeoPoint;

import de.hdm.cefx.concurrency.operations.NodePosition;
import de.hdm.cefx.util.XMLHelper;
import de.tudresden.inf.rn.mobilis.services.cores.MapDrawKMLDocumentBuilder;

public class MapDrawKMLModelUpdater extends ModelUpdater {
	
	private Node extendedData;
	
	@Override
	public boolean prepareEditing() {
		if (super.prepareEditing()) {
			if (extendedData == null) {
				findMapDrawExtendedData();
			}
			if (extendedData != null) {
				return true;
			} else return false;
		} else return false;
	}

	/**
	 * Adds a GeoPath (a list of GeoPoints with a Paint) to the data model
	 * (the DOM Document). In collaborative drawing, the order of elements does not matter.
	 * That is why it is added at the end of the list as a new KML Placemark containing
	 * a LineStyle and LineString.
	 * @param geoPath the GeoPath to add to the XML document
	 */
	public void addGeoPath(GeoPath geoPath) {
		if (prepareEditing()) {
			Element newPlacemark = createKMLGeoPath(geoPath);
			collabEditingService.insertNode(
					extendedData.getParentNode(), newPlacemark, 
					extendedData, NodePosition.INSERT_BEFORE);
//			collabEditingService.showDocument();
		}
	}

	private void findMapDrawExtendedData() {
		// looks for the MapDraw folder
		Node mapDrawFolder = null;
		NodeList folders = doc.getElementsByTagName("Folder");
		for (int i = 0; i < folders.getLength(); i++) {
			Node node = folders.item(i);
			Element folderName = XMLHelper.findFirstChildElement(node, "name");
			if ((folderName != null) &&
				(XMLHelper.getElementText(folderName).
						equals(MapDrawKMLDocumentBuilder.FOLDER_NAME))) {
				mapDrawFolder = node;
				break;
			}
		}
		// looks inside the folder for the extended data tag
		if (mapDrawFolder != null) {
			extendedData = XMLHelper.findFirstChildElement(mapDrawFolder, "ExtendedData");
			//extendedDataID = extE.getAttributeNS(CEFXUtil.CEFX_NAMESPACE, CEFXUtil.CEFXUID);
		}
	}
	
	private Element createKMLGeoPath(GeoPath geoPath) {
		
		Element placemarkE = doc.createElement("Placemark");
		Element nameE = doc.createElement("name");
		Text nameT = doc.createTextNode("GeoPath");
		nameE.appendChild(nameT);
		placemarkE.appendChild(nameE);
		
		Element styleE = doc.createElement("Style");
		Element lineStyleE = doc.createElement("LineStyle");
		Element colorE = doc.createElement("color");
		
		int color = geoPath.getPaint().getColor();
		String kmlColor = getKMLHexColorString(color);
		Text colorT = doc.createTextNode(kmlColor);
		colorE.appendChild(colorT);
		lineStyleE.appendChild(colorE);
		
		Element widthE = doc.createElement("width");
		float width = geoPath.getPaint().getStrokeWidth();
		Text widthT = doc.createTextNode(Float.toString(width));
		widthE.appendChild(widthT);
		lineStyleE.appendChild(widthE);
		styleE.appendChild(lineStyleE);
		placemarkE.appendChild(styleE);
		
		Element lineE = doc.createElement("LineString");
		Element tesE = doc.createElement("tessellate");
		Text tesT = doc.createTextNode("1");
		tesE.appendChild(tesT);
		lineE.appendChild(tesE);
		Element coE = doc.createElement("coordinates");
		
		String coordinates = "";
		ArrayList<GeoPoint> points = geoPath.getGeoPoints();
		for (GeoPoint p : points) {
			double lon = ((double)p.getLongitudeE6()) / 1E6;
			double lat = ((double)p.getLatitudeE6()) / 1E6;
			coordinates += Double.toString(lon) + "," + Double.toString(lat) + ",0 ";
		}
		
		Text coT = doc.createTextNode(coordinates);
		coE.appendChild(coT);
		lineE.appendChild(coE);
		placemarkE.appendChild(lineE);
		return placemarkE;
	}
	
	private String getKMLHexColorString(int color) {
		int alpha = Color.alpha(color);
		int blue = Color.blue(color);
		int green = Color.green(color);
		int red = Color.red(color);
		return makeTwoDigits(Integer.toHexString(alpha))
			+ makeTwoDigits(Integer.toHexString(blue))
			+ makeTwoDigits(Integer.toHexString(green))
			+ makeTwoDigits(Integer.toHexString(red));
	}

	private String makeTwoDigits(String str) {
		if (str.length() == 1) {
			str = "0" + str;
		}
		return str;
	}
}
