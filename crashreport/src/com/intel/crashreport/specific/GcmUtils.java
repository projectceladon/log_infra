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

package com.intel.crashreport.specific;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import com.intel.crashreport.database.EventDB;
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
	 * @return boolean value indicating whether or not entries within
	 *		the database have been updated.
	 */
	public boolean markAllAsRead() {
		// Update all the GCM messages
		boolean modified = false;
		try {
			EventDB db = new EventDB(this.getContext());
			db.open();
			modified = db.markAllGcmMessagesAsRead();
			db.close();
		}
		catch (SQLException e){
			Log.e("Exception occured while generating GCM messages list :" + e.getMessage());
		}
		return modified;
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
