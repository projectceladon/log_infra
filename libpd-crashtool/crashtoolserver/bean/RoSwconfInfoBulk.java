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

package com.intel.crashtoolserver.bean;

/**
 * Bean that encapsulates RoSw information : telephony/Battery
 * @author glivon
 */
public class RoSwconfInfoBulk implements RoSwconfInfoBulkKey {
	private static final String DEFAULT_VALUE = "";
	
	private String roSwconfInfoTelephony = DEFAULT_VALUE;
	private String roSwconfInfoBattery = DEFAULT_VALUE;
	
	/**
	 * Default Constructor
	 */
	public RoSwconfInfoBulk() {
		// Default constructor
	}
	
	/**
	 * Constructor with all Attributes
	 * @param tel
	 * @param battery
	 */
	public RoSwconfInfoBulk(String telephony, String battery) {
		this.roSwconfInfoTelephony = telephony;
		this.roSwconfInfoBattery = battery;
	}

	/**
	 * @return the roSwconfInfotelephony
	 */
	@Override
	public String getRoSwconfInfoTelephony() {
		return roSwconfInfoTelephony;
	}

	/**
	 * @param roSwconfInfotelephony the roSwconfInfotelephony to set
	 */
	@Override
	public void setRoSwconfInfoTelephony(String roSwconfInfotelephony) {
		this.roSwconfInfoTelephony = roSwconfInfotelephony;
	}

	/**
	 * @return the roSwconfInfoBattery
	 */
	@Override
	public String getRoSwconfInfoBattery() {
		return roSwconfInfoBattery;
	}

	/**
	 * @param roSwconfInfoBattery the roSwconfInfoBattery to set
	 */
	@Override
	public void setRoSwconfInfoBattery(String roSwconfInfoBattery) {
		this.roSwconfInfoBattery = roSwconfInfoBattery;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RoSwconfInfoBulk [roSwconfInfoTelephony="
				+ roSwconfInfoTelephony + ", roSwconfInfoBattery="
				+ roSwconfInfoBattery + "]";
	}
}
