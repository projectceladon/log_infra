
package com.intel.crashreport.logconfig.bean;

import java.util.ArrayList;

public class LogConfig {

    private String name;
    private String description;
    public ArrayList<LogSetting> config;

    public LogConfig(String name) {
        this.name = name;
    }

    public void addSetting(LogSetting s) {
        if (config == null)
            config = new ArrayList<LogSetting>();
        config.add(s);
    }

    public String[] getSettings() {
        String settingsName[] = new String[config.size()];
        int i = 0;
        for (LogSetting setting : config) {
            settingsName[i] = setting.toString();
            i++;
        }
        return settingsName;
    }

    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isEmpty() {
        return (config == null);
    }

    public void setApplyValue(boolean enabled){
        for( LogSetting setting:config) {
            setting.setApplyValue(enabled);
        }
    }

}
