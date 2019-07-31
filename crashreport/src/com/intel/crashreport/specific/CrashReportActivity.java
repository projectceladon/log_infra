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

package com.intel.crashreport.specific;

import java.io.FileNotFoundException;

import com.intel.crashreport.ApplicationPreferences;
import com.intel.crashreport.database.EventDB;
import com.intel.crashreport.GeneralCrashReportActivity;
import com.intel.crashreport.Log;
import com.intel.crashreport.NotificationMgr;
import com.intel.crashreport.R;

import android.content.Context;
import android.content.BroadcastReceiver.PendingResult;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.view.View;

public class CrashReportActivity extends GeneralCrashReportActivity {
	private static Boolean gcmEnabled = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		boolean state = this.getResources().getBoolean(R.bool.enable_gcm);
		if(app.isUserBuild()) {
			CheckBoxPreference wifiPreference = (CheckBoxPreference)findPreference(getString(R.string.settings_connection_wifi_only_key));
			PreferenceCategory dataPreferences = (PreferenceCategory)findPreference(getString(R.string.settings_event_data_category_key));
			if(wifiPreference != null && dataPreferences != null) {
				dataPreferences.removePreference(wifiPreference);
			}
		}
		CheckBoxPreference crashNotificationPreference = (CheckBoxPreference)findPreference(getString(R.string.settings_all_crash_notification_key));
		if(null != crashNotificationPreference) {
			if (this.getResources().getBoolean(R.bool.enable_crash_notification) == false) {
				PreferenceCategory dataPreferences = (PreferenceCategory)findPreference(getString(R.string.settings_event_data_category_key));
				if(dataPreferences != null) {
					dataPreferences.removePreference(crashNotificationPreference);
				}
			}
			else {
				crashNotificationPreference.setOnPreferenceChangeListener(changeNotificationListener);
			}
		}

		CheckBoxPreference checkGcm = (CheckBoxPreference)findPreference(getString(R.string.settings_gcm_activation_key));
		if(null != checkGcm) {
			checkGcm.setOnPreferenceChangeListener(gcmListener);
			if(null == gcmEnabled)
				gcmEnabled = checkGcm.isChecked();
			checkGcm.setEnabled(state);
		}

		CheckBoxPreference checkGcmSound = (CheckBoxPreference)this.findPreference(getString(R.string.settings_gcm_sound_activation_key));
		if(null != checkGcmSound) {
			checkGcmSound.setOnPreferenceChangeListener(gcmSoundListener);
			checkGcmSound.setEnabled(state);
		}
	}

	private final OnPreferenceChangeListener gcmListener = new OnPreferenceChangeListener(){

		@Override
		public boolean onPreferenceChange(Preference preference,
				Object newValue) {
			if((Boolean)newValue){
				Log.i("GeneralCrashReportActivity:GCM set to ON");
				GcmEvent.INSTANCE.checkTokenGCM(getApplicationContext());
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

	private OnPreferenceChangeListener changeNotificationListener = new OnPreferenceChangeListener(){

		public boolean onPreferenceChange(Preference preference, Object newValue) {
			Boolean notifyAllCrashes = (Boolean)newValue;
			if(notifyAllCrashes)
				new NotifyEventsTask(getApplicationContext()).execute();
			else {
				NotificationMgr nMgr = new NotificationMgr(getApplicationContext());
				nMgr.cancelNotifNoCriticalEvent();
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
				GcmEvent.INSTANCE.checkTokenGCM(getApplicationContext());
			}
			else {
				GcmEvent.INSTANCE.disableGcm();
			}
		}
		gcmEnabled = null;
		super.onDestroy();
	}

	public class NotifyEventsTask extends AsyncTask<Void, Void, Void> {

		private Context context;

		public NotifyEventsTask(Context ctx){
			context = ctx;
		}

		@Override
		protected Void doInBackground(Void... params) {
			EventDB db = new EventDB(context);
			try {
				db.open();
				if (db.isThereEventToNotify(true)) {
					NotificationMgr nMgr = new NotificationMgr(context);
					nMgr.notifyCriticalEvent(db.getCriticalEventsNumber(), db.getCrashToNotifyNumber());
				}
				db.close();
			} catch (SQLException e) {
				db.close();
				throw e;
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... params) {
		}

		protected void onPostExecute(Void... params) {

		}

	}

}
