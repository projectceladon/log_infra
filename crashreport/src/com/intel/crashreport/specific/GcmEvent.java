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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.intel.crashreport.ApplicationPreferences;
import com.intel.crashreport.CustomizableEventData;
import com.intel.crashreport.database.EventDB;
import com.intel.crashreport.GeneralEventGenerator;
import com.intel.crashreport.Log;
import com.intel.crashreport.specific.GcmMessage.GCM_ACTION;

public enum GcmEvent {

	INSTANCE;
	private Context context;

	public void registerGcm(String registrationId) {
		CustomizableEventData mEvent = generateGcmRegisterEvent();
		mEvent.setData1("ON");
		mEvent.setData3(registrationId);
		GeneralEventGenerator.INSTANCE.generateEvent(mEvent);
	}

	public void unregisterGcm() {
		CustomizableEventData mEvent = generateGcmRegisterEvent();
		mEvent.setData1("OFF");
		GeneralEventGenerator.INSTANCE.generateEvent(mEvent);
	}

	public void enableGcm() {
		CustomizableEventData mEvent = generateGcmActivationEvent();
		mEvent.setData1("ON");
		GeneralEventGenerator.INSTANCE.generateEvent(mEvent);
	}

	public void disableGcm() {
		CustomizableEventData mEvent = generateGcmActivationEvent();
		mEvent.setData1("OFF");
		GeneralEventGenerator.INSTANCE.generateEvent(mEvent);
	}

	private CustomizableEventData generateGcmRegisterEvent() {
		CustomizableEventData mEvent = EventGenerator.INSTANCE.getEmptyInfoEvent();
		mEvent.setType("GCM");
		mEvent.setData0("REGISTER_ID");
		return mEvent;
	}

	private CustomizableEventData generateGcmActivationEvent() {
		CustomizableEventData mEvent = EventGenerator.INSTANCE.getEmptyInfoEvent();
		mEvent.setType("GCM");
		mEvent.setData0("USER_ACTION");
		return mEvent;
	}

	public void setContext(Context ctx) {
		context = ctx;
	}

	public synchronized void checkTokenGCM(){
		final String regId;
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean isConnected = false;
		if(cm != null){
			NetworkInfo ni = cm.getActiveNetworkInfo();
			if((ni != null) && ni.isConnected()) {
				Log.d("Active network info: " + ni.toString());
				isConnected = true;
			} else {
				Log.d("No network info available.");
			}
		}
		if(isConnected) {
			Log.d("GCMRegistrar");
			try{
				GCMRegistrar.checkDevice(context);
				regId = GCMRegistrar.getRegistrationId(context);
				if (regId.equals("")) {
					// Automatically registers application on startup.
					Log.d("not registered, trying...");
					GCMRegistrar.register(context, GCMIntentService.SENDER_ID);
				} else {
					// Device is already registered on GCM, check server.
					if (GCMRegistrar.isRegisteredOnServer(context)) {
						// Skips registration.
						Log.d( "Already registered");
						Log.d("GCM TOKEN GCMRegistrar = " + regId);
						ApplicationPreferences privatePrefs = new ApplicationPreferences(context);
						if(!regId.equals(privatePrefs.getGcmToken())) {
							privatePrefs.setGcmToken(regId);
							GcmEvent.INSTANCE.registerGcm(regId);
						}
					}else{
						//there is a problem, need to unregister/register
						GCMRegistrar.unregister(context);
						GCMRegistrar.register(context, GCMIntentService.SENDER_ID);
						Log.d("not registered on server...");
					}
				}
			}
			catch(IllegalStateException e){
				Log.w("GCMRegistrar: GCM not fully installed");
			}
			catch(UnsupportedOperationException e){
				Log.w("GCMRegistrar: GCM not supported");
			}
		} else {
			Log.d("No network connection for GCM registration.");
		}
	}

	/**
	 * Performs the action associated with a Gcm message:
	 * <ul>
	 * <li>open a web browser for an URL message</li>
	 * <li>open an application for an APP message</li>
	 * <li>start/stop a MPM (Kratos) session</li>
	 * <li>nothing for a NONE message</li>
	 * </ul>
	 * @param rowId the message's row ID
	 * @param type the message's action
	 * @param data the message's data
	 * @return
	 * <ul>
	 * <li><code>true</code> if the action has been done successfully</li>
	 * <li><code>false</code> otherwise</li>
	 * </ul>
	 */
	public boolean takeGcmAction(int rowId, GCM_ACTION type, String data) {
		boolean result = false;
		EventDB db = new EventDB(context);
		try {
			db.open();
			db.updateGcmMessageToCancelled(rowId);
			result = true;
			db.close();
		}
		catch (SQLException e){
			Log.e("Exception occured while generating GCM messages list :" + e.getMessage());
		}

		switch(type) {
		case GCM_URL: {
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(data));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
			catch (NullPointerException e) {
				Log.w("CrashReport:takeGcmAction: no url:"+data);
			}
			catch (ActivityNotFoundException e) {
				Log.w("CrashReport:takeGcmAction: bad url format:"+data);
			}
			break;
		}
		case GCM_APP: {
			try {
				Intent intent = context.getPackageManager().getLaunchIntentForPackage(data);
				if (intent != null)
				{
					// start the activity
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent);
				}
				else
				{
					// bring user to the market
					// or let them choose an app?
					intent = new Intent(Intent.ACTION_VIEW);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setData(Uri.parse("market://details?id="+data));
					context.startActivity(intent);
				}
			}
			catch (NullPointerException e) {
				Log.w("CrashReport:takeGcmAction: no application:"+data);
			}
			catch (ActivityNotFoundException e) {
				Log.w("CrashReport:takeGcmAction: can't open application:"+data);
			}
			break;
		}
		case GCM_PHONE_DOCTOR: {
			// Create an intent for MPM
			Intent mpmIntent = buildMpmBroadcastIntent(data);
			// If the intent is valid
			if(mpmIntent != null) {
				Log.d("[GCM] Broadcasting intent: " + mpmIntent);
				// Start the activity
				context.sendBroadcast(mpmIntent);
			} else {
				// Otherwise display a text for the end user.
				Toast.makeText(
						context,
						"Invalid input data from GCM message.",
						Toast.LENGTH_SHORT).show();
			}
			break;
		}
		default: break;
		}
		return result;
	}

	/**
	 * Returns a new <code>Intent</code> instance to be broadcasted.
	 *
	 * The new <code>Intent</code> is built from the given <code>data</code>
	 * that came with a <i>GCM</i> message.
	 *
	 * @param data the data from the GCM message.
	 * @return the new <i>MPM</i> intent (or null if input data was
	 * 	not valid).
	 */
	private static Intent buildMpmBroadcastIntent(String data) {
		// Initialize an action
		String action = null;
		// Update the action according to the input parameter
		if(GcmMessage.GCM_KRATOS_START.equals(data)) {
			action = GcmMessage.MPM_ACTION_START;
		} else if(GcmMessage.GCM_KRATOS_STOP.equals(data)) {
			action = GcmMessage.MPM_ACTION_STOP;
		}
		// If data was not valid return null
		if(action == null) {
			return null;
		}
		// Else build an Intent to broadcast
		Intent mpmIntent = new Intent();
		// Mark the intent as a new task
		mpmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// Configure the intent action
		mpmIntent.setAction(action);
		// Configure the calling application name
		mpmIntent.putExtra(
				GcmMessage.MPM_EXTRA_CALLING_APP_NAME,
				GcmMessage.MPM_EXTRA_VALUE_CALLING_APP);
		// Configure the profile name.
		mpmIntent.putExtra(
				GcmMessage.MPM_EXTRA_PROFILE_NAME,
				GcmMessage.MPM_EXTRA_VALUE_PROFILE);
		// Return the intent
		return mpmIntent;
	}
}
