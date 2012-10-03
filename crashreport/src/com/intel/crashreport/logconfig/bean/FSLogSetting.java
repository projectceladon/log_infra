
package com.intel.crashreport.logconfig.bean;

public class FSLogSetting implements LogSetting {

    private String path;
    private String value;
    private Boolean append;

    public FSLogSetting() {
    }

    public FSLogSetting(String path, String value, Boolean append) {
        this.path = path;
        this.value = value;
        this.append = append;
    }

    public String toString() {
        return new String("File => Path: " + path + " Value: " +
                value + " Append: " + append);
    }

    public String getType() {
        return new String("FS");
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getAppend() {
        return append;
    }

    public void setAppend(Boolean append) {
        this.append = append;
    }

}
