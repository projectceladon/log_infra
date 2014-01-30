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
 * Author: Jeremy Rocher <jeremyx.rocher@intel.com>
 */

package com.intel.crashreport.specific;

import java.util.Date;

import com.intel.crashreport.GeneralEventDB;
import com.intel.crashreport.Log;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

public class EventDB extends GeneralEventDB{

	private static final int BEGIN_FIBONACCI = 13;
	private static final int BEGIN_FIBONACCI_BEFORE = 8;
	private static final int RAIN_DURATION_MAX = 3600;
	private static final int MAX_DELAY_RAIN = 600;
	private static final int RAIN_CRASH_NUMBER = 10;

	public static final String KEY_REASON = "reason";
	public static final String KEY_OCCURRENCES = "occurrences";
	public static final String KEY_LAST_FIBONACCI = "last_fibo";
	public static final String KEY_NEXT_FIBONACCI = "next_fibo";
	public static final String KEY_RAINID = "raindId";

	public EventDB(Context ctx) {
		super(ctx);
	}

	/**
	 * Add the input event in the Black events database
	 *
	 * @param event is the event to add in the Blacklisted events db
	 * @param reason is the reason set in the db "reason" column
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 * @throws SQLException
	 */
	public long addBlackEvent(Event event, String reason) throws SQLException {
		ContentValues initialValues = new ContentValues();

		if(reason.equals("RAIN")) {
			Cursor cursor = getRainEventInfo(new RainSignature(event));
			if(cursor != null){
				initialValues.put(KEY_RAINID, cursor.getString(cursor.getColumnIndex(KEY_ID)));
				cursor.close();
			}
		}

		initialValues.put(KEY_ID, event.getEventId());
		initialValues.put(KEY_REASON, reason);
		initialValues.put(KEY_CRASHDIR, event.getCrashDir());

		return mDb.insert(DATABASE_BLACK_EVENTS_TABLE, null, initialValues);
	}

	public Cursor fetchAllBlackEvents() throws SQLException {
		return mDb.query(DATABASE_BLACK_EVENTS_TABLE, new String[] {KEY_ID, KEY_REASON}, null, null, null, null, null);
	}

	public Cursor fetchBlackEventsFromQuery(String query) throws SQLException {
		Cursor mCursor;
		mCursor = mDb.query(DATABASE_BLACK_EVENTS_TABLE, new String[] {KEY_ID, KEY_REASON,KEY_CRASHDIR}, query, null, null, null, null);
		if(mCursor != null)
			mCursor.moveToFirst();
		return mCursor;
	}

	/**
	 * perform the given query on the rain of crashes database table
	 *
	 * @param query to perform
	 * @return the cursor set to the first row if found, null otherwise
	 * @throws SQLException
	 */
	public Cursor fetchRainOfCrashesFromQuery(String query) throws SQLException {
		Cursor mCursor;
		mCursor = mDb.query(DATABASE_RAIN_OF_CRASHES_TABLE, new String[] {KEY_DATE,KEY_TYPE,KEY_DATA0,KEY_DATA1,KEY_DATA2,KEY_DATA3,KEY_ID,KEY_OCCURRENCES,KEY_LAST_FIBONACCI,KEY_NEXT_FIBONACCI}, query, null, null, null, null);
		if(mCursor != null)
			mCursor.moveToFirst();
		return mCursor;
	}

	public Cursor fetchBlackEventsRain(String rainId) throws SQLException {
		String query = KEY_RAINID + " = '"+ rainId +"'";
		return fetchBlackEventsFromQuery(query);
	}

	/**
	 * Fetch all elements from Rain of Crashes database with a date anterior
	 * to (current date - rain of crashes maximum duration)
	 *
	 * @param date is the current date
	 * @return the cursor set to the first row if found, null otherwise
	 * @throws SQLException
	 */
	public Cursor fetchLastRain(Date date) throws SQLException {
		int mDate = convertDateForDb(date);
		mDate -=  RAIN_DURATION_MAX;
		String query = KEY_DATE + " < " + mDate;
		return fetchRainOfCrashesFromQuery(query);
	}

	/**
	 * @brief add a rain event in the rain of crashes database
	 *
	 * @param event must contains the input of which the rain to add is made
	 * @return the row ID of the newly inserted row(here the rain event), or -1 if
	 * an error occurred
	 */
	public long addRainEvent(Event event) {
		ContentValues initialValues = new ContentValues();
		//Get the rain signature associated to this event
		RainSignature signature = new RainSignature(event);
		initialValues.put(KEY_TYPE, signature.getType());
		initialValues.put(KEY_DATA0, signature.getData0());
		initialValues.put(KEY_DATA1, signature.getData1());
		initialValues.put(KEY_DATA2, signature.getData2());
		initialValues.put(KEY_DATA3, signature.getData3());
		initialValues.put(KEY_DATE, convertDateForDb(event.getDate()));
		initialValues.put(KEY_OCCURRENCES, 1);
		initialValues.put(KEY_LAST_FIBONACCI, BEGIN_FIBONACCI_BEFORE);
		initialValues.put(KEY_NEXT_FIBONACCI, BEGIN_FIBONACCI);
		initialValues.put(KEY_ID, event.getEventId());

		return mDb.insert(DATABASE_RAIN_OF_CRASHES_TABLE, null, initialValues);
	}

	private Cursor getRainEventInfo(RainSignature signature) throws SQLException {
		return fetchRainOfCrashesFromQuery(signature.querySignature());
	}

	/**
	 * Fetch the rain event from the rain of crashes db that matchs the input
	 * signature and generate an event RAIN with its number of occurrences
	 *
	 * @param signature is the signature of the ended rain
	 * @throws SQLException
	 */
	public void endOfRain(RainSignature signature) throws SQLException {
		Cursor cursor = getRainEventInfo(signature);

		if(cursor != null) {
			int occurences = cursor.getInt(cursor.getColumnIndex(KEY_OCCURRENCES));
			cursor.close();

			if( occurences > 0)
				EventGenerator.INSTANCE.generateEventRain(signature, occurences);
		}

	}

	public boolean resetRainEvent(RainSignature signature, Date date) throws SQLException {
		ContentValues args = new ContentValues();
		endOfRain(signature);
		args.put(KEY_OCCURRENCES, 1);
		args.put(KEY_DATE, convertDateForDb(date) );
		args.put(KEY_LAST_FIBONACCI, BEGIN_FIBONACCI_BEFORE);
		args.put(KEY_NEXT_FIBONACCI, BEGIN_FIBONACCI);
		return mDb.update(DATABASE_RAIN_OF_CRASHES_TABLE, args, signature.querySignature(), null) > 0;
	}

	public boolean updateRainEvent(RainSignature signature, Date date) throws SQLException {
		ContentValues args = new ContentValues();

		Cursor cursor = getRainEventInfo(signature);
		if(cursor != null) {
			int occurences = cursor.getInt(cursor.getColumnIndex(KEY_OCCURRENCES));
			int nextFibo = cursor.getInt(cursor.getColumnIndex(KEY_NEXT_FIBONACCI));
			int lastFibo = cursor.getInt(cursor.getColumnIndex(KEY_LAST_FIBONACCI));

			occurences++;

			args.put(KEY_DATE, convertDateForDb(date) );
			cursor.close();
			if( occurences == nextFibo) {
				args.put(KEY_NEXT_FIBONACCI, lastFibo + nextFibo);
				args.put(KEY_LAST_FIBONACCI, nextFibo);
				args.put(KEY_OCCURRENCES, 0);
				EventGenerator.INSTANCE.generateEventRain(signature, nextFibo);
			}
			else
				args.put(KEY_OCCURRENCES, occurences);


			return mDb.update(DATABASE_RAIN_OF_CRASHES_TABLE, args, signature.querySignature(), null) > 0;
		}
		return false;

	}

	public boolean deleteRainEvent(RainSignature signature) {
		String whereQuery = signature.querySignature();
		endOfRain(signature);
		return mDb.delete(DATABASE_RAIN_OF_CRASHES_TABLE, whereQuery, null) > 0;
	}

	public boolean isRainEventExist(RainSignature signature) {
		Cursor mCursor;
		boolean ret;
		try {
			mCursor = mDb.query(true, DATABASE_RAIN_OF_CRASHES_TABLE, new String[] {KEY_TYPE,KEY_DATA0,KEY_DATA1,KEY_DATA2,KEY_DATA3},
					signature.querySignature(), null,
					null, null, null, null);
			if (mCursor != null) {
				ret = mCursor.moveToFirst();
				mCursor.close();
				return ret;
			}
		} catch (SQLException e) {
			Log.e("isRainEventExist : " + e.getMessage());
		}
		return false;
	}

	/**
	 * Check if the input event is in the Black_Events database
	 *
	 * @param eventId is the event to check
	 * @return true is the event is in the Black_Events db. False otherwise
	 * @throws SQLException
	 */
	public boolean isEventInBlackList(String eventId) throws SQLException{
		Cursor mCursor;
		Boolean ret;
		mCursor = mDb.query(true, DATABASE_BLACK_EVENTS_TABLE, new String[] {KEY_ID},
				KEY_ID + " = '" + eventId + "'", null,
				null, null, null, null);
		if (mCursor != null) {
			ret = mCursor.moveToFirst();
			mCursor.close();
			return ret;
		}
		return false;
	}

	/**
	 * Check if the input event belongs to the last rain of crashes with signature
	 * matching the input event signature. If the date of the input crash doesn't
	 * exceed the maximum delay, it is considered as part of the current rain.
	 *
	 * @param event to test
	 * @return true if the event belongs to the current rain with matching signature. False otherwise.
	 * @throws SQLException
	 */
	public boolean isInTheCurrentRain(Event event) throws SQLException {
		Date date = event.getDate();

		Cursor mCursor = getRainEventInfo(new RainSignature(event));
		if (mCursor != null) {
			int lastEvent = mCursor.getInt(mCursor.getColumnIndex(KEY_DATE));
			mCursor.close();
			int newDate = convertDateForDb(date);
			if( lastEvent <= newDate) {
				if ( (newDate - lastEvent) <=  MAX_DELAY_RAIN) {
					return true;
				}
				else
					return false;
			}
		}
		return false;
	}

	/**
	 * Return the date of the last event contained in the rain of crashes that
	 * matches the input crash signature
	 *
	 * @param signature of the crashes belonging to the rain
	 * @return the date value of the last event belonging to the rain matching
	 * the input signature. 0 if no rain matching the input signature exist.
	 * @throws SQLException
	 */
	public int getLastCrashDate(CrashSignature signature) throws SQLException {

		Cursor mCursor = getRainEventInfo(new RainSignature(signature));
		if (mCursor != null) {
			int lastEvent = mCursor.getInt(mCursor.getColumnIndex(KEY_DATE));
			mCursor.close();
			return lastEvent;
		}
		return 0;
	}

	public boolean checkNewRain(Event event, int lastRain) {
		Cursor mCursor;
		int lastEvent;
		Date date = event.getDate();
		CrashSignature signature = new CrashSignature(event);
		RainSignature rainSignature = new RainSignature(event);

		if(-1 != lastRain)
			lastEvent = lastRain;
		else {
			lastEvent = convertDateForDb(date);
			lastEvent -= RAIN_DURATION_MAX;
		}
		//Fetch from events database the events with matching signature and with a matching date value
		String whereQuery = signature.querySignature() + " AND " + KEY_DATE + " > " + lastEvent;

		int count = -1;

		try {
			mCursor = mDb.query(true, "events", new String[] {"date"},
					whereQuery, null,
					null, null, "date desc", "0,"+RAIN_CRASH_NUMBER);
			if (mCursor != null) {
				count = mCursor.getCount();
				mCursor.close();
				if (count == RAIN_CRASH_NUMBER) {
					if (-1 == lastRain)
						addRainEvent(event);
					else
						resetRainEvent(rainSignature, date);
					EventGenerator.INSTANCE.generateEventRain(rainSignature, RAIN_CRASH_NUMBER);
					return true;
				}
			}
		}
		catch (SQLException e){
			Log.e("checkNewRain : " + e.getMessage());
		}
		return false;
	}

	public boolean isPathUploaded(String sPath) throws SQLException {
		Cursor mCursor;
		Boolean ret;
		mCursor = mDb.query(true, DATABASE_TABLE, new String[] {KEY_ID},
				KEY_CRASHDIR + " = '" + sPath + "' AND " + KEY_UPLOADLOG + "=1", null,
				null, null, null, null);
		if (mCursor != null) {
			ret = mCursor.moveToFirst();
			mCursor.close();
			return ret;
		}
		return false;
	}

	public boolean checkNewRain(Event event) {
		return checkNewRain(event, -1);
	}
}
