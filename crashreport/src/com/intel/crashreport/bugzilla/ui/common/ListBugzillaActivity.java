/* Copyright (C) 2019 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.crashreport.bugzilla.ui.common;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.os.UserHandle;
import android.widget.ListView;

import com.intel.crashreport.*;
import com.intel.crashreport.core.BZ;
import com.intel.crashreport.database.EventDB;

public class ListBugzillaActivity extends Activity {

	private BugzillaViewAdapter bugzillaAdapter;
	private static String TAG = "ListBugzillaActivity";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_bugzilla);
		ListView listBugzilla = (ListView) findViewById(R.id.list_bugzilla_all);
		bugzillaAdapter = new BugzillaViewAdapter(getApplicationContext());
		if(listBugzilla != null && bugzillaAdapter != null) {
			listBugzilla.setAdapter(bugzillaAdapter);
		}
		setTitle(getString(R.string.activity_name));
	}

	public void onResume() {
		ListView listBugzilla = (ListView) findViewById(R.id.list_bugzilla_all);
		if(listBugzilla != null) {
			bugzillaAdapter = (BugzillaViewAdapter)listBugzilla.getAdapter();
			if(bugzillaAdapter != null) {
				bugzillaAdapter.setListBz(getAllBz());
			}
			listBugzilla.invalidateViews();
		}
		super.onResume();
		CrashReport app = (CrashReport)getApplicationContext();
		if(!app.isServiceStarted()) {
			Intent crashReportStartServiceIntent = new Intent(app.getApplicationContext(), CrashReportService.class);
			crashReportStartServiceIntent.putExtra("fromActivity", false);
			app.getApplicationContext().startServiceAsUser(crashReportStartServiceIntent, UserHandle.CURRENT);
		}
	}

	public ArrayList<BZ> getAllBz() {
		EventDB db = new EventDB(getApplicationContext());
		ArrayList<BZ> listBz = new ArrayList<BZ>();
		Cursor cursor;
		BZ bz;
		try {
			db.open();
			cursor = db.fetchAllBZs();
			if (cursor != null) {
				while (!cursor.isAfterLast()) {
					bz = db.fillBZFromCursor(cursor);
					listBz.add(bz);
					cursor.moveToNext();
					Log.w(TAG+":onResume "+bz.getEventId());
				}
				cursor.close();
			}
			db.close();
		} catch (SQLException e) {
			Log.w(TAG+":getCount: db Exception");
		}
		return listBz;
	}


}

