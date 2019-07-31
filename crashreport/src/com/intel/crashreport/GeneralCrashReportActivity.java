/* Copyright (C) 2019 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.crashreport;

import android.database.SQLException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class GeneralCrashReportActivity extends PreferenceActivity {

	protected CrashReport app;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		boolean state = this.getResources().getBoolean(R.bool.enable_bugzilla);
		addPreferencesFromResource(R.xml.menu);
		setTitle(getString(R.string.activity_name));
		app = (CrashReport)getApplicationContext();
		EditTextPreference editLastName = (EditTextPreference)findPreference(getString(R.string.settings_bugzilla_user_last_name_key));
		if(null != editLastName) {
			editLastName.setOnPreferenceChangeListener(listener);
			editLastName.setEnabled(state);
		}
		EditTextPreference editFirstName = (EditTextPreference)findPreference(getString(R.string.settings_bugzilla_user_first_name_key));
		if(null != editFirstName) {
			editFirstName.setOnPreferenceChangeListener(listener);
			editFirstName.setEnabled(state);
		}
		EditTextPreference editMail = (EditTextPreference)findPreference(getString(R.string.settings_bugzilla_user_email_key));

		if(null != editMail) {
			if(!app.getUserEmail().isEmpty())
				editMail.setText(app.getUserEmail());
			else editMail.setText(getString(R.string.settings_bugzilla_user_email_value_default));
			editMail.setOnPreferenceChangeListener(listener);
			editMail.setEnabled(state);
		}
	}





	private final OnPreferenceChangeListener listener = new OnPreferenceChangeListener(){

		@Override
		public boolean onPreferenceChange(Preference preference,
				Object newValue) {
			String sValue = (String)newValue;
			sValue = sValue.trim();

			if(preference.getKey().equals(getString(R.string.settings_bugzilla_user_email_key))) {
				if(sValue.endsWith("@intel.com") && (sValue.indexOf("@") == sValue.lastIndexOf("@")) && (sValue.indexOf("@")!=0))
					return true;
				else {
					Toast.makeText(getApplicationContext(), "Wrong email address.", Toast.LENGTH_LONG).show();
					return false;
				}
			}
			return true;
		}
	};



	@Override
	public void onPause() {
		EditTextPreference editMail = (EditTextPreference)findPreference(getString(R.string.settings_bugzilla_user_email_key));
		if(null != editMail &&
				editMail.getText().equals(getString(R.string.settings_bugzilla_user_email_value_default)))
			editMail.setText("");
		super.onPause();
	}
}
