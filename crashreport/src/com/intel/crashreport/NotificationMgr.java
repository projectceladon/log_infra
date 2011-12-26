package com.intel.crashreport;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class NotificationMgr {

	private Context context;
	private static final int NOTIF_EVENT_ID = 1;

	public NotificationMgr(Context context) {
		this.context = context;
	}

	public void notifyEventToUpload() {
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.icon;
		CharSequence tickerText = "Crash events";
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, tickerText, when);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		CharSequence contentTitle = "Crash Report";
		CharSequence contentText = "New events to upload";
		Intent notificationIntent = new Intent(context, StartServiceActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		mNotificationManager.notify(NOTIF_EVENT_ID, notification);
	}

}
