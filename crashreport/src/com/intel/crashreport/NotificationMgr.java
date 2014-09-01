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

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import com.intel.crashreport.bugzilla.ui.common.BugzillaMainActivity;

public class NotificationMgr {

	private final Context context;
	private static final int NOTIF_EVENT_ID = 1;
	private static final int NOTIF_UPLOAD_ID = 2;
	private static final int NOTIF_CRITICAL_EVENT_ID = 3;
	private static final int NOTIF_UPLOAD_WIFI_ONLY_ID = 4;
	public static final int NOTIF_CRASHTOOL = 5;
	private static final int NOTIF_CRASH_EVENT_ID = 6;
	public static final int NOTIF_BIGDATA_ID = 7;
	public static final int NOTIF_BZ_FAIL = 8;


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
		mNotificationManager.cancel(NOTIF_BIGDATA_ID);
	}

	public void notifyEventToUpload(int crashNumber, int uptimeNumber) {
		CharSequence tickerText;
		CharSequence contentTitle = context.getResources().getString(R.string.app_name);
		CharSequence contentText;

		// notification should be displayed only if at least one crash is present
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
		Intent notificationIntent = new Intent(context, StartServiceActivity.class);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		notificationIntent.putExtra("com.intel.crashreport.extra.fromOutside", true);
		PendingIntent contentIntent = PendingIntent.getActivity(context, NOTIF_EVENT_ID, notificationIntent, 0);
		Notification.Builder mBuilder =
		        new Notification.Builder(context)
				.setContentTitle(contentTitle)
				.setContentText(contentText)
				.setContentIntent(contentIntent)
				.setAutoCancel(true)
				.setTicker(tickerText)
				.setWhen(when)
				.setSmallIcon(icon);
		clearNonCriticalNotification();
		mNotificationManager.notify(NOTIF_EVENT_ID, mBuilder.build());

	}

	public void notifyUploadingLogs(int logNumber, int crashNumber) {
		CharSequence tickerText = "Start uploading event data";
		CharSequence contentTitle = "Phone Doctor";
		CharSequence contentText = "Uploading "+logNumber+" event data files";
		if (crashNumber>0){
			NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			int icon = R.drawable.icon;
			long when = System.currentTimeMillis();
			Intent notificationIntent = new Intent(context, StartServiceActivity.class);
			notificationIntent.putExtra("com.intel.crashreport.extra.fromOutside", true);
			notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			PendingIntent contentIntent = PendingIntent.getActivity(context, NOTIF_UPLOAD_ID, notificationIntent, 0);
			Notification.Builder mBuilder =
			        new Notification.Builder(context)
					.setContentTitle(contentTitle)
					.setContentText(contentText)
					.setContentIntent(contentIntent)
					.setWhen(when)
					.setTicker(tickerText)
					.setSmallIcon(icon);
			clearNonCriticalNotification();
			mNotificationManager.notify(NOTIF_UPLOAD_ID, mBuilder.build());
		}else{
			//no explicit notification but a warning log
			Log.w(contentText.toString());
		}
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
		Intent notificationIntent = new Intent(context, StartServiceActivity.class);
		notificationIntent.putExtra("com.intel.crashreport.extra.fromOutside", true);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(context, NOTIF_UPLOAD_WIFI_ONLY_ID, notificationIntent, 0);
		Notification.Builder mBuilder =
		        new Notification.Builder(context)
				.setContentTitle(contentTitle)
				.setContentText(contentText)
				.setContentIntent(contentIntent)
				.setWhen(when)
				.setAutoCancel(true)
				.setTicker(tickerText)
				.setSmallIcon(icon);
		clearNonCriticalNotification();
		mNotificationManager.notify(NOTIF_UPLOAD_WIFI_ONLY_ID, mBuilder.build());
	}

	public void notifyConnectWifiOrMpta() {
		CharSequence tickerText = "Phone Doctor event data to upload";
		CharSequence contentTitle = "Connect WiFi or use MPTA";
		CharSequence contentText = "You have too big(>10MB) data files to upload.\n\nPlease connect to WiFi or use MPTA to upload";
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.icon;
		long when = System.currentTimeMillis();
		Intent notificationIntent = new Intent(context, StartServiceActivity.class);
		notificationIntent.putExtra("com.intel.crashreport.extra.fromOutside", true);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(context, NOTIF_BIGDATA_ID, notificationIntent, 0);
		Notification.Builder mBuilder =
		        new Notification.Builder(context)
				.setContentTitle(contentTitle)
				.setContentText(contentText)
				.setContentIntent(contentIntent)
				.setWhen(when)
				.setAutoCancel(true)
				.setTicker(tickerText)
				.setSmallIcon(icon);
		clearNonCriticalNotification();
		mNotificationManager.notify(NOTIF_BIGDATA_ID, mBuilder.build());
	}

	public void notifyBZFailure() {
		CharSequence tickerText = "Phone Doctor BZ failure";
		CharSequence contentTitle = "BZ could not be created";
		CharSequence contentText = "Please retry or contact support to clean your device";
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.icon;
		long when = System.currentTimeMillis();
		Intent notificationIntent = new Intent(context, BugzillaMainActivity.class);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(context, NOTIF_BZ_FAIL, notificationIntent, 0);
		Notification.Builder mBuilder =
		        new Notification.Builder(context)
				.setContentTitle(contentTitle)
				.setContentText(contentText)
				.setContentIntent(contentIntent)
				.setWhen(when)
				.setAutoCancel(true)
				.setTicker(tickerText)
				.setSmallIcon(icon);
		clearNonCriticalNotification();
		mNotificationManager.notify(NOTIF_BZ_FAIL, mBuilder.build());
	}

	/**
	 * Remove Event data file no wifi notification
	 */
	public void cancelNotifyEventDataWifiOnly() {
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(NOTIF_UPLOAD_WIFI_ONLY_ID);
	}

	/**
	 * Remove Critical events notification
	 */
	public void cancelNotifCriticalEvent(){
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(NOTIF_CRITICAL_EVENT_ID);
	}

	/**
	 * Remove no Critical events notification
	 */
	public void cancelNotifNoCriticalEvent(){
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(NOTIF_CRASH_EVENT_ID);
	}

	public void notifyCriticalEvent(int criticalEventNumber, int crashNumber){

		ApplicationPreferences prefs = new ApplicationPreferences(context);

		CharSequence tickerText = "Critical events occured";
		CharSequence contentText ;
		if( criticalEventNumber > 1)
			contentText = criticalEventNumber+" critical events occured";
		else contentText = "A critical event occured";

		int icon = R.drawable.icon_critical;
		if(criticalEventNumber > 0 || crashNumber > 0)
			clearNonCriticalNotification();
		if(criticalEventNumber > 0)
			notifyCrashOrCriticalEvent(NOTIF_CRITICAL_EVENT_ID, icon, tickerText, contentText);

		if(prefs.isNotificationForAllCrash() && (crashNumber > 0)) {
			tickerText = "Crashes occured";
			icon = R.drawable.icon_crash;
			if( crashNumber > 1)
				contentText = crashNumber+" crashes occured";
			else contentText = "A crash occured";
			notifyCrashOrCriticalEvent(NOTIF_CRASH_EVENT_ID, icon, tickerText, contentText);
		}

	}

	public void notifyCrashOrCriticalEvent(int notifId, int icon, CharSequence tickerText, CharSequence contentText) {
		long when = System.currentTimeMillis();
		CharSequence contentTitle = context.getResources().getString(R.string.app_name);
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent notificationIntent = new Intent(context, StartServiceActivity.class);
		notificationIntent.putExtra("com.intel.crashreport.extra.fromOutside", true);
		notificationIntent.putExtra("com.intel.crashreport.extra.notifyEvents", true);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(context, notifId, notificationIntent, 0);
		Notification.Builder mBuilder =
		        new Notification.Builder(context)
				.setContentTitle(contentTitle)
				.setContentText(contentText)
				.setContentIntent(contentIntent)
				.setWhen(when)
				.setTicker(tickerText)
				.setSmallIcon(icon);
		mNotificationManager.notify(notifId, mBuilder.build());
	}



}
