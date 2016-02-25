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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.intel.crashreport.core.BZ;
import com.intel.crashreport.core.GeneralEvent;
import com.intel.crashreport.CrashLogs;

import com.intel.crashreport.common.Constants;
import com.intel.crashreport.common.Utils.EVENT_FILTER;

import java.util.List;

public class GeneralEventDB extends General {

	private static final String DATABASE_NAME = "eventlogs.db";
	protected static final String DATABASE_TABLE = "events";
	protected static final String DATABASE_BZ_TABLE = "bz_events";
	protected static final String DATABASE_BLACK_EVENTS_TABLE = "black_events";
	protected static final String DATABASE_RAIN_OF_CRASHES_TABLE = "rain_of_crashes";
	protected static final String DATABASE_GCM_MESSAGES_TABLE = "gcm_messages";
	protected static final String DATABASE_DEVICE_TABLE = "device";
	protected static final int DATABASE_VERSION = 18;

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
	public static final String KEY_VARIANT = "variant";
	public static final String KEY_INGREDIENTS = "ingredients";
	public static final String KEY_OS_BOOT_MODE = "bootMode";
	public static final String KEY_UNIQUEKEY_COMPONENT = "uniqueKeyComponents";
	public static final String KEY_MODEM_VERSION_USED = "modemVersionUsed";
	public static final String KEY_EVENT_CLEANED = "eventCleaned";
	public static final String KEY_TEST_CASE = "testCase";

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
					KEY_VARIANT + " text, " +
					KEY_INGREDIENTS + " text, " +
					KEY_OS_BOOT_MODE + " text, " +
					KEY_UNIQUEKEY_COMPONENT + " text, " +
					KEY_MODEM_VERSION_USED + " text, " +
					KEY_IMEI + " text not null, " +
					KEY_UPTIME + " text not null, " +
					KEY_UPLOAD + " integer, " +
					KEY_CRASHDIR + " text, " +
					KEY_UPLOADLOG + " integer, "+
					KEY_NOTIFIED + " integer, "+
					KEY_DATA_READY + " integer, "+
					KEY_ORIGIN + " text, " +
					KEY_PDSTATUS + " text, " +
					KEY_LOGS_SIZE + " integer, " +
					KEY_EVENT_CLEANED + " integer, " +
					KEY_CRITICAL + " integer, " +
					KEY_TEST_CASE + " text);";


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

	public static final String[] gcmTableColums = new String[] {KEY_ROWID, KEY_GCM_TITLE,
			KEY_GCM_TEXT, KEY_TYPE, KEY_GCM_DATA, KEY_DATE, KEY_NOTIFIED};

	public static final String[] deviceTableColums = new String[] {KEY_DEVICEID, KEY_IMEI,
			KEY_DEVICE_SSN, KEY_DEVICE_TOKEN, KEY_DEVICE_SPID};

	public static final String[] eventsTableColums = new String[] {KEY_ROWID, KEY_ID, KEY_NAME,
				KEY_TYPE, KEY_DATA0, KEY_DATA1, KEY_DATA2, KEY_DATA3, KEY_DATA4,
				KEY_DATA5, KEY_DATE, KEY_BUILDID, KEY_DEVICEID, KEY_VARIANT,
				KEY_INGREDIENTS, KEY_OS_BOOT_MODE, KEY_UNIQUEKEY_COMPONENT,
				KEY_MODEM_VERSION_USED, KEY_IMEI, KEY_UPTIME, KEY_UPLOAD,
				KEY_CRASHDIR, KEY_UPLOADLOG, KEY_NOTIFIED, KEY_DATA_READY,
				KEY_ORIGIN, KEY_PDSTATUS, KEY_LOGS_SIZE, KEY_EVENT_CLEANED,
				KEY_CRITICAL, KEY_TEST_CASE};

	public static final String[] eventsTableBaseColums = new String[] {KEY_ROWID, KEY_ID,
				KEY_NAME, KEY_TYPE, KEY_DATA0, KEY_DATA1, KEY_DATA2,
				KEY_DATE,KEY_CRASHDIR};

	public static final String[] eventsTableDetailColums = new String[] {KEY_ROWID, KEY_ID,
				KEY_NAME, KEY_TYPE, KEY_DATA0, KEY_DATA1, KEY_DATA2, KEY_DATE,
				KEY_CRASHDIR, KEY_DATA3, KEY_DATA4, KEY_DATA5, KEY_UPTIME,
				KEY_UPLOAD, KEY_UPLOADLOG, KEY_DATA_READY};

	public static final String[] rainTableColums = new String[] {KEY_DATE, KEY_TYPE, KEY_DATA0,
				KEY_DATA1, KEY_DATA2, KEY_DATA3,  KEY_ID, KEY_OCCURRENCES,
				KEY_LAST_FIBONACCI, KEY_NEXT_FIBONACCI};
	public static final String[] bzColums = new String[]{KEY_ID, KEY_SUMMARY,
				KEY_DESCRIPTION, KEY_SEVERITY, KEY_BZ_TYPE, KEY_BZ_COMPONENT,
				KEY_SCREENSHOT, KEY_SCREENSHOT_PATH};

	public static final List<Table> tables = Arrays.asList(
		new Table(DATABASE_TABLE, DATABASE_CREATE),
		new Table(DATABASE_BLACK_EVENTS_TABLE, DATABASE_BLACK_EVENTS_CREATE),
		new Table(DATABASE_RAIN_OF_CRASHES_TABLE, DATABASE_RAIN_CREATE),
		new Table(DATABASE_BZ_TABLE, DATABASE_BZ_CREATE),
		new Table(DATABASE_GCM_MESSAGES_TABLE, DATABASE_GCM_MESSAGES_CREATE),
		new Table(DATABASE_DEVICE_TABLE, DATABASE_DEVICE_CREATE)
	);

	public GeneralEventDB() {
		super();
	}

	public GeneralEventDB(Context ctx) {
		super(ctx, DATABASE_NAME, DATABASE_VERSION, tables);
	}

	public long addEvent(String eventId, String eventName, String type,
			String data0, String data1, String data2, String data3,
			String data4, String data5, Date date, String buildId,
			String deviceId, String imei, String uptime, String crashDir,
			boolean bDataReady, String origin, String pdStatus, String variant,
			String ingredients, String osBootMode, String uniqueKeyComponent,
			String modemVersionUsed, boolean critical, String testCase) {
		ContentValues initialValues = new ContentValues();
		int eventDate = Utils.convertDateForDb(date);
		if (eventName.isEmpty()) return -2;
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
				/* Set event as invalid if needed */
		initialValues.put(KEY_UPLOAD, Utils.isEventLogsValid(type) ? 0 : -1);
		initialValues.put(KEY_CRASHDIR, crashDir);
		initialValues.put(KEY_UPLOADLOG, Utils.isEventLogsValid(type) ? 0 : -1);
		initialValues.put(KEY_NOTIFIED, 0);
		if(bDataReady)
			initialValues.put(KEY_DATA_READY, 1);
		else
			initialValues.put(KEY_DATA_READY, 0);
		if (bDataReady && !crashDir.isEmpty())
			initialValues.put(KEY_LOGS_SIZE,
					CrashLogs.getCrashLogsSize(mCtx,crashDir,eventId));
		else
			initialValues.put(KEY_LOGS_SIZE, 0);
		initialValues.put(KEY_ORIGIN, origin);
		initialValues.put(KEY_PDSTATUS, pdStatus);
		initialValues.put(KEY_VARIANT, variant);
		initialValues.put(KEY_INGREDIENTS, ingredients);
		initialValues.put(KEY_OS_BOOT_MODE, osBootMode);
		initialValues.put(KEY_UNIQUEKEY_COMPONENT, uniqueKeyComponent);
		initialValues.put(KEY_MODEM_VERSION_USED, modemVersionUsed);
		initialValues.put(KEY_CRITICAL, critical);
		initialValues.put(KEY_TEST_CASE, testCase);

		removeOldCrashdir(crashDir);
		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	public long addEvent(GeneralEvent event) {
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
				event.getPdStatus(),
				event.getVariant(),
				event.getIngredients(),
				event.getOsBootMode(),
				event.getUniqueKeyComponent(),
				event.getModemVersionUsed(),
				event.isCritical(),
				event.getTestCase());
	}

	public Cursor fetchLastNEvents(String sNlimit, EVENT_FILTER filter) {
		String sQuery = null;

		if(EVENT_FILTER.INFO == filter)
			sQuery = KEY_NAME + "<> 'STATS'";
		else if(EVENT_FILTER.CRASH == filter)
			sQuery = KEY_NAME + "= 'CRASH'";

		return selectEntries(DATABASE_TABLE, eventsTableColums,
				sQuery, KEY_ROWID, true, sNlimit);
	}

	public Cursor fetchNotUploadedEvents() throws SQLException {
		String whereQuery = KEY_UPLOAD + "='0' and " + KEY_DATA_READY + "='1'";
		return fetchEventFromWhereQuery(whereQuery);
	}

	public Cursor fetchNotUploadedLogs(String crashTypes[]) throws SQLException {
		StringBuilder bQuery = new StringBuilder(
				"( ("+KEY_NAME+" in ( " + OTHER_EVENT_NAMES + " ) and "
				+ KEY_UPLOADLOG + "='0') or (" + KEY_NAME + "='CRASH' and "
				+ KEY_UPLOADLOG + "='0' and " + KEY_DATA_READY + "='1'");

		if (crashTypes != null) {
			String sExcludedType = getExcludeTypeInLine(crashTypes);

			if (!sExcludedType.isEmpty()) {
				bQuery.append(" and " + KEY_TYPE
						+ " not in (" + sExcludedType + ")");
			}
		}
		bQuery.append(") )");
		/* Only logs for events already uploaded*/
		bQuery.append(" and " + KEY_UPLOAD + "='1' and "
				+ KEY_CRASHDIR+" != ''");
		return fetchEventFromWhereQuery(bQuery.toString());

	}

	public Cursor fetchEventFromWhereQuery(String whereQuery) throws SQLException {
		return selectEntries(DATABASE_TABLE, eventsTableColums, whereQuery);
	}

	public GeneralEvent fillEventFromCursor(Cursor cursor) {
		GeneralEvent event = new GeneralEvent();
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
		event.setDate(Utils.convertDateForJava(
				cursor.getInt(cursor.getColumnIndex(KEY_DATE))));
		event.setBuildId(cursor.getString(cursor.getColumnIndex(KEY_BUILDID)));
		event.setDeviceId(cursor.getString(cursor.getColumnIndex(KEY_DEVICEID)));
		event.setVariant(cursor.getString(cursor.getColumnIndex(KEY_VARIANT)));
		event.setIngredients(cursor.getString(cursor.getColumnIndex(KEY_INGREDIENTS)));
		event.setUniqueKeyComponent(
				cursor.getString(cursor.getColumnIndex(KEY_UNIQUEKEY_COMPONENT)));
		event.setModemVersionUsed(
				cursor.getString(cursor.getColumnIndex(KEY_MODEM_VERSION_USED)));
		event.setOsBootMode(cursor.getString(cursor.getColumnIndex(KEY_OS_BOOT_MODE)));
		event.setImei(cursor.getString(cursor.getColumnIndex(KEY_IMEI)));
		event.setUptime(cursor.getString(cursor.getColumnIndex(KEY_UPTIME)));
		event.setCrashDir(cursor.getString(cursor.getColumnIndex(KEY_CRASHDIR)));
		event.setUploaded(cursor.getInt(cursor.getColumnIndex(KEY_UPLOAD)));
		event.setValid(cursor.getInt(cursor.getColumnIndex(KEY_UPLOAD))!=-1);
		event.setDataReady(cursor.getInt(cursor.getColumnIndex(KEY_DATA_READY))==1);
		event.setLogUploaded(cursor.getInt(cursor.getColumnIndex(KEY_UPLOADLOG)));
		event.setOrigin(cursor.getString(cursor.getColumnIndex(KEY_ORIGIN)));
		event.setPdStatus(cursor.getString(cursor.getColumnIndex(KEY_PDSTATUS)));
		event.setLogsSize(cursor.getInt(cursor.getColumnIndex(KEY_LOGS_SIZE)));
		event.setCritical(cursor.getInt(cursor.getColumnIndex(KEY_CRITICAL))==1);
		event.setTestCase(cursor.getString(cursor.getColumnIndex(KEY_TEST_CASE)));

		return event;
	}

	/**
	 *
	 * @param eventId The event Id to search
	 * @return The event searched
	 * @throws SQLException
	 */
	public Cursor getEventFromId(String eventId) throws SQLException{
		return fetchEventFromWhereQuery(KEY_ID + "='" + eventId +"'");
	}

	public Boolean isEventInDb(String eventId) throws SQLException {
		return isEventInDatabase(KEY_ID + "='" + eventId + "'");
	}

	private String getExcludeTypeInLine(String crashTypes[])
	{
		StringBuffer sExcludedType = new StringBuffer();
		for (int i=0; i<crashTypes.length; i++){
			if (i > 0){
				sExcludedType.append(",");
			}
			sExcludedType.append("'" + crashTypes[i] +"'");
		}
		return sExcludedType.toString();
	}

	public Boolean isThereEventToUpload() throws SQLException {
		String where = KEY_UPLOAD + "='0' and " + KEY_DATA_READY + "='1'";
		return isEventInDatabase(where);
	}

	public Boolean isThereEventToUpload(String crashTypes[]) throws SQLException {
		StringBuilder where = new StringBuilder("(" + KEY_UPLOAD + "='0' and "
				+ KEY_DATA_READY+"='1') or (" + KEY_NAME + " in ( "
				+ OTHER_EVENT_NAMES + " ) and " + KEY_UPLOADLOG + "='0' and "
				+ KEY_CRASHDIR + "!='' )");
		appendQueryForCrashTypes(crashTypes,where);
		return isEventInDatabase(where.toString());
	}

	public void appendQueryForCrashTypes(String crashTypes[],StringBuilder aQuery ) {

		aQuery.append(" or (" + KEY_NAME + "='CRASH' and " + KEY_UPLOADLOG + "='0' and "
				+ KEY_CRASHDIR + "!='' and " + KEY_DATA_READY + "='1' ");

		if (crashTypes != null) {
			String sExcludedType = getExcludeTypeInLine(crashTypes);

			if (!sExcludedType.isEmpty()) {
				aQuery.append(" and " + KEY_TYPE
						+ " not in(" + sExcludedType + ")");
			}
		}
		aQuery.append(")");
	}

	public int getEventNumberLogToUpload(String crashTypes[]) {
		StringBuilder bQuery = new StringBuilder(KEY_UPLOAD+"='1'");
		bQuery.append(" and( ("+KEY_NAME + " in  ( " + OTHER_EVENT_NAMES +" ) and "
				+ KEY_UPLOADLOG + "='0' and " + KEY_CRASHDIR + "!='' )");
		appendQueryForCrashTypes(crashTypes,bQuery);
		//required to close the query properly
		bQuery.append(")");
		return getNumberFromWhereQuery(bQuery.toString());
	}

	public Boolean isThereEventToUploadNoReboot() throws SQLException {
		return isEventInDatabase(KEY_UPLOAD + "='0' AND "
				+ KEY_NAME + "<>'REBOOT' AND " + KEY_DATA_READY + "='1'");
	}

	public Boolean isThereRebootToUpload() {
		return isEventInDatabase(KEY_UPLOAD + "='0' AND " + KEY_NAME + "='REBOOT'");
	}

	protected boolean isEventInDatabase(String where) throws SQLException {
		return ((getEntriesCount(DATABASE_TABLE, where) > 0) ? true : false);
	}

	public int getNewCrashNumber() {
		return getNumberFromWhereQuery(KEY_UPLOAD + "='0' AND "
				+ KEY_NAME + "='CRASH' and " + KEY_DATA_READY + "='1'");
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
		return updateEventField(eventId, KEY_UPLOAD, "1");
	}

	public boolean updateEventLogToUploaded(String eventId) {
		return updateEventField(eventId, KEY_UPLOADLOG, "1");
	}

	public boolean updateEventCrashdir(String eventId, String crashDir) {
		ContentValues args = new ContentValues();

		args.put(KEY_CRASHDIR, crashDir);
		if(crashDir.isEmpty())
			args.put(KEY_LOGS_SIZE, 0);

		return mDb.update(DATABASE_TABLE, args,
				KEY_ID + "='" + eventId + "'", null) > 0;
	}

	public boolean removeOldCrashdir(String crashDir) {
		return updateEventsOnLogPaths(crashDir, KEY_CRASHDIR, "");
	}

	public boolean updateEventsOnLogPaths(String crashDir, String field, String data) {
		ContentValues args = new ContentValues();

		if(crashDir.isEmpty())
			return false;

		args.put(field, data);
		return mDb.update(DATABASE_TABLE, args,
				KEY_CRASHDIR + "='" + crashDir + "'", null) > 0;
	}

	public boolean updateEventField(String eventId, String field, String data) {
		ContentValues args = new ContentValues();
		args.put(field, data);

		return mDb.update(DATABASE_TABLE, args, KEY_ID + "='" + eventId + "'", null) > 0;
	}

	private int getNumberFromWhereQuery(String where) {
		return getEntriesCount(DATABASE_TABLE, where);
	}

	public boolean updateEventToNotified(String eventId) {
		return updateEventField(eventId, KEY_UPLOAD, "1");
	}

	/**
	 * Get all critical events or uncritical crashes depending on critical parameter value
	 * @param critical true to get critical events, false to get uncritical crashes
	 * @return critical events or uncritical crashes
	 * @throws SQLException
	 */
	public Cursor fetchNotNotifiedEvents(boolean critical) throws SQLException {
		String whereQuery = KEY_NOTIFIED+"='0' and "
			+ KEY_CRITICAL + ((critical) ? "=1" : "<>1");
		return fetchEventFromWhereQuery(whereQuery);
	}

	/**
	 * Check if there is events to be notified.
	 * @param bAllCrashes   true crashes events or critical events to notify,
	 *			false only critical events to notify
	 * @return true if there are events to notify
	 * @throws SQLException
	 */
	public boolean isThereEventToNotify(boolean bAllCrashes) throws SQLException {

		String where = KEY_NOTIFIED+"='0' and (";

		//all events related to known event_type are used
		where += ((bAllCrashes) ? KEY_NAME+"='CRASH' or " : "")
			+ KEY_CRITICAL + "=1)";
		return isEventInDatabase(where);
	}

	/**
	 * Get number of events to notify
	 * @param crash true to get uncritical crashes number,
	 * 		false to get all critical events number
	 * @return uncritical crashes number or critical events number
	 */
	public int getEventsToNotifyNumber(boolean crash) {
		String where = KEY_NOTIFIED + "='0' and "
			+ KEY_CRITICAL + ((!crash) ? "=1" : "<>1");

		return getEntriesCount(DATABASE_TABLE, where);
	}

	public int getCriticalEventsNumber() {
		return getEventsToNotifyNumber(false);
	}

	public int getCrashToNotifyNumber() {
		return getEventsToNotifyNumber(true);
	}

	public int getNumberEventByCriticty(boolean bCritical){
		return getEntriesCount(DATABASE_TABLE,
			KEY_CRITICAL + ((bCritical) ? "=1" : "<>1"));
	}

	public boolean updateEventDataReady(String eventId) {
		ContentValues args = new ContentValues();
		Cursor cursor = selectEntries(DATABASE_TABLE, new String[] {KEY_CRASHDIR},
				KEY_ID + "='" + eventId + "'");
		String crashDir = "";

		if (cursor != null) {
			try {
				crashDir = cursor.getString(cursor.getColumnIndex(KEY_CRASHDIR));
			} catch (SQLException e) {
				crashDir = "";
			}
			cursor.close();
		}
		args.put(KEY_DATA_READY, 1);

		if(!crashDir.isEmpty())
			args.put(KEY_LOGS_SIZE,
					CrashLogs.getCrashLogsSize(mCtx, crashDir, eventId));

		return mDb.update(DATABASE_TABLE, args, KEY_ID + "='" + eventId + "'", null) > 0;
	}

	public boolean eventDataAreReady(String eventId) throws SQLException {
		String whereQuery = KEY_ID + "='" + eventId + "' and " + KEY_DATA_READY + "=1";
		return isEventInDatabase(whereQuery);
	}

	public boolean updatePDStatus(String pdStatus, String eventId) {
		return updateEventField(eventId, KEY_PDSTATUS, pdStatus);
	}

	public void deleteEventsBeforeUpdate(String eventId){
		String whereQuery = KEY_ROWID + " < (select " + KEY_ROWID + " from "
				+ DATABASE_TABLE + " where " + KEY_ID + "='" + eventId+"')"
				+ " and "+KEY_NAME+"<> 'BZ'";
		mDb.delete(DATABASE_TABLE, whereQuery, null);
	}

	public boolean isThereLogToUploadWithoutWifi(String crashTypes[])
			throws SQLException {
		return isThereLogToUploadByWifi(false, crashTypes);
	}

	public boolean isThereLogToUploadWithWifi(String crashTypes[])
			throws SQLException {
		return isThereLogToUploadByWifi(true, crashTypes);
	}

	public boolean isThereLogToUploadByWifi(boolean bWithWifi, String crashTypes[])
			throws SQLException {
		StringBuilder bQuery = new StringBuilder("(("+KEY_NAME+" in ( " + OTHER_EVENT_NAMES
				+ " ) and " + KEY_UPLOADLOG + "='0') or "
				+ "(" + KEY_NAME + "='CRASH' and "
				+ KEY_UPLOADLOG + "='0' and " + KEY_DATA_READY + "='1'");

		if (crashTypes != null) {
			String sExcludedType = getExcludeTypeInLine(crashTypes);

			if (!sExcludedType.isEmpty()) {
				bQuery.append(" and " + KEY_TYPE
						+ " not in (" + sExcludedType + ")");
			}
		}
		bQuery.append(")");

		/* Only logs for events already uploaded*/
		String sLogOperator;
		if (bWithWifi)
			sLogOperator = ">=";
		else
			sLogOperator = "<";
		bQuery.append(") and " + KEY_UPLOAD + "='1' and " + KEY_CRASHDIR + "!='' and "
				+ KEY_LOGS_SIZE + sLogOperator
				+ com.intel.crashreport.common.Utils.WIFI_LOGS_SIZE);
		return isEventInDatabase(bQuery.toString());
	}

	public long addBZ(BZ bz) {
		return addBZ(bz.getEventId(), bz.getSummary(), bz.getDescription(), bz.getType(),
				bz.getSeverity(), bz.getComponent(), bz.getScreenshotsToString(),
				bz.getCreationDate());
	}

	public long addBZ(String eventId, String summary, String description,
			String type, String severity, String component,
			String screenshotPath, Date creationDate) {
		ContentValues initialValues = new ContentValues();

		initialValues.put(KEY_ID, eventId);
		initialValues.put(KEY_SUMMARY, summary);
		initialValues.put(KEY_DESCRIPTION, description);
		initialValues.put(KEY_SEVERITY, severity);
		initialValues.put(KEY_BZ_TYPE, type);
		initialValues.put(KEY_BZ_COMPONENT, component);
		if (screenshotPath.isEmpty()) {
			initialValues.put(KEY_SCREENSHOT, 0);
		}
		else {
			initialValues.put(KEY_SCREENSHOT, 1);
			initialValues.put(KEY_SCREENSHOT_PATH, screenshotPath);
		}
		initialValues.put(KEY_CREATION_DATE, Utils.convertDateForDb(creationDate));

		return mDb.insert(DATABASE_BZ_TABLE, null, initialValues);
	}

	public boolean deleteBZ(String eventId) {
		return mDb.delete(DATABASE_BZ_TABLE, KEY_ID + "='" + eventId + "'", null) > 0;
	}

	public Cursor fetchAllBZs() {
		Cursor cursor;
		String whereQuery = "Select "
				+ "bz." + KEY_ID + " as " + KEY_ID + ", " + KEY_SUMMARY + ", "
				+ KEY_DESCRIPTION + ", " + KEY_SEVERITY + ", " + KEY_BZ_TYPE + ", "
				+ KEY_BZ_COMPONENT + ", " + KEY_SCREENSHOT + ", "
				+ KEY_UPLOAD + ", " + KEY_UPLOADLOG + ", " + KEY_UPLOAD_DATE +", "
				+ KEY_CREATION_DATE + ", " + KEY_SCREENSHOT_PATH
				+ " from " + DATABASE_TABLE + " e," + DATABASE_BZ_TABLE + " bz "
				+ "where bz." + KEY_ID + " = e." + KEY_ID
				+ " order by " + KEY_CREATION_DATE + " DESC";
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
			bz.setScreenshots(cursor.getString(
				cursor.getColumnIndex(KEY_SCREENSHOT_PATH)));
		}
		bz.setValidity(cursor.getInt(cursor.getColumnIndex(KEY_UPLOAD))!=-1);
		bz.setUploaded(cursor.getInt(cursor.getColumnIndex(KEY_UPLOAD)));
		bz.setLogsUploaded(cursor.getInt(cursor.getColumnIndex(KEY_UPLOADLOG)));
		if (bz.logsAreUploaded()) {
			bz.setUploadDate(Utils.convertDateForJava(
				cursor.getInt(cursor.getColumnIndex(KEY_UPLOAD_DATE))));
		}
		bz.setCreationDate(Utils.convertDateForJava(
				cursor.getInt(cursor.getColumnIndex(KEY_CREATION_DATE))));

		return bz;
	}

	public boolean updateBzToUpload(String eventId) {
		ContentValues args = new ContentValues();
		args.put(KEY_UPLOAD, 1);

		return mDb.update(DATABASE_BZ_TABLE, args,
				KEY_ID + "='" + eventId + "'", null) > 0;
	}

	public boolean updateBzLogsToUpload(String eventId) {
		ContentValues args = new ContentValues();
		args.put(KEY_UPLOADLOG, 1);

		return mDb.update(DATABASE_BZ_TABLE, args,
				KEY_ID + "='" + eventId + "'", null) > 0;
	}

	public int getBzNumber() {
		return getEntriesCount(DATABASE_BZ_TABLE);
	}

	public void updateEventsNotReadyBeforeREBOOT(String eventId) {
		String where = KEY_ROWID + " < (select " + KEY_ROWID
				+ " from " + DATABASE_TABLE
				+ " where " + KEY_ID + "='" + eventId
				+ "')  AND " + KEY_DATA_READY + "=0";

		ContentValues args = new ContentValues();
		args.put(KEY_DATA_READY, 1);
		mDb.update(DATABASE_TABLE, args, where, null);
	}

	public boolean isOriginExist(String origin) throws SQLException {
		String query = KEY_ORIGIN + " = '" + origin + "'";
		return isEventInDatabase(query);
	}

	/**
	 * Search in database if an event having its 'origin' attribute beginning with
	 * input 'originBasename' exists.
	 * @param originBasename is a event origin basename file
	 * @return true if a maching element is found in DB. False otherwise.
	 */
	public boolean isOriginBasenameExist(String originBasename) {
		String query = KEY_ORIGIN + " LIKE '" + originBasename + "%'";
		return isEventInDatabase(query);
	}

	/**
	 * Add a new GCM Message in the database
	 * @param title Title of the message
	 * @param text The text of the message
	 * @param type The type of the message
	 *
	 * @return the result of the database insertion request
	 */
	public long addGcmMessage(String title, String text, String type) {
		return addGcmMessage(title, text, type, "", null);
	}

	/**
	 * Add a new GCM Message in the database
	 * @param title Title of the message
	 * @param text The text of the message
	 * @param type The type of the message
	 * @param data The action to do in case of action message.
	 * @param date The message date
	 *
	 * @return the result of the database insertion request
	 */
	public long addGcmMessage(String title, String text, String type,
			String data, Date date) {
		ContentValues initialValues = new ContentValues();

		initialValues.put(KEY_GCM_TITLE, title);
		initialValues.put(KEY_GCM_TEXT, text);
		initialValues.put(KEY_TYPE, type);
		initialValues.put(KEY_GCM_DATA, data);
		initialValues.put(KEY_NOTIFIED, 0);

		if(date == null) {
			date= new Date();
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd/HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		String displayDate = dateFormat.format(date);
		try {
			date = dateFormat.parse(displayDate);
		} catch (ParseException e) {
			date = new Date();
		}
		initialValues.put(KEY_DATE, Utils.convertDateForDb(date));

		return mDb.insert(DATABASE_GCM_MESSAGES_TABLE, null, initialValues);
	}

	/**
	 * Get the list of all GCM Messages not cancelled by the user
	 * @return GCM Messages list
	 */
	public Cursor fetchAllGcmMessages() {
		return selectEntries(DATABASE_GCM_MESSAGES_TABLE,
				gcmTableColums, null, KEY_ROWID, true);
	}

	/**
	 * Get the GCM messages that the notification hasn't been cancelled.
	 * @return GCM messages not cancelled
	 * @throws SQLException
	 */
	public Cursor fetchNewGcmMessages() throws SQLException {
		return selectEntries(DATABASE_GCM_MESSAGES_TABLE,
				gcmTableColums, KEY_NOTIFIED + "=0", KEY_ROWID, true);
	}

	/**
	 * Returns the number of unread GCM messages.
	 *
	 * @return the number of unread messages.
	 */
	public long getUnreadGcmMessageCount() {
		return getEntriesCount(DATABASE_GCM_MESSAGES_TABLE, KEY_NOTIFIED + "=0");
	}

	/**
	 * Returns the total number of GCM messages.
	 *
	 * @return the number of messages.
	 */
	public long getTotalGcmMessageCount() {
		return getEntriesCount(DATABASE_GCM_MESSAGES_TABLE);
	}

	/**
	 * Delete a gcm message in the GCM_MESSAGES_TABLE
	 * @param id The row id of the message
	 * @return True if the delete works
	 */
	public boolean deleteGcmMessage(int id) {
		return mDb.delete(DATABASE_GCM_MESSAGES_TABLE,
			KEY_ROWID + "='" + id + "'", null) > 0;
	}

	/**
	 * Update the status of a GCM Message
	 * @param id rowId of the message
	 * @return True if the update succeed
	 */
	public boolean updateGcmMessageToCancelled(int id) {
		ContentValues args = new ContentValues();
		args.put(KEY_NOTIFIED, 1);

		return mDb.update(DATABASE_GCM_MESSAGES_TABLE, args,
				KEY_ROWID + "=" + id + "", null) > 0;
	}

	/**
	 * Mark all GCM messages as read.
	 *
	 * @return the numbers of rows affected
	 */
	public boolean markAllGcmMessagesAsRead() {
		ContentValues args = new ContentValues();
		args.put(KEY_NOTIFIED, 1);

		return mDb.update(DATABASE_GCM_MESSAGES_TABLE, args, null, null) > 0;
	}

	/**
	 * Get the number of GCM Messages which have whereQuery specification
	 * @param whereQuery GCM Messages sepecification
	 * @return number of GCM Messages
	 */
	private int getNumberFromWhereQueryForGcm(String whereQuery) {
		return getEntriesCount(DATABASE_GCM_MESSAGES_TABLE, whereQuery);
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
	 * or -1 on error.
	 */
	public int getLastGCMRowId() {
		int rowId;
		Cursor cursor = selectEntries(DATABASE_GCM_MESSAGES_TABLE,
			new String[] {KEY_ROWID}, KEY_NOTIFIED + "='0'", KEY_ROWID, true, "1");

		if (cursor == null)
			return -1;

		try {
			rowId = cursor.getInt(cursor.getColumnIndex(KEY_ROWID));
		} catch (SQLException e) {
			rowId = -1;
		}

		cursor.close();
		return rowId;
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
	public long addDevice(String deviceId, String imei, String ssn,
			String token, String spid) {
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
		return ((getEntriesCount(DATABASE_DEVICE_TABLE) > 0) ? true : false);
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
	public boolean updateDeviceInformation(String deviceId, String imei,
			String ssn, String token, String spid) {
		if(isDeviceExist()) {
			ContentValues args = new ContentValues();
			args.put(KEY_DEVICEID, deviceId);
			args.put(KEY_IMEI, imei);
			args.put(KEY_DEVICE_SSN, ssn);
			args.put(KEY_DEVICE_TOKEN, token);
			args.put(KEY_DEVICE_SPID, spid);
			return mDb.update(DATABASE_DEVICE_TABLE, args, null, null) > 0;
		}

		return (addDevice(deviceId, imei, ssn, token, spid) != -1);
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

	public Cursor fetchMatchingLogPaths(String logsDir) {
		return selectEntries(DATABASE_TABLE, new String[] {KEY_CRASHDIR},
				KEY_CRASHDIR + " like '" + logsDir + "%'");
	}

	public boolean updateEventFolderPath(String orginal, String target) {
		return updateEventsOnLogPaths(orginal, KEY_CRASHDIR, target);
	}

	public boolean isEventLogCleaned(String eventID) {
		Cursor cursor = selectEntries(DATABASE_TABLE, new String[] {KEY_EVENT_CLEANED},
				KEY_ID + "='" + eventID + "'");
		boolean status = false;

		if (cursor == null)
			return status;

		status = (cursor.getInt(cursor.getColumnIndex(KEY_EVENT_CLEANED)) != 0);
		cursor.close();
		return status;
	}

	public boolean setEventLogCleaned( String eventPath ) {
		return updateEventsOnLogPaths(eventPath, KEY_EVENT_CLEANED, "");
	}

	public String getLogDirByEventId(String eventId) {
		Cursor cursor = selectEntries(DATABASE_TABLE, new String[] {KEY_CRASHDIR},
				KEY_ID + "='" + eventId + "'");
		String crashDir = "";

		if (cursor != null) {
			try {
				crashDir = cursor.getString(cursor.getColumnIndex(KEY_CRASHDIR));
			} catch (SQLException e) {
				crashDir = "";
			}
			cursor.close();
		}

		return crashDir;
	}
}
