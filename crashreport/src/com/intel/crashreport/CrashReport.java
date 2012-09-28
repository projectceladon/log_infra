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

import com.intel.crashreport.bugzilla.ui.BugStorage;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.SQLException;
import android.preference.PreferenceManager;

public class CrashReport extends Application {

	private Boolean serviceStarted = false;
	private Boolean tryingToConnect = false;
	private Boolean activityBounded = false;
	private Boolean wifiOnly = false;
	private Build myBuild;
	private BugStorage bugzillaStorage;

	public void onCreate() {
		super.onCreate();
		ApplicationPreferences privatePrefs = new ApplicationPreferences(this);
		bugzillaStorage = new BugStorage(this);
		String version = this.getString(R.string.app_version);
		if (!privatePrefs.getVersion().contentEquals(version)) {
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			Editor editor = sharedPrefs.edit();
			editor.clear();
			editor.commit();
			PreferenceManager.setDefaultValues(this, R.xml.menu, true);
			privatePrefs.setVersion(version);

			EventDB db = new EventDB(this.getApplicationContext());

			try {
				db.open();
				db.deleteAllTypes();

				db.deleteAllCriticalEvents();

				db.addTypes(new String[]{"IPANIC","FABRICERR","IPANIC_FORCED","MEMERR","INSTERR","SRAMECCERR","HWWDTLOGERR","MSHUTDOWN","UIWDT","WDT"},1);

				for (String type : getResources().getStringArray(R.array.reportCrashLogsTypeValues)) {
					if (!db.isTypeInDb(type)) {
						db.addType(type,0);
					}
				}

				db.insertCricitalEvent("TOMBSTONE", "system_server", "", "", "", "", "");
				db.close();
			} catch (SQLException e) {
				Log.w("CrashReport: update of critical crash db failed");
			}

		}

		EventDB db = new EventDB(this.getApplicationContext());
		db.updatePermission();
	}

	public boolean isServiceStarted(){
		return serviceStarted;
	}
	public void setServiceStarted(Boolean s){
		serviceStarted = s;
	}

	public boolean isTryingToConnect(){
		return tryingToConnect;
	}
	public void setTryingToConnect(Boolean s){
		tryingToConnect = s;
	}

	public boolean isActivityBounded() {
		return activityBounded;
	}
	public void setActivityBounded(Boolean s) {
		activityBounded = s;
	}

	public boolean isWifiOnly() {
		return wifiOnly;
	}
	public void setWifiOnly(Boolean s) {
		wifiOnly = s;
	}

	public void setMyBuild(Build myBuild) {
		this.myBuild = myBuild;
	}
	public Build getMyBuild() {
		return myBuild;
	}

	public String getUserLastName(){
		ApplicationPreferences privatePrefs = new ApplicationPreferences(this);
		return privatePrefs.getUserLastName();
	}

	public String getUserFirstName() {
		ApplicationPreferences privatePrefs = new ApplicationPreferences(this);
		return privatePrefs.getUserFirstName();
	}

	public String getUserEmail() {
		ApplicationPreferences privatePrefs = new ApplicationPreferences(this);
		return privatePrefs.getUserEmail();
	}

	public void setUserFirstName(String firstname){
		ApplicationPreferences privatePrefs = new ApplicationPreferences(this);
		privatePrefs.setUserFirstName(firstname);
	}

	public void setUserLastName(String lastname) {
		ApplicationPreferences privatePrefs = new ApplicationPreferences(this);
		privatePrefs.setUserLastName(lastname);
	}

	public void setUserEmail(String email) {
		ApplicationPreferences privatePrefs = new ApplicationPreferences(this);
		privatePrefs.setUserEmail(email);
	}

	public BugStorage getBugzillaStorage() {
		return bugzillaStorage;
	}

}
