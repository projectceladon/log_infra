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
