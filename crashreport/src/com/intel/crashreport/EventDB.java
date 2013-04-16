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

import java.util.Date;

import com.intel.crashreport.bugzilla.BZ;
import com.intel.crashreport.bugzilla.BZFile;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class EventDB {

	private static final int COEF_S_TO_MS = 1000;
	private static final int BEGIN_FIBONACCI = 13;
	private static final int BEGIN_FIBONACCI_BEFORE = 8;
	private static final int RAIN_DURATION_MAX = 3600;
	private static final int MAX_DELAY_RAIN = 600;
	private static final int RAIN_CRASH_NUMBER = 10;

	private static final String DATABASE_NAME = "eventlogs.db";
	private static final String DATABASE_TABLE = "events";
	private static final String DATABASE_TYPE_TABLE = "events_type";
	private static final String DATABASE_CRITICAL_EVENTS_TABLE = "critical_events";
	private static final String DATABASE_CRITICAL_TABLE = "critical_events_type";
	private static final String DATABASE_BZ_TABLE = "bz_events";
	private static final String DATABASE_BLACK_EVENTS_TABLE = "black_events";
	private static final String DATABASE_RAIN_OF_CRASHES_TABLE = "rain_of_crashes";
	private static final int DATABASE_VERSION = 10;

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
	public static final String KEY_DATA_READY = "dataReady";
	public static final String KEY_ORIGIN = "origin";
	public static final String KEY_PDSTATUS = "pdStatus";
	public static final String KEY_SUMMARY = "summary";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_SEVERITY = "severity";
	public static final String KEY_BZ_TYPE = "bzType";
	public static final String KEY_BZ_COMPONENT = "bzComponent";
	public static final String KEY_SCREENSHOT = "screenshot";
	public static final String KEY_SCREENSHOT_PATH = "screenshotPath";
	public static final String KEY_CREATION_DATE = "creationDate";
	public static final String KEY_UPLOAD_DATE = "uploadDate";
	public static final String KEY_REASON = "reason";
	public static final String KEY_OCCURRENCES = "occurrences";
	public static final String KEY_LAST_FIBONACCI = "last_fibo";
	public static final String KEY_NEXT_FIBONACCI = "next_fibo";
	public static final String KEY_RAINID = "raindId";
	public static final String OTHER_EVENT_NAMES = "'STATS','APLOG','BZ','INFO','ERROR'";

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
					KEY_NOTIFIED + " integer, "+
					KEY_DATA_READY + " integer, "+
					KEY_ORIGIN + " text, " +
					KEY_PDSTATUS + " text " + ");";

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

	private static final String DATABASE_BLACK_EVENTS_CREATE =
			"create table " + DATABASE_BLACK_EVENTS_TABLE + " ("+
					KEY_ID + " text primary key, " +
					KEY_REASON + " text not null, "+
					KEY_CRASHDIR + " text, " +
					KEY_RAINID + " text );";

	private static final String DATABASE_RAIN_CREATE =
			"create table " + DATABASE_RAIN_OF_CRASHES_TABLE + " ("+
					KEY_ID + " text not null, "+
					KEY_TYPE + " text not null, " +
					KEY_DATA0 + " text not null, " +
					KEY_DATA1 + " text not null, " +
					KEY_DATA2 + " text not null, " +
					KEY_DATA3 + " text not null, " +
					KEY_DATE + " integer not null, " +
					KEY_OCCURRENCES + " integer, " +
					KEY_LAST_FIBONACCI + " integer, "+
					KEY_NEXT_FIBONACCI + " integer, "+
					"PRIMARY KEY ( "+KEY_TYPE+", "+KEY_DATA0+", "+KEY_DATA1+", "+KEY_DATA2+"));";

	private static final String DATABASE_CRITICAL_EVENTS_EMPTY =
			"delete from "+DATABASE_CRITICAL_EVENTS_TABLE+";";

	private static final String SELECT_CRITICAL_EVENTS_QUERY = "select "+KEY_ID+" from "+DATABASE_TABLE+" e,"+DATABASE_CRITICAL_EVENTS_TABLE+" ce"
			+" where ce."+KEY_TYPE+"=e."+KEY_TYPE+" and trim(e."+KEY_DATA0+")=ce."+KEY_DATA0+" and "
			+"(ce."+KEY_DATA1+"='' or ce."+KEY_DATA1+"=trim(e."+KEY_DATA1+")) and "
			+"(ce."+KEY_DATA2+"='' or ce."+KEY_DATA2+"=trim(e."+KEY_DATA2+")) and "
			+"(ce."+KEY_DATA3+"='' or ce."+KEY_DATA3+"=trim(e."+KEY_DATA3+")) and "
			+"(ce."+KEY_DATA4+"='' or ce."+KEY_DATA4+"=trim(e."+KEY_DATA4+")) and "
			+"(ce."+KEY_DATA5+"='' or ce."+KEY_DATA5+"=trim(e."+KEY_DATA5+"))";

	private static final String DATABASE_BZ_CREATE =
			"create table " + DATABASE_BZ_TABLE + " (" +
					KEY_ID + " text primary key, " +
					KEY_SUMMARY + " text not null, " +
					KEY_DESCRIPTION + " text not null, " +
					KEY_SEVERITY + " text not null, " +
					KEY_BZ_TYPE + " text not null, " +
					KEY_BZ_COMPONENT + " text not null, " +
					KEY_SCREENSHOT + " integer not null, " +
					KEY_UPLOAD_DATE + " integer, " +
					KEY_CREATION_DATE + " integer, "+
					KEY_SCREENSHOT_PATH + " text); ";

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
			db.execSQL(DATABASE_TYPE_CREATE);
			db.execSQL(DATABASE_CRITICAL_EVENTS_CREATE);
			db.execSQL(DATABASE_BZ_CREATE);
			db.execSQL(DATABASE_BLACK_EVENTS_CREATE);
			db.execSQL(DATABASE_RAIN_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w("Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_BZ_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TYPE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_CRITICAL_EVENTS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_CRITICAL_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_BLACK_EVENTS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_RAIN_OF_CRASHES_TABLE);
			onCreate(db);
		}

		@Override
		public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w("Downgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_BZ_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TYPE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_CRITICAL_EVENTS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_CRITICAL_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_BLACK_EVENTS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_RAIN_OF_CRASHES_TABLE);
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
			String deviceId, String imei, String uptime, String crashDir,
			boolean bDataReady, String origin, String pdStatus) {
		ContentValues initialValues = new ContentValues();
		int eventDate = convertDateForDb(date);
		if (eventName.equals("")) return -2;
		else if (eventDate == -1) return -3;

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
		if (bDataReady){
			initialValues.put(KEY_DATA_READY, 1);
		} else {
			initialValues.put(KEY_DATA_READY, 0);
		}
		initialValues.put(KEY_ORIGIN, origin);
		initialValues.put(KEY_PDSTATUS, pdStatus);

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
				event.getCrashDir(),
				event.isDataReady(),
				event.getOrigin(),
				event.getPdStatus());
	}

	public boolean deleteEvent(String eventId) {

		return mDb.delete(DATABASE_TABLE, KEY_ID + "='" + eventId + "'", null) > 0;
	}

	public Cursor fetchAllEvents() {

		return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_ID, KEY_NAME, KEY_TYPE,
				KEY_DATA0, KEY_DATA1, KEY_DATA2, KEY_DATA3, KEY_DATA4, KEY_DATA5,
				KEY_DATE, KEY_BUILDID, KEY_DEVICEID, KEY_IMEI, KEY_UPTIME,
				KEY_UPLOAD, KEY_CRASHDIR, KEY_UPLOADLOG, KEY_NOTIFIED, KEY_DATA_READY, KEY_ORIGIN, KEY_PDSTATUS}, null, null, null, null, null);
	}

	public Cursor fetchLastNEvents(String sNlimit) {

		return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_ID, KEY_NAME, KEY_TYPE,
				KEY_DATA0, KEY_DATA1, KEY_DATA2, KEY_DATA3, KEY_DATA4, KEY_DATA5,
				KEY_DATE, KEY_BUILDID, KEY_DEVICEID, KEY_IMEI, KEY_UPTIME,
				KEY_UPLOAD, KEY_CRASHDIR, KEY_UPLOADLOG, KEY_NOTIFIED, KEY_DATA_READY, KEY_ORIGIN, KEY_PDSTATUS}, null, null, null, null,
				KEY_ROWID + " DESC",sNlimit);
	}


	public Cursor fetchNotUploadedEvents() throws SQLException {
		String whereQuery = KEY_UPLOAD+"='0' and "+KEY_DATA_READY+"='1'";
		return fetchEventFromWhereQuery(whereQuery);
	}

	public Cursor fetchNotUploadedLogs(String crashTypes[]) throws SQLException {
		StringBuilder bQuery = new StringBuilder("");
		bQuery.append("("+KEY_NAME+" in ( " + OTHER_EVENT_NAMES +" ) and "+KEY_UPLOADLOG+"='0') or ");
		if (crashTypes != null) {
			String sExcludedType = getExcludeTypeInLine(crashTypes);

			if (sExcludedType != "") {
				bQuery.append("("+KEY_NAME+"='CRASH' and "+KEY_TYPE+" not in ("+sExcludedType+") and "+KEY_UPLOADLOG+"='0' and "+KEY_DATA_READY+"='1')");
			} else {
				bQuery.append("("+KEY_NAME+"='CRASH' and "+KEY_UPLOADLOG+"='0' and "+KEY_DATA_READY+"='1')");
			}
		} else {
			bQuery.append("("+KEY_NAME+"='CRASH' and "+KEY_UPLOADLOG+"='0' and "+KEY_DATA_READY+"='1')");
		}
		/* Only logs for events already uploaded*/
		bQuery.append(" and "+KEY_UPLOAD+"='1'");
		Log.d("fetchNotUploadedLogs : Query string = " +bQuery.toString() );
		return fetchEventFromWhereQuery(bQuery.toString());

	}

	private Cursor fetchEventFromWhereQuery(String whereQuery) throws SQLException {
		Cursor mCursor;

		mCursor = mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,KEY_ID, KEY_NAME, KEY_TYPE,
				KEY_DATA0, KEY_DATA1, KEY_DATA2, KEY_DATA3, KEY_DATA4, KEY_DATA5, KEY_DATE,
				KEY_BUILDID, KEY_DEVICEID, KEY_IMEI, KEY_UPTIME, KEY_CRASHDIR,
				KEY_UPLOAD, KEY_UPLOADLOG, KEY_DATA_READY, KEY_ORIGIN, KEY_PDSTATUS}, whereQuery, null,
				null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Event fillEventFromCursor(Cursor cursor) {
		Event event = new Event();
		event.setiRowID(cursor.getInt(cursor.getColumnIndex(KEY_ROWID)));
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
		event.setUploaded(cursor.getInt(cursor.getColumnIndex(KEY_UPLOAD))==1);
		event.setValid(cursor.getInt(cursor.getColumnIndex(KEY_UPLOAD))!=-1);
		event.setDataReady(cursor.getInt(cursor.getColumnIndex(KEY_DATA_READY))==1);
		event.setLogUploaded(cursor.getInt(cursor.getColumnIndex(KEY_UPLOADLOG))==1);
		event.setOrigin(cursor.getString(cursor.getColumnIndex(KEY_ORIGIN)));
		event.setPdStatus(cursor.getString(cursor.getColumnIndex(KEY_PDSTATUS)));


		return event;
	}

	public Boolean isEventInDb(String eventId) throws SQLException {
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

	public Boolean isThereEventToUpload() throws SQLException {
		StringBuilder bQuery = new StringBuilder(KEY_UPLOAD+"='0' and "+KEY_DATA_READY+"='1'");
		Log.d("isThereEventToUpload : Query string = " +bQuery.toString() );
		return isEventExistFromWhereQuery(bQuery.toString());
	}

	public Boolean isThereEventToUpload(String crashTypes[]) throws SQLException {
		StringBuilder bQuery = new StringBuilder(KEY_UPLOAD+"='0'");
		bQuery.append(" or ("+KEY_NAME+" in ( " + OTHER_EVENT_NAMES +" ) and "+KEY_UPLOADLOG+"='0' and "+ KEY_CRASHDIR + "!='' )");
		appendQueryForCrashTypes(crashTypes,bQuery);
		Log.d("isThereEventToUpload : Query string = " +bQuery.toString() );
		return isEventExistFromWhereQuery(bQuery.toString());
	}

	public void appendQueryForCrashTypes(String crashTypes[],StringBuilder aQuery ) {
		if (crashTypes != null) {

			String sExcludedType = getExcludeTypeInLine(crashTypes);

			if (sExcludedType != "") {
				aQuery.append(" or ("+KEY_NAME+"='CRASH' and "+KEY_TYPE+" not in("+ sExcludedType +") and "
						+KEY_UPLOADLOG + "='0' and "+ KEY_CRASHDIR + "!='' and "+ KEY_DATA_READY + "='1' )");
			} else {
				// Case with no excluded type but still need to check Uploadlog
				aQuery.append(" or ("+KEY_NAME+"='CRASH' and "+KEY_UPLOADLOG+"='0' and "+ KEY_CRASHDIR + "!='' and "+ KEY_DATA_READY + "='1' )");
			}
		} else {
			// Case with no excluded type but still need to check Uploadlog
			aQuery.append(" or ("+KEY_NAME+"='CRASH' and "+ KEY_UPLOADLOG +"='0' and "+ KEY_CRASHDIR + "!='' and "+ KEY_DATA_READY + "='1' )");
		}
	}

	public int getEventNumberLogToUpload(String crashTypes[]) {
		StringBuilder bQuery = new StringBuilder(KEY_UPLOAD+"='1'");
		bQuery.append(" and( ("+KEY_NAME+ " in  ( " + OTHER_EVENT_NAMES +" ) and "+KEY_UPLOADLOG+"='0' and "+ KEY_CRASHDIR + "!='' )");
		appendQueryForCrashTypes(crashTypes,bQuery);
		//required to close the query properly
		bQuery.append(")");
		Log.d("getEventNumberLogToUpload : Query string = " +bQuery.toString() );
		return getNumberFromWhereQuery(bQuery.toString());
	}

	public Boolean isThereEventToUploadNoReboot() throws SQLException {
		return isEventExistFromWhereQuery(KEY_UPLOAD + "='0' AND " + KEY_NAME + "<>'REBOOT' AND "+KEY_DATA_READY + "='1'");
	}

	public Boolean isThereRebootToUpload() {
		return isEventExistFromWhereQuery(KEY_UPLOAD + "='0' AND " + KEY_NAME + "='REBOOT'");
	}

	private Boolean isEventExistFromWhereQuery(String whereQuery) throws SQLException {
		Cursor mCursor;
		Boolean ret;
		mCursor = mDb.query(true, DATABASE_TABLE, new String[] {KEY_ID},
				whereQuery, null,
				null, null, null, null);
		if (mCursor != null) {
			ret = mCursor.moveToFirst();
			mCursor.close();
			return ret;
		}
		return false;
	}

	public int getNewCrashNumber() {
		return getNumberFromWhereQuery(KEY_UPLOAD + "='0' AND " + KEY_NAME + "='CRASH' and "+ KEY_DATA_READY + "='1'");
	}

	public int getNewUptimeNumber() {
		return getNumberFromWhereQuery(KEY_UPLOAD + "='0' AND " + KEY_NAME + "='UPTIME'");
	}

	public int getUptimeNumber() {
		return getNumberFromWhereQuery(KEY_NAME + "='UPTIME'");
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

	public boolean updateEventCrashdir(String eventId, String crashDir) {
		ContentValues args = new ContentValues();

		args.put(KEY_CRASHDIR, crashDir);

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

	public boolean isThereEventToNotify() throws SQLException {
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

	public boolean updateEventDataReady(String eventId) {
		ContentValues args = new ContentValues();

		args.put(KEY_DATA_READY, 1);

		return mDb.update(DATABASE_TABLE, args, KEY_ID + "='" + eventId + "'", null) > 0;
	}

	public boolean eventDataAreReady(String eventId) throws SQLException {
		String whereQuery = KEY_ID+"='"+eventId+"' and "+KEY_DATA_READY+"=1";
		return isEventExistFromWhereQuery(whereQuery);
	}

	public boolean updatePDStatus(String pdStatus, String eventId) {
		ContentValues args = new ContentValues();

		args.put(KEY_PDSTATUS, pdStatus);

		return mDb.update(DATABASE_TABLE, args, KEY_ID + "='" + eventId + "'", null) > 0;
	}

	public void deleteEventsBeforeUpdate(String eventId){
		String whereQuery = KEY_ROWID+" < (select "+KEY_ROWID+" from "+DATABASE_TABLE+" where "+KEY_ID+"='"+eventId+"')";
		mDb.delete(DATABASE_TABLE, whereQuery, null);
	}

	public long addBZ(String eventId,BZFile bzfile,Date date) {
		return addBZ(eventId,bzfile.getSummary(),bzfile.getDescription(),bzfile.getType(),bzfile.getSeverity(),bzfile.getComponent(),
				bzfile.getScreenshotsPathToString(),date);
	}

	public long addBZ(String eventId, String summary, String description,
			String type, String severity, String component, String screenshotPath, Date creationDate) {
		ContentValues initialValues = new ContentValues();

		initialValues.put(KEY_ID, eventId);
		initialValues.put(KEY_SUMMARY, summary);
		initialValues.put(KEY_DESCRIPTION, description);
		initialValues.put(KEY_SEVERITY, severity);
		initialValues.put(KEY_BZ_TYPE, type);
		initialValues.put(KEY_BZ_COMPONENT, component);
		if (screenshotPath.equals("")) {
			initialValues.put(KEY_SCREENSHOT, 0);
		}
		else {
			initialValues.put(KEY_SCREENSHOT, 1);
			initialValues.put(KEY_SCREENSHOT_PATH, screenshotPath);
		}
		initialValues.put(KEY_CREATION_DATE, convertDateForDb(creationDate));

		return mDb.insert(DATABASE_BZ_TABLE, null, initialValues);
	}

	public boolean deleteBZ(String eventId) {

		return mDb.delete(DATABASE_BZ_TABLE, KEY_ID + "='" + eventId + "'", null) > 0;
	}

	public Cursor fetchAllBZs() {
		Cursor cursor;
		String whereQuery = "Select bz."+KEY_ID+" as "+KEY_ID+", "+KEY_SUMMARY+" as "+KEY_SUMMARY+", "+KEY_DESCRIPTION+" as "+KEY_DESCRIPTION+", "+
				KEY_SEVERITY+" as "+KEY_SEVERITY+", "+KEY_BZ_TYPE+" as "+KEY_BZ_TYPE+", "+KEY_BZ_COMPONENT+" as "+KEY_BZ_COMPONENT+", "+KEY_SCREENSHOT+" as "+KEY_SCREENSHOT+", "+
				KEY_UPLOAD+" as "+KEY_UPLOAD+", "+KEY_UPLOADLOG+" as "+KEY_UPLOADLOG+", "+
				KEY_UPLOAD_DATE+" as "+KEY_UPLOAD_DATE+", "+KEY_CREATION_DATE+" as "+KEY_CREATION_DATE+", "+KEY_SCREENSHOT_PATH+ " as "+KEY_SCREENSHOT_PATH+" from "+DATABASE_TABLE+" e,"+DATABASE_BZ_TABLE+" bz "+
				"where bz."+KEY_ID+" = "+"e."+KEY_ID;
		cursor = mDb.rawQuery(whereQuery, null);
		if (cursor != null)
			cursor.moveToFirst();
		return cursor;
	}

	public BZ fillBZFromCursor(Cursor cursor) {
		BZ bz = new BZ();

		bz.setEventId(cursor.getString(cursor.getColumnIndex(KEY_ID)));
		bz.setSummary(cursor.getString(cursor.getColumnIndex(KEY_SUMMARY)));
		bz.setDescription(cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)));
		bz.setType(cursor.getString(cursor.getColumnIndex(KEY_BZ_TYPE)));
		bz.setComponent(cursor.getString(cursor.getColumnIndex(KEY_BZ_COMPONENT)));
		bz.setSeverity(cursor.getString(cursor.getColumnIndex(KEY_SEVERITY)));
		bz.setHasScreenshot(cursor.getInt(cursor.getColumnIndex(KEY_SCREENSHOT)));
		if (bz.hasScreenshot()) {
			bz.setScreenshots(cursor.getString(cursor.getColumnIndex(KEY_SCREENSHOT_PATH)));
		}
		bz.setValidity(cursor.getInt(cursor.getColumnIndex(KEY_UPLOAD))!=-1);
		bz.setUploaded(cursor.getInt(cursor.getColumnIndex(KEY_UPLOAD)));
		bz.setLogsUploaded(cursor.getInt(cursor.getColumnIndex(KEY_UPLOADLOG)));
		if (bz.logsAreUploaded()) {
			bz.setUploadDate(convertDateForJava(cursor.getInt(cursor.getColumnIndex(KEY_UPLOAD_DATE))));
		}
		bz.setCreationDate(convertDateForJava(cursor.getInt(cursor.getColumnIndex(KEY_CREATION_DATE))));

		return bz;
	}

	public boolean updateBzToUpload(String eventId) {
		ContentValues args = new ContentValues();

		args.put(KEY_UPLOAD, 1);

		return mDb.update(DATABASE_BZ_TABLE, args, KEY_ID + "='" + eventId + "'", null) > 0;
	}

	public boolean updateBzLogsToUpload(String eventId) {
		ContentValues args = new ContentValues();

		args.put(KEY_UPLOADLOG, 1);

		return mDb.update(DATABASE_BZ_TABLE, args, KEY_ID + "='" + eventId + "'", null) > 0;
	}

	public int getBzNumber() {
		Cursor mCursor;
		int count;
		try {
			mCursor = mDb.query(true, DATABASE_BZ_TABLE, new String[] {KEY_ID},
					null, null,
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

	public void updateEventsNotReadyBeforeREBOOT(String eventId) {
		String whereQuery = KEY_ROWID+" < (select "+KEY_ROWID+" from "+DATABASE_TABLE+" where "+KEY_ID+"='"+eventId
				+"')  AND " +KEY_DATA_READY + "=0";
		ContentValues args = new ContentValues();
		args.put(KEY_DATA_READY, 1);
		mDb.update(DATABASE_TABLE, args,whereQuery, null) ;
	}

	/**
	 * Add the input event in the Black events database
	 *
	 * @param event is the event to add in the Blacklisted events db
	 * @param reason is the reason set in the db "reason" column
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 * @throws SQLException
	 */
	public long addBlackEvent(Event event, String reason) throws SQLException {
		ContentValues initialValues = new ContentValues();

		if(reason.equals("RAIN")) {
			Cursor cursor = getRainEventInfo(new RainSignature(event));
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
	public Cursor fetchLastRain(Date date) throws SQLException {
		int mDate = convertDateForDb(date);
		mDate -=  RAIN_DURATION_MAX;
		String query = KEY_DATE + " < " + mDate;
		return fetchRainOfCrashesFromQuery(query);
	}

	/**
	 * @brief add a rain event in the rain of crashes database
	 *
	 * @param event must contains the input of which the rain to add is made
	 * @return the row ID of the newly inserted row(here the rain event), or -1 if
	 * an error occurred
	 */
	public long addRainEvent(Event event) {
		ContentValues initialValues = new ContentValues();
		//Get the rain signature associated to this event
		RainSignature signature = new RainSignature(event);
		initialValues.put(KEY_TYPE, signature.getType());
		initialValues.put(KEY_DATA0, signature.getData0());
		initialValues.put(KEY_DATA1, signature.getData1());
		initialValues.put(KEY_DATA2, signature.getData2());
		initialValues.put(KEY_DATA3, signature.getData3());
		initialValues.put(KEY_DATE, convertDateForDb(event.getDate()));
		initialValues.put(KEY_OCCURRENCES, 1);
		initialValues.put(KEY_LAST_FIBONACCI, BEGIN_FIBONACCI_BEFORE);
		initialValues.put(KEY_NEXT_FIBONACCI, BEGIN_FIBONACCI);
		initialValues.put(KEY_ID, event.getEventId());

		return mDb.insert(DATABASE_RAIN_OF_CRASHES_TABLE, null, initialValues);
	}

	private Cursor getRainEventInfo(RainSignature signature) throws SQLException {
		return fetchRainOfCrashesFromQuery(signature.querySignature());
	}

	/**
	 * Fetch the rain event from the rain of crashes db that matchs the input
	 * signature and generate an event RAIN with its number of occurrences
	 *
	 * @param signature is the signature of the ended rain
	 * @throws SQLException
	 */
	public void endOfRain(RainSignature signature) throws SQLException {
		Cursor cursor = getRainEventInfo(signature);

		if(cursor != null) {
			int occurences = cursor.getInt(cursor.getColumnIndex(KEY_OCCURRENCES));
			cursor.close();

			if( occurences > 0)
				EventGenerator.INSTANCE.generateEventRain(signature, occurences);
		}

	}

	public boolean resetRainEvent(RainSignature signature, Date date) throws SQLException {
		ContentValues args = new ContentValues();
		endOfRain(signature);
		args.put(KEY_OCCURRENCES, 1);
		args.put(KEY_DATE, convertDateForDb(date) );
		args.put(KEY_LAST_FIBONACCI, BEGIN_FIBONACCI_BEFORE);
		args.put(KEY_NEXT_FIBONACCI, BEGIN_FIBONACCI);
		return mDb.update(DATABASE_RAIN_OF_CRASHES_TABLE, args, signature.querySignature(), null) > 0;
	}

	public boolean updateRainEvent(RainSignature signature, Date date) throws SQLException {
		ContentValues args = new ContentValues();

		Cursor cursor = getRainEventInfo(signature);
		if(cursor != null) {
			int occurences = cursor.getInt(cursor.getColumnIndex(KEY_OCCURRENCES));
			int nextFibo = cursor.getInt(cursor.getColumnIndex(KEY_NEXT_FIBONACCI));
			int lastFibo = cursor.getInt(cursor.getColumnIndex(KEY_LAST_FIBONACCI));

			occurences++;

			args.put(KEY_DATE, convertDateForDb(date) );
			cursor.close();
			if( occurences == nextFibo) {
				args.put(KEY_NEXT_FIBONACCI, lastFibo + nextFibo);
				args.put(KEY_LAST_FIBONACCI, nextFibo);
				args.put(KEY_OCCURRENCES, 0);
				EventGenerator.INSTANCE.generateEventRain(signature, nextFibo);
			}
			else
				args.put(KEY_OCCURRENCES, occurences);


			return mDb.update(DATABASE_RAIN_OF_CRASHES_TABLE, args, signature.querySignature(), null) > 0;
		}
		return false;

	}

	public boolean deleteRainEvent(RainSignature signature) {
		String whereQuery = signature.querySignature();
		endOfRain(signature);
		return mDb.delete(DATABASE_RAIN_OF_CRASHES_TABLE, whereQuery, null) > 0;
	}

	public boolean isRainEventExist(RainSignature signature) {
		Cursor mCursor;
		boolean ret;
		try {
			mCursor = mDb.query(true, DATABASE_RAIN_OF_CRASHES_TABLE, new String[] {KEY_TYPE,KEY_DATA0,KEY_DATA1,KEY_DATA2,KEY_DATA3},
					signature.querySignature(), null,
					null, null, null, null);
			if (mCursor != null) {
				ret = mCursor.moveToFirst();
				mCursor.close();
				return ret;
			}
		} catch (SQLException e) {
			Log.e("isRainEventExist : " + e.getMessage());
		}
		return false;
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
	public boolean isInTheCurrentRain(Event event) throws SQLException {
		Date date = event.getDate();

		Cursor mCursor = getRainEventInfo(new RainSignature(event));
		if (mCursor != null) {
			int lastEvent = mCursor.getInt(mCursor.getColumnIndex(KEY_DATE));
			mCursor.close();
			int newDate = convertDateForDb(date);
			if( lastEvent <= newDate) {
				if ( (newDate - lastEvent) <=  MAX_DELAY_RAIN) {
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
	public int getLastCrashDate(CrashSignature signature) throws SQLException {

		Cursor mCursor = getRainEventInfo(new RainSignature(signature));
		if (mCursor != null) {
			int lastEvent = mCursor.getInt(mCursor.getColumnIndex(KEY_DATE));
			mCursor.close();
			return lastEvent;
		}
		return 0;
	}

	public boolean checkNewRain(Event event, int lastRain) {
		Cursor mCursor;
		int lastEvent;
		Date date = event.getDate();
		CrashSignature signature = new CrashSignature(event);
		RainSignature rainSignature = new RainSignature(event);

		if(-1 != lastRain)
			lastEvent = lastRain;
		else {
			lastEvent = convertDateForDb(date);
			lastEvent -= RAIN_DURATION_MAX;
		}
		//Fetch from events database the events with matching signature and with a matching date value
		String whereQuery = signature.querySignature() + " AND " + KEY_DATE + " > " + lastEvent;

		int count = -1;

		try {
			mCursor = mDb.query(true, "events", new String[] {"date"},
					whereQuery, null,
					null, null, "date desc", "0,"+RAIN_CRASH_NUMBER);
			if (mCursor != null) {
				count = mCursor.getCount();
				mCursor.close();
				if (count == RAIN_CRASH_NUMBER) {
					if (-1 == lastRain)
						addRainEvent(event);
					else
						resetRainEvent(rainSignature, date);
					EventGenerator.INSTANCE.generateEventRain(rainSignature, RAIN_CRASH_NUMBER);
					return true;
				}
			}
		}
		catch (SQLException e){
			Log.e("checkNewRain : " + e.getMessage());
		}
		return false;
	}

	public boolean checkNewRain(Event event) {
		return checkNewRain(event, -1);
	}

	public boolean isOriginExist(String origin) throws SQLException {
		String query = KEY_ORIGIN + " = '" + origin + "'";
		return isEventExistFromWhereQuery(query);
	}

	/**
	 * Search in database if an event having its 'origin' attribute beginning with
	 * input 'originBasename' exists.
	 * @param originBasename is a event origin basename file
	 * @return true if a maching element is found in DB. False otherwise.
	 */
	public boolean isOriginBasenameExist(String originBasename) {
		String query = KEY_ORIGIN + " LIKE '" + originBasename + "%'";
		return isEventExistFromWhereQuery(query);
	}
}
