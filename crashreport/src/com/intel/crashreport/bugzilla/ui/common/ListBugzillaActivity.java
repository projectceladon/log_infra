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
		setTitle(getString(R.string.app_name)+" "+getString(R.string.app_version));

	}

	public void onResume() {
		ListView listBugzilla = (ListView) findViewById(R.id.list_bugzilla_all);
		bugzillaAdapter = (BugzillaViewAdapter)listBugzilla.getAdapter();
		if(listBugzilla != null && bugzillaAdapter != null) {
			bugzillaAdapter.setListBz(getAllBz());
			listBugzilla.invalidateViews();
		}
		super.onResume();
		CrashReport app = (CrashReport)getApplicationContext();
		if(!app.isServiceStarted()) {
			Intent crashReportStartServiceIntent = new Intent("com.intel.crashreport.CrashReportService");
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

