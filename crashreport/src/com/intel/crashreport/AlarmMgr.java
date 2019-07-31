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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class AlarmMgr {

	private Context context;
	private static final Intent intent = new Intent("com.intel.crashreport.intent.ALARM_NOTIFY");
	private PendingIntent pendingIntent;

	public AlarmMgr(Context context) {
		this.context = context;
		pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
	}

	public int setDateToRetryOneHour() {
		return setDateToRetry(AlarmManager.INTERVAL_HOUR);
	}

	public int setDateToRetry(long delayMillis) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		long setTime = System.currentTimeMillis() + delayMillis;
		Log.d("AlarmMgr: setDateToRetry: " + setTime);
		alarmManager.set(AlarmManager.RTC, setTime, pendingIntent);
		ApplicationPreferences prefs = new ApplicationPreferences(context);
		prefs.saveAlarmDate(setTime);
		return toMinutes(delayMillis);
	}

	public int checkDateToRetry() {
		ApplicationPreferences prefs = new ApplicationPreferences(context);
		long savedAlarm = prefs.getAlarmDate();
		long now = System.currentTimeMillis();
		long inOneHour = now + AlarmManager.INTERVAL_HOUR;
		if ((savedAlarm < now) || (savedAlarm > inOneHour)) {
			setDateToRetryOneHour();
			return toMinutes(AlarmManager.INTERVAL_HOUR);
		} else
			return toMinutes(savedAlarm - now);
	}

	public void removeAlarmIfAny() {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
	}

	private int toMinutes(long delayMillis) {
		final int MILLI_TO_MINUTES_COEF = 60000;
		int time = (int)(delayMillis / MILLI_TO_MINUTES_COEF);
		return time;
	}

}
