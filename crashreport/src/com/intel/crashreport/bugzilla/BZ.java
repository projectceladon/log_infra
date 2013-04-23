package com.intel.crashreport.bugzilla;

import java.util.ArrayList;
import java.util.Date;

import com.intel.crashreport.Log;

public class BZ {

	private String eventId = "";
	private String summary = "";
	private String description = "";
	private String component = "";
	private String severity = "";
	private String type = "";
	private boolean hasScreenshot = false;
	private ArrayList<String> screenshots;
	private boolean isUploaded = false;
	private boolean logsAreUploaded = false;
	private boolean isValid = true;
	private Date creationDate;
	private Date uploadDate;
	public static final int INVALID_STATE = -1;
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

	public void setScreenshots(ArrayList<String> screen) {
		screenshots = screen;
	}

	public void setScreenshots(String screen) {
		try{
			String screens[] = screen.split(",");
			if(screens.length > 0) {
				screenshots = new ArrayList<String>();
				for(String screenshot:screens){
					screenshots.add(screenshot);
				}
			}

		}
		catch(NullPointerException e){
			Log.w("BZ:setScreenshots: not screenshot founded");
		}
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

	public void setValidity(boolean validity) {
		isValid = validity;
	}

	public boolean isValid() {
		return isValid;
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

	public ArrayList<String> getScreenshots() {
		return screenshots;
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

	public Date getCreationDate() {
		return creationDate;
	}

	public void setUploadDate(Date date) {
		uploadDate = date;
	}

	public int getState() {
		if (!isValid)
			return INVALID_STATE;
		if(isUploaded && logsAreUploaded)
			return UPLOADED_STATE;
		if(isUploaded)
			return PENDING_STATE;
		return NOT_UPLOADED_STATE;
	}

}
