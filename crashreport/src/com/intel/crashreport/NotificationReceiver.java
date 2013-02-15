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

import java.io.FileNotFoundException;
import java.util.Timer;

import com.intel.crashreport.NotifyCrashTask;
import com.intel.crashreport.bugzilla.BZFile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.DropBoxManager;

public class NotificationReceiver extends BroadcastReceiver {

	// am broadcast -n com.intel.crashreport/.NotificationReceiver -a com.intel.crashreport.intent.CRASH_NOTIFY -c android.intent.category.ALTERNATIVE
	private static final String crashNotificationIntent = "com.intel.crashreport.intent.CRASH_NOTIFY";
	private static final String bootCompletedIntent = "android.intent.action.BOOT_COMPLETED";
	private static final String networkStateChangeIntent = "android.net.conn.BACKGROUND_DATA_SETTING_CHANGED";
	private static final String alarmNotificationIntent = "com.intel.crashreport.intent.ALARM_NOTIFY";
	private static final String crashLogsCopyFinishedIntent = "com.intel.crashreport.intent.CRASH_LOGS_COPY_FINISHED";
	private static final String eventIdExtra = "com.intel.crashreport.extra.EVENT_ID";

	private static final Intent crashReportStartServiceIntent = new Intent("com.intel.crashreport.CrashReportService");
	private static final Intent phoneInspectorStartServiceIntent = new Intent("com.intel.crashreport.PhoneInspectorService");

	//PhoneInspectorService intent type
	public static final String EXTRA_TYPE = "type";
	//PhoneInspectorService intent type values
	public static final String DROPBOX_ENTRY_ADDED = "DROPBOX_ENTRY_ADDED";
	public static final String BOOT_COMPLETED = "BOOT_COMPLETED";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(crashNotificationIntent)) {
			Log.d("NotificationReceiver: crashNotificationIntent");
			startCrashReport(context);
		} else if (intent.getAction().equals(bootCompletedIntent)) {
			Log.d("NotificationReceiver: bootCompletedIntent");
			startCrashReport(context);

			//Add type to intent and send it
			phoneInspectorStartServiceIntent.putExtra(EXTRA_TYPE, BOOT_COMPLETED);
			context.startService(phoneInspectorStartServiceIntent);
		} else if (intent.getAction().equals(networkStateChangeIntent)) {
			Log.d("NotificationReceiver: networkStateChangeIntent");
			startCrashReport(context);
		} else if (intent.getAction().equals(crashLogsCopyFinishedIntent)){
			Log.d("NotificationReceiver: crashLogsCopyFinishedIntent");
			if (intent.hasExtra(eventIdExtra)) {
				String eventId = intent.getStringExtra(eventIdExtra);
				EventDB db = new EventDB(context);
				boolean isPresent = false;
				if (db!=null){
					try {
						db.open();
						if (db.isEventInDb(eventId)) {

							if (!db.eventDataAreReady(eventId)) {
								isPresent = true;
								db.updateEventDataReady(eventId);
							}
						}
					} catch (SQLException e) {
						Log.w("NotificationReceiver: Fail to access DB", e);
					}
					db.close();
					if(isPresent) {
						startCrashReport(context);
					}
				}
			}
		} else if (intent.getAction().equals(alarmNotificationIntent)) {
			Log.d("NotificationReceiver: alarmNotificationIntent");
			ApplicationPreferences prefs = new ApplicationPreferences(context);
			String uploadState = prefs.getUploadState();
			if ((uploadState != null) && uploadState.contentEquals("uploadReported"))
				prefs.setUploadStateToAsk();
			startCrashReport(context);

		//Intent indicating a new entry has been added to the dropbox
		} else if (intent.getAction().equals(DropBoxManager.ACTION_DROPBOX_ENTRY_ADDED)) {
			Log.d("NotificationReceiver: dropBoxEntryAddedIntent");

			//Add data to intent
			phoneInspectorStartServiceIntent.putExtra(EXTRA_TYPE, DROPBOX_ENTRY_ADDED);
			phoneInspectorStartServiceIntent.putExtra(DropBoxManager.EXTRA_TAG, intent.getStringExtra(DropBoxManager.EXTRA_TAG));
			phoneInspectorStartServiceIntent.putExtra(DropBoxManager.EXTRA_TIME, intent.getLongExtra(DropBoxManager.EXTRA_TIME, 0));

			context.startService(phoneInspectorStartServiceIntent);
		}
	}

	public void startCrashReport(Context context) {
		CrashReport app = (CrashReport)context.getApplicationContext();
		if (!app.isServiceStarted())
			context.startService(crashReportStartServiceIntent);
		else {
			try {
				app.checkEvents("NotificationReceiver");
			} catch (FileNotFoundException e) {
				Log.w("NotificationReceiver: history_event file not found");
			} catch (SQLException e) {
				Log.w("NotificationReceiver: db Exception");
			}
		}
	}

}
