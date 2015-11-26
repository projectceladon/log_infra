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

	/**
	 * Creates a notification for the given <code>GcmMessage</code>.
	 *
	 * If one or several messages have been notified and not yet
	 * dismissed, this method will chose whether the messages titles
	 * must be written in the notification (instead of the last message
	 * only).
	 *
	 * If too many messages are to be notified a generic message will
	 * be displayed instead.
	 *
	 * @param latestGcmMessage the latest <code>GcmMessage</code> that
	 * has been received.
	 */
	public void notifyGcmMessage(GcmMessage latestGcmMessage) {
		// Check that the message is not null
		if(null == latestGcmMessage) {
			Log.e("[GCM] Nothing to display for a <null> message.");
			return;
		}
		// Initialize the values that will be used for the notification
		PENDING_GCM_NOTIFICATIONS.add(latestGcmMessage);
		Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		long when = System.currentTimeMillis();
		CharSequence tickerText = "PhoneDoctor notification";
		String contentTitle = latestGcmMessage.getTitle();
		CharSequence contentText = latestGcmMessage.getText();
		int messageCount = PENDING_GCM_NOTIFICATIONS.size();
		Log.d("[GCM] " + messageCount + " messages to notify.");

		// Check whether several messages have to be notified
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
	 * Return the <code>PendingIntent</code> associated to the <i>dismiss</i> action.
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
	 * Clears the list of pending <i>GCM</i> notifications.
	 *
	 * @param aContext the context to use to retrieve the
	 * <code>NOTIFICATION_SERVICE</code> instance.
	 */
	public static void clearGcmNotification(Context aContext) {
		NotificationManager mNotificationManager = (NotificationManager)
				aContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(
				NOTIFICATION_GCM,
				NotificationMgr.NOTIF_CRASHTOOL);
		PENDING_GCM_NOTIFICATIONS.clear();
	}


	/**
	 * Returns a <code>boolean</code> indicating whether the sound is
	 * enabled or not.
	 * @return
	 * <ul>
	 * <li><code>true</code> if sound is enabled</li>
	 * <li><code>false</code> otherwise</li>
	 * </ul>
	 */
	private boolean gcmSoundNotificationEnabled() {
		ApplicationPreferences preferences = new ApplicationPreferences(this.context);
		boolean soundEnabled = preferences.isSoundEnabledForGcmNotifications();
		return soundEnabled;
	}
}
