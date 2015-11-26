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
