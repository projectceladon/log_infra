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

package com.intel.commands.crashinfo;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.intel.commands.crashinfo.option.OptionData;
import com.intel.commands.crashinfo.subcommand.GetEvent;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.SystemProperties;

import java.text.ParseException;
import java.util.TimeZone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;

import com.intel.crashtoolserver.bean.Event;
import com.intel.crashtoolserver.bean.FileInfo;
import com.intel.crashtoolserver.bean.Build;
import com.intel.crashtoolserver.bean.Device;
import java.lang.reflect.Modifier;

public class DBManager implements Closeable{
	private static final int FIELD_NUMBER = 4;
	private static final int COEF_S_TO_MS = 1000;
	private final static SimpleDateFormat PARSE_DF = new SimpleDateFormat("yyyy-MM-dd/HH:mm:ss");
	private final static String SEPARATOR = " | ";

	private static final String PATH_TO_DB ="/data/data/com.intel.crashreport/databases/eventlogs.db";
	public static final String KEY_TYPE = "type";
	private static final String DATABASE_TYPE_TABLE = "events_type";
	private static final String DATABASE_EVENTS_TABLE = "events";
	private static final String DATABASE_CRITICAL_EVENTS_TABLE = "critical_events";
	private static final String DATABASE_BZ_TABLE = "bz_events";
	private static final String DATABASE_DEVICE_TABLE = "device";
	private static final String OS = "Android";
	public static final String KEY_CRITICAL = "critical";

	public static final String KEY_ROWID = "_id";
	public static final String KEY_ID = "eventId";
	public static final String KEY_NAME = "eventName";
	public static final String KEY_DATA0 = "data0";
	public static final String KEY_DATA1 = "data1";
	public static final String KEY_DATA2 = "data2";
	public static final String KEY_DATA3 = "data3";
	public static final String KEY_DATA4 = "data4";
	public static final String KEY_DATA5 = "data5";
	public static final String KEY_DATE = "date";
	public static final String KEY_BUILDID = "buildId";
	public static final String KEY_UPTIME = "uptime";
	public static final String KEY_UPLOAD = "uploaded";
	public static final String KEY_CRASHDIR = "crashdir";
	public static final String KEY_UPLOADLOG = "logsuploaded";
	public static final String KEY_NOTIFIED = "notified";
	public static final String KEY_DATA_READY = "dataReady";
	public static final String KEY_SUMMARY = "summary";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_SEVERITY = "severity";
	public static final String KEY_BZ_TYPE = "bzType";
	public static final String KEY_BZ_COMPONENT = "bzComponent";
	public static final String KEY_SCREENSHOT = "screenshot";
	public static final String KEY_SCREENSHOT_PATH = "screenshotPath";
	public static final String KEY_CREATION_DATE = "creationDate";
	public static final String KEY_UPLOAD_DATE = "uploadDate";
	public static final String KEY_PDSTATUS = "pdStatus";
	public static final String KEY_DEVICEID = "deviceId";
	public static final String KEY_VARIANT = "variant";
	public static final String KEY_INGREDIENTS = "ingredients";
	public static final String KEY_OS_BOOT_MODE = "bootMode";
	public static final String KEY_IMEI = "imei";
	public static final String KEY_SSN = "ssn";
	public static final String KEY_GCM_TOKEN = "gcmToken";
	public static final String KEY_SPID = "spid";
	public static final String KEY_UNIQUEKEY_COMPONENT = "uniqueKeyComponents";
	public static final String KEY_MODEM_VERSION_USED = "modemVersionUsed";
	private static final String PATTERN_ISO8601_Z = "yyyy-MM-dd'T'HH:mm:ssZ";

	private static final String SELECT_CRITICAL_EVENTS_QUERY = "select "+KEY_ID+" from "+DATABASE_EVENTS_TABLE+" e,"+DATABASE_CRITICAL_EVENTS_TABLE+" ce"
			+" where ce."+KEY_TYPE+"=e."+KEY_TYPE+" and trim(e."+KEY_DATA0+")=ce."+KEY_DATA0+" and "
			+"(ce."+KEY_DATA1+"='' or ce."+KEY_DATA1+"=trim(e."+KEY_DATA1+")) and "
			+"(ce."+KEY_DATA2+"='' or ce."+KEY_DATA2+"=trim(e."+KEY_DATA2+")) and "
			+"(ce."+KEY_DATA3+"='' or ce."+KEY_DATA3+"=trim(e."+KEY_DATA3+")) and "
			+"(ce."+KEY_DATA4+"='' or ce."+KEY_DATA4+"=trim(e."+KEY_DATA4+")) and "
			+"(ce."+KEY_DATA5+"='' or ce."+KEY_DATA5+"=trim(e."+KEY_DATA5+"))";

	public Cursor fetchAllBZs() {
		Cursor cursor = null;
		String whereQuery = "Select bz."+KEY_ID+" as "+KEY_ID+", "+KEY_SUMMARY+" as "+KEY_SUMMARY+", "+KEY_DESCRIPTION+" as "+KEY_DESCRIPTION+", "+
				KEY_SEVERITY+" as "+KEY_SEVERITY+", "+KEY_BZ_TYPE+" as "+KEY_BZ_TYPE+", "+KEY_BZ_COMPONENT+" as "+KEY_BZ_COMPONENT+", "+KEY_SCREENSHOT+" as "+KEY_SCREENSHOT+", "+
				KEY_UPLOAD+" as "+KEY_UPLOAD+", "+KEY_UPLOADLOG+" as "+KEY_UPLOADLOG+", "+
				KEY_UPLOAD_DATE+" as "+KEY_UPLOAD_DATE+", "+KEY_CREATION_DATE+" as "+KEY_CREATION_DATE+", "+KEY_SCREENSHOT_PATH+ " as "+KEY_SCREENSHOT_PATH+" from "+DATABASE_EVENTS_TABLE+" e,"+DATABASE_BZ_TABLE+" bz "+
				"where bz."+KEY_ID+" = "+"e."+KEY_ID;

		cursor = myDB.rawQuery(whereQuery, null);
		if (cursor != null)
			cursor.moveToFirst();
		return cursor;
	}

	public static enum EventLevel{BASE,DETAIL,FULL};

	/*Define possible values for event uploaded/logUploaded state in DB*/
	public enum eventUploadState {EVENT_UPLOADED, LOG_UPLOADED, EVENT_INVALID, LOG_INVALID};

	/* Define output format types*/
	public static enum outputFormat {STANDARD, JSON};

	private SQLiteDatabase myDB;

	public DBManager() {
		super();
		createDB(false);
	}

	public DBManager(boolean bWriteOnDB) {
		createDB(bWriteOnDB);
	}

	protected void finalize() throws Throwable {
		try {
			close();
		} finally {
			super.finalize();
		}
	}

	private void createDB(boolean bWriteOnDB){
		int iOpenFlag;
		if (bWriteOnDB){
			iOpenFlag = SQLiteDatabase.OPEN_READWRITE;
		}else{
			iOpenFlag = SQLiteDatabase.OPEN_READONLY;
		}

		try{
			myDB = SQLiteDatabase.openDatabase(
					PATH_TO_DB
					, null
					, iOpenFlag
					);
		} catch (SQLException e) {
			myDB = null;
			System.err.println("Database could not be opened.");
		}
	}

	public int getVersion(){
		return myDB.getVersion();
	}

	public int getNumberEventByCriticty(boolean bCritical){
		Cursor mCursor;
		int iResult = -1;
		String sWhereQuery;

		if (bCritical){
			sWhereQuery = KEY_TYPE + " in (select "+KEY_TYPE+" from "+
					DATABASE_TYPE_TABLE+" where "+KEY_CRITICAL+"=1)"
					+ "or ("+KEY_ID+" in ("+SELECT_CRITICAL_EVENTS_QUERY+" ))";
		}else{
			sWhereQuery = KEY_TYPE + " not in (select "+KEY_TYPE+" from "+
					DATABASE_TYPE_TABLE+" where "+KEY_CRITICAL+"=1)"
					+ "and ("+KEY_ID+" not in ("+SELECT_CRITICAL_EVENTS_QUERY+" ))";
		}

		mCursor = myDB.query(true, DATABASE_EVENTS_TABLE, new String[] {KEY_ID},
				sWhereQuery, null,
				null, null, null, null);
		if (mCursor != null) {
			iResult = mCursor.getCount();
			mCursor.close();
		}
		return iResult;
	}

	private String appendSqlStatementAND(String sSql, String sToAppend)
	{
		if (sSql != null){
			if (sSql.equals("")){
				sSql += sToAppend;
			}else{
				sSql += " AND " + sToAppend;
			}
		}
		return sSql;
	}


	public void getBz() throws Exception
	{
		Cursor mCursor;
		try {
			//Defining columns to return
			String[] listColumns;
			listColumns = new String[] {KEY_ID,KEY_SUMMARY,KEY_DESCRIPTION,KEY_SEVERITY,KEY_BZ_TYPE,KEY_BZ_COMPONENT,KEY_SCREENSHOT,KEY_SCREENSHOT_PATH};
			mCursor = fetchAllBZs();

			//header
			if (mCursor != null) {
				int [] indexListColumns = new int[listColumns.length];
				String sHeader="";
				for (int i = 0; i < indexListColumns.length; i++) {
					indexListColumns[i] = mCursor.getColumnIndex(listColumns[i]);
					if (i==0){
						sHeader = listColumns[i];
					}else{
						sHeader += " | " + listColumns[i];
					}
				}
				System.out.println(sHeader);
				//content
				while (!mCursor.isAfterLast()) {
					String sLine="";
					for (int i = 0; i < listColumns.length; i++) {
						String sColValue ="";
						sColValue = mCursor.getString(indexListColumns[i]);
						if (i==0){
							sLine = sColValue;
						}else{
							sLine += " | " + sColValue;
						}
					}
					System.out.println(sLine);
					mCursor.moveToNext();
				}
				mCursor.close();
			}
		} catch (SQLException e) {
			System.err.println( "count SQLException");
		}
	}

	protected static Date convertDateForJava(int date) {
		long dateLong = date;
		dateLong = dateLong * COEF_S_TO_MS;
		return new Date(dateLong);
	}

	protected static String getProperty(String name) {
		String property = "";
		try {
			property = SystemProperties.get(name, "");
		} catch (IllegalArgumentException e) {}

		return property;
	}

	static private List<String> parseUniqueKey(String aKey) {
		List<String> resultList = new ArrayList<String>();
		String filteredKey = aKey.replaceAll("\\[", "" );
		filteredKey = filteredKey.replaceAll("\\]", "" );
		String[] tmpList = filteredKey.split(", ");
		for (String retval:tmpList) {
			resultList.add(retval);
		}
		return resultList;
	}

	private Event fillEventFromCursor(Cursor cursor) {
		String bootMode = cursor.getString(cursor.getColumnIndex(KEY_OS_BOOT_MODE));
		String buildID = cursor.getString(cursor.getColumnIndex(KEY_BUILDID));
		String eventId = cursor.getString(cursor.getColumnIndex(KEY_ID));
		String eventName = cursor.getString(cursor.getColumnIndex(KEY_NAME));
		String type = cursor.getString(cursor.getColumnIndex(KEY_TYPE));
		String data0 = cursor.getString(cursor.getColumnIndex(KEY_DATA0));
		String data1 = cursor.getString(cursor.getColumnIndex(KEY_DATA1));
		String data2 = cursor.getString(cursor.getColumnIndex(KEY_DATA2));
		String data3 = cursor.getString(cursor.getColumnIndex(KEY_DATA3));
		String data4 = cursor.getString(cursor.getColumnIndex(KEY_DATA4));
		String data5 = cursor.getString(cursor.getColumnIndex(KEY_DATA5));
		String crashdir = cursor.getString(cursor.getColumnIndex(KEY_CRASHDIR));
		String ingredients = cursor.getString(cursor.getColumnIndex(KEY_INGREDIENTS));
		String uniqueKeyComponents = cursor.getString(cursor.getColumnIndex(KEY_UNIQUEKEY_COMPONENT));
		Date date = convertDateForJava(cursor.getInt(cursor.getColumnIndex(KEY_DATE)));
		String pdStatus = cursor.getString(cursor.getColumnIndex(KEY_PDSTATUS));
		long lUptime = convertUptime(cursor.getString(cursor.getColumnIndex(KEY_UPTIME)));
		int rowID = cursor.getInt(cursor.getColumnIndex(KEY_ROWID));
		String modemVersionUsed = cursor.getString(cursor.getColumnIndex(KEY_MODEM_VERSION_USED));
		int uploaded = (cursor.getInt(cursor.getColumnIndex(KEY_UPLOAD)));
		int logsuploaded = (cursor.getInt(cursor.getColumnIndex(KEY_UPLOADLOG)));
		String variant = cursor.getString(cursor.getColumnIndex(KEY_VARIANT));

		Build sBuild = new Build();
		if (buildID != null && buildID.contains(",")) {
			String buildFields[] = buildID.split(",");
			if ((buildFields != null) && (buildFields.length >= FIELD_NUMBER)) {
				sBuild.setBuildId(buildFields[0]);
				sBuild.setFingerPrint(buildFields[1]);
				sBuild.setKernelVersion(buildFields[2]);
				sBuild.setBuildUserHostname(buildFields[3]);
				if (buildFields.length > FIELD_NUMBER)
					sBuild.setOs(buildFields[4]);
			}
		}
		else
			sBuild.setBuildId(buildID);

		sBuild.setVariant(variant);

		sBuild.setIngredientsJson(ingredients);
		sBuild.setUniqueKeyComponents(parseUniqueKey(uniqueKeyComponents));
		sBuild.setOrganization(com.intel.parsing.ParsableEvent.ORGANIZATION_MCG);

		Cursor mCursor;
		mCursor = fetchDeviceInfo();

		String imei = mCursor.getString(mCursor.getColumnIndex(KEY_IMEI));
		String deviceId = mCursor.getString(mCursor.getColumnIndex(KEY_DEVICEID));
		String sSSN = mCursor.getString(mCursor.getColumnIndex(KEY_SSN));
		String sSPID = mCursor.getString(mCursor.getColumnIndex(KEY_SPID));
		String sTokenGCM = mCursor.getString(mCursor.getColumnIndex(KEY_GCM_TOKEN));
                mCursor.close();

		if (sSSN.equals("")) sSSN = null;
		Device aDevice = new Device(deviceId, imei, sSSN, sTokenGCM, sSPID);

		Event event = new Event(eventId, eventName, type, data0, data1, data2, data3,
					data4, data5, date, lUptime, null /*logfile*/, sBuild,
					Event.Origin.CLOTA, aDevice, rowID, pdStatus, bootMode,
					new com.intel.crashtoolserver.bean.Modem(modemVersionUsed),
					crashdir, uploaded, logsuploaded);
		return event;
	}

	private Cursor fetchEventFromWhereQuery(String whereQuery) throws SQLException {
		Cursor mCursor;

		mCursor = myDB.query(true, DATABASE_EVENTS_TABLE, new String[] {KEY_ROWID,KEY_ID, KEY_NAME, KEY_TYPE,
				KEY_DATA0, KEY_DATA1, KEY_DATA2, KEY_DATA3, KEY_DATA4, KEY_DATA5, KEY_DATE,
				KEY_BUILDID, KEY_DEVICEID, KEY_VARIANT, KEY_INGREDIENTS,
				KEY_OS_BOOT_MODE, KEY_UNIQUEKEY_COMPONENT, KEY_MODEM_VERSION_USED, KEY_IMEI, KEY_UPTIME,
				KEY_CRASHDIR, KEY_UPLOAD, KEY_UPLOADLOG, KEY_DATA_READY, KEY_PDSTATUS},
				whereQuery, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public List<Event> getEvent(String sId)
	{
		List<Event> curEvent = null;
		try {
			Cursor cursor = fetchEventFromWhereQuery(KEY_ID + " like '%" + sId +"%'");

			if (cursor == null)
				return curEvent;

			curEvent = new ArrayList<Event>();
			while (!cursor.isAfterLast()) {
				curEvent.add(fillEventFromCursor(cursor));
				cursor.moveToNext();
			}
			cursor.close();
		} catch (SQLException e) {
			System.err.println( "count SQLException");
		}

		return curEvent;
	}

	public List<FileInfo> getFileInfo(String sId, File cacheDir)
	{
		List<FileInfo> fileInfo = null;
		Event curEvent = null;
		try {
			Cursor cursor = fetchEventFromWhereQuery(KEY_ID + " like '%" + sId +"%'");

			if (cursor == null)
				return fileInfo;

			fileInfo = new ArrayList<FileInfo>();
			while (!cursor.isAfterLast()) {
				File crashLogs = null;
				String eventId = cursor.getString(cursor.getColumnIndex(KEY_ID));
				String crashdir = cursor.getString(cursor.getColumnIndex(KEY_CRASHDIR));
				int iDate = cursor.getInt(cursor.getColumnIndex(KEY_DATE));
				cursor.moveToNext();

				try { crashLogs = FileOperations.getCrashLogsFile(cacheDir, crashdir, eventId); }
				catch (IOException e) {}

				if (crashLogs != null) {
					Date date = convertDateForJava(iDate);
					SimpleDateFormat gmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
					gmt.setTimeZone(TimeZone.getTimeZone("GMT"));
					String dayDate = gmt.format(date).toString();

					fileInfo.add(new FileInfo(
							crashLogs.getName(),
							crashLogs.getAbsolutePath(),
							crashLogs.length(),
							dayDate,
							eventId));
				}
			}
			cursor.close();

		} catch (SQLException e) {
			System.err.println( "count SQLException");
		}

		return fileInfo;
	}

	public void getEvent(EventLevel aLevel, ArrayList<OptionData> mySubOptions) throws Exception
	{
		Cursor mCursor;
		String[] listColumns;
		String sSelection = "";
		String sLimit = null;
		boolean bChangeOrder = false;
		boolean bUseHeader = false;
		boolean headerToPrint = true;
		outputFormat format = outputFormat.STANDARD;

		try {
			//Defining columns to return
			switch(aLevel){
			case BASE :
				listColumns = new String[] {KEY_ROWID,KEY_ID, KEY_NAME,KEY_TYPE,KEY_DATA0,KEY_DATA1,KEY_DATA2, KEY_DATE,KEY_CRASHDIR};
				break;
			case DETAIL :
				listColumns = new String[] {KEY_ROWID,KEY_ID, KEY_NAME,KEY_TYPE,KEY_DATA0,KEY_DATA1,KEY_DATA2, KEY_DATE,
						KEY_CRASHDIR,KEY_DATA3,KEY_DATA4,KEY_DATA5,KEY_UPTIME,KEY_UPLOAD,KEY_UPLOADLOG,KEY_DATA_READY};
				break;
			case FULL :
				listColumns = new String[] {KEY_ROWID,KEY_ID, KEY_NAME,KEY_TYPE,KEY_DATA0,KEY_DATA1,KEY_DATA2, KEY_DATE,
						KEY_CRASHDIR,KEY_DATA3,KEY_DATA4,KEY_DATA5,KEY_UPTIME,KEY_UPLOAD,KEY_UPLOADLOG,KEY_BUILDID,
						KEY_DEVICEID, KEY_VARIANT, KEY_INGREDIENTS, KEY_OS_BOOT_MODE, KEY_UNIQUEKEY_COMPONENT,
						KEY_MODEM_VERSION_USED,KEY_IMEI,KEY_NOTIFIED,KEY_DATA_READY,KEY_PDSTATUS};
				break;
			default :
				listColumns = new String[]{};
			}

			int [] indexListColumns = new int[listColumns.length];

			//Defining selection depending on options
			for (OptionData aSubOption : mySubOptions) {
				if (aSubOption.getKey().equals(GetEvent.OPTION_LAST)){
					sLimit = "1";
				}else if (aSubOption.getKey().equals(GetEvent.OPTION_REVERSE)){
					bChangeOrder = true;
				}else if (aSubOption.getKey().equals(GetEvent.OPTION_HEADER)){
					bUseHeader = true;
				}else if (aSubOption.getKey().equals(GetEvent.OPTION_ID)){
					String sTmpValue =  aSubOption.getValues(0);
					if (sTmpValue != null){
						sSelection = appendSqlStatementAND( sSelection, KEY_ROWID + "=" + sTmpValue);
					}
				}else if (aSubOption.getKey().equals(GetEvent.OPTION_TYPE)){
					String sTmpValue =  aSubOption.getValues(0);
					if (sTmpValue != null){
						sSelection = appendSqlStatementAND( sSelection, KEY_TYPE + "=" + "'" + sTmpValue + "'" );
					}
				}else if (aSubOption.getKey().equals(GetEvent.OPTION_NAME)){
					String sTmpValue =  aSubOption.getValues(0);
					if (sTmpValue != null){
						sSelection = appendSqlStatementAND( sSelection, KEY_NAME + "=" + "'" + sTmpValue + "'" );
					}
				}else if (aSubOption.getKey().equals(GetEvent.OPTION_UPLOADED)){
					String sTmpValue =  aSubOption.getValues(0);
					if (sTmpValue != null){
						sSelection = appendSqlStatementAND( sSelection, KEY_UPLOAD + "=" + sTmpValue );
					}
				}else if (aSubOption.getKey().equals(GetEvent.OPTION_TIME)){
					String sTmpValue =  aSubOption.getValues(0);
					if (sTmpValue != null){
						int iTimeValue = convertDateForDB(sTmpValue);
						if (iTimeValue > 0) {
							sSelection = appendSqlStatementAND( sSelection, KEY_DATE + ">" + iTimeValue );
						}else{
							System.err.println("PARSE ERROR for : " + sTmpValue);
							throw new Exception("PARSE ERROR for : " + sTmpValue);
						}
					}
				}else if (aSubOption.getKey().equals(GetEvent.OPTION_JSON)){
					 format = outputFormat.JSON;
				}
			}
			//execute query to database
			String sOrderTag = " DESC";
			if (bChangeOrder){
				sOrderTag = " ASC";
			}

			mCursor = myDB.query(true, DATABASE_EVENTS_TABLE, listColumns,
					sSelection, null,
					null, null, KEY_ROWID + sOrderTag, sLimit);

			if (mCursor != null) {
				mCursor.moveToFirst();
				while (!mCursor.isAfterLast()) {
					if ( format == outputFormat.JSON ) {
						/* JSON output : create a Map with 'event' table content and print it as a JSON formatted string*/
						Map <String, String> dbContent = new LinkedHashMap <String, String>();
						for (String key : listColumns) {
							if (key.equals(KEY_DATE))
								dbContent.put(key, convertDate( mCursor.getLong(mCursor.getColumnIndex(key))));
							else
								dbContent.put(key, mCursor.getString(mCursor.getColumnIndex(key)));
						}
						if ( printToJsonFormat(dbContent) != 0 )
							break;
					}
					else {
						/* STANDARD text output*/
						String sLine="";
						/* Print header only once */
						if ( headerToPrint ) {
							String sHeader="";
							for (int i = 0; i < indexListColumns.length; i++) {
								indexListColumns[i] = mCursor.getColumnIndex(listColumns[i]);
								if (i==0){
									sHeader = listColumns[i];
								}else{
									sHeader += " | " + listColumns[i];
								}
							}
							CrashInfo.outputCrashinfo(sHeader, bUseHeader);
							headerToPrint = false;
						}
						/* Print column content */
						for (int i = 0; i < listColumns.length; i++) {
							String sColValue ="";
							if (listColumns[i] == KEY_DATE)
								sColValue = convertDate(mCursor.getLong(indexListColumns[i]));
							else
								sColValue = mCursor.getString(indexListColumns[i]);
							if (i==0){
								sLine = sColValue;
							}else{
								sLine += " | " + sColValue;
							}
						}
						CrashInfo.outputCrashinfo(sLine,bUseHeader);
					}
					mCursor.moveToNext();
				}
				mCursor.close();
			}
		} catch (SQLException e) {
			System.err.println( "count SQLException");
		}
	}

	public static int convertDateForDB(String sDate) {
		int iResult = -1;
		Date cDate = null;
		if (sDate != null) {
			try {
				PARSE_DF.setTimeZone(TimeZone.getTimeZone("GMT"));
				cDate = PARSE_DF.parse(sDate);
				iResult = (int)(cDate.getTime() / COEF_S_TO_MS);
			} catch (ParseException e) {
				iResult = -1;
			}
		}
		return iResult;
	}

	public int getLastSWUpdate(){
		Cursor mCursor;
		int iResultId = -1;
		try {
			mCursor = myDB.query(true, DATABASE_EVENTS_TABLE, new String[] {KEY_ROWID},
					KEY_TYPE+"='SWUPDATE'", null,
					null, null, KEY_ROWID + " DESC", "1");
			if (mCursor != null) {
				mCursor.moveToFirst();
				if (!mCursor.isAfterLast()) {
					iResultId = mCursor.getInt(mCursor.getColumnIndex(KEY_ROWID));
				}
				mCursor.close();
			}
		} catch (SQLException e) {
			System.err.println( "getLastSWUpdate SQLException");
		}
		return iResultId;
	}

	public String getLogDirByID(int iID){
		Cursor mCursor;
		String sResultDir = "";

		try {
			mCursor = myDB.query(true, DATABASE_EVENTS_TABLE, new String[] {KEY_CRASHDIR},
					KEY_ROWID + "=" + iID, null,
					null, null, null, null);
			if (mCursor != null) {
				mCursor.moveToFirst();
				if (!mCursor.isAfterLast()) {
					sResultDir = mCursor.getString(mCursor.getColumnIndex(KEY_CRASHDIR));
				}
				mCursor.close();
			}
		} catch (SQLException e) {
			System.err.println( "getLogDirByID  SQLexception");
		}
		return sResultDir;
	}

	public String[] getAllLogsDir(){
		return getLogsDirByQuery(KEY_CRASHDIR + " is not null");
	}

	public String[] getLogsDirByTime(String sTime){
		if (sTime != null){
			String sQuery;
			int iTimeValue = convertDateForDB(sTime);
			if (iTimeValue > 0) {
				sQuery = KEY_DATE + "<" + iTimeValue ;
			}else{
				System.err.println("PARSE ERROR for : " + sTime);
				return new String[0];
			}
			return getLogsDirByQuery(sQuery + " AND " + KEY_CRASHDIR + " is not null");
		}else{
			return new String[0];
		}
	}


	public  String[] getLogsDirByQuery(String sWhereQuery){
		Cursor mCursor;
		String[] sResultLogsDir = new String[0];

		try {
			mCursor = myDB.query(true, DATABASE_EVENTS_TABLE, new String[] {KEY_CRASHDIR},
					sWhereQuery, null,
					null, null, null, null);
			if (mCursor != null) {
				int i = 0;
				mCursor.moveToFirst();
				int iDirCount = mCursor.getCount();
				sResultLogsDir = new String[iDirCount];
				while (!mCursor.isAfterLast()) {
					sResultLogsDir[i] = mCursor.getString(mCursor.getColumnIndex(KEY_CRASHDIR));
					mCursor.moveToNext();
					i++;
				}
				mCursor.close();
			}
		} catch (SQLException e) {
			System.err.println( "getLogsDir  SQLexception : " + e);
			return null;
		}
		return sResultLogsDir;
	}

	public void cleanCrashDirByID(int iIdToClean){
		ContentValues updateValue = new ContentValues();
		updateValue.put(KEY_CRASHDIR,"");
		myDB.update(DATABASE_EVENTS_TABLE, updateValue, KEY_ROWID+ "=" + iIdToClean,null);
	}

	/**
	 * Upload in DB the upload state of the event defined by the input iIdToUpdate
	 * and as following :
	 *  - EVENT_UPLOADED  => 'uploaded' =  1
	 *  - LOG_UPLOADED    => 'uploaded' =  1 & 'logsUploaded' =  1
	 *  - EVENT_INVALID   => 'uploaded' = -1 & 'logsUploaded' = -1
	 *  - LOG_INVALID     => 'uploaded' =  1 & 'logsUploaded' = -1
	 * @param iIdToUpdate is id of the event to update
	 * @param state
	 */
	public void updateUploadStateByID(int iIdToUpdate, eventUploadState state){
		ContentValues updateValue = new ContentValues();

		switch (state) {
		case EVENT_UPLOADED:
			updateValue.put(KEY_UPLOAD,"1");
			break;
		case LOG_UPLOADED:
			updateValue.put(KEY_UPLOAD,"1");
			updateValue.put(KEY_UPLOADLOG,"1");
			break;
		case EVENT_INVALID:
			updateValue.put(KEY_UPLOAD,"-1");
			updateValue.put(KEY_UPLOADLOG,"-1");
			break;
		case LOG_INVALID:
			updateValue.put(KEY_UPLOAD,"1");
			updateValue.put(KEY_UPLOADLOG,"-1");
			break;
		}

		myDB.update(DATABASE_EVENTS_TABLE, updateValue, KEY_ROWID+ "=" + iIdToUpdate,null);
	}

	public void cleanCrashDirByTime(String sTime){
		if (sTime != null){
			String sQuery;
			int iTimeValue = convertDateForDB(sTime);
			if (iTimeValue > 0) {
				sQuery = KEY_DATE + "<" + iTimeValue ;
				ContentValues updateValue = new ContentValues();
				updateValue.put(KEY_CRASHDIR,"");
				myDB.update(DATABASE_EVENTS_TABLE, updateValue, sQuery + " AND " + KEY_CRASHDIR + " is not null",null);
			}else{
				System.err.println("PARSE ERROR for : " + sTime);
			}
		}
	}

	public long getCurrentUptime()
	{
		Cursor mCursor;
		long lResultUptime = 0;
		int iLastSWUpdateID = getLastSWUpdate();
		boolean bUptimePresent = false;

		try {
			mCursor = myDB.query(true, DATABASE_EVENTS_TABLE, new String[] {KEY_NAME,KEY_UPTIME},
					KEY_ROWID + " > " + iLastSWUpdateID, null,
					null, null, KEY_ROWID + " ASC", null);
			if (mCursor != null) {
				long lUptimeReboot = 0;
				long lUptimeOther = 0;
				mCursor.moveToFirst();
				while (!mCursor.isAfterLast()) {
					String sName, sUptime;
					sName = mCursor.getString(mCursor.getColumnIndex(KEY_NAME));
					sUptime = mCursor.getString(mCursor.getColumnIndex(KEY_UPTIME));
					int iCurUptime = convertUptime(sUptime);
					if (iCurUptime >= 0){
						bUptimePresent = true;
						if (sName.equals("REBOOT")){
							lUptimeReboot += iCurUptime;
							lUptimeOther = 0;
						}else if (iCurUptime > 0){
							lUptimeOther = iCurUptime;
						}
					}
					mCursor.moveToNext();
				}
				mCursor.close();
				lResultUptime = lUptimeReboot + lUptimeOther;
			}
		} catch (SQLException e) {
			System.err.println( "getCurrentUptime  SQLexception : " + e);
			lResultUptime = -1;
		}
		if (bUptimePresent) {
			return lResultUptime;
		}else{
			return -1;
		}
	}

	private int convertUptime(String sUpTime){
		int iResultUpTime = -1;
		int iHour, iMinute, iSecond;
		String[] sDecodedUptime = sUpTime.split(":");
		if (sDecodedUptime.length == 3){
			try {
				iHour = Integer.parseInt(sDecodedUptime[0]);
				iMinute = Integer.parseInt(sDecodedUptime[1]);
				iSecond = Integer.parseInt(sDecodedUptime[2]);
				iResultUpTime = (iHour * 60 * 60) + (iMinute * 60) + iSecond;
			}
			catch (Exception e) {
				iResultUpTime= -1;
				System.err.println( "convertUptime  exception : " + e);
			}
		}

		return iResultUpTime;
	}

	/**
	 * Returns all elements contained in the 'device' table of CrashReport DB.
	 * @return a cursor to the first and only row of the 'device' table
	 */
	public Cursor fetchDeviceInfo() {
		Cursor cursor = null;
		String whereQuery = "Select * from "+DATABASE_DEVICE_TABLE;

		cursor = myDB.rawQuery(whereQuery, null);
		if (cursor != null)
			cursor.moveToFirst();
		return cursor;
	}

	/**
	 * Prints the 'device' database table under the specified format.
	 * @param format the desired output format (standard text or JSON)
	 */
	public void getDeviceInfo(outputFormat format) {

		Cursor mCursor;
		mCursor = fetchDeviceInfo();

		if (mCursor != null) {
			/* Cursor already points to the only one line of 'device' table that is assumed to be always up-to-date*/
			if ( format == outputFormat.JSON ) {
				/* JSON output : create a Map with 'device' table content and print it as a JSON formatted string*/
				Map <String, String> dbContent = new LinkedHashMap <String, String>();
				for (String infoName : mCursor.getColumnNames())
					dbContent.put(infoName, mCursor.getString(mCursor.getColumnIndex(infoName)));
				printToJsonFormat(dbContent);
			}
			else {
				/* STANDARD output : print all 'device' table column names and their text value */
				String sHeader = "";
				String sContent = "";
				int iNbColumn = mCursor.getColumnCount()-1;
				for (String infoName : mCursor.getColumnNames()) {
					/* Don't add a separator for the last column*/
					sHeader += infoName + (mCursor.getColumnIndex(infoName) == iNbColumn ? "" : SEPARATOR);
					sContent += mCursor.getString(mCursor.getColumnIndex(infoName)) + (mCursor.getColumnIndex(infoName) == iNbColumn ? "" : SEPARATOR);
				}
				System.out.println(sHeader);
				System.out.println(sContent);
			}
			mCursor.close();
		}
	}

	/**
	 * Prints the given object under JSON format.
	 * @param src the object to print under JSON format
	 */
	public int printToJsonFormat(Object src) {
		try {
			/* Json convert and print */
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.setExclusionStrategies(new EventDateExclusionStrategy());
			gsonBuilder.excludeFieldsWithModifiers(Modifier.STATIC);
			Gson gson = gsonBuilder.create();
			System.out.println(gson.toJson(src));
			return 0;
		} catch(NoClassDefFoundError e) {
			System.err.println( CrashInfo.Module+ "can't convert to JSON format : " + e);
			return -1;
		}
	}

	/**
	 * Converts a date defined in seconds and under long format to
	 * a human readable date under string format.
	 * @param date date defined in seconds
	 * @return human readable date
	 */
	private String convertDate( long date ) {
		Date convertedDate = new Date(date * COEF_S_TO_MS);
		PARSE_DF.setTimeZone(TimeZone.getTimeZone("GMT"));
		return PARSE_DF.format(convertedDate);
	}

	public void close() {
		if ( myDB != null ){
			myDB.close();
			myDB = null;
		}
	}

	public boolean isOpened() {
		boolean bResult = true;
		if ( myDB == null ){
			bResult = false;
			System.err.println( CrashInfo.Module+ " Database not opened!");
		}

		return bResult;
	}
}
