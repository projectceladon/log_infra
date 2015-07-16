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

package com.intel.crashreport.specific;

import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.DropBoxManager;
import android.widget.Toast;

import com.intel.crashreport.CrashReport;
import com.intel.crashreport.CrashReportRequest;
import com.intel.crashreport.CrashReportService;
import com.intel.crashreport.database.EventDB;
import com.intel.crashreport.GeneralNotificationReceiver;
import com.intel.crashreport.Log;
import com.intel.crashreport.specific.GcmUtils;

public class NotificationReceiver extends GeneralNotificationReceiver {

	// am broadcast -n com.intel.crashreport/.NotificationReceiver -a com.intel.crashreport.intent.CRASH_NOTIFY -c android.intent.category.ALTERNATIVE
	private static final String CRASHLOGS_COPY_FINISHED_INTENT 	= "com.intel.crashreport.intent.CRASH_LOGS_COPY_FINISHED";
	private static final String EVENT_ID_EXTRA 					= "com.intel.crashreport.extra.EVENT_ID";
	private static final String RELAUNCH_CHECK_EVENTS_SERVICE 	= "com.intel.crashreport.intent.RELAUNCH_SERVICE";
	private static final String START_CRASHREPORT_SERVICE 		= "com.intel.crashreport.intent.START_CRASHREPORT";

	//PhoneInspectorService intent type
	public static final String EXTRA_TYPE			 	= "type";
	//PhoneInspectorService intent type values
	public static final String DROPBOX_ENTRY_ADDED 		= "DROPBOX_ENTRY_ADDED";
	public static final String MANAGE_FREE_SPACE 		= "MANAGE_FREE_SPACE";
	public static final String BOOT_COMPLETED 			= "BOOT_COMPLETED";

	@Override
	public void onReceive(Context context, Intent intent) {

		iStartCrashReport = new StartCrashReport(){

			@Override
			public void startCrashReport(Context context) {
				CrashReport app = (CrashReport)context.getApplicationContext();
				app.addRequest(new CrashReportRequest());
				if(!app.isCheckEventsServiceStarted())
					context.startService(new Intent(context, CheckEventsService.class));
			}

			@Override
			public void startUpload(Context context) {
				CrashReport app = (CrashReport)context.getApplicationContext();
				if(!app.isServiceStarted())
					context.startService(new Intent(context, CrashReportService.class));
			}

		};

		if (intent.getAction().equals(BOOT_COMPLETED_INTENT)) {
			Log.d("NotificationReceiver: bootCompletedIntent");
			//first, we need to check GCM token
			CrashReport app = (CrashReport)context.getApplicationContext();
			if(app.isGcmEnabled()){
				//Set context for case when intent is called before service
				GcmEvent.INSTANCE.setContext(app);
				GcmEvent.INSTANCE.checkTokenGCM();
			}
			super.onReceive(context, intent);
			//Add type to intent and send it
				Intent aIntent = new Intent(context, PhoneInspectorService.class);
				aIntent.putExtra(EXTRA_TYPE, BOOT_COMPLETED);
				context.startService(aIntent);
		} else if (intent.getAction().equals(START_CRASHREPORT_SERVICE)) {
			Log.d("NotificationReceiver: startCrashReportService");
			iStartCrashReport.startUpload(context);
		} else if (intent.getAction().equals(RELAUNCH_CHECK_EVENTS_SERVICE)) {
			CrashReport app = (CrashReport)context.getApplicationContext();
			Log.d("NotificationReceiver: relaunchCheckEventsService");
			app.setServiceRelaunched(false);
			if(!app.isCheckEventsServiceStarted())
				context.startService(new Intent(context, CheckEventsService.class));
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
							context.startService(new Intent(context, CrashReportService.class));
						else
							app.setNeedToUpload(true);
					}
					Intent aIntent = new Intent(context, PhoneInspectorService.class);
					aIntent.putExtra(EXTRA_TYPE, MANAGE_FREE_SPACE);
					context.startService(aIntent);
				}
			}
		} else if (intent.getAction().equals(DropBoxManager.ACTION_DROPBOX_ENTRY_ADDED)) {
			Log.d("NotificationReceiver: dropBoxEntryAddedIntent");

			//Add data to intent
			Intent aIntent = new Intent(context, PhoneInspectorService.class);
			aIntent.putExtra(EXTRA_TYPE, DROPBOX_ENTRY_ADDED);
			aIntent.putExtra(DropBoxManager.EXTRA_TAG, intent.getStringExtra(DropBoxManager.EXTRA_TAG));
			aIntent.putExtra(DropBoxManager.EXTRA_TIME, intent.getLongExtra(DropBoxManager.EXTRA_TIME, 0));

			context.startService(aIntent);
		}else if (intent.getAction().equals(GeneralNotificationReceiver.GCM_MARK_AS_READ)) {
			int rowId = -1;
			// Check whether a row id has been supplied
			if(intent.hasExtra(GcmMessage.GCM_ROW_ID)) {
				rowId = intent.getIntExtra(GcmMessage.GCM_ROW_ID, rowId);
				Log.d("[GCM] Got GCM message with row id: " + rowId);
			}
			// If we found a row id
			if(rowId >= 0) {
				// Mark the corresponding message as read
				markGcmMessageAsRead(rowId);
			} else {
				// If we don't have any row id, mark all messages as read
				markAllGcmMessagesAsRead();
				Toast.makeText(
					this.context,
					"All GCM messages marked as read.",
					Toast.LENGTH_SHORT).show();
			}
			// Cancel the notification
			GCMNotificationMgr.clearGcmNotification(this.context);
		} else if (intent.getAction().equals(Intent.ACTION_DEVICE_STORAGE_LOW)) {
			Intent aIntent = new Intent(context, PhoneInspectorService.class);
			aIntent.putExtra(EXTRA_TYPE, MANAGE_FREE_SPACE);
			context.startService(aIntent);
		} else {
			super.onReceive(context, intent);
		}
	}

	private void markGcmMessageAsRead(int rowId) {
		GcmUtils gcmUtils = new GcmUtils(this.context);
		gcmUtils.markAsRead(rowId);
	}

	private void markAllGcmMessagesAsRead() {
		GcmUtils gcmUtils = new GcmUtils(this.context);
		gcmUtils.markAllAsRead();
	}



}
