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

import java.io.File;
import java.io.Serializable;
import java.util.Date;

/**
 * Bean Event, used to transmit event data between a client and a server
 *
 * Be aware that bean is used by CLOTA and MPTA, do not rename package and class
 * name for down ward.
 *
 * @author mauretx
 *
 */
public class Event implements Serializable {

	private static final long serialVersionUID = -1516767267932884874L;

	/**
	 * Origin exhaustive list
	 * default value : GENE
	 */
	public enum Origin {
		CLOTA, MPTA, GENE, ACS
	}

	/**
	 * rowid default value : GENE
	 */
	public static final long UNKNOWN_ROWID = -1;

	private Long id;
	private String eventId;
	private String event;
	private String type;
	private String data0;
	private String data1;
	private String data2;
	private String data3;
	private String data4;
	private String data5;
	private Date date;
	private String dateString;

	@Deprecated
	private String buildId;
	@Deprecated
	private String deviceId;
	private String testId;
	private long uptime;
	private String logFileName;
	private File logFile;
	private String reportFile;
	private String imei;
	private String origin;
	private String fileOrigin;
	private Date insertedEventDate;
	private Date insertedFileDate;
	private Boolean rejected;
	private int bplog;
	private long rowId;
	private String pdStatus;

	private Build build;
	private Device device;
	private Uptime uptimeObj;
	private Crashtype crashtype;

	public Event() {
		super();
	}

	/**
	 * Constructor used for CLOTA without IMEI
	 *
	 * @param eventId
	 * @param event
	 * @param type
	 * @param data0
	 * @param data1
	 * @param data2
	 * @param date
	 * @param buildId
	 * @param deviceId
	 * @param uptime
	 */
	@Deprecated
	public Event(String eventId, String event, String type, String data0,
			String data1, String data2, Date date, String buildId,
			String deviceId, long uptime) {

		this( eventId, event, type, data0, data1, data2, null, null, null, date, buildId, deviceId, null, null, uptime, null, null, null, null, UNKNOWN_ROWID, null);
	}

	/**
	 * Instantiate an event used by CLOTA with IMEI and data3-5
	 *
	 * @param eventId
	 * @param event
	 * @param type
	 * @param data0
	 * @param data1
	 * @param data2
	 * @param data3
	 * @param data4
	 * @param data5
	 * @param date
	 * @param buildId
	 * @param deviceId
	 * @param imei
	 * @param uptime
	 */
	@Deprecated
	public Event(String eventId, String event, String type, String data0,
			String data1, String data2, String data3,
			String data4, String data5, Date date, String buildId,
			String deviceId, String imei, long uptime) {

		this( eventId, event, type, data0, data1, data2, data3, data4, data5, date, buildId, deviceId, imei, null, uptime, null, null, null, null, UNKNOWN_ROWID, null);
	}

	/**
	 * Instantiate an event used by CLOTA with IMEI and data3-5 and buildObject
	 *
	 * @param eventId
	 * @param event
	 * @param type
	 * @param data0
	 * @param data1
	 * @param data2
	 * @param data3
	 * @param data4
	 * @param data5
	 * @param date
	 * @param deviceId
	 * @param imei
	 * @param uptime
	 * @param build
	 */
	public Event(String eventId, String event, String type, String data0,
			String data1, String data2, String data3,
			String data4, String data5, Date date,
			String deviceId, String imei, long uptime, Build build) {

		this( eventId, event, type, data0, data1, data2, data3, data4, data5, date, null, deviceId, imei, null, uptime, null, build, null, null, UNKNOWN_ROWID, null);
	}

	/**
	 * Instantiate an event used by MPTA 2.4
	 *
	 * @param eventId
	 * @param event
	 * @param type
	 * @param data0
	 * @param data1
	 * @param data2
	 * @param date
	 * @param buildId
	 * @param deviceId
	 * @param uptime
	 * @param logFile
	 */
	@Deprecated
	public Event(String eventId, String event, String type, String data0,
			String data1, String data2, Date date, String buildId,
			String deviceId, long uptime, File logFile) {

		this( eventId, event, type, data0, data1, data2, null, null, null, date, buildId, deviceId, null, null, uptime, logFile, null, null, null, UNKNOWN_ROWID, null);
	}

	/**
	 * Instantiate an event used by MPTA 2.4.1
	 *
	 * @param eventId
	 * @param event
	 * @param type
	 * @param data0
	 * @param data1
	 * @param data2
	 * @param data3
	 * @param data4
	 * @param data5
	 * @param date
	 * @param buildId
	 * @param deviceId
	 * @param uptime
	 * @param logFile
	 */
	@Deprecated
	public Event(String eventId, String event, String type, String data0,
			String data1, String data2, String data3, String data4,
			String data5, Date date, String buildId, String deviceId,
			long uptime, File logFile) {

		this( eventId, event, type, data0, data1, data2, data3, data4, data5, date, buildId, deviceId, null, null, uptime, logFile, null, null, null, UNKNOWN_ROWID, null);
	}

	/**
	 * Instantiate an event used by MPTA 2.4.1 to 2.4.3
	 *
	 * @param eventId
	 * @param event
	 * @param type
	 * @param data0
	 * @param data1
	 * @param data2
	 * @param data3
	 * @param data4
	 * @param data5
	 * @param date
	 * @param buildId
	 * @param deviceId
	 * @param imei
	 * @param uptime
	 * @param logFile
	 */
	@Deprecated
	public Event(String eventId, String event, String type, String data0,
			String data1, String data2, String data3, String data4,
			String data5, Date date, String buildId, String deviceId, String imei,
			long uptime, File logFile) {

		this( eventId, event, type, data0, data1, data2, data3, data4, data5, date, buildId, deviceId, imei, null, uptime, logFile, null, null, null, UNKNOWN_ROWID, null);
	}

	/**
	 * Instantiate an event used by MPTA above 2.4.4
	 *
	 * @param eventId
	 * @param event
	 * @param type
	 * @param data0
	 * @param data1
	 * @param data2
	 * @param data3
	 * @param data4
	 * @param data5
	 * @param date
	 * @param deviceId
	 * @param imei
	 * @param uptime
	 * @param logFile
	 * @param build
	 */
	@Deprecated
	public Event(String eventId, String event, String type, String data0,
			String data1, String data2, String data3, String data4,
			String data5, Date date, String deviceId, String imei,
			long uptime, File logFile, Build build) {

		this( eventId, event, type, data0, data1, data2, data3, data4, data5, date, null, deviceId, imei, null, uptime, logFile, build, null, null, UNKNOWN_ROWID, null);
	}

	/**
	 * Instantiate an event used by MPTA above 2.6 and ACS
	 *
	 * @param eventId
	 * @param event
	 * @param type
	 * @param data0
	 * @param data1
	 * @param data2
	 * @param data3
	 * @param data4
	 * @param data5
	 * @param date
	 * @param deviceId
	 * @param imei
	 * @param uptime
	 * @param logFile
	 * @param build
	 */
	@Deprecated
	public Event(String eventId, String event, String type, String data0,
			String data1, String data2, String data3, String data4,
			String data5, Date date, String deviceId, String imei,
			long uptime, File logFile, Build build, Event.Origin origin) {

		this( eventId, event, type, data0, data1, data2, data3, data4, data5, date, null, deviceId, imei, null, uptime, logFile, build, origin, null, UNKNOWN_ROWID, null);
	}

	/**
	 * Instantiate an event used by MPTA 2.7 (has never been used in production)
	 * @param eventId
	 * @param event
	 * @param type
	 * @param data0
	 * @param data1
	 * @param data2
	 * @param data3
	 * @param data4
	 * @param data5
	 * @param date
	 * @param uptime
	 * @param logFile
	 * @param build
	 * @param origin
	 * @param device
	 * @param rowId
	 */
	@Deprecated
	public Event(String eventId, String event, String type, String data0,
			String data1, String data2, String data3, String data4,
			String data5, Date date,
			long uptime, File logFile, Build build, Event.Origin origin, Device device, long rowId) {

		this( eventId, event, type, data0, data1, data2, data3, data4, data5, date, null, null, null, null, uptime, logFile, build, origin, device, rowId, null);
	}


	/**
	 * Instantiate an event used by MPTA above 2.7 and ACS
	 * @param eventId
	 * @param event
	 * @param type
	 * @param data0
	 * @param data1
	 * @param data2
	 * @param data3
	 * @param data4
	 * @param data5
	 * @param date
	 * @param uptime
	 * @param logFile
	 * @param build
	 * @param origin
	 * @param device
	 * @param rowId
	 */
	public Event(String eventId, String event, String type, String data0,
			String data1, String data2, String data3, String data4,
			String data5, Date date,
			long uptime, File logFile, Build build, Event.Origin origin, Device device, long rowId, String pdStatus) {

		this( eventId, event, type, data0, data1, data2, data3, data4, data5, date, null, null, null, null, uptime, logFile, build, origin, device, rowId, pdStatus);
	}

	/**
	 * Instantiate an event
	 *
	 * @param eventId
	 * @param event
	 * @param type
	 * @param data0
	 * @param data1
	 * @param data2
	 * @param data3
	 * @param data4
	 * @param data5
	 * @param date
	 * @param buildId
	 * @param deviceId
	 * @param imei
	 * @param testId
	 * @param uptime
	 * @param logFile
	 */
	private Event(String eventId, String event, String type, String data0,
			String data1, String data2, String data3, String data4,
			String data5, Date date, String buildId, String deviceId, String imei,
			String testId, long uptime, File logFile, Build build, Event.Origin origin, Device device, long rowId, String pdStatus) {

		this.eventId = eventId;
		this.event = event;
		this.type = type;
		this.data0 = data0;
		this.data1 = data1;
		this.data2 = data2;
		this.date = date;
		this.buildId = buildId;
		this.deviceId = deviceId;
		this.imei = imei;
		this.uptime = uptime;
		this.logFile = logFile;

		if (this.logFile != null) {
			this.logFileName = logFile.getName();
		}

		this.data3 = data3;
		this.data4 = data4;
		this.data5 = data5;
		this.testId = testId;
		this.build = build;
		if (origin != null) {
			this.origin = origin.name();
		}

		this.device = device;
		this.dateString = com.intel.crashtool.crashtoolDb.bean.util.DateUtils.dateToString(date);
		this.rowId = rowId;
		this.pdStatus = pdStatus;
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}

	public String getOrigin() {
		return origin;
	}

	public Event.Origin getOriginEnum() {
		if (origin == null) {
			return  null;
		}

		try {
			return Event.Origin.valueOf(origin);
		} catch (Exception e) {
			//do nothing
		}
		return null;
	}

	public void setOrigin(Origin value) {
		if (value != null) {
			this.origin = value.name();
		}
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
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

	public File getLogFile() {
		return logFile;
	}

	public void setLogFile(File logFile) {
		this.logFile = logFile;
		if (this.logFile != null) {
			this.logFileName = logFile.getName();
		}
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
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

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * deprecated, should use getBuild().getBuildId() instead
	 * @return
	 */
	@Deprecated
	public String getBuildId() {
		return buildId;
	}

	/**
	 * deprecated, should use getBuild().setBuildId() instead
	 * @return
	 */
	@Deprecated
	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}

	@Deprecated
	public String getDeviceId() {
		return deviceId;
	}

	@Deprecated
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public long getUptime() {
		return uptime;
	}

	public void setUptime(long uptime) {
		this.uptime = uptime;
	}

	public String getLogFileName() {
		return logFileName;
	}

	public void setLogFileName(String logFileName) {
		this.logFileName = logFileName;
	}

	/**
	 * @return the reportFile
	 */
	public String getReportFile() {
		return reportFile;
	}

	/**
	 * @param reportFile the reportFile to set
	 */
	public void setReportFile(String reportFile) {
		this.reportFile = reportFile;
	}

	public String getTestId() {
		return testId;
	}

	public void setTestId(String testId) {
		this.testId = testId;
	}

	/**
	 * @return the fileOrigin
	 */
	public String getFileOrigin() {
		return fileOrigin;
	}

	/**
	 * @param fileOrigin the fileOrigin to set
	 */
	public void setFileOrigin(Origin fileOrigin) {
		this.fileOrigin = fileOrigin.name();
	}

	/**
	 * @return the insertedEventDate
	 */
	public Date getInsertedEventDate() {
		return insertedEventDate;
	}

	/**
	 * @param insertedEventDate the insertedEventDate to set
	 */
	public void setInsertedEventDate(Date insertedEventDate) {
		this.insertedEventDate = insertedEventDate;
	}

	/**
	 * @return the insertedFileDate
	 */
	public Date getInsertedFileDate() {
		return insertedFileDate;
	}

	/**
	 * @param insertedFileDate the insertedFileDate to set
	 */
	public void setInsertedFileDate(Date insertedFileDate) {
		this.insertedFileDate = insertedFileDate;
	}


	/**
	 * @param origin the origin to set
	 */
	public void setOrigin(String origin) {
		this.origin = origin;
	}

	/**
	 * @param fileOrigin the fileOrigin to set
	 */
	public void setFileOrigin(String fileOrigin) {
		this.fileOrigin = fileOrigin;
	}

	/**
	 * @return the rejected
	 */
	public Boolean getRejected() {
		return rejected;
	}

	/**
	 * @param rejected the rejected to set
	 */
	public void setRejected(Boolean rejected) {
		this.rejected = rejected;
	}

	/**
	 * @return the bplog
	 */
	public int getBplog() {
		return bplog;
	}

	/**
	 * @param bplog the bplog to set
	 */
	public void setBplog(int bplog) {
		this.bplog = bplog;
	}

	/**
	 * @return the build
	 */
	public Build getBuild() {
		// used for backwork compatibility
		if (build == null) {
			build = new Build(this.getBuildId());
		}
		return build;
	}

	/**
	 * @param build the build to set
	 */
	public void setBuild(Build build) {
		this.build = build;
	}

	/**
	 * @return the device
	 */
	public Device getDevice() {

		// used for backwork compatibility
		if (device == null) {
			device = new Device(this.getDeviceId(), this.getImei());
		}
		return device;
	}

	/**
	 * @param device the device to set
	 */
	public void setDevice(Device device) {
		this.device = device;
	}

	/**
	 * @return the uptimeObj
	 */
	public Uptime getUptimeObj() {
		return uptimeObj;
	}

	/**
	 * @param uptimeObj the uptimeObj to set
	 */
	public void setUptimeObj(Uptime uptimeObj) {
		this.uptimeObj = uptimeObj;
	}

	/**
	 * @return the crashtype
	 */
	public Crashtype getCrashtype() {
		return crashtype;
	}

	/**
	 * @param crashtype the crashtype to set
	 */
	public void setCrashtype(Crashtype crashtype) {
		this.crashtype = crashtype;
	}

	/**
	 * @return the dateString
	 */
	public String getDateString() {
		return dateString;
	}

	/**
	 * @param dateString the dateString to set
	 */
	public void setDateString(String dateString) {
		this.dateString = dateString;
	}

	/**
	 * @return the rowId
	 */
	public long getRowId() {
		return rowId;
	}

	/**
	 * @param rowId the rowId to set
	 */
	public void setRowId(long rowId) {
		this.rowId = rowId;
	}

	/**
	 * @return the pdStatus
	 */
	public String getPdStatus() {
		return pdStatus;
	}

	/**
	 * @param pdStatus the pdStatus to set
	 */
	public void setPdStatus(String pdStatus) {
		this.pdStatus = pdStatus;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Event [id=" + id + ", eventId=" + eventId + ", event=" + event + ", type=" + type
				+ ", data0=" + data0 + ", data1=" + data1 + ", data2=" + data2 + ", data3=" + data3
				+ ", data4=" + data4 + ", data5=" + data5 + ", date=" + date + ", dateString="
				+ dateString + ", buildId=" + buildId + ", deviceId=" + deviceId + ", testId="
				+ testId + ", uptime=" + uptime + ", logFileName=" + logFileName + ", logFile="
				+ logFile + ", reportFile=" + reportFile + ", imei=" + imei + ", origin=" + origin
				+ ", fileOrigin=" + fileOrigin + ", insertedEventDate=" + insertedEventDate
				+ ", insertedFileDate=" + insertedFileDate + ", rejected=" + rejected + ", bplog="
				+ bplog + ", rowId=" + rowId + ", pdStatus=" + pdStatus + ", build=" + build
				+ ", device=" + device + ", uptimeObj=" + uptimeObj + ", crashtype=" + crashtype
				+ "]";
	}


}