package com.intel.crashreport.bugzilla.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.intel.crashreport.CrashReport;
import com.intel.crashreport.CrashReportHome;
import com.intel.crashreport.R;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

		sendButton.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				Button editButton = (Button) findViewById(R.id.bugzilla_edit_button);
				editButton.setEnabled(false);
				v.setEnabled(false);
				new CreateBzTask().execute();
				AlertDialog alert = new AlertDialog.Builder(context).create();
				alert.setMessage("A background request of new bugzilla creation has been submitted.");
				alert.setButton(DialogInterface.BUTTON_NEUTRAL,"OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						finish();
						if (!fromGallery) {
							Intent intent = new Intent(context,CrashReportHome.class);
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
				});
				alert.show();
			}

		});

		editButton.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				finish();
				Intent intent = new Intent(getApplicationContext(),BugzillaMainActivity.class);
				intent.putExtras(getIntent().getExtras());
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}

		});
	}

	@Override
	public void onResume() {
		TextView infos = (TextView) findViewById(R.id.bugzilla_summary_text);
		CrashReport app = (CrashReport)getApplicationContext();
		BugStorage bugzillaStorage = app.getBugzillaStorage();
		String text = "";
		text += "Summary : ["+bugzillaStorage.getBugType()+"]";
		text += bugzillaStorage.getSummary() + "\n \n";
		text += "Component : "+bugzillaStorage.getComponent()+ "\n";
		text += "Severity : "+bugzillaStorage.getSeverity()+ "\n";
		text += "Description : "+bugzillaStorage.getDescription()+ "\n \n";
		text += "With"+ (bugzillaStorage.getBugHasScreenshot()?" ":"out ")+"screenshot(s)\n";
		text += "With Aplogs attached";
		infos.setText(text);
		super.onResume();
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
			File bzTrigger = new File("/logs/aplogs/bz_trigger");
			if (!bzTrigger.exists()) {
				//to manage case  of crashlogd not launched
				bzTrigger.delete();
			}
			try {
				FileOutputStream write = new FileOutputStream(bzTrigger);
				CrashReport app = (CrashReport)getApplicationContext();
				BugStorage bugzillaStorage = app.getBugzillaStorage();
				String line = "";
				if (bugzillaStorage.getLogLevel()>0){
					line = "APLOG="+bugzillaStorage.getLogLevel()+"\n";
					write.write(line.getBytes());
				}
				line = "SUMMARY="+bugzillaStorage.getSummary()+"\n";
				write.write(line.getBytes());
				line = "TYPE="+bugzillaStorage.getBugType()+"\n";
				write.write(line.getBytes());
				Boolean bzMode = PreferenceManager.getDefaultSharedPreferences(app).getBoolean("uploadBZPref", false);
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
				write.write(line.getBytes());
				line = "SEVERITY="+bugzillaStorage.getSeverity()+"\n";
				write.write(line.getBytes());
				String strDescription = bugzillaStorage.getDescription();
				strDescription = strDescription.replace("\n", "\\n");
				line = "DESCRIPTION="+strDescription+"\n";
				write.write(line.getBytes());
				if(bugzillaStorage.getBugHasScreenshot()) {
					ArrayList<String> screenshots = bugzillaStorage.getScreenshotPath();
					for(String screenshot:screenshots) {
						line = "SCREENSHOT=/mnt/sdcard/Pictures/Screenshots/"+screenshot+"\n";
						write.write(line.getBytes());
					}

				}
				line = "USERFIRSTNAME="+app.getUserFirstName()+"\n";
				write.write(line.getBytes());
				line = "USERLASTNAME="+app.getUserLastName()+"\n";
				write.write(line.getBytes());
				line = "USEREMAIL="+app.getUserEmail()+"\n";
				write.write(line.getBytes());
				if (bzMode) {
					line = "TEST=true\n";
					write.write(line.getBytes());
				}

				write.close();
				bugzillaStorage.clearValues();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}


			return null;
		}

		@Override
		protected void onProgressUpdate(Void... params) {
		}

		protected void onPostExecute(Void... params) {

		}

	}

	@Override
	public void onBackPressed() {
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


}
