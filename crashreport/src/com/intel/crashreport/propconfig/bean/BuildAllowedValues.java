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

package com.intel.crashreport.propconfig.bean;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * A class that allows to manage allowed values
 * for properties of a given build.
 *
 */
public class BuildAllowedValues {
	private String build;

	@SerializedName("allowed_values")
	private PropertyValues[] allowedValues;

	public static final BuildAllowedValues empty() {
		BuildAllowedValues values = new BuildAllowedValues();
		values.setAllowedValues(new PropertyValues[0]);
		return values;
	}

	public String getBuild() {
		return this.build;
	}

	public void setBuild(String build) {
		this.build = build;
	}

	public PropertyValues[] getAllowedValues() {
		return this.allowedValues;
	}

	public void setAllowedValues(PropertyValues[] allowedValues) {
		this.allowedValues = allowedValues;
	}

	public String[] getConfiguredProperties() {
		List<String> names = new ArrayList<String>();
		for(PropertyValues values : this.allowedValues) {
			names.add(values.getPropertyName());
		}
		return names.toArray(new String[names.size()]);
	}

	public String[] getAllowedValuesForProperty(String name) {
		String [] allowedValues = null;
		for(PropertyValues allowed : this.allowedValues) {
			if(allowed.getPropertyName().equals(name)) {
				allowedValues = allowed.getValues();
			}
		}
		return allowedValues;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("BuildAllowedValues[build:");
		sb.append(this.build);
		sb.append(", allowed_values:{");
		for(PropertyValues values : this.allowedValues) {
			sb.append(values);
			sb.append(",");
		}
		sb.append("}");
		sb.append("]");
		return sb.toString();
	}
}
