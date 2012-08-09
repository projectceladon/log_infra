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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import android.os.SystemProperties;

public class Event {

	private final static SimpleDateFormat EVENT_DF = new SimpleDateFormat("yyyy-MM-dd/HH:mm:ss");
	private final static SimpleDateFormat EVENT_DF_OLD = new SimpleDateFormat("yy-MM-dd-HH-mm-ss");

	private String eventId = "";
	private String eventName = "";
	private String type = "";
	private String data0 = "";
	private String data1 = "";
	private String data2 = "";
	private String data3 = "";
	private String data4 = "";
	private String data5 = "";
	private Date date = null;
	private String buildId = "";
	private String deviceId = "";
	private String imei = "";
	private String uptime = "";
	private String crashDir = "";

	public Event() {}

	public Event(String eventId, String eventName, String type, String data0,
			String data1, String data2, String data3,
			String data4, String data5, String date, String buildId,
			String deviceId, String imei, String uptime, String crashDir) {
		this.eventId = eventId;
		this.eventName = eventName;
		this.type = type;
		this.data0 = data0;
		this.data1 = data1;
		this.data2 = data2;
		this.data3 = data3;
		this.data4 = data4;
		this.data5 = data5;
		this.date = convertDate(date);
		this.buildId = buildId;
		this.deviceId = deviceId;
		this.imei = imei;
		this.uptime = uptime;
		this.crashDir = crashDir;
	}

	public Event(String eventId, String eventName, String type, String data0,
			String data1, String data2, String data3,
			String data4, String data5, Date date, String buildId,
			String deviceId, String imei, String uptime, String crashDir) {
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
		this.crashDir = crashDir;
	}

	public Event(HistoryEvent histEvent, String myBuild) {
		fillEvent(histEvent, myBuild);
	}

	private void fillEvent(HistoryEvent histEvent, String myBuild) {
		if (histEvent.getEventName().equals("CRASH"))
			fillCrashEvent(histEvent, myBuild);
		else if (histEvent.getEventName().equals("REBOOT"))
			fillRebootEvent(histEvent, myBuild);
		else if (histEvent.getEventName().equals("UPTIME"))
			fillUptimeEvent(histEvent, myBuild);
		else if (histEvent.getEventName().equals("STATS"))
			fillStatsEvent(histEvent, myBuild);
		else if (histEvent.getEventName().equals("STATE"))
			fillStateEvent(histEvent, myBuild);
		else if (histEvent.getEventName().equals("APLOG"))
			fillAplogEvent(histEvent, myBuild);
	}

	private void fillCrashEvent(HistoryEvent histevent, String myBuild) {
		crashDir = histevent.getOption();
		try {
			CrashFile crashFile = new CrashFile(crashDir);
			eventId = histevent.getEventId();
			eventName = histevent.getEventName();
			type = histevent.getType();
			data0 = crashFile.getData0();
			data1 = crashFile.getData1();
			data2 = crashFile.getData2();
			data3 = crashFile.getData3();
			data4 = crashFile.getData4();
			data5 = crashFile.getData5();
			date = convertDate(histevent.getDate());
			buildId = myBuild;
			deviceId = crashFile.getSn();
			setImei(crashFile.getImei());
			uptime = crashFile.getUptime();
		} catch (FileNotFoundException e) {
			eventId = histevent.getEventId();
			eventName = histevent.getEventName();
			type = histevent.getType();
			date = convertDate(histevent.getDate());
			buildId = myBuild.toString();
			readDeviceIdFromFile();
			imei = readImeiFromSystem();
			Log.w(toString() + ", Crashfile not found, path: " + crashDir);
		}
	}

	private void fillRebootEvent(HistoryEvent histevent, String myBuild) {
		eventId = histevent.getEventId();
		eventName = histevent.getEventName();
		type = histevent.getType();
		date = convertDate(histevent.getDate());
		buildId = myBuild;
		readDeviceIdFromFile();
		imei = readImeiFromSystem();
		uptime = histevent.getOption();
	}

	private void fillUptimeEvent(HistoryEvent histevent, String myBuild) {
		eventId = histevent.getEventId();
		eventName = histevent.getEventName();
		date = convertDate(histevent.getDate());
		buildId = myBuild;
		readDeviceIdFromFile();
		imei = readImeiFromSystem();
		uptime = histevent.getType();
	}

		private void fillStatsEvent(HistoryEvent histevent, String myBuild) {
		crashDir = histevent.getOption();
		eventId = histevent.getEventId();
		eventName = histevent.getEventName();
		date = convertDate(histevent.getDate());
		type = histevent.getType();
		buildId = myBuild;
		readDeviceIdFromFile();
		imei = readImeiFromSystem();
	}

	private void fillStateEvent(HistoryEvent histevent, String myBuild) {
		eventId = histevent.getEventId();
		eventName = histevent.getEventName();
		type = histevent.getType();
		date = convertDate(histevent.getDate());
		buildId = myBuild;
		readDeviceIdFromFile();
		imei = readImeiFromSystem();
	}

	private void fillAplogEvent(HistoryEvent histevent, String myBuild) {
		crashDir = histevent.getOption();
		eventId = histevent.getEventId();
		eventName = histevent.getEventName();
		date = convertDate(histevent.getDate());
		type = histevent.getType();
		buildId = myBuild;
		readDeviceIdFromFile();
		imei = readImeiFromSystem();
	}

	public com.intel.crashtoolserver.bean.Event getEventForServer(Build build) {
		com.intel.crashtoolserver.bean.Event event;
		long sUptime = convertUptime(this.uptime);
		Build mBuild;
		if (this.buildId.contentEquals(build.toString()))
			mBuild = build;
		else {
			mBuild = new Build(this.buildId);
			if (mBuild.getBuildId().contentEquals(""))
				mBuild = build;
		}
		com.intel.crashtoolserver.bean.Build sBuild = mBuild.getBuildForServer();
		event = new com.intel.crashtoolserver.bean.Event(this.eventId, this.eventName, this.type,
				this.data0, this.data1, this.data2, this.data3, this.data4, this.data5,
				this.date, this.deviceId, this.imei, sUptime, sBuild);
		return event;
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
			scan.close();
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

	public static Date convertDate(String date) {
		Date cDate = null;
		if (date != null) {
			try {
				cDate = EVENT_DF.parse(date);
			} catch (ParseException e) {
				try {
					cDate = EVENT_DF_OLD.parse(date);
				} catch (ParseException e1) {
					cDate = new Date();
				}
			}
		} else
			cDate = new Date();
		return cDate;
	}

	public static long convertUptime(String uptime) {
		long cUptime = 0;
		if (uptime != null) {
			String uptimeSplited[] = uptime.split(":");
			if (uptimeSplited.length == 3) {
				long hours = Long.parseLong(uptimeSplited[0]);
				long minutes = Long.parseLong(uptimeSplited[1]);
				long seconds = Long.parseLong(uptimeSplited[2]);
				cUptime = seconds + (60 * minutes) + (3600 * hours);
			}
		}
		return cUptime;
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

	public String getDateAsString() {
		return EVENT_DF.format(date);
	}

	public void setDate(String date) {
		this.date = convertDate(date);
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
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

	public void setCrashDir(String crashDir) {
		this.crashDir = crashDir;
	}

	public String getCrashDir() {
		return crashDir;
	}

}
