package com.intel.phonemonitor;

import android.content.Intent;
import java.io.IOException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class PhoneActionsMonitor extends Monitor {

    private static final boolean DBG  = true;
    private static final boolean VDBG = true;
    private static final String tag = "PhoneActionsMonitor";

    public static final String strPhoneEvent          = "PHONE_EVENT";
    public static final String strAirplaneModeChanged = "AIRPLANE_MODE_CHANGED";
    public static final String strScreenStateChanged  = "SCREEN_STATE_CHANGED";
    public static final String strAirplaneModeON      = "AIRPLANE_MODE_ON";
    public static final String strAirplaneModeOFF     = "AIRPLANE_MODE_OFF";
    public static final String strScreenON            = "ACTION_SCREEN_ON";
    public static final String strScreenOFF           = "ACTION_SCREEN_OFF";

    /**
     * Constructor.
     * @param: None
     */
    public PhoneActionsMonitor() {
    }

    public void start(Context ctx, String out_file_name, boolean append){

        super.start(ctx, out_file_name, append);

        IntentFilter filter = new IntentFilter();

        // Set the filter
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);

        // Register the filter
        ctx.registerReceiver(this, filter);
    }

    public void stop(Context ctx) {
        super.stop(ctx);
        ctx.unregisterReceiver(this);
    }

    public void collectMetrics(){
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        // Case Intent=NETWORK_STATE_CHANGED_ACTION for WiFi
        if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction() )) {
            handleAirplaneModeChanged(context, intent);
        }

        else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())
                 || Intent.ACTION_SCREEN_OFF.equals(intent.getAction()) ){
            handleScreenStateChanged(context, intent);
        }
    }

    public void handleAirplaneModeChanged(Context context, Intent intent){
        if (!intent.getBooleanExtra("state", false)) {
            // Airplane mode is now OFF!
            flush(strPhoneEvent, strAirplaneModeChanged, strAirplaneModeOFF);;
        }
        else{
            // Airplane mode is now ON!
            flush(strPhoneEvent, strAirplaneModeChanged, strAirplaneModeON);
        }

    }

    public void handleScreenStateChanged(Context context, Intent intent){
        if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())){
            // display  is now ON!
            flush(strPhoneEvent, strScreenStateChanged, strScreenON);
        }
        else{
            // display  is now OFF!
            flush(strPhoneEvent, strScreenStateChanged, strScreenOFF);
        }
    }

}
