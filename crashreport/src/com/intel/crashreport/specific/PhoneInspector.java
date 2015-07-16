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

package com.intel.crashreport.specific;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intel.crashreport.ApplicationPreferences;
import com.intel.crashreport.database.EventDB;
import com.intel.crashreport.Log;
import com.intel.phonedoctor.Constants;
import com.intel.phonedoctor.utils.FileOps;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.os.DropBoxManager;
import android.os.StatFs;
import android.os.SystemProperties;

/**
 * This singleton class is responsible for watching infrastructure state allowing
 * CrashReport application to adapt its behavior depending on certain conditions.
 */
public class PhoneInspector {

	private static final PhoneInspector INSTANCE = new PhoneInspector();

	/**
	 * Private class attributes
	 */
	private static final String TAG = "PhoneDoctor";
	private static final String Module = "PhoneInspector: ";
	private static Context mCtx;
	private static DropBoxManager mDropBoxManager;
	private static Map<String,Integer> lEventListFailure = new HashMap<String,Integer>();


	/**
	 * Crashlog daemon mode property : this property is read by crashlog daemon
	 * allowing to modify its behavior
	 */
	private static final String FULL_DROPBOX_PROP = "persist.sys.crashlogd.mode";
	private static final String DATA_QUOTA_PROP = "persist.crashlogd.data_quota";

	/**
	 * Values that can be taken by Crashlog daemon FULL_DROPBOX_PROP property
	 */
	public static final String LOW_MEM_MODE = "lowmemory";
	public static final String NOMINAL_MODE = "nominal";
	private static final String CLEAN_PATTERN = "Cleaned_By_";
	public static final int RETRY_ALLOWED = 3;

	/**
	 * Returns the PhoneInspector instance
	 * @param aContext
	 * @return PhoneInspector singleton
	 */
	public static PhoneInspector getInstance (Context aContext) {

		if (aContext == null) {
			throw new IllegalArgumentException("Unresolved context");
		}
		mCtx = aContext;
		mDropBoxManager = (DropBoxManager) mCtx.getSystemService(Context.DROPBOX_SERVICE);
		return INSTANCE;
	}

	/**
	 * Constructor
	 */
	private PhoneInspector() {}

	public boolean newDropBoxEntryAdded(long intentTimeMs) {
		boolean bResult = false;
		if (intentTimeMs == 0) {
			Log.d(Module + ": Timestamp == 0 on dropbox entry");
		}

		DropBoxManager.Entry entry = mDropBoxManager.getNextEntry(null, intentTimeMs - 1);

		if (entry == null)
			return false;

		if ((entry.getFlags() & DropBoxManager.IS_EMPTY) == DropBoxManager.IS_EMPTY){
			bResult = true;
		}

		entry.close();
		setDropBoxFullState(bResult);
		return bResult;
	}

	private void setDropBoxFullState(boolean state) {
		if (state) {
			Log.i(Module + "DropBox: Items dropped");
			SystemProperties.set(FULL_DROPBOX_PROP, LOW_MEM_MODE);
		} else {
			Log.d(Module + "DropBox: Last Item succesfully added");
			SystemProperties.set(FULL_DROPBOX_PROP, NOMINAL_MODE);
		}
	}

	public boolean hasDroppedEntries(boolean state) {
		return hasDroppedEntries(state, 0);
	}

	public boolean hasDroppedEntries(boolean state, long since) {
		boolean bResult = false;
		DropBoxManager.Entry entry = mDropBoxManager.getNextEntry(null, since);

		while (entry != null){
			long time = entry.getTimeMillis();
			if ((entry.getFlags() & DropBoxManager.IS_EMPTY) == DropBoxManager.IS_EMPTY){
				bResult = true;
				entry.close();
				break;
			}
			entry.close();
			entry = mDropBoxManager.getNextEntry(null, time);
		}

		setDropBoxFullState(bResult);
		return bResult;
	}

	public void addEventLogUploadFailure(String aEventid){
		Integer iNbFailure = lEventListFailure.get(aEventid);
		if (iNbFailure == null) {
			lEventListFailure.put(aEventid, 1);
		} else {
			lEventListFailure.put(aEventid, iNbFailure + 1);
		}
	}

	public boolean isUploadableLog(String aEventid) {
		Integer iNbFailure = lEventListFailure.get(aEventid);
		if (iNbFailure == null) {
			return true ;
		} else {
			if (iNbFailure > RETRY_ALLOWED){
				// no more retry allowed
				return false;
			} else {
				return true;
			}
		}

	}

	private void cleanUploadedLogs(String path) {
		File[] files = null;

		File logsDir = new File(path);
		files = logsDir.listFiles();

		if(files == null)
			return;

		EventDB db = new EventDB(mCtx);
		Pattern pattern = Pattern.compile("^(aplogs|bz|crashlog|stats)[0-9].*");

		try {
			db.open();

			for (File c : files)
				if (c.isDirectory()){
					Matcher m = pattern.matcher(c.getName());
					if (!m.find())
						continue;

					path = c.getAbsolutePath();
					//need to check if data is uploaded, otherwise skip
					if (db.checkPathStatus(path) == 0)
						continue;

					// need to check that we delete only unreferenced files
					// older than a specified time, and newer than current time
					if (db.checkPathStatus(path) < 0) {
						Date fileModified = new Date(c.lastModified());
						Date now = new Date();
						// threshold set at 1 minute
						Date lowThreashold =
							new Date(now.getTime() - 60000);

						if (fileModified.before(now)
							&&  fileModified.after(lowThreashold))
							continue;
						Log.d("Removing unreferenced path: " + path);
					}
					else
						Log.d("Removing uploaded path: " + path);

					db.setEventLogCleaned(path);
					FileOps.delete(c);
					db.updateEventFolderPath(path, "");
				}

			db.close();
		}
		catch(SQLException e){
			Log.w("cleanUploadedLogs : can't use database",e);
		}
	}

	private void cleanOldLogs(String logsPath, int targetedFreeSpace) {
		EventDB db = new EventDB(mCtx);
		db.open();

		Cursor cursor = db.fetchMatchingLogPaths(logsPath);
		if(cursor == null) {
			db.close();
			return;
		}

		int index = cursor.getColumnIndex(EventDB.KEY_CRASHDIR);
		do {
			cursor.moveToNext();
			if (cursor.isAfterLast()) break;
			String path = cursor.getString(index);

			File logsDir = new File(path);
			if (!logsDir.exists()) {
				db.updateEventFolderPath(path, "");
				continue;
			}

			if (logsDir.isDirectory()){
				Log.d("Cleaning not uploaded path: " + path);
				Boolean skipReasonFile = false;
				db.setEventLogCleaned(path);

				File[] files = logsDir.listFiles();
				for (File file : files) {
					if (!file.isDirectory()
						&& file.getName().startsWith(CLEAN_PATTERN)) {
						skipReasonFile = true;
						continue;
					}

					FileOps.delete(file);
				}

				if (skipReasonFile)
					continue;

				String date = new SimpleDateFormat("yyyy-MM-dd_HHmmss.SSS")
						.format(new Date());
				String filename = CLEAN_PATTERN + "PhoneInspector_"
					+ date + ".txt";
				try {
					FileOps.createNewFile(logsDir, filename);
				} catch (Exception e) {
					Log.w("Could not create cleanup file: " + filename);
				}
			}
		} while (!isFreeSpaceAvailable(logsPath, targetedFreeSpace));
		cursor.close();
		db.close();
	}

	/**
	 * Returns whether or not cleanup was required.
	 * @param logsPath indicates the path to the folder to be checked.
	 * @return boolean indicating whether or not cleanup was required.
	 */
	public boolean manageFreeSpace(String logsPath) {
		if (isFreeSpaceAvailable(logsPath, Constants.LOGS_CRITICAL_SIZE_STAGE1))
			return false;

		cleanUploadedLogs(logsPath);

		if (!isFreeSpaceAvailable(logsPath, Constants.LOGS_CRITICAL_SIZE_STAGE2))
			cleanOldLogs(logsPath, Constants.LOGS_CRITICAL_SIZE_STAGE2);

                return true;
	}

	private static Boolean isFreeSpaceAvailable(String absolutePath, int trigger) {
		StatFs path;
                long availablePortion;
		try {
			path = new StatFs(absolutePath);
		} catch (IllegalArgumentException e) {
			Log.e("Exception occured while calculating free space");
			return true;
		}

		try {
			String prop = SystemProperties.get(DATA_QUOTA_PROP, "100");
                        availablePortion = Integer.parseInt(prop);
		} catch (IllegalArgumentException e) {
                        availablePortion = 100;
		}

		if (0 == availablePortion) {
			Log.w("No space reserved for crash logging");
			return false;
		}

		availablePortion = (availablePortion > 100 || availablePortion < 0)
			? 100 : availablePortion;

		long fullSpace = path.getBlockCountLong() * path.getBlockSize();
		long usedSpace = FileOps.getPathSize(absolutePath);
                long threshold = 100 - trigger;

		long stopSpace = fullSpace * availablePortion * threshold / 10000;

		if (usedSpace < stopSpace)
			return true;

		return false;
	}
}
