/* Crash Report (CLOTA)
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

import android.app.Application;
import android.preference.PreferenceManager;

public class CrashReport extends Application {

	private Boolean serviceStarted = false;
	private Boolean tryingToConnect = false;
	private Boolean activityBounded = false;
	private Boolean wifiOnly = false;

	public void onCreate() {
		super.onCreate();
		PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.menu, false);
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
}
