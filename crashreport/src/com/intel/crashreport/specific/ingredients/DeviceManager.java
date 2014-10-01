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

package com.intel.crashreport.specific.ingredients;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.intel.crashreport.Log;
import com.intel.crashreport.specific.Event;
import com.intel.crashreport.specific.EventDB;
import com.intel.phonedoctor.utils.FileOps;

import android.database.Cursor;

public enum DeviceManager {
	INSTANCE;

	private static final String LATE_MODEMVERSION_NAME = "modem_version.txt";
	private static List<String> lMpanicNotReady = new ArrayList<String>();

	private String getModemValue(){
		return IngredientManager.INSTANCE.getIngredient("modem");
	}

	public boolean isModemUnknown(){
		String sModem = getModemValue();

		if (sModem == null || sModem.isEmpty() ||
			sModem.equalsIgnoreCase("unknown"))
			return true;

		return false;
	}

	private String getModemExtValue(){
		return IngredientManager.INSTANCE.getIngredient("modemext");
	}

	public boolean hasModemExtension(boolean refreshIndredients){
		if (refreshIndredients)
			IngredientManager.INSTANCE.refreshIngredients();

		return (getModemExtValue() != null);
	}

	public void addEventMPanicNotReady(String aEventid) {
		lMpanicNotReady.add(aEventid);
	}

	public void removeEventMPanicNotReady(String aEventid) {
		lMpanicNotReady.remove(aEventid);
	}

	public void checkMpanicNotReady(EventDB db) {
		if (lMpanicNotReady.isEmpty()) {
			return;
		}

		IngredientManager.INSTANCE.refreshIngredients();
		if (isModemUnknown()) {
			return;
		}
		//we have modem events to update!
		for (String sId : lMpanicNotReady){
			Cursor cursor = db.getEventFromId(sId);
			Event curEvent = db.fillEventFromCursor(cursor);
			cursor.close();

			if (curEvent == null)
				continue;

			//get the crashlogpath to create modemversion file
			if (!curEvent.getCrashDir().isEmpty()) {
				String sModemFilePath = curEvent.getCrashDir() + "/" + LATE_MODEMVERSION_NAME;
				Map<String,String> parsedValue = new HashMap<String,String>();
				parsedValue.put("modem", getModemValue());
				JSONObject aJson = new JSONObject(parsedValue);

				try {
					FileOps.fileWriteString(sModemFilePath, aJson.toString());
				} catch (FileNotFoundException e) {
					Log.e( "file "+ sModemFilePath +" is not found", e);
				} catch (IOException e) {
					Log.e( "can't write in file "+ sModemFilePath, e);
				}
			} else {
				Log.w("no crashdir, can't put modem file : " + sId );
			}
			//finally, we tag the event as ready
			db.updateEventDataReady(sId);
			removeEventMPanicNotReady(sId);
		}
	}
}
