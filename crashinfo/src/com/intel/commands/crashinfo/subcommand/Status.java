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

package com.intel.commands.crashinfo.subcommand;

import com.intel.commands.crashinfo.DBManager;
import com.intel.commands.crashinfo.ISubCommand;

public class Status implements ISubCommand {
	//private static final int COEF_S_TO_MS = 1000;

	public static final String PATH_LOGS = "/data/logs/";
	String[] myArgs;
	@Override
	public int execute() {
		if (myArgs == null){
			return execBaseStatus();
		}else if (myArgs.length == 0){
			return execBaseStatus();
		}else{
			if (myArgs[0].equals("--uptime") || myArgs[0].equals("-u")){
				return execUptime();
			}else{
				System.out.println("error : nothing to do");
				return -1;
			}
		}

	}

	private int execUptime(){
		System.out.println("Empty uptime Function");
		return 0;
	}

	private int execBaseStatus(){
		try {
			//displayContacts();
			displayDbstatus();
			System.out.println("Main Path for logs : " + PATH_LOGS);
		}catch (Exception e) {
			System.out.println("Exception : "+e.toString());
			return -1;
		}
		return 0;
	}

	@Override
	public void setArgs(String[] subArgs) {
		myArgs = subArgs;
	}

	@Override
	public boolean checkArgs() {
		boolean result = true;
		if (myArgs == null){
			//correct, nothing to do
		}else if (myArgs.length == 0){
			//correct, nothing to do
		}else{
			for (int i = 0; i < myArgs.length; i++) {
				if (myArgs[i].equals("--uptime")||myArgs[i].equals("-u")){
					//correct, nothing to do
				}else{
					result = false;
					break;
				}
			}
		}
		return result;
	}

	private void displayDbstatus(){
		DBManager aDB = new DBManager();
		System.out.println("Version database : "  + aDB.getVersion());
		System.out.println("Number of critical crash : "  +aDB.getNumberEventByCriticty(true));
		System.out.println("Number of non-critical crash : "  +aDB.getNumberEventByCriticty(false));
		aDB.getEvent();
		/*SQLiteDatabase db;
		db = SQLiteDatabase.openDatabase(
				"/data/data/com.intel.crashreport/databases/eventlogs.db"
				, null
				, SQLiteDatabase.OPEN_READONLY
				);
		System.out.println("Version database : "  + db.getVersion());
		Cursor mCursor;
		int count;
		try {
			//warning : missing "order by time" for query
			mCursor = db.query(true, "events", new String[] {"eventId","eventName","type", "date"},
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
		}*/
	}
}
