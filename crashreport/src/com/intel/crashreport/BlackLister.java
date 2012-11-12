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
 * Author: Charles-Edouard Vidoine <charles.edouardx.vidoine@intel.com>
 */

package com.intel.crashreport;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import android.database.Cursor;
import android.database.SQLException;

public class BlackLister {
	private EventDB db;

	public boolean blackList(Event event){
		if(event.getType().equals("TOMBSTONE")) {
			return blackListTombstone(event);
		}
		if(event.getType().equals("JAVACRASH")) {
			return blackListJavacrash(event);
		}
		if (event.getType().equals("ANR")) {
			return blackListAnr(event);
		}
		return false;
	}

	public void setDb(EventDB mDb) {
		db = mDb;
	}

	public boolean blackListTombstone(Event event) {
		return blackListCrash(event);
	}

	public boolean blackListJavacrash(Event event) {
		return blackListCrash(event);
	}

	public boolean blackListCrash(Event event) {
		boolean result = false;
		try{
			CrashSignature signature = new CrashSignature(event);
			if(!signature.isEmpty()) {
				if(db.isRainEventExist(signature)){
					if(db.isInTheCurrentRain(event)){
						db.updateRainEvent(signature, event.getDate());
						result = true;
					}
					else
						result = db.checkNewRain(event, db.getLastCrashDate(signature));
				}
				else
					result = db.checkNewRain(event);
				if(result) {
					db.addBlackEvent(event, "DUPLICATE");
					Log.w("BlackLister: event "+event.getEventId()+" is DUPLICATE");
				}
			}
		}
		catch(SQLException e){
			Log.e("BlaskLister:blackListCrash" + e.getMessage());
		}
		return result;
	}

	public boolean blackListAnr(Event event) {
		try{
			if(!event.getOrigin().equals("") && db.isOriginExist(event.getOrigin())){
				//db.addBlackEvent(event, "FAKE");
				String crashdir = event.getCrashDir();
				event.setData4("FAKE");
				if(!crashdir.equals("")) {
					try {
						changeCrashType(crashdir,"FAKE");
					} catch (FileNotFoundException e) {
						Log.e("BlackLister:blackListAnr:crashfile is not present "+crashdir);
					}
					catch(IOException e){
						Log.e("BlackLister:blackListAnr:write problem in crashfile "+crashdir);
					}
				}
				Log.w("BlackLister: event "+event.getEventId()+" is FAKE");
			}
			return blackListCrash(event);
		}
		catch(SQLException e){
			Log.e("BlackLister:blackListAnr: can't open database "+e.getMessage());
		}
		return false;
	}

	public void cleanRain(Date date) {
		try{
			Cursor cursor = db.fetchLastRain(date);
			if(cursor != null){
				while(!cursor.isAfterLast()) {
					String type = cursor.getString(cursor.getColumnIndex("type"));
					String data0 = cursor.getString(cursor.getColumnIndex("data0"));
					String data1 = cursor.getString(cursor.getColumnIndex("data1"));
					String data2 = cursor.getString(cursor.getColumnIndex("data2"));
					String rainId = cursor.getString(cursor.getColumnIndex("eventId"));
					Cursor blackEvents = db.fetchBlackEventsRain(rainId);
					if(blackEvents!=null){
						while(!blackEvents.isAfterLast()) {
							String crashDir = blackEvents.getString(blackEvents.getColumnIndex("crashdir"));

							try {
								changeCrashType(crashDir,blackEvents.getString(blackEvents.getColumnIndex("reason")));
							} catch (FileNotFoundException e) {
								Log.e("BlackLister:cleanRain:crashfile is not present "+crashDir);
							}
							catch(IOException e){
								Log.e("BlackLister:cleanRain:write problem in crashfile "+crashDir);
							}
							blackEvents.moveToNext();
						}
						blackEvents.close();
					}
					db.deleteRainEvent(new CrashSignature(type,data0,data1,data2));
					cursor.moveToNext();
				}
				cursor.close();
			}
		}catch(SQLException e){
			Log.e("BlackLister:cleanRain: cannot open db "+e.getMessage());
		}
	}

	public void changeCrashType(String crashDir,String reason) throws IOException,FileNotFoundException{
		File logData = new File(crashDir);
		if (logData.exists() && logData.isDirectory()) {
			for(File file:logData.listFiles()){
				if(file.getName().equals("crashfile")){
					CrashFile crashfile = new CrashFile(crashDir,false);
					crashfile.writeCrashFile(reason);
				}
			}
		}
	}
}
