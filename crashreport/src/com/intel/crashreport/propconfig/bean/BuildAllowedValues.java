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
