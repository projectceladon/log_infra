
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
			mainMenuAdapter.add(new HomeScreenElement(R.id.button_logconfig, getString(R.string.button_logconfig_text), R.drawable.advanced_logs, 6));
		mainMenuAdapter.add(new HomeScreenElement(R.id.button_report_aplogs, getString(R.string.menu_aplogs), R.drawable.upload_logs, 5));
		mainMenuAdapter.add(new HomeScreenElement(R.id.button_list_gcm_messages, getString(R.string.menu_gcm_list), R.drawable.check_gcm, 3));
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
			default:
				super.handleMenuAction(action);
		}
	}
}
