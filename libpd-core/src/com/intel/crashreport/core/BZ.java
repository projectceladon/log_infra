/* Copyright (C) 2019 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.crashreport.core;

import com.intel.crashreport.common.IEventLog;

import java.util.ArrayList;
import java.util.Date;

public class BZ {
	private static final IEventLog log = Logger.getLog();

	protected String eventId = "";
	protected String summary = "";
	protected String description = "";
	protected String component = "";
	protected String severity = "";
	protected String type = "";
	protected boolean hasScreenshot = false;
	protected ArrayList<String> screenshots;
	protected boolean isUploaded = false;
	protected boolean logsAreUploaded = false;
	protected boolean isValid = true;
	protected Date creationDate;
	protected Date uploadDate;
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

	public String getScreenshotsToString() {
		StringBuffer path = new StringBuffer();
		for(String screen:screenshots) {
			if (path.length() == 0)
				path.append(screen);
			else
				path.append("," + screen);

		}
		return path.toString();
	}

	public void setScreenshots(String screen) {
		if (screen == null) {
			log.w("BZ:setScreenshots: not screenshot founded");
			return;
                }

		String screens[] = screen.split(",");
		if(screens.length > 0) {
			screenshots = new ArrayList<String>();
			for(String screenshot:screens){
				screenshots.add(screenshot);
			}
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
