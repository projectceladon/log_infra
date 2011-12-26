package com.intel.crashreport;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;

public class NetworkStateReceiver extends BroadcastReceiver {


	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			Log.d("NetworkStateReceiver: CONNECTIVITY_ACTION");
			if (isNetworkAvailable(context)) {
				Log.d("NetworkStateReceiver: connection available");
				PackageManager pm = context.getPackageManager();
				pm.setComponentEnabledSetting(new ComponentName(context, NetworkStateReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				context.startService(new Intent(context, CrashReportService.class));
			} else
				Log.d("NetworkStateReceiver: connection not available");
		}
	}

	private boolean isNetworkAvailable(Context context) {
		Connector conn = new Connector(context);
		return conn.getDataConnectionAvailability();
	}

}
