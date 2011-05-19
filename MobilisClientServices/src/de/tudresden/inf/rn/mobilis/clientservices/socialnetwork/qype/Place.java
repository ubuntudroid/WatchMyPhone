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
package de.tudresden.inf.rn.mobilis.clientservices.socialnetwork.qype;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 
 * @author Robert Lübke
 *
 */
public class Place implements Parcelable{
	
	private String id, title;
	private String created, point;
	private double latitude, longitude;	
	private Address address;
	private String phone;
	private boolean closed;
	private int average_rating;
	
	
	public static final Parcelable.Creator<Place> CREATOR = new Parcelable.Creator<Place>() {
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        public Place[] newArray(int size) {
            return new Place[size];
        }
    };
    
    public Place() {		
	}
	
	public Place(Parcel in) {
		readFromParcel(in);
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(title);
		dest.writeString(created);
		dest.writeString(point);
		dest.writeDouble(latitude);
		dest.writeDouble(longitude);
		dest.writeParcelable(address, flags);
	}
	
	public void readFromParcel(Parcel in) {
        id = in.readString();
        title = in.readString();
        created = in.readString();
        point = in.readString();        
        latitude = in.readDouble();
        longitude = in.readDouble(); 
        address = in.readParcelable(Address.class.getClassLoader());
    }
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	//Getter & Setter
		
	public String getId() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getCreated() {
		return created;
	}
	
	public String getPoint() {
		return point;
	}
	
	public double getLatitutde() {
		return latitude;		
	}
	
	public double getLongitude() {
		return longitude;		
	}	
	
	public Address getAddress() {
		return address;
	}
	
	public void setLatitutde(double latitude) {
		this.latitude = latitude;		
	}
	
	public void setLongitude(double longitude) {
		this.longitude = longitude;		
	}
}
