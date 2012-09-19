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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class ApplicationPreferences {
	private static final String APP_PRIVATE_PREFS = "crashReportPrivatePreferences";
	private SharedPreferences appPrivatePrefs;
	private SharedPreferences appSharedPrefs;
	private Editor privatePrefsEditor;
	private Editor sharedPrefsEditor;
	private Context mCtx;

	public ApplicationPreferences(Context context) {
		this.mCtx = context;
		this.appPrivatePrefs = context.getSharedPreferences(APP_PRIVATE_PREFS, Context.MODE_PRIVATE);
		this.privatePrefsEditor = appPrivatePrefs.edit();
		this.appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		this.sharedPrefsEditor = appSharedPrefs.edit();
	}

	public int getUploadStateItem() {
		return appPrivatePrefs.getInt(mCtx.getString(R.string.upload_state_item_index), -1);
	}

	public void setUploadStateItem(int item) {
		privatePrefsEditor.putInt(mCtx.getString(R.string.upload_state_item_index), item);
		privatePrefsEditor.commit();
	}

	public void saveAlarmDate(long date) {
		privatePrefsEditor.putLong("alarmDate", date);
		privatePrefsEditor.commit();
	}

	public long getAlarmDate() {
		return appPrivatePrefs.getLong("alarmDate", 0);
	}

	public String getUploadState() {
		return appSharedPrefs.getString("uploadStatePref", "askForUpload");
	}

	public void setUploadStateToAsk() {
		sharedPrefsEditor.putString("uploadStatePref", "askForUpload");
		sharedPrefsEditor.commit();
	}

	public void setUploadStateToUpload() {
		sharedPrefsEditor.putString("uploadStatePref", "uploadImmediately");
		sharedPrefsEditor.commit();
	}

	public void setUploadStateToReport() {
		sharedPrefsEditor.putString("uploadStatePref", "uploadReported");
		sharedPrefsEditor.commit();
	}

	public void setUploadStateToDisable() {
		sharedPrefsEditor.putString("uploadStatePref", "uploadDisabled");
		sharedPrefsEditor.commit();
	}

	public void setUploadStateToNeverButNotify() {
		//TODO setUploadStateToNeverButNotify
		setUploadStateToAsk();
	}

	public Boolean isCrashLogsUploadEnable() {
		return appSharedPrefs.getBoolean("enableCrashLogReport", false);
	}

	public String[] getCrashLogsUploadTypes() {
		return CrashLogsListPrefs.parseStoredValue(appSharedPrefs.getString("setReportCrashLogType", ""));
	}

	public String getVersion() {
		return appPrivatePrefs.getString("version", "0");
	}

	public void setVersion(String version) {
		privatePrefsEditor.putString("version", version);
		privatePrefsEditor.commit();
	}
}
