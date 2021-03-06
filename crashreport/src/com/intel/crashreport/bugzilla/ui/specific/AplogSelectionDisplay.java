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

package com.intel.crashreport.bugzilla.ui.specific;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.intel.crashreport.CrashReport;
import com.intel.crashreport.R;
import com.intel.crashreport.bugzilla.ui.common.BugStorage;
import com.intel.crashreport.bugzilla.ui.common.BugzillaMainActivity;
import com.intel.crashreport.specific.LogTimeProcessing;
import com.intel.crashreport.specific.UploadAplogActivity;
import com.intel.phonedoctor.Constants;

public class AplogSelectionDisplay {

	private BugzillaMainActivity bugzillaActivity;

	public AplogSelectionDisplay(BugzillaMainActivity activity) {
		bugzillaActivity = activity;
	};

	public void displaySelectAplogDepth() {
		RadioButton rdDef = (RadioButton) bugzillaActivity.findViewById(R.id.bz_radioButtonDefault);
		RadioButton rdAll = (RadioButton) bugzillaActivity.findViewById(R.id.bz_radioButtonAll);
		TextView rdLabel = (TextView)bugzillaActivity.findViewById(R.id.bz_textViewSelect);
		CrashReport app = (CrashReport)bugzillaActivity.getApplicationContext();
		boolean isViewValid = false;

		if(null != rdDef && null != rdAll && null != rdLabel) {
			isViewValid = true;
		}

		if(isViewValid && app.isUserBuild() && rdDef.isShown()) {
			rdDef.setVisibility(View.GONE);
			rdAll.setVisibility(View.GONE);
			rdLabel.setVisibility(View.GONE);
		}
		else {
			if(isViewValid && !rdDef.isShown()) {
				rdDef.setVisibility(View.VISIBLE);
				rdAll.setVisibility(View.VISIBLE);
				rdLabel.setVisibility(View.VISIBLE);
			}

			LogTimeProcessing process = new LogTimeProcessing(Constants.LOGS_DIR);

			long lDefHour = process.getDefaultLogHour();
			long lAllHour = process.getLogHourByNumber(UploadAplogActivity.ALL_LOGS_VALUE);

			if (null != rdDef && lDefHour > 1 ) {
				rdDef.setText(bugzillaActivity.getResources().getText(R.string.upload_log_DEFAULT) + " ("+ lDefHour + " Hours of log)");
			}

			if (null != rdAll && lAllHour > 1 ) {
				rdAll.setText(bugzillaActivity.getResources().getText(R.string.upload_log_ALL) + " ("+ lAllHour + " Hours of log)");
			}
		}

	}

	public void hideSelectAplogDepth() {
		RadioButton rdDef = (RadioButton) bugzillaActivity.findViewById(R.id.bz_radioButtonDefault);
		RadioButton rdAll = (RadioButton) bugzillaActivity.findViewById(R.id.bz_radioButtonAll);
		TextView rdLabel = (TextView)bugzillaActivity.findViewById(R.id.bz_textViewSelect);
		boolean isViewValid = false;
		if(null != rdDef && null != rdAll && null != rdLabel) {
			isViewValid = true;
		}

		if(isViewValid && rdDef.isShown()) {
			rdDef.setVisibility(View.GONE);
			rdAll.setVisibility(View.GONE);
			rdLabel.setVisibility(View.GONE);
		}
	}

	public void radioButtonIsAvailable() {
		Spinner bz_severity = (Spinner) bugzillaActivity.findViewById(R.id.bz_severity_list);
		if(null == bz_severity) {
			return;
		}

		bz_severity.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View v,
					int posSel, long arg3) {
				Spinner bz_severity = (Spinner) bugzillaActivity.findViewById(R.id.bz_severity_list);
				if(null == bz_severity) {
					return;
				}
				ArrayAdapter<String> adapter = (ArrayAdapter)bz_severity.getAdapter();
				int pos = adapter.getPosition(BugzillaMainActivity.ENHANCEMENT_SEVERITY);
				if( pos >= 0 && posSel >= 0) {
					if(posSel == pos) {

						hideSelectAplogDepth();
						bugzillaActivity.hideBzTimeSelection();
					} else {
						displaySelectAplogDepth();
						bugzillaActivity.displayBzTimeSelection();
					}
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}

		});

	}

	public void checkRadioButton() {
		RadioButton radioButtonAll = (RadioButton) bugzillaActivity.findViewById(R.id.bz_radioButtonAll);
		RadioButton radioButtonDef = (RadioButton) bugzillaActivity.findViewById(R.id.bz_radioButtonDefault);
		CrashReport app = (CrashReport)bugzillaActivity.getApplicationContext();
		BugStorage bugzillaStorage = app.getBugzillaStorage();

		if(null != radioButtonAll && null != radioButtonDef && !app.isUserBuild()) {
			if (bugzillaStorage.getLogLevel() > 0){
				radioButtonAll.setChecked(true);
			}else{
				radioButtonDef.setChecked(true);
			}
		}
	}

	public int computeNbLog() {
		int nbLog = -1;
		Spinner bz_severity = (Spinner) bugzillaActivity.findViewById(R.id.bz_severity_list);
		if(bz_severity == null) {
			return 0;
		}
		CrashReport app = (CrashReport)bugzillaActivity.getApplicationContext();
		String bzSeverityStr = (String)bz_severity.getSelectedItem();
		if(bzSeverityStr != null && !bzSeverityStr.equals(BugzillaMainActivity.ENHANCEMENT_SEVERITY)) {
			if(!app.isUserBuild()) {

				RadioGroup radioGroup = (RadioGroup) bugzillaActivity.findViewById(R.id.bz_radiogroup_upload);

				if(radioGroup != null) {

					int checkedRadioButton = radioGroup.getCheckedRadioButtonId();
					switch (checkedRadioButton) {
						case R.id.bz_radioButtonDefault : nbLog = -1;
						break;
						case R.id.bz_radioButtonAll :  nbLog = UploadAplogActivity.ALL_LOGS_VALUE;
						break;
					}
				}
			}
		}
		else
			nbLog = 0;
		return nbLog;
	}


}
