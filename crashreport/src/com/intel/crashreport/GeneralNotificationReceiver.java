package com.intel.crashreport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GeneralNotificationReceiver extends BroadcastReceiver {

	// am broadcast -n com.intel.crashreport/.NotificationReceiver -a com.intel.crashreport.intent.CRASH_NOTIFY -c android.intent.category.ALTERNATIVE
	private static final String crashNotificationIntent = "com.intel.crashreport.intent.CRASH_NOTIFY";
	protected static final String bootCompletedIntent = "android.intent.action.BOOT_COMPLETED";
	private static final String networkStateChangeIntent = "android.net.conn.BACKGROUND_DATA_SETTING_CHANGED";
	private static final String alarmNotificationIntent = "com.intel.crashreport.intent.ALARM_NOTIFY";

	protected StartCrashReport iStartCrashReport;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(crashNotificationIntent)) {
			Log.d("NotificationReceiver: crashNotificationIntent");
			iStartCrashReport.startCrashReport(context);
		} else if (intent.getAction().equals(bootCompletedIntent)) {
			Log.d("NotificationReceiver: bootCompletedIntent");
			iStartCrashReport.startCrashReport(context);
		} else if (intent.getAction().equals(networkStateChangeIntent)) {
			Log.d("NotificationReceiver: networkStateChangeIntent");
			//first, we need to check GCM token
			CrashReport app = (CrashReport)context.getApplicationContext();
			if(app.isGcmEnabled())
				app.checkTokenGCM();
			iStartCrashReport.startCrashReport(context);
		} else if (intent.getAction().equals(alarmNotificationIntent)) {
			Log.d("NotificationReceiver: alarmNotificationIntent");
			ApplicationPreferences prefs = new ApplicationPreferences(context);
			String uploadState = prefs.getUploadState();
			if ((uploadState != null) && uploadState.contentEquals("uploadReported"))
				prefs.setUploadStateToAsk();
			iStartCrashReport.startUpload(context);
		}
	}

	protected interface StartCrashReport {
		public void startCrashReport(Context context);
		public void startUpload(Context context);
	}

}
