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
