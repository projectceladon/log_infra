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
