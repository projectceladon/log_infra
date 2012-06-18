
package com.intel.crashreport.logconfig.bean;

public class FSLogSetting implements LogSetting {

    public String path;
    public String value;
    public Boolean append;
    public String valueToApply;
    public String rollBackValue;

    public FSLogSetting() {
    }

    public FSLogSetting(String path, String value, Boolean append, String roll) {
        this.path = path;
        this.value = value;
        this.append = append;
        rollBackValue = roll;
        valueToApply = rollBackValue;
    }

    public String toString() {
        return new String("File => Path: " + path + " Value: " +
                value + " Append: " + append);
    }

    public String getType() {
        return new String("FS");
    }

    public void setApplyValue(boolean enabled) {
        if(enabled)
            valueToApply = value;
        else valueToApply = rollBackValue;
    }

}
