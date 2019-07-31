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

package com.intel.crashreport.core;

public class ParsableEvent {

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
