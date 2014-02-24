/* Phone Doctor (CLOTA)
 *
 * Copyright (C) Intel 2014
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

import android.database.SQLException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

import com.intel.crashreport.specific.EventDB;

public class GeneralCrashReportActivity extends PreferenceActivity {

    protected CrashReport app;
    private static Boolean gcmEnabled = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.menu);
        setTitle(getString(R.string.app_name)+" "+getString(R.string.app_version));
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
	        if(!app.getUserEmail().equals(""))
	            editMail.setText(app.getUserEmail());
	        else editMail.setText(getString(R.string.settings_bugzilla_user_email_value_default));
	        editMail.setOnPreferenceChangeListener(listener);
	}

	CheckBoxPreference checkGcm = (CheckBoxPreference)findPreference(getString(R.string.settings_gcm_activation_key));
	if(null != checkGcm) {
		checkGcm.setOnPreferenceChangeListener(gcmListener);
		if(null == gcmEnabled)
			gcmEnabled = checkGcm.isChecked();
	}

	CheckBoxPreference checkGcmSound = (CheckBoxPreference)this.findPreference(getString(R.string.settings_gcm_sound_activation_key));
	if(null != checkGcmSound) {
		checkGcmSound.setOnPreferenceChangeListener(gcmSoundListener);
	}
    }

    private final OnPreferenceChangeListener gcmListener = new OnPreferenceChangeListener(){

        @Override
		public boolean onPreferenceChange(Preference preference,
                Object newValue) {
            if((Boolean)newValue){
                Log.i("GeneralCrashReportActivity:GCM set to ON");
                GcmEvent.INSTANCE.checkTokenGCM();
            }
            else
                Log.i("GeneralCrashReportActivity:GCM set to OFF");
            EventDB db = new EventDB(getApplicationContext());
            try {
                db.open();
                ApplicationPreferences privatePrefs = new ApplicationPreferences(getApplicationContext());
                db.updateDeviceToken(((Boolean)newValue?privatePrefs.getGcmToken():""));
                db.close();
            }
            catch(SQLException e) {
                Log.e("GeneralCrashReportActivity:gcmListener: Fail to access DB.");
            }
            return true;
        }
    };

    private final OnPreferenceChangeListener gcmSoundListener = new OnPreferenceChangeListener(){

        @Override
		public boolean onPreferenceChange(Preference preference,
                Object newValue) {
            Boolean newBooleanValue = (Boolean) newValue;
            ApplicationPreferences preferences = new ApplicationPreferences(getApplicationContext());
            preferences.setSoundEnabledForGcmNotifications(newBooleanValue);
            return true;
        }
    };

    private final OnPreferenceChangeListener listener = new OnPreferenceChangeListener(){

        @Override
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

    @Override
    public void onDestroy() {
        CheckBoxPreference checkGcm = (CheckBoxPreference)findPreference(getString(R.string.settings_gcm_activation_key));
        if(null != checkGcm && gcmEnabled != checkGcm.isChecked()) {
            if(checkGcm.isChecked()){
                GcmEvent.INSTANCE.enableGcm();
                GcmEvent.INSTANCE.checkTokenGCM();
            }
            else {
                GcmEvent.INSTANCE.disableGcm();
            }
        }
        gcmEnabled = null;
        super.onDestroy();
    }

    @Override
    public void onPause() {
        EditTextPreference editMail = (EditTextPreference)findPreference(getString(R.string.settings_bugzilla_user_email_key));
        if(null != editMail &&
                editMail.getText().equals(getString(R.string.settings_bugzilla_user_email_value_default)))
            editMail.setText("");
        super.onPause();
    }
}
