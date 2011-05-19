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
public class PrimaryCategory implements Parcelable{
	
	private int id;
	private String fullpathname;
	private String iconurl;
	
	public static final Parcelable.Creator<PrimaryCategory> CREATOR = new Parcelable.Creator<PrimaryCategory>() {
        public PrimaryCategory createFromParcel(Parcel in) {
            return new PrimaryCategory(in);
        }

        public PrimaryCategory[] newArray(int size) {
            return new PrimaryCategory[size];
        }
    };	
	
    public PrimaryCategory() {		
	}
	
	public PrimaryCategory(Parcel in) {
		readFromParcel(in);
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(fullpathname);		
		dest.writeString(iconurl);		
	}
	
	public void readFromParcel(Parcel in) {
        id = in.readInt();
        fullpathname = in.readString();
        iconurl = in.readString();        
    }
	
	@Override
	public int describeContents() {		
		return 0;
	}
	
	//Getter
	
	public int getId() {
		return id;
	}	
	
	public String getFullpathname() {
		return fullpathname;
	}
	
	public String getIconurl() {
		return iconurl;
	}

	
	
	
}
