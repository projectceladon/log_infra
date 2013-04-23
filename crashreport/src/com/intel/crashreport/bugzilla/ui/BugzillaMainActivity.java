package com.intel.crashreport.bugzilla.ui;

import java.util.ArrayList;

import com.intel.crashreport.CrashReport;
import com.intel.crashreport.CrashReportHome;
import com.intel.crashreport.LogTimeProcessing;
import com.intel.crashreport.R;
import com.intel.crashreport.UploadAplogActivity;

import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.text.method.TextKeyListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class BugzillaMainActivity extends Activity {

	private ScreenshotAdapter galleryAdapter;
	private boolean fromGallery;
	private CrashReport app;
	private Context context = this;
	private static String TYPE_DEFAULT_VALUE = "medium";


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (CrashReport) getApplicationContext();
		setContentView(R.layout.activity_bugzilla_main);
		galleryAdapter = new ScreenshotAdapter(getApplicationContext());
		Gallery screenshot = (Gallery)findViewById(R.id.bz_select_screenshot);
		screenshot.setAdapter(galleryAdapter);
		screenshot.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> adapter, View view, int position,
					long id) {
				galleryAdapter.updateItem(view);
				CheckBox pictureBox = (CheckBox)findViewById(R.id.bz_screenshot_box);
				pictureBox.setText(getResources().getText(R.string.bugzilla_screenshot) + " ("
												+galleryAdapter.getScreenshotsSelected().size() + ")");
			}

		});

		EditText summary = (EditText)findViewById(R.id.bz_summary_text);
		TextKeyListener tListener = TextKeyListener.getInstance(false, TextKeyListener.Capitalize.SENTENCES);
		summary.setKeyListener(tListener);

		CheckBox pictureBox = (CheckBox)findViewById(R.id.bz_screenshot_box);
		pictureBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				Gallery screenshot = (Gallery)findViewById(R.id.bz_select_screenshot);
				if (isChecked) {
					galleryAdapter.refreshScreenshotsSelected();
					if (screenshot.getAdapter().getCount() > 0) {
						screenshot.setVisibility(View.VISIBLE);
						galleryAdapter.notifyDataSetChanged();
						buttonView.setText(getResources().getText(R.string.bugzilla_screenshot) + " ("
                                                         +galleryAdapter.getScreenshotsSelected().size() + ")");
					}
					else {
						AlertDialog alert = new AlertDialog.Builder(context).create();
						alert.setMessage("No screenshot available.\nPlease make a screen capture in holding on Power button and Volume Down.");
						alert.setButton(DialogInterface.BUTTON_NEUTRAL,"OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
							}
						});
						alert.show();
						CheckBox box = (CheckBox)findViewById(R.id.bz_screenshot_box);
						box.setChecked(false);
					}
				}
				else {
					screenshot.setVisibility(View.GONE);
					buttonView.setText(getResources().getText(R.string.bugzilla_screenshot));
				}

			}

		});



		Button button_report = (Button) findViewById(R.id.bz_apply_button);
		button_report.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				EditText title = (EditText)findViewById(R.id.bz_title_text);
				EditText summary = (EditText)findViewById(R.id.bz_summary_text);
				String strTitle = title.getText().toString();
				strTitle = strTitle.trim();
				String strSummary = summary.getText().toString();
				strSummary = strSummary.trim();

				if (!strTitle.equals("") && !strSummary.equals("")) {
					Intent intent = new Intent(getApplicationContext(), BugzillaSummaryActivity.class);
					intent.putExtra("com.intel.crashreport.bugzilla.fromgallery", fromGallery);
					finish();
					startActivity(intent);
				}
				else {
					Toast.makeText(getApplicationContext(), "Some informations are not filled, please fill them before report your bug.", Toast.LENGTH_LONG).show();
				}
			}
		});

		Spinner bz_severity = (Spinner) findViewById(R.id.bz_severity_list);
		bz_severity.setAdapter(ArrayAdapter.createFromResource(getApplicationContext(), R.array.reportBugzillaSeverityValues, R.layout.spinner_bugzilla_item));

		Spinner bz_types = (Spinner) findViewById(R.id.bz_type_list);
		bz_types.setAdapter(ArrayAdapter.createFromResource(getApplicationContext(), R.array.reportBugzillaTypeValues, R.layout.spinner_bugzilla_item));

		Spinner bz_components = (Spinner) findViewById(R.id.bz_component_list);
		bz_components.setAdapter(ArrayAdapter.createFromResource(getApplicationContext(), R.array.reportBugzillaComponentText, R.layout.spinner_bugzilla_item));

		RadioButton rdDef = (RadioButton) findViewById(R.id.bz_radioButtonDefault);
		RadioButton rdAll = (RadioButton) findViewById(R.id.bz_radioButtonAll);

		if(app.isUserBuild()) {
			rdDef.setVisibility(View.GONE);
			rdAll.setVisibility(View.GONE);
			TextView rdLabel = (TextView)findViewById(R.id.bz_textViewSelect);
			rdLabel.setVisibility(View.GONE);
		}
		else {
			LogTimeProcessing process = new LogTimeProcessing(UploadAplogActivity.LOG_PATH);

			long lDefHour = process.getDefaultLogHour();
			long lAllHour = process.getLogHourByNumber(UploadAplogActivity.ALL_LOGS_VALUE);

			if (lDefHour > 1 ){
				rdDef.setText(rdDef.getText() + " ("+ lDefHour + " Hours of log)");
			}

			if (lAllHour > 1 ){
				rdAll.setText(rdAll.getText() + " ("+ lAllHour + " Hours of log)");
			}
		}

	}

	@Override
	public void onResume(){
		super.onResume();
		CheckBox pictureBox = (CheckBox)findViewById(R.id.bz_screenshot_box);
		Gallery screenshot = (Gallery)findViewById(R.id.bz_select_screenshot);
		EditText title = (EditText)findViewById(R.id.bz_title_text);
		EditText summary = (EditText)findViewById(R.id.bz_summary_text);
		Spinner bz_types = (Spinner) findViewById(R.id.bz_type_list);
		Spinner bz_component = (Spinner) findViewById(R.id.bz_component_list);
		Spinner bz_severity = (Spinner) findViewById(R.id.bz_severity_list);
		RadioButton radioButtonAll = (RadioButton) findViewById(R.id.bz_radioButtonAll);
		RadioButton radioButtonDef = (RadioButton) findViewById(R.id.bz_radioButtonDefault);
		galleryAdapter = (ScreenshotAdapter)screenshot.getAdapter();

		screenshot.setVisibility(View.GONE);
		Intent intent = getIntent();

		pictureBox.setChecked(false);
		BugStorage bugzillaStorage = app.getBugzillaStorage();
		if (bugzillaStorage.hasValuesSaved()) {
			title.setText(bugzillaStorage.getSummary());
			summary.setText(bugzillaStorage.getDescription());
			ArrayAdapter<String> adapter = (ArrayAdapter)bz_types.getAdapter();
			int pos = adapter.getPosition(bugzillaStorage.getBugType());
			if( pos >= 0)
				bz_types.setSelection(pos);

			adapter = (ArrayAdapter)bz_component.getAdapter();
			pos = adapter.getPosition(bugzillaStorage.getComponent());
			if( pos >= 0)
				bz_component.setSelection(pos);

			adapter = (ArrayAdapter)bz_severity.getAdapter();
			pos = adapter.getPosition(bugzillaStorage.getSeverity());
			if( pos >= 0)
				bz_severity.setSelection(pos);
			if(!app.isUserBuild()) {
				if (bugzillaStorage.getLogLevel() > 0){
					radioButtonAll.setChecked(true);
				}else{
					radioButtonDef.setChecked(true);
				}
			}


		}
		else {
			ArrayAdapter<String> adapter = (ArrayAdapter)bz_severity.getAdapter();
			int pos = adapter.getPosition(TYPE_DEFAULT_VALUE);
			if( pos >= 0)
				bz_severity.setSelection(pos);

			String[] componentText = getResources().getStringArray(R.array.reportBugzillaComponentText);
			String[] componentValues = getResources().getStringArray(R.array.reportBugzillaComponentValues);
			String component = bugzillaStorage.getComponent();
			pos = 0;
			if( componentText.length == componentValues.length) {
				for (int i=0; i<componentText.length;i++) {
					if(componentText[i].equals(component)) {
						pos = i;
						break;
					}
				}
			}

			if( pos >= 0)
				bz_component.setSelection(pos);

		}

		ArrayList<String> screenshots = new ArrayList<String>();
		if(bugzillaStorage.hasValuesSaved()){
			if (bugzillaStorage.getBugHasScreenshot()) {
				screenshots = bugzillaStorage.getScreenshotPath();
			}
		}

		if ((null != intent) && (null != intent.getAction()) && intent.getAction().equals(Intent.ACTION_VIEW)) {
			if(intent.resolveType(context).startsWith("image/")){
				Uri imageUri = intent.getData();
				String fileName="unknown";
				if (imageUri.getScheme().toString().compareTo("content")==0)
				{
					Cursor cursor = getApplicationContext().getContentResolver().query(imageUri, null, null, null, null);
					if (cursor.moveToFirst())
					{
						int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
						if (column_index >= 0) {
							imageUri = Uri.parse(cursor.getString(column_index));
							fileName = imageUri.getLastPathSegment().toString();
						}
						cursor.close();
					}

				}
				else if (imageUri.getScheme().equals("file"))
				{
					fileName = imageUri.getLastPathSegment().toString();
				}

				if (!fileName.equals("unknown")) {
					screenshots.add(fileName);
				}
				else {
					AlertDialog alert = new AlertDialog.Builder(context).create();
					alert.setMessage("This picture isn't a screenshot and it can't be associated with this BZ.");
					alert.setButton(DialogInterface.BUTTON_NEUTRAL,"OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
						}
					});
					alert.show();
				}

			}
		}
		galleryAdapter.refreshScreenshotsSelected(screenshots);

		if(screenshots.size() > 0) {
			int pos = galleryAdapter.getItemPosition(screenshots.get(screenshots.size() - 1));
			if (-1 != pos)
				screenshot.setSelection(pos);
			pictureBox.setChecked(true);
		}

	}

	@Override
	public void onStart() {
		super.onStart();
		if (app.getUserEmail().equals("") || app.getUserFirstName().equals("") || app.getUserLastName().equals("")) {
			finish();
			Intent intent = new Intent(getApplicationContext(),UserInformationsActivity.class);
			startActivity(intent);
		}
		else {
			Intent intent = getIntent();
			if (null != intent) fromGallery = intent.getBooleanExtra("com.intel.crashreport.bugzilla.fromgallery", true);
			else fromGallery = true;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
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

	@Override
	public void onPause() {
		saveData();
		super.onPause();
	}

	public void saveData() {
		EditText title = (EditText)findViewById(R.id.bz_title_text);
		EditText summary = (EditText)findViewById(R.id.bz_summary_text);
		CheckBox pictureBox = (CheckBox)findViewById(R.id.bz_screenshot_box);
		Spinner bz_types = (Spinner) findViewById(R.id.bz_type_list);
		Spinner bz_component = (Spinner) findViewById(R.id.bz_component_list);
		Spinner bz_severity = (Spinner) findViewById(R.id.bz_severity_list);
		Gallery screenshot = (Gallery)findViewById(R.id.bz_select_screenshot);
		int iNbLog =-1;
		if(!app.isUserBuild()) {
			RadioGroup radioGroup = (RadioGroup) findViewById(R.id.bz_radiogroup_upload);


			int checkedRadioButton = radioGroup.getCheckedRadioButtonId();


			switch (checkedRadioButton) {
			case R.id.bz_radioButtonDefault : iNbLog = -1;
			break;
			case R.id.bz_radioButtonAll :  iNbLog = UploadAplogActivity.ALL_LOGS_VALUE;
			break;
			}
		}


		BugStorage bugzillaStorage = app.getBugzillaStorage();
		bugzillaStorage.setSummary(title.getText().toString());
		bugzillaStorage.setDescription(summary.getText().toString());
		bugzillaStorage.setBugType((String)bz_types.getSelectedItem());
		bugzillaStorage.setComponent((String)bz_component.getSelectedItem());
		bugzillaStorage.setBugSeverity((String)bz_severity.getSelectedItem());
		bugzillaStorage.setBugHasScreenshot(pictureBox.isChecked());
		bugzillaStorage.setBugLogLevel(iNbLog);
		if(pictureBox.isChecked())
			bugzillaStorage.setBugScreenshotPath(galleryAdapter.getScreenshotsSelected());
	}


}
