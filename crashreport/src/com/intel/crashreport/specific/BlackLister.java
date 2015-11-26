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

package com.intel.crashreport.specific;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import com.intel.crashreport.Log;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import com.intel.crashreport.GeneralEventDB;

public class BlackLister {
	private static final String module = "BlackLister: ";
	private EventDB db;
	private static Context mCtxt;

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

	public boolean blackListCrash(Event event) {
		boolean result = false;
		int lastCrashDate;

		try{
			RainSignature rainSignature = new RainSignature(event);

			if (rainSignature.isEmpty()) {
				//exit condition
				return false;
			}
			if (db.isRainEventExist(rainSignature)) {
				if (db.isInTheCurrentRain(event)) {
					db.updateRainEvent(rainSignature, event.getDate());
					result = true;
				}
				else {
					//need to check last crash date consistency for corner case
					lastCrashDate = db.getLastCrashDate(rainSignature);
					if (lastCrashDate > GeneralEventDB.convertDateForDb(event.getDate())) {
						//wrong last date, need clean
						Log.w("BlackLister: wrong date detected - "+ lastCrashDate);
						cleanRain(GeneralEventDB.convertDateForJava(lastCrashDate +
											 EventDB.RAIN_DURATION_MAX + 1 ));
						lastCrashDate = -1;
					}

					result = db.checkNewRain(event, lastCrashDate);
				}
			}
			else {
				result = db.checkNewRain(event);
			}
			if (result) {
				db.addBlackEvent(event, "RAIN");
				Log.w("BlackLister: event "+event.getEventId()+" is RAIN");
			}
		}
		catch(SQLException e){
			Log.e("BlaskLister:blackListCrash" + e.getMessage());
		}
		if (result) {
			//If the event is blacklisted its crashlog directories shall be removed
			CrashlogDaemonCmdFile.CreateCrashlogdCmdFile(CrashlogDaemonCmdFile.Command.DELETE, "ARGS="+event.getEventId()+";\n", mCtxt);
		}
		return result;
	}

	public void cleanRain(Date date) {
		Cursor cursor = null;
		try{
			cursor = db.fetchLastRain(date);
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
					db.deleteRainEvent(new RainSignature(type,data0,data1,data2,data3));
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

		if (event.isDropboxEvent() && !event.getOrigin().equals("")) {
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
				db.addBlackEvent(event, "DUPLICATE");
				Log.i(module +"event "+event.getEventId()+" is DUPLICATE");
				//the event is blacklisted so its crashlog directory shall be removed
				CrashlogDaemonCmdFile.CreateCrashlogdCmdFile(CrashlogDaemonCmdFile.Command.DELETE, "ARGS="+event.getEventId()+";\n", mCtxt);
			}
		}
		return isEventDuplicate;
	}
}
