/* Copyright (C) 2019 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.crashreport.specific.ingredients;

import android.os.SystemProperties;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONException;

import com.intel.crashreport.Log;

public class PropertyIngredientBuilder implements IngredientBuilder {

	private final List<String> properties = new ArrayList<String>();
	private JSONObject ingredients = null;

	public PropertyIngredientBuilder(String propertyName) {
		this.properties.add(propertyName);
	}

	public PropertyIngredientBuilder(List<String> propertyNames) {
		this.properties.addAll(propertyNames);
	}

	@Override
	public JSONObject getIngredients() {
		if(this.ingredients == null) {
			this.ingredients = new JSONObject();
			for(String propertyName : this.properties) {
				String value = SystemProperties.get(propertyName, "");
				try {
					this.ingredients.put(propertyName, value);
				} catch (JSONException e) {
					Log.d("Could not add ingredient " + propertyName + " " + value);
				}
			}
		}
		return this.ingredients;
	}
}
