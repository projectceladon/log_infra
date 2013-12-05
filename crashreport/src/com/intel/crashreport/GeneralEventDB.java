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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.intel.crashreport.StartServiceActivity.EVENT_FILTER;
import com.intel.crashreport.bugzilla.BZ;
import com.intel.crashreport.bugzilla.BZFile;
import com.intel.crashreport.specific.Event;
import com.intel.crashtoolserver.bean.Device;
import com.intel.phonedoctor.Constants;

public class GeneralEventDB {

	private static final int COEF_S_TO_MS = 1000;

	private static final String DATABASE_NAME = "eventlogs.db";
	private static final String DATABASE_TABLE = "events";
	private static final String DATABASE_TYPE_TABLE = "events_type";
	private static final String DATABASE_CRITICAL_EVENTS_TABLE = "critical_events";
	private static final String DATABASE_CRITICAL_TABLE = "critical_events_type";
	private static final String DATABASE_BZ_TABLE = "bz_events";
	protected static final String DATABASE_BLACK_EVENTS_TABLE = "black_events";
	protected static final String DATABASE_RAIN_OF_CRASHES_TABLE = "rain_of_crashes";
	protected static final String DATABASE_GCM_MESSAGES_TABLE = "gcm_messages";
	protected static final String DATABASE_DEVICE_TABLE = "device";
	protected static final int DATABASE_VERSION = 12;

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
	public static final String KEY_GCM_DATA = "data";
	public static final String KEY_GCM_TITLE = "title";
	public static final String KEY_GCM_TEXT = "text";
	public static final String KEY_DEVICE_SSN = "ssn";
	public static final String KEY_DEVICE_TOKEN = "gcmToken";
	public static final String KEY_DEVICE_SPID = "spid";
	public static final String KEY_LOGS_SIZE = "logsSize";
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
					KEY_PDSTATUS + " text, " +
					KEY_LOGS_SIZE + " integer);";

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

	private static final String DATABASE_GCM_MESSAGES_CREATE =
			"create table " + DATABASE_GCM_MESSAGES_TABLE + "(" +
					KEY_ROWID + " integer primary key autoincrement, " +
					KEY_GCM_TITLE + " text not null," +
					KEY_GCM_TEXT + " text," +
					KEY_TYPE + " text not null," +
					KEY_GCM_DATA + " text, " +
					KEY_DATE + " integer not null, " +
					KEY_NOTIFIED + " integer);";

	private static final String DATABASE_DEVICE_CREATE =
			"create table " + DATABASE_DEVICE_TABLE + "(" +
					KEY_DEVICEID + " text primary key, " +
					KEY_IMEI + " text not null, " +
					KEY_DEVICE_SSN + " text, " +
					KEY_DEVICE_TOKEN + " text, " +
					KEY_DEVICE_SPID + " text);";

	private DatabaseHelper mDbHelper;
	protected SQLiteDatabase mDb;

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
			db.execSQL(DATABASE_GCM_MESSAGES_CREATE);
			db.execSQL(DATABASE_DEVICE_CREATE);
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
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_GCM_MESSAGES_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_DEVICE_TABLE);
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
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_GCM_MESSAGES_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_DEVICE_TABLE);
			onCreate(db);
		}
	}

	public GeneralEventDB(Context ctx) {
		this.mCtx = ctx;
	}

	public GeneralEventDB open() throws SQLException, SQLiteException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		if (mDb != null)
			mDb.close();
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
		initialValues.put(KEY_UPLOAD, isEventLogsValid(type) ? 0 : -1); /* Set event as invalid if needed */
		initialValues.put(KEY_CRASHDIR, crashDir);
		initialValues.put(KEY_UPLOADLOG, isEventLogsValid(type) ? 0 : -1);
		initialValues.put(KEY_NOTIFIED, 0);
		if(bDataReady)
			initialValues.put(KEY_DATA_READY, 1);
		else
			initialValues.put(KEY_DATA_READY, 0);
		if (bDataReady && !crashDir.isEmpty())
			initialValues.put(KEY_LOGS_SIZE, CrashLogs.getCrashLogsSize(mCtx,crashDir,eventId));
		else
			initialValues.put(KEY_LOGS_SIZE, 0);
		initialValues.put(KEY_ORIGIN, origin);
		initialValues.put(KEY_PDSTATUS, pdStatus);
		CrashReport app = (CrashReport)mCtx;
		updateDeviceInformation(deviceId,imei,GeneralEvent.getSSN(),app.getTokenGCM(),Event.getSpid());
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
				KEY_UPLOAD, KEY_CRASHDIR, KEY_UPLOADLOG, KEY_NOTIFIED, KEY_DATA_READY, KEY_ORIGIN, KEY_PDSTATUS, KEY_LOGS_SIZE}, null, null, null, null, null);
	}

	public Cursor fetchLastNEvents(String sNlimit, EVENT_FILTER filter) {

		String sQuery = null;

		if(EVENT_FILTER.INFO == filter)
			sQuery = KEY_NAME + "<> 'STATS'";
		else if(EVENT_FILTER.CRASH == filter)
			sQuery = KEY_NAME + "= 'CRASH'";
		return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_ID, KEY_NAME, KEY_TYPE,
				KEY_DATA0, KEY_DATA1, KEY_DATA2, KEY_DATA3, KEY_DATA4, KEY_DATA5,
				KEY_DATE, KEY_BUILDID, KEY_DEVICEID, KEY_IMEI, KEY_UPTIME,
				KEY_UPLOAD, KEY_CRASHDIR, KEY_UPLOADLOG, KEY_NOTIFIED, KEY_DATA_READY, KEY_ORIGIN, KEY_PDSTATUS, KEY_LOGS_SIZE}, sQuery, null, null, null,
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
		bQuery.append(" and "+KEY_UPLOAD+"='1' and "+KEY_CRASHDIR+" != ''");
		Log.d("fetchNotUploadedLogs : Query string = " +bQuery.toString() );
		return fetchEventFromWhereQuery(bQuery.toString());

	}

	private Cursor fetchEventFromWhereQuery(String whereQuery) throws SQLException {
		Cursor mCursor;

		mCursor = mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,KEY_ID, KEY_NAME, KEY_TYPE,
				KEY_DATA0, KEY_DATA1, KEY_DATA2, KEY_DATA3, KEY_DATA4, KEY_DATA5, KEY_DATE,
				KEY_BUILDID, KEY_DEVICEID, KEY_IMEI, KEY_UPTIME, KEY_CRASHDIR,
				KEY_UPLOAD, KEY_UPLOADLOG, KEY_DATA_READY, KEY_ORIGIN, KEY_PDSTATUS, KEY_LOGS_SIZE}, whereQuery, null,
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
		event.setLogsSize(cursor.getInt(cursor.getColumnIndex(KEY_LOGS_SIZE)));


		return event;
	}

	/**
	 *
	 * @param eventId The event Id to search
	 * @return The event searched
	 * @throws SQLException
	 */
	public Cursor getEventFromId(String eventId) throws SQLException{
		String whereQuery = KEY_ID + "='" + eventId +"'";
		return fetchEventFromWhereQuery(whereQuery);
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
		StringBuilder bQuery = new StringBuilder("("+KEY_UPLOAD+"='0' and "+KEY_DATA_READY+"='1')");
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

	protected Boolean isEventExistFromWhereQuery(String whereQuery) throws SQLException {
		Cursor mCursor;
		Boolean ret;
		try{
			mCursor = mDb.query(true, DATABASE_TABLE, new String[] {KEY_ID},
					whereQuery, null,
					null, null, null, null);
			if (mCursor != null) {
				ret = mCursor.moveToFirst();
				mCursor.close();
				return ret;
			}
		}catch(NullPointerException e){
			Log.w("isEventExistsFromWhereQuery: Can't access to DB");
			throw new SQLException("Can't open DB");
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
		if(crashDir.isEmpty())
			args.put(KEY_LOGS_SIZE, 0);

		return mDb.update(DATABASE_TABLE, args, KEY_ID + "='" + eventId + "'", null) > 0;
	}

	public boolean updateEventField(String eventId, String field, String data) {
		ContentValues args = new ContentValues();

		args.put(field, data);

		return mDb.update(DATABASE_TABLE, args, KEY_ID + "='" + eventId + "'", null) > 0;
	}

	protected int convertDateForDb(Date date) {
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

	public Boolean isTypeListEmpty() throws SQLException {
		return !isTypeExistFromWhereQuery(null);
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
		} catch (NullPointerException e) {
			return 0;
		}
		return 0;
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

	/**
	 * Get all critical events or uncritical crashes depending on critical parameter value
	 * @param critical true to get critical events, false to get uncritical crashes
	 * @return critical events or uncritical crashes
	 * @throws SQLException
	 */
	public Cursor fetchNotNotifiedEvents(boolean critical) throws SQLException {
		String whereQuery = KEY_NOTIFIED+"='0' and "+
				"("+KEY_TYPE + " in (select "+KEY_TYPE+" from "+
				DATABASE_TYPE_TABLE+" where "+KEY_CRITICAL+"="+(critical?"1":"0")+")"
				+ (critical?" or":" and")+" ("+KEY_ID+(critical?"":" not")+" in ("+SELECT_CRITICAL_EVENTS_QUERY+" )))";
		return fetchEventFromWhereQuery(whereQuery);
	}

	/**
	 * Check if there is events to be notified.
	 * @param bAllCrashes true crashes events or critical events to notify, false only critical events to notify
	 * @return true if there are events to notify
	 * @throws SQLException
	 */
	public boolean isThereEventToNotify(boolean bAllCrashes) throws SQLException {

		String whereQuery;
		if (bAllCrashes){
			//all events related to known event_type are used (corresponds to crashes)
			whereQuery= KEY_NOTIFIED+"='0' and ("+
					KEY_NAME+"='CRASH' "
					+ "or ("+KEY_ID+" in ("+SELECT_CRITICAL_EVENTS_QUERY+" )))";
		}else{
			//critical only
			whereQuery= KEY_NOTIFIED+"='0' and "+
					"("+KEY_TYPE + " in (select "+KEY_TYPE+" from "+
					DATABASE_TYPE_TABLE+" where "+KEY_CRITICAL+"=1)"
					+ " or ("+KEY_ID+" in ("+SELECT_CRITICAL_EVENTS_QUERY+" )))";

		}
		return isEventExistFromWhereQuery(whereQuery);
	}

	/**
	 * Get number of events to notify
	 * @param crash true to get uncritical crashes number, false to get all critical events number
	 * @return uncritical crashes number or critical events number
	 */
	public int getEventsToNotifyNumber(boolean crash) {
		Cursor mCursor;
		int count;
		String whereQuery = KEY_NOTIFIED+"='0' and "+
				"("+KEY_TYPE + " in (select "+KEY_TYPE+" from "+
				DATABASE_TYPE_TABLE+" where "+KEY_CRITICAL+"="+(crash?"0":"1")+")"
				+ (crash?" and":" or")+" ("+KEY_ID+" "+(crash?"not ":"")+"in ("+SELECT_CRITICAL_EVENTS_QUERY+" )))";
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

	public int getCriticalEventsNumber() {
		return getEventsToNotifyNumber(false);
	}

	public int getCrashToNotifyNumber() {
		return getEventsToNotifyNumber(true);
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
		try{
			String crashDir = getEventCrashDir(eventId);
			if(!crashDir.isEmpty()){
				args.put(KEY_LOGS_SIZE, CrashLogs.getCrashLogsSize(mCtx, crashDir, eventId));
			}
		}
		catch(SQLException e){
			Log.e("GeneralEventDB:updateEventDataReady: can't access database");
		}

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
		String whereQuery = KEY_ROWID+" < (select "+KEY_ROWID+" from "+DATABASE_TABLE+" where "+KEY_ID+"='"+eventId+"')"
				+ " and "+KEY_NAME+"<> 'BZ'";
		mDb.delete(DATABASE_TABLE, whereQuery, null);
	}

	/**
	 * Get the crashlogs repository of an event
	 * @param eventId event id of the crashlogs event repository to search
	 * @return crashlogs repository
	 * @throws SQLException
	 */
	public String getEventCrashDir(String eventId) throws SQLException{
		String crashDir = "";
		Cursor cursor = mDb.query(true,DATABASE_TABLE, new String[] {KEY_CRASHDIR}, KEY_ID + "='"+eventId+"'", null, null, null,
				null,null);
		if (cursor != null) {
			if(cursor.moveToFirst())
				crashDir = cursor.getString(cursor.getColumnIndex(KEY_CRASHDIR));
			cursor.close();
		}
		return crashDir;
	}

	public Boolean isThereLogToUploadWithoutWifi(String crashTypes[]) throws SQLException {
		return isThereLogToUploadByWifi(false, crashTypes);
	}

	public Boolean isThereLogToUploadWithWifi(String crashTypes[]) throws SQLException {
		return isThereLogToUploadByWifi(true, crashTypes);
	}

	public Boolean isThereLogToUploadByWifi(boolean bWithWifi, String crashTypes[]) throws SQLException {
		StringBuilder bQuery = new StringBuilder("");
		bQuery.append("(("+KEY_NAME+" in ( " + OTHER_EVENT_NAMES +" ) and "+KEY_UPLOADLOG+"='0') or ");
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
		String sLogOperator;
		if (bWithWifi)
			sLogOperator = ">=";
		else
			sLogOperator = "<";
		bQuery.append(") and "+KEY_UPLOAD+"='1' and "+KEY_CRASHDIR + "!='' and "+ KEY_LOGS_SIZE + sLogOperator +Constants.WIFI_LOGS_SIZE);
		Log.d("isThereLogToUploadWithWifi : Query string = " +bQuery.toString() );
		return isEventExistFromWhereQuery(bQuery.toString());
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
				"where bz."+KEY_ID+" = "+"e."+KEY_ID + " order by "+KEY_CREATION_DATE+" DESC";
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

	/**
	 * Add a new GCM Message in the database
	 * @param title Title of the message
	 * @param text The text of the message
	 * @param type The type of the message
	 * @return the result of the database insertion request
	 */
	public long addGcmMessage(String title, String text, String type) {
		return addGcmMessage(title, text, type, "");
	}

	/**
	 * Add a new GCM Message in the database
	 * @param title Title of the message
	 * @param text The text of the message
	 * @param type The type of the message
	 * @param data The action to do in case of action message.
	 * @return the result of the database insertion request
	 */
	public long addGcmMessage(String title, String text, String type, String data) {
		ContentValues initialValues = new ContentValues();

		initialValues.put(KEY_GCM_TITLE, title);
		initialValues.put(KEY_GCM_TEXT, text);
		initialValues.put(KEY_TYPE, type);
		initialValues.put(KEY_GCM_DATA, data);
		initialValues.put(KEY_NOTIFIED, 0);

		Date date= new Date();
		SimpleDateFormat EVENT_DF_GEN = new SimpleDateFormat("yyyy-MM-dd/HH:mm:ss");
		String displayDate = EVENT_DF_GEN.format(date);
		try {
			EVENT_DF_GEN.setTimeZone(TimeZone.getTimeZone("GMT"));
			date = EVENT_DF_GEN.parse(displayDate);
		} catch (ParseException e) {
			date = new Date();
		}
		initialValues.put(KEY_DATE,convertDateForDb(date));

		return mDb.insert(DATABASE_GCM_MESSAGES_TABLE, null, initialValues);
	}

	/**
	 * Get the list of all GCM Messages not cancelled by the user
	 * @return GCM Messages list
	 */
	public Cursor fetchAllGcmMessages() {
		return mDb.query(DATABASE_GCM_MESSAGES_TABLE, new String[] {KEY_ROWID, KEY_GCM_TITLE, KEY_GCM_TEXT, KEY_TYPE,
				KEY_GCM_DATA,KEY_DATE,KEY_NOTIFIED}, null, null, null, null, KEY_ROWID+" DESC");
	}

	/**
	 * Get the GCM messages that the notification hasn't been cancelled.
	 * @return GCM messages not cancelled
	 * @throws SQLException
	 */
	public Cursor fetchNewGcmMessages() throws SQLException {
		Cursor mCursor;
		String whereQuery = KEY_NOTIFIED+"=0";

		mCursor = mDb.query(true, DATABASE_GCM_MESSAGES_TABLE, new String[] {KEY_ROWID, KEY_GCM_TITLE, KEY_GCM_TEXT, KEY_TYPE,
				KEY_GCM_DATA,KEY_DATE,KEY_NOTIFIED}, whereQuery, null,
				null, null, KEY_ROWID+" DESC", null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
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
	 * Delete a gcm message in the GCM_MESSAGES_TABLE
	 * @param id The row id of the message
	 * @return True if the delete works
	 */
	public boolean deleteGcmMessage(int id) {

	return mDb.delete(DATABASE_GCM_MESSAGES_TABLE, KEY_ROWID + "='" + id + "'", null) > 0;
	}

	/**
	 * Update the status of a GCM Message
	 * @param id rowId of the message
	 * @return True if the update succeed
	 */
	public boolean updateGcmMessageToCancelled(int id) {
		ContentValues args = new ContentValues();

		args.put(KEY_NOTIFIED, 1);

		return mDb.update(DATABASE_GCM_MESSAGES_TABLE, args, KEY_ROWID + "=" + id + "", null) > 0;
	}

	/**
	 * Get the number of GCM Messages which have whereQuery specification
	 * @param whereQuery GCM Messages sepecification
	 * @return number of GCM Messages
	 */
	private int getNumberFromWhereQueryForGcm(String whereQuery) {
		Cursor mCursor;
		int count;
		try {
			mCursor = mDb.query(true, DATABASE_GCM_MESSAGES_TABLE, new String[] {KEY_ROWID},
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

	/**
	 * Get the number of new GCM Messages
	 * @return number of GCM Messages
	 */
	public int getNewGcmMessagesNumber() {
		return getNumberFromWhereQueryForGcm(KEY_NOTIFIED + "='0'");
	}

	/**
	 * Get the id of the last GCM message received
	 * @return the id of the last GCM message
	 */
	public int getLastGCMRowId() {
		Cursor mCursor;
		try {
			mCursor = mDb.query(true, DATABASE_GCM_MESSAGES_TABLE, new String[] {KEY_ROWID},
					KEY_NOTIFIED + "='0'", null,
					null, null, KEY_ROWID+" DESC", "1");
			if (mCursor != null) {
				mCursor.moveToFirst();
				int rowId = mCursor.getInt(0);
				mCursor.close();
				return rowId;
			}
		} catch (SQLException e) {
			return -1;
		}
		return -1;
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

	/**
	 * Add a device in the database
	 * @param deviceId the device id of the board
	 * @param imei the imei of the board
	 * @param ssn the ssn of the board
	 * @param token the token of the board
	 * @param spid the spid of the board
	 * @return the result of the database insertion request
	 */
	public long addDevice(String deviceId, String imei, String ssn, String token, String spid) {
		ContentValues initialValues = new ContentValues();

		initialValues.put(KEY_DEVICEID, deviceId);
		initialValues.put(KEY_IMEI, imei);
		initialValues.put(KEY_DEVICE_SSN, ssn);
		initialValues.put(KEY_DEVICE_TOKEN, token);
		initialValues.put(KEY_DEVICE_SPID, spid);

		return mDb.insert(DATABASE_DEVICE_TABLE, null, initialValues);
	}

	/**
	 * Check is a device exists in the database
	 * @param deviceId The device id
	 * @return true if the device exists
	 * @throws SQLException
	 */
	protected Boolean isDeviceExist() throws SQLException {
		Cursor mCursor;
		Boolean ret;
		mCursor = mDb.query(true, DATABASE_DEVICE_TABLE, new String[] {KEY_DEVICEID},
				null, null,
				null, null, null, null);
		if (mCursor != null) {
			ret = mCursor.moveToFirst();
			mCursor.close();
			return ret;
		}
		return false;
	}

	/**
	 * Update the device information
	 * @param deviceId the device id of the board
	 * @param imei the imei of the board
	 * @param ssn the ssn of the board
	 * @param token the token of the board
	 * @param spid the spid of the board
	 * @return True if the update succeed
	 */
	public boolean updateDeviceInformation(String deviceId, String imei, String ssn, String token, String spid) {

		if(isDeviceExist()) {
			ContentValues args = new ContentValues();
			args.put(KEY_DEVICEID, deviceId);
			args.put(KEY_IMEI, imei);
			args.put(KEY_DEVICE_SSN, ssn);
			args.put(KEY_DEVICE_TOKEN, token);
			args.put(KEY_DEVICE_SPID, spid);
			return mDb.update(DATABASE_DEVICE_TABLE, args, null, null) > 0;
		}
		else {
			long result = addDevice(deviceId, imei, ssn, token, spid);
			return (result!=-1);
		}

	}

	/**
	 * Update the GCM token of the device
	 * @param gcmToken the current gcm token
	 * @return true if operation succeed
	 */
	public boolean updateDeviceToken(String gcmToken) {
		ContentValues args = new ContentValues();
		args.put(KEY_DEVICE_TOKEN, gcmToken);
		return mDb.update(DATABASE_DEVICE_TABLE, args, null, null) > 0;
	}

	/**
	 * Returns if the event log is valid or not depending on the event type.
	 * This aims to never upload very large event logs whatever the
	 * available connection type is.
	 * @param eventType is the type of the event
	 * @return true if the event log is valid by default. False otherwise.
	 */
	private static boolean isEventLogsValid( String eventType ) {
		return ( !Arrays.asList(Constants.INVALID_EVENTS).contains(eventType) );
	}
}
