package com.intel.crashreport.bugzilla.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.widget.ListView;

import com.intel.crashreport.*;
import com.intel.crashreport.bugzilla.BZ;

public class ListBugzillaActivity extends Activity {

	private BugzillaViewAdapter bugzillaAdapter;
	private static String TAG = "ListBugzillaActivity";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_bugzilla);
		ListView listBugzilla = (ListView) findViewById(R.id.list_bugzilla_all);
		bugzillaAdapter = new BugzillaViewAdapter(getApplicationContext());
		listBugzilla.setAdapter(bugzillaAdapter);
		setTitle(getString(R.string.app_name)+" "+getString(R.string.app_version));

	}

	public void onResume() {
		ListView listBugzilla = (ListView) findViewById(R.id.list_bugzilla_all);
		bugzillaAdapter = (BugzillaViewAdapter)listBugzilla.getAdapter();
		bugzillaAdapter.setListBz(getAllBz());
		listBugzilla.invalidateViews();
		super.onResume();
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

