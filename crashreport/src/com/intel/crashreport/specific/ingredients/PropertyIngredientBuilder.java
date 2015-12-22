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
