
package com.intel.crashreport;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.intel.crashreport.bugzilla.ui.BugzillaMainActivity;
import com.intel.crashreport.bugzilla.ui.ListBugzillaActivity;
import com.intel.crashreport.bugzilla.ui.UserInformationsActivity;
import com.intel.crashreport.logconfig.ui.LogConfigHomeActivity;

public class CrashReportHome extends Activity {
	private MenuItem aboutMenu;
	private MenuItem settingsMenu;
	final Context context = this;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		Button button_start = (Button) findViewById(R.id.button_report_events);
		// Attach a click listener for launching the system settings.
		button_start.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent("com.intel.crashreport.intent.START_SERVICE");
				startActivity(intent);
			}
		});

		Button button_logconfig = (Button) findViewById(R.id.button_logconfig);
		// Attach a click listener for launching the system settings.
		button_logconfig.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), LogConfigHomeActivity.class);
				startActivity(intent);
			}
		});

		Button button_aplogs = (Button) findViewById(R.id.button_report_aplogs);
		button_aplogs.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new UploadAplogTask().execute();
				AlertDialog alert = new AlertDialog.Builder(context).create();
				alert.setMessage("A background request of log upload has been created.");
				alert.setButton(DialogInterface.BUTTON_NEUTRAL,"OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					}
				});
				alert.show();
			}
		});

		Button button_bugzilla = (Button) findViewById(R.id.button_report_bugzilla);
		button_bugzilla.setOnClickListener(new View.OnClickListener() {
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

		Button button_list_bugzilla = (Button) findViewById(R.id.button_list_bugzilla);
		button_list_bugzilla.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), ListBugzillaActivity.class);
				startActivity(intent);
			}
		});
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
					+ "\n" + "© Intel 2012.")
					.create();
		}
	}

	public void showDialog() {
		DialogFragment newFragment = AboutDialog.newInstance();
		newFragment.show(getFragmentManager(), "dialog");
	}

	private class UploadAplogTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			File aplogTrigger = new File("/logs/aplogs/aplog_trigger");
			if (!aplogTrigger.exists()) {
				//to manage case  of crashlogd not launched
				aplogTrigger.delete();
			}
			try {
				BufferedOutputStream write = new BufferedOutputStream(new FileOutputStream(aplogTrigger));
				write.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}


			return null;
		}

		@Override
		protected void onProgressUpdate(Void... params) {
		}

		protected void onPostExecute(Void... params) {

		}

	}
}
