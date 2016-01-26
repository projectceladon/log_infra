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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
				startActivity(intent);
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
					startActivity(intent);
				}

			break;
			case (R.id.button_list_gcm_messages):
				intent = new Intent(getApplicationContext(), ListGcmMessagesActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			break;
			case (R.id.button_device_info):
				intent = new Intent(getApplicationContext(), DeviceInfoActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			break;
			default:
				super.handleMenuAction(action);
		}
	}
}
