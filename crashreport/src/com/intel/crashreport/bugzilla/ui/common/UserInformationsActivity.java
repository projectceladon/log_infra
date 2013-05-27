package com.intel.crashreport.bugzilla.ui.common;

import com.intel.crashreport.CrashReport;
import com.intel.crashreport.R;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;

public class UserInformationsActivity extends Activity {

	private boolean fromGallery;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_informations_activity);

		Button saveButton = (Button) findViewById(R.id.infosSaveButton);
		saveButton.setOnClickListener(new View.OnClickListener(){

			public void onClick(View v) {
				checkData();
			}

		});
		CrashReport app = (CrashReport) getApplicationContext();

		EditText lastName = (EditText)findViewById(R.id.lastNameText);
		lastName.setText(app.getUserLastName());
		EditText firstName = (EditText)findViewById(R.id.firstNameText);
		firstName.setText(app.getUserFirstName());

		EditText email = (EditText)findViewById(R.id.mailText);
		email.setOnEditorActionListener(new OnEditorActionListener(){

			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					checkData();
					return true;
				}
				return false;
			}

		});
		if(!app.getUserEmail().equals(""))
			email.setText(app.getUserEmail());
	}

	public void onStart(){
		Intent intent = getIntent();
		if(null != intent)
			fromGallery = intent.getBooleanExtra("com.intel.crashreport.bugzilla.fromgallery", true);
		else fromGallery = true;
		super.onStart();
	}
	public void checkData() {
		EditText lastName = (EditText)findViewById(R.id.lastNameText);
		EditText firstName = (EditText)findViewById(R.id.firstNameText);
		EditText email = (EditText)findViewById(R.id.mailText);
		String strLastName = lastName.getText().toString();
		String strFirstName = firstName.getText().toString();
		String strEmail = email.getText().toString();
		strLastName = strLastName.trim();
		strFirstName = strFirstName.trim();
		strEmail = strEmail.trim();

		if(!strLastName.equals("") && !strFirstName.equals("") && !strEmail.equals("")) {
			if(strEmail.endsWith("@intel.com") && (strEmail.indexOf("@") == strEmail.lastIndexOf("@")) && (strEmail.indexOf("@")!=0)) {
				CrashReport app = (CrashReport) getApplicationContext();
				app.setUserEmail(strEmail);
				app.setUserFirstName(strFirstName);
				app.setUserLastName(strLastName);
				Intent intent = new Intent(getApplicationContext(),BugzillaMainActivity.class);
				intent.putExtra("com.intel.crashreport.bugzilla.fromgallery", fromGallery);
				startActivity(intent);
			}
			else Toast.makeText(getApplicationContext(), "Wrong email address.", Toast.LENGTH_LONG).show();
		}
		else Toast.makeText(getApplicationContext(), "Date are missing, please fill empty field(s)", Toast.LENGTH_LONG).show();
	}



}
