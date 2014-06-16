/* Phone Doctor (CLOTA)
 *
 * Copyright (C) Intel 2014
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
 * Author: Charles-Edouard Vidoine <charles.edouardx.vidoine@intel.com>
 */

package com.intel.crashreport;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.intel.crashreport.bugzilla.ui.common.BugzillaMainActivity;
import com.intel.crashreport.bugzilla.ui.common.ListBugzillaActivity;
import com.intel.crashreport.bugzilla.ui.common.UserInformationsActivity;
import com.intel.crashreport.specific.CrashReportActivity;
import com.intel.crashreport.specific.Event;


public class GeneralCrashReportHome extends Activity {
	private MenuItem aboutMenu;
	private MenuItem settingsMenu;
	protected final Context context = this;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);

		Button button_start = (Button) findViewById(R.id.button_report_events);
		// Attach a click listener for launching the system settings.
		if(button_start != null) {
			button_start.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent("com.intel.crashreport.intent.START_SERVICE");
					intent.putExtra("com.intel.crashreport.extra.fromOutside", true);
					startActivity(intent);
				}
			});
		}

		Button button_bugzilla = (Button) findViewById(R.id.button_report_bugzilla);
		if(null != button_bugzilla) {
			button_bugzilla.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					CrashReport app = (CrashReport)getApplicationContext();
					if(!app.getUserEmail().equals("") && !app.getUserFirstName().equals("") && !app.getUserLastName().equals("")) {
						Intent intent = new Intent(getApplicationContext(), BugzillaMainActivity.class);
						intent.putExtra("com.intel.crashreport.bugzilla.fromgallery", false);
						startActivity(intent);
					}
					else {
						Intent intent = new Intent(getApplicationContext(), UserInformationsActivity.class);
						intent.putExtra("com.intel.crashreport.bugzilla.fromgallery", false);
						startActivity(intent);
					}

				}
			});
		}

		Button button_list_bugzilla = (Button) findViewById(R.id.button_list_bugzilla);
		if(null != button_list_bugzilla) {
			button_list_bugzilla.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getApplicationContext(), ListBugzillaActivity.class);
					startActivity(intent);
				}
			});
		}

		setTitle(getString(R.string.app_name)+" "+getString(R.string.app_version));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		settingsMenu = menu.add(R.string.menu_settings);
		aboutMenu = menu.add(R.string.menu_about);
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.equals(settingsMenu)) {
			startCrashReport();
			return true;
		}
		if (item.equals(aboutMenu)) {
			showDialog();
			return true;
		}
		switch (item.getItemId()) {
		case R.id.settings:
			startCrashReport();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void startCrashReport() {
		Intent intent = new Intent(getApplicationContext(), CrashReportActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	public static class AboutDialog extends DialogFragment {

		public static AboutDialog newInstance() {
			AboutDialog frag = new AboutDialog();
			Bundle args = new Bundle();
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {

			return new AlertDialog.Builder(getActivity())
			.setTitle(getString(R.string.about_title))
			.setMessage(
					getString(R.string.app_name) + " v" + getString(R.string.app_version)
					+ "\n" + "© Intel 2014."
					+ "\n" + "SSN : "  + Event.getSSN()
					+ "\n" + "DeviceID : " + Event.deviceId()
					+ "\n" + "Product : " + GeneralBuild.getProperty(GeneralBuild.PRODUCT_PROPERTY_NAME))
					.create();
		}
	}

	public void showDialog() {
		DialogFragment newFragment = AboutDialog.newInstance();
		newFragment.show(getFragmentManager(), "dialog");
	}
}
