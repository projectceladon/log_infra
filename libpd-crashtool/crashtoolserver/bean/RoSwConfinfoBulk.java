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

/**
 * Bean that encapsulates RoSw information : telephony/Battery
 * @author glivon
 */
public class RoSwConfinfoBulk {
	private static final String DEFAULT_VALUE = "";
	
	private String roSwconfInfotelephony = DEFAULT_VALUE;
	private String roSwconfInfoBattery = DEFAULT_VALUE;
	
	/**
	 * Default Constructor
	 */
	public RoSwConfinfoBulk() {
		// Default constructor
	}
	
	/**
	 * Constructor with all Attributes
	 * @param tel
	 * @param battery
	 */
	public RoSwConfinfoBulk(String telephony, String battery) {
		this.roSwconfInfotelephony = telephony;
		this.roSwconfInfoBattery = battery;
	}

	/**
	 * @return the roSwconfInfotelephony
	 */
	public String getRoSwconfInfotelephony() {
		return roSwconfInfotelephony;
	}

	/**
	 * @param roSwconfInfotelephony the roSwconfInfotelephony to set
	 */
	public void setRoSwconfInfotelephony(String roSwconfInfotelephony) {
		this.roSwconfInfotelephony = roSwconfInfotelephony;
	}

	/**
	 * @return the roSwconfInfoBattery
	 */
	public String getRoSwconfInfoBattery() {
		return roSwconfInfoBattery;
	}

	/**
	 * @param roSwconfInfoBattery the roSwconfInfoBattery to set
	 */
	public void setRoSwconfInfoBattery(String roSwconfInfoBattery) {
		this.roSwconfInfoBattery = roSwconfInfoBattery;
	}

	
	/**
	 * Is no roSwconfInfoBulk field filled
	 * @return
	 */
	public boolean isEmpty() {
		return (roSwconfInfoBattery == null || roSwconfInfoBattery.isEmpty()) 
				&& (roSwconfInfotelephony == null || roSwconfInfotelephony.isEmpty());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RoSwConfinfoBulk [roSwconfInfotelephony="
				+ roSwconfInfotelephony + ", roSwconfInfoBattery="
				+ roSwconfInfoBattery + "]";
	}
}
