
package com.intel.crashreport.logconfig.bean;

import java.util.List;

public class ConfigStatus {

    private LogConfig config = null;
    private ConfigState mState;
    private String name;
    private String description;

    public ConfigStatus(String name) {
        setState(ConfigState.OFF);
        this.name = name;
    }

    public ConfigStatus(String name, LogConfig config) {
        this(name);
        this.config = config;
    }

    public LogConfig getLogConfig() {
        return config;
    }

    public void setLogConfig(LogConfig c) {
        this.config = c;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String d) {
        description = d;
    }

    public String getDescription() {
        return description;
    }

    public ConfigState getState() {
        return mState;
    }

    public void setState(ConfigState mState) {
        this.mState = mState;
    }

    public void updateStateAfterApply() {
        if (mState == ConfigState.TO_ON)
            setState(ConfigState.ON);
        else if (mState == ConfigState.TO_OFF)
            setState(ConfigState.OFF);
    }

    public void updateStateAfterFail() {
        if (mState == ConfigState.TO_ON)
            setState(ConfigState.OFF);
        else if (mState == ConfigState.TO_OFF)
            setState(ConfigState.ON);
    }

    public List<LogSetting> getSettingsToApply() throws IllegalStateException {
        if (mState == ConfigState.TO_ON || mState == ConfigState.LOCKED_ON)
            return getLogConfig().getConfig();
        else if (mState == ConfigState.TO_OFF || mState == ConfigState.LOCKED_OFF)
            return getLogConfig().getRollBackConfig();
        else
            throw new IllegalStateException("ConfigStatus not in a good state : " + mState);
    }

    public enum ConfigState {
        OFF, TO_ON, ON, TO_OFF, LOCKED_ON, LOCKED_OFF
    }

}
