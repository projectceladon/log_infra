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

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
public class NetworkStateReceiver extends BroadcastReceiver {



	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			Log.d("NetworkStateReceiver: Async process creation");
			LongNetworkCheck aCheck = new LongNetworkCheck();
			aCheck.contextASync = context;
			//goAsync() is required to keep top priority for asyncTask
			aCheck.result = goAsync();
			aCheck.execute("");
		}
	}

	private boolean isNetworkAvailable(Context context) {
		Connector conn = new Connector(context);
		return conn.getDataConnectionAvailability();
	}

	private class LongNetworkCheck extends AsyncTask<String, Void, String> {
		protected Context contextASync;
		protected PendingResult result;
		@Override
		protected String doInBackground(String... params) {
			Log.d("NetworkStateReceiver: CONNECTIVITY_ACTION");
			if (isNetworkAvailable(contextASync)) {
				Log.d("NetworkStateReceiver: connection available");
				PackageManager pm = contextASync.getPackageManager();
				pm.setComponentEnabledSetting(new ComponentName(contextASync, NetworkStateReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				CrashReport app = (CrashReport)contextASync.getApplicationContext();
				if(!app.isServiceStarted())
					contextASync.startService(new Intent(contextASync, CrashReportService.class));
			}else{
				Log.d("NetworkStateReceiver: connection not available");
			}
			if (result != null){
				result.finish();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}



}
