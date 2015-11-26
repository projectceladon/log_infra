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

package com.intel.crashreport.specific;

import java.io.FileNotFoundException;

import com.intel.crashreport.ApplicationPreferences;
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


public class CrashReportActivity extends GeneralCrashReportActivity {
	private static Boolean gcmEnabled = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(app.isUserBuild()) {
			CheckBoxPreference wifiPreference = (CheckBoxPreference)findPreference(getString(R.string.settings_connection_wifi_only_key));
			PreferenceCategory dataPreferences = (PreferenceCategory)findPreference(getString(R.string.settings_event_data_category_key));
			if(wifiPreference != null && dataPreferences != null) {
				dataPreferences.removePreference(wifiPreference);
			}
		}
		CheckBoxPreference crashNotificationPreference = (CheckBoxPreference)findPreference(getString(R.string.settings_all_crash_notification_key));
		if(null != crashNotificationPreference) {
			crashNotificationPreference.setOnPreferenceChangeListener(changeNotificationListener);
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
				GcmEvent.INSTANCE.checkTokenGCM();
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
