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

package com.intel.crashreport.specific;

import java.io.FileNotFoundException;

import com.intel.crashreport.GeneralCrashReportActivity;
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
    }

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

    public class NotifyEventsTask extends AsyncTask<Void, Void, Void> {

        private Context context;

        public NotifyEventsTask(Context ctx){
            context = ctx;
        }

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

        protected void onProgressUpdate(Void... params) {
        }

        protected void onPostExecute(Void... params) {

        }

    }

}
