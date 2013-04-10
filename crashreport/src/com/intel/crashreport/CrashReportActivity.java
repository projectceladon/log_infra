/* Phone Doctor (CLOTA)
 *
 * Copyright (C) Intel 2012
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
 * Author: Jeremy Rocher <jeremyx.rocher@intel.com>
 */

package com.intel.crashreport;

import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.view.View;
import android.widget.Toast;

public class CrashReportActivity extends PreferenceActivity {

    private CrashReport app;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.menu);
        setTitle(getString(R.string.app_name)+" "+getString(R.string.app_version));
        app = (CrashReport)getApplicationContext();
        if(app.isUserBuild()) {
            CheckBoxPreference wifiPreference = (CheckBoxPreference)findPreference(getString(R.string.settings_connection_wifi_only_key));
            PreferenceCategory dataPreferences = (PreferenceCategory)findPreference(getString(R.string.settings_event_data_category_key));
            dataPreferences.removePreference(wifiPreference);
        }
        EditTextPreference editLastName = (EditTextPreference)findPreference(getString(R.string.settings_bugzilla_user_last_name_key));
        editLastName.setOnPreferenceChangeListener(listener);
        EditTextPreference editFirstName = (EditTextPreference)findPreference(getString(R.string.settings_bugzilla_user_first_name_key));
        editFirstName.setOnPreferenceChangeListener(listener);
        EditTextPreference editMail = (EditTextPreference)findPreference(getString(R.string.settings_bugzilla_user_email_key));
        app = (CrashReport)getApplicationContext();
        if(!app.getUserEmail().equals(""))
            editMail.setText(app.getUserEmail());
        else editMail.setText(getString(R.string.settings_bugzilla_user_email_value_default));
        editMail.setOnPreferenceChangeListener(listener);
    }

    private OnPreferenceChangeListener listener = new OnPreferenceChangeListener(){

        public boolean onPreferenceChange(Preference preference,
                Object newValue) {
            String sValue = (String)newValue;
            sValue = sValue.trim();
            newValue = sValue;

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

    public void onPause() {
        EditTextPreference editMail = (EditTextPreference)findPreference(getString(R.string.settings_bugzilla_user_email_key));
        if(editMail.getText().equals(getString(R.string.settings_bugzilla_user_email_value_default)))
            editMail.setText("");
        super.onPause();
    }

}