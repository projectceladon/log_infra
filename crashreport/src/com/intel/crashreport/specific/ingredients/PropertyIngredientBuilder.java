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
package com.intel.crashreport.specific.ingredients;

import android.os.SystemProperties;
import android.util.Log;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyIngredientBuilder implements IngredientBuilder {

	private final List<String> properties = new ArrayList<String>();
	private Map<String, String> ingredients = null;

	public PropertyIngredientBuilder(String propertyName) {
		this.properties.add(propertyName);
	}

	public PropertyIngredientBuilder(List<String> propertyNames) {
		this.properties.addAll(propertyNames);
	}

	@Override
	public Map<String, String> getIngredients() {
		if(this.ingredients == null) {
			this.ingredients = new HashMap<String, String>();
			for(String propertyName : this.properties) {
				String value = SystemProperties.get(propertyName, "");
				this.ingredients.put(propertyName, value);
			}
		}
		return this.ingredients;
	}
}
