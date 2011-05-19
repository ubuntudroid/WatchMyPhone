/**
 * Copyright (C) 2009 Technische Universität Dresden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contains parts of Android Email App (C) 2008 The Android Open Source Project
 */

/**
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: http://mobilisplatform.sourceforge.net
 */

package de.tudresden.inf.rn.mobilis.mxa.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import de.tudresden.inf.rn.mobilis.mxa.R;

/**
 * Displays the activity for typing in the XMPP ID and its password.
 * @author Istvan Koren
 */
public class SetupBasics extends Activity implements OnClickListener,
		TextWatcher {

	private static final String TAG = "SetupBasics";

	// views
	private EditText mEdtAddress;
	private EditText mEdtPassword;
	private Button mBtnNext;

	// members
	private Server mServer;
	private SharedPreferences mPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setup_basics);

		// initialize members for UI elements.
		initResourceRefs();
	}

	// ==========================================================
	// Interface methods
	// ==========================================================

	@Override
	public void onClick(View v) {
		if (v == mBtnNext) {
			String xmppID = mEdtAddress.getText().toString().trim();
			String[] xmppIDParts = xmppID.split("@");
			String domain = xmppIDParts[1].trim();
			mServer = findServerForDomain(domain);
			if (mServer == null) {
				// no default settings available, show Preference Activity
				Intent i = new Intent(this, PreferencesClient.class);
				startActivity(i);
				return;
			}
			finishAutoSetup();
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
		validateFields();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	// ==========================================================
	// Private methods
	// ==========================================================

	/**
	 * Initialize all UI elements from resources.
	 */
	private void initResourceRefs() {
		mEdtAddress = (EditText) findViewById(R.id.setup_basics_edt_address);
		mEdtAddress.addTextChangedListener(this);
		mEdtPassword = (EditText) findViewById(R.id.setup_basics_edt_password);
		mEdtPassword.addTextChangedListener(this);
		mBtnNext = (Button) findViewById(R.id.setup_basics_btn_next);
		validateFields();
		mBtnNext.setOnClickListener(this);
	}

	/**
	 * Enables and disables the next button.
	 */
	private void validateFields() {
		// TODO validate ID
		boolean valid = (mEdtAddress.getText().length() > 2)
				&& (mEdtPassword.getText().length() > 0);
		mBtnNext.setEnabled(valid);
	}

	/**
	 * Attempts to get the given attribute as a String resource first, and if it
	 * fails returns the attribute as a simple String value.
	 * 
	 * @param xml
	 * @param name
	 * @return
	 */
	private String getXmlAttribute(XmlResourceParser xml, String name) {
		int resId = xml.getAttributeResourceValue(null, name, 0);
		if (resId == 0) {
			return xml.getAttributeValue(null, name);
		} else {
			return getString(resId);
		}
	}

	/**
	 * Looks up if there is a server in the servers.xml file for the given
	 * domain.
	 * 
	 * @param domain
	 * @return
	 */
	private Server findServerForDomain(String domain) {
		XmlResourceParser xml = getResources().getXml(R.xml.servers);
		int xmlEventType;
		Server server = null;
		try {
			while ((xmlEventType = xml.next()) != XmlResourceParser.END_DOCUMENT) {
				if (xmlEventType == XmlResourceParser.START_TAG
						&& "server".equals(xml.getName())
						&& domain.equalsIgnoreCase(getXmlAttribute(xml,
								"domain"))) {
					server = new Server();
					server.id = getXmlAttribute(xml, "id");
					server.domain = getXmlAttribute(xml, "domain");
				} else if (xmlEventType == XmlResourceParser.START_TAG
						&& "host".equals(xml.getName()) && server != null) {
					server.host = xml.nextText();
				} else if (xmlEventType == XmlResourceParser.START_TAG
						&& "port".equals(xml.getName()) && server != null) {
					server.port = xml.nextText();
				} else if (xmlEventType == XmlResourceParser.START_TAG
						&& "servicename".equals(xml.getName())
						&& server != null) {
					server.serviceName = xml.nextText();
				} else if (xmlEventType == XmlResourceParser.END_TAG
						&& "server".equals(xml.getName()) && server != null) {
					return server;
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Error while trying to load server settings.", e);
		}
		return null;
	}

	/**
	 * Saves the configuration in the Preferences file.
	 */
	private void finishAutoSetup() {
		Log.i(TAG, "Saving preference data.");

		String xmppID = mEdtAddress.getText().toString().trim();
		String password = mEdtPassword.getText().toString().trim();

		mPreferences = getSharedPreferences(
				"de.tudresden.inf.rn.mobilis.mxa_preferences",
				Context.MODE_PRIVATE);

		SharedPreferences.Editor editor = mPreferences.edit();

		editor.putString("pref_host", mServer.host);
		editor.putString("pref_service", mServer.serviceName);
		editor.putString("pref_resource", "MXA");
		editor.putString("pref_port", mServer.port);

		editor.putString("pref_xmpp_user", xmppID);
		editor.putString("pref_xmpp_password", password);

		editor.commit();

		// show main activity
		Intent i = new Intent(this, SetupComplete.class);
		startActivity(i);
	}

	// ==========================================================
	// Inner classes
	// ==========================================================

	static class Server {
		public String id;
		public String domain;
		public String host;
		public String port;
		public String serviceName;
	}
}
