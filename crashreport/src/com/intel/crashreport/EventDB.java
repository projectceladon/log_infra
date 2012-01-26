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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class EventDB {

	private static final String DATABASE_NAME = "eventlogs.db";
	private static final String DATABASE_TABLE = "events";
	private static final int DATABASE_VERSION = 2;

	public static final String KEY_ROWID = "_id";
	public static final String KEY_ID = "eventId";
	public static final String KEY_NAME = "eventName";
	public static final String KEY_TYPE = "type";
	public static final String KEY_DATA0 = "data0";
	public static final String KEY_DATA1 = "data1";
	public static final String KEY_DATA2 = "data2";
	public static final String KEY_DATA3 = "data3";
	public static final String KEY_DATA4 = "data4";
	public static final String KEY_DATA5 = "data5";
	public static final String KEY_DATE = "date";
	public static final String KEY_BUILDID = "buildId";
	public static final String KEY_DEVICEID = "deviceId";
	public static final String KEY_IMEI = "imei";
	public static final String KEY_UPTIME = "uptime";
	public static final String KEY_UPLOAD = "uploaded";

	private static final String DATABASE_CREATE =
			"create table " + DATABASE_TABLE + " (" +
					KEY_ROWID + " integer primary key autoincrement, " +
					KEY_ID + " text not null, " +
					KEY_NAME + " text not null, " +
					KEY_TYPE + " text not null, " +
					KEY_DATA0 + " text not null, " +
					KEY_DATA1 + " text not null, " +
					KEY_DATA2 + " text not null, " +
					KEY_DATA3 + " text not null, " +
					KEY_DATA4 + " text not null, " +
					KEY_DATA5 + " text not null, " +
					KEY_DATE + " text not null, " +
					KEY_BUILDID + " text not null, " +
					KEY_DEVICEID + " text not null, " +
					KEY_IMEI + " text not null, " +
					KEY_UPTIME + " text not null, " +
					KEY_UPLOAD + " integer);";

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		public void onCreate(SQLiteDatabase db) {

			db.execSQL(DATABASE_CREATE);
		}

		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w("Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			onCreate(db);
		}
	}

	public EventDB(Context ctx) {
		this.mCtx = ctx;
	}

	public EventDB open() throws SQLException, SQLiteException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	public long addEvent(String eventId, String eventName, String type,
			String data0, String data1, String data2, String data3,
			String data4, String data5, String date, String buildId,
			String deviceId, String imei, String uptime) {
		ContentValues initialValues = new ContentValues();

		initialValues.put(KEY_ID, eventId);
		initialValues.put(KEY_NAME, eventName);
		initialValues.put(KEY_TYPE, type);
		initialValues.put(KEY_DATA0, data0);
		initialValues.put(KEY_DATA1, data1);
		initialValues.put(KEY_DATA2, data2);
		initialValues.put(KEY_DATA3, data3);
		initialValues.put(KEY_DATA4, data4);
		initialValues.put(KEY_DATA5, data5);
		initialValues.put(KEY_DATE, date);
		initialValues.put(KEY_BUILDID, buildId);
		initialValues.put(KEY_DEVICEID, deviceId);
		initialValues.put(KEY_IMEI, imei);
		initialValues.put(KEY_UPTIME, uptime);
		initialValues.put(KEY_UPLOAD, 0);

		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	public long addEvent(Event event) {
		ContentValues initialValues = new ContentValues();

		initialValues.put(KEY_ID, event.getEventId());
		initialValues.put(KEY_NAME, event.getEventName());
		initialValues.put(KEY_TYPE, event.getType());
		initialValues.put(KEY_DATA0, event.getData0());
		initialValues.put(KEY_DATA1, event.getData1());
		initialValues.put(KEY_DATA2, event.getData2());
		initialValues.put(KEY_DATA3, event.getData3());
		initialValues.put(KEY_DATA4, event.getData4());
		initialValues.put(KEY_DATA5, event.getData5());
		initialValues.put(KEY_DATE, event.getDate());
		initialValues.put(KEY_BUILDID, event.getBuildId());
		initialValues.put(KEY_DEVICEID, event.getDeviceId());
		initialValues.put(KEY_IMEI, event.getImei());
		initialValues.put(KEY_UPTIME, event.getUptime());
		initialValues.put(KEY_UPLOAD, 0);

		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	public boolean deleteEvent(String eventId) {

		return mDb.delete(DATABASE_TABLE, KEY_ID + "='" + eventId + "'", null) > 0;
	}

	public Cursor fetchAllEvents() {

		return mDb.query(DATABASE_TABLE, new String[] {KEY_ID, KEY_NAME, KEY_TYPE,
				KEY_DATA0, KEY_DATA1, KEY_DATA2, KEY_DATA3, KEY_DATA4, KEY_DATA5,
				KEY_DATE, KEY_BUILDID, KEY_DEVICEID, KEY_IMEI,
				KEY_UPTIME, KEY_UPLOAD}, null, null, null, null, null);
	}

	public Cursor fetchNotUploadedEvents() throws SQLException {

		Cursor mCursor;

		mCursor = mDb.query(true, DATABASE_TABLE, new String[] {KEY_ID, KEY_NAME, KEY_TYPE,
				KEY_DATA0, KEY_DATA1, KEY_DATA2, KEY_DATA3, KEY_DATA4, KEY_DATA5, KEY_DATE,
				KEY_BUILDID, KEY_DEVICEID, KEY_IMEI, KEY_UPTIME}, KEY_UPLOAD + "=" + 0, null,
				null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}

	public Event fillEventFromCursor(Cursor cursor) {
		Event event = new Event();

		event.setEventId(cursor.getString(cursor.getColumnIndex(KEY_ID)));
		event.setEventName(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
		event.setType(cursor.getString(cursor.getColumnIndex(KEY_TYPE)));
		event.setData0(cursor.getString(cursor.getColumnIndex(KEY_DATA0)));
		event.setData1(cursor.getString(cursor.getColumnIndex(KEY_DATA1)));
		event.setData2(cursor.getString(cursor.getColumnIndex(KEY_DATA2)));
		event.setData3(cursor.getString(cursor.getColumnIndex(KEY_DATA3)));
		event.setData4(cursor.getString(cursor.getColumnIndex(KEY_DATA4)));
		event.setData5(cursor.getString(cursor.getColumnIndex(KEY_DATA5)));
		event.setDate(cursor.getString(cursor.getColumnIndex(KEY_DATE)));
		event.setBuildId(cursor.getString(cursor.getColumnIndex(KEY_BUILDID)));
		event.setDeviceId(cursor.getString(cursor.getColumnIndex(KEY_DEVICEID)));
		event.setImei(cursor.getString(cursor.getColumnIndex(KEY_IMEI)));
		event.setUptime(cursor.getString(cursor.getColumnIndex(KEY_UPTIME)));

		return event;
	}

	public Boolean isEventInDb(String eventId) {
		return isEventExistFromWhereQuery(KEY_ID + "='" + eventId + "'");
	}

	public Boolean isThereEventToUpload() {
		return isEventExistFromWhereQuery(KEY_UPLOAD + "=" + 0);
	}

	public Boolean isThereEventToUploadNoReboot() {
		return isEventExistFromWhereQuery(KEY_UPLOAD + "='0' AND " + KEY_NAME + "<>'REBOOT'");
	}

	public Boolean isThereRebootToUpload() {
		return isEventExistFromWhereQuery(KEY_UPLOAD + "='0' AND " + KEY_NAME + "='REBOOT'");
	}

	private Boolean isEventExistFromWhereQuery(String whereQuery) {
		Cursor mCursor;
		Boolean ret;
		try {
			mCursor = mDb.query(true, DATABASE_TABLE, new String[] {KEY_ID},
					whereQuery, null,
					null, null, null, null);
			if (mCursor != null) {
				ret = mCursor.moveToFirst();
				mCursor.close();
				return ret;
			}
		} catch (SQLException e) {
			return false;
		}
		return false;
	}

	public int getNewCrashNumber() {
		return getNumberFromWhereQuery(KEY_UPLOAD + "='0' AND " + KEY_NAME + "='CRASH'");
	}

	public int getNewUptimeNumber() {
		return getNumberFromWhereQuery(KEY_UPLOAD + "='0' AND " + KEY_NAME + "='UPTIME'");
	}

	public int getNewRebootNumber() {
		return getNumberFromWhereQuery(KEY_UPLOAD + "='0' AND " + KEY_NAME + "='REBOOT'");
	}

	private int getNumberFromWhereQuery(String whereQuery) {
		Cursor mCursor;
		int count;
		try {
			mCursor = mDb.query(true, DATABASE_TABLE, new String[] {KEY_ID},
					whereQuery, null,
					null, null, null, null);
			if (mCursor != null) {
				count = mCursor.getCount();
				mCursor.close();
				return count;
			}
		} catch (SQLException e) {
			return 0;
		}
		return 0;
	}

	public boolean updateEventToUploaded(String eventId) {
		ContentValues args = new ContentValues();

		args.put(KEY_UPLOAD, 1);

		return mDb.update(DATABASE_TABLE, args, KEY_ID + "='" + eventId + "'", null) > 0;
	}

}
