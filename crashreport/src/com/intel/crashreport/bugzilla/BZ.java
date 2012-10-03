package com.intel.crashreport.bugzilla;

import java.util.Date;

public class BZ {

	private String eventId = "";
	private String summary = "";
	private String description = "";
	private String component = "";
	private String severity = "";
	private String type = "";
	private boolean hasScreenshot = false;;
	private String screenshot = "";
	private boolean isUploaded = false;
	private boolean logsAreUploaded = false;
	private Date creationDate;
	private Date uploadDate;
	public static final int UPLOADED_STATE = 0;
	public static final int NOT_UPLOADED_STATE = 3;
	public static final int PENDING_STATE = 2;

	public BZ() {}

	public void setEventId(String id) {
		eventId = id;
	}

	public void setSummary(String sum) {
		summary = sum;
	}

	public void setDescription(String desc) {
		description = desc;
	}

	public void setComponent(String comp) {
		component = comp;
	}

	public void setType(String t) {
		type = t;
	}

	public void setHasScreenshot(int screenshot) {
		hasScreenshot = (screenshot == 1);
	}

	public void setScreenshot(String screen) {
		screenshot = screen;
	}

	public void setSeverity(String sev) {
		severity = sev;
	}

	public void setUploaded(int up) {
		isUploaded = (up == 1);
	}

	public void setLogsUploaded(int up) {
		logsAreUploaded = (up == 1);
	}

	public String getEventId() {
		return eventId;
	}

	public String getSummary() {
		return summary;
	}

	public String getDescription() {
		return description;
	}

	public String getComponent() {
		return component;
	}

	public String getSeverity() {
		return severity;
	}

	public String getType() {
		return type;
	}

	public boolean hasScreenshot() {
		return hasScreenshot;
	}

	public String getScreenshot() {
		return screenshot;
	}

	public boolean isUploaded() {
		return isUploaded;
	}

	public boolean logsAreUploaded() {
		return logsAreUploaded;
	}

	public void setCreationDate(Date date) {
		 creationDate = date;
	}

	public void setUploadDate(Date date) {
		uploadDate = date;
	}

	public int getState() {
		if(isUploaded && logsAreUploaded)
			return UPLOADED_STATE;
		if(isUploaded)
			return PENDING_STATE;
		return NOT_UPLOADED_STATE;
	}



}
