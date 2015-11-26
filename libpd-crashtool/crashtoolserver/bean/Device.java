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
	
	// label identifier
    public static final String HARDWARE_OTHER = "other";

	private Long id;
	private String deviceId;
	private String imei;
	/**
	 * @deprecated : pid does not exist anymore in CrashTool database
	 */
	@Deprecated
	private String pid;
	private String hwType;
	private Long hwTypeId;
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
	
	/**
	 * not use anymore 
	 */
	@Deprecated
	public Device() {
		this(null, null, null, null, null, null, null, null, null, null);
	}

	/**
	 * Light Constructor used by PD under 1.0
	 *
	 * @param deviceId
	 * @param imei
	 */
	@Deprecated
	public Device(String deviceId, String imei) {
		this(deviceId, imei, null, null, null, null, null, null, null, null);
	}


	/**
	 * used by parser
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
	 * used By ACL
	 * @param deviceId
	 * @param imei
	 * @param ssn
	 * @param owner
	 * @param location
	 * @param tag
	 */
	public Device(String deviceId, String imei, String ssn, String owner, String location, String tag) {
		this(deviceId, imei, null, owner, location, tag, null, ssn, null, null);
	}

	/**
	 * used By PD under 1.6
	 * @param deviceId
	 * @param imei
	 * @param ssn
	 */
	public Device(String deviceId, String imei, String ssn) {
		this(deviceId, imei, null, null, null, null, null, ssn, null, null);
	}
	
	/**
	 * used By PD above 1.6
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
	 * Used by parser
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
	
	@Deprecated
	public String getPid() {
		return pid;
	}
	@Deprecated
	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getHwType() {
		return hwType;
	}

	public void setHwType(String hwType) {
		this.hwType = hwType;
	}
	
	public Long getHwTypeId() {
        return hwTypeId;
    }

    public void setHwTypeId(Long hwTypeId) {
        this.hwTypeId = hwTypeId;
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
	
	public boolean isHardwareTypeDefined() {
		return hwType != null && !hwType.equals(HARDWARE_OTHER);
	}
	
	@Override
    public String toString() {
        return "Device [id=" + id + ", deviceId=" + deviceId + ", imei=" + imei
                + ", pid=" + pid + ", hwType=" + hwType + ", hwTypeId="
                + hwTypeId + ", platform=" + platform + ", ssn=" + ssn
                + ", team=" + team + ", domain=" + domain + ", owner=" + owner
                + ", location=" + location + ", tag=" + tag
                + ", lastUpdatedDate=" + lastUpdatedDate + ", gcmToken="
                + gcmToken + ", spid=" + spid + ", tracmorDevice="
                + tracmorDevice + "]";
    }
}

