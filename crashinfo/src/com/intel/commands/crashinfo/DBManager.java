/* crashinfo
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBManager {
	private static final int COEF_S_TO_MS = 1000;

	public static final String KEY_TYPE = "type";
	private static final String DATABASE_CRITICAL_TABLE = "critical_events_type";
	public static final String KEY_CRITICAL = "critical";

	public DBManager() {
		super();
		myDB = SQLiteDatabase.openDatabase(
				"/data/data/com.intel.crashreport/databases/eventlogs.db"
				, null
				, SQLiteDatabase.OPEN_READONLY
				);
	}

	private SQLiteDatabase myDB;

	public int getVersion(){
		return myDB.getVersion();
	}

	public int getNumberEventByCriticty(boolean bCritical){
		Cursor mCursor;

		String sCriticalValue;
		if (bCritical){
			sCriticalValue = " in (select "+KEY_TYPE+" from "+
				    DATABASE_CRITICAL_TABLE+" where "+KEY_CRITICAL+ " ='1')";
		}else{
			sCriticalValue = " not in (select "+KEY_TYPE+" from "+
				    DATABASE_CRITICAL_TABLE+" where "+KEY_CRITICAL+ " ='1')";;
		}
		mCursor = myDB.query(true, "events", new String[] {"eventId"},
				 KEY_TYPE+sCriticalValue, null,
				null, null, null, null);
		if (mCursor != null) {
			return mCursor.getCount();
		}else{
			return -1;
		}
	}

	public void getEvent()
	{
		Cursor mCursor;
		int count;
		try {
			mCursor = myDB.query(true, "events", new String[] {"eventId","eventName","type", "date"},
					null, null,
					null, null, "date DESC", null);
			if (mCursor != null) {
				count = mCursor.getCount();
				System.out.println( "count events : " + count);
				mCursor.moveToFirst();
				DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				while (!mCursor.isAfterLast()) {
					String sLine = mCursor.getString(mCursor.getColumnIndex("eventId"));
					sLine += " | " + mCursor.getString(mCursor.getColumnIndex("eventName"));
					sLine += " | " + mCursor.getString(mCursor.getColumnIndex("type"));
					long lDate = mCursor.getLong(mCursor.getColumnIndex("date"));
					sLine += " | " + lDate;
					try {
					Date date = new Date(lDate*COEF_S_TO_MS);
					sLine += " | " + iso8601Format.format(date);
					} catch (Exception e) {
						System.out.println("Managing datetime failed" + e);
						sLine += " | parse error"  ;
					}
					System.out.println(sLine);
					mCursor.moveToNext();
				}
				mCursor.close();
			}
		} catch (SQLException e) {
			System.out.println( "count SQLException");
		}
	}

}
