package com.intel.crashreport.bugzilla.ui.common;

import java.util.ArrayList;

import com.intel.crashreport.ApplicationPreferences;
import com.intel.crashreport.CrashReport;
import com.intel.crashreport.R;
import com.intel.crashreport.bugzilla.ui.specific.AplogSelectionDisplay;
import com.intel.crashreport.specific.CrashReportHome;

import android.net.Uri;
import android.os.Bundle;
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
	public static String ENHANCEMENT_SEVERITY = "enhancement";
	//the following values should be aligned with bugTrackerMenuValues from "res"
	public static String IRDA_VALUE = "IRDA";
	public static String STARPEAK_VALUE = "STARPEAK";
	public static String MCG_VALUE = "MCG";
	public static String ICONIC_VALUE = "ICONIC";

	private AplogSelectionDisplay aplogSelection;
	private boolean bShowSeverity = true;
	private boolean bShowComponent = true;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (CrashReport) getApplicationContext();
		updateUIForTracker();
		aplogSelection = new AplogSelectionDisplay(this);
		setContentView(R.layout.activity_bugzilla_main);
		galleryAdapter = new ScreenshotAdapter(getApplicationContext());
		Gallery screenshot = (Gallery)findViewById(R.id.bz_select_screenshot);
		if(screenshot != null) {
			screenshot.setAdapter(galleryAdapter);
			screenshot.setOnItemClickListener(new OnItemClickListener(){

				public void onItemClick(AdapterView<?> adapter, View view, int position,
						long id) {
					galleryAdapter.updateItem(view);
					CheckBox pictureBox = (CheckBox)findViewById(R.id.bz_screenshot_box);
					if(pictureBox != null) {
						pictureBox.setText(
								getResources().getText(R.string.bugzilla_screenshot) + " (" +
										galleryAdapter.getScreenshotsSelected().size() + ")");
					}
				}

			});
		}

		EditText summary = (EditText)findViewById(R.id.bz_summary_text);
		TextKeyListener tListener = TextKeyListener.getInstance(false, TextKeyListener.Capitalize.SENTENCES);
		if(summary != null) {
			summary.setKeyListener(tListener);
		}

		CheckBox pictureBox = (CheckBox)findViewById(R.id.bz_screenshot_box);
		if(pictureBox != null) {
			pictureBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){

				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					Gallery screenshot = (Gallery)findViewById(R.id.bz_select_screenshot);
					if (isChecked) {
						galleryAdapter.refreshScreenshotsSelected();
						if (screenshot != null && screenshot.getAdapter().getCount() > 0) {
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
							if(box != null) {
								box.setChecked(false);
							}
						}
					}
					else {
						if(screenshot != null) {
							screenshot.setVisibility(View.GONE);
						}
						buttonView.setText(getResources().getText(R.string.bugzilla_screenshot));
					}

				}

			});
		}


		Button button_report = (Button) findViewById(R.id.bz_apply_button);
		if(button_report != null) {
			button_report.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					EditText title = (EditText)findViewById(R.id.bz_title_text);
					EditText summary = (EditText)findViewById(R.id.bz_summary_text);
					String strTitle = "";
					if(title != null) {
						strTitle = title.getText().toString();
						strTitle = strTitle.trim();
					}
					String strSummary = "";
					if(summary != null) {
						strSummary = summary.getText().toString();
						strSummary = strSummary.trim();
					}

					if (!strTitle.equals("") && !strSummary.equals("")) {
						Intent intent = new Intent(getApplicationContext(), BugzillaSummaryActivity.class);
						intent.putExtra("com.intel.crashreport.bugzilla.fromgallery", fromGallery);
						finish();
						startActivity(intent);
					} else {
						Toast.makeText(getApplicationContext(), "Some informations are not filled, please fill them before report your bug.", Toast.LENGTH_LONG).show();
					}
				}
			});
		}

		Spinner bz_severity = (Spinner) findViewById(R.id.bz_severity_list);
		if(bz_severity != null) {
			bz_severity.setAdapter(
					ArrayAdapter.createFromResource(
							getApplicationContext(),
							R.array.reportBugzillaSeverityValues,
							R.layout.spinner_bugzilla_item));
		}
		if (!bShowSeverity){
			bz_severity.setVisibility(View.GONE);
			//should hide text also
			View aView = findViewById(R.id.bz_severity_view);
			if (aView != null) {
				aView.setVisibility(View.GONE);
			}
		}
		aplogSelection.radioButtonIsAvailable();

		Spinner bz_types = (Spinner) findViewById(R.id.bz_type_list);
		if(bz_types != null) {
			bz_types.setAdapter(
					ArrayAdapter.createFromResource(
							getApplicationContext(),
							R.array.reportBugzillaTypeValues,
							R.layout.spinner_bugzilla_item));
		}

		Spinner bz_components = (Spinner) findViewById(R.id.bz_component_list);
		if(bz_components != null) {
			bz_components.setAdapter(
					ArrayAdapter.createFromResource(
							getApplicationContext(),
							R.array.reportBugzillaComponentText,
							R.layout.spinner_bugzilla_item));
		}

		if (!bShowComponent){
			bz_components.setVisibility(View.GONE);
			//should hide text also
			View aView = findViewById(R.id.bz_component_view);
			if (aView != null) {
				aView.setVisibility(View.GONE);
			}
		}

		Spinner bz_time = (Spinner) findViewById(R.id.bz_time_list);
		if(bz_time != null) {
			bz_time.setAdapter(
					ArrayAdapter.createFromResource(
							getApplicationContext(),
							R.array.reportBugzillaTimeValues,
							R.layout.spinner_bugzilla_item));
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
		Spinner bz_time = (Spinner) findViewById(R.id.bz_time_list);
		boolean isViewValid = false;
		if(screenshot != null && bz_types != null && title != null &&
				summary != null && bz_component != null &&
				bz_component != null && bz_severity != null &&
				bz_time != null && pictureBox != null) {
			isViewValid = true;
		}

		if(!isViewValid) {
			return;
		}
		galleryAdapter = (ScreenshotAdapter)screenshot.getAdapter();
		screenshot.setVisibility(View.GONE);
		Intent intent = getIntent();

		pictureBox.setChecked(false);
		BugStorage bugzillaStorage = app.getBugzillaStorage();
		if (bugzillaStorage.hasValuesSaved()) {
			if(title != null) {
				title.setText(bugzillaStorage.getSummary());
			}
			if(summary != null) {
				summary.setText(bugzillaStorage.getDescription());
			}
			ArrayAdapter<String> adapter = (ArrayAdapter)bz_types.getAdapter();
			int pos = 0;
			if(adapter != null) {
				pos = adapter.getPosition(bugzillaStorage.getBugType());
				if( pos >= 0)
					bz_types.setSelection(pos);
			}

			adapter = (ArrayAdapter)bz_component.getAdapter();
			if(adapter != null) {
				pos = adapter.getPosition(bugzillaStorage.getComponent());
				if( pos >= 0)
					bz_component.setSelection(pos);
			}

			adapter = (ArrayAdapter)bz_time.getAdapter();
			if(adapter != null) {
				pos = adapter.getPosition(bugzillaStorage.getTime());
				if( pos >= 0)
					bz_time.setSelection(pos);
			}

			adapter = (ArrayAdapter)bz_severity.getAdapter();
			if(adapter != null) {
				pos = adapter.getPosition(bugzillaStorage.getSeverity());
				if( pos >= 0)
					bz_severity.setSelection(pos);
			}
			aplogSelection.checkRadioButton();


		} else {
			// An index used to compute positions
			int pos = 0;

			if(bz_severity != null) {
				ArrayAdapter<String> adapter = (ArrayAdapter)bz_severity.getAdapter();
				pos = adapter.getPosition(TYPE_DEFAULT_VALUE);
				if( pos >= 0)
					bz_severity.setSelection(pos);
			}

			String[] componentText = getResources().getStringArray(R.array.reportBugzillaComponentText);
			String[] componentValues = getResources().getStringArray(R.array.reportBugzillaComponentValues);
			String component = bugzillaStorage.getComponent();
			pos = 0;
			if(bz_component != null) {
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

		}

		ArrayList<String> screenshots = new ArrayList<String>();
		if(bugzillaStorage.hasValuesSaved()){
			if (bugzillaStorage.getBugHasScreenshot()) {
				screenshots = bugzillaStorage.getScreenshotPath();
			}
		}

		if ((intent != null) && (intent.getAction() != null) && intent.getAction().equals(Intent.ACTION_VIEW)) {
			String intentType = intent.resolveType(context);
			if(intentType != null && intentType.startsWith("image/")){
				Uri imageUri = intent.getData();
				String fileName="unknown";
				if (imageUri.getScheme().toString().compareTo("content")==0)
				{
					Cursor cursor = getApplicationContext().getContentResolver().query(imageUri, null, null, null, null);
					if(cursor != null) {
						if (cursor.moveToFirst()) {
							int column_index = cursor.getColumnIndex(MediaColumns.DATA);
							if (column_index >= 0) {
								imageUri = Uri.parse(cursor.getString(column_index));
								fileName = imageUri.getLastPathSegment().toString();
							}
						}
						if(!cursor.isClosed()) {
							cursor.close();
						}
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
		if(galleryAdapter != null) {
			galleryAdapter.refreshScreenshotsSelected(screenshots);

			if(screenshots.size() > 0) {
				int pos = galleryAdapter.getItemPosition(screenshots.get(screenshots.size() - 1));
				if (-1 != pos) {
					if(screenshot != null) {
						screenshot.setSelection(pos);
					}
				}
				if(pictureBox != null) {
					pictureBox.setChecked(true);
				}
			}
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
		String strSummary = "";
		String strTitle = "";
		if(summary != null) {
			strSummary = summary.getText().toString();
			strSummary = strSummary.trim();
		}
		if(title != null) {
			strTitle = title.getText().toString();
		}
		CheckBox pictureBox = (CheckBox)findViewById(R.id.bz_screenshot_box);
		Spinner bz_types = (Spinner) findViewById(R.id.bz_type_list);
		Spinner bz_component = (Spinner) findViewById(R.id.bz_component_list);
		Spinner bz_severity = (Spinner) findViewById(R.id.bz_severity_list);
		Spinner bz_time = (Spinner) findViewById(R.id.bz_time_list);
		Gallery screenshot = (Gallery)findViewById(R.id.bz_select_screenshot);
		BugStorage bugzillaStorage = app.getBugzillaStorage();
		boolean isViewValid = false;

		if(title != null && summary != null && pictureBox != null && bz_types != null &&
				bz_component != null && bz_severity != null && bz_time != null) {
			isViewValid = true;
		}

		if(isViewValid && bugzillaStorage != null) {

			int iNbLog = aplogSelection.computeNbLog();

			bugzillaStorage.setSummary(title.getText().toString());
			bugzillaStorage.setDescription(summary.getText().toString());
			bugzillaStorage.setBugType((String)bz_types.getSelectedItem());
			bugzillaStorage.setComponent((String)bz_component.getSelectedItem());
			bugzillaStorage.setBugSeverity((String)bz_severity.getSelectedItem());
			String selectedSeverity = (String)bz_severity.getSelectedItem();
			if(selectedSeverity != null && !selectedSeverity.equals(ENHANCEMENT_SEVERITY))
				bugzillaStorage.setBugTime((String)bz_time.getSelectedItem());
			else bugzillaStorage.setBugTime("");
			bugzillaStorage.setBugHasScreenshot(pictureBox.isChecked());
			bugzillaStorage.setBugLogLevel(iNbLog);
			if(pictureBox.isChecked())
				bugzillaStorage.setBugScreenshotPath(galleryAdapter.getScreenshotsSelected());
			bugzillaStorage.setTracker(getBugTrackerValue());
		}
	}

	/**
	 * @brief display the dropdown list that allows the user
	 * to select a time information about the issue occurance
	 */
	public void displayBzTimeSelection() {
		Spinner bz_time = (Spinner) findViewById(R.id.bz_time_list);
		TextView bz_time_label = (TextView)findViewById(R.id.bz_time_view);

		if(bz_time != null && bz_time_label != null && !bz_time_label.isShown()) {
			bz_time.setVisibility(View.VISIBLE);
			bz_time_label.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * @brief hide the dropdown list that allows the user
	 * to select a time information about the issue occurance
	 */
	public void hideBzTimeSelection() {
		Spinner bz_time = (Spinner) findViewById(R.id.bz_time_list);
		TextView bz_time_label = (TextView)findViewById(R.id.bz_time_view);

		if(bz_time != null && bz_time_label != null && bz_time_label.isShown()) {
			bz_time.setVisibility(View.GONE);
			bz_time_label.setVisibility(View.GONE);
		}
	}

	public String  getBugTrackerValue() {
		//Improvement : manage it by a bug tracker object
		return new ApplicationPreferences(app).getBZTracker();
	}

	private void updateUIForTracker() {
		String sTracker = getBugTrackerValue();
		if (sTracker.equals(STARPEAK_VALUE)){
			bShowSeverity = false;
			bShowComponent = false;
		}
	}


}
