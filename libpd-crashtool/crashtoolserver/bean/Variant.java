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

package com.intel.crashtoolserver.bean;

import java.io.Serializable;


/**
 * @author mauret
 */
public class Variant implements Serializable {

	private static final long serialVersionUID = 2123982837115733180L;

	private Long id;
	private String name;
	private String displayedName;
	private boolean defaultVariant;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDisplayedName() {
		return displayedName;
	}
	public void setDisplayedName(String displayedName) {
		this.displayedName = displayedName;
	}
	
	public boolean isDefaultVariant() {
		return defaultVariant;
	}
	public void setDefaultVariant(boolean defaultVariant) {
		this.defaultVariant = defaultVariant;
	}
	@Override
	public String toString() {
		return "Variant [id=" + id + ", name=" + name + ", displayedName="
				+ displayedName + ", defaultVariant=" + defaultVariant + "]";
	}


}
