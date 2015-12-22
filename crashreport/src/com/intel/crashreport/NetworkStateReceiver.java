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
		if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
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
