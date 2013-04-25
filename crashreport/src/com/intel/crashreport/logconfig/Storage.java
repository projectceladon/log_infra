
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
    /*logconfig state possibles values stored in shared preferences*/
    public enum StorableConfigState {ENABLED, DISABLED};

    private static final String ENABLED_LOGCONFIG_APPLIED_LIST = "logconfig_applied_list";
    private static final String DISABLED_LOGCONFIG_APPLIED_LIST = "disable_logconfig_applied_list";

    public Storage(Context c) {
        this.mCtx = c;
        this.mPrivatePrefs = mCtx.getSharedPreferences(CONFIG_PRIVATE_PREFS, Context.MODE_PRIVATE);
        this.mPrivatePrefsEditor = mPrivatePrefs.edit();
    }

    public void savePersistentConfigs(List<String> configNames) {
        mPrivatePrefsEditor
                .putStringSet(ENABLED_LOGCONFIG_APPLIED_LIST, new HashSet<String>(configNames));
        mPrivatePrefsEditor.commit();
    }

    /**
     * Saves the current config state in shared preferences to be re-applied later
     * @param enabledConfigNames is the list of config names at ON state
     * @param disabledConfigNames is the list of config names at OFF state
     */
    public void saveAppliedConfigs(List<String> enabledConfigNames, List<String> disabledConfigNames) {
        mPrivatePrefsEditor
                .putStringSet(ENABLED_LOGCONFIG_APPLIED_LIST, new HashSet<String>(enabledConfigNames));
        mPrivatePrefsEditor
                .putStringSet(DISABLED_LOGCONFIG_APPLIED_LIST, new HashSet<String>(disabledConfigNames));
        mPrivatePrefsEditor.commit();
    }

    /**
     * Retrieve from SharedPreferences the applied logconfigs list in the specified
     * input state (enabled or disabled)
     * @param state is state of the applied logconfig state to retrieve
     * @return the list of the applied logconfigs in the input state
     */
    public ArrayList<String> getAppliedConfigs(StorableConfigState state) {
        HashSet<String> mSet;
        switch(state){
        case ENABLED:
            mSet = (HashSet<String>) mPrivatePrefs.getStringSet(ENABLED_LOGCONFIG_APPLIED_LIST, null);
            break;
        default:
            mSet = (HashSet<String>) mPrivatePrefs.getStringSet(DISABLED_LOGCONFIG_APPLIED_LIST, null);
            break;
        }
        if (mSet != null)
            return new ArrayList<String>(mSet);
        else
            return new ArrayList<String>();
    }
}