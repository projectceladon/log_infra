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

package com.intel.crashreport;

import android.app.Activity;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.database.Cursor;
import java.util.*;

import com.intel.crashreport.specific.Event;
import com.intel.crashreport.specific.EventDB;


public class NotifyEventActivity extends Activity {

	private ApplicationPreferences appPrefs;
	private TextView crashText;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.critical_events);
		appPrefs = new ApplicationPreferences(getApplicationContext());
		TextView criticalText = (TextView) findViewById(R.id.criticalEventsView);
		crashText = (TextView) findViewById(R.id.crashEventsView);
		if(crashText != null) {
			crashText.setVisibility(View.GONE);
		}
		if(criticalText != null) {
			criticalText.setVisibility(View.GONE);
		}

	}

	public void printInfo(boolean critical){
		EventDB db = new EventDB(getApplicationContext());
		Event event;
		Cursor cursor;
		TextView viewText;
		HashMap<String,Integer> infos = new HashMap<String,Integer>();

		try {
			db.open();
			cursor = db.fetchNotNotifiedEvents(critical);
			if (cursor != null) {
				while (!cursor.isAfterLast()) {
					event = db.fillEventFromCursor(cursor);
					String eventType = event.getType();
					if (eventType != null && infos.containsKey(eventType)) {
						Integer resultHash = infos.get(eventType);
						if (resultHash != null){
							int value = resultHash.intValue();
							infos.put(eventType, ++value);
						}else{
							infos.put(eventType, 1);
						}
					} else {
						infos.put(event.getType(), 1);
					}
					cursor.moveToNext();
				}
				cursor.close();
			}
		} catch (SQLException e) {
			Log.w("Service: db Exception");
		}
		db.close();
		if(critical)
			viewText = (TextView) findViewById(R.id.criticalEventsView);
		else
			viewText = (TextView) findViewById(R.id.crashEventsView);

		if(viewText != null) {
			if(infos.keySet().size() > 0)
				viewText.setVisibility(View.VISIBLE);

			viewText.setText("");
			for ( String type : infos.keySet()){
				viewText.append(infos.get(type)+" "+type+" occured\n");
			}
		}
	}

	public void notifyEvents(boolean critical){
		EventDB db = new EventDB(getApplicationContext());
		Event event;
		Cursor cursor;
		try {
			db.open();
			cursor = db.fetchNotNotifiedEvents(critical);
			if (cursor != null) {
				while (!cursor.isAfterLast()) {
					event = db.fillEventFromCursor(cursor);
					db.updateEventToNotified(event.getEventId());
					cursor.moveToNext();
				}
				cursor.close();
			}
		} catch (SQLException e) {
			Log.w("Service: db Exception");
		}
		db.close();
	}

	protected void onResume() {
		super.onResume();
		printInfo(true);
		if(appPrefs.isNotificationForAllCrash())
			printInfo(false);
		else
			crashText.setVisibility(View.GONE);
	}

	public void onBackPressed() {
		super.onBackPressed();
		notifyEvents(true);
		NotificationMgr nMgr = new NotificationMgr(getApplicationContext());
		nMgr.cancelNotifCriticalEvent();
		if(appPrefs.isNotificationForAllCrash()) {
			notifyEvents(false);
			nMgr.cancelNotifNoCriticalEvent();
		}
	}

}
