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

import android.app.Activity;
import android.database.SQLException;
import android.os.Bundle;
import android.widget.TextView;
import android.database.Cursor;
import java.util.*;


public class NotifyEventActivity extends Activity {

	private ApplicationPreferences appPrefs;
	private TextView text;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.critical_events);
		appPrefs = new ApplicationPreferences(getApplicationContext());
		text = (TextView) findViewById(R.id.criticalEventsView);
		text.setText("");

	}

	public void printInfo(){
		EventDB db = new EventDB(getApplicationContext());
		Event event;
		Cursor cursor;
		HashMap<String,Integer> infos = new HashMap<String,Integer>();

		try {
			db.open();
			cursor = db.fetchNotNotifiedEvents();
			if (cursor != null) {
				while (!cursor.isAfterLast()) {
					event = db.fillEventFromCursor(cursor);
					if (infos.containsKey(event.getType())) {
						int value = infos.get(event.getType());
						infos.put(event.getType(), ++value);
					} else {
						infos.put(event.getType(), 1);
					}
					cursor.moveToNext();
				}
				cursor.close();
			}
			db.close();
		} catch (SQLException e) {
			Log.w("Service: db Exception");
		}
		text.setText("");

		for ( String type : infos.keySet()){

			if( infos.get(type) == 1)
				text.append(infos.get(type)+" "+type+" crash occured\n");
			else
				text.append(infos.get(type)+" "+type+" crashes occured\n");

		}
	}

	public void notifyEvents(){
		EventDB db = new EventDB(getApplicationContext());
		Event event;
		Cursor cursor;
		try {
			db.open();
			cursor = db.fetchNotNotifiedEvents();
			if (cursor != null) {
				while (!cursor.isAfterLast()) {
					event = db.fillEventFromCursor(cursor);
					db.updateEventToNotified(event.getEventId());
					cursor.moveToNext();
				}
				cursor.close();
			}
			db.close();
		} catch (SQLException e) {
			Log.w("Service: db Exception");
		}
		NotificationMgr nMgr = new NotificationMgr(getApplicationContext());
		nMgr.cancelNotifCriticalEvent();
	}

	protected void onResume() {
		super.onResume();
		printInfo();
	}

	public void onBackPressed() {
		super.onBackPressed();
		notifyEvents();
	}

}
