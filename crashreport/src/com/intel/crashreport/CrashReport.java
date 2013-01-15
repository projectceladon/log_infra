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

import java.io.FileNotFoundException;
import java.util.Timer;

import com.intel.crashreport.bugzilla.BZFile;
import com.intel.crashreport.bugzilla.ui.BugStorage;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.SQLException;
import android.preference.PreferenceManager;

public class CrashReport extends Application {

	private Boolean serviceStarted = false;
	private Boolean tryingToConnect = false;
	private Boolean activityBounded = false;
	private Boolean wifiOnly = false;
	private Build myBuild;
	private BugStorage bugzillaStorage;
	public static final int CRASH_POSTPONE_DELAY = 120; // crash delay postpone in sec
	public static Activity boundedActivity = null;

	public void onCreate() {
		super.onCreate();
		ApplicationPreferences privatePrefs = new ApplicationPreferences(this);
		bugzillaStorage = new BugStorage(this);
		String version = this.getString(R.string.app_version);
		EventGenerator.INSTANCE.setContext(getApplicationContext());
		if (!privatePrefs.getVersion().contentEquals(version)) {
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			Editor editor = sharedPrefs.edit();
			editor.clear();
			editor.commit();
			PreferenceManager.setDefaultValues(this, R.xml.menu, true);
			privatePrefs.setVersion(version);

			resetCrashLogsUploadTypes();
			EventDB db = new EventDB(this.getApplicationContext());

			try {
				db.open();
				db.deleteAllTypes();

				db.deleteAllCriticalEvents();

				db.addTypes(new String[]{"IPANIC","FABRICERR","IPANIC_FORCED","MEMERR","INSTERR","SRAMECCERR","HWWDTLOGERR","MSHUTDOWN","UIWDT","WDT"},1);

				for (String type : getResources().getStringArray(R.array.reportCrashLogsTypeValues)) {
					if (!db.isTypeInDb(type)) {
						db.addType(type,0);
					}
				}

				db.insertCricitalEvent("TOMBSTONE", "system_server", "", "", "", "", "");
				db.close();
			} catch (SQLException e) {
				Log.w("CrashReport: update of critical crash db failed");
			}
		}
	}

	public boolean isServiceStarted(){
		return serviceStarted;
	}
	public void setServiceStarted(Boolean s){
		serviceStarted = s;
		if( (false == serviceStarted) && (null != boundedActivity) ) {
			FragmentTransaction ft = boundedActivity.getFragmentManager().beginTransaction();
			Fragment prev = boundedActivity.getFragmentManager().findFragmentByTag("dialog");
			if (prev != null) {
				ft.remove(prev);
			}
			ft.addToBackStack(null);
		}
	}

	public boolean isTryingToConnect(){
		return tryingToConnect;
	}
	public void setTryingToConnect(Boolean s){
		tryingToConnect = s;
	}

	public boolean isActivityBounded() {
		return activityBounded;
	}

	public void setActivityBounded(Boolean s) {
		activityBounded = s;
	}

	public void setActivity(Activity activity) {
		boundedActivity = activity;
	}

	public boolean isWifiOnly() {
		return wifiOnly;
	}
	public void setWifiOnly(Boolean s) {
		wifiOnly = s;
	}

	public void setMyBuild(Build myBuild) {
		this.myBuild = myBuild;
	}
	public Build getMyBuild() {
		return myBuild;
	}

	public String getUserLastName(){
		ApplicationPreferences privatePrefs = new ApplicationPreferences(this);
		return privatePrefs.getUserLastName();
	}

	public String getUserFirstName() {
		ApplicationPreferences privatePrefs = new ApplicationPreferences(this);
		return privatePrefs.getUserFirstName();
	}

	public String getUserEmail() {
		ApplicationPreferences privatePrefs = new ApplicationPreferences(this);
		return privatePrefs.getUserEmail();
	}

	public void setUserFirstName(String firstname){
		ApplicationPreferences privatePrefs = new ApplicationPreferences(this);
		privatePrefs.setUserFirstName(firstname);
	}

	public void setUserLastName(String lastname) {
		ApplicationPreferences privatePrefs = new ApplicationPreferences(this);
		privatePrefs.setUserLastName(lastname);
	}

	public void setUserEmail(String email) {
		ApplicationPreferences privatePrefs = new ApplicationPreferences(this);
		privatePrefs.setUserEmail(email);
	}

	public void resetCrashLogsUploadTypes() {
		ApplicationPreferences privatePrefs = new ApplicationPreferences(this);
		privatePrefs.resetCrashLogsUploadTypes();
	}

	public BugStorage getBugzillaStorage() {
		return bugzillaStorage;
	}

	public void checkEvents(String from) throws FileNotFoundException,SQLException{
		HistoryEventFile histFile;
		String histEventLine;
		EventDB db;
		String myBuild;
		Event event;
		NotificationMgr nMgr;
		PhoneInspector phoneInspector;
		BlackLister blackLister = new BlackLister();

		db = new EventDB(getApplicationContext());
		myBuild = ((CrashReport) getApplicationContext()).getMyBuild().toString();
		nMgr = new NotificationMgr(getApplicationContext());

		try {
			db.open();
			blackLister.setDb(db);
			histFile = new HistoryEventFile();

			while (histFile.hasNext()) {
				histEventLine = histFile.getNextEvent();
				if (histEventLine.length() != 0) {
					HistoryEvent histEvent = new HistoryEvent(histEventLine);
					if (histEvent.getEventId().replaceAll("0", "").length() != 0) {
						if (!db.isEventInDb(histEvent.getEventId()) && !db.isEventInBlackList(histEvent.getEventId())) {
							event = new Event(histEvent, myBuild);
							blackLister.cleanRain(event.getDate());
							if (!blackLister.blackList(event)) {

								//Manage full Dropbox case before adding an event
							    phoneInspector = PhoneInspector.getInstance(getApplicationContext());
							    phoneInspector.manageFullDropBox();

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
										BZFile bzfile = new BZFile(event.getCrashDir());
										db.addBZ(event.getEventId(), bzfile, event.getDate());
										Log.d(from+": BZ added in DB, " + histEvent.getEventId());
									}
									Log.d(from+": Event successfully added to DB, " + event.toString());
									if (!event.isDataReady()) {
										Timer timer = new Timer(true);
										timer.schedule(new NotifyCrashTask(event.getEventId(),getApplicationContext()), CRASH_POSTPONE_DELAY*1000);
									}
								}
							}
						} else
							Log.d(from+": Event already in DB, " + histEvent.getEventId());
					} else
						Log.d(from+": Event ignored ID:" + histEvent.getEventId());
				}
			}

			if (db.isThereEventToNotify()) {
				nMgr.notifyCriticalEvent(db.getCriticalEventsNumber());
			}

			db.close();
		} catch (FileNotFoundException e) {
			db.close();
			throw e;
		} catch (SQLException e) {
			throw e;
		}

	}
}
