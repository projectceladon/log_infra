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

package com.intel.crashreport.database;

import java.util.Date;

import com.intel.crashreport.core.GcmMessage;
import com.intel.crashreport.core.GeneralEvent;
import com.intel.crashreport.database.GeneralEventDB;

import com.intel.crashtoolserver.bean.Device;
import com.intel.crashtoolserver.bean.Event;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EventDB extends GeneralEventDB{

	private static final int BEGIN_FIBONACCI = 13;
	private static final int BEGIN_FIBONACCI_BEFORE = 8;

	public EventDB() {
		super();
	}

	public EventDB(Context ctx) {
		super(ctx);
	}

	public int getMatchingRainEventsCount(int lastEvent, String signature) {

		return getEntriesCount(DATABASE_TABLE,
				signature + " AND "
				+ KEY_DATE + " > " + lastEvent);
	}

	/**
	 * Add the input event in the Black events database
	 *
	 * @param event is the event to add in the Blacklisted events db
	 * @param reason is the reason set in the db "reason" column
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 * @throws SQLException
	 */
	public long addBlackEvent(GeneralEvent event, String reason, String signature)
			throws SQLException {
		ContentValues initialValues = new ContentValues();

		if(reason.equals("RAIN")) {
			Cursor cursor = getRainEventInfo(signature);
			if(cursor != null){
				initialValues.put(KEY_RAINID,
						cursor.getString(cursor.getColumnIndex(KEY_ID)));
				cursor.close();
			}
		}

		initialValues.put(KEY_ID, event.getEventId());
		initialValues.put(KEY_REASON, reason);
		initialValues.put(KEY_CRASHDIR, event.getCrashDir());

		return mDb.insert(DATABASE_BLACK_EVENTS_TABLE, null, initialValues);
	}

	public Cursor fetchAllBlackEvents() throws SQLException {
		return selectEntries(DATABASE_BLACK_EVENTS_TABLE,
				new String[] {KEY_ID, KEY_REASON});
	}

	public Cursor fetchBlackEventsFromQuery(String query) throws SQLException {
		return selectEntries(DATABASE_BLACK_EVENTS_TABLE,
				new String[] {KEY_ID, KEY_REASON, KEY_CRASHDIR}, query);
	}

	/**
	 * perform the given query on the rain of crashes database table
	 *
	 * @param query to perform
	 * @return the cursor set to the first row if found, null otherwise
	 * @throws SQLException
	 */
	public Cursor fetchRainOfCrashesFromQuery(String query) throws SQLException {
		return selectEntries(DATABASE_RAIN_OF_CRASHES_TABLE, rainTableColums, query);
	}

	public Cursor fetchBlackEventsRain(String rainId) throws SQLException {
		String query = KEY_RAINID + " = '"+ rainId +"'";
		return fetchBlackEventsFromQuery(query);
	}

	/**
	 * Fetch all elements from Rain of Crashes database with a date anterior
	 * to (current date - rain of crashes maximum duration)
	 *
	 * @param date is the current date
	 * @return the cursor set to the first row if found, null otherwise
	 * @throws SQLException
	 */
	public Cursor fetchLastRain(Date date, int maxDuration) throws SQLException {
		int mDate = Utils.convertDateForDb(date);
		mDate -= maxDuration;
		return fetchRainOfCrashesFromQuery(KEY_DATE + " < " + mDate);
	}

	/**
	 * @brief add a rain event in the rain of crashes database
	 *
	 * @param event must contains the input of which the rain to add is made
	 * @return the row ID of the newly inserted row(here the rain event), or -1 if
	 * an error occurred
	 */
	public long addRainEvent(GeneralEvent event) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_TYPE, event.getType());
		initialValues.put(KEY_DATA0, event.getData0());
		initialValues.put(KEY_DATA1, event.getData1());
		initialValues.put(KEY_DATA2, event.getData2());
		initialValues.put(KEY_DATA3, event.getData3());
		initialValues.put(KEY_DATE, Utils.convertDateForDb(event.getDate()));
		initialValues.put(KEY_OCCURRENCES, 1);
		initialValues.put(KEY_LAST_FIBONACCI, BEGIN_FIBONACCI_BEFORE);
		initialValues.put(KEY_NEXT_FIBONACCI, BEGIN_FIBONACCI);
		initialValues.put(KEY_ID, event.getEventId());

		return mDb.insert(DATABASE_RAIN_OF_CRASHES_TABLE, null, initialValues);
	}

	public Cursor getRainEventInfo(String signature) throws SQLException {
		return fetchRainOfCrashesFromQuery(signature);
	}

	public int getRainLastFibo(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndex(KEY_LAST_FIBONACCI));
	}

	public int getRainNextFibo(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndex(KEY_NEXT_FIBONACCI));
	}

	public int getRainOccurances(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndex(KEY_OCCURRENCES));
	}

	public int getRainOccurances(String signature) {
		Cursor cursor = getRainEventInfo(signature);
		int occurances = 0;
		if (cursor != null) {
			occurances = getRainOccurances(cursor);
			cursor.close();
		}
		return occurances;
	}

	public boolean resetRainEvent(String signature, Date date)
			throws SQLException {
		ContentValues args = new ContentValues();
		args.put(KEY_OCCURRENCES, 1);
		args.put(KEY_DATE, Utils.convertDateForDb(date));
		args.put(KEY_LAST_FIBONACCI, BEGIN_FIBONACCI_BEFORE);
		args.put(KEY_NEXT_FIBONACCI, BEGIN_FIBONACCI);
		return mDb.update(DATABASE_RAIN_OF_CRASHES_TABLE, args, signature, null) > 0;
	}

	public boolean updateRainEvent(String signature, Date date, int occurences) {
		ContentValues args = new ContentValues();
		args.put(KEY_DATE, Utils.convertDateForDb(date));
		args.put(KEY_OCCURRENCES, occurences);

		return mDb.update(DATABASE_RAIN_OF_CRASHES_TABLE, args, signature, null) > 0;
	}

	public boolean updateRainEvent(String signature, Date date, int occurences,
			int nextFibo, int lastFibo) {
		ContentValues args = new ContentValues();
		args.put(KEY_DATE, Utils.convertDateForDb(date) );
		args.put(KEY_OCCURRENCES, occurences);
		args.put(KEY_NEXT_FIBONACCI, nextFibo);
		args.put(KEY_LAST_FIBONACCI, lastFibo);

		return mDb.update(DATABASE_RAIN_OF_CRASHES_TABLE, args, signature, null) > 0;
	}

	public boolean deleteRainEvent(String signature) {
		return mDb.delete(DATABASE_RAIN_OF_CRASHES_TABLE, signature, null) > 0;
	}

	public boolean isRainEventExist(String signature) {
		return ((getEntriesCount(DATABASE_RAIN_OF_CRASHES_TABLE,
				signature) > 0) ? true : false);
	}

	/**
	 * Check if the input event is in the Black_Events database
	 *
	 * @param eventId is the event to check
	 * @return true is the event is in the Black_Events db. False otherwise
	 * @throws SQLException
	 */
	public boolean isEventInBlackList(String eventId) throws SQLException{
		return ((getEntriesCount(DATABASE_BLACK_EVENTS_TABLE,
				KEY_ID + " = '" + eventId + "'") > 0) ? true : false);
	}

	/**
	 * Check if the input event belongs to the last rain of crashes with signature
	 * matching the input event signature. If the date of the input crash doesn't
	 * exceed the maximum delay, it is considered as part of the current rain.
	 *
	 * @param event to test
	 * @return true only if the event belongs to the current rain with matching signature.
	 * @throws SQLException
	 */
	public boolean isInTheCurrentRain(GeneralEvent event, String signature, int maxDelay)
			throws SQLException {
		Date date = event.getDate();

		Cursor cursor = getRainEventInfo(signature);
		if (cursor != null) {
			int lastEvent = cursor.getInt(cursor.getColumnIndex(KEY_DATE));
			cursor.close();
			int newDate = Utils.convertDateForDb(date);
			if( lastEvent <= newDate) {
				if ( (newDate - lastEvent) <=  maxDelay) {
					return true;
				}
				else
					return false;
			}
		}
		return false;
	}

	/**
	 * Return the date of the last event contained in the rain of crashes that
	 * matches the input crash signature
	 *
	 * @param signature of the crashes belonging to the rain
	 * @return the date value of the last event belonging to the rain matching
	 * the input signature. 0 if no rain matching the input signature exist.
	 * @throws SQLException
	 */
	public int getLastCrashDate(String signature) throws SQLException {

		Cursor cursor = getRainEventInfo(signature);
		if (cursor != null) {
			int lastEvent = cursor.getInt(cursor.getColumnIndex(KEY_DATE));
			cursor.close();
			return lastEvent;
		}
		return 0;
	}


	public int checkPathStatus(String sPath) throws SQLException {
		int ret;
		Cursor cursor = selectEntries(DATABASE_TABLE,
				new String[] {KEY_UPLOADLOG},
				KEY_CRASHDIR + " = '" + sPath + "'");

		if (cursor == null)
			return 0;

		ret = cursor.getInt(cursor.getColumnIndex(KEY_UPLOADLOG));
		cursor.close();
		return ret;
	}

	/**
	 * Writes the given GCM message to database.
	 *
	 * The message's date is updated if it does not have one at this point
	 * in time.
	 *
	 * @param aGcmMessage the message to write
	 *
	 * @return the result of the database insertion request
	 *	(-1 if an error occurred or if the message is null).
	 */
	public long addGcmMessage(GcmMessage aGcmMessage) {
		if(aGcmMessage == null) {
			return -1;
		}
		if(aGcmMessage.getDate() == null) {
			aGcmMessage.setDate(new Date());
		}

		return addGcmMessage(
				aGcmMessage.getTitle(),
				aGcmMessage.getText(),
				GcmMessage.getTypeLabel(aGcmMessage.getType()),
				aGcmMessage.getData(),
				aGcmMessage.getDate());
	}

	/**
	 * Get a GcmMessage object from a GCM_MESSAGES_TABLE cursor
	 * @param cursor a GCM_MESSAGES_TABLE cursor
	 * @return the GcmMessage object associated with the cursor
	 */
	public GcmMessage fillGCMFromCursor(Cursor cursor) {

		Date date = Utils.convertDateForJava(
			cursor.getInt(cursor.getColumnIndex(KEY_DATE)));
		GcmMessage message = new GcmMessage(
				cursor.getInt(cursor.getColumnIndex(KEY_ROWID)),
				cursor.getString(cursor.getColumnIndex(KEY_GCM_TITLE)),
				cursor.getString(cursor.getColumnIndex(KEY_GCM_TEXT)),
				cursor.getString(cursor.getColumnIndex(KEY_TYPE)),
				cursor.getString(cursor.getColumnIndex(KEY_GCM_DATA)),
				cursor.getInt(cursor.getColumnIndex(KEY_NOTIFIED)) == 1,
				date);

		return message;
	}

	/**
	 * Get a GcmMessage with its rowId
	 * @param rowId the row id of the gcm message to get
	 * @return the gcm message
	 */
	public GcmMessage getGcmMessageFromId(int rowId) {
		Cursor cursor;
		GcmMessage message = null;

		cursor = selectEntries(DATABASE_GCM_MESSAGES_TABLE, gcmTableColums,
				KEY_ROWID + "=" + rowId);

		if (cursor != null) {
			message = fillGCMFromCursor(cursor);
			cursor.close();
		}
		return message;
	}

	private Device fillDeviceFromCursor(Cursor cursor) {
		String imei = cursor.getString(cursor.getColumnIndex(KEY_IMEI));
		String deviceId = cursor.getString(cursor.getColumnIndex(KEY_DEVICEID));
		String sSSN = cursor.getString(cursor.getColumnIndex(KEY_DEVICE_SSN));
		String sSPID = cursor.getString(cursor.getColumnIndex(KEY_DEVICE_SPID));
		String sTokenGCM = cursor.getString(cursor.getColumnIndex(KEY_DEVICE_TOKEN));

		if (sSSN.isEmpty()) sSSN = null;
		return new Device(deviceId, imei, sSSN, sTokenGCM, sSPID);
	}

	public Device fillDeviceInformation() {
		Cursor cursor;
		Device device = null;

		cursor = selectEntries(DATABASE_DEVICE_TABLE,
				deviceTableColums);

		if (cursor != null) {
			cursor.moveToFirst();
			device = fillDeviceFromCursor(cursor);
			cursor.close();
		} else {
			device = new Device();
		}
		return device;
	}

	public Cursor fillDeviceInfo() {
		return selectEntries(DATABASE_DEVICE_TABLE,
				deviceTableColums);
	}

	public int getLastSWUpdate(){
		int retVal;
		Cursor cursor = selectEntries(DATABASE_TABLE, new String[] {KEY_ROWID},
				KEY_TYPE+"='SWUPDATE'", KEY_ROWID, true, "1");

		if (cursor == null)
			return -1;

		try {
			retVal = cursor.getInt(cursor.getColumnIndex(KEY_ROWID));
		} catch (SQLException e) {
			retVal = -1;
		} finally {
			cursor.close();
		}
		return retVal;
	}

	public long getCurrentUptime() {
		Cursor cursor;
		long lResultUptime = -1;
		int iLastSWUpdateID = getLastSWUpdate();

		cursor = selectEntries(DATABASE_TABLE, new String[] {KEY_NAME,KEY_UPTIME},
				 KEY_ROWID + " > " + iLastSWUpdateID, KEY_ROWID, false);

		if (cursor != null) {
			long lUptimeReboot = 0;
			long lUptimeOther = 0;
			while (!cursor.isAfterLast()) {
				String sUptime;
				try {
					sUptime = cursor.getString(cursor.getColumnIndex(KEY_UPTIME));
				} catch (SQLException e) {
					sUptime = "";
				}

				int iCurUptime = com.intel.crashreport.common.Utils.convertUptime(
						sUptime);
				if (iCurUptime >= 0){
					String sName;
					try {
						sName = cursor.getString(cursor.getColumnIndex(KEY_NAME));
					} catch (SQLException e) {
						sName = "";
					}
					if (sName.equals("REBOOT")) {
						lUptimeReboot += iCurUptime;
						lUptimeOther = 0;
					} else if (iCurUptime > 0) {
						lUptimeOther = iCurUptime;
					}
				}
				cursor.moveToNext();
			}
			cursor.close();
			lResultUptime = lUptimeReboot + lUptimeOther;
		}

		return lResultUptime;
	}

	public boolean updateEventUploadStateByRow(int row, int upload,
			int upload_log, boolean update_log) {
		ContentValues args = new ContentValues();
		args.put(KEY_UPLOAD, upload);
		if (update_log)
			args.put(KEY_UPLOADLOG, upload_log);

		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "='" + row + "'", null) > 0;
	}

	public boolean cleanCrashDirByID(int row){
		ContentValues args = new ContentValues();
		args.put(KEY_CRASHDIR, "");
		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "='" + row + "'", null) > 0;
	}

	public boolean cleanCrashDirByTime(String sTime){
		int iTimeValue = Utils.convertDateForDB(sTime);
		if (iTimeValue <= 0)
			return false;

		ContentValues args = new ContentValues();
		args.put(KEY_CRASHDIR, "");
		return mDb.update(DATABASE_TABLE, args, KEY_DATE + "<" + iTimeValue
				+ " AND " + KEY_CRASHDIR + " is not null", null) > 0;
	}


	public  String[] getLogsDir(String where) {
		Cursor cursor;
		String[] sResultLogsDir = new String[0];

		cursor = selectEntries(DATABASE_TABLE, new String[] {KEY_CRASHDIR}, where);
		if (cursor != null) {
			int i = 0;
			sResultLogsDir = new String[cursor.getCount()];
			while (!cursor.isAfterLast()) {
				String dr;
				try {
					dr = cursor.getString(cursor.getColumnIndex(KEY_CRASHDIR));
				} catch (SQLException e) {
					dr = "getLogsDir  SQLexception";
				}
				sResultLogsDir[i++] = dr;
				cursor.moveToNext();
			}
			cursor.close();
		}
		return sResultLogsDir;
	}

	public  String[] getAllLogsDir() {
		return getLogsDir(null);
	}

	public String[] getLogsDirByTime(String sTime){
		int iTimeValue = Utils.convertDateForDB(sTime);
		if (iTimeValue <= 0)
			return new String[0];

		return getLogsDir(KEY_DATE + "<" + iTimeValue);
	}

	public String getLogDirByID(int iID){
		String[] logs = getLogsDir(KEY_ROWID + "=" + iID);

		if (logs != null && logs.length > 0)
			return logs[0];

		return "";
	}

	public static Map <String, String> cursorToHashMap(Cursor cursor, boolean formatDate) {
		if (cursor == null)
			return null;

		Map <String, String> map = new LinkedHashMap <String, String>();
		for (String name : cursor.getColumnNames())
			if (formatDate && name.equals(KEY_DATE))
				map.put(name, com.intel.crashreport.common.Utils.convertDate(
					cursor.getLong(	cursor.getColumnIndex(KEY_DATE))));
			else
				map.put(name, cursor.getString(cursor.getColumnIndex(name)));
		return map;
	}
}
