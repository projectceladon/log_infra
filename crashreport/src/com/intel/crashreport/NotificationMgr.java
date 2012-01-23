/* Crash Report (CLOTA)
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

package com.intel.crashreport;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class NotificationMgr {

	private Context context;
	private static final int NOTIF_EVENT_ID = 1;
	private static final int NOTIF_UPLOAD_ID = 2;

	public NotificationMgr(Context context) {
		this.context = context;
	}

	public void notifyEventToUpload(int crashNumber, int uptimeNumber) {
		CharSequence tickerText;
		CharSequence contentTitle = "Crash Report";
		CharSequence contentText;
		if ((crashNumber == 0) && (uptimeNumber != 0)) {
			tickerText = "Uptime event";
			contentText = "Uptime event to report";
		} else if ((crashNumber == 1) && (uptimeNumber == 0)) {
			tickerText = "Crash event";
			contentText = "1 Crash event to report";
		} else if ((crashNumber == 1) && (uptimeNumber != 0)) {
			tickerText = "Uptime and Crash events";
			contentText = "Uptime and 1 Crash events to report";
		} else if ((crashNumber > 1) && (uptimeNumber == 0)) {
			tickerText = "Crash events";
			contentText = crashNumber+" Crash events to report";
		} else if ((crashNumber > 1) && (uptimeNumber != 0)) {
			tickerText = "Uptime and Crash events";
			contentText = "Uptime and "+crashNumber+" Crash events to report";
		} else {
			tickerText = "New event";
			contentText = "New events to upload";
		}
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.icon;
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, tickerText, when);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		Intent notificationIntent = new Intent(context, StartServiceActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		mNotificationManager.notify(NOTIF_EVENT_ID, notification);
	}

	public void notifyUploadingLogs(int logNumber) {
		CharSequence tickerText = "Start uploading crash logs";
		CharSequence contentTitle = "Crash Report";
		CharSequence contentText = "Uploading "+logNumber+" crash logs";
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.icon;
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, tickerText, when);
		Intent notificationIntent = new Intent(context, StartServiceActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		mNotificationManager.notify(NOTIF_UPLOAD_ID, notification);
	}

	public void cancelNotifUploadingLogs() {
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(NOTIF_UPLOAD_ID);
	}

}
