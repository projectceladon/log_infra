package com.intel.phonemonitor;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PhoneDoctorListener extends BroadcastReceiver {
    private static final String START_ACTION = "com.intel.action.phonemonitor.START_MONITORING";
    private static final String STOP_ACTION = "com.intel.action.phonemonitor.STOP_MONITORING";

    @Override
    public void onReceive(Context context, Intent intent) {
        String a = intent.getAction();

        if (a != null) { // Should not occur - but who knows
            Intent mMonitorMasterService = new Intent(context, MonitorMasterService.class);
            mMonitorMasterService.putExtra(MonitorMasterService.EXTRA_ORIGINAL_ACTION, a);
            mMonitorMasterService.putExtras(intent);

            if (a.equals(START_ACTION)) {
                context.startService(mMonitorMasterService);
            }
            else if (a.equals(STOP_ACTION)) {
                context.stopService(mMonitorMasterService);
            }
        }
    }
}
