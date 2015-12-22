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

package com.intel.crashreport.specific.ingredients;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.intel.crashreport.Log;
import com.intel.crashreport.specific.Event;
import com.intel.crashreport.database.EventDB;
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
		if (!IngredientManager.INSTANCE.isIngredientEnabled()) {
			return false;
		}

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

	public void checkMpanicNotReady(EventDB db) {
		if (lMpanicNotReady.isEmpty()) {
			return;
		}

		IngredientManager.INSTANCE.refreshIngredients();
		if (isModemUnknown()) {
			return;
		}
		//we have modem events to update!
		Iterator<String> it = lMpanicNotReady.iterator();
		while (it.hasNext()){
			String sId = it.next();
			Cursor cursor = db.getEventFromId(sId);
			Event curEvent = new Event(db.fillEventFromCursor(cursor));
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
			it.remove();
		}
	}
}
