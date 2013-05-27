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

		if(app.isUserBuild() && rdDef.isShown()) {
			rdDef.setVisibility(View.GONE);
			rdAll.setVisibility(View.GONE);
			rdLabel.setVisibility(View.GONE);
		}
		else {
			if(!rdDef.isShown()) {
				rdDef.setVisibility(View.VISIBLE);
				rdAll.setVisibility(View.VISIBLE);
				rdLabel.setVisibility(View.VISIBLE);
			}

			LogTimeProcessing process = new LogTimeProcessing(UploadAplogActivity.LOG_PATH);

			long lDefHour = process.getDefaultLogHour();
			long lAllHour = process.getLogHourByNumber(UploadAplogActivity.ALL_LOGS_VALUE);

			if (lDefHour > 1 ){
				rdDef.setText(bugzillaActivity.getResources().getText(R.string.upload_log_DEFAULT) + " ("+ lDefHour + " Hours of log)");
			}

			if (lAllHour > 1 ){
				rdAll.setText(bugzillaActivity.getResources().getText(R.string.upload_log_ALL) + " ("+ lAllHour + " Hours of log)");
			}
		}

	}

	public void hideSelectAplogDepth() {
		RadioButton rdDef = (RadioButton) bugzillaActivity.findViewById(R.id.bz_radioButtonDefault);
		RadioButton rdAll = (RadioButton) bugzillaActivity.findViewById(R.id.bz_radioButtonAll);
		TextView rdLabel = (TextView)bugzillaActivity.findViewById(R.id.bz_textViewSelect);

		if(rdDef.isShown()) {
			rdDef.setVisibility(View.GONE);
			rdAll.setVisibility(View.GONE);
			rdLabel.setVisibility(View.GONE);
		}
	}

	public void radioButtonIsAvailable() {
		Spinner bz_severity = (Spinner) bugzillaActivity.findViewById(R.id.bz_severity_list);

		bz_severity.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View v,
					int posSel, long arg3) {
				Spinner bz_severity = (Spinner) bugzillaActivity.findViewById(R.id.bz_severity_list);
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

		if(!app.isUserBuild()) {
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
		CrashReport app = (CrashReport)bugzillaActivity.getApplicationContext();
		if(!((String)bz_severity.getSelectedItem()).equals(BugzillaMainActivity.ENHANCEMENT_SEVERITY)) {
			if(!app.isUserBuild()) {

				RadioGroup radioGroup = (RadioGroup) bugzillaActivity.findViewById(R.id.bz_radiogroup_upload);


				int checkedRadioButton = radioGroup.getCheckedRadioButtonId();


				switch (checkedRadioButton) {
				case R.id.bz_radioButtonDefault : nbLog = -1;
				break;
				case R.id.bz_radioButtonAll :  nbLog = UploadAplogActivity.ALL_LOGS_VALUE;
				break;
				}
			}
		}
		else
			nbLog = 0;
		return nbLog;
	}


}
