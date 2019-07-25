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
import android.os.UserHandle;
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
					startActivityAsUser(intent, UserHandle.CURRENT);
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
		if(!bugzillaStorage.getTime().isEmpty())
			text += "Issue occured in last " +  bugzillaStorage.getTime()+ ".\n \n";
		text += "With"+ (bugzillaStorage.getBugHasScreenshot()?" ":"out ")+"screenshot(s)\n";
		text += (bugzillaStorage.getLogLevel() == 0)?"Without":"With";
		text += " Aplogs attached";
		if(infos != null) {
			infos.setText(text);
		}
		if(bugzillaStorage.getSummary().isEmpty() || bugzillaStorage.getBugType().isEmpty() || bugzillaStorage.getComponent().isEmpty()
				|| bugzillaStorage.getSeverity().isEmpty()|| bugzillaStorage.getDescription().isEmpty()){
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
			if(!bugzillaStorage.getSummary().isEmpty() && !bugzillaStorage.getDescription().isEmpty()){
				String line;
				if (bugzillaStorage.getLogLevel()>=0){
					line = "APLOG="+bugzillaStorage.getLogLevel()+"\n";
					sArguments.add(line);
				}

				ApplicationPreferences appPrefs = new ApplicationPreferences(getApplicationContext());
				appPrefs.setUploadStateItem(3);

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
				if (strDescription.length() > 255) {
					strDescription = strDescription.substring(0, 255);
				}
				if(!bugzillaStorage.getTime().isEmpty()) {
					String occurrence = "Issue occured in last " + bugzillaStorage.getTime();
					if (strDescription.length() + occurrence.length() <= 255)
						strDescription += occurrence;
					else if (strDescription.length() + bugzillaStorage.getTime().length() <= 255)
						strDescription += bugzillaStorage.getTime();
				}
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
				appPrefs.setUploadStateItem(-1);
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
			startActivityAsUser(intent, UserHandle.CURRENT);
		}
		else {
			Intent startMain = new Intent(Intent.ACTION_MAIN);
			startMain.addCategory(Intent.CATEGORY_HOME);
			startMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivityAsUser(startMain, UserHandle.CURRENT);
		}
	}

	@Override
	public void onBackPressed() {
		comeBack();
	}


}
