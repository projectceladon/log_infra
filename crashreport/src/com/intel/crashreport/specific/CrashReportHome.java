
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
		Button button_start = (Button) findViewById(R.id.button_report_events);
		// Attach a click listener for launching the system settings.
		button_start.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent("com.intel.crashreport.intent.START_SERVICE");
				intent.putExtra("com.intel.crashreport.extra.fromOutside", true);
				startActivity(intent);
			}
		});

		Button button_logconfig = (Button) findViewById(R.id.button_logconfig);
		CrashReport app = (CrashReport) getApplicationContext();
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

		Button button_aplogs = (Button) findViewById(R.id.button_report_aplogs);
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

}
