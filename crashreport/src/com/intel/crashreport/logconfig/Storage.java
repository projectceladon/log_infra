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
