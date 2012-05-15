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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ProtocolException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.app.NotificationManager;
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
import android.preference.PreferenceManager;
import android.os.SystemProperties;

import com.intel.crashreport.StartServiceActivity.ServiceToActivityMsg;
import com.intel.crashtoolserver.bean.FileInfo;

public class CrashReportService extends Service {

	private CrashReport app;
	private HandlerThread handlerThread;
	private Thread runThread;
	private ServiceHandler serviceHandler;
	private ServiceState serviceState;
	private final IBinder mBinder = new LocalBinder();
	private Logger logger = new Logger();
	private static final DateFormat DAY_DF = new SimpleDateFormat("yyyy-MM-dd");

	public void onCreate() {
		super.onCreate();
		app = (CrashReport)getApplicationContext();
		Log.d("Service: created");
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("Service: onStartCommand");
		if (app.isServiceStarted()) {
			Log.d("Service: Already started: stop");
			stopSelf();
		} else {
			Log.d("Service: Not already started");
			handlerThread = new HandlerThread("CrashReportService_Thread");
			handlerThread.start();
			while(!handlerThread.isAlive()) {};
			Looper handlerThreadLooper = handlerThread.getLooper();
			if (handlerThreadLooper != null) {
				serviceHandler = new ServiceHandler(handlerThreadLooper);
				app.setServiceStarted(true);
				this.serviceState = ServiceState.Init;
				logger.clearLog();
				Build myBuild = new Build();
				myBuild.fillBuildWithSystem();
				app.setMyBuild(myBuild);
				if (!intent.getBooleanExtra("fromActivity", false))
					this.serviceHandler.sendEmptyMessageDelayed(ServiceMsg.startProcessEvents, 100);
			} else {
				stopService();
			}
		}
		return START_NOT_STICKY;
	}

	public IBinder onBind(Intent arg0) {
		Log.d("Service: onBind");
		return mBinder;
	}

	public boolean onUnbind(Intent intent) {
		Log.d("Service: onUnbind");
		return false;
	}

	private void stopService() {
		Log.d("Stop Service");
		app.setServiceStarted(false);
		handlerThread.quit();
		stopSelf();
	}

	public Boolean isServiceUploading() {
		return (serviceState == ServiceState.UploadEvent);
	}

	public void cancelDownload() {
		if (serviceState == ServiceState.UploadEvent) {
			if ((runThread != null) && runThread.isAlive())
				runThread.interrupt();
		}
	}

	private Runnable processEvent = new Runnable(){
		HistoryEventFile histFile;
		String histEventLine;
		EventDB db;
		String myBuild;
		ApplicationPreferences prefs;
		Cursor cursor;
		Event event;
		NotificationMgr nMgr;

		public void run() {
			db = new EventDB(getApplicationContext());
			myBuild = ((CrashReport) getApplicationContext()).getMyBuild().toString();
			nMgr = new NotificationMgr(getApplicationContext());

			try {
				db.open();
				histFile = new HistoryEventFile();
				prefs = new ApplicationPreferences(getApplicationContext());

				while (histFile.hasNext()) {
					histEventLine = histFile.getNextEvent();
					if (histEventLine.length() != 0) {
						HistoryEvent histEvent = new HistoryEvent(histEventLine);
						if (histEvent.getEventId().replaceAll("0", "").length() != 0) {
							Event event = new Event(histEvent, myBuild);
							if (!db.isEventInDb(event.getEventId())) {
								long ret = db.addEvent(event);
								if (ret == -1)
									Log.w("Service: Event error when added to DB, " + event.toString());
								else
									Log.d("Service: Event successfully added to DB, " + event.toString());
							} else
								Log.d("Service: Event already in DB, " + event.toString());
						} else
							Log.d("Service: Event ignored ID:" + histEvent.getEventId());
					}
				}

				if (db.isThereEventToNotified()) {
					nMgr.notifyCriticalEvent(db.getCriticalEventsNumber());
				}

				db.close();
				serviceHandler.sendEmptyMessage(ServiceMsg.successProcessEvents);
			} catch (FileNotFoundException e) {
				Log.w("Service: history_event file not found");
				db.close();
				serviceHandler.sendEmptyMessage(ServiceMsg.failProcessEvents);
			} catch (SQLException e) {
				Log.w("Service: db Exception");
				serviceHandler.sendEmptyMessage(ServiceMsg.failProcessEvents);
			}
		}
	};

	private Runnable getUploadState = new Runnable() {
		public void run() {
			String uploadState = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("uploadStatePref", "askForUpload");
			if ( SystemProperties.get("persist.crashreport.disabled", "0").equals("1")) {
				Log.d("Service: Property persist.crashreport.disabled set to 1");
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
					if (db.isThereEventToUpload(crashTypes)) {
						Message msg = Message.obtain();
						msg.what = ServiceMsg.eventToUpload;
						msg.arg1 = db.getNewRebootNumber();
						msg.arg2 = db.getNewCrashNumber();
						serviceHandler.sendMessage(msg);
					} else
						serviceHandler.sendEmptyMessage(ServiceMsg.noEventToUpload);
				}
				db.close();
			} catch (SQLException e) {
				Log.w("Service:isThereEventToUpload : Fail to open DB");
				serviceHandler.sendEmptyMessage(ServiceMsg.noEventToUpload);
			}

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

	private Runnable isUploadWifiOnly = new Runnable() {
		public void run() {
			Boolean wifiOnly = PreferenceManager.getDefaultSharedPreferences(app).getBoolean("uploadWifiOnlyPref", false);
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
				if( SystemProperties.get("persist.crashreport.disabled", "0").equals("1") ) {
					sendMsgToActivity("Warning : Background Upload is disabled due to property persist.crashreport.disabled set to 1");
					Log.d("Service: Property persist.crashreport.disabled set to 1");
					serviceHandler.sendEmptyMessage(ServiceMsg.uploadDisabled);
				} else {
					Intent askForUploadIntent = new Intent(ServiceToActivityMsg.askForUpload);
					getApplicationContext().sendBroadcast(askForUploadIntent);
				}
			} else {
				Log.w("R:askForUpload: Activity not bounded to service");
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
			Log.w("Service:notifyEventToUpload : Fail to access DB", e);
		}
		db.close();
	}

	private void registerNetworkStateReceiver() {
		Log.d("CrashReportService: registerNetworkStateReceiver");
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
			getApplicationContext().sendBroadcast(uploadStartedIntent);
		}
	}

	private void uploadProgressStop() {
		if (app.isActivityBounded()) {
			Intent uploadFinishedIntent = new Intent(ServiceToActivityMsg.uploadFinished);
			getApplicationContext().sendBroadcast(uploadFinishedIntent);
		}
	}

	private Runnable uploadEvent = new Runnable(){
		Context context;
		PowerManager pm = null;
		PowerManager.WakeLock wakeLock = null;
		ApplicationPreferences prefs;
		NotificationMgr nMgr;
		EventDB db;
		Cursor cursor;
		Connector con;
		Event event;
		FileInfo fileInfo;
		File crashLogs;
		String dayDate;
		Build myBuild;

		public void run() {
			context = getApplicationContext();
			myBuild = ((CrashReport) context).getMyBuild();
			prefs = new ApplicationPreferences(context);
			db = new EventDB(context);
			con = new Connector(context, serviceHandler);
			pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CrashReport");
			wakeLock.acquire();

			Runnable uploadExec = new Runnable(){
				public void run() {
					try {
						db.open();
						con.setupServerConnection();

						//upload Events
						cursor = db.fetchNotUploadedEvents();
						if (cursor != null) {
							while (!cursor.isAfterLast()) {
								if (runThread.isInterrupted())
									throw new InterruptedException();
								event = db.fillEventFromCursor(cursor);
								com.intel.crashtoolserver.bean.Event sEvent = event.getEventForServer(myBuild);
								if (con.sendEvent(sEvent)) {
									db.updateEventToUploaded(event.getEventId());
									Log.d("Service:uploadEvent : Success upload of " + event);
									Log.i("Service:uploadEvent : Success upload of " + event);
									cursor.moveToNext();
								} else {
									Log.w("Service:uploadEvent : Fail upload of " + event);
									throw new ProtocolException();
								}
							}
							cursor.close();
						}

						//upload logs
						String crashTypes[] = null;
						if (prefs.isCrashLogsUploadEnable()) {
							crashTypes = prefs.getCrashLogsUploadTypes();
							cursor = db.fetchNotUploadedLogs(crashTypes);
							if ((cursor != null) && (cursor.getCount() != 0)) {
								nMgr = new NotificationMgr(context);
								nMgr.notifyUploadingLogs(cursor.getCount());
								while (!cursor.isAfterLast()) {
									if (runThread.isInterrupted())
										throw new InterruptedException();
									event = db.fillEventFromCursor(cursor);
									crashLogs = CrashLogs.getCrashLogsFile(context, event.getCrashDir(), event.getEventId());
									if (crashLogs != null) {
										dayDate = DAY_DF.format(event.getDate());
										fileInfo = new FileInfo(crashLogs.getName(), crashLogs.getAbsolutePath(), crashLogs.length(), dayDate, event.getEventId());
										Log.i("Service:uploadEvent : Upload of "+event);
										if (con.sendLogsFile(fileInfo, runThread)) {
											db.updateEventLogToUploaded(event.getEventId());
											Log.d("Service:uploadEvent : Success upload of " + crashLogs.getAbsolutePath());
											crashLogs.delete();
											cursor.moveToNext();
										} else {
											Log.w("Service:uploadEvent : Fail upload of " + crashLogs.getAbsolutePath());
											throw new ProtocolException();
										}
									} else
										cursor.moveToNext();
								}
								cursor.close();
								nMgr.cancelNotifUploadingLogs();
								Log.i("Service:uploadEvent : Upload files finished");
							}
						}
						serviceHandler.sendEmptyMessage(ServiceMsg.uploadOK);
					} catch (InterruptedException e) {
						Log.i("Service:uploadEvent : upload interrupted");
						serviceHandler.sendEmptyMessage(ServiceMsg.cancelUpload);
					} catch (SQLException e) {
						Log.w("Service:uploadEvent : Fail to access DB", e);
						serviceHandler.sendEmptyMessage(ServiceMsg.uploadFailFromSQL);
					} catch (ProtocolException e) {
						Log.w("Service:uploadEvent:ProtocolException", e);
						serviceHandler.sendEmptyMessage(ServiceMsg.uploadFailFromConnection);
					} catch (UnknownHostException e) {
						Log.w("Service:uploadEvent:UnknownHostException", e);
						serviceHandler.sendEmptyMessage(ServiceMsg.uploadFailFromConnection);
					} catch (InterruptedIOException e) {
						Log.w("Service:uploadEvent:InterruptedIOException", e);
						serviceHandler.sendEmptyMessage(ServiceMsg.uploadFailFromConnection);
					} catch (IOException e) {
						Log.w("Service:uploadEvent:IOException", e);
						serviceHandler.sendEmptyMessage(ServiceMsg.uploadFailFromConnection);
					}
				}
			};
			runThread = new Thread(uploadExec, "UploadThread");
			runThread.start();
			try {
				runThread.join();
			} catch (InterruptedException e) {
				serviceHandler.sendEmptyMessage(ServiceMsg.cancelUpload);
			}

			CrashLogs.deleteCrashLogsZipFiles(context);
			if (nMgr != null)
				nMgr.cancelNotifUploadingLogs();
			if (cursor != null)
				cursor.close();
			try {
				if (con != null)
					con.closeServerConnection();
			} catch (IOException e) {
				Log.w("Service: close connection exception", e);
			} catch (NullPointerException e) {
				Log.w("Service: close connection exception", e);
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
			getApplicationContext().sendBroadcast(updateLogIntent);
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

	protected class ServiceMsg {

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
		this.serviceHandler.sendEmptyMessage(msg);
	}

	private class ServiceHandler extends Handler {

		public ServiceHandler(Looper looper) {
			super(looper);
		}

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
					sendMsgToActivity("Report fail : Can't read local files");
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
                //Waiting time before stop the StartServiceActivity if the binder makes too much time to create CrashReportService
		//67 is for 2seconds fo waiting with a sleep of 30ms.(2000ms/30)
		private static final int waiting_time = 67;

		CrashReportService getService() {
			int counter = 0;
			while( CrashReportService.this == null && counter < waiting_time){
				try{
					Thread.sleep(30);
                                        counter++;
				}
				catch(InterruptedException e){
					Log.d("LocalBinder: getService: Interrupted Exception");
				}
			}
			return CrashReportService.this;
		}
	}


}
