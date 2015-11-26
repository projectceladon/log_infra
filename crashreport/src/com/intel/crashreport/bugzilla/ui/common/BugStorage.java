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

package com.intel.crashreport.bugzilla.ui.common;

import java.util.ArrayList;
import java.util.HashSet;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class BugStorage {

	private static String BUGZILLA_PREFERENCES  = "bugzillaPrivatePreferences";
	private static String BUGZILLA_SUMMARY = "bugzillaSummary";
	private static String BUGZILLA_DESCRIPTION = "bugzillaDescription";
	private static String BUGZILLA_COMPONENT = "bugzillaComponent";
	private static String BUGZILLA_TYPE = "bugzillaType";
	private static String BUGZILLA_SEVERITY = "bugzillaSeverity";
	private static String BUGZILLA_HAS_SCREENSHOT = "bugzillaHasScreenshot";
	private static String BUGZILLA_SCREENSHOT_PATH = "bugzillaScreenshotPath";
	private static String BUGZILLA_LOG_LEVEL= "bugzillaLogLevel";
	private static String BUGZILLA_TIME= "bugzillaTime";
	private Context mCtx;
	private SharedPreferences mPrivatePrefs;
	private Editor mPrivatePrefsEditor;

	public BugStorage(Context c) {
		this.mCtx = c;
		this.mPrivatePrefs = mCtx.getSharedPreferences(BUGZILLA_PREFERENCES, Context.MODE_PRIVATE);
		this.mPrivatePrefsEditor = mPrivatePrefs.edit();
	}

	public void setSummary(String summary) {
		mPrivatePrefsEditor.putString(BUGZILLA_SUMMARY, summary);
		mPrivatePrefsEditor.commit();
	}

	public void setDescription(String description) {
		mPrivatePrefsEditor.putString(BUGZILLA_DESCRIPTION, description);
		mPrivatePrefsEditor.commit();
	}

	public void setComponent(String component) {
		mPrivatePrefsEditor.putString(BUGZILLA_COMPONENT, component);
		mPrivatePrefsEditor.commit();
	}

	public void setBugType(String type) {
		mPrivatePrefsEditor.putString(BUGZILLA_TYPE, type);
		mPrivatePrefsEditor.commit();
	}

	public void setBugSeverity(String severity) {
		mPrivatePrefsEditor.putString(BUGZILLA_SEVERITY, severity);
		mPrivatePrefsEditor.commit();
	}

	public void setBugHasScreenshot(boolean screenshot) {
		if(!screenshot && hasValuesSaved() && getBugHasScreenshot())
			mPrivatePrefsEditor.remove(BUGZILLA_SCREENSHOT_PATH);
		mPrivatePrefsEditor.putBoolean(BUGZILLA_HAS_SCREENSHOT, screenshot);
		mPrivatePrefsEditor.commit();
	}

	public void setBugScreenshotPath(ArrayList<String> screenshots) {
		mPrivatePrefsEditor.putStringSet(BUGZILLA_SCREENSHOT_PATH, new HashSet<String>(screenshots));
		mPrivatePrefsEditor.commit();
	}


	public void setBugLogLevel(int iLevel) {
		mPrivatePrefsEditor.putInt(BUGZILLA_LOG_LEVEL, iLevel);
		mPrivatePrefsEditor.commit();
	}

	public void setBugTime(String time) {
		mPrivatePrefsEditor.putString(BUGZILLA_TIME, time);
		mPrivatePrefsEditor.commit();
	}

	public String getSummary() {
		return mPrivatePrefs.getString(BUGZILLA_SUMMARY, "");
	}

	public String getDescription() {
		return mPrivatePrefs.getString(BUGZILLA_DESCRIPTION, "");
	}

	public String getBugType() {
		return mPrivatePrefs.getString(BUGZILLA_TYPE, "");
	}

	public String getComponent() {
		return mPrivatePrefs.getString(BUGZILLA_COMPONENT, "");
	}

	public String getSeverity() {
		return mPrivatePrefs.getString(BUGZILLA_SEVERITY, "");
	}

	public boolean getBugHasScreenshot() {
		return mPrivatePrefs.getBoolean(BUGZILLA_HAS_SCREENSHOT, false);
	}

	public ArrayList<String> getScreenshotPath() {
		HashSet<String> screenshots = (HashSet<String>)mPrivatePrefs.getStringSet(BUGZILLA_SCREENSHOT_PATH, null);
		if(screenshots == null)
			return new ArrayList<String>();
		return new ArrayList<String>(screenshots);


	}

	public int getLogLevel() {
		return mPrivatePrefs.getInt(BUGZILLA_LOG_LEVEL, -1);
	}

	public String getTime() {
		return mPrivatePrefs.getString(BUGZILLA_TIME, "");
	}

	public boolean hasValuesSaved() {
		return mPrivatePrefs.contains(BUGZILLA_SUMMARY);
	}

	public void clearValues() {
		String component = getComponent();
		mPrivatePrefsEditor.clear();
		mPrivatePrefsEditor.commit();
		if (!component.equals(""))
			setComponent(component);
	}


}
