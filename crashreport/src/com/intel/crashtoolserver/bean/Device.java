/* Crash Report (CLOTA)
 *
 * Copyright (C) Intel 2012
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Mathieu Auret <mathieux.auret@intel.com>
 */

package com.intel.crashtoolserver.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * Bean Device
 *
 * Be aware that bean is used by CLOTA and MPTA, do not rename package and class
 * name for down ward.
 *
 * @author mauretx
 *
 */
public class Device implements Serializable {

	private static final long serialVersionUID = -3175450272008389846L;

	private Long id;
	private String deviceId;
	private String imei;
	private String pid;
	private String hwType;
	private String team;
	private String domain;
	private String owner;
	private String location;
	private String tag;
	private Date lastUpdatedDate;

	/**
	 * Default constructor
	 */
	public Device() {
		// do nothing
	}

	/**
	 * Light Constructor
	 *
	 * @param deviceId
	 * @param imei
	 */
	public Device(String deviceId, String imei) {

		this(deviceId, imei, null, null, null, null, null);
	}

	public Device(String deviceId, String imei, String pid, String owner, String location,
			String tag, Date lastUpdatedDate) {
		super();
		this.deviceId = deviceId;
		this.imei = imei;
		this.pid = pid;
		this.owner = owner;
		this.location = location;
		this.tag = tag;

		if (lastUpdatedDate == null) {
			lastUpdatedDate = new Date();
		}
		this.lastUpdatedDate = lastUpdatedDate;
	}

	/**
	 * @return the deviceId
	 */
	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * @param deviceId the deviceId to set
	 */
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	/**
	 * @return the imei
	 */
	public String getImei() {
		return imei;
	}

	/**
	 * @param imei the imei to set
	 */
	public void setImei(String imei) {
		this.imei = imei;
	}

	/**
	 * @return the pid
	 */
	public String getPid() {
		return pid;
	}

	/**
	 * @param pid the pid to set
	 */
	public void setPid(String pid) {
		this.pid = pid;
	}

	/**
	 * @return the hwType
	 */
	public String getHwType() {
		return hwType;
	}

	/**
	 * @param hwType the hwType to set
	 */
	public void setHwType(String hwType) {
		this.hwType = hwType;
	}

	/**
	 * @return the team
	 */
	public String getTeam() {
		return team;
	}

	/**
	 * @param team the team to set
	 */
	public void setTeam(String team) {
		this.team = team;
	}

	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * @param domain the domain to set
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @param owner the owner to set
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * @return the tag
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * @param tag the tag to set
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}

	/**
	 * @return the lastUpdatedDate
	 */
	public Date getLastUpdatedDate() {
		return lastUpdatedDate;
	}

	/**
	 * @param lastUpdatedDate the lastUpdatedDate to set
	 */
	public void setLastUpdatedDate(Date lastUpdatedDate) {
		this.lastUpdatedDate = lastUpdatedDate;
	}

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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Device [id=" + id + ", deviceId=" + deviceId + ", imei=" + imei + ", pid=" + pid
				+ ", hwType=" + hwType + ", team=" + team + ", domain=" + domain + ", owner="
				+ owner + ", location=" + location + ", tag=" + tag + ", lastUpdatedDate="
				+ lastUpdatedDate + "]";
	}
}
