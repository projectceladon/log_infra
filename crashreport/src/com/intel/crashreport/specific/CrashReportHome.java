
package com.intel.crashreport.specific;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.intel.crashreport.CrashReport;
import com.intel.crashreport.GeneralCrashReportHome;
import com.intel.crashreport.R;
import com.intel.crashreport.logconfig.ui.LogConfigHomeActivity;

public class CrashReportHome extends GeneralCrashReportHome {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Button button_logconfig = (Button) findViewById(R.id.button_logconfig);
		CrashReport app = (CrashReport) getApplicationContext();
		if(button_logconfig != null) {
			if(app.isUserBuild()) {
				button_logconfig.setVisibility(View.GONE);
			}
			// Attach a click listener for launching the system settings.
			button_logconfig.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(getApplicationContext(), LogConfigHomeActivity.class);
					startActivity(intent);
				}
			});
		}

		Button button_aplogs = (Button) findViewById(R.id.button_report_aplogs);
		if(button_aplogs != null) {
			button_aplogs.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
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
						Intent intent = new Intent(getApplicationContext(), UploadAplogActivity.class);
						startActivity(intent);
					}
				}
			});
		}
		Button button_list_gcm = (Button) findViewById(R.id.button_list_gcm_messages);
		if(null != button_list_gcm) {
			button_list_gcm.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(getApplicationContext(), ListGcmMessagesActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
			});
		}
	}

}
