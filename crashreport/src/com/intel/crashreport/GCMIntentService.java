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


import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.util.Log;

import com.intel.crashreport.specific.EventDB;
import com.intel.crashreport.specific.EventGenerator;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

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
		GcmEvent.INSTANCE.registerGcm(registrationId);
		ApplicationPreferences privatePrefs = new ApplicationPreferences(context);
		privatePrefs.setGcmToken(registrationId);
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		Log.i(TAG, "Device unregistered");
		GCMRegistrar.setRegisteredOnServer(context, false);
		//generating an info event to track this unregistration
		GcmEvent.INSTANCE.unregisterGcm();
		ApplicationPreferences privatePrefs = new ApplicationPreferences(context);
		privatePrefs.setGcmToken("");
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		CrashReport app = (CrashReport)context.getApplicationContext();
		if(app.isGcmEnabled()) {
			Log.i(TAG, "Received message context:"+context+", intent:"+intent);

			// if your key/value is a JSON string, just extract it and parse it using JSONObject
			if(intent.hasExtra(GcmMessage.GCM_EXTRA_TITLE) && intent.hasExtra(GcmMessage.GCM_EXTRA_TEXT)
					&& intent.hasExtra(GcmMessage.GCM_EXTRA_TYPE)) {
				String title = intent.getExtras().getString(GcmMessage.GCM_EXTRA_TITLE);
				String text = intent.getExtras().getString(GcmMessage.GCM_EXTRA_TEXT);
				String type = intent.getExtras().getString(GcmMessage.GCM_EXTRA_TYPE);
				String data = "";
				if(intent.hasExtra(GcmMessage.GCM_EXTRA_DATA)) {
					data = intent.getExtras().getString(GcmMessage.GCM_EXTRA_DATA);
				}
				if(!title.isEmpty() && !text.isEmpty() && !type.isEmpty() && GcmMessage.typeExist(type)) {
					EventDB db = new EventDB(context);
					try {
						db.open();
						db.addGcmMessage(title, text, type, data);
						int nbMessages = db.getNewGcmMessagesNumber();
						int lastMessage = db.getLastGCMRowId();
						db.close();
						// notifies user
						GcmMessage message = new GcmMessage(lastMessage, title, text, type, data, false);
						NotificationMgr nMgr = new NotificationMgr(context);
						nMgr.notifyGcmMessage(nbMessages, message);
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
