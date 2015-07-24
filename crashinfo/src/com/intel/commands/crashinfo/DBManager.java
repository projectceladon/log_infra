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

import com.intel.crashreport.database.EventDB;
import com.intel.crashreport.database.Utils;
import com.intel.crashreport.core.GeneralEvent;

import com.intel.crashtoolserver.bean.Event;
import com.intel.crashtoolserver.bean.FileInfo;
import com.intel.crashtoolserver.bean.Build;
import com.intel.crashtoolserver.bean.Device;
import java.lang.reflect.Modifier;


public class DBManager extends EventDB {
	private static final String PATH_TO_DB ="/data/data/com.intel.crashreport/databases/eventlogs.db";

	public static enum EventLevel{BASE,DETAIL,FULL};

	/*Define possible values for event uploaded/logUploaded state in DB*/
	public enum eventUploadState {EVENT_UPLOADED, LOG_UPLOADED, EVENT_INVALID, LOG_INVALID};

	/* Define output format types*/
	public static enum outputFormat {STANDARD, JSON};

	public DBManager() {
		super();
		open(PATH_TO_DB, false);
	}

	public DBManager(boolean bWriteOnDB) {
		open(PATH_TO_DB, bWriteOnDB);
	}

	public boolean isOpened() {
		return (mDb == null) ? false : true;
	}

	public int getVersion(){
		return mDb.getVersion();
	}

	private String appendSqlStatementAND(String sSql, String sToAppend) {
		if (sSql == null)
			return null;

		if (!sSql.equals(""))
			sSql += " AND ";

		sSql += sToAppend;
		return sSql;
	}

	public void getBz() throws Exception {
		Cursor mCursor = fetchAllBZs();
		if (mCursor != null) {
			printFromCursor(mCursor, bzColums, "");
			mCursor.close();
		}
	}

	public List<Event> getEvent(String sId) {
		List<Event> curEvent = null;
		try {
			Cursor cursor = fetchEventFromWhereQuery(KEY_ID + " like '%" + sId +"%'");

			if (cursor == null)
				return curEvent;

			curEvent = new ArrayList<Event>();
			while (!cursor.isAfterLast()) {
				GeneralEvent ge = fillEventFromCursor(cursor);
				Device di = fillDeviceInformation();
				curEvent.add(ge.getEventForServer(di));
				cursor.moveToNext();
			}
			cursor.close();
		} catch (SQLException e) {
			System.err.println( "Exception: " + e);
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
					Date date = Utils.convertDateForJava(iDate);
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
			System.err.println( "Exception: " + e);
		}

		return fileInfo;
	}

	public void getEvent(EventLevel aLevel, ArrayList<OptionData> mySubOptions) throws Exception
	{
		Cursor mCursor;
		String[] listColumns;
		String sSelection = "";
		String sLimit = null;
		boolean bOrderDesc = true;
		boolean bUseHeader = false;
		boolean headerToPrint = true;
		outputFormat format = outputFormat.STANDARD;

		//Defining columns to return
		switch(aLevel){
		case BASE :
			listColumns = eventsTableBaseColums;
			break;
		case DETAIL :
			listColumns = eventsTableDetailColums;
			break;
		case FULL :
			listColumns = eventsTableColums;
			break;
		default :
			listColumns = new String[]{};
			return;
		}

		int[] indexListColumns = new int[listColumns.length];

		//Defining selection depending on options
		for (OptionData aSubOption : mySubOptions) {
			if (aSubOption.getKey().equals(GetEvent.OPTION_LAST)){
				sLimit = "1";
			}else if (aSubOption.getKey().equals(GetEvent.OPTION_REVERSE)){
				bOrderDesc = false;
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
					int iTimeValue = Utils.convertDateForDB(sTmpValue);
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

		Cursor cursor = selectEntries(DATABASE_TABLE, listColumns, sSelection, KEY_ROWID,
				bOrderDesc, sLimit);

		if (cursor != null) {
			cursor.moveToFirst();

			if ( format == outputFormat.JSON ) {
				while (!cursor.isAfterLast()) {
					/* JSON output : create a Map with 'event' table
					content and print it as a JSON formatted string*/
					if (printToJsonFormat(cursorToHashMap(cursor, true)) != 0)
						break;
					cursor.moveToNext();
				}
			}
			else
				printFromCursor(cursor, listColumns,
						(bUseHeader ? CrashInfo.TAG_HEADER : ""));

			cursor.close();
		} else {
			System.err.println( "getEvent: Select failed.");
		}
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
		int upload = 1;
		int upload_log = 1;
		boolean update_log = true;

		if (state == eventUploadState.EVENT_UPLOADED) {
			update_log = false;
		} else if (state == eventUploadState.EVENT_INVALID) {
			upload = -1;
			upload_log = -1;
		} else if (state == eventUploadState.LOG_INVALID) {
			upload_log = -1;
		} else if (state != eventUploadState.LOG_UPLOADED)
			return;

		updateEventUploadStateByRow(iIdToUpdate, upload, upload_log, update_log);
	}
	/**
	 * Prints the 'device' database table under the specified format.
	 * @param format the desired output format (standard text or JSON)
	 */
	public void getDeviceInfo(outputFormat format) {
		Cursor cursor;
		cursor = fillDeviceInfo();

		if (cursor == null)
			return;

		/* Cursor already points to the only one line of
		'device' table that is assumed to be always up-to-date*/
		if ( format == outputFormat.JSON ) {
			/* JSON output : create a Map with 'device' table content and
			print it as a JSON formatted string */
			printToJsonFormat(cursorToHashMap(cursor, false));
		}
		else
			printFromCursor(cursor, deviceTableColums, "");

		cursor.close();
	}

	/**
	 * Prints the given object under JSON format.
	 * @param src the object to print under JSON format
	 */
	public static int printToJsonFormat(Object src) {
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

	private void printFromCursor(Cursor cursor, String[] listColumns, String tagHeader) {
		int [] indexListColumns = new int[listColumns.length];
		String sHeader="";
		for (int i = 0; i < indexListColumns.length; i++) {
			indexListColumns[i] = cursor.getColumnIndex(listColumns[i]);
			if (i==0){
				sHeader = listColumns[i];
			}else{
				sHeader += " | " + listColumns[i];
			}
		}
		System.out.println(sHeader);

		//content
		while (!cursor.isAfterLast()) {
			String sLine="";
			for (int i = 0; i < listColumns.length; i++) {
				String sColValue ="";
				sColValue = cursor.getString(indexListColumns[i]);
				if (i==0){
					sLine = tagHeader + sColValue;
				}else{
					sLine += " | " + sColValue;
				}
			}
			System.out.println(sLine);
			cursor.moveToNext();
		}
	}
}
