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
