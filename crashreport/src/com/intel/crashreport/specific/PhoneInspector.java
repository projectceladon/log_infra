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
 * Author: Jean Thiry <jeanx.thiry@intel.com>
 */
package com.intel.crashreport.specific;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.intel.crashreport.ApplicationPreferences;
import com.intel.crashreport.Log;
import com.intel.phonedoctor.Constants;
import com.intel.phonedoctor.utils.FileOps;

import android.content.Context;
import android.database.SQLException;
import android.os.DropBoxManager;
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

	/**
	 * Values that can be taken by Crashlog daemon FULL_DROPBOX_PROP property
	 */
	public static final String LOW_MEM_MODE = "lowmemory";
	public static final String NOMINAL_MODE = "nominal";
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

	public void checkLogsPartition() {
		File logsDir = new File(Constants.LOGS_DIR);
		if(logsDir.getUsableSpace() < Constants.LOGS_CRITICAL_SIZE) {
			Log.w("checkLogsPartition : partition full! activate cleaning");
			//we need to clean logs partition
			cleanLogsPartition(true);
			/* for the moment, we don't want to activate unsafemode
			if(logsDir.getUsableSpace() < Constants.LOGS_CRITICAL_SIZE) {
				//now switching in unsafemode
				Log.w("checkLogsPartition : need to use full cleaning");
				cleanLogsPartition(false);
			}*/

		} else {
			Log.d("checkLogsPartition : partition OK!");
		}
	}

	public void cleanLogsPartition(boolean bSafeMode) {
		File[] files = null;

		File logsDir = new File(Constants.LOGS_DIR);
		files = logsDir.listFiles();
		if(files != null) {
			if (bSafeMode) {
				//need to check if data is uploaded

				EventDB db = new EventDB(mCtx);
				try {
					db.open();

					for (File c : files)
						if (c.isDirectory()){
							if (c.getName().startsWith("crashlog")){
								//need to check if data is uploaded
								if (db.isPathUploaded(c.getAbsolutePath())){
									FileOps.delete(c);
								}
							}
						}
					db.close();
				}
				catch(SQLException e){
					Log.w("checkLogsPartition : can't use database",e);
				}
			}else{
				for (File c : files)
					if (c.isDirectory()){
						if (c.getName().startsWith("crashlog")){
							FileOps.delete(c);
						}
					}
			}
		}
	}
}
