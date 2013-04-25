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
