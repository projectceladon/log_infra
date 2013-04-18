/* Phone Doctor (CLOTA)
 *
 * Copyright (C) Intel 2012
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
 * Author: Jeremy Rocher <jeremyx.rocher@intel.com>
 */

package com.intel.crashreport;


import java.net.ProtocolException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.intel.crashreport.CrashReportService.LocalBinder;
import com.intel.crashreport.CrashReportService.ServiceMsg;

public class StartServiceActivity extends Activity {

	private CrashReport app;
	private Intent mServiceIntent;
	private CrashReportService mService = null;
	private static final int DIALOG_ASK_UPLOAD_ID = 0;
	private static final int DIALOG_ASK_UPLOAD_SAVE_ID = 1;
	private static final int DIALOG_REP_NOW = 0;
	private static final int DIALOG_REP_POSTPONE = 1;
	private static final int DIALOG_REP_NEVER = 2;
	private static final int DIALOG_REP_ABORT = 3;
	private static final int DIALOG_REP_READ = 4;
	private static Boolean needToStartService = false;
	private ApplicationPreferences appPrefs;
	private int dialog_value = 0;
	private TextView text;
	private Button cancelButton;
	private ViewStub waitStub;
	private ProgressBar progressBar;
	private static boolean instanceStateSaved = false;
	private static boolean progressBarDisplayable = false;
	private DialogFragment askDialog = null;
	private EventViewAdapter eventAdapter;
	private ListView lvEvent;
	final Context context = this;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		app = (CrashReport)getApplicationContext();
		appPrefs = new ApplicationPreferences(getApplicationContext());
		text = (TextView) findViewById(R.id.logTextView);
		if (text != null) {
			if (savedInstanceState != null)
				text.setText(savedInstanceState.getString("textLogger"));
			else
				text.setText("");
		}
		cancelButton = (Button) findViewById(R.id.buttonCancel);
		if (cancelButton != null)
			cancelButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (mService.isServiceUploading())
						mService.cancelDownload();
				}
			});
		setTitle(getString(R.string.app_name)+" "+getString(R.string.app_version));
		waitStub = (ViewStub) findViewById(R.id.waitStub);
		progressBar = (ProgressBar) findViewById(R.id.progressAplog);
		lvEvent = (ListView) findViewById(R.id.listEventView);
		eventAdapter = new EventViewAdapter(getApplicationContext());
		lvEvent.setAdapter(eventAdapter);
		lvEvent.setOnItemClickListener(new OnItemClickListener() {
			   public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
			      Event aEvent = (Event) lvEvent.getItemAtPosition(position);
					AlertDialog alert = new AlertDialog.Builder(context).create();
					alert.setMessage("EventID : " + aEvent.getEventId() + "\n" +
									 " Data0 : " +  aEvent.getData0() + "\n" +
									 " Data1 : " +  aEvent.getData1() + "\n" +
									 " Data2 : " +  aEvent.getData2() + "\n" );
					alert.setButton(DialogInterface.BUTTON_NEUTRAL,"OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
						}
					});
					alert.show();
			   }
			});
		instanceStateSaved = false;
	}

	private void updateSummary() {
			ArrayList<Event> listEvent = new ArrayList<Event>();
			EventDB db = new EventDB(getApplicationContext());
			try {
				db.open();
				Cursor cursor = db.fetchLastNEvents("1000");
				if (cursor != null) {
					cursor.moveToFirst();
					while (!cursor.isAfterLast()) {
						Event aEvent = db.fillEventFromCursor(cursor);
						listEvent.add(aEvent);
						cursor.moveToNext();
					}
					cursor.close();
				}
				db.close();
			}
			catch (Exception e){
				Log.e("Exception occured while generating summary :" + e.getMessage());
			}
			if (lvEvent != null)
			{
				eventAdapter.setListEvent(listEvent);
				eventAdapter.notifyDataSetChanged();
				//lvEvent.invalidateViews();
			}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		instanceStateSaved = true;
		outState.putString("textLogger", (String)text.getText());
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!app.isServiceStarted()) {
			needToStartService = true;
			startService();
		}
		if (!app.isActivityBounded())
			doBindService();
		if(app.isActivityBounded())
			app.setActivity(this);
		instanceStateSaved = false;
		updateSummary();
	}

	@Override
	protected void onPause() {
		Log.d("StartServiceActivity: onPause");
		doUnbindService();
		cancelButton.setEnabled(false);
		hidePleaseWait();
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		Log.d("StartServiceActivity: onBackPressed");
		if(askDialog != null)askDialog.dismiss();
		askDialog = null;
		super.onBackPressed();
	}

	private void startService() {
		Intent crashReportStartServiceIntent = new Intent("com.intel.crashreport.CrashReportService");
		crashReportStartServiceIntent.putExtra("fromActivity", true);
		app.getApplicationContext().startService(crashReportStartServiceIntent);
	}

	private void doBindService() {
		mServiceIntent = new Intent(this, CrashReportService.class);
		bindService(mServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
	}

	private void doUnbindService() {
		if (app.isActivityBounded()) {
			unbindService(mConnection);
			mService = null;
			app.setActivityBounded(false);
			app.setActivity(null);
			unregisterMsgReceiver();
		}
	}

	private void showPleaseWait() {
		if (waitStub != null)
			waitStub.setVisibility(View.VISIBLE);
		if (progressBar != null) {
			if (progressBarDisplayable)
				progressBar.setVisibility(View.VISIBLE);
			else progressBar.setVisibility(View.GONE);
		}
	}

	private void hidePleaseWait() {
		if (waitStub != null)
			waitStub.setVisibility(View.GONE);
		if (progressBar != null)
			progressBar.setVisibility(View.GONE);
	}

	/**
	 * Create the Dialog windows that prompts the user to select an uploading mode for events not yet uploaded.
	 * The user choice is validated once he has clicked on positive button ("OK")
	 * @return the Dialog built.
	 */
	public Dialog createAskForUploadDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("PSI Phone Doctor management");
		builder.setSingleChoiceItems(R.array.uploadStateDialogText, DIALOG_REP_NOW, null); /*Don't need listener on checked items*/
		builder.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				/*Once user choice is validated, the single checked item is got. If checked item position is invalid, default value is "REPORT NOW"*/
				dialog_value = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
				dialog_value = dialog_value == android.widget.AbsListView.INVALID_POSITION ? DIALOG_REP_NOW : dialog_value;
				doActionAfterSelectUploadState(DIALOG_REP_READ);
			}
		});
		builder.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				doActionAfterSelectUploadState(DIALOG_REP_ABORT);
			}
		});
		return builder.create();
	}

	public Dialog createAskForUploadSaveDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.alert_dialog_upload_save);
		builder.setPositiveButton(R.string.alert_dialog_yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				appPrefs.setUploadStateToUpload();
				mService.sendMessage(ServiceMsg.uploadImmadiately);
			}
		});
		builder.setNegativeButton(R.string.alert_dialog_no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				appPrefs.setUploadStateToAsk();
				mService.sendMessage(ServiceMsg.uploadImmadiately);
			}
		});
		return builder.create();
	}

	public void displayDialog(int id) {
		if (!instanceStateSaved){
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			Fragment prev = getFragmentManager().findFragmentByTag("dialog");
			if (prev != null) {
				ft.remove(prev);
			}
			ft.addToBackStack(null);
			askDialog = AskDialog.newInstance(id);
			askDialog.show(getFragmentManager(), "dialog");
		}
	}

	public void cancel(int num) {
		// TODO Auto-generated method stub
		switch(num){
		case DIALOG_ASK_UPLOAD_ID:
			doActionAfterSelectUploadState(DIALOG_REP_ABORT);
			break;
		case DIALOG_ASK_UPLOAD_SAVE_ID:
			appPrefs.setUploadStateToAsk();
			mService.sendMessage(ServiceMsg.uploadDisabled);
			break;
		default:
			break;
		}
	}

	private void doActionAfterSelectUploadState(int response) {
		int value;
		if (response == DIALOG_REP_READ)
			value = this.dialog_value;
		else
			value = response;

		switch (value) {
		case DIALOG_REP_NOW:
			String uploadStatePref = appPrefs.getUploadState();
			if (uploadStatePref.contentEquals("uploadImmediately"))
				mService.sendMessage(ServiceMsg.uploadImmadiately);
			else
				displayDialog(DIALOG_ASK_UPLOAD_SAVE_ID);
			break;
		case DIALOG_REP_POSTPONE:
			appPrefs.setUploadStateToReport();
			mService.sendMessage(ServiceMsg.uploadReported);
			break;
		case DIALOG_REP_NEVER:
			appPrefs.setUploadStateToNeverButNotify();
			mService.sendMessage(ServiceMsg.uploadDisabled);
			break;
		default:
			appPrefs.setUploadStateToAsk();
			mService.sendMessage(ServiceMsg.uploadDisabled);
			break;
		}
	}

	public void registerMsgReceiver() {
		Context appCtx = getApplicationContext();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ServiceToActivityMsg.askForUpload);
		filter.addAction(ServiceToActivityMsg.updateLogTextView);
		filter.addAction(ServiceToActivityMsg.uploadStarted);
		filter.addAction(ServiceToActivityMsg.uploadFinished);
		filter.addAction(ServiceToActivityMsg.uploadProgressBar);
		filter.addAction(ServiceToActivityMsg.showProgressBar);
		filter.addAction(ServiceToActivityMsg.hideProgressBar);
		appCtx.registerReceiver(msgReceiver, filter);
	}

	public void unregisterMsgReceiver() {
		Context appCtx = getApplicationContext();
		appCtx.unregisterReceiver(msgReceiver);
	}

	private BroadcastReceiver msgReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ServiceToActivityMsg.askForUpload)) {
				displayDialog(DIALOG_ASK_UPLOAD_ID);
			} else if (intent.getAction().equals(ServiceToActivityMsg.updateLogTextView)) {
				text.setText(mService.getLogger().getLog());
				updateSummary();
			} else if (intent.getAction().equals(ServiceToActivityMsg.uploadStarted)) {
				Log.d("StartServiceActivity:msgReceiver: uploadStarted");
				showPleaseWait();
				cancelButton.setEnabled(true);
			} else if (intent.getAction().equals(ServiceToActivityMsg.uploadFinished)) {
				Log.d("StartServiceActivity:msgReceiver: uploadFinish");
				updateSummary();
				hidePleaseWait();
				cancelButton.setEnabled(false);
			} else if (intent.getAction().equals(ServiceToActivityMsg.uploadProgressBar)) {
				if (null != progressBar && progressBarDisplayable) {
					progressBar.setProgress(intent.getIntExtra("progressValue", 0));
				}
			}
			else if (intent.getAction().equals(ServiceToActivityMsg.showProgressBar)) {
				progressBarDisplayable = true;
				if (null != progressBar)
					progressBar.setVisibility(View.VISIBLE);
			}
			else if (intent.getAction().equals(ServiceToActivityMsg.hideProgressBar)) {
				progressBarDisplayable = false;
				if (null != progressBar)
					progressBar.setVisibility(View.GONE);
			}
		}
	};

	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d("StartServiceActivity: onServiceConnected");
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			if(mService == null){
				Log.d("StartServiceActivity: onServiceConnected: CrashReportService creation failed");
				text.setText("CrashReportService is not created!");
				onServiceDisconnected(name);
			} else {
				text.setText(mService.getLogger().getLog());
				registerMsgReceiver();
				app.setActivityBounded(true);
				if (needToStartService) {
					needToStartService = false;
					mService.sendMessage(ServiceMsg.startProcessEvents);
				}
				if (mService.isServiceUploading()) {
					cancelButton.setEnabled(true);
					showPleaseWait();
				}
			}
		}

		public void onServiceDisconnected(ComponentName name) {
			Log.d("StartServiceActivity: onServiceDisconnected");
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			Fragment prev = getFragmentManager().findFragmentByTag("dialog");
			if (prev != null) {
				ft.remove(prev);
			}
			ft.addToBackStack(null);
			if(null != askDialog)
				askDialog.dismiss();
			askDialog = null;
			mService = null;
			unregisterMsgReceiver();
			if (app.isActivityBounded())
				app.setActivityBounded(false);
			cancelButton.setEnabled(false);
			hidePleaseWait();
		}

	};

	protected class ServiceToActivityMsg {
		public static final String askForUpload = "com.intel.crashreport.askForUpload";
		public static final String updateLogTextView = "com.intel.crashreport.updateLogTextView";
		public static final String uploadStarted = "com.intel.crashreport.uploadStarted";
		public static final String uploadFinished = "com.intel.crashreport.uploadFinished";
		public static final String uploadProgressBar = "com.intel.crashreport.uploadProgressBarView";
		public static final String showProgressBar = "com.intel.crashreport.showProgressBarView";
		public static final String hideProgressBar = "com.intel.crashreport.hideProgressBarView";
	}

}
