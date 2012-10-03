
package com.intel.crashreport.logconfig.bean;

public class EventTagLogSetting implements LogSetting {

    private String tag;

    public String toString() {
        return new String("EventTag => " + tag);
    }

    public String getType() {
        return new String("EventTag");
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

}
