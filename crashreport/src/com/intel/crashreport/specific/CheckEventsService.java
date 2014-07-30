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
 * Author: Charles-Edouard Vidoine <charles.edouardx.vidoine@intel.com>
 */

package com.intel.crashreport.specific;

import java.io.FileNotFoundException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.intel.crashreport.ApplicationPreferences;
import com.intel.crashreport.CrashReport;
import com.intel.crashreport.Log;
import com.intel.crashreport.Logger;
import com.intel.crashreport.NotificationMgr;
import com.intel.crashreport.bugzilla.BZFile;
import com.intel.phonedoctor.Constants;

public class CheckEventsService extends Service {

	private CrashReport app;
	private HandlerThread handlerThread;
	private CheckEventsServiceHandler serviceHandler;
	private CheckEventsServiceState serviceState;
	private final IBinder mBinder = new CheckEventsLocalBinder();
	private static final Intent crashReportStartServiceIntent = new Intent("com.intel.crashreport.CrashReportService");
	private Logger logger = new Logger();
	private static final String MODULE = "CheckEventsService";

	@Override
	public void onCreate() {
		super.onCreate();
		app = (CrashReport)getApplicationContext();
		Log.d(MODULE+": created");
		GcmEvent.INSTANCE.setContext(getApplicationContext());
		ParserContainer.INSTANCE.initDirector(getApplicationContext());
		//first try to register GCM TOKEN
		ApplicationPreferences privatePrefs = new ApplicationPreferences(getApplicationContext());
		if(privatePrefs.isGcmEnable())
			GcmEvent.INSTANCE.checkTokenGCM();
		Log.d(MODULE+": GCM init done");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(MODULE+": onStartCommand");
		if (app.isCheckEventsServiceStarted()) {
			Log.d(MODULE+": Service already running");
		} else {
			Log.d(MODULE+": Not already started");
			handlerThread = new HandlerThread("CheckEventsService_Thread");
			handlerThread.start();
			while(!handlerThread.isAlive()) {};
			Looper handlerThreadLooper = handlerThread.getLooper();
			if (handlerThreadLooper != null) {
				serviceHandler = new CheckEventsServiceHandler(handlerThreadLooper);
				app.setCheckEventsServiceStarted(true);
				this.serviceState = CheckEventsServiceState.Init;
				logger.clearLog();
				Build myBuild = new Build(getApplicationContext());
				myBuild.fillBuildWithSystem();
				app.setMyBuild(myBuild);
				this.serviceHandler.sendEmptyMessageDelayed(ServiceMsg.startProcessEvents, 100);
			} else {
				stopService();
			}
		}
		return START_NOT_STICKY;
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
		app.setCheckEventsServiceStarted(false);
		if(handlerThread != null) {
			handlerThread.quit();
		}
		if(app.getRequestListCount() > 0 && !app.isServiceRelaunched()) {
			app.setServiceRelaunched(true);
			long lDelay = (long) Constants.CRASH_POSTPONE_DELAY*1000;
			RelaunchServiceTask relaunchService = new RelaunchServiceTask(getApplicationContext(),lDelay);
			relaunchService.start();
		}
		stopSelf();
	}

	private Runnable processEvent = new Runnable(){

		public void run() {

			try {
				while(app.getRequestListCount() > 0) {
					app.emptyList();
					checkEvents(MODULE);
				}
				serviceHandler.sendEmptyMessage(ServiceMsg.successProcessEvents);
			} catch (FileNotFoundException e) {
				Log.w(MODULE+": history_event file not found");
				serviceHandler.sendEmptyMessage(ServiceMsg.failProcessEvents);
			} catch (SQLException e) {
				Log.w(MODULE+": db Exception");
				serviceHandler.sendEmptyMessage(ServiceMsg.failProcessEvents);
			}
		}
	};

	public Logger getLogger() {
		return logger;
	}

	private enum CheckEventsServiceState {
		Init, ProcessEvent, Exit;
	}

	protected class ServiceMsg {

		public static final int startProcessEvents = 0;
		public static final int successProcessEvents = 1;
		public static final int failProcessEvents = 2;
	}

	protected void sendMessage(int msg) {
		if(serviceHandler == null) {
			if(handlerThread != null){
				Looper handlerThreadLooper = handlerThread.getLooper();
				if (handlerThreadLooper != null)
					serviceHandler = new CheckEventsServiceHandler(handlerThreadLooper);
			}
		}
		if(serviceHandler != null)
			serviceHandler.sendEmptyMessage(msg);
		else {
			serviceState = CheckEventsServiceState.Exit;
			stopService();
		}
	}

	public void checkEvents(String from) throws FileNotFoundException,SQLException{
		HistoryEventFile histFile;
		String histEventLine;
		EventDB db;
		String myBuild;
		Event event;
		NotificationMgr nMgr;
		BlackLister blackLister = new BlackLister(getApplicationContext());
		boolean historyEventCorrupted = false;
		boolean result;
		String bootMode = "";
		Context context = getApplicationContext();


		db = new EventDB(getApplicationContext());
		myBuild = ((CrashReport) getApplicationContext()).getMyBuild().toString();
		nMgr = new NotificationMgr(getApplicationContext());
		PDStatus.INSTANCE.setContext(getApplicationContext());

		try {
			db.open();
			if(!app.isUserBuild())
				blackLister.setDb(db);
			histFile = new HistoryEventFile();
			histFile.open();
			while (histFile.hasNext()) {
				histEventLine = histFile.getNextEvent();
				if (histEventLine.length() != 0) {
					HistoryEvent histEvent = new HistoryEvent(histEventLine);
					historyEventCorrupted |= histEvent.isCorrupted();
					PDStatus.INSTANCE.setHistoryEventCorrupted(historyEventCorrupted);
					if ((histEvent.getEventId().replaceAll("0", "").length() != 0) && !histEvent.getEventName().contentEquals("DELETE")){
						try {
							if(app.isUserBuild())
								result = !db.isEventInDb(histEvent.getEventId());
							else
								result = !db.isEventInDb(histEvent.getEventId()) && !db.isEventInBlackList(histEvent.getEventId());
							if (result) {
								event = new Event(histEvent, myBuild, app.isUserBuild());

								if (!histEvent.getEventName().contentEquals("REBOOT"))
									event.setOsBootMode(bootMode);
								else
									bootMode = event.getOsBootMode();

								if(!app.isUserBuild() && blackLister.hasDb())
									blackLister.cleanRain(event.getDate());
								result = true;
								if(!app.isUserBuild() && blackLister.hasDb())
									result = !blackLister.blackList(event);
								if (result) {

									long ret = db.addEvent(event);
									if (ret == -1)
										Log.w(from+": Event error when added to DB, " + event.toString());
									else if (ret == -2)
										Log.w(from+": Event name " +histEvent.getEventName() + " unkown, addition in DB canceled");
									else if (ret == -3)
										Log.w(from+": Event " +event.toString() + " with wrong date, addition in DB canceled");
									else {
										if (event.getEventName().contentEquals("REBOOT")) {
											if (event.getType().contentEquals("SWUPDATE")){
												db.deleteEventsBeforeUpdate(event.getEventId());
											}else{
												db.updateEventsNotReadyBeforeREBOOT(event.getEventId());
											}
										}
										if (event.getEventName().equals("BZ")) {
											try {
												BZFile bzfile = new BZFile(event.getCrashDir());
												db.addBZ(event.getEventId(), bzfile, event.getDate());
												Log.d(from+": BZ added in DB, " + histEvent.getEventId());
											} catch (FileNotFoundException e) {
												Log.e("bzfile not found during history_event parsing");
											}
										}
										Log.d(from+": Event successfully added to DB, " + event.toString());
										if (!event.isDataReady()) {
											Long lDelay = (long) Constants.CRASH_POSTPONE_DELAY*1000;
											NotifyCrashTask notify = new NotifyCrashTask(event.getEventId(),getApplicationContext(),lDelay);
											notify.start();
										}
									}
								}
							} else {
								if (histEvent.getEventName().contentEquals("REBOOT")){
									event = new Event(histEvent, myBuild, app.isUserBuild());
									bootMode = event.getOsBootMode();
								}
								Log.d(from+": Event already in DB, " + histEvent.getEventId());
							}
						} catch (SQLException e) {
							Log.e(from+": Can't access database. Skip treatment of event " + histEvent.getEventId(), e);
						}
					} else
						Log.d(from+": Event ignored ID:" + histEvent.getEventId());
				}
			}



			ApplicationPreferences prefs = new ApplicationPreferences(context);
			boolean bNotifyAllCrashes = prefs.isNotificationForAllCrash();

			if (db.isThereEventToNotify(bNotifyAllCrashes)) {
				nMgr.notifyCriticalEvent(db.getCriticalEventsNumber(), db.getCrashToNotifyNumber());
			}
			if (db.isThereEventToUpload()){
				if(!app.isServiceStarted())
					context.startService(crashReportStartServiceIntent);
			}
			histFile.close();
			db.close();
		} catch (FileNotFoundException e) {
			db.close();
			throw e;
		} catch (SQLException e) {
			db.close();
			throw e;
		}

	}

	private class CheckEventsServiceHandler extends Handler {

		public CheckEventsServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ServiceMsg.startProcessEvents: {
				if (serviceState == CheckEventsServiceState.Init) {
					Log.i("CheckEventsServiceHandler: startProcessEvents");
					serviceState = CheckEventsServiceState.ProcessEvent;
					this.post(processEvent);
				} else
					unsuportedMsg(msg);
				break;
			}
			case ServiceMsg.successProcessEvents: {
				if (serviceState == CheckEventsServiceState.ProcessEvent) {
					Log.i("CheckEventsServiceHandler: successProcessEvents");
					serviceState = CheckEventsServiceState.Exit;
					stopService();
				} else
					unsuportedMsg(msg);
				break;
			}
			case ServiceMsg.failProcessEvents: {
				if (serviceState == CheckEventsServiceState.ProcessEvent) {
					Log.i("CheckEventsServiceHandler: failProcessEvents");
					serviceState = CheckEventsServiceState.Exit;
					stopService();
				} else
					unsuportedMsg(msg);
				break;
			}
			default: {
				Log.w("CheckEventsServiceHandler: Unsuported msg: " + msg.what + ":" + serviceState);
				break;
			}
			}

		}

		private void unsuportedMsg(Message msg) {
			Log.w("CheckEventsServiceHandler: Unsuported msg: " + msg.what + ":" + serviceState);
			serviceState = CheckEventsServiceState.Exit;
			stopService();
		}

	}

	public class CheckEventsLocalBinder extends Binder {
		//Waiting time before stop the StartServiceActivity if the binder makes too much time to create CrashReportService
		//67 is for 2seconds fo waiting with a sleep of 30ms.(2000ms/30)
		private static final int waiting_time = 67;

		CheckEventsService getService() {
			int counter = 0;
			while( CheckEventsService.this == null && counter < waiting_time){
				try{
					Thread.sleep(30);
					counter++;
				}
				catch(InterruptedException e){
					Log.d("CheckEventsLocalBinder: getService: Interrupted Exception");
				}
			}
			return CheckEventsService.this;
		}
	}

	public class NotifyCrashTask extends Thread{
		private String eventId;
		private Context context;
		private boolean isPresent;
		private long waitingTime;

		public NotifyCrashTask(String id,Context ctx, long delay){
			super();
			eventId = id;
			context = ctx;
			waitingTime = delay;

		}

		public void run() {
			try {
				sleep(waitingTime);
			} catch (InterruptedException e1) {
				Log.e("NotifyCrashTask:sleep failed");
			}
			EventDB db = new EventDB(context);
			isPresent = false;

			try {
				db.open();
				if (!db.eventDataAreReady(eventId)) {
					db.updateEventDataReady(eventId);
					isPresent = true;
				}
			}catch (SQLException e) {
				Log.w("NotifyCrashTask: Fail to access DB", e);
			}
			db.close();

			if (isPresent) {
				Intent intent = new Intent("com.intel.crashreport.intent.START_CRASHREPORT");
				context.sendBroadcast(intent);
			}

		}
	}


	private class RelaunchServiceTask extends Thread{
		private Context context;
		private long waitingTime;

		public RelaunchServiceTask(Context ctx, long delay){
			super();
			context = ctx;
			waitingTime = delay;
		}

		public void run() {
			try {
				sleep(waitingTime);
			} catch (InterruptedException e) {
				Log.e("RelaunchServiceTask:sleep failed");
			}
			Intent intent = new Intent("com.intel.crashreport.intent.RELAUNCH_SERVICE");
			context.sendBroadcast(intent);
		}
	}

}
