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

import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.DropBoxManager;

import com.intel.crashreport.CrashReport;
import com.intel.crashreport.CrashReportRequest;
import com.intel.crashreport.GeneralNotificationReceiver;
import com.intel.crashreport.Log;

public class NotificationReceiver extends GeneralNotificationReceiver {

	// am broadcast -n com.intel.crashreport/.NotificationReceiver -a com.intel.crashreport.intent.CRASH_NOTIFY -c android.intent.category.ALTERNATIVE
	private static final String CRASHLOGS_COPY_FINISHED_INTENT 	= "com.intel.crashreport.intent.CRASH_LOGS_COPY_FINISHED";
	private static final String EVENT_ID_EXTRA 					= "com.intel.crashreport.extra.EVENT_ID";
	private static final String RELAUNCH_CHECK_EVENTS_SERVICE 	= "com.intel.crashreport.intent.RELAUNCH_SERVICE";
	private static final String START_CRASHREPORT_SERVICE 		= "com.intel.crashreport.intent.START_CRASHREPORT";

	private static final Intent CHECK_EVENTS_SERVICE_INTENT 	= new Intent("com.intel.crashreport.specific.CheckEventsService");

	private static final Intent PHONE_INSPECTOR_START_SERVICE_INTENT 	= new Intent("com.intel.crashreport.specific.PhoneInspectorService");
	private static final Intent CRASH_REPORT_START_SERVICE_INTENT 		= new Intent("com.intel.crashreport.CrashReportService");

	//PhoneInspectorService intent type
	public static final String EXTRA_TYPE			 	= "type";
	//PhoneInspectorService intent type values
	public static final String DROPBOX_ENTRY_ADDED 		= "DROPBOX_ENTRY_ADDED";
	public static final String BOOT_COMPLETED 			= "BOOT_COMPLETED";

	private static boolean serviceIsRunning = false;

	@Override
	public void onReceive(Context context, Intent intent) {

		iStartCrashReport = new StartCrashReport(){

			@Override
			public void startCrashReport(Context context) {
				CrashReport app = (CrashReport)context.getApplicationContext();
				app.addRequest(new CrashReportRequest());
				if(!app.isCheckEventsServiceStarted())
					context.startService(CHECK_EVENTS_SERVICE_INTENT);
			}

			@Override
			public void startUpload(Context context) {
				CrashReport app = (CrashReport)context.getApplicationContext();
				if(!app.isServiceStarted())
					context.startService(CRASH_REPORT_START_SERVICE_INTENT);
			}

		};

		if (intent.getAction().equals(BOOT_COMPLETED_INTENT)) {
			Log.d("NotificationReceiver: bootCompletedIntent");
			super.onReceive(context, intent);
			//Add type to intent and send it
			if(!serviceIsRunning) {
				serviceIsRunning = true;
				PHONE_INSPECTOR_START_SERVICE_INTENT.putExtra(EXTRA_TYPE, BOOT_COMPLETED);
				context.startService(PHONE_INSPECTOR_START_SERVICE_INTENT);
			}
		} else if (intent.getAction().equals(START_CRASHREPORT_SERVICE)) {
			Log.d("NotificationReceiver: startCrashReportService");
			iStartCrashReport.startUpload(context);
		} else if (intent.getAction().equals(RELAUNCH_CHECK_EVENTS_SERVICE)) {
			CrashReport app = (CrashReport)context.getApplicationContext();
			Log.d("NotificationReceiver: relaunchCheckEventsService");
			app.setServiceRelaunched(false);
			if(!app.isCheckEventsServiceStarted())
				context.startService(CHECK_EVENTS_SERVICE_INTENT);
		} else if (intent.getAction().equals(CRASHLOGS_COPY_FINISHED_INTENT)){
			CrashReport app = (CrashReport)context.getApplicationContext();
			Log.d("NotificationReceiver: crashLogsCopyFinishedIntent");
			if (intent.hasExtra(EVENT_ID_EXTRA)) {
				String eventId = intent.getStringExtra(EVENT_ID_EXTRA);
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
							context.startService(CRASH_REPORT_START_SERVICE_INTENT);
						else
							app.setNeedToUpload(true);
					}
				}
			}
		} else if (intent.getAction().equals(DropBoxManager.ACTION_DROPBOX_ENTRY_ADDED)) {
			Log.d("NotificationReceiver: dropBoxEntryAddedIntent");

			//Add data to intent
			if(!serviceIsRunning) {
				serviceIsRunning = true;
				PHONE_INSPECTOR_START_SERVICE_INTENT.putExtra(EXTRA_TYPE, DROPBOX_ENTRY_ADDED);
				PHONE_INSPECTOR_START_SERVICE_INTENT.putExtra(DropBoxManager.EXTRA_TAG, intent.getStringExtra(DropBoxManager.EXTRA_TAG));
				PHONE_INSPECTOR_START_SERVICE_INTENT.putExtra(DropBoxManager.EXTRA_TIME, intent.getLongExtra(DropBoxManager.EXTRA_TIME, 0));

				context.startService(PHONE_INSPECTOR_START_SERVICE_INTENT);
			}
		} else {
			super.onReceive(context, intent);
		}
	}


}
