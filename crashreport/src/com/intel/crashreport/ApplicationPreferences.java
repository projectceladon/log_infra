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

import java.util.Arrays;
import java.util.List;

import com.intel.phonedoctor.Constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemProperties;
import android.preference.PreferenceManager;

public class ApplicationPreferences {
	private static final String APP_PRIVATE_PREFS = "crashReportPrivatePreferences";
	protected SharedPreferences appPrivatePrefs;
	private SharedPreferences appSharedPrefs;
	protected Editor privatePrefsEditor;
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
		return appSharedPrefs.getString(
			mCtx.getString(R.string.settings_event_report_management_key),
			mCtx.getString(R.string.settings_event_report_management_value_default));
	}

	public void setUploadStateToAsk() {
		sharedPrefsEditor.putString(
			mCtx.getString(R.string.settings_event_report_management_key),
			"askForUpload");
		sharedPrefsEditor.commit();
	}

	public void setUploadStateToUpload() {
		sharedPrefsEditor.putString(
			mCtx.getString(R.string.settings_event_report_management_key),
			"uploadImmediately");
		sharedPrefsEditor.commit();
	}

	public void setUploadStateToReport() {
		sharedPrefsEditor.putString(
			mCtx.getString(R.string.settings_event_report_management_key),
			"uploadReported");
		sharedPrefsEditor.commit();
	}

	public void setUploadStateToDisable() {
		sharedPrefsEditor.putString(
			mCtx.getString(R.string.settings_event_report_management_key),
			"uploadDisabled");
		sharedPrefsEditor.commit();
	}

	public void setUploadStateToNeverButNotify() {
		//TODO setUploadStateToNeverButNotify
		setUploadStateToAsk();
	}

	public Boolean isCrashLogsUploadEnable() {
		return appSharedPrefs.getBoolean(
			mCtx.getString(R.string.settings_event_data_enable_key),
			Boolean.valueOf(mCtx.getString(R.string.settings_event_data_enable_value_default)));
	}

	public String[] getCrashLogsUploadTypes() {
		return CrashLogsListPrefs.parseStoredValue(
			appSharedPrefs.getString(
				mCtx.getString(R.string.settings_event_data_types_key),
				mCtx.getString(R.string.settings_event_data_types_value_default)));
	}

	public void resetCrashLogsUploadTypes() {
		List<String> savedValues = Arrays.asList(getCrashLogsUploadTypes());

		String defaultValues[] = CrashLogsListPrefs.parseStoredValue(
				mCtx.getString(R.string.settings_event_data_types_value_default));

		for(String value:defaultValues)
			if(!savedValues.contains(value))
				savedValues.add(value);

		sharedPrefsEditor.putString(
			mCtx.getString(R.string.settings_event_data_types_key),
			CrashLogsListPrefs.prepareToStoreStrings(savedValues));
		sharedPrefsEditor.commit();
	}

	public String getVersion() {
		return appPrivatePrefs.getString("version", "0");
	}

	public void setVersion(String version) {
		privatePrefsEditor.putString("version", version);
		privatePrefsEditor.commit();
	}

	/**
	 *
	 * @return
	 */
	public String getUserLastName() {
		return appSharedPrefs.getString(
				mCtx.getString(R.string.settings_bugzilla_user_last_name_key),
				mCtx.getString(R.string.settings_bugzilla_user_last_name_value_default));
	}

	public void setUserLastName(String lastname) {
		sharedPrefsEditor.putString(
				mCtx.getString(R.string.settings_bugzilla_user_last_name_key),
				lastname);
		sharedPrefsEditor.commit();
	}

	public String getUserFirstName() {
		return appSharedPrefs.getString(
				mCtx.getString(R.string.settings_bugzilla_user_first_name_key),
				mCtx.getString(R.string.settings_bugzilla_user_first_name_value_default));
	}

	public void setUserFirstName(String firstname) {
		sharedPrefsEditor.putString(
				mCtx.getString(R.string.settings_bugzilla_user_first_name_key),
				firstname);
		sharedPrefsEditor.commit();
	}

	public String getUserEmail() {
		return appSharedPrefs.getString(
				mCtx.getString(R.string.settings_bugzilla_user_email_key),
				"");
	}

	public void setUserEmail(String email) {
		sharedPrefsEditor.putString(
				mCtx.getString(R.string.settings_bugzilla_user_email_key),
				email);
		sharedPrefsEditor.commit();
	}

	/**
	 * Return true if Bugzilla module is in test mode, which means that created
	 * BZ will be created under the test project in Bugzilla DB.
	 *
	 * @return true if in test mode, else false.
	 */
	public Boolean isBugzillaModuleInTestMode() {
		return appPrivatePrefs.getBoolean(
				mCtx.getString(R.string.settings_private_bugzilla_test_mode_key),
				Boolean.valueOf(mCtx.getString(R.string.settings_private_bugzilla_test_mode_value)));
	}

	/**
	 * @brief Provide a directory name (full path) to store new event data files.
	 *
	 * No checks are done on directory, it's your responsibility to clean it if
	 * not empty. Directory numbers are incremented on each call.
	 *
	 * @return directory path
	 */
	public synchronized String getNewEventDirectoryName() {
		int number = appPrivatePrefs.getInt("newEventDirectoryNumber", 0);
		privatePrefsEditor.putInt("newEventDirectoryNumber", number+1);
		privatePrefsEditor.commit();
		return Constants.PD_EVENT_DATA_DIR + Constants.PD_EVENT_DATA_DIR_ELEMENT_ROOT + number;
	}

	/**
	 * Return true if event data, formerly named crashlogs, must be uploaded through WiFi only.
	 *
	 * @return true if event data are uploaded through WiFi only, else false.
	 */
	public Boolean isWifiOnlyForEventData() {
		if(isUserBuild()) {
			return true;
		}
		return appSharedPrefs.getBoolean(
			mCtx.getString(R.string.settings_connection_wifi_only_key),
			Boolean.valueOf(mCtx.getString(R.string.settings_connection_wifi_only_value_default)));
	}

	/**
	 * Return the Crashtool server address, domain name.
	 *
	 * @return a String representing the Crashtool server address
	 */
	public String getServerAddress() {
		return appPrivatePrefs.getString(
				mCtx.getString(R.string.settings_private_app_server_host_key),
				mCtx.getString(R.string.settings_private_app_server_host_value));
	}

	/**
	 * Return the Crashtool server port.
	 *
	 * @return an int, representing Crashtool server port
	 */
	public int getServerPort() {
		String port = appPrivatePrefs.getString(
				mCtx.getString(R.string.settings_private_app_server_port_key),
				mCtx.getString(R.string.settings_private_app_server_port_value));
		try {
			return Integer.parseInt(port);
		} catch (NumberFormatException e) {
			Log.w("ApplicationPreferences: getServerPort: port parse failed: " + port);
			return 4001;
		}
	}

	/**
	 * @brief Provide an index value to generate crashlogd trigger command file
	 *
	 * The index value is incremented on each call, and modulo 999.
	 *
	 * @return index value
	 */
	public synchronized int getNewTriggerFileIndex() {
		int index = appPrivatePrefs.getInt("newTriggerFileIndex", 1);
		privatePrefsEditor.putInt("newTriggerFileIndex", ((index+1) % 999));
		privatePrefsEditor.commit();
		return index;
	}

	/**
	 * @brief Provides information to know if the build used is an user build
	 *
	 * Provides information to know if the build used is an user build.
	 *
	 * @return true if the build is an user build and false else.
	 */
	public boolean isUserBuild() {
		String buildType = SystemProperties.get("ro.build.type", "user");
		return buildType.equals("user");
	}

	/**
	 * Private settings to save if DropBoxManager.isFull() method is available
	 */
	private static final String settings_private_full_dropbox_api_available = "settings_private_full_dropbox_api_available";

	/**
	 * Return if DropBoxManager.isFull() method is available, true by default in Intel build.
	 *
	 * @return true if method is available, false otherwise
	 */
	public boolean isFullDropboxMethodAvailable() {
		return appPrivatePrefs.getBoolean(settings_private_full_dropbox_api_available, true);
	}

	/**
	 * Set DropBoxManager.isFull() method availability to false
	 *
	 * Usually, if method is not available, it will not change across executions
	 */
	public void setFullDropboxMethodNotAvailable() {
		privatePrefsEditor.putBoolean(settings_private_full_dropbox_api_available, false);
	}

}
