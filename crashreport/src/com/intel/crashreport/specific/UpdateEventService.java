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
