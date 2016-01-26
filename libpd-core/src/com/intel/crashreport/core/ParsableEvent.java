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

package com.intel.crashreport.core;

public class ParsableEvent {

	public static final String ORGANIZATION_MCG = "mcg";

	protected String eventId = "";
	protected String eventName = "";
	protected String type = "";
	protected String data0 = "";
	protected String data1 = "";
	protected String data2 = "";
	protected String data3 = "";
	protected String data4 = "";
	protected String data5 = "";
	protected String buildId = "";
	protected String deviceId = "";
	protected String variant = "";
	protected String imei = "";
	protected String uptime = "";
	protected String crashDir = "";
	protected boolean dataReady = true;
	protected boolean critical = false;
	protected int iRowID;
	protected String modemVersionUsed = "";


	public ParsableEvent(int rowid, String eventId, String eventName, String type, String data0,
			String data1, String data2, String data3,
			String data4, String data5, String buildId,
			String deviceId, String imei, String uptime, String crashDir) {
		this.iRowID = rowid;
		this.eventId = eventId;
		this.eventName = eventName;
		this.type = type;
		this.data0 = data0;
		this.data1 = data1;
		this.data2 = data2;
		this.data3 = data3;
		this.data4 = data4;
		this.data5 = data5;
		this.buildId = buildId;
		this.deviceId = deviceId;
		this.imei = imei;
		this.uptime = uptime;
		this.crashDir = crashDir;
	}

	public String getEventId() {
		return eventId;
	}
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getData0() {
		return data0;
	}
	public void setData0(String data0) {
		this.data0 = data0;
	}
	public String getData1() {
		return data1;
	}
	public void setData1(String data1) {
		this.data1 = data1;
	}
	public String getData2() {
		return data2;
	}
	public void setData2(String data2) {
		this.data2 = data2;
	}
	public String getData3() {
		return data3;
	}
	public void setData3(String data3) {
		this.data3 = data3;
	}
	public String getData4() {
		return data4;
	}
	public void setData4(String data4) {
		this.data4 = data4;
	}
	public String getData5() {
		return data5;
	}
	public void setData5(String data5) {
		this.data5 = data5;
	}
	public String getBuildId() {
		return buildId;
	}
	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getVariant() {
		return variant;
	}
	public void setVariant(String variant) {
		this.variant = variant;
	}
	public String getImei() {
		return imei;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}
	public String getUptime() {
		return uptime;
	}
	public void setUptime(String uptime) {
		this.uptime = uptime;
	}
	public String getCrashDir() {
		return crashDir;
	}
	public void setCrashDir(String crashDir) {
		this.crashDir = crashDir;
	}
	public boolean isDataReady() {
		return dataReady;
	}

	public int getDataReadyAsInt(){
		if (dataReady) {
			return 1;
		} else {
			return 0;
		}
	}

	public void setDataReady(boolean dataReady) {
		this.dataReady = dataReady;
	}

	public boolean isCritical() {
		return critical;
	}

	public void setCritical(boolean value) {
		this.critical = value;
	}

	public int getiRowID() {
		return iRowID;
	}

	public void setiRowID(int iRowID) {
		this.iRowID = iRowID;
	}

	public String getModemVersionUsed() {
		return modemVersionUsed;
	}

	public void setModemVersionUsed(String modemVersionUsed) {
		this.modemVersionUsed = modemVersionUsed;
	}

}
