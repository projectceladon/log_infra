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
 * Author: Nicolas Benoit <nicolasx.benoit@intel.com>
 */

package com.intel.crashreport;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.google.android.gcm.GCMRegistrar;
import com.intel.crashreport.GcmMessage.GCM_ACTION;
import com.intel.crashreport.specific.EventDB;
import com.intel.crashreport.specific.EventGenerator;

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
	 * Do the action associated with a Gcm message
	 * - Open a web browser for an URL message
	 * - Open an app for an APP message
	 * - Nothing for a NONE message
	 * @param message
	 * @return true if the action has been done successfully
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

		if(GCM_ACTION.GCM_URL == type) {
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
		} else if(GCM_ACTION.GCM_APP == type) {
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
		}
		return result;
	}

}
