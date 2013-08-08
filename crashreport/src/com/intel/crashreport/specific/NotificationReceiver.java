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

import com.intel.crashreport.CrashReport;
import com.intel.crashreport.CrashReportRequest;
import com.intel.crashreport.GeneralNotificationReceiver;
import com.intel.crashreport.Log;

import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.DropBoxManager;

public class NotificationReceiver extends GeneralNotificationReceiver {

	// am broadcast -n com.intel.crashreport/.NotificationReceiver -a com.intel.crashreport.intent.CRASH_NOTIFY -c android.intent.category.ALTERNATIVE
	private static final String crashLogsCopyFinishedIntent = "com.intel.crashreport.intent.CRASH_LOGS_COPY_FINISHED";
	private static final String eventIdExtra = "com.intel.crashreport.extra.EVENT_ID";
	private static final String relaunchCheckEventsService = "com.intel.crashreport.intent.RELAUNCH_SERVICE";
	private static final String startCrashReportService = "com.intel.crashreport.intent.START_CRASHREPORT";

	private static final Intent checkEventsServiceIntent = new Intent("com.intel.crashreport.specific.CheckEventsService");

	private static final Intent phoneInspectorStartServiceIntent = new Intent("com.intel.crashreport.specific.PhoneInspectorService");
	private static final Intent crashReportStartServiceIntent = new Intent("com.intel.crashreport.CrashReportService");

	//PhoneInspectorService intent type
	public static final String EXTRA_TYPE = "type";
	//PhoneInspectorService intent type values
	public static final String DROPBOX_ENTRY_ADDED = "DROPBOX_ENTRY_ADDED";
	public static final String BOOT_COMPLETED = "BOOT_COMPLETED";

	private static boolean serviceIsRunning = false;

	@Override
	public void onReceive(Context context, Intent intent) {

		iStartCrashReport = new StartCrashReport(){

			@Override
			public void startCrashReport(Context context) {
				CrashReport app = (CrashReport)context.getApplicationContext();
				app.addRequest(new CrashReportRequest());
				if(!app.isCheckEventsServiceStarted())
					context.startService(checkEventsServiceIntent);
			}

			public void startUpload(Context context) {
				CrashReport app = (CrashReport)context.getApplicationContext();
				if(!app.isServiceStarted())
					context.startService(crashReportStartServiceIntent);
			}

		};

		if (intent.getAction().equals(bootCompletedIntent)) {
			Log.d("NotificationReceiver: bootCompletedIntent");
			super.onReceive(context, intent);
			//Add type to intent and send it
			if(!serviceIsRunning) {
				serviceIsRunning = true;
				phoneInspectorStartServiceIntent.putExtra(EXTRA_TYPE, BOOT_COMPLETED);
				context.startService(phoneInspectorStartServiceIntent);
			}
		} else if (intent.getAction().equals(startCrashReportService)) {
			Log.d("NotificationReceiver: startCrashReportService");
			iStartCrashReport.startUpload(context);
		} else if (intent.getAction().equals(relaunchCheckEventsService)) {
			CrashReport app = (CrashReport)context.getApplicationContext();
			Log.d("NotificationReceiver: relaunchCheckEventsService");
			app.setServiceRelaunched(false);
			if(!app.isCheckEventsServiceStarted())
				context.startService(checkEventsServiceIntent);
		} else if (intent.getAction().equals(crashLogsCopyFinishedIntent)){
			CrashReport app = (CrashReport)context.getApplicationContext();
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
						if(!app.isServiceStarted())
							context.startService(crashReportStartServiceIntent);
					}
				}
			}
		} else if (intent.getAction().equals(DropBoxManager.ACTION_DROPBOX_ENTRY_ADDED)) {
			Log.d("NotificationReceiver: dropBoxEntryAddedIntent");

			//Add data to intent
			if(!serviceIsRunning) {
				serviceIsRunning = true;
				phoneInspectorStartServiceIntent.putExtra(EXTRA_TYPE, DROPBOX_ENTRY_ADDED);
				phoneInspectorStartServiceIntent.putExtra(DropBoxManager.EXTRA_TAG, intent.getStringExtra(DropBoxManager.EXTRA_TAG));
				phoneInspectorStartServiceIntent.putExtra(DropBoxManager.EXTRA_TIME, intent.getLongExtra(DropBoxManager.EXTRA_TIME, 0));

				context.startService(phoneInspectorStartServiceIntent);
			}
		} else {
			super.onReceive(context, intent);
		}
	}


}
