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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemProperties;
import android.preference.PreferenceManager;

import com.intel.crashreport.StartServiceActivity.EVENT_FILTER;
import com.intel.crashreport.bugzilla.ui.common.BugzillaMainActivity;
import com.intel.phonedoctor.Constants;

public class ApplicationPreferences {
	private static final String APP_PRIVATE_PREFS = "crashReportPrivatePreferences";
	protected SharedPreferences appPrivatePrefs;
	private final SharedPreferences appSharedPrefs;
	protected Editor privatePrefsEditor;
	private final Editor sharedPrefsEditor;
	private final Context mCtx;

	public ApplicationPreferences(Context context) {
		this.mCtx = context;
		this.appPrivatePrefs = context.getSharedPreferences(APP_PRIVATE_PREFS, Context.MODE_PRIVATE);
		this.privatePrefsEditor = appPrivatePrefs.edit();
		this.appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		this.sharedPrefsEditor = appSharedPrefs.edit();
	}

	public int getReportingState() {
		return appPrivatePrefs.getInt(mCtx.getString(R.string.reporting_state_item), -1);
	}

	public void setReportingState(int state) {
		privatePrefsEditor.putInt(mCtx.getString(R.string.reporting_state_item), state);
		privatePrefsEditor.commit();
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

		if(defaultValues != null) {
			for(String value:defaultValues)
				if(!savedValues.contains(value))
					savedValues.add(value);
		}

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
	 * Return true if event data, formerly named crashlogs, must be uploaded through WiFi only.
	 *
	 * @return true if event data are uploaded through WiFi only, else false.
	 */
	public Boolean isNotificationForAllCrash() {
		return appSharedPrefs.getBoolean(
				mCtx.getString(R.string.settings_all_crash_notification_key),
				Boolean.valueOf(mCtx.getString(R.string.settings_all_crash_notification_value_default)));
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
	 * Chekc is the GCM Messaging system is enabled
	 * @return True if GCM Messaging is enabled
	 */
	public Boolean isGcmEnable() {
		return appSharedPrefs.getBoolean(
				mCtx.getString(R.string.settings_gcm_activation_key),
				Boolean.valueOf(mCtx.getString(R.string.settings_gcm_activation_value_default)));
	}

	/**
	 * Get the GCM token ID
	 * @return GCM Token ID
	 */
	public String getGcmToken() {
		return appPrivatePrefs.getString(mCtx.getString(R.string.settings_private_app_gcm_token_key), "");
	}

	/**
	 * Store the GCM token in a shared preferences
	 * @param token The GCM token
	 */
	public void setGcmToken(String token) {
		privatePrefsEditor.putString(mCtx.getString(R.string.settings_private_app_gcm_token_key), token);
		privatePrefsEditor.commit();
	}

	/**
	 * Get the Build fingerprint
	 * @return build
	 */
	public String getBuild() {
		return appPrivatePrefs.getString(mCtx.getString(R.string.settings_private_app_build_key), "");
	}

	/**
	 * Store the Build fingerprint
	 * @param build The build fingerprint
	 */
	public void setBuild(String build) {
		privatePrefsEditor.putString(mCtx.getString(R.string.settings_private_app_build_key), build);
		privatePrefsEditor.commit();
	}

	/**
	 * Get the event filter selected by the user
	 * @return event filter
	 */
	public EVENT_FILTER getFilterChoice() {
		return EVENT_FILTER.valueOf(appPrivatePrefs.getString(mCtx.getString(R.string.settings_private_app_event_filter_key), EVENT_FILTER.ALL.name()));
	}

	/**
	 * Store the event filter chosen by the user in a shared preferences
	 * @param choice the event filter
	 */
	public void setFilterChoice(EVENT_FILTER choice) {
		privatePrefsEditor.putString(mCtx.getString(R.string.settings_private_app_event_filter_key), choice.name());
		privatePrefsEditor.commit();
	}

	public boolean isSoundEnabledForGcmNotifications() {
		boolean soundEnabled = this.appSharedPrefs.getBoolean(
				mCtx.getString(R.string.settings_gcm_notification_sound),
				Boolean.valueOf(mCtx.getString(R.string.settings_gcm_notification_default_value)));
		Log.d("[GCM] Retrieving sound preference for GCM : " + soundEnabled);
		return soundEnabled;
	}

	public void setSoundEnabledForGcmNotifications(boolean enabled) {
		Log.d("[GCM] Changing preference for GCM sound to: " + enabled);
		this.sharedPrefsEditor.putBoolean(
				mCtx.getString(R.string.settings_gcm_notification_sound),
				enabled);
		this.sharedPrefsEditor.commit();
	}


	public String getGcmFilterAsStr(String sDefault) {
		String filterAsString = this.appSharedPrefs.getString(
				mCtx.getString(R.string.settings_gcm_filter),
				sDefault);
		return filterAsString;
	}

	public void setGcmFilterAsStr(String filter) {
		this.sharedPrefsEditor.putString(
				mCtx.getString(R.string.settings_gcm_filter),
				filter);
		this.sharedPrefsEditor.commit();
	}


	public boolean getOverrideTracker() {
		boolean bResult = this.appSharedPrefs.getBoolean(
				mCtx.getString(R.string.settings_override_tracker_key),
				false);
		return bResult;
	}

	public String getBZTracker() {
		//no default value, in order to manage properly empty value
		String filterAsString = this.appSharedPrefs.getString(
				mCtx.getString(R.string.settings_bugzilla_tracker_key),
				"");
		return filterAsString;
	}

	public void setBZTracker(String tracker) {
		this.sharedPrefsEditor.putString(
				mCtx.getString(R.string.settings_bugzilla_tracker_key),
				tracker);
		this.sharedPrefsEditor.commit();
	}

	public void setDefaultTracker() {
		//set the default tracker depending on the product
		String sProduct = GeneralBuild.getProperty(GeneralBuild.PRODUCT_PROPERTY_NAME);
		String sIncremental = GeneralBuild.getProperty("ro.build.version.incremental").toUpperCase();
		//for known values we use a dedicated tracker
		if (sProduct.equalsIgnoreCase("STARPEAK")) {
			setBZTracker(BugzillaMainActivity.STARPEAK_VALUE);
		} else if (sProduct.equalsIgnoreCase("COHO") || sIncremental.contains("COHO")) {
			setBZTracker(BugzillaMainActivity.IRDA_VALUE);
		} else if (sProduct.equalsIgnoreCase("ICONIC")) {
			setBZTracker(BugzillaMainActivity.ICONIC_VALUE);
		} else if (!sProduct.isEmpty()) {
			//set default value to MCG
			setBZTracker(BugzillaMainActivity.MCG_VALUE);
		}
	}
}
