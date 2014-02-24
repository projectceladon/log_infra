/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intel.crashreport;

import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.intel.crashreport.specific.EventDB;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {

	//The GCM Sender ID used by CrashTool to send messages to devices over the Google Cloud Messaging service.
	public static final String SENDER_ID = "703896775006";

	@SuppressWarnings("hiding")
	private static final String TAG = "GCMIntentService";

	public GCMIntentService() {
		super(SENDER_ID);
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		Log.i(TAG, "Device registered: regId = " + registrationId);
		GCMRegistrar.setRegisteredOnServer(context, true);
		//generating an info event to track this registration
		ApplicationPreferences privatePrefs = new ApplicationPreferences(context);
		if(!registrationId.equals(privatePrefs.getGcmToken())) {
			privatePrefs.setGcmToken(registrationId);
			GcmEvent.INSTANCE.registerGcm(registrationId);
		}
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		Log.i(TAG, "Device unregistered");
		GCMRegistrar.setRegisteredOnServer(context, false);
		//generating an info event to track this unregistration
		ApplicationPreferences privatePrefs = new ApplicationPreferences(context);
		privatePrefs.setGcmToken("");
		GcmEvent.INSTANCE.unregisterGcm();
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		if(intent == null) {
			return;
		}
		CrashReport app = (CrashReport)context.getApplicationContext();
		if(app.isGcmEnabled()) {
			Log.i(TAG, "Received message context:"+context+", intent:"+intent);

			// if your key/value is a JSON string, just extract it and parse it using JSONObject
			Bundle incomingExtras = intent.getExtras();
			if(incomingExtras != null && intent.hasExtra(GcmMessage.GCM_EXTRA_TITLE) && intent.hasExtra(GcmMessage.GCM_EXTRA_TEXT)
					&& intent.hasExtra(GcmMessage.GCM_EXTRA_TYPE)) {
				String title = incomingExtras.getString(GcmMessage.GCM_EXTRA_TITLE);
				String text = incomingExtras.getString(GcmMessage.GCM_EXTRA_TEXT);
				String type = incomingExtras.getString(GcmMessage.GCM_EXTRA_TYPE);
				if(title == null || text == null || type == null) {
					// Do nothing if either one of these is null
					return;
				}
				String data = "";
				if(intent.hasExtra(GcmMessage.GCM_EXTRA_DATA)) {
					data = incomingExtras.getString(GcmMessage.GCM_EXTRA_DATA);
				}
				if(!title.isEmpty() && !text.isEmpty() && !type.isEmpty() && GcmMessage.typeExist(type)) {
					EventDB db = new EventDB(context);
					try {
						// Write the message to database
						db.open();
						int lastMessage = db.getLastGCMRowId();
						GcmMessage message = new GcmMessage(lastMessage, title, text, type, data, false);
						Long rowId = db.addGcmMessage(message);
						db.close();
						message.setRowId(rowId.intValue());
						// Notify the user
						NotificationMgr nMgr = new NotificationMgr(context);
						// The message can now be safely used for notification
						// because the database insertion will have consolidated
						// it if required.
						nMgr.notifyGcmMessage(message);
					} catch (SQLException e) {
						Log.e(TAG,"onMessage: SQLException");
					}
				}
			}
		}
	}

	@Override
	protected void onDeletedMessages(Context context, int total) {
		Log.i(TAG, "Received deleted messages notification");
	}

	@Override
	public void onError(Context context, String errorId) {
		Log.i(TAG, "Received error: " + errorId);
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		// log message
		Log.i(TAG, "Received recoverable error: " + errorId);
		return super.onRecoverableError(context, errorId);
	}

}
