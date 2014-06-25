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
 * Author: Nicolas Benoit <nicolasx.benoit@intel.com>
 */

package com.intel.crashreport.specific.ingredients;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public enum IngredientManager {
	INSTANCE;

	private static final String ING_CONF_FILE_PATH = "/system/etc/ingredients.conf";

	private boolean bNeedRefresh = true;
	private Map<String, String> lastIngredients = null;
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
		Map<String, String> ingredients_unique = builder_unique.getIngredients();
		for(String key : ingredients_unique.keySet()) {
			// Add each ingredient value to the JSON string
			String value = ingredients_unique.get(key);
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

	public void storeLastIngredients(Map<String, String> aIng) {
		lastIngredients = aIng;
	}

	public Map<String, String> getLastIngredients() {
		return lastIngredients;
	}

}