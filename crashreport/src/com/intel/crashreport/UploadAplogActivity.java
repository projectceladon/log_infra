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
 * Author: Nicolas BENOIT <nicolasx.benoit@intel.com>
 */

package com.intel.crashreport;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class UploadAplogActivity extends Activity{
	final Context context = this;
	final Activity curActivity = this;

	public static int ALL_LOGS_VALUE = 21;
	public static String LOG_PATH = "/logs";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload_aplog_main);


		Button buttonUpload = (Button) findViewById(R.id.buttonUpload);
		buttonUpload.setOnClickListener(new AplogListener());

		RadioButton rdDef = (RadioButton) findViewById(R.id.radioButtonDefault);
		RadioButton rdAll = (RadioButton) findViewById(R.id.radioButtonAll);

		LogTimeProcessing process = new LogTimeProcessing(LOG_PATH);

		long lDefHour = process.getDefaultLogHour();
		long lAllHour = process.getLogHourByNumber(ALL_LOGS_VALUE);

		if (lDefHour > 1 ){
			rdDef.setText(rdDef.getText() + " ("+ lDefHour + " Hours of log)");
		}

		if (lAllHour > 1 ){
			rdAll.setText(rdAll.getText() + " ("+ lAllHour + " Hours of log)");
		}
	}

	private class AplogListener implements  View.OnClickListener {
		public void onClick(View v) {
			RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radiogroup_upload);


			int checkedRadioButton = radioGroup.getCheckedRadioButtonId();

			String aplogSelected = "";
			int iNbLog =-1;

			switch (checkedRadioButton) {
			case R.id.radioButtonDefault : aplogSelected = "(with default number of aplog)";
				iNbLog = -1;
				break;
			case R.id.radioButtonAll : aplogSelected = "(with all aplog)";
				iNbLog = ALL_LOGS_VALUE;
				break;
			}

			new UploadAplogTask(iNbLog).execute();
			AlertDialog alert = new AlertDialog.Builder(context).create();
			alert.setMessage("A background request of log upload has been created. \n " + aplogSelected);
			alert.setButton(DialogInterface.BUTTON_NEUTRAL,"OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					curActivity.finish();
				}
			});
			alert.show();
		}
	}


	private class UploadAplogTask extends AsyncTask<Void, Void, Void> {
		private int m_iLog = -1;

		public UploadAplogTask(int iLog){
			m_iLog = iLog;
		}

		@Override
		protected Void doInBackground(Void... params) {

			//Create a file to trigger crashlog daemon
			String sArgument = "";
			if (m_iLog > 0)
				sArgument="APLOG=" + m_iLog + "\n";
			CrashlogDaemonCmdFile.CreateCrashlogdCmdFile(CrashlogDaemonCmdFile.Command.APLOG, sArgument, context);

			return null;
		}

		@Override
		protected void onProgressUpdate(Void... params) {
		}

		protected void onPostExecute(Void... params) {

		}

	}
}