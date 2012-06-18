
package com.intel.crashreport.logconfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Storage {

    private static final String CONFIG_PRIVATE_PREFS = "logconfigPrivatePreferences";
    private Context mCtx;
    private SharedPreferences mPrivatePrefs;
    private Editor mPrivatePrefsEditor;

    private static final String logconfig_peristent_list = "logconfig_peristent_list";
    private static final String logconfig_applied_list = "logconfig_applied_list";

    public Storage(Context c) {
        this.mCtx = c;
        this.mPrivatePrefs = mCtx.getSharedPreferences(CONFIG_PRIVATE_PREFS, Context.MODE_PRIVATE);
        this.mPrivatePrefsEditor = mPrivatePrefs.edit();
    }

    public void savePersistentConfigs(List<String> configNames) {
        mPrivatePrefsEditor
                .putStringSet(logconfig_peristent_list, new HashSet<String>(configNames));
        mPrivatePrefsEditor.commit();
    }

    public ArrayList<String> getPersistentConfigs() {
        HashSet<String> mSet = (HashSet<String>) mPrivatePrefs.getStringSet(
                logconfig_peristent_list, null);
        if (mSet != null)
            return new ArrayList<String>(mSet);
        else
            return new ArrayList<String>();
    }

    public void saveAppliedConfigs(List<String> configNames) {
        mPrivatePrefsEditor
                .putStringSet(logconfig_applied_list, new HashSet<String>(configNames));
        mPrivatePrefsEditor.commit();
    }

    public ArrayList<String> getAppliedConfigs() {
        HashSet<String> mSet = (HashSet<String>) mPrivatePrefs.getStringSet(logconfig_applied_list,
                null);
        if (mSet != null)
            return new ArrayList<String>(mSet);
        else
            return new ArrayList<String>();
    }

}
