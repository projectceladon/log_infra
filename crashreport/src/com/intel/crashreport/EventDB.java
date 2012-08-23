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

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class EventDB {

	private static final int COEF_S_TO_MS = 1000;

	private static final String DATABASE_NAME = "eventlogs.db";
	private static final String DATABASE_TABLE = "events";
	private static final String DATABASE_TYPE_TABLE = "events_type";
	private static final String DATABASE_CRITICAL_EVENTS_TABLE = "critical_events";
	private static final String DATABASE_CRITICAL_TABLE = "critical_events_type";
	private static final int DATABASE_VERSION = 5;

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
	public static final String KEY_CRASHDIR = "crashdir";
	public static final String KEY_UPLOADLOG = "logsuploaded";
	public static final String KEY_NOTIFIED = "notified";
	public static final String KEY_CRITICAL = "critical";

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
					KEY_DATE + " integer not null, " +
					KEY_BUILDID + " text not null, " +
					KEY_DEVICEID + " text not null, " +
					KEY_IMEI + " text not null, " +
					KEY_UPTIME + " text not null, " +
					KEY_UPLOAD + " integer, " +
					KEY_CRASHDIR + " text, " +
					KEY_UPLOADLOG + " integer, "+
					KEY_NOTIFIED + " integer);";

	private static final String DATABASE_TYPE_CREATE =
			"create table " + DATABASE_TYPE_TABLE + " ("+
					KEY_TYPE + " text primary key, "+
					KEY_CRITICAL+" integer );";

	private static final String DATABASE_TYPE_EMPTY =
			"delete from "+DATABASE_TYPE_TABLE+";";

	private static final String DATABASE_CRITICAL_EVENTS_CREATE =
			"create table " + DATABASE_CRITICAL_EVENTS_TABLE + " ("+
					KEY_TYPE + " text not null, "+
					KEY_DATA0 + " text not null, "+
					KEY_DATA1 + " text, "+
					KEY_DATA2 + " text, "+
					KEY_DATA3 + " text, "+
					KEY_DATA4 + " text, "+
					KEY_DATA5 + " text, "+
					"PRIMARY KEY ( "+KEY_TYPE+", "+KEY_DATA0+", "+KEY_DATA1+", "+KEY_DATA2+", "+KEY_DATA3+", "+KEY_DATA4+", "+KEY_DATA5+"));";

	private static final String DATABASE_CRITICAL_EVENTS_EMPTY =
			"delete from "+DATABASE_CRITICAL_EVENTS_TABLE+";";

	private static final String SELECT_CRITICAL_EVENTS_QUERY = "select "+KEY_ID+" from "+DATABASE_TABLE+" e,"+DATABASE_CRITICAL_EVENTS_TABLE+" ce"
			                             +" where ce."+KEY_TYPE+"=e."+KEY_TYPE+" and trim(e."+KEY_DATA0+")=ce."+KEY_DATA0+" and "
			                             +"(ce."+KEY_DATA1+"='' or ce."+KEY_DATA1+"=trim(e."+KEY_DATA1+")) and "
			                             +"(ce."+KEY_DATA2+"='' or ce."+KEY_DATA2+"=trim(e."+KEY_DATA2+")) and "
			                             +"(ce."+KEY_DATA3+"='' or ce."+KEY_DATA3+"=trim(e."+KEY_DATA3+")) and "
			                             +"(ce."+KEY_DATA4+"='' or ce."+KEY_DATA4+"=trim(e."+KEY_DATA4+")) and "
			                             +"(ce."+KEY_DATA5+"='' or ce."+KEY_DATA5+"=trim(e."+KEY_DATA5+"))";

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
			db.execSQL(DATABASE_TYPE_CREATE);
			db.execSQL(DATABASE_CRITICAL_EVENTS_CREATE);
		}

		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w("Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TYPE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_CRITICAL_EVENTS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_CRITICAL_TABLE);
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
		if (mDbHelper != null)
			mDbHelper.close();
	}

	public long addEvent(String eventId, String eventName, String type,
			String data0, String data1, String data2, String data3,
			String data4, String data5, Date date, String buildId,
			String deviceId, String imei, String uptime, String crashDir) {
		ContentValues initialValues = new ContentValues();
		int eventDate = convertDateForDb(date);
		if (eventName.equals("")) return -2;
		else if (eventDate == -1) return -3;
		if (eventName.contentEquals("REBOOT") && type.contentEquals("SWUPDATE"))
			mDb.delete(DATABASE_TABLE, KEY_DATE+"<"+eventDate, null);

		initialValues.put(KEY_ID, eventId);
		initialValues.put(KEY_NAME, eventName);
		initialValues.put(KEY_TYPE, type);
		initialValues.put(KEY_DATA0, data0);
		initialValues.put(KEY_DATA1, data1);
		initialValues.put(KEY_DATA2, data2);
		initialValues.put(KEY_DATA3, data3);
		initialValues.put(KEY_DATA4, data4);
		initialValues.put(KEY_DATA5, data5);
		initialValues.put(KEY_DATE, eventDate);
		initialValues.put(KEY_BUILDID, buildId);
		initialValues.put(KEY_DEVICEID, deviceId);
		initialValues.put(KEY_IMEI, imei);
		initialValues.put(KEY_UPTIME, uptime);
		initialValues.put(KEY_UPLOAD, 0);
		initialValues.put(KEY_CRASHDIR, crashDir);
		initialValues.put(KEY_UPLOADLOG, 0);
		initialValues.put(KEY_NOTIFIED, 0);

		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	public long addEvent(Event event) {
		return addEvent(event.getEventId(),
				event.getEventName(),
				event.getType(),
				event.getData0(),
				event.getData1(),
				event.getData2(),
				event.getData3(),
				event.getData4(),
				event.getData5(),
				event.getDate(),
				event.getBuildId(),
				event.getDeviceId(),
				event.getImei(),
				event.getUptime(),
				event.getCrashDir());
	}

	public boolean deleteEvent(String eventId) {

		return mDb.delete(DATABASE_TABLE, KEY_ID + "='" + eventId + "'", null) > 0;
	}

	public Cursor fetchAllEvents() {

		return mDb.query(DATABASE_TABLE, new String[] {KEY_ID, KEY_NAME, KEY_TYPE,
				KEY_DATA0, KEY_DATA1, KEY_DATA2, KEY_DATA3, KEY_DATA4, KEY_DATA5,
				KEY_DATE, KEY_BUILDID, KEY_DEVICEID, KEY_IMEI, KEY_UPTIME,
				KEY_UPLOAD, KEY_CRASHDIR, KEY_UPLOADLOG, KEY_NOTIFIED}, null, null, null, null, null);
	}

	public Cursor fetchNotUploadedEvents() throws SQLException {
		String whereQuery = KEY_UPLOAD+"='0'";
		return fetchEventFromWhereQuery(whereQuery);
	}

	public Cursor fetchNotUploadedLogs(String crashTypes[]) throws SQLException {
		StringBuilder bQuery = new StringBuilder("");
		bQuery.append("("+KEY_NAME+"='STATS' and "+KEY_UPLOADLOG+"='0') or "+
		              "("+KEY_NAME+"='APLOG' and "+KEY_UPLOADLOG+"='0') or ");
		if (crashTypes != null) {
			String sExcludedType = getExcludeTypeInLine(crashTypes);

			if (sExcludedType != "") {
				bQuery.append("("+KEY_NAME+"='CRASH' and "+KEY_TYPE+" not in ("+sExcludedType+") and "+KEY_UPLOADLOG+"='0')");
			} else {
				bQuery.append("("+KEY_NAME+"='CRASH' and "+KEY_UPLOADLOG+"='0')");
			}
		} else {
			bQuery.append("("+KEY_NAME+"='CRASH' and "+KEY_UPLOADLOG+"='0')");
		}
		Log.d("fetchNotUploadedLogs : Query string = " +bQuery.toString() );
		return fetchEventFromWhereQuery(bQuery.toString());

	}

	private Cursor fetchEventFromWhereQuery(String whereQuery) throws SQLException {
		Cursor mCursor;

		mCursor = mDb.query(true, DATABASE_TABLE, new String[] {KEY_ID, KEY_NAME, KEY_TYPE,
				KEY_DATA0, KEY_DATA1, KEY_DATA2, KEY_DATA3, KEY_DATA4, KEY_DATA5, KEY_DATE,
				KEY_BUILDID, KEY_DEVICEID, KEY_IMEI, KEY_UPTIME, KEY_CRASHDIR}, whereQuery, null,
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
		event.setDate(convertDateForJava(cursor.getInt(cursor.getColumnIndex(KEY_DATE))));
		event.setBuildId(cursor.getString(cursor.getColumnIndex(KEY_BUILDID)));
		event.setDeviceId(cursor.getString(cursor.getColumnIndex(KEY_DEVICEID)));
		event.setImei(cursor.getString(cursor.getColumnIndex(KEY_IMEI)));
		event.setUptime(cursor.getString(cursor.getColumnIndex(KEY_UPTIME)));
		event.setCrashDir(cursor.getString(cursor.getColumnIndex(KEY_CRASHDIR)));

		return event;
	}

	public Boolean isEventInDb(String eventId) {
		return isEventExistFromWhereQuery(KEY_ID + "='" + eventId + "'");
	}

	private String getExcludeTypeInLine(String crashTypes[])
	{
		String sExcludedType = "";
		for (int i=0; i<crashTypes.length; i++){
			if (i > 0){
				sExcludedType+= ",";
			}
			sExcludedType+= "'" + crashTypes[i] +"'";
		}
		return sExcludedType;
	}


	public Boolean isThereEventToUpload(String crashTypes[]) {
		StringBuilder bQuery = new StringBuilder(KEY_UPLOAD+"='0'");
		bQuery.append(" or ("+KEY_NAME+"='STATS' and "+KEY_UPLOADLOG+"='0' and "+ KEY_CRASHDIR + "!='' )");
		bQuery.append(" or ("+KEY_NAME+"='APLOG' and "+KEY_UPLOADLOG+"='0' and "+ KEY_CRASHDIR + "!='' )");
		if (crashTypes != null) {

			String sExcludedType = getExcludeTypeInLine(crashTypes);

			if (sExcludedType != "") {
				bQuery.append(" or ("+KEY_NAME+"='CRASH' and "+KEY_TYPE+" not in("+ sExcludedType +") and "
						+KEY_UPLOADLOG + "='0' and "+ KEY_CRASHDIR + "!='' )");
			} else {
				// Case with no excluded type but still need to check Uploadlog
				bQuery.append(" or ("+KEY_NAME+"='CRASH' and "+KEY_UPLOADLOG+"='0' and "+ KEY_CRASHDIR + "!='' )");
			}
		} else {
			// Case with no excluded type but still need to check Uploadlog
			bQuery.append(" or ("+KEY_NAME+"='CRASH' and "+ KEY_UPLOADLOG +"='0' and "+ KEY_CRASHDIR + "!='' )");
		}
		Log.d("isThereEventToUpload : Query string = " +bQuery.toString() );
		return isEventExistFromWhereQuery(bQuery.toString());
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
			Log.d("isEventExistFromWhereQuery : " + e.getMessage());
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

	public boolean updateEventLogToUploaded(String eventId) {
		ContentValues args = new ContentValues();

		args.put(KEY_UPLOADLOG, 1);

		return mDb.update(DATABASE_TABLE, args, KEY_ID + "='" + eventId + "'", null) > 0;
	}

	private int convertDateForDb(Date date) {
		if (date==null) {
			return -1;
		}
		return (int)(date.getTime() / COEF_S_TO_MS);
	}

	private Date convertDateForJava(int date) {
		long dateLong = date;
		dateLong = dateLong * COEF_S_TO_MS;
		return new Date(dateLong);
	}

	public long addType(String type,int critical) {
		ContentValues initialValues = new ContentValues();

		initialValues.put(KEY_TYPE, type);
		initialValues.put(KEY_CRITICAL,critical);

		return mDb.insert(DATABASE_TYPE_TABLE, null, initialValues);
	}

	public void addTypes(String[] types,int critical){
		for(String type : types){
			addType(type,critical);
		}
	}

	public boolean updateEventToNotified(String eventId) {
		ContentValues args = new ContentValues();

		args.put(KEY_NOTIFIED, 1);

		return mDb.update(DATABASE_TABLE, args, KEY_ID + "='" + eventId + "'", null) > 0;
	}

	private Boolean isTypeExistFromWhereQuery(String whereQuery) {
		Cursor mCursor;
		Boolean ret;
		try {
			mCursor = mDb.query(true, DATABASE_TYPE_TABLE, new String[] {KEY_TYPE},
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

	public boolean isTypeInDb(String type){
		return isTypeExistFromWhereQuery(KEY_TYPE + "='" + type + "'");
	}

	public Cursor fetchNotNotifiedEvents() throws SQLException {
		String whereQuery = KEY_NOTIFIED+"='0' and "+
	                        "("+KEY_TYPE + " in (select "+KEY_TYPE+" from "+
				            DATABASE_TYPE_TABLE+" where "+KEY_CRITICAL+"=1)"
				            + "or ("+KEY_ID+" in ("+SELECT_CRITICAL_EVENTS_QUERY+" )))";
		return fetchEventFromWhereQuery(whereQuery);
	}

	public boolean isThereEventToNotified(){
		String whereQuery = KEY_NOTIFIED+"='0' and "+
	                        "("+KEY_TYPE + " in (select "+KEY_TYPE+" from "+
				            DATABASE_TYPE_TABLE+" where "+KEY_CRITICAL+"=1)"
				            + "or ("+KEY_ID+" in ("+SELECT_CRITICAL_EVENTS_QUERY+" )))";
		return isEventExistFromWhereQuery(whereQuery);
	}

	public int getCriticalEventsNumber() {
		Cursor mCursor;
		int count;
                String whereQuery = KEY_NOTIFIED+"='0' and "+
	                        "("+KEY_TYPE + " in (select "+KEY_TYPE+" from "+
				            DATABASE_TYPE_TABLE+" where "+KEY_CRITICAL+"=1)"
				            + "or ("+KEY_ID+" in ("+SELECT_CRITICAL_EVENTS_QUERY+" )))";
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


	public void deleteAllTypes(){
		mDb.execSQL(DATABASE_TYPE_EMPTY);
	}

	public long insertCricitalEvent(String type, String data0, String data1, String data2, String data3, String data4, String data5){
		ContentValues initialValues = new ContentValues();

		initialValues.put(KEY_TYPE, type);
		initialValues.put(KEY_DATA0, data0);
		initialValues.put(KEY_DATA1, data1);
		initialValues.put(KEY_DATA2, data2);
		initialValues.put(KEY_DATA3, data3);
		initialValues.put(KEY_DATA4, data4);
		initialValues.put(KEY_DATA5, data5);

		return mDb.insert(DATABASE_CRITICAL_EVENTS_TABLE, null, initialValues);
	}

	public void deleteAllCriticalEvents(){
		mDb.execSQL(DATABASE_CRITICAL_EVENTS_EMPTY);
	}


}
