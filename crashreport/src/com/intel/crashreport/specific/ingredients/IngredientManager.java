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

import com.intel.crashreport.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.os.SystemProperties;

import org.json.JSONObject;
import org.json.JSONException;

public enum IngredientManager {
	INSTANCE;

	private static final String ING_CONF_FILE_PATH = "/system/vendor/etc/ingredients.conf";

	private boolean bNeedRefresh = true;
	private JSONObject lastIngredients = null;
	List<String> sUniqueKeyList = new ArrayList<String>();


	public List<String> getUniqueKeyList() {
		if (bNeedRefresh){
			RefreshUniqueKey();
		}
		return sUniqueKeyList;
	}

	private void RefreshUniqueKey(){
		// test conf file exits
		File confFile = new File(ING_CONF_FILE_PATH);
		if (!confFile.exists()){
			//no file => no refresh
			bNeedRefresh = false;
			return;
		}
		sUniqueKeyList.clear();
		IngredientBuilder builder_unique = new FileIngredientBuilder(ING_CONF_FILE_PATH);
		// Retrieve the ingredients
		JSONObject ingredients_unique = builder_unique.getIngredients();
		for(String key : ingredients_unique.keySet()) {
			// Add each ingredient value to the JSON string
			String value = null;
			try {
				value = ingredients_unique.getString(key);
			} catch (JSONException e) {
				Log.e("Could not retrieve value from ingredients");
			}
			if(value != null) {
				if (value.equalsIgnoreCase("true")) {
					sUniqueKeyList.add(key);
				}
			}
		}
		//string updated, no need to refresh
		bNeedRefresh = false;
	}

	public List<String> parseUniqueKey(String aKey) {
		List<String> resultList = new ArrayList<String>();
		String filteredKey = aKey.replaceAll("\\[", "" );
		filteredKey = filteredKey.replaceAll("\\]", "" );
		String[] tmpList = filteredKey.split(", ");
		for (String retval:tmpList) {
			resultList.add(retval);
		}
		return resultList;
	}

	public void storeLastIngredients(JSONObject aIng) {
		lastIngredients = aIng;
	}

	public JSONObject getLastIngredients() {
		return lastIngredients;
	}

	public JSONObject getDefaultIngredients() {
		JSONObject ingredients = new JSONObject();
		try {
			ingredients.put("ifwi", SystemProperties.get("sys.ifwi.version"));
			ingredients.put("pmic", SystemProperties.get("sys.pmic.version"));
			ingredients.put("punit", SystemProperties.get("sys.punit.version"));
			ingredients.put("modem", SystemProperties.get("gsm.version.baseband"));
		} catch (JSONException e) {
			Log.e("Could not set up default ingredients");
		}
		return ingredients;
	}

	public String getIngredient(String key) {
		if (lastIngredients == null)
			return null;
		try {
			return lastIngredients.getString(key);
		} catch (JSONException e) {
			return null;
		}
	}

	public void refreshIngredients() {
		//need to update lastingredients
		IngredientBuilder builder = new FileIngredientBuilder(com.intel.crashreport.specific.Build.INGREDIENTS_FILE_PATH);
		// Retrieve the ingredients
		JSONObject ingredients = builder.getIngredients();
		IngredientManager.INSTANCE.storeLastIngredients(ingredients);
	}

        public boolean IsIngredientEnabled() {
                // test conf file exits
                File confFile = new File(ING_CONF_FILE_PATH);
                if (confFile.exists()){
                        //no file => consider ingredient disabled
                        return true;
                }
                return false;
        }
}
