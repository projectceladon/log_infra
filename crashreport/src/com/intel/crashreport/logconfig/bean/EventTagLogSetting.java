
package com.intel.crashreport.logconfig.bean;

public class EventTagLogSetting implements LogSetting {

    public String tag;

    public String toString() {
        return new String("EventTag => " + tag);
    }

    public String getType() {
        return new String("EventTag");
    }

    public void setApplyValue(boolean b){};

}
