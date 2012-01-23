/* Crash Report (CLOTA)
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;

import com.intel.crashreport.CrashReportService.LocalBinder;
import com.intel.crashreport.CrashReportService.ServiceMsg;

public class StartServiceActivity extends Activity {

	private CrashReport app;
	private Intent mServiceIntent;
	private CrashReportService mService = null;
	private static final int DIALOG_ASK_UPLOAD_ID = 0;
	private static final int DIALOG_ASK_UPLOAD_SAVE_ID = 1;
	private static final int DIALOG_UPLOAD_PROGRESS_ID = 2;
	private static final int DIALOG_REP_NOW = 0;
	private static final int DIALOG_REP_POSTPONE = 1;
	private static final int DIALOG_REP_NEVER = 2;
	private static final int DIALOG_REP_ABORT = 3;
	private static final int DIALOG_REP_READ = 4;
	private static ProgressDialog uploadProgress;
	private static Boolean needToStartService = false;
	private ApplicationPreferences appPrefs;
	private int dialog_value = 0;
	private TextView text;

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
	}

	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("textLogger", (String)text.getText());
		super.onSaveInstanceState(outState);
	}

	protected void onResume() {
		if (!app.isServiceStarted()) {
			needToStartService = true;
			startService();
		}
		if (!app.isActivityBounded())
			doBindService();
		super.onResume();
	}

	protected void onPause() {
		Log.d("StartServiceActivity: onPause");
		doUnbindService();
		super.onPause();
	}

	public void onBackPressed() {
		Log.d("StartServiceActivity: onBackPressed");
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
			unregisterMsgReceiver();
		}
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch(id) {
		case DIALOG_ASK_UPLOAD_ID:
			dialog = createAskForUploadDialog();
			break;
		case DIALOG_ASK_UPLOAD_SAVE_ID:
			dialog = createAskForUploadSaveDialog();
			break;
		case DIALOG_UPLOAD_PROGRESS_ID:
			dialog = createUploadProgress();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

	private Dialog createAskForUploadDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Crash report management");
		builder.setSingleChoiceItems(R.array.uploadStateDialogText, 0, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				dialog_value = item;
			}
		});
		builder.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				doActionAfterSelectUploadState(DIALOG_REP_READ);
			}
		});
		builder.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				doActionAfterSelectUploadState(DIALOG_REP_ABORT);
			}
		});
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				doActionAfterSelectUploadState(DIALOG_REP_ABORT);
			}
		});
		return builder.create();
	}

	private Dialog createAskForUploadSaveDialog() {
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
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				appPrefs.setUploadStateToAsk();
				mService.sendMessage(ServiceMsg.uploadDisabled);
			}
		});
		return builder.create();
	}

	private Dialog createUploadProgress() {
		uploadProgress = new ProgressDialog(this);
		uploadProgress.setMessage("Uploading report. Please wait...");
		return uploadProgress;
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
				showDialog(DIALOG_ASK_UPLOAD_SAVE_ID);
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
		appCtx.registerReceiver(msgReceiver, filter);
	}

	public void unregisterMsgReceiver() {
		Context appCtx = getApplicationContext();
		appCtx.unregisterReceiver(msgReceiver);
	}

	private BroadcastReceiver msgReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ServiceToActivityMsg.askForUpload)) {
				showDialog(DIALOG_ASK_UPLOAD_ID);
			} else if (intent.getAction().equals(ServiceToActivityMsg.updateLogTextView)) {
				text.setText(mService.getLogger().getLog());
			} else if (intent.getAction().equals(ServiceToActivityMsg.uploadStarted)) {
				Log.d("StartServiceActivity:msgReceiver: uploadStarted");
				showDialog(DIALOG_UPLOAD_PROGRESS_ID);
			} else if (intent.getAction().equals(ServiceToActivityMsg.uploadFinished)) {
				Log.d("StartServiceActivity:msgReceiver: uploadFinish");
				if ((uploadProgress != null) && uploadProgress.isShowing())
					uploadProgress.dismiss();
			}
		}
	};

	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d("StartServiceActivity: onServiceConnected");
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			text.setText(mService.getLogger().getLog());
			registerMsgReceiver();
			app.setActivityBounded(true);
			if (needToStartService) {
				needToStartService = false;
				mService.sendMessage(ServiceMsg.startProcessEvents);
			}
		}

		public void onServiceDisconnected(ComponentName name) {
			Log.d("StartServiceActivity: onServiceDisconnected");
			mService = null;
			unregisterMsgReceiver();
			if (app.isActivityBounded())
				app.setActivityBounded(false);
		}

	};

	protected class ServiceToActivityMsg {
		public static final String askForUpload = "com.intel.crashreport.askForUpload";
		public static final String updateLogTextView = "com.intel.crashreport.updateLogTextView";
		public static final String uploadStarted = "com.intel.crashreport.uploadStarted";
		public static final String uploadFinished = "com.intel.crashreport.uploadFinished";
	}

}
