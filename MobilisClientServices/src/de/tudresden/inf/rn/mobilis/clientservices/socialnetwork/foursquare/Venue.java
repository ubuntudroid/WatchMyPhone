/*******************************************************************************
 * Copyright (C) 2010 Technische Universität Dresden
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
package de.tudresden.inf.rn.mobilis.clientservices.socialnetwork.foursquare;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 
 * @author Robert Lübke
 *
 */
public class Venue implements Parcelable{
	
	private int id;
	private String name;
	private double geolat, geolong;
	private int distance;
	private String address, city, state;
	private PrimaryCategory primarycategory;
	
	public static final Parcelable.Creator<Venue> CREATOR = new Parcelable.Creator<Venue>() {
        public Venue createFromParcel(Parcel in) {
            return new Venue(in);
        }

        public Venue[] newArray(int size) {
            return new Venue[size];
        }
    };
    
    public Venue() {		
	}
	
	public Venue(Parcel in) {
		readFromParcel(in);
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(name);
		dest.writeDouble(geolat);
		dest.writeDouble(geolong);
		dest.writeInt(distance);
		dest.writeString(address);
		dest.writeString(city);
		dest.writeString(state);
		dest.writeParcelable(primarycategory, flags);		
	}
	
	public void readFromParcel(Parcel in) {
        id = in.readInt();
        name = in.readString();
        geolat = in.readDouble();
        geolong = in.readDouble();
        distance = in.readInt();
        address = in.readString();
        city = in.readString();
        state = in.readString();
        primarycategory = in.readParcelable(PrimaryCategory.class.getClassLoader());
    }
	
	@Override
	public int describeContents() {
		return 0;
	}

	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public double getGeolat() {
		return geolat;
	}
	
	public double getGeolong() {
		return geolong;
	}
	
	public double getDistance() {
		return distance;
	}
	
	public String getAdress() {
		return address;
	}
	
	public String getCity() {
		return city;
	}
	
	public String getState() {
		return state;
	}	
	
	public PrimaryCategory getPrimaryCategory() {
		return primarycategory;
	}
}
