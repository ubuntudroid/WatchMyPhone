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
public class Address implements Parcelable{
	
	private String country_code, postcode, housenumber, street, city;
	
	public static final Parcelable.Creator<Address> CREATOR = new Parcelable.Creator<Address>() {
        public Address createFromParcel(Parcel in) {
            return new Address(in);
        }

        public Address[] newArray(int size) {
            return new Address[size];
        }
    };	
	
    public Address() {		
	}
	
	public Address(Parcel in) {
		readFromParcel(in);
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(country_code);
		dest.writeString(postcode);		
		dest.writeString(housenumber);	
		dest.writeString(street);	
		dest.writeString(city);	
	}
	
	public void readFromParcel(Parcel in) {
        country_code = in.readString();
        postcode = in.readString();
        housenumber = in.readString();    
        street = in.readString();  
        city = in.readString();  
    }
	
	@Override
	public int describeContents() {		
		return 0;
	}
	
	//Getter
	
	public String getCountry_code() {
		return country_code;
	}
	
	public String getPostcode() {
		return postcode;
	}
	
	public String getHousenumber() {
		return housenumber;
	}
	
	public String getStreet() {
		return street;
	}
	
	public String getCity() {
		return city;
	}	
	
}
