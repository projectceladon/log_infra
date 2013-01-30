/* crashinfo - DBManager contains all methods that access to database
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
 * Author: Nicolas Benoit <nicolasx.benoit@intel.com>
 */

package com.intel.commands.crashinfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.intel.commands.crashinfo.option.OptionData;
import com.intel.commands.crashinfo.subcommand.GetEvent;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import java.text.ParseException;
import java.util.TimeZone;

public class DBManager {
	private static final int COEF_S_TO_MS = 1000;
	private final static SimpleDateFormat PARSE_DF = new SimpleDateFormat("yyyy-MM-dd/HH:mm:ss");

	private static final String PATH_TO_DB ="/data/data/com.intel.crashreport/databases/eventlogs.db";
	public static final String KEY_TYPE = "type";
	private static final String DATABASE_TYPE_TABLE = "events_type";
	private static final String DATABASE_EVENTS_TABLE = "events";
	private static final String DATABASE_CRITICAL_EVENTS_TABLE = "critical_events";
	private static final String DATABASE_BZ_TABLE = "bz_events";
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
	public static final String KEY_DEVICEID = "deviceId";
	public static final String KEY_IMEI = "imei";
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


	private static final String SELECT_CRITICAL_EVENTS_QUERY = "select "+KEY_ID+" from "+DATABASE_EVENTS_TABLE+" e,"+DATABASE_CRITICAL_EVENTS_TABLE+" ce"
			+" where ce."+KEY_TYPE+"=e."+KEY_TYPE+" and trim(e."+KEY_DATA0+")=ce."+KEY_DATA0+" and "
			+"(ce."+KEY_DATA1+"='' or ce."+KEY_DATA1+"=trim(e."+KEY_DATA1+")) and "
			+"(ce."+KEY_DATA2+"='' or ce."+KEY_DATA2+"=trim(e."+KEY_DATA2+")) and "
			+"(ce."+KEY_DATA3+"='' or ce."+KEY_DATA3+"=trim(e."+KEY_DATA3+")) and "
			+"(ce."+KEY_DATA4+"='' or ce."+KEY_DATA4+"=trim(e."+KEY_DATA4+")) and "
			+"(ce."+KEY_DATA5+"='' or ce."+KEY_DATA5+"=trim(e."+KEY_DATA5+"))";

	public Cursor fetchAllBZs() {
		Cursor cursor;
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

	private SQLiteDatabase myDB;

	public DBManager() {
		super();
		createDB(false);
	}

	public DBManager(boolean bWriteOnDB) {
		createDB(bWriteOnDB);
	}

	private void createDB(boolean bWriteOnDB){
		int iOpenFlag;
		if (bWriteOnDB){
			iOpenFlag = SQLiteDatabase.OPEN_READWRITE;
		}else{
			iOpenFlag = SQLiteDatabase.OPEN_READONLY;
		}
		myDB = SQLiteDatabase.openDatabase(
				PATH_TO_DB
				, null
				, iOpenFlag
				);
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


	public void getEvent(EventLevel aLevel, ArrayList<OptionData> mySubOptions) throws Exception
	{
		Cursor mCursor;
		try {
			//Defining columns to return
			String[] listColumns;
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
						KEY_DEVICEID,KEY_IMEI,KEY_NOTIFIED,KEY_DATA_READY};
				break;
			default :
				listColumns=null;
			}

			//Defining selection depending on options
			String sSelection = "";
			String sLimit = null;
			boolean bChangeOrder = false;
			boolean bUseHeader = false;
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
			//header
			if (mCursor != null) {
				mCursor.moveToFirst();
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
				CrashInfo.outputCrashinfo(sHeader, bUseHeader);
				//content
				while (!mCursor.isAfterLast()) {
					String sLine="";
					for (int i = 0; i < listColumns.length; i++) {
						String sColValue ="";
						if (listColumns[i] == KEY_DATE){
							long lDate = mCursor.getLong(indexListColumns[i]);
							try {
								Date date = new Date(lDate*COEF_S_TO_MS);
								PARSE_DF.setTimeZone(TimeZone.getTimeZone("GMT"));
								sColValue = PARSE_DF.format(date);
							} catch (Exception e) {
								sColValue = "parse error"  ;
							}
						}else{
							sColValue = mCursor.getString(indexListColumns[i]);
						}
						if (i==0){
							sLine = sColValue;
						}else{
							sLine += " | " + sColValue;
						}
					}
					CrashInfo.outputCrashinfo(sLine,bUseHeader);
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

	public void updateUploadStateByID(int iIdToUpdate, boolean bUploadLog){
		ContentValues updateValue = new ContentValues();
		updateValue.put(KEY_UPLOAD,"1");
		if (bUploadLog){
			updateValue.put(KEY_UPLOADLOG,"1");
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
}
