package com.intel.crashreport.bugzilla.ui.common;

import java.util.ArrayList;

import com.intel.crashreport.ApplicationPreferences;
import com.intel.crashreport.CrashReport;
import com.intel.crashreport.NotificationMgr;
import com.intel.crashreport.R;
import com.intel.crashreport.bugzilla.ui.specific.BZCreator;
import com.intel.crashreport.specific.CrashReportHome;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class BugzillaSummaryActivity extends Activity {

	private Context context = this;
	private boolean fromGallery;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bugzilla_summary);

		Button sendButton = (Button) findViewById(R.id.bugzilla_send_button);
		Button editButton = (Button) findViewById(R.id.bugzilla_edit_button);

		if(sendButton != null) {
			sendButton.setOnClickListener(new OnClickListener(){

				public void onClick(View v) {
					Button editButton = (Button) findViewById(R.id.bugzilla_edit_button);
					if(editButton != null) {
						editButton.setEnabled(false);
					}
					v.setEnabled(false);
					new CreateBzTask().execute();
					AlertDialog alert = new AlertDialog.Builder(context).create();
					alert.setMessage("A background request of new bugzilla creation has been submitted.");
					alert.setButton(DialogInterface.BUTTON_NEUTRAL,"OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							comeBack();
						}
					});
					alert.show();
				}

			});
		}

		if(editButton != null) {
			editButton.setOnClickListener(new OnClickListener(){

				public void onClick(View v) {
					finish();
					Intent intent = new Intent(getApplicationContext(),BugzillaMainActivity.class);
					Intent incomingIntent = getIntent();
					if(incomingIntent != null) {
						Bundle incomingExtras = incomingIntent.getExtras();
						if(incomingExtras != null) {
							intent.putExtras(incomingExtras);
						}
					}
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}

			});
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		TextView infos = (TextView) findViewById(R.id.bugzilla_summary_text);
		CrashReport app = (CrashReport)getApplicationContext();
		BugStorage bugzillaStorage = app.getBugzillaStorage();
		String text = "";
		text += "Summary : ["+bugzillaStorage.getBugType()+"]";
		text += bugzillaStorage.getSummary() + "\n \n";
		text += "Component : "+bugzillaStorage.getComponent()+ "\n";
		text += "Severity : "+bugzillaStorage.getSeverity()+ "\n";
		text += "Description : "+bugzillaStorage.getDescription()+ "\n";
		if(!bugzillaStorage.getTime().equals(""))
			text += "Issue occured in last " +  bugzillaStorage.getTime()+ ".\n \n";
		text += "With"+ (bugzillaStorage.getBugHasScreenshot()?" ":"out ")+"screenshot(s)\n";
		text += (bugzillaStorage.getLogLevel() == 0)?"Without":"With";
		text += " Aplogs attached";
		if(infos != null) {
			infos.setText(text);
		}
		if(bugzillaStorage.getSummary().equals("") || bugzillaStorage.getBugType().equals("") || bugzillaStorage.getComponent().equals("")
				|| bugzillaStorage.getSeverity().equals("") || bugzillaStorage.getDescription().equals("")){
			comeBack();
		}
	}

	@Override
	public void onStart() {
		Intent intent = getIntent();
		fromGallery = intent.getBooleanExtra("com.intel.crashreport.bugzilla.fromgallery", false);
		super.onStart();
	}

	private class CreateBzTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			ArrayList<String> sArguments = new ArrayList<String>();

			CrashReport app = (CrashReport)getApplicationContext();
			BugStorage bugzillaStorage = app.getBugzillaStorage();
			if(!bugzillaStorage.getSummary().equals("") && !bugzillaStorage.getDescription().equals("")){
				String line = "";
				if (bugzillaStorage.getLogLevel()>=0){
					line = "APLOG="+bugzillaStorage.getLogLevel()+"\n";
					sArguments.add(line);
				}

		        if(!app.isUserBuild()) {
				String[] bplogTypesValues = getResources().getStringArray(R.array.reportBugzillaBplogTypeValues);
				for (int j=0; j<bplogTypesValues.length;j++) {
					if(bplogTypesValues[j].equals(bugzillaStorage.getBugType())) {
						line = "BPLOG=1\n";
						sArguments.add(line);
						break;
					}
				}
			}
				line = "SUMMARY="+bugzillaStorage.getSummary()+"\n";
				sArguments.add(line);
				line = "TYPE="+bugzillaStorage.getBugType()+"\n";
				sArguments.add(line);
				Boolean bzMode = new ApplicationPreferences(app).isBugzillaModuleInTestMode();
				if (!bzMode) {
					String[] componentText = getResources().getStringArray(R.array.reportBugzillaComponentText);
					String[] componentValues = getResources().getStringArray(R.array.reportBugzillaComponentValues);
					if( componentText.length == componentValues.length) {
						for (int i=0; i<componentText.length;i++) {
							if(componentText[i].equals(bugzillaStorage.getComponent())) {
								line = "COMPONENT="+componentValues[i]+ "\n";
								break;
							}
						}
					}
				}
				else
					line = "COMPONENT=Test Component\n";
				sArguments.add(line);
				line = "SEVERITY="+bugzillaStorage.getSeverity()+"\n";
				sArguments.add(line);
				String strDescription = bugzillaStorage.getDescription();
				strDescription = strDescription.replace("\n", "\\n");
				if(!strDescription.endsWith("\\n"))
					strDescription += "\\n";
				if(!bugzillaStorage.getTime().equals(""))
					strDescription += "Issue occured in last " + bugzillaStorage.getTime();
				line = "DESCRIPTION="+strDescription+"\n";
				sArguments.add(line);
				if(bugzillaStorage.getBugHasScreenshot()) {
					ArrayList<String> screenshots = bugzillaStorage.getScreenshotPath();
					for(String screenshot:screenshots) {
						line = "SCREENSHOT=/mnt/sdcard/Pictures/Screenshots/"+screenshot+"\n";
						sArguments.add(line);
					}
				}
				line = "USERFIRSTNAME="+app.getUserFirstName()+"\n";
				sArguments.add(line);
				line = "USERLASTNAME="+app.getUserLastName()+"\n";
				sArguments.add(line);
				line = "USEREMAIL="+app.getUserEmail()+"\n";
				sArguments.add(line);
				if (bzMode) {
					line = "TEST=true\n";
					sArguments.add(line);
				}
				//Create a file to trigger crashlog daemon with arguments previously set
				if (BZCreator.INSTANCE.createBZ(sArguments,context)) {
					bugzillaStorage.clearValues();
				} else {
					//we should notify the user of the BZ creation failure
					NotificationMgr nMgr = new NotificationMgr(getApplicationContext());
					//test  : to be updated with BZ error notification
					nMgr.notifyBZFailure();
				}
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Void... params) {
		}

		protected void onPostExecute(Void... params) {

		}

	}

	public void comeBack() {
		finish();
		if (!fromGallery) {
			Intent intent = new Intent(getApplicationContext(), CrashReportHome.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
		else {
			Intent startMain = new Intent(Intent.ACTION_MAIN);
			startMain.addCategory(Intent.CATEGORY_HOME);
			startMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(startMain);
		}
	}

	@Override
	public void onBackPressed() {
		comeBack();
	}


}
