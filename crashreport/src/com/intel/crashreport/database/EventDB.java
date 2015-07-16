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

import com.intel.crashreport.database.GeneralEventDB;
import com.intel.crashreport.Log;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import com.intel.crashreport.GeneralEvent;

import com.intel.crashreport.specific.GcmMessage;

public class EventDB extends GeneralEventDB{

	private static final int BEGIN_FIBONACCI = 13;
	private static final int BEGIN_FIBONACCI_BEFORE = 8;

	public EventDB(Context ctx) {
		super(ctx);
	}

	public int getEntriesCount(String table, String where) {
		Cursor cursor;
		int count = -1;
		cursor = mDb.rawQuery("SELECT count(*) FROM " + table
				+ ((where != null) ? " WHERE " + where : ""), null);

		if (cursor == null) {
			Log.e("Cursor instance creation failed.");
			return count;
		}

		try {
			if(cursor.moveToFirst()) {
				count = cursor.getInt(0);
			}
		} catch (SQLException e) {
			Log.e("Could not move cursor to expected record.");
		} finally {
			cursor.close();
		}

		return count;
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
				initialValues.put(KEY_RAINID, cursor.getString(cursor.getColumnIndex(KEY_ID)));
				cursor.close();
			}
		}

		initialValues.put(KEY_ID, event.getEventId());
		initialValues.put(KEY_REASON, reason);
		initialValues.put(KEY_CRASHDIR, event.getCrashDir());

		return mDb.insert(DATABASE_BLACK_EVENTS_TABLE, null, initialValues);
	}

	public Cursor fetchAllBlackEvents() throws SQLException {
		return mDb.query(DATABASE_BLACK_EVENTS_TABLE, new String[] {KEY_ID, KEY_REASON}, null, null, null, null, null);
	}

	public Cursor fetchBlackEventsFromQuery(String query) throws SQLException {
		Cursor mCursor;
		mCursor = mDb.query(DATABASE_BLACK_EVENTS_TABLE, new String[] {KEY_ID, KEY_REASON,KEY_CRASHDIR}, query, null, null, null, null);
		if(mCursor != null)
			mCursor.moveToFirst();
		return mCursor;
	}

	/**
	 * perform the given query on the rain of crashes database table
	 *
	 * @param query to perform
	 * @return the cursor set to the first row if found, null otherwise
	 * @throws SQLException
	 */
	public Cursor fetchRainOfCrashesFromQuery(String query) throws SQLException {
		Cursor mCursor;
		mCursor = mDb.query(DATABASE_RAIN_OF_CRASHES_TABLE, new String[] {KEY_DATE,KEY_TYPE,KEY_DATA0,KEY_DATA1,KEY_DATA2,KEY_DATA3,KEY_ID,KEY_OCCURRENCES,KEY_LAST_FIBONACCI,KEY_NEXT_FIBONACCI}, query, null, null, null, null);
		if(mCursor != null)
			mCursor.moveToFirst();
		return mCursor;
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
		int mDate = convertDateForDb(date);
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
		initialValues.put(KEY_DATE, convertDateForDb(event.getDate()));
		initialValues.put(KEY_OCCURRENCES, 1);
		initialValues.put(KEY_LAST_FIBONACCI, BEGIN_FIBONACCI_BEFORE);
		initialValues.put(KEY_NEXT_FIBONACCI, BEGIN_FIBONACCI);
		initialValues.put(KEY_ID, event.getEventId());

		return mDb.insert(DATABASE_RAIN_OF_CRASHES_TABLE, null, initialValues);
	}

	public Cursor getRainEventInfo(String signature) throws SQLException {
		return fetchRainOfCrashesFromQuery(signature);
	}

	public static int getRainLastFibo(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndex(KEY_LAST_FIBONACCI));
	}

	public static int getRainNextFibo(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndex(KEY_NEXT_FIBONACCI));
	}

	public static int getRainOccurances(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndex(KEY_OCCURRENCES));
	}

	public int getRainOccurances(String signature) {
		Cursor cursor = getRainEventInfo(signature);
		int occurances = 0;
		if (cursor != null) {
			occurances = getRainOccurances(getRainEventInfo(signature));
			cursor.close();
		}
		return occurances;
	}

	public boolean resetRainEvent(String signature, Date date)
			throws SQLException {
		ContentValues args = new ContentValues();
		args.put(KEY_OCCURRENCES, 1);
		args.put(KEY_DATE, convertDateForDb(date) );
		args.put(KEY_LAST_FIBONACCI, BEGIN_FIBONACCI_BEFORE);
		args.put(KEY_NEXT_FIBONACCI, BEGIN_FIBONACCI);
		return mDb.update(DATABASE_RAIN_OF_CRASHES_TABLE, args, signature, null) > 0;
	}


	public boolean updateRainEvent(String signature, Date date, int occurences) {
		ContentValues args = new ContentValues();
		args.put(KEY_DATE, convertDateForDb(date));
		args.put(KEY_OCCURRENCES, occurences);

		return mDb.update(DATABASE_RAIN_OF_CRASHES_TABLE, args, signature, null) > 0;
	}

	public boolean updateRainEvent(String signature, Date date, int occurences,
			int nextFibo, int lastFibo) {
		ContentValues args = new ContentValues();
		args.put(KEY_DATE, convertDateForDb(date));
		args.put(KEY_OCCURRENCES, occurences);
		args.put(KEY_NEXT_FIBONACCI, nextFibo);
		args.put(KEY_LAST_FIBONACCI, lastFibo);

		return mDb.update(DATABASE_RAIN_OF_CRASHES_TABLE, args, signature, null) > 0;
	}

	public boolean deleteRainEvent(String signature) {
		String whereQuery = signature;
		return mDb.delete(DATABASE_RAIN_OF_CRASHES_TABLE, whereQuery, null) > 0;
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
		Cursor mCursor;
		Boolean ret;
		mCursor = mDb.query(true, DATABASE_BLACK_EVENTS_TABLE, new String[] {KEY_ID},
				KEY_ID + " = '" + eventId + "'", null,
				null, null, null, null);
		if (mCursor != null) {
			ret = mCursor.moveToFirst();
			mCursor.close();
			return ret;
		}
		return false;
	}

	/**
	 * Check if the input event belongs to the last rain of crashes with signature
	 * matching the input event signature. If the date of the input crash doesn't
	 * exceed the maximum delay, it is considered as part of the current rain.
	 *
	 * @param event to test
	 * @return true if the event belongs to the current rain with matching signature. False otherwise.
	 * @throws SQLException
	 */
	public boolean isInTheCurrentRain(GeneralEvent event, String signature, int maxDelay)
			throws SQLException {
		Date date = event.getDate();

		Cursor mCursor = getRainEventInfo(signature);
		if (mCursor != null) {
			int lastEvent = mCursor.getInt(mCursor.getColumnIndex(KEY_DATE));
			mCursor.close();
			int newDate = convertDateForDb(date);
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

		Cursor mCursor = getRainEventInfo(signature);
		if (mCursor != null) {
			int lastEvent = mCursor.getInt(mCursor.getColumnIndex(KEY_DATE));
			mCursor.close();
			return lastEvent;
		}
		return 0;
	}


	public int checkPathStatus(String sPath) throws SQLException {
		Cursor mCursor;
		int ret;
		mCursor = mDb.query(true, DATABASE_TABLE, new String[] {KEY_UPLOADLOG},
				KEY_CRASHDIR + " = '" + sPath + "'", null,
				null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToNext();
			if (mCursor.isAfterLast()) {
				// unreferenced path
				ret = -1;
			}
			else {
				ret = mCursor.getInt(mCursor.getColumnIndex(KEY_UPLOADLOG));
			}

			mCursor.close();
			return ret;
		}
		return 0;
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
		String typeAsString = GcmMessage.getTypeLabel(aGcmMessage.getType());

		return addGcmMessage(
				aGcmMessage.getTitle(),
				aGcmMessage.getText(),
				typeAsString,
				aGcmMessage.getData(),
				aGcmMessage.getDate());
	}

	/**
	 * Get a GcmMessage object from a GCM_MESSAGES_TABLE cursor
	 * @param cursor a GCM_MESSAGES_TABLE cursor
	 * @return the GcmMessage object associated with the cursor
	 */
	public GcmMessage fillGCMFromCursor(Cursor cursor) {

		Date date = convertDateForJava(cursor.getInt(cursor.getColumnIndex(KEY_DATE)));
		GcmMessage message = new GcmMessage(cursor.getInt(cursor.getColumnIndex(KEY_ROWID)),
				cursor.getString(cursor.getColumnIndex(KEY_GCM_TITLE)),
				cursor.getString(cursor.getColumnIndex(KEY_GCM_TEXT)),
				cursor.getString(cursor.getColumnIndex(KEY_TYPE)),
				cursor.getString(cursor.getColumnIndex(KEY_GCM_DATA)),
				cursor.getInt(cursor.getColumnIndex(KEY_NOTIFIED))==1,
				date);

		return message;
	}

	/**
	 * Get a GcmMessage with its rowId
	 * @param rowId the row id of the gcm message to get
	 * @return the gcm message
	 */
	public GcmMessage getGcmMessageFromId(int rowId) {
		Cursor mCursor;
		String whereQuery = KEY_ROWID+"="+rowId;
		GcmMessage message = null;

		mCursor = mDb.query(true, DATABASE_GCM_MESSAGES_TABLE, new String[] {KEY_ROWID, KEY_GCM_TITLE, KEY_GCM_TEXT, KEY_TYPE,
				KEY_GCM_DATA,KEY_DATE,KEY_NOTIFIED}, whereQuery, null,
				null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
			message = fillGCMFromCursor(mCursor);
			mCursor.close();
		}
		return message;
	}
}
