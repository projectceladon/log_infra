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

package com.intel.crashreport.specific;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import com.intel.crashreport.database.EventDB;
import com.intel.crashreport.database.Utils;
import com.intel.crashreport.Log;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import com.intel.crashreport.database.GeneralEventDB;
import com.intel.crashreport.database.Utils;

public class BlackLister {
	private static final String module = "BlackLister: ";
	private EventDB db;
	private static Context mCtxt;

	private static final int RAIN_DURATION_MAX = 3600;
	private static final int MAX_DELAY_RAIN = 600;
	private static final int RAIN_CRASH_NUMBER = 10;

	/**
	 * Constructor
	 *
	 * @param aContext is caller context
	 */
	public BlackLister (Context aContext) {
		mCtxt = aContext;
	}

	/**
	 * Checks if the input event should be blacklisted because
	 * included in a rain of crashes or duplicated.
	 *
	 * @param event is the input event to check.
	 * @return true if the event has been blacklisted. False otherwise.
	 * @throws SQLException
	 */
	public boolean blackList(Event event) throws SQLException {
		/* Manage duplicate dropbox events*/
		if(blackListDuplicate(event))
			return true;
		/* Manage rain events*/
		if (event.isRainEventKind()) {
			return blackListCrash(event);
		}
		return false;
	}

	/**
	 * Returns a boolean indicating whether a valid <code>db</code> has
	 * been provided to this object or not.
	 * @return
	 * <ul>
	 * <li><code>true</code> if this object's <code>db</code> instance is valid</li>
	 * <li><code>false</code> otherwise</li>
	 * </ul>
	 */
	public boolean hasDb() {
		return (null != this.db);
	}

	public void setDb(EventDB mDb) {
		db = mDb;
	}

        private void notifyEndOfRain(RainSignature rainSignature) {
		int occurences = db.getRainOccurances(rainSignature.querySignature());
		if( occurences > 0)
			EventGenerator.INSTANCE.generateEventRain(
				rainSignature, occurences);
	}

        private void updateRainOccurance(RainSignature rainSignature, Date date) {
		String signature = rainSignature.querySignature();
		Cursor cursor = db.getRainEventInfo(signature);
		int occurences = db.getRainOccurances(cursor);
		int lastFibo = db.getRainLastFibo(cursor);
		int nextFibo = db.getRainNextFibo(cursor);
		if (cursor != null)
			cursor.close();

		occurences++;
		if (occurences == nextFibo) {
			db.updateRainEvent(signature, date, 0,
				lastFibo + nextFibo, nextFibo);
			EventGenerator.INSTANCE.generateEventRain(rainSignature, occurences);
		}
		else
			db.updateRainEvent(signature, date, occurences);
        }

	public boolean checkNewRain(Event event, int lastRain) {
		Cursor mCursor;
		int lastEventDate, defaultDate;
		Date date = event.getDate();
		RainSignature rainSignature = new RainSignature(event);

		//robustness for all corner case around date
		defaultDate = Utils.convertDateForDb(date);
		if ((lastRain != -1) && (lastRain < defaultDate)) {
			lastEventDate = lastRain;
		} else {
			lastEventDate = defaultDate;
			lastEventDate -= RAIN_DURATION_MAX;
		}

		// Fetch from events database the number of events with
		// matching signature and with a matching date value
		int count = db.getMatchingRainEventsCount(lastEventDate,
			rainSignature.querySignature());

		if (count < RAIN_CRASH_NUMBER)
			return false;

		if (-1 == lastRain)
			db.addRainEvent(event);
		else {
			notifyEndOfRain(rainSignature);
			db.resetRainEvent(rainSignature.querySignature(), date);
		}
		EventGenerator.INSTANCE.generateEventRain(rainSignature, RAIN_CRASH_NUMBER);
		return true;
	}

	public boolean blackListCrash(Event event) {
		boolean result = false;
		int lastCrashDate;

		try{
			RainSignature rainSignature = new RainSignature(event);

			if (rainSignature.isEmpty()) {
				//exit condition
				return false;
			}

			String signature = rainSignature.querySignature();
			if(db.isRainEventExist(signature)) {
				if(db.isInTheCurrentRain(event, signature, MAX_DELAY_RAIN)) {
					updateRainOccurance(rainSignature, event.getDate());
					result = true;
				}
				else {
					//need to check last crash date consistency for corner case
					int eventDate = Utils.convertDateForDb(event.getDate());
					lastCrashDate = db.getLastCrashDate(signature);
					if (lastCrashDate > eventDate) {
						Log.w("BlackLister: wrong date detected - "
							+ lastCrashDate);
						cleanRain(Utils.convertDateForJava(lastCrashDate
							+ RAIN_DURATION_MAX + 1 ));
						lastCrashDate = -1;
					}

					result = checkNewRain(event, lastCrashDate);
				}
			}
			else {
				result = checkNewRain(event, -1);
			}
			if(result) {
				db.addBlackEvent(event, "RAIN", rainSignature.querySignature());
				Log.w("BlackLister: event "+event.getEventId()+" is RAIN");
			}
		}
		catch(SQLException e){
			Log.e("BlaskLister:blackListCrash" + e.getMessage());
		}
		if (result) {
			//If the event is blacklisted its crashlog directories shall be removed
			CrashlogDaemonCmdFile.createCrashlogdCmdFile(CrashlogDaemonCmdFile.Command.DELETE, "ARGS="+event.getEventId()+";\n", mCtxt);
		}
		return result;
	}

	public void cleanRain(Date date) {
		Cursor cursor = null;
		try{
			cursor = db.fetchLastRain(date, RAIN_DURATION_MAX);
			if(cursor != null){
				while(!cursor.isAfterLast()) {
					String type = cursor.getString(cursor.getColumnIndex("type"));
					String data0 = cursor.getString(cursor.getColumnIndex("data0"));
					String data1 = cursor.getString(cursor.getColumnIndex("data1"));
					String data2 = cursor.getString(cursor.getColumnIndex("data2"));
					String data3 = cursor.getString(cursor.getColumnIndex("data3"));
					String rainId = cursor.getString(cursor.getColumnIndex("eventId"));
					Cursor blackEvents = db.fetchBlackEventsRain(rainId);
					if(blackEvents!=null){
						while(!blackEvents.isAfterLast()) {
							String crashDir = blackEvents.getString(blackEvents.getColumnIndex("crashdir"));

							try {
								changeCrashType(crashDir,blackEvents.getString(blackEvents.getColumnIndex("reason")));
							} catch (FileNotFoundException e) {
								Log.e("BlackLister:cleanRain:crashfile is not present "+crashDir);
							}
							catch(IOException e){
								Log.e("BlackLister:cleanRain:write problem in crashfile "+crashDir);
							}
							blackEvents.moveToNext();
						}
						blackEvents.close();
					}

					RainSignature rain = new RainSignature(type, data0,
							data1, data2, data3);
					notifyEndOfRain(rain);
					db.deleteRainEvent(rain.querySignature());
					cursor.moveToNext();
				}
				cursor.close();
			}
		}catch(SQLException e){
			Log.e("BlackLister:cleanRain: cannot open db "+e.getMessage());
			if(cursor != null) {
				cursor.close();
			}
		}
	}

	public void changeCrashType(String crashDir,String reason) throws IOException,FileNotFoundException{
		File logData = new File(crashDir);
		if (logData.exists() && logData.isDirectory()) {
			File logDataFiles[] = logData.listFiles();
			if (logDataFiles != null) {
				for(File file:logDataFiles){
					if(file.getName().equals("crashfile")){
						CrashFile crashfile = new CrashFile(crashDir,false);
						crashfile.writeCrashFile(reason);
					}
				}
			}
		}
	}

	/**
	 * This function blacklists 2 kinds of duplicate dropbox events :
	 *  - duplicates generated when full dropbox condition is encountered and where all
	 *    logfiles in dropbox directory are renamed with *.lost suffixe
	 *  - duplicates when a same dropbox (with origin file name identical) event is detected twice
	 * The event is blacklisted if detected as DUPLICATE.
	 * Note : it is assumed each dropbox logfile name is unique thanks to the timestamp it contains.
	 *
	 * @param event to check.
	 * @return true is the event is duplicate. False otherwise.
	 * @throws SQLException
	 */
	public boolean blackListDuplicate(Event event) throws SQLException {

		boolean isEventDuplicate = false;

		if (event.isDropboxEvent() && !event.getOrigin().isEmpty()) {
			/* 1st case : a same dropbox event detected twice (with origin file name identical)*/
			if (db.isOriginExist(event.getOrigin())) {
				isEventDuplicate = true;
				Log.d(module + "blackListDuplicate: event "+event.getEventId()+ " : origin file "+event.getOrigin()+ " already in DB");
			}
			/* 2nd case : an event already in DB but with its dropbox logfiles renamed with *.lost suffix */
			if (!isEventDuplicate && event.isFullDropboxEvent() && event.getOrigin().endsWith(".lost")) {
				String originBasename = event.getOrigin().substring(0, event.getOrigin().indexOf(".lost"));
				if(db.isOriginBasenameExist(originBasename)) {
					isEventDuplicate = true;
					Log.d(module + "blackListDuplicate: event "+event.getEventId()+ " : origin basename file "+originBasename+ " already in DB");
				}
			}
			if (isEventDuplicate) {
				db.addBlackEvent(event, "DUPLICATE", (new RainSignature(event)).querySignature() );
				Log.i(module +"event "+event.getEventId()+" is DUPLICATE");
				//the event is blacklisted so its crashlog directory shall be removed
				CrashlogDaemonCmdFile.createCrashlogdCmdFile(CrashlogDaemonCmdFile.Command.DELETE, "ARGS="+event.getEventId()+";\n", mCtxt);
			}
		}
		return isEventDuplicate;
	}
}
