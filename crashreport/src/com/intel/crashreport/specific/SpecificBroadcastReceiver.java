package com.intel.crashreport.specific;

import android.content.Context;

import com.google.android.gcm.GCMBroadcastReceiver;

public class SpecificBroadcastReceiver extends GCMBroadcastReceiver {

    @Override
    protected String getGCMIntentServiceClassName(Context context)
    {
        return GCMIntentService.class.getName();
    }
}
