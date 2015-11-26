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
