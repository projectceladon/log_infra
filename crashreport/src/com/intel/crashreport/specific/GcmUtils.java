/* Phone Doctor (CLOTA)
 *
 * Copyright (C) Intel 2014
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
 * Author: Adrien Sebbane <adrienx.sebbane@intel.com>
 */

package com.intel.crashreport.specific;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import com.intel.crashreport.Log;

/**
 * A class that groups some actions that can be performed <i>on GCM</i>
 * messages.
 *
 * @author asebbanx
 */
public class GcmUtils {

	private final Context context;

	public GcmUtils(Context context) {
		this.context = context;
	}

	/**
	 * Returns this object's associated context.
	 *
	 * @return this object's context.
	 */
	public Context getContext() {
		return this.context;
	}

	/**
	 * Returns the list of <i>GCM</i> messages that
	 * match the given unread/all status.
	 *
	 * @param unreadOnly a boolean indicating whether we want to
	 * 		retrieve unread messages only or not.
	 *
	 * @return the <i>GCM</i> messages
	 */
	private ArrayList<GcmMessage> getMessages(boolean unreadOnly) {
		ArrayList<GcmMessage> messageList = null;
		EventDB db = new EventDB(this.getContext());
		try {
			db.open();
			Cursor cursor = null;
			if(unreadOnly) {
				cursor = db.fetchNewGcmMessages();
			} else {
				cursor = db.fetchAllGcmMessages();
			}
			messageList = new ArrayList<GcmMessage>();
			if (cursor != null) {
				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					GcmMessage gMessage = db.fillGCMFromCursor(cursor);
					messageList.add(gMessage);
					cursor.moveToNext();
				}
				cursor.close();
			}
			db.close();
		}
		catch (SQLException e){
			Log.e("Exception occured while generating GCM messages list :" + e.getMessage());
		}
		return messageList;
	}

	/**
	 * Returns the list of all <i>GCM</i> messages.
	 *
	 * @return all <i>GCM</i> messages.
	 */
	public ArrayList<GcmMessage> getList() {
		return this.getMessages(false);
	}
	/**
	 * Returns the list of all unread <i>GCM</i> messages.
	 *
	 * @return the unread <i>GCM</i> messages.
	 */
	public ArrayList<GcmMessage> getNonRead() {
		return this.getMessages(true);
	}

	/**
	 * Marks all the GCM messages as read.
	 *
	 * @return the number of messages that have been updated.
	 */
	public int markAllAsRead() {
		// Update all the GCM messages
		int count = -1;
		try {
			EventDB db = new EventDB(this.getContext());
			db.open();
			count = db.markAllGcmMessagesAsRead();
			db.close();
			return count;
		}
		catch (SQLException e){
			Log.e("Exception occured while generating GCM messages list :" + e.getMessage());
		}
		return count;
	}

	/**
	 * Marks the GCM message with the given row id as read.
	 *
	 * @return <code>true</code> if the operation succeeded
	 * 	(and <code>false</code> otherwise).
	 */
	public boolean markAsRead(int gcmRowId) {
		boolean operationStatus = false;
		try {
			EventDB db = new EventDB(this.getContext());
			db.open();
			db.updateGcmMessageToCancelled(gcmRowId);
			operationStatus = db.updateGcmMessageToCancelled(gcmRowId);
			db.close();
		}
		catch (SQLException e){
			Log.e("Exception occured while generating GCM messages list :" + e.getMessage());
		}
		return operationStatus;
	}

	/**
	 * Returns the number of unread GCM messages.
	 *
	 * @return the number of unread GCM messages (and -1 in case of error).
	 */
	public long getUnreadMessageCount() {
		return this.getMessageCount(true);
	}

	/**
	 * Returns the total number of GCM messages.
	 *
	 * @return the number of GCM messages (and -1 in case of error).
	 */
	public long getMessageCount() {
		return this.getMessageCount(false);
	}

	private long getMessageCount(boolean unreadOnly) {
		long count = -1;
		try {
			EventDB db = new EventDB(this.getContext());
			db.open();
			if(unreadOnly) {
				count = db.getUnreadGcmMessageCount();
			} else {
				count = db.getTotalGcmMessageCount();
			}
			db.close();
		} catch (SQLException e){
			Log.e("Exception occured counting GCM messages:" + e.getMessage());
		}
		return count;
	}
}
