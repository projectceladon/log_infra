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

package com.intel.crashreport.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.intel.crashreport.common.IEventLog;
import com.intel.crashreport.core.Logger;

import java.io.Closeable;
import java.util.List;

public class General implements Closeable {

	private static final IEventLog log = Logger.getLog();
	private DatabaseHelper mDbHelper;
	protected SQLiteDatabase mDb;
	protected String mDbName;
	protected int mDbVersion;
	protected List<Table> mTables;

	protected Context mCtx;

	public General(Context ctx, String dbName, int dbVersion, List<Table> tables) {
		this.mCtx = ctx;
		this.mDbName = dbName;
		this.mDbVersion = dbVersion;
		this.mTables = tables;
	}

	public General() { }

	protected void finalize() throws Throwable {
		try {
			close();
		} finally {
			super.finalize();
		}
	}

	public General open() throws SQLException, SQLiteException {
		mDbHelper = new DatabaseHelper(mCtx, mDbName, mDbVersion, mTables);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public General open(String dbPath, boolean writeMode) throws SQLException, SQLiteException {
		try{
			mDb = SQLiteDatabase.openDatabase(
					dbPath
					, null
					, (writeMode) ? SQLiteDatabase.OPEN_READWRITE :
							SQLiteDatabase.OPEN_READONLY
					);
		} catch (SQLException e) {
			mDb = null;
		}

		return this;
	}

	public void close() {
		if (mDb != null)
			mDb.close();
	}

	public Cursor selectEntries(String table, String[] fields) {
		return selectEntries(table, fields, null, null, false, null);
	}

	public Cursor selectEntries(String table, String[] fields, String where) {
		return selectEntries(table, fields, where, null, false, null);
	}

	public Cursor selectEntries(String table, String[] fields, String where, String orderBy,
			boolean orderDesc) {
		return selectEntries(table, fields, where, orderBy, orderDesc, null);
	}

	public Cursor selectEntries(String table, String[] fields, String where, String limit) {
		return selectEntries(table, fields, where, null, false, limit);
	}

	public Cursor selectEntries(String table, String[] fields, String where, String orderBy,
			boolean orderDesc, String limit) {
		Cursor cursor;

		if (orderBy != null) orderBy += (orderDesc) ? " DESC" : " ASC";
		cursor = mDb.query(true, table, fields, where, null, null, null, orderBy, limit);
		if (cursor != null && !cursor.moveToFirst()) {
			cursor.close();
			cursor = null;
		}
		return cursor;
	}

	public int getEntriesCount(String table) {
		return getEntriesCount(table, null);
	}

	public int getEntriesCount(String table, String where) {
		Cursor cursor;
		int count = -1;
		cursor = mDb.rawQuery("SELECT count(*) FROM " + table
				+ ((where != null) ? " WHERE " + where : ""), null);

		if (cursor == null) {
			log.e("Cursor instance creation failed.");
			return count;
		}

		try {
			if(cursor.moveToFirst())
				count = cursor.getInt(0);
		} catch (SQLException e) {
			log.e("Could not move cursor to expected record.");
		}
		cursor.close();

		return count;
	}

	public static class Table {
		String mName;
		String mCreateStatement;

		public Table(String name, String createStatement) {
			mName = name;
			mCreateStatement = createStatement;
		}

		public String getName() {
			return mName;
		}

		public String getCreateStatement() {
			return mCreateStatement;
		}
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		List<Table> mTables;

		DatabaseHelper(Context context, String name, int version, List<Table> tables) {
			super(context, name, null, version);
			mTables = tables;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			for(int i = 0; i < mTables.size(); i++)
				db.execSQL(mTables.get(i).getCreateStatement());
		}

		private void regenerate_tables(SQLiteDatabase db, int reason,
				int oldVersion, int newVersion) {
			log.w(((reason == 1) ? "Up" : "Down")
				+ "grading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
			for(int i = 0; i < mTables.size(); i++)
				db.execSQL("DROP TABLE IF EXISTS " + mTables.get(i).getName());
			onCreate(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			regenerate_tables(db, 1, oldVersion, newVersion);
		}

		@Override
		public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			regenerate_tables(db, 1, oldVersion, newVersion);
		}
	}
}
