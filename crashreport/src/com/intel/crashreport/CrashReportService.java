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

import java.io.File;
import java.io.IOException;
import java.net.ProtocolException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import com.intel.crashreport.StartServiceActivity.ServiceToActivityMsg;
import com.intel.crashreport.specific.Build;
import com.intel.crashreport.specific.Event;
import com.intel.crashreport.database.EventDB;
import com.intel.crashreport.specific.PDStatus;
import com.intel.crashreport.specific.PDStatus.PDSTATUS_TIME;
import com.intel.crashreport.threads.EventUploadThread;
import com.intel.crashtoolserver.bean.FileInfo;

public class CrashReportService extends Service {

	private CrashReport app;
	private HandlerThread handlerThread;
	private Thread runThread = null;
	private ServiceHandler serviceHandler;
	private ServiceState serviceState;
	private final IBinder mBinder = new LocalBinder();
	private Logger logger = new Logger();
	public static final DateFormat DAY_DF = new SimpleDateFormat("yyyy-MM-dd");
	private static final String MODULE = "CrashReportService";

	@Override
	public void onCreate() {
		super.onCreate();
		app = (CrashReport)getApplicationContext();
		Log.d(MODULE+": created");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(MODULE+": onStartCommand");
		if (app.isServiceStarted()) {
			Log.d(MODULE+": Service already running");
		} else {
			Log.d(MODULE+": Not already started");
			handlerThread = new HandlerThread("CrashReportService_Thread");
			handlerThread.start();
			while(!handlerThread.isAlive()) {};
			Looper handlerThreadLooper = handlerThread.getLooper();
			if (handlerThreadLooper != null) {
				serviceHandler = new ServiceHandler(handlerThreadLooper);
				app.setServiceStarted(true);
				app.setUploadService(this);
				this.serviceState = ServiceState.ProcessEvent;
				logger.clearLog();
				Build myBuild = new Build(getApplicationContext());
				myBuild.fillBuildWithSystem();
				app.setMyBuild(myBuild);
				if (!intent.getBooleanExtra("fromActivity", false))
					this.serviceHandler.sendEmptyMessageDelayed(ServiceMsg.successProcessEvents, 100);
			} else {
				stopService();
			}
		}
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		stopUploadThread();

		if(app.isServiceStarted()) {
			app.setServiceStarted(false);
			if(app.isActivityBounded()) {
				Intent hideDialog = new Intent(ServiceToActivityMsg.unbindActivity);
				getApplicationContext().sendBroadcastAsUser(hideDialog, UserHandle.CURRENT);
			}
		}
		app.setUploadService(null);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.d(MODULE+": onBind");
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(MODULE+": onUnbind");
		return false;
	}

	private void stopService() {
		Log.d(MODULE+":Stop Service");
		app.setServiceStarted(false);
		if(app.isActivityBounded()) {
			Intent hideDialog = new Intent(ServiceToActivityMsg.unbindActivity);
			getApplicationContext().sendBroadcastAsUser(hideDialog, UserHandle.CURRENT);
		}
		app.setUploadService(null);
		if(handlerThread != null) {
			handlerThread.quit();
		}
		stopSelf();
	}

	public Boolean isServiceUploading() {
		return (serviceState == ServiceState.UploadEvent);
	}

	public boolean isServiceWaitForResponse() {
		return (serviceState == ServiceState.WaitForUploadResponse);
	}

	public void cancelDownload() {
		if (serviceState == ServiceState.UploadEvent)
			stopUploadThread();
	}

	public void stopUploadThread() {
		if ((runThread != null) && runThread.isAlive())
			runThread.interrupt();
	}

	private Runnable processEvent = new Runnable(){

		public void run() {
			serviceHandler.sendEmptyMessage(ServiceMsg.successProcessEvents);
		}
	};

	private Runnable getUploadState = new Runnable() {
		ApplicationPreferences prefs;
		public void run() {
			prefs = new ApplicationPreferences(getApplicationContext());
			String uploadState = prefs.getUploadState();
			if ( SystemProperties.get("persist.vendor.crashreport.disabled", "0").equals("1")) {
				Log.d(MODULE+": Property persist.vendor.crashreport.disabled set to 1");
				serviceHandler.sendEmptyMessage(ServiceMsg.uploadDisabled);
			}
			else{
				if (uploadState.contentEquals("uploadImmediately"))
					serviceHandler.sendEmptyMessage(ServiceMsg.uploadImmadiately);
				else if (uploadState.contentEquals("uploadReported"))
					serviceHandler.sendEmptyMessage(ServiceMsg.uploadReported);
				else if (uploadState.contentEquals("uploadDisabled"))
					serviceHandler.sendEmptyMessage(ServiceMsg.uploadDisabled);
				else
					serviceHandler.sendEmptyMessage(ServiceMsg.askForUpload);
			}
		}
	};

	private Runnable isThereEventToUpload = new Runnable() {
		EventDB db;
		ApplicationPreferences prefs;
		Context context;
		public void run() {
			context = getApplicationContext();
			db = new EventDB(context);
			prefs = new ApplicationPreferences(context);
			try {
				db.open();
				if (!app.isActivityBounded() && prefs.getUploadState().equals("askForUpload")) {
					if (db.isThereEventToUploadNoReboot())
						serviceHandler.sendEmptyMessage(ServiceMsg.eventToUpload);
					else
						serviceHandler.sendEmptyMessage(ServiceMsg.noEventToUpload);
				} else {
					String crashTypes[] = null;
					if (prefs.isCrashLogsUploadEnable())
						crashTypes = prefs.getCrashLogsUploadTypes();
					else Log.d(MODULE+":isThereEventToUpload : upload logs disabled");
					if (db.isThereEventToUpload(crashTypes)) {
						Message msg = Message.obtain();
						msg.what = ServiceMsg.eventToUpload;
						msg.arg1 = db.getNewRebootNumber();
						msg.arg2 = db.getNewCrashNumber();
						serviceHandler.sendMessage(msg);
					} else
						serviceHandler.sendEmptyMessage(ServiceMsg.noEventToUpload);
				}
			} catch (SQLException e) {
				Log.w(MODULE+":isThereEventToUpload : Fail to open DB");
				serviceHandler.sendEmptyMessage(ServiceMsg.noEventToUpload);
			}
			db.close();

		}
	};

	private Runnable isServiceBoundedToActivity = new Runnable() {
		public void run() {
			if (app.isActivityBounded())
				serviceHandler.sendEmptyMessage(ServiceMsg.isBoundedToActivity);
			else
				serviceHandler.sendEmptyMessage(ServiceMsg.notBoundedToActivity);
		}
	};

	// TODO remove
	private Runnable isUploadWifiOnly = new Runnable() {
		public void run() {
			// Boolean wifiOnly = new ApplicationPreferences(app).isWifiOnlyForEventData();
			Boolean wifiOnly = false;
			if (wifiOnly) {
				app.setWifiOnly(true);
				serviceHandler.sendEmptyMessage(ServiceMsg.wifiOnly);
			}else {
				app.setWifiOnly(false);
				serviceHandler.sendEmptyMessage(ServiceMsg.noWifiOnly);
			}
		}
	};

	private Runnable isInternalWifiAvailable = new Runnable() {
		public void run() {
			Connector con = new Connector(getApplicationContext(), serviceHandler);
			con.getWifiAvailability();
		}
	};

	private Runnable isDataConnectionAvailable = new Runnable() {
		public void run() {
			Connector con = new Connector(getApplicationContext(), serviceHandler);
			if (con.getDataConnectionAvailability()){
				//TODO register ACTION_BACKGROUND_DATA_SETTING_CHANGED
				serviceHandler.sendEmptyMessage(ServiceMsg.dataConnectionAvailable);
			} else {
				serviceHandler.sendEmptyMessage(ServiceMsg.dataConnectionNotAvailable);
			}
		}
	};

	private Runnable askForUpload = new Runnable() {
		public void run() {
			if (app.isActivityBounded()) {
				if( SystemProperties.get("persist.vendor.crashreport.disabled", "0").equals("1") ) {
					sendMsgToActivity("Warning : Background Upload is disabled due to property persist.vendor.crashreport.disabled set to 1");
					Log.d(MODULE+": Property persist.vendor.crashreport.disabled set to 1");
					serviceHandler.sendEmptyMessage(ServiceMsg.uploadDisabled);
				} else {
					ApplicationPreferences prefs = new ApplicationPreferences(getApplicationContext());
					String uploadState = prefs.getUploadState();
					if (uploadState.contentEquals("uploadImmediately"))
						serviceHandler.sendEmptyMessage(ServiceMsg.uploadImmadiately);
					else if (uploadState.contentEquals("uploadReported"))
						serviceHandler.sendEmptyMessage(ServiceMsg.uploadReported);
					else if (uploadState.contentEquals("uploadDisabled"))
						serviceHandler.sendEmptyMessage(ServiceMsg.uploadDisabled);
					else {
						Intent askForUploadIntent = new Intent(ServiceToActivityMsg.askForUpload);
						getApplicationContext().sendBroadcastAsUser(askForUploadIntent, UserHandle.CURRENT);
					}
				}
			} else {
				Log.w(MODULE+":askForUpload: Activity not bounded to service");
			}
		}
	};

	private int setDateToRetry() {
		AlarmMgr alarm = new AlarmMgr(getApplicationContext());
		return alarm.setDateToRetryOneHour();
	}

	private int checkDateToRetry() {
		AlarmMgr alarm = new AlarmMgr(getApplicationContext());
		return alarm.checkDateToRetry();
	}

	private void removeAlarmIfAny() {
		AlarmMgr alarm = new AlarmMgr(getApplicationContext());
		alarm.removeAlarmIfAny();
	}

	private void notifyEventToUpload() {
		NotificationMgr nMgr = new NotificationMgr(getApplicationContext());
		EventDB db = new EventDB(getApplicationContext());
		try {
			db.open();
			int crashNumber = db.getNewCrashNumber();
			int uptimeNumber = db.getNewUptimeNumber();
			nMgr.notifyEventToUpload(crashNumber, uptimeNumber);
		} catch (SQLException e) {
			Log.w(MODULE+":notifyEventToUpload : Fail to access DB", e);
		}
		db.close();
	}

	private void registerNetworkStateReceiver() {
		Log.d(MODULE+": registerNetworkStateReceiver");
		PackageManager pm = app.getPackageManager();
		pm.setComponentEnabledSetting(new ComponentName(app, NetworkStateReceiver.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
	}

	private Runnable getWifiState = new Runnable() {
		public void run() {
			Connector con = new Connector(getApplicationContext(), serviceHandler);
			con.getConnectionState();
		}
	};

	private Runnable connectToNetwork = new Runnable() {
		public void run() {
			Connector con = new Connector(getApplicationContext(), serviceHandler);
			con.connect();
		}
	};

	private Runnable disconnectToNetwork = new Runnable() {
		public void run() {
			Connector con = new Connector(getApplicationContext(), serviceHandler);
			con.disconnect();
		}
	};

	private void uploadProgressStart() {
		if (app.isActivityBounded()) {
			Intent uploadStartedIntent = new Intent(ServiceToActivityMsg.uploadStarted);
			getApplicationContext().sendBroadcastAsUser(uploadStartedIntent, UserHandle.CURRENT);
		}
	}

	private void uploadProgressStop() {
		if (app.isActivityBounded()) {
			Intent uploadFinishedIntent = new Intent(ServiceToActivityMsg.uploadFinished);
			getApplicationContext().sendBroadcastAsUser(uploadFinishedIntent, UserHandle.CURRENT);
		}
	}

	public void updateProgressBar(int value) {
		if (app.isActivityBounded()) {
			Intent intent = new Intent(ServiceToActivityMsg.uploadProgressBar);
			intent.putExtra("progressValue", value);
			getApplicationContext().sendBroadcastAsUser(intent, UserHandle.CURRENT);
		}
	}

	public void hideProgressBar() {
		if (app.isActivityBounded()) {
			Intent intent = new Intent(ServiceToActivityMsg.hideProgressBar);
			getApplicationContext().sendBroadcastAsUser(intent, UserHandle.CURRENT);
		}
	}

	public void showProgressBar() {
		if (app.isActivityBounded()) {
			Intent intent = new Intent(ServiceToActivityMsg.showProgressBar);
			getApplicationContext().sendBroadcastAsUser(intent, UserHandle.CURRENT);
		}
	}


	public void sendEvents(EventDB db, Connector con,Build myBuild) throws InterruptedException,ProtocolException,SQLException{
		Cursor cursor = db.fetchNotUploadedEvents();
		Event event;
		hideProgressBar();
		if (cursor != null) {
			PDStatus.INSTANCE.setContext(getApplicationContext());
			while (!cursor.isAfterLast()) {
				if (runThread == null || runThread.isInterrupted()) {
					cursor.close();
					throw new InterruptedException();
				}
				event = new Event(db.fillEventFromCursor(cursor));
				event.setPdStatus(PDStatus.INSTANCE.computePDStatus(event, PDSTATUS_TIME.UPLOAD_TIME));
				com.intel.crashtoolserver.bean.Event sEvent = event.getEventForServer(myBuild, app.getTokenGCM());
				if (con.sendEvent(sEvent)) {
					db.updateEventToUploaded(event.getEventId());
					db.updatePDStatus(event.getPdStatus(), event.getEventId());
					Log.i(MODULE+":uploadEvent : Success upload of " + event);
					cursor.moveToNext();
				} else {
					Log.w(MODULE+":uploadEvent : Fail upload of " + event);
					cursor.close();
					throw new ProtocolException();
				}
			}
			cursor.close();
		}
	}

	public void updateEventsSummary(EventDB db) {
		logger.clearLog();
		int uptimeNumber = db.getNewRebootNumber();
		int crashNumber = db.getNewCrashNumber();
		if ((crashNumber == 0) && (uptimeNumber != 0))
			sendMsgToActivity("There is Uptime event to report");
		else if ((crashNumber == 1) && (uptimeNumber == 0))
			sendMsgToActivity("There is 1 Crash event to report");
		else if ((crashNumber == 1) && (uptimeNumber != 0))
			sendMsgToActivity("There are Uptime and 1 Crash events to report");
		else if ((crashNumber > 1) && (uptimeNumber == 0))
			sendMsgToActivity("There are "+crashNumber+" Crash events to report");
		else if ((crashNumber > 1) && (uptimeNumber != 0))
			sendMsgToActivity("There are Uptime and "+crashNumber+" Crash events to report");
		else
			sendMsgToActivity("There are events to report");
	}

	private crRunnable uploadEvent = new crRunnable(this);

	public class crRunnable implements Runnable {
		Context context;
		PowerManager pm = null;
		PowerManager.WakeLock wakeLock = null;
		ApplicationPreferences prefs;
		NotificationMgr nMgr;
		EventDB db;
		Cursor cursor;
		CrashReportService crService;

		public crRunnable(CrashReportService aCrObject){
			crService = aCrObject;
		}

		public void run() {
			if (runThread != null){
				Log.w(MODULE +
						": attempt to restart upload, while a thread is already running");
				return;
			}

			context = getApplicationContext();
			prefs = new ApplicationPreferences(context);
			db = new EventDB(context);
			pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CrashReport");
			wakeLock.acquire();

			// Create the thread instance and run it
			EventUploadThread uploadExec = new EventUploadThread(
					context,
					prefs,
					serviceHandler,
					db,
					crService);
			runThread = new Thread(uploadExec, "UploadThread");
			uploadExec.setRunningThread(runThread);
			runThread.start();
			try {
				runThread.join();
				runThread = null;
			} catch (InterruptedException e) {
				runThread = null;
				serviceHandler.sendEmptyMessage(ServiceMsg.cancelUpload);
			}
			boolean needsWifi = uploadExec.needsWifi();
			if (nMgr != null){
				if(needsWifi)
					nMgr.notifyConnectWifiOrMpta();
				else
					nMgr.cancelNotifUploadingLogs();
			}
			if (cursor != null) {
				cursor.close();
			}
			if (db != null)
				db.close();
			if (wakeLock != null) {
				wakeLock.release();
				wakeLock = null;
			}
			uploadProgressStop();
		}
	};

	private void sendMsgToActivity(String msg) {
		logger.addMsg(msg);
		if (app.isActivityBounded()) {
			Intent updateLogIntent = new Intent(ServiceToActivityMsg.updateLogTextView);
			getApplicationContext().sendBroadcastAsUser(updateLogIntent, UserHandle.CURRENT);
		}
	}

	public Logger getLogger() {
		return logger;
	}

	private enum ServiceState {
		Init, ProcessEvent, WaitForEventToUpload, WaitUploadState,
		WaitForInternalWifiAvailable, WaitForUploadResponse, WaitWifiState,
		Connecting, UploadEvent, Disconnecting, Exit,
		IsBoundedToActivity, WaitForUploadWifiOnly,
		WaitForDataConnectionAvailableUser, WaitForDataConnectionAvailableAuto;
	}

	public class ServiceMsg {

		public static final int startProcessEvents = 0;
		public static final int successProcessEvents = 1;
		public static final int failProcessEvents = 2;
		public static final int uploadDisabled = 3;
		public static final int uploadReported = 4;
		public static final int uploadImmadiately = 17;
		public static final int askForUpload = 18;
		public static final int noEventToUpload = 5;
		public static final int eventToUpload = 6;
		public static final int internalwifiNotAvailable = 19;
		public static final int internalwifiAvailable = 20;
		public static final int wifiConnectedInternal = 7;
		public static final int wifiConnectedOther = 8;
		public static final int wifiNotConnected = 9;
		public static final int wifiConnectTimeOut = 10;
		public static final int wifiDisconnected = 11;
		public static final int uploadOK = 14;
		public static final int uploadFailFromConnection = 15;
		public static final int uploadFailFromSQL = 16;
		public static final int killService = 21;
		public static final int isBoundedToActivity = 22;
		public static final int notBoundedToActivity = 23;
		public static final int wifiOnly = 24;
		public static final int noWifiOnly = 25;
		public static final int dataConnectionAvailable = 26;
		public static final int dataConnectionNotAvailable = 27;
		public static final int cancelUpload = 28;
	}

	protected void sendMessage(int msg) {
		if(serviceHandler == null) {
			if(handlerThread != null){
				Looper handlerThreadLooper = handlerThread.getLooper();
				if (handlerThreadLooper != null)
					serviceHandler = new ServiceHandler(handlerThreadLooper);
			}
		}
		if(serviceHandler != null)
			serviceHandler.sendEmptyMessage(msg);
		else {
			serviceState = ServiceState.Exit;
			stopService();
		}
	}

	public class ServiceHandler extends Handler {

		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ServiceMsg.startProcessEvents: {
				if (serviceState == ServiceState.Init) {
					Log.i("ServiceHandler: startProcessEvents");
					serviceState = ServiceState.ProcessEvent;
					this.post(processEvent);
				} else
					unsuportedMsg(msg);
				break;
			}
			case ServiceMsg.successProcessEvents: {
				if (serviceState == ServiceState.ProcessEvent) {
					Log.i("ServiceHandler: successProcessEvents");
					serviceState = ServiceState.WaitForEventToUpload;
					this.post(isThereEventToUpload);
				} else
					unsuportedMsg(msg);
				break;
			}
			case ServiceMsg.failProcessEvents: {
				if (serviceState == ServiceState.ProcessEvent) {
					Log.i("ServiceHandler: failProcessEvents");
					sendMsgToActivity("Report fail");
					serviceState = ServiceState.Exit;
					stopService();
				} else
					unsuportedMsg(msg);
				break;
			}
			case ServiceMsg.eventToUpload: {
				if (serviceState == ServiceState.WaitForEventToUpload) {
					Log.i("ServiceHandler: eventToUpload");
					int uptimeNumber = msg.arg1;
					int crashNumber = msg.arg2;
					if ((crashNumber == 0) && (uptimeNumber != 0))
						sendMsgToActivity("There is Uptime event to report");
					else if ((crashNumber == 1) && (uptimeNumber == 0))
						sendMsgToActivity("There is 1 Crash event to report");
					else if ((crashNumber == 1) && (uptimeNumber != 0))
						sendMsgToActivity("There are Uptime and 1 Crash events to report");
					else if ((crashNumber > 1) && (uptimeNumber == 0))
						sendMsgToActivity("There are "+crashNumber+" Crash events to report");
					else if ((crashNumber > 1) && (uptimeNumber != 0))
						sendMsgToActivity("There are Uptime and "+crashNumber+" Crash events to report");
					else
						sendMsgToActivity("There are events to report");
					serviceState = ServiceState.IsBoundedToActivity;
					this.post(isServiceBoundedToActivity);
				} else
					unsuportedMsg(msg);
				break;
			}
			case ServiceMsg.noEventToUpload: {
				if (serviceState == ServiceState.WaitForEventToUpload) {
					Log.i("ServiceHandler: noEventToUpload");
					sendMsgToActivity("There is no event to upload");
					serviceState = ServiceState.Exit;
					stopService();
				} else
					unsuportedMsg(msg);
				break;
			}
			case ServiceMsg.uploadDisabled: {
				if (serviceState == ServiceState.WaitUploadState || serviceState == ServiceState.WaitForUploadResponse) {
					Log.i("ServiceHandler: uploadDisabled");
					sendMsgToActivity("Report cancelled");
					serviceState = ServiceState.Exit;
					stopService();
				} else
					unsuportedMsg(msg);
				break;
			}
			case ServiceMsg.uploadReported: {
				if (serviceState == ServiceState.WaitUploadState) {
					Log.i("ServiceHandler: uploadReported");
					serviceState = ServiceState.Exit;
					final int time = checkDateToRetry();
					sendMsgToActivity("Report is postponed in "+ time +" minutes");
					stopService();
				} else if (serviceState == ServiceState.WaitForUploadResponse) {
					Log.i("ServiceHandler: uploadReported");
					final int time = setDateToRetry();
					sendMsgToActivity("Report is postponed in " + time + " minutes");
					serviceState = ServiceState.Exit;
					stopService();
				} else
					unsuportedMsg(msg);
				break;
			}
			case ServiceMsg.askForUpload: {
				if (serviceState == ServiceState.WaitUploadState) {
					Log.i("ServiceHandler: askForUpload");
					serviceState = ServiceState.Exit;
					notifyEventToUpload();
					stopService();
				} else
					unsuportedMsg(msg);
				break;
			}
			case ServiceMsg.uploadImmadiately: {
				if (serviceState == ServiceState.WaitForUploadResponse) {
					Log.i("ServiceHandler: uploadImmadiately");
					if (app.isWifiOnly()) {
						serviceState = ServiceState.WaitWifiState;
						this.post(getWifiState);
					} else {
						uploadProgressStart();
						sendMsgToActivity("Uploading report");
						serviceState = ServiceState.UploadEvent;
						this.postDelayed(uploadEvent, 200);
					}
				} else if (serviceState == ServiceState.WaitUploadState) {
					Log.i("ServiceHandler: uploadImmadiately");
					if (app.isWifiOnly()) {
						serviceState = ServiceState.WaitWifiState;
						this.post(getWifiState);
					} else {
						serviceState = ServiceState.WaitForDataConnectionAvailableAuto;
						this.post(isDataConnectionAvailable);
					}
				} else
					unsuportedMsg(msg);
				break;
			}
			case ServiceMsg.internalwifiNotAvailable: {
				if (serviceState == ServiceState.WaitForInternalWifiAvailable) {
					Log.i("ServiceHandler: internalwifiNotAvailable");
					sendMsgToActivity("Report fail : Internal wifi not available");
					final int time = checkDateToRetry();
					sendMsgToActivity("Report is postponed in "+ time +" minutes");
					sendMsgToActivity("Otherwise, exit and check again the available events");
					serviceState = ServiceState.Exit;
					stopService();
				} else
					unsuportedMsg(msg);
				break;
			}
			case ServiceMsg.internalwifiAvailable: {
				if (serviceState == ServiceState.WaitForInternalWifiAvailable) {
					Log.i("ServiceHandler: internalwifiAvailable");
					serviceState = ServiceState.WaitForUploadResponse;
					this.post(askForUpload);
				} else
					unsuportedMsg(msg);
				break;
			}
			case ServiceMsg.wifiConnectedInternal: {
				if (serviceState == ServiceState.Connecting || serviceState == ServiceState.WaitWifiState) {
					Log.i("ServiceHandler: wifiConnected");
					uploadProgressStart();
					sendMsgToActivity("Uploading report");
					serviceState = ServiceState.UploadEvent;
					this.postDelayed(uploadEvent, 200);
				} else
					unsuportedMsg(msg);
				break;
			}
			case ServiceMsg.wifiNotConnected: {
				if (serviceState == ServiceState.WaitWifiState) {
					Log.i("ServiceHandler: wifiNotConnected");
					serviceState = ServiceState.Connecting;
					this.post(connectToNetwork);
				} else
					unsuportedMsg(msg);
				break;
			}
			case ServiceMsg.wifiConnectTimeOut: {
				if (serviceState == ServiceState.Connecting) {
					Log.i("ServiceHandler: wifiConnectTimeOut");
					sendMsgToActivity("Report fail : Wifi connection time out");
					serviceState = ServiceState.Disconnecting;
					this.post(disconnectToNetwork);
				} else
					unsuportedMsg(msg);
				break;
			}
			case ServiceMsg.wifiDisconnected: {
				if (serviceState == ServiceState.Disconnecting) {
					Log.i("ServiceHandler: wifiDisconnected");
					serviceState = ServiceState.Exit;
					stopService();
				} else
					unsuportedMsg(msg);
				break;
			}
			case ServiceMsg.uploadOK: {
				if (serviceState == ServiceState.UploadEvent) {
					Log.i("ServiceHandler: uploadOK");
					sendMsgToActivity("Uploading report successful");
					removeAlarmIfAny();
					if (app.isWifiOnly()) {
						serviceState = ServiceState.Disconnecting;
						this.post(disconnectToNetwork);
					} else {
						serviceState = ServiceState.Exit;
						stopService();
					}
				} else
					unsuportedMsg(msg);
				break;
			}
			case ServiceMsg.uploadFailFromConnection: {
				if (serviceState == ServiceState.UploadEvent) {
					Log.i("ServiceHandler: uploadFailFromConnection");
					sendMsgToActivity("Report fail : Server connection error");
					ApplicationPreferences prefs = new ApplicationPreferences(getApplicationContext());
					String serverAddress = prefs.getServerAddress();
					int serverPort = prefs.getServerPort();
					sendMsgToActivity("Please check "+ serverAddress +" is reachable on port " + serverPort );
					final int time = checkDateToRetry();
					sendMsgToActivity("Report is postponed in "+ time +" minutes");
					sendMsgToActivity("Otherwise, exit and check again the available events");
					if (app.isWifiOnly()) {
						serviceState = ServiceState.Disconnecting;
						this.post(disconnectToNetwork);
					} else {
						serviceState = ServiceState.Exit;
						stopService();
					}
				} else
					unsuportedMsg(msg);
				break;
			}
			case ServiceMsg.uploadFailFromSQL: {
				if (serviceState == ServiceState.UploadEvent) {
					Log.i("ServiceHandler: uploadFailFromSQL");
					sendMsgToActivity("Report fail : error while accessing SQL database");
					final int time = checkDateToRetry();
					sendMsgToActivity("Report is postponed in "+ time +" minutes");
					sendMsgToActivity("Otherwise, exit and check again the available events");
					if (app.isWifiOnly()) {
						serviceState = ServiceState.Disconnecting;
						this.post(disconnectToNetwork);
					} else {
						serviceState = ServiceState.Exit;
						stopService();
					}
				} else
					unsuportedMsg(msg);
				break;
			}
			case ServiceMsg.isBoundedToActivity: {
				if (serviceState == ServiceState.IsBoundedToActivity) {
					Log.i("ServiceHandler: isBoundedToActivity");
					serviceState = ServiceState.WaitForUploadWifiOnly;
					this.post(isUploadWifiOnly);
				} else
					unsuportedMsg(msg);
				break;
			}
			case ServiceMsg.notBoundedToActivity: {
				if (serviceState == ServiceState.IsBoundedToActivity) {
					Log.i("ServiceHandler: notBoundedToActivity");
					serviceState = ServiceState.WaitUploadState;
					this.post(getUploadState);
				} else
					unsuportedMsg(msg);
				break;
			}
			case ServiceMsg.wifiOnly: {
				if (serviceState == ServiceState.WaitForUploadWifiOnly) {
					Log.i("ServiceHandler: wifiOnly");
					serviceState = ServiceState.WaitForInternalWifiAvailable;
					this.post(isInternalWifiAvailable);
				} else
					unsuportedMsg(msg);
				break;
			}
			case ServiceMsg.noWifiOnly: {
				if (serviceState == ServiceState.WaitForUploadWifiOnly) {
					Log.i("ServiceHandler: noWifiOnly");
					serviceState = ServiceState.WaitForDataConnectionAvailableUser;
					this.post(isDataConnectionAvailable);
				} else
					unsuportedMsg(msg);
				break;
			}
			case ServiceMsg.dataConnectionAvailable: {
				if (serviceState == ServiceState.WaitForDataConnectionAvailableUser) {
					Log.i("ServiceHandler: dataConnectionAvailable");
					serviceState = ServiceState.WaitForUploadResponse;
					this.post(askForUpload);
				} else if (serviceState == ServiceState.WaitForDataConnectionAvailableAuto) {
					Log.i("ServiceHandler: dataConnectionAvailable");
					uploadProgressStart();
					sendMsgToActivity("Uploading report");
					serviceState = ServiceState.UploadEvent;
					this.postDelayed(uploadEvent, 200);
				} else
					unsuportedMsg(msg);
				break;
			}
			case ServiceMsg.dataConnectionNotAvailable: {
				if (!app.isWifiOnly() &&
						((serviceState == ServiceState.WaitForDataConnectionAvailableUser) ||
								(serviceState == ServiceState.WaitForUploadResponse) ||
								(serviceState == ServiceState.UploadEvent) ||
								(serviceState == ServiceState.WaitForDataConnectionAvailableAuto))) {
					Log.i("ServiceHandler: dataConnectionNotAvailable");
					sendMsgToActivity("Report fail : Internet is not available");
					serviceState = ServiceState.Exit;
					registerNetworkStateReceiver();
					stopService();
				} else
					unsuportedMsg(msg);
				break;
			}
			case ServiceMsg.cancelUpload: {
				if (serviceState == ServiceState.UploadEvent) {
					Log.i("ServiceHandler: cancelUpload");
					sendMsgToActivity("Report canceled");
					if (app.isWifiOnly()) {
						serviceState = ServiceState.Disconnecting;
						this.post(disconnectToNetwork);
					} else {
						serviceState = ServiceState.Exit;
						stopService();
					}
				} else
					unsuportedMsg(msg);
				break;
			}
			case ServiceMsg.killService: {
				if (serviceState != ServiceState.Exit) {
					Log.i("ServiceHandler: killService");
					sendMsgToActivity("Report fail : Aborted");
					serviceState = ServiceState.Exit;
					this.post(disconnectToNetwork);
				} else
					unsuportedMsg(msg);
				break;
			}
			default: {
				Log.w("ServiceHandler: Unsuported msg: " + msg.what + ":" + serviceState);
				break;
			}
			}

		}

		private void unsuportedMsg(Message msg) {
			Log.w("ServiceHandler: Unsuported msg: " + msg.what + ":" + serviceState);
			serviceState = ServiceState.Exit;
			stopService();
		}

	}

	public class LocalBinder extends Binder {
		CrashReportService getService() {
			return CrashReportService.this;
		}
	}

	public CrashReport getApp() {
		return app;
	};
}
