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

package com.intel.crashreport;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import android.widget.ToggleButton;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

import com.intel.crashreport.bugzilla.ui.common.BugzillaMainActivity;
import com.intel.crashreport.bugzilla.ui.common.ListBugzillaActivity;
import com.intel.crashreport.bugzilla.ui.common.UserInformationsActivity;
import com.intel.crashreport.specific.Build;
import com.intel.crashreport.specific.CrashReportActivity;
import com.intel.crashreport.specific.Event;


public class GeneralCrashReportHome extends Activity {
	private MenuItem aboutMenu;
	private MenuItem settingsMenu;
	protected final Context context = this;
	public  ArrayAdapterHomeScreenElement mainMenuAdapter;
	private int activeMenu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.crashreport_main);

		List<HomeScreenElement> menuItems = new ArrayList<HomeScreenElement>();
		menuItems.clear();

		mainMenuAdapter = new ArrayAdapterHomeScreenElement(this, R.layout.crashreport_element, menuItems);
		mainMenuAdapter.add(new HomeScreenElement(R.id.button_report_events,
			getString(R.string.settings_button_report), R.drawable.check_events, 4,
			getResources().getBoolean(R.bool.enable_device_events)));
		mainMenuAdapter.add(new HomeScreenElement(R.id.button_report_bugzilla,
			getString(R.string.menu_bugzilla), R.drawable.report_bug, 1,
			getResources().getBoolean(R.bool.enable_bugzilla)));
		mainMenuAdapter.add(new HomeScreenElement(R.id.button_list_bugzilla,
			getString(R.string.list_bugzilla),R.drawable.bug_history, 2,
			getResources().getBoolean(R.bool.enable_bugzilla)));

		ListView listViewItems = (ListView) findViewById(R.id.CrashReport_listView1);
		listViewItems.setAdapter(mainMenuAdapter);
		listViewItems.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TextView textViewItem = ((TextView) view.findViewById(R.id.textViewEntry));

				int cv = (Integer) textViewItem.getTag();
				onClickhandleMenuAction(cv);
			}
		});

		GridView gridViewItems = (GridView) findViewById(R.id.CrashReport_gridView1);
		gridViewItems.setAdapter(mainMenuAdapter);
		gridViewItems.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TextView textViewItem = ((TextView) view.findViewById(R.id.textViewEntry));

				int cv = (Integer) textViewItem.getTag();
				onClickhandleMenuAction(cv);
			}
		});

		activeMenu = 0;
	}

	void switchView(MenuItem item) {
		ViewFlipper vf = (ViewFlipper)findViewById(R.id.CrashReport_viewFlipper1);

		activeMenu = (activeMenu + 1)%2;
		vf.setDisplayedChild(activeMenu);
	}

	protected void onClickhandleMenuAction(int action) {
		        handleMenuAction(action);
	}

	protected void handleMenuAction(int action) {
		Intent intent;
		switch (action) {
			case (R.id.button_report_events):
				intent = new Intent("com.intel.crashreport.intent.START_SERVICE");
				intent.putExtra("com.intel.crashreport.extra.fromOutside", true);
				startActivityAsUser(intent, UserHandle.CURRENT);
			break;
			case (R.id.button_report_bugzilla):
					ApplicationPreferences appPrefs = new ApplicationPreferences(getApplicationContext());
					int state = appPrefs.getUploadStateItem();
					if (state > 0) {
						state--;
						Toast.makeText(this.context, getString(R.string.warning_report_a_bug_start) +
								" (" + Integer.toString(state) + " tries left)", Toast.LENGTH_SHORT).show();
						appPrefs.setUploadStateItem(state);
						return;
					}

					CrashReport app = (CrashReport)getApplicationContext();
					if(!app.getUserEmail().isEmpty() && !app.getUserFirstName().isEmpty() && !app.getUserLastName().isEmpty()) {
						intent = new Intent(getApplicationContext(), BugzillaMainActivity.class);
						intent.putExtra("com.intel.crashreport.bugzilla.fromgallery", false);
						startActivityAsUser(intent, UserHandle.CURRENT);
					}
					else {
						intent = new Intent(getApplicationContext(), UserInformationsActivity.class);
						intent.putExtra("com.intel.crashreport.bugzilla.fromgallery", false);
						startActivityAsUser(intent, UserHandle.CURRENT);
					}

			break;
			case (R.id.button_list_bugzilla):
					intent = new Intent(getApplicationContext(), ListBugzillaActivity.class);
					startActivityAsUser(intent, UserHandle.CURRENT);
			break;
			default:
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		settingsMenu = menu.add(R.string.menu_settings);
		aboutMenu = menu.add(R.string.menu_about);
		super.onCreateOptionsMenu(menu);
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

		return super.onOptionsItemSelected(item);
	}

	public void startCrashReport() {
		Intent intent = new Intent(getApplicationContext(), CrashReportActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityAsUser(intent, UserHandle.CURRENT);
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
					+ "\n" + "Product : " + Build.getProperty(com.intel.crashreport.common.Constants.PRODUCT_PROPERTY_NAME))
					.create();
		}
	}

	public void showDialog() {
		DialogFragment newFragment = AboutDialog.newInstance();
		newFragment.show(getFragmentManager(), "dialog");
	}
}
