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
import com.intel.crashreport.specific.ingredients.IngredientManager;
import com.intel.crashtoolserver.bean.Device;
import com.intel.phonedoctor.Constants;
import com.intel.phonedoctor.utils.FileOps;

public class Event extends GeneralEvent{

	private static final String UUID_FILE_PATH = Constants.LOGS_DIR + "/uuid.txt";
	private static final String SPID_FILE_PATH = Constants.LOGS_DIR + "/spid.txt";
	private static final String EVENTS_DIR = Constants.LOGS_DIR + "/events";

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
		setOsBootMode(GeneralEvent.BOOT_UNDEFINED);
		fillEvent(histEvent, myBuild, isUserBuild);
		setVariant(GeneralBuild.getVariant());
		setIngredients(Build.getIngredients());
		setUniqueKeyComponent(IngredientManager.INSTANCE.getUniqueKeyList().toString());
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

		FileOps.compressFolderContent(getCrashDir());
	}

	private void fillCrashEvent(HistoryEvent histevent, String myBuild, boolean isUserBuild) {
		boolean bResult = false;

		setCrashDir(histevent.getOption());
		try {
			//crashfile is only used to get data and should not be parsed here
			CrashFile crashFile = new CrashFile(getCrashDir(),false);
			//Name and type should be specified before parsing
			setEventId(histevent.getEventId());
			setEventName(histevent.getEventName());;
			setType(histevent.getType());
			date = convertDate(histevent.getDate());
			setBuildId(myBuild);
			setDeviceId(crashFile.getSn());
			if(!crashFile.getImei().equals(""))
				setImei(crashFile.getImei());
			else setImei(readImeiFromSystem());
			setUptime(crashFile.getUptime());
			bResult = ParserContainer.INSTANCE.parseEvent(this);
			//DATA0-5 expected to be set by parserContainer
			if (!bResult){
				Log.w("parser error, could not get a valid parsing");
				throw new FileNotFoundException("invalid parsing");
			}else{
				Log.d("parser succes");
			}

			if (getType().equals("JAVACRASH") ||
					getType().equals("ANR") ||
					getType().equals("TOMBSTONE")) {
				if(!isUserBuild)
					this.setDataReady(false);
			}else if (crashFile.getDataReady() == 0){
				if(!isUserBuild)
					this.setDataReady(false);
			}
			/* Get origin logfile name for dropbox events (only) to manage duplicate */
			if (this.isDropboxEvent()) {
				try {
					DropboxEvent dropboxFile = new DropboxEvent(getCrashDir(), getType());
					origin = dropboxFile.getDropboxFileName();
				} catch (FileNotFoundException e) {
					Log.w(toString() + ", origin dropbox logfile not found, path: " + getCrashDir());
				}
			}
		} catch (FileNotFoundException e) {
			setEventId(histevent.getEventId());
			setEventName(histevent.getEventName());;
			setType(histevent.getType());
			date = convertDate(histevent.getDate());
			ParserContainer.INSTANCE.parseEvent(this);
			setBuildId(myBuild.toString());
			readDeviceIdFromFile();
			setImei(readImeiFromSystem());
			Log.w(toString() + ", Crashfile not found, path: " + getCrashDir());
		}
	}

	private void fillRebootEvent(HistoryEvent histevent, String myBuild) {
		setEventId(histevent.getEventId());
		setEventName(histevent.getEventName());;
		setType(histevent.getType());
		date = convertDate(histevent.getDate());
		setBuildId(myBuild);
		readDeviceIdFromFile();
		setImei(readImeiFromSystem());
		setUptime(histevent.getOption());
		updateRebootReason();
	}

	private void fillUptimeEvent(HistoryEvent histevent, String myBuild) {
		setEventId(histevent.getEventId());
		setEventName(histevent.getEventName());
		date = convertDate(histevent.getDate());
		setBuildId(myBuild);
		readDeviceIdFromFile();
		setImei(readImeiFromSystem());
		setUptime(histevent.getType());
	}

	private void fillStatsEvent(HistoryEvent histevent, String myBuild) {
		//crashdir should be filled before genericparsefile
		setCrashDir(histevent.getOption());
		GenericParseFile aParseFile = null;
		try{
			aParseFile = new GenericParseFile(getCrashDir(), "_trigger");

		}catch (FileNotFoundException e){
			Log.w(toString() + ",parsefile couldn't be created: " + getCrashDir());
		}
		fillGenericEvent(histevent,myBuild,aParseFile);
	}

	private void fillGenericEvent(HistoryEvent histevent, String myBuild,GenericParseFile aParseFile) {
		setEventId(histevent.getEventId());
		setEventName(histevent.getEventName());
		date = convertDate(histevent.getDate());
		setType(histevent.getType());
		setBuildId(myBuild);
		readDeviceIdFromFile();
		setImei(readImeiFromSystem());
		if (aParseFile != null) {
			setData0(aParseFile.getValueByName("DATA0"));
			setData1(aParseFile.getValueByName("DATA1"));
			setData2(aParseFile.getValueByName("DATA2"));
			setData3(aParseFile.getValueByName("DATA3"));
			setData4(aParseFile.getValueByName("DATA4"));
			setData5(aParseFile.getValueByName("DATA5"));
			setDataReady(!aParseFile.getValueByName("DATAREADY").equals("0"));
		}
	}

	private void fillStateEvent(HistoryEvent histevent, String myBuild) {
		setEventId(histevent.getEventId());
		setEventName(histevent.getEventName());
		setType(histevent.getType());
		date = convertDate(histevent.getDate());
		setBuildId(myBuild);
		readDeviceIdFromFile();
		setImei(readImeiFromSystem());
	}

	private void fillAplogEvent(HistoryEvent histevent, String myBuild) {
		setCrashDir(histevent.getOption());
		setEventId(histevent.getEventId());
		setEventName(histevent.getEventName());
		date = convertDate(histevent.getDate());
		setType(histevent.getType());
		setBuildId(myBuild);
		readDeviceIdFromFile();
		setImei(readImeiFromSystem());
	}

	private void fillBzEvent(HistoryEvent histevent, String myBuild) {
		setCrashDir(histevent.getOption());
		setEventId(histevent.getEventId());
		setEventName(histevent.getEventName());
		date = convertDate(histevent.getDate());
		setType(histevent.getType());
		setBuildId(myBuild);
		readDeviceIdFromFile();
		setImei(readImeiFromSystem());
	}

	private void fillErrorEvent(HistoryEvent histevent, String myBuild) {
		//crashdir should be filled before genericparsefile
		setCrashDir(histevent.getOption());
		GenericParseFile aParseFile = null;
		try{
			aParseFile = new GenericParseFile(getCrashDir(), "_errorevent");

		}catch (FileNotFoundException e){
			Log.w(toString() + ",parsefile couldn't be created: " + getCrashDir());
		}
		fillGenericEvent(histevent,myBuild,aParseFile);
	}


	private void fillInfoEvent(HistoryEvent histevent, String myBuild) {
		//crashdir should be filled before genericparsefile
		setCrashDir(histevent.getOption());
		GenericParseFile aParseFile = null;
		try{
			aParseFile = new GenericParseFile(getCrashDir(), "_infoevent");

		}catch (FileNotFoundException e){
			Log.w(toString() + ",parsefile couldn't be created: " + getCrashDir());
		}
		fillGenericEvent(histevent,myBuild,aParseFile);
	}

	public com.intel.crashtoolserver.bean.Event getEventForServer(com.intel.crashreport.specific.Build build, String sToken) {

		com.intel.crashtoolserver.bean.Event event;
		long lUptime = convertUptime(getUptime());
		com.intel.crashreport.specific.Build mBuild;
		if (getBuildId().contentEquals(build.toString()))
			mBuild = build;
		else {
			mBuild = new com.intel.crashreport.specific.Build(getBuildId());
			if (mBuild.getBuildId().contentEquals(""))
				mBuild = build;
		}
		com.intel.crashtoolserver.bean.Build sBuild = mBuild.getBuildForServer();
		sBuild.setVariant(getVariant());
		sBuild.setIngredientsJson(this.ingredients);
		// do not use "uniquekey" of crashtool object, it is for internal use only
		//uniqueKeyComponents should be used
		sBuild.setUniqueKeyComponents(IngredientManager.INSTANCE.parseUniqueKey(this.uniqueKeyComponent));
		com.intel.crashtoolserver.bean.Device aDevice;
		String sSSN = getSSN();
		//GCM not fully implemented

		String sTokenGCM = sToken;

		String sSPID = getSPIDFromFile();
		if (sSSN.equals("")){
			aDevice = new Device(getDeviceId(), getImei(), null /*ssn not implemented if property empty*/, sTokenGCM, sSPID);
		}else{
			aDevice = new Device(getDeviceId(), getImei(), sSSN, sTokenGCM, sSPID);
		}
		event = new com.intel.crashtoolserver.bean.Event(this.getEventId(), this.getEventName(), this.getType(),
				this.getData0(), this.getData1(), this.getData2(), this.getData3(), this.getData4(), this.getData5(),
				this.date, lUptime, null /*logfile*/,sBuild,com.intel.crashtoolserver.bean.Event.Origin.CLOTA,
                                aDevice, getiRowID(),this.pdStatus );
		event.setBootMode(this.osBootMode);
		return event;
	}

	@Override
	public void readDeviceIdFromFile() {
		setDeviceId(getDeviceIdFromFile());
	}

	public static String getDeviceIdFromFile() {
		String sResult = "";
		File uuidFile = new File(UUID_FILE_PATH);
		Scanner scan = null;
		try {
			scan = new Scanner(uuidFile);
			if (scan.hasNext())
				sResult = scan.nextLine();
		} catch (FileNotFoundException e) {
			Log.w("CrashReportService: deviceId not set");
		} catch (IllegalStateException e) {
			Log.e("CrashReportService: deviceId not in good state");
		} finally {
			if (scan != null) {
				scan.close();
			}
		}
		return sResult;
	}

	public static String getSPIDFromFile() {
		String sResult = "";
		File spidFile = new File(SPID_FILE_PATH);
		Scanner scan = null;
		try {
			scan = new Scanner(spidFile);
			if (scan.hasNext())
				sResult = scan.nextLine();
		} catch (FileNotFoundException e) {
			Log.w("CrashReportService: spid not set");
		} catch (IllegalStateException e) {
			Log.e("CrashReportService: spid not in good state");
		} finally {
			 if (scan != null) {
                                scan.close();
                        }
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
		return ((getType().equals("JAVACRASH") ||
				getType().equals("ANR") ||
				getType().equals("UIWDT")));
	}

	/**
	 * Indicates if the event is a Dropbox event detected in full Dropbox condition.
	 *
	 * @return true if the event is a Dropbox event detected in full Dropbox condition. False otherwise.
	 */
	public boolean isFullDropboxEvent() {
		return (isDropboxEvent() && getData0().equals("full dropbox"));
	}

	/**
	 * Indicates if the kind of the event is allowed to be included in a rain of crashes.
	 * @return true if the kind of the event is subject to rain mechanism. False otherwise.
	 */
	public boolean isRainEventKind() {
		return ((getType().equals("JAVACRASH") ||
				getType().equals("ANR") ||
				getType().equals("TOMBSTONE")));
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
			aParseFile = new GenericParseFile(EVENTS_DIR, "eventfile_"+getEventId());

		}catch (FileNotFoundException e){
			Log.w(toString() + ",eventfile couldn't be created: " + EVENTS_DIR);
		}
		if(aParseFile != null){
			setData0(aParseFile.getValueByName("DATA0"));
			setData1(aParseFile.getValueByName("DATA1"));
			setData2(aParseFile.getValueByName("DATA2"));
			setData3(aParseFile.getValueByName("DATA3"));
			setData4(aParseFile.getValueByName("DATA4"));
			setOsBootMode(aParseFile.getValueByName("BOOTMODE"));
		}
	}


}
