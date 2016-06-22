/* INTEL CONFIDENTIAL
 * Copyright 2015 Intel Corporation
 *
 * The source code contained or described herein and all documents
 * related to the source code ("Material") are owned by Intel
 * Corporation or its suppliers or licensors. Title to the Material
 * remains with Intel Corporation or its suppliers and
 * licensors. The Material contains trade secrets and proprietary
 * and confidential information of Intel or its suppliers and
 * licensors. The Material is protected by worldwide copyright and
 * trade secret laws and treaty provisions. No part of the Material
 * may be used, copied, reproduced, modified, published, uploaded,
 * posted, transmitted, distributed, or disclosed in any way without
 * Intel's prior express written permission.
 *
 * No license under any patent, copyright, trade secret or other
 * intellectual property right is granted to or conferred upon you
 * by disclosure or delivery of the Materials, either expressly, by
 * implication, inducement, estoppel or otherwise. Any license under
 * such intellectual property rights must be express and approved by
 * Intel in writing.
 */

package com.intel.crashreport.bugzilla.ui.common;

import com.intel.crashreport.CrashReport;
import com.intel.crashreport.R;

import android.os.Bundle;
import android.os.UserHandle;
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
		if(saveButton != null) {
			saveButton.setOnClickListener(new View.OnClickListener(){

				public void onClick(View v) {
					checkData();
				}

			});
		}

                setTitle(getString(R.string.activity_name));
		CrashReport app = (CrashReport) getApplicationContext();

		EditText lastName = (EditText)findViewById(R.id.lastNameText);
		if(lastName != null) {
			lastName.setText(app.getUserLastName());
		}
		EditText firstName = (EditText)findViewById(R.id.firstNameText);
		if(firstName != null) {
			firstName.setText(app.getUserFirstName());
		}

		EditText email = (EditText)findViewById(R.id.mailText);
		if(email != null) {
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
			if(!app.getUserEmail().isEmpty())
				email.setText(app.getUserEmail());
		}
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
		String strLastName = "";
		String strFirstName = "";
		String strEmail = "";

		if(lastName != null) {
			strLastName = lastName.getText().toString();
			strLastName = strLastName.trim();
		}
		if(firstName != null) {
			strFirstName = firstName.getText().toString();
			strFirstName = strFirstName.trim();
		}
		if(email != null) {
			strEmail = email.getText().toString();
			strEmail = strEmail.trim();
		}

		if(!strLastName.isEmpty() && !strFirstName.isEmpty() && !strEmail.isEmpty()) {
			if(strEmail.endsWith("@intel.com") && (strEmail.indexOf("@") == strEmail.lastIndexOf("@")) && (strEmail.indexOf("@")!=0)) {
				CrashReport app = (CrashReport) getApplicationContext();
				app.setUserEmail(strEmail);
				app.setUserFirstName(strFirstName);
				app.setUserLastName(strLastName);
				Intent intent = new Intent(getApplicationContext(),BugzillaMainActivity.class);
				intent.putExtra("com.intel.crashreport.bugzilla.fromgallery", fromGallery);
				startActivityAsUser(intent, UserHandle.CURRENT);
			}
			else Toast.makeText(getApplicationContext(), "Wrong email address.", Toast.LENGTH_LONG).show();
		}
		else Toast.makeText(getApplicationContext(), "Date are missing, please fill empty field(s)", Toast.LENGTH_LONG).show();
	}



}
