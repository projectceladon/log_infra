
package com.intel.crashreport.logconfig.bean;

public class ConfigStatus {

    private LogConfig config = null;
    private Boolean applied;
    private Boolean persistent;
    private String name;
    private String description;

    public ConfigStatus(String name) {
        applied = false;
        persistent = false;
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

    public Boolean isPersistent() {
        return persistent;
    }

    public void setPersistent(Boolean b) {
        this.persistent = b;
    }

    public Boolean isApplied() {
        return applied;
    }

    public void setApplied(Boolean b) {
        this.applied = b;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String d) {
        description = d;
    }

    public String getDescription(){
        return description;
    }

}
