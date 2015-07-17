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
		String path = "";
		for(String screen:screenshots) {
			if(path.equals(""))
				path = screen;
			else
				path += "," + screen;

		}
		return path;
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
