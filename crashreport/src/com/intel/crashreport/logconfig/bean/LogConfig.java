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
