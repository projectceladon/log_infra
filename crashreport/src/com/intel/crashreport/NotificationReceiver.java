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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {

	// am broadcast -n com.intel.crashreport/.NotificationReceiver -a com.intel.crashreport.intent.CRASH_NOTIFY -c android.intent.category.ALTERNATIVE
	private static final String crashNotificationIntent = "com.intel.crashreport.intent.CRASH_NOTIFY";
	private static final String bootCompletedIntent = "android.intent.action.BOOT_COMPLETED";
	private static final String networkStateChangeIntent = "android.net.conn.BACKGROUND_DATA_SETTING_CHANGED";
	private static final String alarmNotificationIntent = "com.intel.crashreport.intent.ALARM_NOTIFY";

	private static final Intent crashReportStartServiceIntent = new Intent("com.intel.crashreport.CrashReportService");

	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(crashNotificationIntent)) {
			Log.d("NotificationReceiver: crashNotificationIntent");
			context.startService(crashReportStartServiceIntent);
		} else if (intent.getAction().equals(bootCompletedIntent)) {
			Log.d("NotificationReceiver: bootCompletedIntent");
			context.startService(crashReportStartServiceIntent);
		} else if (intent.getAction().equals(networkStateChangeIntent)) {
			Log.d("NotificationReceiver: networkStateChangeIntent");
			context.startService(crashReportStartServiceIntent);
		} else if (intent.getAction().equals(alarmNotificationIntent)) {
			Log.d("NotificationReceiver: alarmNotificationIntent");
			ApplicationPreferences prefs = new ApplicationPreferences(context);
			String uploadState = prefs.getUploadState();
			if ((uploadState != null) && uploadState.contentEquals("uploadReported"))
				prefs.setUploadStateToAsk();
			context.startService(crashReportStartServiceIntent);
		}

	}

}
