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

public class PropertyValues {

	private String property_name;
	private String[] values;

	public PropertyValues() {
		this.property_name = "default";
		this.values = new String[]{};
	}

	public PropertyValues(String name) {
		this.property_name = name;
		this.values = new String[]{};
	}

	public PropertyValues(String name, String[] values) {
		this.property_name = name;
		this.values = values;
	}


	public String getPropertyName() {
		return this.property_name;
	}

	public void setPropertyName(String name) {
		this.property_name = name;
	}

	public String[] getValues() {
		return this.values;
	}

	public void setValues(String[] values) {
		this.values = values;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("PropertyValues[name:");
		sb.append(this.property_name);
		sb.append(", values:{");
		for(String value : this.values) {
			sb.append("\"");
			sb.append(value);
			sb.append("\"");
			sb.append(",");
		}
		sb.append("}]");
		return sb.toString();
	}
}
