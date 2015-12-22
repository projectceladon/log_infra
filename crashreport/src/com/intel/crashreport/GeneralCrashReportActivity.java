/* INTEL CONFIDENTIAL
 * Copyright 2015 Intel Corporation
 *
 * The source code contained or described herein and all documents
 * related to the source code ("Material") are owned by Intel
 * Corporation or its suppliers or licensors. Title to the Material
 * remains with Intel Corporation or its suppliers and
 * licensors. The Material contains trade secrets and proprietary
 * and confidential information of Intel or its suppliers and
 * licensors. The Material is protected by worldwide copyright and
 * trade secret laws and treaty provisions. No part of the Material
 * may be used, copied, reproduced, modified, published, uploaded,
 * posted, transmitted, distributed, or disclosed in any way without
 * Intel's prior express written permission.
 *
 * No license under any patent, copyright, trade secret or other
 * intellectual property right is granted to or conferred upon you
 * by disclosure or delivery of the Materials, either expressly, by
 * implication, inducement, estoppel or otherwise. Any license under
 * such intellectual property rights must be express and approved by
 * Intel in writing.
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
		addPreferencesFromResource(R.xml.menu);
		setTitle(getString(R.string.activity_name));
		app = (CrashReport)getApplicationContext();
		EditTextPreference editLastName = (EditTextPreference)findPreference(getString(R.string.settings_bugzilla_user_last_name_key));
		if(null != editLastName) {
			editLastName.setOnPreferenceChangeListener(listener);
		}
		EditTextPreference editFirstName = (EditTextPreference)findPreference(getString(R.string.settings_bugzilla_user_first_name_key));
		if(null != editFirstName) {
			editFirstName.setOnPreferenceChangeListener(listener);
		}
		EditTextPreference editMail = (EditTextPreference)findPreference(getString(R.string.settings_bugzilla_user_email_key));

		if(null != editMail) {
			if(!app.getUserEmail().isEmpty())
				editMail.setText(app.getUserEmail());
			else editMail.setText(getString(R.string.settings_bugzilla_user_email_value_default));
			editMail.setOnPreferenceChangeListener(listener);
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
