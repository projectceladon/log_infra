/* INTEL CONFIDENTIAL
 * Copyright 2015 Intel Corporation
 *
 * The source code contained or described herein and all documents
 * related to the source code ("Material") are owned by Intel
 * Corporation or its suppliers or licensors. Title to the Material
 * remains with Intel Corporation or its suppliers and
 * licensors. The Material contains trade secrets and proprietary
 * and confidential information of Intel or its suppliers and
 * licensors. The Material is protected by worldwide copyright and
 * trade secret laws and treaty provisions. No part of the Material
 * may be used, copied, reproduced, modified, published, uploaded,
 * posted, transmitted, distributed, or disclosed in any way without
 * Intel's prior express written permission.
 *
 * No license under any patent, copyright, trade secret or other
 * intellectual property right is granted to or conferred upon you
 * by disclosure or delivery of the Materials, either expressly, by
 * implication, inducement, estoppel or otherwise. Any license under
 * such intellectual property rights must be express and approved by
 * Intel in writing.
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
