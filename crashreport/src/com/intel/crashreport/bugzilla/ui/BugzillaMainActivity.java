package com.intel.crashreport.bugzilla.ui;

import java.util.ArrayList;

import com.intel.crashreport.CrashReport;
import com.intel.crashreport.CrashReportHome;
import com.intel.crashreport.R;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.Spinner;
import android.widget.Toast;

public class BugzillaMainActivity extends Activity {

	private ScreenshotAdapter galleryAdapter;
	private boolean fromGallery;
	private CrashReport app;
	private Context context = this;
	private static String TYPE_DEFAULT_VALUE = "medium";


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
	}

	public void onResume(){
		super.onResume();
		CheckBox pictureBox = (CheckBox)findViewById(R.id.bz_screenshot_box);
		Gallery screenshot = (Gallery)findViewById(R.id.bz_select_screenshot);
		EditText title = (EditText)findViewById(R.id.bz_title_text);
		EditText summary = (EditText)findViewById(R.id.bz_summary_text);
		Spinner bz_types = (Spinner) findViewById(R.id.bz_type_list);
		Spinner bz_component = (Spinner) findViewById(R.id.bz_component_list);
		Spinner bz_severity = (Spinner) findViewById(R.id.bz_severity_list);
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
			if(intent.getType().startsWith("image/")){
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

	public void onStart() {
		super.onStart();
		if (app.getUserEmail().equals("")) {
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

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

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

		BugStorage bugzillaStorage = app.getBugzillaStorage();
		bugzillaStorage.setSummary(title.getText().toString());
		bugzillaStorage.setDescription(summary.getText().toString());
		bugzillaStorage.setBugType((String)bz_types.getSelectedItem());
		bugzillaStorage.setComponent((String)bz_component.getSelectedItem());
		bugzillaStorage.setBugSeverity((String)bz_severity.getSelectedItem());
		bugzillaStorage.setBugHasScreenshot(pictureBox.isChecked());

		if(pictureBox.isChecked())
			bugzillaStorage.setBugScreenshotPath(galleryAdapter.getScreenshotsSelected());
	}


}
