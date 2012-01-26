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
 * Author: Jeremy Rocher <jeremyx.rocher@intel.com>
 */

package com.intel.crashreport;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import android.os.SystemProperties;

public class Event {

	private String eventId = "";
	private String eventName = "";
	private String type = "";
	private String data0 = "";
	private String data1 = "";
	private String data2 = "";
	private String data3 = "";
	private String data4 = "";
	private String data5 = "";
	private String date = "";
	private String buildId = "";
	private String deviceId = "";
	private String imei = "";
	private String uptime = "";

	public Event() {}

	public Event(String eventId, String eventName, String type, String data0,
			String data1, String data2, String data3,
			String data4, String data5, String date, String buildId,
			String deviceId, String imei, String uptime) {
		this.eventId = eventId;
		this.eventName = eventName;
		this.type = type;
		this.data0 = data0;
		this.data1 = data1;
		this.data2 = data2;
		this.data3 = data3;
		this.data4 = data4;
		this.data5 = data5;
		this.date = date;
		this.buildId = buildId;
		this.deviceId = deviceId;
		this.imei = imei;
		this.uptime = uptime;
	}

	public Event(HistoryEvent histEvent) {
		fillEvent(histEvent);
	}

	private void fillEvent(HistoryEvent histEvent) {
		if (histEvent.getEventName().equals("CRASH"))
			fillCrashEvent(histEvent);
		else if (histEvent.getEventName().equals("REBOOT"))
			fillRebootEvent(histEvent);
		else if (histEvent.getEventName().equals("UPTIME"))
			fillUptimeEvent(histEvent);
	}

	private void fillCrashEvent(HistoryEvent histevent) {
		try {
			CrashFile crashFile = new CrashFile(histevent.getOption());
			eventId = histevent.getEventId();
			eventName = histevent.getEventName();
			type = histevent.getType();
			data0 = crashFile.getData0();
			data1 = crashFile.getData1();
			data2 = crashFile.getData2();
			data3 = crashFile.getData3();
			data4 = crashFile.getData4();
			data5 = crashFile.getData5();
			date = histevent.getDate();
			buildId = crashFile.getBuildId();
			deviceId = crashFile.getSn();
			setImei(crashFile.getImei());
			uptime = crashFile.getUptime();
		} catch (FileNotFoundException e) {
			eventId = histevent.getEventId();
			eventName = histevent.getEventName();
			type = histevent.getType();
			date = histevent.getDate();
			buildId = android.os.Build.VERSION.INCREMENTAL;
			readDeviceIdFromFile();
			imei = readImeiFromSystem();
			Log.w(toString() + ", Crashfile not found, path: " + histevent.getOption());
		}
	}

	private void fillRebootEvent(HistoryEvent histevent) {
		eventId = histevent.getEventId();
		eventName = histevent.getEventName();
		type = histevent.getType();
		date = histevent.getDate();
		buildId = android.os.Build.VERSION.INCREMENTAL;
		readDeviceIdFromFile();
		imei = readImeiFromSystem();
		uptime = histevent.getOption();
	}

	private void fillUptimeEvent(HistoryEvent histevent) {
		eventId = histevent.getEventId();
		eventName = histevent.getEventName();
		date = histevent.getDate();
		buildId = android.os.Build.VERSION.INCREMENTAL;
		readDeviceIdFromFile();
		imei = readImeiFromSystem();
		uptime = histevent.getType();
	}

	public String toString() {
		if (eventName.equals("UPTIME"))
			return new String("Event: " + eventId + ":" + eventName + ":" + uptime);
		else
			return new String("Event: " + eventId + ":" + eventName + ":" + type);
	}

	private void readDeviceIdFromFile() {
		File uuidFile = new File("/data/logs/" + "uuid.txt");
		try {
			Scanner scan = new Scanner(uuidFile);
			if (scan.hasNext())
				deviceId = scan.nextLine();
		} catch (FileNotFoundException e) {
			Log.w("CrashReportService: deviceId not set");
		}
	}

	private String readImeiFromSystem() {
		String imeiRead = "";
		try {
			imeiRead = SystemProperties.get("persist.radio.device.imei", "");
		} catch (IllegalArgumentException e) {
			Log.w("CrashReportService: IMEI not available");
		}
		return imeiRead;
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

	public void setData3(String data) {
		this.data3 = data;
	}

	public String getData4() {
		return data4;
	}

	public void setData4(String data) {
		this.data4 = data;
	}

	public String getData5() {
		return data5;
	}

	public void setData5(String data) {
		this.data5 = data;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
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

	public String getUptime() {
		return uptime;
	}

	public void setUptime(String uptime) {
		this.uptime = uptime;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

}
