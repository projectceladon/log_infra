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

import java.util.ArrayList;
import java.util.List;

public class LogConfig {

    private String name;
    private String description;
    private String type;
    private List<LogSetting> config;
    private List<LogSetting> rollBackConfig;
    /*LogConfig types values are :
     * - lock    : value is locked to ON value and can't be modified
     * - normal  : value is OFF at first boot and value updated by user is stored for next reboot
     * - default : value is ON at first boot and value updated by user is stored for next reboot*/
    public static String LOCK_TYPE = "lock";
    public static String NORMAL_TYPE = "normal";
    public static String DEFAULT_TYPE = "default";

    public LogConfig(String name) {
        this.name = name;
    }

    public void addSetting(LogSetting s) {
        if (config == null)
            config = new ArrayList<LogSetting>();
        config.add(s);
    }

    public String[] getSettingNames() {
        String settingNames[] = new String[config.size()];
        for (int i = 0; i < config.size(); i++)
            settingNames[i] = config.get(i).toString();
        return settingNames;
    }

    public String toString() {
        return name;
    }

    public Boolean isEmpty() {
        return (config == null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<LogSetting> getConfig() {
        return config;
    }

    public void setConfig(List<LogSetting> config) {
        this.config = config;
    }

    public List<LogSetting> getRollBackConfig() {
        return rollBackConfig;
    }

    public void setRollBackConfig(List<LogSetting> rollBackConfig) {
        this.rollBackConfig = rollBackConfig;
    }

    public boolean isAppliedByDefault() {
        return type.equals(DEFAULT_TYPE);
    }

    public boolean isLockConfig() {
        return type.equals(LOCK_TYPE);
    }

    public String getType() {
        return type;
    }

    public void setType(String t) {
        type = t;
    }

}
