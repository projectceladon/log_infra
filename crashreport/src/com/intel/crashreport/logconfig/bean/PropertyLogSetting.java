
package com.intel.crashreport.logconfig.bean;

public class PropertyLogSetting implements LogSetting {

    public String name;
    public String value;
    public String rollBackValue;
    public String valueToApply;

    public PropertyLogSetting(String name, String value, String roll) {
        this.name = name;
        this.value = value;
        rollBackValue = roll;
        valueToApply = rollBackValue;
    }

    public String toString() {
        return new String("Property => " + name + ":" + value);
    }

    public String getType() {
        return new String("Property");
    }

    public void setApplyValue(boolean enabled) {
        if(enabled)
            valueToApply = value;
        else valueToApply = rollBackValue;
    }

}
