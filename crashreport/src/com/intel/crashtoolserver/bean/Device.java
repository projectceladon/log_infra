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
 * Author: Mathieu Auret <mathieu.auret@intel.com>
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
	private String platform;
	private String ssn;
	private String team;
	private String domain;
	private String owner;
	private String location;
	private String tag;
	private Date lastUpdatedDate;
	private String gcmToken;
	private String spid;
	private TracmorDevice tracmorDevice;

	public Device() {
		// do nothing
	}

	/**
	 * Light Constructor
	 *
	 * @param deviceId
	 * @param imei
	 */
	@Deprecated
	public Device(String deviceId, String imei) {
		this(deviceId, imei, null, null, null, null, null, null, null, null);
	}


	/**
	 *
	 * @param deviceId
	 * @param imei
	 * @param pid
	 * @param owner
	 * @param location
	 * @param tag
	 * @param lastUpdatedDate
	 */
	@Deprecated
	public Device(String deviceId, String imei, String pid, String owner, String location,
			String tag, Date lastUpdatedDate) {

		this(deviceId, imei, pid, owner, location, tag, lastUpdatedDate, null, null, null);
	}

	/**
	 *
	 * @param deviceId
	 * @param imei
	 * @param ssn
	 */
	public Device(String deviceId, String imei, String ssn) {
		this(deviceId, imei, null, null, null, null, null, ssn, null, null);
	}

	/**
	 * @param deviceId
	 * @param imei
	 * @param ssn
	 * @param gcmToken
	 * @param spid
	 */
	public Device(String deviceId, String imei, String ssn, String gcmToken, String spid) {
		this(deviceId, imei, null, null, null, null, null, ssn, gcmToken, spid);
	}

	/**
	 * @param deviceId
	 * @param imei
	 * @param pid
	 * @param owner
	 * @param location
	 * @param tag
	 * @param lastUpdatedDate
	 * @param ssn
	 * @param gcmToken
	 * @param spid
	 */
	private Device(String deviceId, String imei, String pid, String owner, String location,
			String tag, Date lastUpdatedDate, String ssn, String gcmToken, String spid) {

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
		this.ssn = ssn;
		this.gcmToken = gcmToken;
		this.spid = spid;
	}

	public String getSsn() {
		return ssn;
	}

	public void setSsn(String ssn) {
		this.ssn = ssn;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getHwType() {
		return hwType;
	}

	public void setHwType(String hwType) {
		this.hwType = hwType;
	}

	public String getTeam() {
		return team;
	}

	public void setTeam(String team) {
		this.team = team;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getTag() {
		return tag;
	}

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

	public TracmorDevice getTracmorDevice() {
		return tracmorDevice;
	}

	public void setTracmorDevice(TracmorDevice tracmorDevice) {
		this.tracmorDevice = tracmorDevice;
	}

	public String getGcmToken() {
		return gcmToken;
	}

	public void setGcmToken(String gcmToken) {
		this.gcmToken = gcmToken;
	}

	public String getSpid() {
		return spid;
	}

	public void setSpid(String spid) {
		this.spid = spid;
	}

	@Override
	public String toString() {
		return "Device [id=" + id + ", deviceId=" + deviceId + ", imei=" + imei
				+ ", pid=" + pid + ", hwType=" + hwType + ", platform="
				+ platform + ", ssn=" + ssn + ", team=" + team + ", domain="
				+ domain + ", owner=" + owner + ", location=" + location
				+ ", tag=" + tag + ", lastUpdatedDate=" + lastUpdatedDate
				+ ", gcmToken=" + gcmToken + ", spid=" + spid
				+ ", tracmorDevice=" + tracmorDevice + "]";
	}
}

