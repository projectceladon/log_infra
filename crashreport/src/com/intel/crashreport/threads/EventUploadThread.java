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
import com.intel.crashreport.R;
import com.intel.crashreport.specific.Build;
import com.intel.crashreport.database.EventDB;
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
	private int wifiLogSize;
	private Thread runningThread;
	private Cursor mCursor;

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
		// Maximum crashlogs size to upload over 3G
		this.wifiLogSize = context.getResources().getInteger(R.integer.wifi_log_size)
			 * 1024 * 1024;
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
			serviceHandler.sendEmptyMessage(ServiceMsg.cancelUpload);
			return;
		}

		if(!this.setUpConnection()) {
			Log.e(
					EventUploadThread.class.getSimpleName() +
					":uploadEvent : no connection could be set up.");
			serviceHandler.sendEmptyMessage(ServiceMsg.uploadFailFromConnection);
			return;
		}

		CrashReport app = (CrashReport) this.context;
		Build myBuild = app.getMyBuild();
		try {
			eventDb.open();
			this.connector.setupServerConnection();
			//upload Events
			do {
				crService.sendEvents(eventDb, this.connector, myBuild);
				try {
					if (eventDb.isThereEventToUpload()) {
						crService.updateEventsSummary(eventDb);
					}
					else {
						break;
					}
				} catch (SQLException e) {
					/* In case of Db access error, skip and go to events logs uploading process*/
					Log.w(EventUploadThread.class.getSimpleName()+":uploadEvent : Can't check if there is event to upload : Fail to access DB", e);
					break;
				}
			} while(true);
			//upload logs
			app.setNeedToUpload(false);
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
		if (connector == null)
			Log.w(EventUploadThread.class.getSimpleName() +
					": close connection exception. null connector");
		else {
			try {
				this.connector.closeServerConnection();
			} catch (IOException e) {
				Log.w(EventUploadThread.class.getSimpleName() +
					": close connection exception", e);
			}
		}

		//finally, clean cursor if required
		if (mCursor != null) {
			if(!mCursor.isClosed()) {
				mCursor.close();
			}
		}
	}

	private void doCrashLogsUpload() throws InterruptedException, ProtocolException {
		String[] crashTypes = prefs.getCrashLogsUploadTypes();
		CrashReport app = (CrashReport) this.context;
		mCursor = eventDb.fetchNotUploadedLogs(crashTypes);
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

		if ((mCursor != null) && (mCursor.getCount() != 0)) {
			boolean wifiAvailable = this.connector.getWifiConnectionAvailability();
			boolean requiresWifi = prefs.isWifiOnlyForEventData();
			if (requiresWifi && !wifiAvailable) {
				this.notificationManager.notifyEventDataWifiOnly(mCursor.getCount());
			} else {
				this.notificationManager.notifyUploadingLogs(
						mCursor.getCount(),
						crashNumber);
				boolean processNext = true;
				while (!mCursor.isAfterLast() && processNext) {
					if (theRunningThread.isInterrupted())
						throw new InterruptedException();
					processNext = this.processNextEvent(myBuild, theRunningThread);
				}
				this.notificationManager.cancelNotifUploadingLogs();
			}
			//should be done at the end of check upload process
			if(eventDb.isThereLogToUploadWithWifi(crashTypes) && !wifiAvailable) {
				this.needsWifi = true;
				this.notificationManager.notifyConnectWifiOrMpta();
			}
			if (mCursor != null)
				mCursor.close();
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
			if (data.exists()) {
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

	private boolean processNextEvent(Build myBuild, Thread theRunningThread)
			throws InterruptedException, ProtocolException {
		Event event = new Event(eventDb.fillEventFromCursor(mCursor));
		if (PhoneInspector.getInstance(this.context).isUploadableLog(event.getEventId())){
			File crashLogs = CrashLogs.getCrashLogsFile(
					context,
					event.getCrashDir(),
					event.getEventId());
			if (crashLogs != null) {
				boolean wifiAvailable = this.connector.getWifiConnectionAvailability();
				boolean logsAreTooBig = crashLogs.length() >= wifiLogSize;
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
		mCursor.moveToNext();
		this.newLogsToUpload = false;
		if(eventDb.isThereEventToUpload()) {
			mCursor.close();
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
		if(this.newLogsToUpload || mCursor.isAfterLast()) {
			this.needsWifi = false;
			if(!mCursor.isClosed())
				mCursor.close();
			String[] crashTypes = prefs.getCrashLogsUploadTypes();
			if(eventDb.isThereEventToUpload(crashTypes)) {
				mCursor = eventDb.fetchNotUploadedLogs(crashTypes);
				if ((mCursor != null) && (mCursor.getCount() != 0)) {
					int crashNumber = eventDb.getNewCrashNumber();
					boolean wifiAvailable = this.connector.getWifiConnectionAvailability();
					boolean canUploadWithoutWifi = eventDb.isThereLogToUploadWithoutWifi(crashTypes);
					this.notificationManager.notifyUploadingLogs(
							mCursor.getCount(),
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
