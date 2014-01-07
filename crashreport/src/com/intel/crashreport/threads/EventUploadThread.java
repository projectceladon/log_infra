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
 * Author: Adrien Sebbane <adrienx.sebbane@intel.com>
 */

package com.intel.crashreport.threads;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ProtocolException;
import java.net.UnknownHostException;
import java.util.TimeZone;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import com.intel.crashreport.ApplicationPreferences;
import com.intel.crashreport.Connector;
import com.intel.crashreport.CrashLogs;
import com.intel.crashreport.CrashReport;
import com.intel.crashreport.CrashReportService;
import com.intel.crashreport.CrashReportService.ServiceHandler;
import com.intel.crashreport.CrashReportService.ServiceMsg;
import com.intel.crashreport.Log;
import com.intel.crashreport.NotificationMgr;
import com.intel.crashreport.specific.Build;
import com.intel.crashreport.specific.EventDB;
import com.intel.crashreport.specific.Event;
import com.intel.crashreport.specific.PhoneInspector;
import com.intel.crashtoolserver.bean.FileInfo;
import com.intel.phonedoctor.Constants;

public class EventUploadThread implements Runnable {

	private final ApplicationPreferences prefs;
	private final Context context;
	private final ServiceHandler serviceHandler;
	private final EventDB eventDb;
	private final NotificationMgr notificationManager;
	private final Connector connector;
	private final CrashReportService crService;

	private boolean needsWifi;
	private boolean newLogsToUpload;
	private Thread runningThread;

	public EventUploadThread(
			Context context,
			ApplicationPreferences preferences,
			ServiceHandler serviceHandler,
			EventDB eventDb,
			CrashReportService aCrService) {
		this.context = context;
		this.prefs = preferences;
		this.serviceHandler = serviceHandler;
		this.eventDb = eventDb;
		this.needsWifi = false;
		this.notificationManager = new NotificationMgr(context);
		this.newLogsToUpload = false;
		this.crService = aCrService;
		// We assume that connector has already been setup
		this.connector = new Connector(
				this.context,
				this.serviceHandler);
	}

	public boolean needsWifi() {
		return this.needsWifi;
	}

	public void setRunningThread(Thread thread) {
		this.runningThread = thread;
	}

	public Thread getRunningThread() {
		return this.runningThread;
	}

	private boolean setUpConnection() {
		try {
			this.connector.setupServerConnection();
			return true;
		} catch(UnknownHostException e) {
			Log.e(
					EventUploadThread.class.getSimpleName() +
					":uploadEvent:UnknownHostException", e);
		} catch(IOException e) {
			Log.e(
					EventUploadThread.class.getSimpleName() +
					":uploadEvent:IOException", e);
		}
		return false;
	}

	public void run() {
		Thread theRunningThread = this.getRunningThread();
		if(null == theRunningThread) {
			Log.e(
					EventUploadThread.class.getSimpleName() +
					":uploadEvent : no running Thread provided.");
			return;
		}

		if(!this.setUpConnection()) {
			Log.e(
					EventUploadThread.class.getSimpleName() +
					":uploadEvent : no connection could be set up.");
			return;
		}

		Cursor cursor;
		CrashReport app = (CrashReport) this.context;
		Build myBuild = app.getMyBuild();
		boolean toContinue = true;
		try {
			eventDb.open();
			this.connector.setupServerConnection();
			int crashNumber = eventDb.getNewCrashNumber();
			//upload Events
			do {
				crService.sendEvents(eventDb, this.connector, myBuild);
				try {
					if ((toContinue = eventDb.isThereEventToUpload()) == true) {
						crService.updateEventsSummary(eventDb);
						crashNumber += eventDb.getNewCrashNumber();
					}
				} catch (SQLException e) {
					/* In case of Db access error, skip and go to events logs uploading process*/
					Log.w(EventUploadThread.class.getSimpleName()+":uploadEvent : Can't check if there is event to upload : Fail to access DB", e);
					toContinue = false;
				}
			}while(toContinue);
			//upload logs
			app.setNeedToUpload(false);
			String crashTypes[] = null;
			if (prefs.isCrashLogsUploadEnable()) {
				this.doCrashLogsUpload();
			}
			else {
				Log.i(EventUploadThread.class.getSimpleName()+":uploadEvent : logs upload disabled");
			}
			serviceHandler.sendEmptyMessage(ServiceMsg.uploadOK);
		} catch (InterruptedException e) {
			Log.i(EventUploadThread.class.getSimpleName()+":uploadEvent : upload interrupted");
			serviceHandler.sendEmptyMessage(ServiceMsg.cancelUpload);
		} catch (SQLException e) {
			Log.w(EventUploadThread.class.getSimpleName()+":uploadEvent : Fail to access DB", e);
			serviceHandler.sendEmptyMessage(ServiceMsg.uploadFailFromSQL);
		} catch (ProtocolException e) {
			Log.w(EventUploadThread.class.getSimpleName()+":uploadEvent:ProtocolException", e);
			serviceHandler.sendEmptyMessage(ServiceMsg.uploadFailFromConnection);
		} catch (UnknownHostException e) {
			Log.w(EventUploadThread.class.getSimpleName()+":uploadEvent:UnknownHostException", e);
			serviceHandler.sendEmptyMessage(ServiceMsg.uploadFailFromConnection);
		} catch (InterruptedIOException e) {
			Log.w(EventUploadThread.class.getSimpleName()+":uploadEvent:InterruptedIOException", e);
			serviceHandler.sendEmptyMessage(ServiceMsg.uploadFailFromConnection);
		} catch (IOException e) {
			Log.w(EventUploadThread.class.getSimpleName()+":uploadEvent:IOException", e);
			serviceHandler.sendEmptyMessage(ServiceMsg.uploadFailFromConnection);
		} finally {
			this.closeServerConnection();
		}
	}

	private void closeServerConnection() {
		try {
			this.connector.closeServerConnection();
		} catch (IOException e) {
			Log.w(
					EventUploadThread.class.getSimpleName() +
					": close connection exception", e);
		} catch (NullPointerException e) {
			Log.w(
					EventUploadThread.class.getSimpleName() +
					": close connection exception", e);
		}
	}

	private void doCrashLogsUpload() throws InterruptedException, ProtocolException {
		String[] crashTypes = prefs.getCrashLogsUploadTypes();
		CrashReport app = (CrashReport) this.context;
		Cursor cursor = eventDb.fetchNotUploadedLogs(crashTypes);
		Event event;
		File crashLogs;

		Build myBuild = app.getMyBuild();
		FileInfo fileInfo;
		Thread theRunningThread = this.getRunningThread();
		if(null == theRunningThread) {
			Log.e(
					EventUploadThread.class.getSimpleName() +
					":uploadEvent : no running Thread provided.");
			return;
		}
		int crashNumber = this.eventDb.getNewCrashNumber();

		if ((cursor != null) && (cursor.getCount() != 0)) {
			boolean wifiAvailable = this.connector.getWifiConnectionAvailability();
			boolean requiresWifi = prefs.isWifiOnlyForEventData();
			if (requiresWifi && !wifiAvailable) {
				this.notificationManager.notifyEventDataWifiOnly(cursor.getCount());
			} else {
				this.notificationManager.notifyUploadingLogs(
						cursor.getCount(),
						crashNumber);
				boolean processNext = true;
				while (!cursor.isAfterLast() && processNext) {
					if (theRunningThread.isInterrupted())
						throw new InterruptedException();
					processNext = this.processNextEvent(cursor, myBuild, theRunningThread);
				}
				this.notificationManager.cancelNotifUploadingLogs();
			}
			//should be done at the end of check upload process
			if(eventDb.isThereLogToUploadWithWifi(crashTypes) && !wifiAvailable) {
				this.needsWifi = true;
				this.notificationManager.notifyConnectWifiOrMpta();
			}
			if (cursor != null)
				cursor.close();
			Log.i(
					EventUploadThread.class.getSimpleName() +
					":uploadEvent : Upload files finished");
		}
	}

	private void updateEventData(EventDB eventDB, Event event, File crashLogs) {
		// Update the event in database
		eventDb.updateEventLogToUploaded(event.getEventId());
		if(crashLogs != null) {
			Log.d(
					EventUploadThread.class.getSimpleName() +
					":uploadEvent : Success upload of " +
					crashLogs.getAbsolutePath());
			// Delete the crashlogs
			crashLogs.delete();
		}
		// We have some additional operations to run in case of
		// APLOG or BZ events.
		String eventName = event.getEventName();
		boolean isAplog = "APLOG".equals(eventName);
		boolean isBz = "BZ".equals(eventName);
		if (isAplog || isBz ) {
			// Update the UI
			if (event.getEventName().equals("APLOG")) {
				crService.updateProgressBar(0);
				crService.hideProgressBar();
			}
			// Delete all file in crash directory
			File data = new File(event.getCrashDir());
			if (data != null && data.exists()) {
				if (data.isDirectory()) {
					File[] files = data.listFiles();
					if(files != null) {
						for(File file: files) {
							file.delete();
						}
					}
				}
				data.delete();
			}
		}
	}

	private boolean processNextEvent(Cursor cursor, Build myBuild, Thread theRunningThread)
			throws InterruptedException, ProtocolException {
		Event event = eventDb.fillEventFromCursor(cursor);
		if (PhoneInspector.getInstance(this.context).isUploadableLog(event.getEventId())){
			File crashLogs = CrashLogs.getCrashLogsFile(
					context,
					event.getCrashDir(),
					event.getEventId());
			if (crashLogs != null) {
				boolean wifiAvailable = this.connector.getWifiConnectionAvailability();
				boolean logsAreTooBig = crashLogs.length() >= Constants.WIFI_LOGS_SIZE;
				if(logsAreTooBig && !wifiAvailable) {
					this.needsWifi = true;
				} else {
					CrashReportService.DAY_DF.setTimeZone(TimeZone.getTimeZone("GMT"));
					String dayDate = CrashReportService.DAY_DF.format(event.getDate());
					FileInfo fileInfo = new FileInfo(
							crashLogs.getName(),
							crashLogs.getAbsolutePath(),
							crashLogs.length(),
							dayDate,
							event.getEventId());
					Log.i(
							EventUploadThread.class.getSimpleName() +
							":uploadEvent : Upload crashlog of "+event);
					if (event.getEventName().equals("APLOG")) {
						crService.showProgressBar();
						crService.updateProgressBar(0);
					}

					if (this.connector.sendLogsFile(fileInfo, theRunningThread)) {
						this.updateEventData(eventDb, event, crashLogs);
					} else {
						if (event.getEventName().equals("APLOG")) {
							crService.updateProgressBar(0);
							crService.hideProgressBar();
						}
						Log.w(
								EventUploadThread.class.getSimpleName() +
								":uploadEvent : Fail upload of " +
								crashLogs.getAbsolutePath());
						throw new ProtocolException();
					}
				}
			} else {
				Log.d(
						EventUploadThread.class.getSimpleName() +
						":uploadEvent : No crashlog to upload for " +
						event);
				PhoneInspector.getInstance(this.context).addEventLogUploadFailure(event.getEventId());
			}
		} else {
			Log.w(EventUploadThread.class.getSimpleName()+":uploadEvent : too much log failure for "+event);
		}
		cursor.moveToNext();
		this.newLogsToUpload = false;
		if(eventDb.isThereEventToUpload()) {
			cursor.close();
			crService.updateEventsSummary(eventDb);
			crService.sendEvents(
					eventDb,
					this.connector,
					myBuild);
			this.newLogsToUpload = true;
		}
		if(crService.getApp().getNeedToUpload()) {
			crService.getApp().setNeedToUpload(false);
			this.newLogsToUpload = true;
		}
		if(this.newLogsToUpload || cursor.isAfterLast()) {
			this.needsWifi = false;
			if(!cursor.isClosed())
				cursor.close();
			String[] crashTypes = prefs.getCrashLogsUploadTypes();
			if(eventDb.isThereEventToUpload(crashTypes)) {
				cursor = eventDb.fetchNotUploadedLogs(crashTypes);
				if ((cursor != null) && (cursor.getCount() != 0)) {
					int crashNumber = eventDb.getNewCrashNumber();
					boolean wifiAvailable = this.connector.getWifiConnectionAvailability();
					boolean canUploadWithoutWifi = eventDb.isThereLogToUploadWithoutWifi(crashTypes);
					this.notificationManager.notifyUploadingLogs(
							cursor.getCount(),
							crashNumber);
					if(!canUploadWithoutWifi && !wifiAvailable) {
						//need to stop try to upload something
						return false;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}


};
