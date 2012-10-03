
package com.intel.crashreport.logconfig;

import android.os.SystemProperties;
import android.util.Log;

import com.intel.crashreport.logconfig.LogConfigClient.CommandLogConfigAdapter;
import com.intel.crashreport.logconfig.bean.EventTagLogSetting;
import com.intel.crashreport.logconfig.bean.FSLogSetting;
import com.intel.crashreport.logconfig.bean.LogSetting;
import com.intel.crashreport.logconfig.bean.PropertyLogSetting;

public class ApplicatorLogSettingAdapter {

    private LogConfigClient mClient;

    public ApplicatorLogSettingAdapter(LogConfigClient client) {
        this.mClient = client;
    }

    public void apply(LogSetting s) throws Error {
        String fullName = s.getClass().getName();
        if (fullName.endsWith("FSLogSetting"))
            applyFSLogSetting((FSLogSetting) s);
        else if (fullName.endsWith("PropertyLogSetting"))
            applyPropertyLogSetting((PropertyLogSetting) s);
        else if (fullName.endsWith("EventTagLogSetting"))
            applyEventTagLogSetting((EventTagLogSetting) s);
        else
            throw new Error("Apply method not found for " + s);
    }

    private void applyEventTagLogSetting(EventTagLogSetting s) throws Error {
        Log.i("LogConfig", "Apply : " + s);
    }

    private void applyPropertyLogSetting(PropertyLogSetting s) throws Error {
        if(!s.valueToApply.equals(SystemProperties.get(s.name, s.rollBackValue))) {
            mClient.writeCommand(CommandLogConfigAdapter.CMD_SET_PROP, s);
            Log.i("LogConfig", "Apply : " + s);
        }
        else Log.i("LogConfig", s+" already set");
    }

    private void applyFSLogSetting(FSLogSetting s) throws Error {
        Log.i("LogConfig", "Apply : " + s);
        mClient.writeCommand(CommandLogConfigAdapter.CMD_WRITE_FILE, s);
    }

}
