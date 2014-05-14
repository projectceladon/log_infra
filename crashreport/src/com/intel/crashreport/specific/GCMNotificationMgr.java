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

package com.intel.crashreport.specific;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import com.intel.crashreport.ApplicationPreferences;
import com.intel.crashreport.GeneralNotificationReceiver;
import com.intel.crashreport.Log;
import com.intel.crashreport.NotificationMgr;
import com.intel.crashreport.R;
import com.intel.crashreport.bugzilla.ui.common.BugzillaMainActivity;
import com.intel.crashreport.specific.GcmMessage;
import com.intel.crashreport.specific.GcmMessageViewAdapter;
import com.intel.crashreport.specific.ListGcmMessagesActivity;

public class GCMNotificationMgr {

	private final Context context;


	/**
	 * The maximum number of messages we want to display within
	 * a notification.
	 */
	private static final int MAX_GCM_DETAILS = 4;

	public static final String NOTIFICATION_GCM = "GCM";

	private static final List<GcmMessage> PENDING_GCM_NOTIFICATIONS = new ArrayList<GcmMessage>();

	public GCMNotificationMgr(Context context) {
		this.context = context;
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
				NotificationMgr.NOTIF_CRASHTOOL,
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
				NotificationMgr.NOTIF_CRASHTOOL);
		PENDING_GCM_NOTIFICATIONS.clear();
	}

	private boolean gcmSoundNotificationEnabled() {
		ApplicationPreferences preferences = new ApplicationPreferences(this.context);
		boolean soundEnabled = preferences.isSoundEnabledForGcmNotifications();
		return soundEnabled;
	}
}
