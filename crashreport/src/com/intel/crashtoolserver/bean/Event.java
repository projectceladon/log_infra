package com.intel.crashtoolserver.bean;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

/**
 * Bean Event
 *
 * @author mauretx
 *
 */
public class Event implements Serializable {

	private static final long serialVersionUID = -1516767267932884874L;

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
	private String buildId;
	private String deviceId;
	private String testId;
	private long uptime;
	private String logFileName;
	private File logFile;
	private String imei;


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

	public Event() {
		super();
	}

	public String getTestId() {
		return testId;
	}

	public void setTestId(String testId) {
		this.testId = testId;
	}

	@Override
	public String toString() {
		return "Event [eventId=" + eventId + ", event=" + event + ", type="
				+ type + ", data0=" + data0 + ", data1=" + data1 + ", data2="
				+ data2 + ", data3=" + data3 + ", data4=" + data4 + ", data5="
				+ data5 + ", date=" + date + ", buildId=" + buildId
				+ ", deviceId=" + deviceId + ", testId=" + testId + ", uptime="
				+ uptime + ", logFileName=" + logFileName + ", logFile="
				+ logFile + ", imei=" + imei + "]";
	}

	/**
	 * Constructor used for CLOTA without imei
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

		this( eventId, event, type, data0, data1, data2, null, null, null, date, buildId, deviceId, null, null, uptime, null);
	}

	/**
	 * Constructor used for CLOTA with imei and data3-5
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
	public Event(String eventId, String event, String type, String data0,
			String data1, String data2, String data3,
			String data4, String data5, Date date, String buildId,
			String deviceId, String imei, long uptime) {

		this( eventId, event, type, data0, data1, data2, data3, data4, data5, date, buildId, deviceId, imei, null, uptime, null);
	}

	/**
	 * Constructor used for MPTA 2.4
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

		this( eventId, event, type, data0, data1, data2, null, null, null, date, buildId, deviceId, null, null, uptime, logFile);
	}

	/**
	 * Constructor used for MPTA 2.4.1
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

		this( eventId, event, type, data0, data1, data2, data3, data4, data5, date, buildId, deviceId, null, null, uptime, logFile);
	}

	/**
	 * Constructor used for MPTA above 2.4.1
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
	public Event(String eventId, String event, String type, String data0,
			String data1, String data2, String data3, String data4,
			String data5, Date date, String buildId, String deviceId, String imei,
			long uptime, File logFile) {

		this( eventId, event, type, data0, data1, data2, data3, data4, data5, date, buildId, deviceId, imei, null, uptime, logFile);
	}

	/**
	 * Constructor used for ACS
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
	public Event(String eventId, String event, String type, String data0,
			String data1, String data2, String data3, String data4,
			String data5, Date date, String buildId, String deviceId, String imei,
			String testId, long uptime, File logFile) {

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
	}
}
