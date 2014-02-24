/* Phone Doctor (CLOTA)
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

package com.intel.crashreport.specific;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Scanner;

import com.intel.crashreport.GeneralBuild;
import com.intel.crashreport.GeneralEvent;
import com.intel.crashreport.GenericParseFile;
import com.intel.crashreport.Log;
import com.intel.crashreport.specific.PDStatus.PDSTATUS_TIME;
import com.intel.crashtoolserver.bean.Device;

public class Event extends GeneralEvent{

	public Event() {
		super();
	}

	public Event(int rowid, String eventId, String eventName, String type, String data0,
			String data1, String data2, String data3,
			String data4, String data5, String date, String buildId,
			String deviceId, String imei, String uptime, String crashDir) {
		super(rowid,eventId,eventName,type,data0,
				data1,data2,data3,
				data4,data5,date,buildId,
				deviceId,imei,uptime,crashDir);
	}

	public Event(int rowid, String eventId, String eventName, String type, String data0,
			String data1, String data2, String data3,
			String data4, String data5, Date date, String buildId,
			String deviceId, String imei, String uptime, String crashDir) {
		super(rowid,eventId,eventName,type, data0,
				data1, data2, data3,
				data4, data5, date, buildId,
				deviceId, imei, uptime, crashDir);
	}

	public Event(int rowid, String eventId, String eventName, String type, String data0,
			String data1, String data2, String data3,
			String data4, String data5, Date date, String buildId,
			String deviceId, String imei, String uptime, String crashDir, String variant) {
		this(rowid,eventId,eventName,type, data0,
				data1, data2, data3,
				data4, data5, date, buildId,
				deviceId, imei, uptime, crashDir);
		this.setVariant(variant);
	}

	public Event(HistoryEvent histEvent, String myBuild, boolean isUserBuild) {
		fillEvent(histEvent, myBuild, isUserBuild);
		setVariant(GeneralBuild.getVariant());
		pdStatus = PDStatus.INSTANCE.computePDStatus(this, PDSTATUS_TIME.INSERTION_TIME);
	}

	private void fillEvent(HistoryEvent histEvent, String myBuild, boolean isUserBuild) {
		if (histEvent.getEventName().equals("CRASH"))
			fillCrashEvent(histEvent, myBuild, isUserBuild);
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
		else if (histEvent.getEventName().equals("BZ"))
			fillBzEvent(histEvent, myBuild);
		else if (histEvent.getEventName().equals("ERROR"))
			fillErrorEvent(histEvent, myBuild);
		else if (histEvent.getEventName().equals("INFO"))
			fillInfoEvent(histEvent, myBuild);
		//extra step : format data for specific event
		new FormatParser(this).execFormat();
	}

	private void fillCrashEvent(HistoryEvent histevent, String myBuild, boolean isUserBuild) {
		crashDir = histevent.getOption();
		try {
			CrashFile crashFile = new CrashFile(crashDir);
			eventId = histevent.getEventId();
			eventName = histevent.getEventName();
			type = histevent.getType();
			if (type.equals("JAVACRASH") || type.equals("ANR") || type.equals("TOMBSTONE")) {
				if(!isUserBuild)
					dataReady = false;
			}else if (crashFile.getDataReady() == 0){
				if(!isUserBuild)
					dataReady = false;
			}
			data0 = crashFile.getData0();
			data1 = crashFile.getData1();
			data2 = crashFile.getData2();
			data3 = crashFile.getData3();
			data4 = crashFile.getData4();
			data5 = crashFile.getData5();
			date = convertDate(histevent.getDate());
			buildId = myBuild;
			deviceId = crashFile.getSn();
			if(!crashFile.getImei().equals(""))
				setImei(crashFile.getImei());
			else setImei(readImeiFromSystem());
			uptime = crashFile.getUptime();
			/* Get origin logfile name for dropbox events (only) to manage duplicate */
			if (this.isDropboxEvent()) {
				try {
					DropboxEvent dropboxFile = new DropboxEvent(crashDir, type);
					origin = dropboxFile.getDropboxFileName();
				} catch (FileNotFoundException e) {
					Log.w(toString() + ", origin dropbox logfile not found, path: " + crashDir);
				}
			}
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
		updateRebootReason();
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
		//crashdir should be filled before genericparsefile
		crashDir = histevent.getOption();
		GenericParseFile aParseFile = null;
		try{
			aParseFile = new GenericParseFile(crashDir, "_trigger");

		}catch (FileNotFoundException e){
			Log.w(toString() + ",parsefile couldn't be created: " + crashDir);
		}
		fillGenericEvent(histevent,myBuild,aParseFile);
	}

	private void fillGenericEvent(HistoryEvent histevent, String myBuild,GenericParseFile aParseFile) {
		eventId = histevent.getEventId();
		eventName = histevent.getEventName();
		date = convertDate(histevent.getDate());
		type = histevent.getType();
		buildId = myBuild;
		readDeviceIdFromFile();
		imei = readImeiFromSystem();
		if (aParseFile != null) {
			data0 = aParseFile.getValueByName("DATA0");
			data1 = aParseFile.getValueByName("DATA1");
			data2 = aParseFile.getValueByName("DATA2");
			data3 = aParseFile.getValueByName("DATA3");
			data4 = aParseFile.getValueByName("DATA4");
			data5 = aParseFile.getValueByName("DATA5");
			dataReady = !aParseFile.getValueByName("DATAREADY").equals("0");
		}
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

	private void fillBzEvent(HistoryEvent histevent, String myBuild) {
		crashDir = histevent.getOption();
		eventId = histevent.getEventId();
		eventName = histevent.getEventName();
		date = convertDate(histevent.getDate());
		type = histevent.getType();
		buildId = myBuild;
		readDeviceIdFromFile();
		imei = readImeiFromSystem();
	}

	private void fillErrorEvent(HistoryEvent histevent, String myBuild) {
		//crashdir should be filled before genericparsefile
		crashDir = histevent.getOption();
		GenericParseFile aParseFile = null;
		try{
			aParseFile = new GenericParseFile(crashDir, "_errorevent");

		}catch (FileNotFoundException e){
			Log.w(toString() + ",parsefile couldn't be created: " + crashDir);
		}
		fillGenericEvent(histevent,myBuild,aParseFile);
	}


	private void fillInfoEvent(HistoryEvent histevent, String myBuild) {
		//crashdir should be filled before genericparsefile
		crashDir = histevent.getOption();
		GenericParseFile aParseFile = null;
		try{
			aParseFile = new GenericParseFile(crashDir, "_infoevent");

		}catch (FileNotFoundException e){
			Log.w(toString() + ",parsefile couldn't be created: " + crashDir);
		}
		fillGenericEvent(histevent,myBuild,aParseFile);
	}

	public com.intel.crashtoolserver.bean.Event getEventForServer(com.intel.crashreport.specific.Build build, String sToken) {

		com.intel.crashtoolserver.bean.Event event;
		long lUptime = convertUptime(this.uptime);
		com.intel.crashreport.specific.Build mBuild;
		if (this.buildId.contentEquals(build.toString()))
			mBuild = build;
		else {
			mBuild = new com.intel.crashreport.specific.Build(this.buildId);
			if (mBuild.getBuildId().contentEquals(""))
				mBuild = build;
		}
		com.intel.crashtoolserver.bean.Build sBuild = mBuild.getBuildForServer();
		sBuild.setVariant(this.variant);
		com.intel.crashtoolserver.bean.Device aDevice;
		String sSSN = getSSN();
		//GCM not fully implemented

		String sTokenGCM = sToken;

		String sSPID = getSPIDFromFile();
		if (sSSN.equals("")){
			aDevice = new Device(this.deviceId, this.imei, null /*ssn not implemented if property empty*/, sTokenGCM, sSPID);
		}else{
			aDevice = new Device(this.deviceId, this.imei, sSSN, sTokenGCM, sSPID);
		}
		event = new com.intel.crashtoolserver.bean.Event(this.eventId, this.eventName, this.type,
				this.data0, this.data1, this.data2, this.data3, this.data4, this.data5,
				this.date, lUptime, null /*logfile*/,sBuild,com.intel.crashtoolserver.bean.Event.Origin.CLOTA,
				aDevice, this.iRowID,this.pdStatus );

		return event;
	}

	@Override
	public void readDeviceIdFromFile() {
		deviceId = getDeviceIdFromFile();
	}

	public static String getDeviceIdFromFile() {
		String sResult = "";
		File uuidFile = new File("/logs/" + "uuid.txt");
		try {
			Scanner scan = new Scanner(uuidFile);
			if (scan.hasNext())
				sResult = scan.nextLine();
			scan.close();
		} catch (FileNotFoundException e) {
			Log.w("CrashReportService: deviceId not set");
		}
		return sResult;
	}

	public static String getSPIDFromFile() {
		String sResult = "";
		File spidFile = new File("/logs/" + "spid.txt");
		try {
			Scanner scan = new Scanner(spidFile);
			if (scan.hasNext())
				sResult = scan.nextLine();
			scan.close();
		} catch (FileNotFoundException e) {
			Log.w("CrashReportService: spid not set");
		}
		return sResult;
	}

	public void readDeviceId() {
		readDeviceIdFromFile();
	}

	public static String deviceId() {
		return getDeviceIdFromFile();
	}

	/**
	 * Indicates if the event is a Dropbox event.
	 *
	 * @return true if the event is a Dropbox event. False otherwise.
	 */
	public boolean isDropboxEvent() {
		return ((type.equals("JAVACRASH") || type.equals("ANR") || type.equals("UIWDT")));
	}

	/**
	 * Indicates if the event is a Dropbox event detected in full Dropbox condition.
	 *
	 * @return true if the event is a Dropbox event detected in full Dropbox condition. False otherwise.
	 */
	public boolean isFullDropboxEvent() {
		return (isDropboxEvent() && data0.equals("full dropbox"));
	}

	/**
	 * Indicates if the kind of the event is allowed to be included in a rain of crashes.
	 * @return true if the kind of the event is subject to rain mechanism. False otherwise.
	 */
	public boolean isRainEventKind() {
		return ((type.equals("JAVACRASH") || type.equals("ANR") || type.equals("TOMBSTONE")));
	}

	public static String getSpid() {
		return getSPIDFromFile();
	}

	/**
	 * Read the eventfile to get the reboot reason (Data0, Data1, Data2, Data3, Data4).
	 */
	public void updateRebootReason() {
		GenericParseFile aParseFile = null;
		try{
			aParseFile = new GenericParseFile("/logs/events", "eventfile_"+eventId);

		}catch (FileNotFoundException e){
			Log.w(toString() + ",eventfile couldn't be created: " + "/logs/events");
		}
		if(aParseFile != null){
			data0 = aParseFile.getValueByName("DATA0");
			data1 = aParseFile.getValueByName("DATA1");
			data2 = aParseFile.getValueByName("DATA2");
			data3 = aParseFile.getValueByName("DATA3");
			data4 = aParseFile.getValueByName("DATA4");
		}
	}


}
