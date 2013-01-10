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
	private static final int NOTIF_CRITICAL_EVENT_ID = 3;
	private static final int NOTIF_UPLOAD_WIFI_ONLY_ID = 4;

	public NotificationMgr(Context context) {
		this.context = context;
	}

	/**
	 * Clear all pending notifications except critical event one.
	 */
	public void clearNonCriticalNotification() {
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(NOTIF_EVENT_ID);
		mNotificationManager.cancel(NOTIF_UPLOAD_ID);
		mNotificationManager.cancel(NOTIF_UPLOAD_WIFI_ONLY_ID);
	}

	public void notifyEventToUpload(int crashNumber, int uptimeNumber) {
		CharSequence tickerText;
		CharSequence contentTitle = "PSI Phone Doctor";
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
		clearNonCriticalNotification();
		mNotificationManager.notify(NOTIF_EVENT_ID, notification);
	}

	public void notifyUploadingLogs(int logNumber) {
		CharSequence tickerText = "Start uploading event data";
		CharSequence contentTitle = "Phone Doctor";
		CharSequence contentText = "Uploading "+logNumber+" event data files";
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.icon;
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, tickerText, when);
		Intent notificationIntent = new Intent(context, StartServiceActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		clearNonCriticalNotification();
		mNotificationManager.notify(NOTIF_UPLOAD_ID, notification);
	}

	public void cancelNotifUploadingLogs() {
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(NOTIF_UPLOAD_ID);
	}

	/**
	 * Display a notification when there are event data files
	 * to upload while wifi is not available.
	 *
	 * @param logNumber Number of data event files to upload
	 */
	public void notifyEventDataWifiOnly(int logNumber) {
		CharSequence tickerText = "Phone Doctor event data to upload";
		CharSequence contentTitle = "Connect WiFi";
		CharSequence contentText = "You have "+logNumber+" event data files to upload.\n\nPlease connect to WiFi or disable WiFi only option";
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.icon;
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, tickerText, when);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		Intent notificationIntent = new Intent(context, StartServiceActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		clearNonCriticalNotification();
		mNotificationManager.notify(NOTIF_UPLOAD_WIFI_ONLY_ID, notification);
	}

	/**
	 * Remove Event data file no wifi notifixation
	 */
	public void cancelNotifyEventDataWifiOnly() {
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(NOTIF_UPLOAD_WIFI_ONLY_ID);
	}

	public void cancelNotifCriticalEvent(){
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(NOTIF_CRITICAL_EVENT_ID);
	}

	public void notifyCriticalEvent(int crashNumber){
		CharSequence tickerText = "Critical events occured";
		CharSequence contentTitle = "Phone Doctor";
		CharSequence contentText ;
		if( crashNumber > 1)
			contentText = crashNumber+" critical events occured";
		else contentText = crashNumber+" critical event occured";
		int icon = R.drawable.icon;
		long when = System.currentTimeMillis();
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(icon, tickerText, when);
		Intent notificationIntent = new Intent(context, NotifyEventActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		clearNonCriticalNotification();
		mNotificationManager.notify(NOTIF_CRITICAL_EVENT_ID, notification);
	}

}
