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

package com.intel.crashreport.bugzilla.ui.common;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.widget.ListView;

import com.intel.crashreport.*;
import com.intel.crashreport.bugzilla.BZ;
import com.intel.crashreport.specific.EventDB;

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
			app.getApplicationContext().startService(crashReportStartServiceIntent);
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

