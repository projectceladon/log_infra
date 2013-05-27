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
 * Author: Charles-Edouard Vidoine <charles.edouardx.vidoine@intel.com>
 */
package com.intel.crashreport.specific;

import java.util.TimerTask;

import com.intel.crashreport.Log;

import android.content.Context;
import android.content.Intent;
import android.database.SQLException;

public class NotifyCrashTask extends TimerTask{
	private String eventId;
	private Context context;
	private boolean isPresent;

	public NotifyCrashTask(String id,Context ctx){
		super();
		eventId = id;
		context = ctx;

	}

	@Override
	public void run() {
		EventDB db = new EventDB(context);
		isPresent = false;

		try {
			db.open();
			if (!db.eventDataAreReady(eventId)) {
				db.updateEventDataReady(eventId);
				isPresent = true;
			}
			db.close();
		}catch (SQLException e) {
			Log.w("NotifyCrashTask: Fail to access DB", e);
		}

		if (isPresent) {
			Intent intent = new Intent("com.intel.crashreport.intent.START_CRASHREPORT");
			context.sendBroadcast(intent);
		}

	}
}
