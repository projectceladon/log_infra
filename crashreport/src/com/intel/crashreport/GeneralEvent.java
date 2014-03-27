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

package com.intel.crashreport;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;

import com.intel.parsing.ParsableEvent;
import com.intel.phonedoctor.Constants;

import android.os.SystemProperties;

public class GeneralEvent {

	public static final String BOOT_MOS_TO_MOS = "MOS-MOS";
	public static final String BOOT_MOS_TO_POS = "MOS-POS";
	public static final String BOOT_MOS_TO_ROS = "MOS-ROS";
	public static final String BOOT_MOS_TO_COS = "MOS-COS";
	public static final String BOOT_POS_TO_MOS = "POS-MOS";
	public static final String BOOT_POS_TO_POS = "POS-POS";
	public static final String BOOT_POS_TO_ROS = "POS-ROS";
	public static final String BOOT_POS_TO_COS = "POS-COS";
	public static final String BOOT_ROS_TO_MOS = "ROS-MOS";
	public static final String BOOT_ROS_TO_POS = "ROS-POS";
	public static final String BOOT_ROS_TO_ROS = "ROS-ROS";
	public static final String BOOT_ROS_TO_COS = "ROS-COS";
	public static final String BOOT_COS_TO_MOS = "COS-MOS";
	public static final String BOOT_COS_TO_POS = "COS-POS";
	public static final String BOOT_COS_TO_ROS = "COS-ROS";
	public static final String BOOT_COS_TO_COS = "COS-COS";

	private static final String UUID_FILE_PATH = Constants.LOGS_DIR + "/uuid.txt";

	protected final static SimpleDateFormat EVENT_DF = new SimpleDateFormat("yyyy-MM-dd/HH:mm:ss");
	protected final static SimpleDateFormat EVENT_DF_OLD = new SimpleDateFormat("yy-MM-dd-HH-mm-ss");

	protected ParsableEvent mParsableEvent;
	protected String ingredients = "";
	protected String osBootMode = "";
	protected boolean uploaded = false;
	protected boolean logUploaded = false;
	/*Define event validity : not valid if a mandatory attribute is missing */
	private boolean valid = true;
	protected Date date = null;

	protected String origin = "";
	protected String pdStatus = "";
	protected int logsSize = 0;

	public GeneralEvent() {
		//default empty values
		mParsableEvent= new ParsableEvent(-1, "", "", "", "", "", "", "", "", "", "",	"", "", "","");
	}

	public GeneralEvent(int rowid, String eventId, String eventName, String type, String data0,
			String data1, String data2, String data3,
			String data4, String data5, String date, String buildId,
			String deviceId, String imei, String uptime, String crashDir) {
		mParsableEvent= new ParsableEvent(rowid, eventId, eventName, type, data0,
				data1, data2, data3,
				data4, data5, buildId,
				deviceId, imei, uptime,crashDir);
		this.date = convertDate(date);

	}

	public GeneralEvent(int rowid, String eventId, String eventName, String type, String data0,
			String data1, String data2, String data3,
			String data4, String data5, Date date, String buildId,
			String deviceId, String imei, String uptime, String crashDir) {
		mParsableEvent= new ParsableEvent(rowid, eventId, eventName, type, data0,
				data1, data2, data3,
				data4, data5, buildId,
				deviceId, imei, uptime,crashDir);
		this.date = date;
	}

	public void readDeviceIdFromSystem() {
		setDeviceId(getDeviceIdFromSystem());
	}

	@Override
	public String toString() {
		if (getEventName().equals("UPTIME"))
			return new String("Event: " + getEventId() + ":" + getEventName() + ":" + getUptime());
		else
			return new String("Event: " + getEventId() + ":" + getEventName() + ":" + getType());
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

	public static String getDeviceIdFromSystem() {
		String sResult = "";
		sResult += android.os.Build.SERIAL;
		return sResult;
	}

	public static String getSSN(){
		return SystemProperties.get("ro.serialno", "");
	}

	public String readImeiFromSystem() {
		String imeiRead = "";
		try {
			imeiRead = SystemProperties.get("persist.radio.device.imei", "");
			if(imeiRead.equals("")) {
				imeiRead = GeneralEventGenerator.INSTANCE.getImei();
			}
		}
		catch (IllegalArgumentException e) {
			Log.w("CrashReportService: IMEI not available");
		}
		return imeiRead;
	}

	public static Date convertDate(String date) {
		Date cDate = null;
		if (date != null) {
			try {
				EVENT_DF.setTimeZone(TimeZone.getTimeZone("GMT"));
				cDate = EVENT_DF.parse(date);
			} catch (ParseException e) {
				try {
					EVENT_DF_OLD.setTimeZone(TimeZone.getTimeZone("GMT"));
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
		return mParsableEvent.getEventId();
	}

	public void setEventId(String eventId) {
		mParsableEvent.setEventId(eventId);
	}

	public String getEventName() {
		return mParsableEvent.getEventName();
	}

	public void setEventName(String eventName) {
		mParsableEvent.setEventName(eventName);
	}

	public String getType() {
		return mParsableEvent.getType();
	}

	public void setType(String type) {
		mParsableEvent.setType(type);
	}

	public String getData0() {
		return mParsableEvent.getData0();
	}

	public void setData0(String data0) {
		mParsableEvent.setData0(data0);
	}

	public String getData1() {
		return mParsableEvent.getData1();
	}

	public void setData1(String data1) {
		mParsableEvent.setData1(data1);
	}

	public String getData2() {
		return mParsableEvent.getData2();
	}

	public void setData2(String data2) {
		mParsableEvent.setData2(data2);
	}

	public String getData3() {
		return mParsableEvent.getData3();
	}

	public void setData3(String data3) {
		mParsableEvent.setData3(data3);
	}

	public String getData4() {
		return mParsableEvent.getData4();
	}

	public void setData4(String data4) {
		mParsableEvent.setData4(data4);
	}

	public String getData5() {
		return mParsableEvent.getData5();
	}

	public void setData5(String data5) {
		mParsableEvent.setData5(data5);
	}

	public String getDateAsString() {
		EVENT_DF.setTimeZone(TimeZone.getTimeZone("GMT"));
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
		return mParsableEvent.getBuildId();
	}

	public void setBuildId(String buildId) {
		mParsableEvent.setBuildId(buildId);
	}

	public String getDeviceId() {
		return mParsableEvent.getDeviceId();
	}

	public void setDeviceId(String deviceId) {
		mParsableEvent.setDeviceId(deviceId);
	}

	public String getUptime() {
		return mParsableEvent.getUptime();
	}

	public void setUptime(String uptime) {
		mParsableEvent.setUptime(uptime);
	}

	public String getImei() {
		return mParsableEvent.getImei();
	}

	public void setImei(String imei) {
		mParsableEvent.setImei(imei);
	}

	public void setCrashDir(String crashDir) {
		mParsableEvent.setCrashDir(crashDir);
	}

	public String getCrashDir() {
		return mParsableEvent.getCrashDir();
	}

	public boolean isDataReady() {
		return mParsableEvent.isDataReady();
	}

	public void setDataReady(boolean dataReady) {
		mParsableEvent.setDataReady(dataReady);
	}

	public boolean isUploaded() {
		return uploaded;
	}

	public void setUploaded(boolean uploaded) {
		this.uploaded = uploaded;
	}

	public boolean isLogUploaded() {
		return logUploaded;
	}

	public void setLogUploaded(boolean logUploaded) {
		this.logUploaded = logUploaded;
	}

	public void setOrigin(String mOrigin) {
		origin = mOrigin;
	}

	public String getOrigin() {
		return origin;
	}

	public int getiRowID() {
		return mParsableEvent.getiRowID();
	}

	public void setiRowID(int iRowID) {
		mParsableEvent.setiRowID(iRowID);
	}
	public String getPdStatus() {
		return pdStatus;
	}

	public void setPdStatus(String pdStatus) {
		this.pdStatus = pdStatus;
	}

	public void setValid(boolean validity) {
		this.valid = validity;
	}

	public boolean isValid() {
		return this.valid;
	}

	public int getLogsSize() {
		return logsSize;
	}

	public void setLogsSize(int size) {
		logsSize = size;
	}

	public String getVariant() {
		return mParsableEvent.getVariant();
	}

	public void setVariant(String variant) {
		mParsableEvent.setVariant(variant);
	}

	public ParsableEvent getParsableEvent(){
		return mParsableEvent;
	}

	/**
	 * Returns this object's <i>ingredients</i> as JSON string.
	 * @return this object <i>ingredients</i>.
	 */
	public String getIngredients() {
		return this.ingredients;
	}

	/**
	 * Sets this object's <i>ingredients</i> value to the given
	 * JSON string.
	 * @param ingredients the new ingredients values as string.
	 */
	public void setIngredients(String ingredients) {
		this.ingredients = ingredients;
	}

	/**
	 * Returns this object's boot mode (actually the boot transition).
	 * This property is a descripive string indicating what the previous
	 * boot mode was (<i>MOS</i>, <i>POS</i>, <i>COS</i>) and what the current
	 * boot mode is.
	 * @return this object's boot mode value
	 */
	public String getOsBootMode() {
		return this.osBootMode;
	}

	/**
	 * Sets this objec's boot mode to the given string.
	 * @param osBootMode the new OS boot mode value
	 */
	public void setOsBootMode(String osBootMode) {
		this.osBootMode = osBootMode;
	}
}
