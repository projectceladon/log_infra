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
import java.util.Date;

/**
 *
 * @author mauret
 *
 */
public class Mainline implements Serializable {

	private static final long serialVersionUID = 6854769745208757379L;

	private Long id;
	private String name;
	private Date startingDate;
	private boolean defaultMainline;
	private String version;
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the startingDate
	 */
	public Date getStartingDate() {
		return startingDate;
	}
	/**
	 * @param startingDate the startingDate to set
	 */
	public void setStartingDate(Date startingDate) {
		this.startingDate = startingDate;
	}
	/**
	 * @return the defaultMainline
	 */
	public boolean isDefaultMainline() {
		return defaultMainline;
	}
	/**
	 * @param defaultMainline the defaultMainline to set
	 */
	public void setDefaultMainline(boolean defaultMainline) {
		this.defaultMainline = defaultMainline;
	}
	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Mainline [id=" + id + ", name=" + name + ", startingDate=" + startingDate
				+ ", defaultMainline=" + defaultMainline + ", version=" + version + "]";
	}


}
