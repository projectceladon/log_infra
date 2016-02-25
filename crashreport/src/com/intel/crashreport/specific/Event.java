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

package com.intel.crashreport.specific;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;

import android.content.Context;
import android.os.SystemProperties;

import com.intel.crashreport.core.GeneralEvent;
import com.intel.crashreport.GenericParseFile;
import com.intel.crashreport.Log;
import com.intel.crashreport.specific.PDStatus.PDSTATUS_TIME;
import com.intel.crashreport.specific.ingredients.IngredientManager;
import com.intel.crashreport.specific.ingredients.DeviceManager;
import com.intel.crashtoolserver.bean.Device;
import com.intel.phonedoctor.Constants;
import com.intel.phonedoctor.utils.FileOps;

import org.json.JSONException;
import org.json.JSONObject;

public class Event extends GeneralEvent{

	private static final String UUID_FILE_PATH = Constants.LOGS_DIR + "/uuid.txt";
	private static final String SPID_FILE_PATH = Constants.LOGS_DIR + "/spid.txt";
	private static final String EVENTS_DIR = Constants.LOGS_DIR + "/events";

	private static final String TC_UUID_PROPERTY_NAME = "persist.crashlogd.TC.uuid";
	private static final String TC_NAME_PROPERTY_NAME = "persist.crashlogd.TC.name";
	private static final String TC_DATE_DUT_PROPERTY_NAME = "persist.crashlogd.TC.date_dut";
	private static final String TC_DATE_HOST_PROPERTY_NAME = "persist.crashlogd.TC.date_host";
	private static final String TC_ENGINE_PROPERTY_NAME = "persist.crashlogd.TC.engine";
	private static final String TC_ITER_PROPERTY_NAME = "persist.crashlogd.TC.iteration";

	public Event() {
		super();
	}

	public Event(GeneralEvent event) {
		super(event);
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
		setVariant(Build.getVariant());
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
                setTestCase(fillTestInfo(getCrashDir()));
		//extra step : format data for specific event
		new FormatParser(this).execFormat();

		FileOps.compressFolderContent(getCrashDir());
	}

	private com.intel.crashtoolserver.bean.TestCase fillTestInfo(String path) {
		String uuid = null, name = null, date_dut = null, date_host = null, engine = null;
		String content;
		int iter = 0;

		File f = new File(path, "test_case.json");
		if(!f.exists() || f.isDirectory()) {
			return null;
		}

		try {
			content = new Scanner(f).useDelimiter("\\Z").next();
		} catch (FileNotFoundException e) {
			Log.e("Problem loading latest test info: " + f + "\n" + e);
			return null;
		}

		try {
			JSONObject jsonObj= new JSONObject(content);

			uuid = (jsonObj.has(TC_UUID_PROPERTY_NAME))
				? (String)jsonObj.get(TC_UUID_PROPERTY_NAME) : null;
			name = (jsonObj.has(TC_NAME_PROPERTY_NAME))
				? (String)jsonObj.get(TC_NAME_PROPERTY_NAME) : null;
			date_dut = (jsonObj.has(TC_DATE_DUT_PROPERTY_NAME))
				? (String)jsonObj.get(TC_DATE_DUT_PROPERTY_NAME) : null;
			date_host = (jsonObj.has(TC_DATE_HOST_PROPERTY_NAME))
				? (String)jsonObj.get(TC_DATE_HOST_PROPERTY_NAME) : null;
			engine = (jsonObj.has(TC_ENGINE_PROPERTY_NAME))
				? (String)jsonObj.get(TC_ENGINE_PROPERTY_NAME) : null;
			iter = Integer.parseInt((jsonObj.has(TC_ITER_PROPERTY_NAME))
				? (String)jsonObj.get(TC_ITER_PROPERTY_NAME) : "0");
		} catch (JSONException e) {
			Log.e("Problem while loading latest test info: " + f + "\n" + e);
		}

		return new com.intel.crashtoolserver.bean.TestCase(uuid, name, iter, date_dut, date_host, engine);
	}

	public com.intel.crashtoolserver.bean.TestCase  fillCurrentTestInfo() {
		String uuid = null, name = null, date_dut = null, date_host = null, engine = null;
		int iter = 0;

		try {
			uuid = SystemProperties.get(TC_UUID_PROPERTY_NAME, null);
			name = SystemProperties.get(TC_NAME_PROPERTY_NAME, null);
			date_dut = SystemProperties.get(TC_DATE_DUT_PROPERTY_NAME, null);
			date_host = SystemProperties.get(TC_DATE_HOST_PROPERTY_NAME, null);
			engine = SystemProperties.get(TC_ENGINE_PROPERTY_NAME, null);
			iter = Integer.parseInt(SystemProperties.get(TC_ITER_PROPERTY_NAME, "0"));
		} catch (IllegalArgumentException e) {
			Log.e("Problem while loading running test info:\n" + e);
		}

		return new com.intel.crashtoolserver.bean.TestCase(uuid, name, iter, date_dut, date_host, engine);
	}

	private void fillCrashEvent(HistoryEvent histevent, String myBuild, boolean isUserBuild) {

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
			if(!crashFile.getImei().isEmpty())
				setImei(crashFile.getImei());
			else setImei(readImeiFromSystem());
			setUptime(crashFile.getUptime());
			//DATA0-5 expected to be set by parserContainer
			if (!ParserContainer.INSTANCE.parseEvent(this)){
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
			} else if (getType().contains("MPANIC") && (!isUserBuild) &&
					!DeviceManager.INSTANCE.hasModemExtension(false) &&
					DeviceManager.INSTANCE.isModemUnknown()) {
				this.setDataReady(false);
				DeviceManager.INSTANCE.addEventMPanicNotReady(this.getEventId());
				this.setData3("not_ready");
			} else if (crashFile.getDataReady() == 0){
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
			setBuildId(myBuild);
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
			setModemVersionUsed(aParseFile.getValueByName("MODEMVERSIONUSED"));
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
		GenericParseFile aParseFile = null;
		try{
			aParseFile = new GenericParseFile(getCrashDir(), "user_comment");

		}catch (FileNotFoundException e){
			Log.w(toString() + ",parsefile couldn't be created: " + getCrashDir());
		}
		fillGenericEvent(histevent,myBuild,aParseFile);
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
		String sSSN = getSSN();
		sSSN = (sSSN.isEmpty()) ? null : sSSN;
		com.intel.crashtoolserver.bean.Device aDevice = new Device(getDeviceId(),
			getImei(), sSSN, sToken, getSPIDFromFile());

		return super.getEventForServer(aDevice, build);
	}

	@Override
	public String readImeiFromSystem() {
		String imeiRead;
		try {
			imeiRead = super.readImeiFromSystem();
		}
		catch (IllegalArgumentException e) {
			Log.d("CrashReportService: IMEI not available");
			imeiRead = "";
		}
		return imeiRead;
	}

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
