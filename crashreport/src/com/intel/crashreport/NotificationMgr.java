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

	/**
	 * The maximum number of messages we want to display within
	 * a notification.
	 */
	private static final int MAX_GCM_DETAILS = 4;

	public static final String NOTIFICATION_GCM = "GCM";

	private static final List<GcmMessage> PENDING_GCM_NOTIFICATIONS = new ArrayList<GcmMessage>();

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
		CharSequence contentTitle = "PSI Phone Doctor";
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
		CharSequence contentTitle = "PSI Phone Doctor";
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

	public void notifyGcmMessage(GcmMessage latestGcmMessage) {
		// Check that the message is not null
		if(null == latestGcmMessage) {
			Log.e("[GCM] Nothing to display for a <null> message.");
			return;
		}
		// Initialize the values that will be used for the notification
		PENDING_GCM_NOTIFICATIONS.add(latestGcmMessage);
		CharSequence tickerText = "PhoneDoctor notification";
		int rowId = latestGcmMessage.getRowId();
		String contentTitle = latestGcmMessage.getTitle();
		CharSequence contentText = latestGcmMessage.getText();
		int messageCount = PENDING_GCM_NOTIFICATIONS.size();
		Log.d("[GCM] " + messageCount + " messages to notify.");
		if(messageCount > 1 && messageCount <= MAX_GCM_DETAILS) {
			contentTitle = "PhoneDoctor notifications (" + messageCount + ")";
			tickerText = contentTitle;
			StringBuilder sb = new StringBuilder();
			for(GcmMessage current : PENDING_GCM_NOTIFICATIONS) {
				sb.append(GcmMessageViewAdapter.formatDate(
						current.getDate(),
						context));
				sb.append(": ");
				sb.append(current.getTitle());
				sb.append("\n");
			}
			contentText = sb.toString();
		} else if(messageCount > MAX_GCM_DETAILS) {
			tickerText = "PhoneDoctor notifications ("+messageCount+")";
			contentTitle = "PhoneDoctor notifications";
			contentText = messageCount + " messages.\nClick to view.";
		}
		Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		long when = System.currentTimeMillis();

		// Create the pending intent that will be user to the notification click
		int icon = R.drawable.icon_phonedoctor;
		PendingIntent contentIntent = this.getContentPendingIntent(messageCount, latestGcmMessage);

		// Create the pending indent that will be used for the notification's action
		int ignoreIcon = R.drawable.ic_gcm_dismiss;
		PendingIntent ignore = this.getIgnorePendingIntent(messageCount, latestGcmMessage);

		// Compute the appropriate label for the 'dismiss' button
		String dismissMessage = "Dismiss";
		if(messageCount > 1) {
			dismissMessage = "Dismiss all";
		}

		// Create a notification builder with the appropriate parameters
		Log.d("[GCM] Creating notification with content text: " + contentText);
		Notification.Builder mBuilder =
		        new Notification.Builder(context)
				.setContentTitle(contentTitle)
				.setContentText(contentText)
				.setContentIntent(contentIntent)
				.setWhen(when)
				.setAutoCancel(true)
				.setTicker(tickerText)
				.setSmallIcon(icon)
				.setNumber(messageCount)
				.setStyle(new Notification.BigTextStyle().bigText(contentText))
				.addAction(ignoreIcon, dismissMessage, ignore);
		// Add sound if needed
		if(gcmSoundNotificationEnabled()) {
			mBuilder.setSound(soundUri);
		}
		// Trigger the notification
		NotificationManager mNotificationManager = (NotificationManager)
				context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(
				NOTIFICATION_GCM,
				NotificationMgr.NOTIF_CRASHTOOL,
				mBuilder.build());
	}

	private PendingIntent getContentPendingIntent(int messageCount, GcmMessage latestMessage) {
		int rowId = latestMessage.getRowId();
		Intent notificationIntent = new Intent(context, ListGcmMessagesActivity.class);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		notificationIntent.putExtra(GcmMessage.GCM_ORIGIN, NotificationMgr.NOTIF_CRASHTOOL);
		if(messageCount == 1) {
			notificationIntent.putExtra(GcmMessage.GCM_ROW_ID, rowId);
		}
		PendingIntent contentIntent = PendingIntent.getActivity(
				context,
				NOTIF_CRASHTOOL,
				notificationIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		return contentIntent;
	}

	/**
	 * Return the <code>PendingIntent</code> associated to the <i>click</i>
	 * on item action.
	 *
	 * @param messageCount the total number of messages in the notification
	 *
	 * @param latestMessage the latest GCM message instance.
	 *
	 * @return the <code>PendingIntent</code> instance to use in notification.
	 */
	private PendingIntent getIgnorePendingIntent(int messageCount, GcmMessage latestMessage) {
		int rowId = latestMessage.getRowId();
		Intent ignoreIntent = new Intent();
		ignoreIntent.setAction(GeneralNotificationReceiver.GCM_MARK_AS_READ);
		if(messageCount == 1) {
			Log.d("[GCM] Adding " + rowId + " row id to \"ignore\" Intent.");
			ignoreIntent.putExtra(GcmMessage.GCM_ROW_ID, rowId);
		}
		ignoreIntent.setFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
		PendingIntent ignore = PendingIntent.getBroadcast(
				context,
				NotificationMgr.NOTIF_CRASHTOOL,
				ignoreIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		return ignore;
	}

	/**
	 * Return the <code>PendingIntent</code> associated to the <i>dismiss</i> action.
	 *
	 * @param messageCount the total number of messages in the notification
	 *
	 * @param latestMessage the latest GCM message instance.
	 *
	 * @return the <code>PendingIntent</code> instance to use in notification.
	 */
	public static void clearGcmNotification(Context aContext) {
		NotificationManager mNotificationManager = (NotificationManager)
				aContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(
				NOTIFICATION_GCM,
				NOTIF_CRASHTOOL);
		PENDING_GCM_NOTIFICATIONS.clear();
	}

	private boolean gcmSoundNotificationEnabled() {
		ApplicationPreferences preferences = new ApplicationPreferences(this.context);
		boolean soundEnabled = preferences.isSoundEnabledForGcmNotifications();
		return soundEnabled;
	}
}
