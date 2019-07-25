/* Copyright (C) 2019 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.crashreport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;



public class GeneralNotificationReceiver extends BroadcastReceiver {

	// am broadcast -n com.intel.crashreport/.NotificationReceiver -a com.intel.crashreport.intent.CRASH_NOTIFY -c android.intent.category.ALTERNATIVE
	private static final String CRASH_NOTIFICATION_INTENT 	= "com.intel.crashreport.intent.CRASH_NOTIFY";
	protected static final String BOOT_COMPLETED_INTENT 	= "android.intent.action.BOOT_COMPLETED";
	private static final String NETWORK_STATE_CHANGE_INTENT = "android.net.conn.CONNECTIVITY_CHANGE";
	private static final String ALARM_NOTIFICATION_INTENT 	= "com.intel.crashreport.intent.ALARM_NOTIFY";
	public static final String GCM_MARK_AS_READ 			= "com.intel.crashreport.intent.MARK_AS_READ";

	protected StartCrashReport iStartCrashReport;
	protected Context context;

	@Override
	public void onReceive(Context context, Intent intent) {
		// First save the context
		this.context = context;
		String action = intent.getAction();
		// Check that we have something to do
		if(action == null) {
			return;
		}
		// Handle the intent
		if (action.equals(CRASH_NOTIFICATION_INTENT)) {
			Log.d("NotificationReceiver: crashNotificationIntent");
			iStartCrashReport.startCrashReport(context);
		} else if (action.equals(BOOT_COMPLETED_INTENT)) {
			Log.d("NotificationReceiver: bootCompletedIntent");
			iStartCrashReport.startCrashReport(context);
		} else if (action.equals(NETWORK_STATE_CHANGE_INTENT)) {
			Log.d("NotificationReceiver: networkStateChangeIntent");
			CrashReport app = (CrashReport)context.getApplicationContext();
			Connector con = new Connector(context.getApplicationContext());
			if(!con.getDataConnectionAvailability()) {
				if(app.isServiceStarted()) {
					CrashReportService mService = app.getUploadService();
					if((mService != null) && mService.isServiceUploading())
						mService.cancelDownload();
				}
			}
			else
				iStartCrashReport.startCrashReport(context);
		} else if (action.equals(ALARM_NOTIFICATION_INTENT)) {
			Log.d("NotificationReceiver: alarmNotificationIntent");
			ApplicationPreferences prefs = new ApplicationPreferences(context);
			String uploadState = prefs.getUploadState();
			if ((uploadState != null) && uploadState.contentEquals("uploadReported"))
				prefs.setUploadStateToAsk();
			iStartCrashReport.startUpload(context);
		} 
	}


	protected interface StartCrashReport {
		public void startCrashReport(Context context);
		public void startUpload(Context context);
	}

}
