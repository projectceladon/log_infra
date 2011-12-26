package com.intel.crashreport;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class ApplicationPreferences {
	private static final String APP_PRIVATE_PREFS = "crashReportPrivatePreferences";
	private SharedPreferences appPrivatePrefs;
	private SharedPreferences appSharedPrefs;
	private Editor privatePrefsEditor;
	private Editor sharedPrefsEditor;
	private Context mCtx;

	public ApplicationPreferences(Context context) {
		this.mCtx = context;
		this.appPrivatePrefs = context.getSharedPreferences(APP_PRIVATE_PREFS, Context.MODE_PRIVATE);
		this.privatePrefsEditor = appPrivatePrefs.edit();
		this.appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		this.sharedPrefsEditor = appSharedPrefs.edit();
	}

	public int getUploadStateItem() {
		return appPrivatePrefs.getInt(mCtx.getString(R.string.upload_state_item_index), -1);
	}

	public void setUploadStateItem(int item) {
		privatePrefsEditor.putInt(mCtx.getString(R.string.upload_state_item_index), item);
		privatePrefsEditor.commit();
	}

	public void saveAlarmDate(long date) {
		privatePrefsEditor.putLong("alarmDate", date);
		privatePrefsEditor.commit();
	}

	public long getAlarmDate() {
		return appPrivatePrefs.getLong("alarmDate", 0);
	}

	public String getUploadState() {
		return appSharedPrefs.getString("uploadStatePref", "askForUpload");
	}

	public void setUploadStateToAsk() {
		sharedPrefsEditor.putString("uploadStatePref", "askForUpload");
		sharedPrefsEditor.commit();
	}

	public void setUploadStateToUpload() {
		sharedPrefsEditor.putString("uploadStatePref", "uploadImmediately");
		sharedPrefsEditor.commit();
	}

	public void setUploadStateToReport() {
		sharedPrefsEditor.putString("uploadStatePref", "uploadReported");
		sharedPrefsEditor.commit();
	}

	public void setUploadStateToDisable() {
		sharedPrefsEditor.putString("uploadStatePref", "uploadDisabled");
		sharedPrefsEditor.commit();
	}

	public void setUploadStateToNeverButNotify() {
		//TODO setUploadStateToNeverButNotify
		setUploadStateToAsk();
	}
}
