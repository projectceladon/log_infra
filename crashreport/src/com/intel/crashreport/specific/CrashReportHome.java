/* Copyright (C) 2019 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.crashreport.specific;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.view.View;
import android.widget.Button;
import java.util.ArrayList;
import java.util.List;

import com.intel.crashreport.HomeScreenElement;
import com.intel.crashreport.CrashReport;
import com.intel.crashreport.GeneralCrashReportHome;
import com.intel.crashreport.R;
import com.intel.crashreport.logconfig.ui.LogConfigHomeActivity;

public class CrashReportHome extends GeneralCrashReportHome {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		CrashReport app = (CrashReport) getApplicationContext();
		if(!app.isUserBuild())
			mainMenuAdapter.add(new HomeScreenElement(R.id.button_logconfig,
				getString(R.string.button_logconfig_text), R.drawable.advanced_logs, 6,
				getResources().getBoolean(R.bool.enable_logconfig)));
		mainMenuAdapter.add(new HomeScreenElement(R.id.button_report_aplogs,
			getString(R.string.menu_aplogs), R.drawable.upload_logs, 5,
			getResources().getBoolean(R.bool.enable_aplogs)));
		mainMenuAdapter.add(new HomeScreenElement(R.id.button_list_gcm_messages,
			getString(R.string.menu_gcm_list), R.drawable.check_gcm, 3,
			getResources().getBoolean(R.bool.enable_gcm)));
		mainMenuAdapter.add(new HomeScreenElement(R.id.button_device_info,
			getString(R.string.menu_device_info), R.drawable.device_info, 7,
			getResources().getBoolean(R.bool.enable_device_info)));
	}

	@Override
	protected void handleMenuAction(int action) {
		Intent intent;
		switch (action) {
			case (R.id.button_logconfig):
				intent = new Intent(getApplicationContext(), LogConfigHomeActivity.class);
				startActivityAsUser(intent, UserHandle.CURRENT);
			break;
			case (R.id.button_report_aplogs):
				CrashReport app = (CrashReport) getApplicationContext();
				if(app.isUserBuild()) {
					new UploadAplogTask(context).execute();
					AlertDialog alert = new AlertDialog.Builder(context).create();
					alert.setMessage("A background request of log upload has been created. \n ");
					alert.setButton(DialogInterface.BUTTON_NEUTRAL,"OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
						}
					});
					alert.show();
				}
				else {
					intent = new Intent(getApplicationContext(), UploadAplogActivity.class);
					startActivityAsUser(intent, UserHandle.CURRENT);
				}

			break;
			case (R.id.button_list_gcm_messages):
				intent = new Intent(getApplicationContext(), ListGcmMessagesActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivityAsUser(intent, UserHandle.CURRENT);
			break;
			case (R.id.button_device_info):
				intent = new Intent(getApplicationContext(), DeviceInfoActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivityAsUser(intent, UserHandle.CURRENT);
			break;
			default:
				super.handleMenuAction(action);
		}
	}
}
