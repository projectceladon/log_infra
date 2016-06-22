/* INTEL CONFIDENTIAL
 * Copyright 2016 Intel Corporation
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

import android.app.IntentService;
import android.database.SQLException;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;

import com.intel.crashreport.CrashReport;
import com.intel.crashreport.CrashReportService;
import com.intel.crashreport.database.EventDB;
import com.intel.crashreport.Log;

import com.intel.phonedoctor.utils.FileOps;

public class UpdateEventService extends IntentService {
	private static final String TAG = "UpdateEventService";
	public static final String EVENT_ID = "EventID";

	public UpdateEventService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		if (extras != null && intent.hasExtra(EVENT_ID)) {
			CrashReport app = (CrashReport)getApplicationContext();
			String eventId = extras.getString(EVENT_ID);
			EventDB db = new EventDB(this);
			boolean isPresent = false;
			try {
				db.open();
				if (db.isEventInDb(eventId)) {

					if (!db.eventDataAreReady(eventId)) {
						String path = db.getLogDirByEventId(eventId);
						FileOps.compressFolderContent(path);

						isPresent = true;
						db.updateEventDataReady(eventId);
					}
				}
			} catch (SQLException e) {
				Log.w("UpdateEventService: Fail to access DB", e);
			}
			db.close();
			if(isPresent) {
				if(!app.isServiceStarted())
					startServiceAsUser(new Intent(this, CrashReportService.class), UserHandle.CURRENT);
				else
					app.setNeedToUpload(true);
			}
		}

		Intent aIntent = new Intent(this, PhoneInspectorService.class);
		aIntent.putExtra(NotificationReceiver.EXTRA_TYPE,
			NotificationReceiver.MANAGE_FREE_SPACE);
		startServiceAsUser(aIntent, UserHandle.CURRENT);
	}
}
