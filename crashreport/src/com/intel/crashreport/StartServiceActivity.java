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

package com.intel.crashreport;

import java.util.ArrayList;

import android.app.ActionBar.OnNavigationListener;
import android.app.ActionBar.Tab;
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
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.intel.crashreport.CrashReportService.LocalBinder;
import com.intel.crashreport.CrashReportService.ServiceMsg;
import com.intel.crashreport.specific.Event;
import com.intel.crashreport.database.EventDB;
import com.intel.crashreport.common.Utils.EVENT_FILTER;
import android.app.ActionBar;

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
	private static final int DIALOG_UNKNOWN_VALUE = -1;
	private static Boolean needToStartService = false;
	private ApplicationPreferences appPrefs;
	private static int dialog_value = 0;
	private TextView text;
	private Button cancelButton;
	private ViewStub waitStub;
	private ProgressBar progressBar;
	private Button uploadEvents;
	private static boolean instanceStateSaved = false;
	private static boolean progressBarDisplayable = false;
	private DialogFragment askDialog = null;
	private EventViewAdapter eventAdapter;
	private ListView lvEvent;
	final Context context = this;
	private boolean alreadyRegistered = false;
	private int waitRequests;

	private EVENT_FILTER search = EVENT_FILTER.ALL;

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
					if (mService != null && mService.isServiceUploading())
						mService.cancelDownload();
				}
			});
		setTitle(getString(R.string.activity_name));
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			SpinnerAdapter eventFilterAdapter = ArrayAdapter.createFromResource(actionBar.getThemedContext(), R.array.eventFilterText,
		            R.layout.spinner_dropdown_item);
			actionBar.setListNavigationCallbacks(eventFilterAdapter, eventFilterListener);
			search = appPrefs.getFilterChoice();
			actionBar.setSelectedNavigationItem(search.compareTo(EVENT_FILTER.ALL));
			actionBar.setDisplayShowTitleEnabled(false);
		} else {
			Log.e("Action bar not initialized!");
		}
		waitRequests = 0;

		waitStub = (ViewStub) findViewById(R.id.waitStub);
		progressBar = (ProgressBar) findViewById(R.id.progressAplog);
		lvEvent = (ListView) findViewById(R.id.listEventView);
		eventAdapter = new EventViewAdapter(getApplicationContext());
		if(eventAdapter != null && lvEvent != null) {
			lvEvent.setAdapter(eventAdapter);
			lvEvent.setOnItemClickListener(new OnItemClickListener() {
			   public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
			      Event aEvent = (Event) lvEvent.getItemAtPosition(position);
					AlertDialog alert = new AlertDialog.Builder(context).create();
					if(aEvent != null) {
						alert.setMessage("EventID : " + aEvent.getEventId() + "\n" +
									 " Data0 : " +  aEvent.getData0() + "\n" +
									 " Data1 : " +  aEvent.getData1() + "\n" +
									 " Data2 : " +  aEvent.getData2() + "\n" );
					} else {
						alert.setMessage("No event found.");
					}
					alert.setButton(DialogInterface.BUTTON_NEUTRAL,"OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
						}
					});
					alert.show();
			   }
			});
		}
		instanceStateSaved = false;

		uploadEvents = (Button) findViewById(R.id.summary_events_upload_button);
		if (uploadEvents != null) {
			uploadEvents.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if(DIALOG_REP_ABORT != dialog_value)
						doActionAfterSelectUploadState(DIALOG_REP_READ);
					else
						displayDialog(DIALOG_ASK_UPLOAD_ID);

				}
			});
			uploadEvents.setEnabled(false);
			uploadEvents.setVisibility(View.GONE);
		}
	}

	private OnNavigationListener eventFilterListener = new OnNavigationListener() {
		@Override
		  public boolean onNavigationItemSelected(int position, long itemId) {
			if ((position >= 0) && (position < EVENT_FILTER.values().length))
				search = EVENT_FILTER.values()[position];
			ApplicationPreferences prefs = new ApplicationPreferences(getApplicationContext());
			prefs.setFilterChoice(search);
			updateSummary();
		    return true;
		  }
	};


	private class UpdateSummaryTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			final ArrayList<Event> listEvent = new ArrayList<Event>();

			if (lvEvent == null)
				return null;

			runOnUiThread(new Runnable() {
				public void run() {
					showPleaseWait() ;
				}
			});

			EventDB db = new EventDB(getApplicationContext());
			try {
				db.open();
				Cursor cursor = db.fetchLastNEvents("1000", search);
				if (cursor != null) {
					cursor.moveToFirst();
					while (!cursor.isAfterLast()) {
						Event aEvent = new Event(
								db.fillEventFromCursor(cursor));
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

			runOnUiThread(new Runnable() {
				public void run() {
					eventAdapter.setListEvent(listEvent);
					eventAdapter.notifyDataSetChanged();
					//lvEvent.invalidateViews();
					hidePleaseWait();
				}
			});
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... params) {}

		protected void onPostExecute(Void... params) {}
	}

	private void updateSummary() {
		new UpdateSummaryTask().execute();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		instanceStateSaved = true;
		if (outState != null) {
			if (text != null) {
				outState.putString("textLogger", text.getText().toString());
			}
			super.onSaveInstanceState(outState);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		Intent intent = getIntent();
		if(null != intent){

			if(intent.getBooleanExtra("com.intel.crashreport.extra.fromOutside", false)) {
				dialog_value = DIALOG_UNKNOWN_VALUE;
				intent.removeExtra("com.intel.crashreport.extra.fromOutside");
			}

			if (intent.getBooleanExtra("com.intel.crashreport.extra.notifyEvents", false)){
				intent.removeExtra("com.intel.crashreport.extra.notifyEvents");
				notifyEvents(true);
				NotificationMgr nMgr = new NotificationMgr(getApplicationContext());
				nMgr.cancelNotifCriticalEvent();
				if(appPrefs.isNotificationForAllCrash()) {
					notifyEvents(false);
					nMgr.cancelNotifNoCriticalEvent();
				}
			}
			setIntent(intent);
		}

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
		hideDialog();
		if((null != mService) && mService.isServiceWaitForResponse()) {
			appPrefs.setUploadStateToAsk();
			mService.sendMessage(ServiceMsg.uploadDisabled);
		}
		super.onBackPressed();
	}

	private void startService() {
		Intent crashReportStartServiceIntent = new Intent(this, CrashReportService.class);
		crashReportStartServiceIntent.putExtra("fromActivity", true);
		app.getApplicationContext().startService(crashReportStartServiceIntent);
	}

	private void doBindService() {
		mServiceIntent = new Intent(this, CrashReportService.class);
		bindService(mServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
	}

	private void doUnbindService() {
		if (app.isActivityBounded()) {
			if(mService != null && app.isServiceStarted())
				unbindService(mConnection);
			mService = null;
			app.setActivityBounded(false);
			app.setActivity(null);
			unregisterMsgReceiver();
		}
	}

	private void showPleaseWait() {
		waitRequests++;

		if (waitStub != null)
			waitStub.setVisibility(View.VISIBLE);
		if (progressBar != null) {
			if (progressBarDisplayable)
				progressBar.setVisibility(View.VISIBLE);
			else progressBar.setVisibility(View.GONE);
		}
	}

	private void hidePleaseWait() {
		waitRequests = (--waitRequests<0) ? 0 : waitRequests;
		if (waitRequests != 0) return;

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
		builder.setTitle(getString(R.string.start_service_activity_AskForUpload_title));
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
				dialog_value = DIALOG_REP_ABORT;
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
				if(mService != null && app.isServiceStarted())
					mService.sendMessage(ServiceMsg.uploadImmadiately);
			}
		});
		builder.setNegativeButton(R.string.alert_dialog_no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				appPrefs.setUploadStateToAsk();
				if(mService != null && app.isServiceStarted())
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
			if(mService != null && app.isServiceStarted())
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

		if(mService != null && app.isServiceStarted()) {
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
		uploadEvents.setEnabled(false);
		uploadEvents.setVisibility(View.GONE);

	}

	public synchronized void registerMsgReceiver() {
		if(!alreadyRegistered) {
			Context appCtx = getApplicationContext();
			IntentFilter filter = new IntentFilter();
			filter.addAction(ServiceToActivityMsg.askForUpload);
			filter.addAction(ServiceToActivityMsg.updateLogTextView);
			filter.addAction(ServiceToActivityMsg.uploadStarted);
			filter.addAction(ServiceToActivityMsg.uploadFinished);
			filter.addAction(ServiceToActivityMsg.uploadProgressBar);
			filter.addAction(ServiceToActivityMsg.showProgressBar);
			filter.addAction(ServiceToActivityMsg.hideProgressBar);
			filter.addAction(ServiceToActivityMsg.unbindActivity);
			appCtx.registerReceiver(msgReceiver, filter);
			alreadyRegistered = true;
		}
	}

	public synchronized void unregisterMsgReceiver() {
		if(alreadyRegistered) {
			Context appCtx = getApplicationContext();
			try {
				appCtx.unregisterReceiver(msgReceiver);
			} catch (IllegalArgumentException e) {
				Log.e("StartServiceActivity:unregisterMsgReceiver(IllegalArgumentException): " + e.getMessage());
			}
			alreadyRegistered = false;
		}
	}

	private BroadcastReceiver msgReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (ServiceToActivityMsg.askForUpload.equals(intent.getAction())) {
				switch(dialog_value){
				case DIALOG_UNKNOWN_VALUE:
					displayDialog(DIALOG_ASK_UPLOAD_ID);
					break;

				case DIALOG_REP_NOW:
					String uploadStatePref = appPrefs.getUploadState();
					if (uploadStatePref.contentEquals("uploadImmediately")) {
						doActionAfterSelectUploadState(DIALOG_REP_READ);
						break;
					}
				case DIALOG_REP_ABORT:
					uploadEvents.setEnabled(true);
					uploadEvents.setVisibility(View.VISIBLE);
					uploadEvents.setWidth(cancelButton.getWidth());
					break;

				case DIALOG_REP_NEVER:
				case DIALOG_REP_POSTPONE:
					doActionAfterSelectUploadState(DIALOG_REP_READ);
					break;

				}
			} else if (ServiceToActivityMsg.updateLogTextView.equals(intent.getAction())) {
				if (mService != null)
					text.setText(mService.getLogger().getLog());
				updateSummary();
			} else if (ServiceToActivityMsg.uploadStarted.equals(intent.getAction())) {
				Log.d("StartServiceActivity:msgReceiver: uploadStarted");
				showPleaseWait();
				cancelButton.setEnabled(true);
			} else if (ServiceToActivityMsg.uploadFinished.equals(intent.getAction())) {
				Log.d("StartServiceActivity:msgReceiver: uploadFinish");
				updateSummary();
				hidePleaseWait();
				cancelButton.setEnabled(false);
			} else if (ServiceToActivityMsg.uploadProgressBar.equals(intent.getAction())) {
				if (null != progressBar && progressBarDisplayable) {
					progressBar.setProgress(intent.getIntExtra("progressValue", 0));
				}
			}
			else if (ServiceToActivityMsg.showProgressBar.equals(intent.getAction())) {
				progressBarDisplayable = true;
				if (null != progressBar)
					progressBar.setVisibility(View.VISIBLE);
			}
			else if (ServiceToActivityMsg.hideProgressBar.equals(intent.getAction())) {
				progressBarDisplayable = false;
				if (null != progressBar)
					progressBar.setVisibility(View.GONE);
			}
			else if (ServiceToActivityMsg.unbindActivity.equals(intent.getAction()))
				onKillService();
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
					mService.sendMessage(ServiceMsg.successProcessEvents);
				}
				if (mService.isServiceUploading()) {
					cancelButton.setEnabled(true);
					showPleaseWait();
				}

				if(mService.isServiceWaitForResponse()) {
					switch(dialog_value){

					case DIALOG_REP_NOW:
						String uploadStatePref = appPrefs.getUploadState();
						if (uploadStatePref.contentEquals("uploadImmediately")) {
							doActionAfterSelectUploadState(DIALOG_REP_READ);
							break;
						}
					case DIALOG_REP_ABORT:
						uploadEvents.setEnabled(true);
						uploadEvents.setVisibility(View.VISIBLE);
						uploadEvents.setWidth(cancelButton.getWidth());
						break;

					case DIALOG_REP_NEVER:
					case DIALOG_REP_POSTPONE:
						doActionAfterSelectUploadState(DIALOG_REP_READ);
						break;

					}
				}
			}
		}

		public void onServiceDisconnected(ComponentName name) {
			Log.d("StartServiceActivity: onServiceDisconnected");
			onKillService();
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
		public static final String unbindActivity = "com.intel.crashreport.unbindActivity";
	}

	public void onKillService() {
		hideDialog();
		mService = null;
		unregisterMsgReceiver();
		if (app.isActivityBounded())
			app.setActivityBounded(false);
		cancelButton.setEnabled(false);
		hidePleaseWait();
	}

	public void hideDialog() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);
		if(null != askDialog)
			askDialog.dismiss();
		askDialog = null;
	}

	public void notifyEvents(boolean critical){
		EventDB db = new EventDB(getApplicationContext());
		Event event;
		Cursor cursor;
		try {
			db.open();
			cursor = db.fetchNotNotifiedEvents(critical);
			if (cursor != null) {
				while (!cursor.isAfterLast()) {
					event = new Event(db.fillEventFromCursor(cursor));
					db.updateEventToNotified(event.getEventId());
					cursor.moveToNext();
				}
				cursor.close();
			}
		} catch (SQLException e) {
			Log.w("Service: db Exception");
		}
		db.close();
	}

}
