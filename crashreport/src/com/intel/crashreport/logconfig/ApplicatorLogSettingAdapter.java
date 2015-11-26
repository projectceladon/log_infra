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

import android.util.Log;

import com.intel.crashreport.logconfig.LogConfigClient.CommandLogConfigAdapter;
import com.intel.crashreport.logconfig.bean.EventTagLogSetting;
import com.intel.crashreport.logconfig.bean.FSLogSetting;
import com.intel.crashreport.logconfig.bean.IntentLogSetting;
import com.intel.crashreport.logconfig.bean.LogSetting;
import com.intel.crashreport.logconfig.bean.PropertyLogSetting;

public class ApplicatorLogSettingAdapter {

    private LogConfigClient mClient;

    public ApplicatorLogSettingAdapter(LogConfigClient client) {
        this.mClient = client;
    }

    public void apply(LogSetting s) throws IllegalStateException {
        String fullName = s.getClass().getName();
        if (fullName.endsWith("FSLogSetting"))
            applyFSLogSetting((FSLogSetting) s);
        else if (fullName.endsWith("PropertyLogSetting"))
            applyPropertyLogSetting((PropertyLogSetting) s);
        else if (fullName.endsWith("EventTagLogSetting"))
            applyEventTagLogSetting((EventTagLogSetting) s);
        else if (fullName.endsWith("IntentLogSetting"))
            applyIntentLogSetting((IntentLogSetting) s);
        else
            throw new Error("Apply method not found for " + s);
    }

    private void applyIntentLogSetting(IntentLogSetting s) throws IllegalStateException {
        Log.i("LogConfig", "Apply : " + s);
        mClient.sendIntent(s);
    }

    private void applyEventTagLogSetting(EventTagLogSetting s) throws IllegalStateException {
        Log.w("LogConfig", "EventTagLogSetting not implemented : " + s);
    }

    private void applyPropertyLogSetting(PropertyLogSetting s) throws IllegalStateException {
        String value = s.getValue();
        String key = s.getName();
        if (value != null && key != null) {
            Log.i("LogConfig", "Apply : " + s);
            mClient.writeCommand(CommandLogConfigAdapter.CMD_SET_PROP, s);
        }
    }

    private void applyFSLogSetting(FSLogSetting s) throws IllegalStateException {
        Log.i("LogConfig", "Apply : " + s);
        mClient.writeCommand(CommandLogConfigAdapter.CMD_WRITE_FILE, s);
    }

}
