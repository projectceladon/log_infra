package com.intel.crashreport.bugzilla.ui;

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
		mPrivatePrefsEditor.putBoolean(BUGZILLA_HAS_SCREENSHOT, screenshot);
		mPrivatePrefsEditor.commit();
	}

	public void setBugScreenshotPath(String screenshot) {
		mPrivatePrefsEditor.putString(BUGZILLA_SCREENSHOT_PATH, screenshot);
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

	public String getScreenshotPath() {
		return mPrivatePrefs.getString(BUGZILLA_SCREENSHOT_PATH, "");
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
