package com.intel.phonemonitor;

import java.io.File;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class ThermalMonitor extends Monitor {
    private static final String ACTION_THERMAL_ZONE_STATE_CHANGED =
            "com.android.server.thermal.action.THERMAL_ZONE_STATE_CHANGED";
    public static final String EXTRA_ZONE = "zone";
    public static final String EXTRA_STATE = "state";
    public static final String EXTRA_EVENT = "event";
    public static final String EXTRA_TEMP = "temp";
    public static final String EXTRA_NAME = "name";

    public static final String STATE_NAMES[] = {"OFF", "NORMAL", "WARNING", "ALERT", "CRITICAL"};

    private static final int MAX_ZONE_SYSFS_ENTRIES = 16;

    @Override
    public void start(Context ctx, String out_file_name, boolean append){
        super.start(ctx, out_file_name, append);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_THERMAL_ZONE_STATE_CHANGED);

        ctx.registerReceiver(this, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (ACTION_THERMAL_ZONE_STATE_CHANGED.equals(action)) {
            handleThermalStateChanged(intent);
        }
    }

    /*
     * code snippet from com/android/server/thermal/ThermalZone.java
     **/

    private String getStateAsString(int index) {
        if (index < -1 || index > 3)
           return "Invalid";
        index++;
        return STATE_NAMES[index];
    }

    private String getEventTypeAsString(int type) {
        return type == 0 ? "LOW" : "HIGH";
    }

    private void handleThermalStateChanged(Intent intent) {
        // Code shamelessly copied from framework/base/core/android/thermal/ThermalCoolingMAnager.java
        String zoneName = intent.getStringExtra(EXTRA_NAME);
        int thermZone = intent.getIntExtra(EXTRA_ZONE, 0);
        int thermState = intent.getIntExtra(EXTRA_STATE, 0);
        int thermEvent = intent.getIntExtra(EXTRA_EVENT, 0);
        int zoneTemp = intent.getIntExtra(EXTRA_TEMP, 0);

        String msg = zoneName  + "," +
                     thermZone + "," +
                     zoneTemp  + "," +
                     getStateAsString(thermState) + "," +
                     getEventTypeAsString(thermEvent);

        flush("THERMAL_EVENT", msg, "");
        collectMetrics();
    }

    public synchronized void collectMetrics(){
        final String sysfsZoneRoot = "/sys/class/thermal/thermal_zone";
        for (int i = 0; i < MAX_ZONE_SYSFS_ENTRIES; i++) {
            String sysfsZoneRootDir = sysfsZoneRoot + i;

            File f = new File(sysfsZoneRootDir);
            if (!f.isDirectory()) {
                continue;
            }

            String temp = Util.stringFromFile(sysfsZoneRootDir + "/temp");
            String type = Util.stringFromFile(sysfsZoneRootDir + "/type");
            flush("ZONE"+i, "", "temp:" + temp + ",type:" + type);
        }

        final String sysfsCoolRoot = "/sys/class/thermal/cooling_device";
        for (int i = 0; i < MAX_ZONE_SYSFS_ENTRIES; i++) {
            String sysfsCoolRootDir = sysfsCoolRoot + i;

            File f = new File(sysfsCoolRootDir);
            if (!f.isDirectory()) {
                continue;
            }

            String state = Util.stringFromFile(sysfsCoolRootDir + "/cur_state");
            String type = Util.stringFromFile(sysfsCoolRootDir + "/type");
            flush("COOLING_DEVICE"+i, "", "state:" + state + ",type:" + type);
        }


        final String sysfsDTSTempRoot = "/sys/devices/platform/coretemp.0";
        for (int i = 0; i < MAX_ZONE_SYSFS_ENTRIES; i++) {
            String sysfsDTSTempFile = sysfsDTSTempRoot + "/temp" + i + "_input";

            File f = new File(sysfsDTSTempFile);
            if (!f.exists()) {
                continue;
            }

            String temp = Util.stringFromFile(sysfsDTSTempFile);
            flush("DTS_INPUT"+i, "", "temp:"+temp);
        }
    }

    public void stop(Context ctx) {
        super.stop(ctx);
        ctx.unregisterReceiver(this);
    }
}
