
package com.intel.crashreport.logconfig.bean;

public class PropertyLogSetting implements LogSetting {

    private String name;
    private String value;

    public PropertyLogSetting(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String toString() {
        return new String("Property => " + name + ":" + value);
    }

    public String getType() {
        return new String("Property");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
