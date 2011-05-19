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
package de.tudresden.inf.rn.mobilis.android.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Useful static functions to access from everywhere :)
 * @author Dirk
 */
public class Util {
    
    /**
     * Shows a simple alert dialog with an okay button. 
     * @param context the context (Activity), which should manage the dialog
     * @param title the dialog title
     * @param message the message to display in the dialog
     */
    public static void showAlertDialog(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int button) {
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
    }
    
    /**
     * Returns a JabberID string, built from a user name, service name (computer name) and
     * a resource name (i.e. Android). All strings will be set to lowercase and 
     * blank spaces will be removed. 
     * @param name the user name, 
     * @param service the service name
     * @param resource the resource name
     * @return String - the built JabberID from the given params
     */
    public static String getJabberID(String name, String service, String resource) {
    	// TODO use smack StringUtils
        if ((name == null) || (service == null)) return null;
        String tempName = name.replaceAll(" ", "").toLowerCase();
        String tempService = service.replaceAll(" ", "").toLowerCase();
        if (resource != null) {
            String tempRes = service.replaceAll(" ", "").toLowerCase();
            return tempName + "@" + tempService + "/" + tempRes;
        } else {
            return tempName + "@" + tempService;
        }
    }
    
    /**
     * Returns only the name part of the jabber ID.
     * @param fullJabberID
     * @return String
     */
    public static String getNameFromJabberID(String fullJabberID) {
    	// TODO use smack StringUtils
        if (fullJabberID == null) return "";
    	int index = fullJabberID.indexOf('@');
        if (index < 0) return fullJabberID;
        if (index == 0) return "";
        else {
            return fullJabberID.substring(0, index);
        }
    }
}
