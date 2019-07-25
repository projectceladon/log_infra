/* Copyright (C) 2019 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    /**
     * Returns the settings to apply for the configStatus depending on its current
     * state.
     * @return the settings list to apply.
     * @throws IllegalStateException if ConfigStatus state is OFF.
     */
    public List<LogSetting> getSettingsToApply() throws IllegalStateException {
        if (mState == ConfigState.TO_ON || mState == ConfigState.LOCKED_ON)
            return getLogConfig().getConfig();
        else if (mState == ConfigState.TO_OFF)
            return getLogConfig().getRollBackConfig();
        else
            throw new IllegalStateException("ConfigStatus not in a good state : " + mState);
    }

    public enum ConfigState {
        OFF, TO_ON, ON, TO_OFF, LOCKED_ON
    }

}
