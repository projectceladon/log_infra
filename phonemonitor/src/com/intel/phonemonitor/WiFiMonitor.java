package com.intel.phonemonitor;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.content.IntentFilter;
import java.util.List;

/**
 * @author mohamed.zied.ben.hamouda@intel.com
 * WiFi Monitor class
 *
 */

public class WiFiMonitor extends Monitor {

    private static final boolean DBG  = true;
    private static final boolean VDBG = true;
    private static final String tag = "WiFiMonitor";

    public static final String strWifiStateEvent            = "WIFI_STATE_CHANGED";
    public static final String strWifiAPStateEvent          = "WIFI_AP_STATE_CHANGED";
    public static final String strSupplicantStateEvent      = "SUPPLICANT_STATE_CHANGED";
    public static final String strSupplicantConnectEvent    = "SUPPLICANT_CONNECT_CHANGED";
    public static final String strRSSIEvent                 = "RSSI_CHANGED";
    public static final String strScanEvent                 = "SCAN_EVENT";
    public static final String strWiFiAvailability          = "WIFI_AVAILABILITY";
    /**
     * Constructor.
     * @param: None
     */
    public WiFiMonitor(){
    }

    @Override
    public void start(Context ctx, String out_file_name, boolean append){
        super.start(ctx, out_file_name, append);

        IntentFilter filter = new IntentFilter();

        // Set the filter
        filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        // Register the filter
        ctx.registerReceiver(this, filter);
    }

    /**
     * Method handling Intents coming from WifiService
     * android.intent.action.WIFI_STATE_CHANGED, SUPPLICANT_STATE_CHANGED_ACTION, SUPPLICANT_CONNECTION_CHANGE_ACTION,
     * RSSI_CHANGED_ACTION
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // Case Intent=WIFI_STATE_CHANGED_ACTION
        if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
            handleWifiStateChanged(intent);
        }
        // Case Intent=SUPPLICANT_STATE_CHANGED_ACTION
        else if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
            handleSupplicantStateChanged(
            context,
            (SupplicantState) intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE),
            intent.hasExtra(WifiManager.EXTRA_SUPPLICANT_ERROR),
            intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 0));

        // Case Intent=SUPPLICANT_CONNECTION_CHANGE_ACTION
        } else if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
            handleSupplicantConnectionChanged(
            intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false));

      // Case of RSSI_CHANGED_ACTION
        } else if (action.equals(WifiManager.RSSI_CHANGED_ACTION)){
            handleSignalChanged(intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, 0));
        }
        // Case scan results available
        else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            handleScanResultsAvailable(context);
        }
        else if (WifiManager.WIFI_AP_STATE_CHANGED_ACTION.equals(action)) {
            handleWifiApStateChanged(intent);
        }
    }

    private void handleWifiStateChanged(Intent intent) {

        int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,-1);
        String msg = "NO_STATE";
        String description = "NONE";

        switch(state){
            case WifiManager.WIFI_STATE_DISABLED:
                msg = "WIFI_STATE_DISABLED";
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                msg = "WIFI_STATE_ENABLED";
                break;
            case WifiManager.WIFI_STATE_DISABLING:
                msg = "WIFI_STATE_DISABLING";
                break;
            case WifiManager.WIFI_STATE_ENABLING:
                msg = "WIFI_STATE_ENABLING";
                break;
            case WifiManager.WIFI_STATE_UNKNOWN:
                msg = "WIFI_STATE_UNKNOWN";
                break;
        }

        // Report Event
        flush(strWifiStateEvent, msg, description);
    }

    private void handleWifiApStateChanged(Intent intent) {

        int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_AP_STATE,
                                       WifiManager.WIFI_AP_STATE_FAILED);
        String msg = "NO_STATE";
        String description = "NONE";

        switch(state){
            case WifiManager.WIFI_AP_STATE_DISABLED:
                msg = "WIFI_AP_STATE_DISABLED";
                break;
            case WifiManager.WIFI_AP_STATE_ENABLED:
                msg = "WIFI_AP_STATE_ENABLED";
                break;
            case WifiManager.WIFI_AP_STATE_DISABLING:
                msg = "WIFI_AP_STATE_DISABLING";
                break;
            case WifiManager.WIFI_AP_STATE_ENABLING:
                msg = "WIFI_AP_STATE_ENABLING";
                break;
            default:
                msg = "WIFI_STATE_UNKNOWN";
                break;
        }

        flush(strWifiAPStateEvent, msg, description);
    }


    private void handleSupplicantStateChanged(Context context, SupplicantState supplicantState, boolean hasError, int error) {
        String msg = null;
        String description = "NONE";

        if (hasError) {
            msg="ERROR AUTHENTICATING";
        } else {
            if (SupplicantState.ASSOCIATED.equals(supplicantState)) {
                msg = "ASSOCIATED";
                description = getAPInfo(context);
            } else if(SupplicantState.ASSOCIATING.equals(supplicantState)) {
                msg = "ASSOCIATING";
                description = getAPInfo(context);
            } else if(SupplicantState.COMPLETED.equals(supplicantState)) {
                msg = "COMPLETED";
                description = getAPInfo(context);
            } else if(SupplicantState.AUTHENTICATING.equals(supplicantState)) {
                msg = "AUTHENTICATING";
                description = getAPInfo(context);
            } else if(SupplicantState.DISCONNECTED.equals(supplicantState)) {
                msg = "DISCONNECTED";
                description = getAPInfo(context);
            } else if(SupplicantState.DORMANT.equals(supplicantState)) {
                msg = "DORMANT";
            } else if(SupplicantState.FOUR_WAY_HANDSHAKE.equals(supplicantState)) {
                msg = "FOUR WAY HANDSHAKE";
            } else if(SupplicantState.GROUP_HANDSHAKE.equals(supplicantState)) {
                msg = "GROUP HANDSHAKE";
            /*} else if(SupplicantState.INACTIVE.equals(supplicantState)) {
                  msg = "INACTIVE";
              */
            } else if(SupplicantState.INVALID.equals(supplicantState)) {
                msg = "INVALID";
            } else if(SupplicantState.UNINITIALIZED.equals(supplicantState)) {
                msg = "UNINITIALIZED";
            } else if(SupplicantState.INTERFACE_DISABLED.equals(supplicantState)){
                msg = "INTERFACE_DISABLED";
            }
        }

        // Report new Event
        if (msg != null){
            flush(strSupplicantStateEvent, msg, description);
        }
    }

    private void handleSignalChanged(int rssi) {
        // Report Event
        flush(strRSSIEvent, Integer.toString(rssi), "NONE");
    }

    private void handleSupplicantConnectionChanged(boolean connected) {
        String msg = null;
        String description = "NONE";

        if (connected) {
            msg = "SUPPLICANT_CONNECTED";
        } else {
            msg = "SUPPLICANT_DISCONNECTED";
        }

        // Report Event
        flush(strSupplicantConnectEvent, msg, description);
    }

    private String getAPInfo(Context context){

        String aAPInfo = " ";
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        // Check WiFi is enabled
        if (wifi.isWifiEnabled() == true ) {
            aAPInfo = wifi.getConnectionInfo().toString();
        }
        return aAPInfo;
    }

    private void handleScanResultsAvailable (Context context) {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        // Check if we are already connected to an AP
        try {
            if (wifiMgr.getConnectionInfo().getNetworkId() != -1) {
                return;
            }
        /* Very rarely the above call causes an exception in os.Parcel.read, when
           trying to retrieve connection info. The issue is likely in the WifiManagerService
           implementation, but we do not want our monitor to crash because of this
           However, still print the stack trace for further debug */
        } catch (NullPointerException e) {
                e.printStackTrace();
                return;
        }

        // Check if we are already conncted to an Access
        // To do that, we check the configs against the scanned APs
        final List<WifiConfiguration> WifiConfigs = wifiMgr.getConfiguredNetworks();

        if(WifiConfigs != null){
            for(WifiConfiguration config: WifiConfigs){

                // Check config is NOT Null
                if (config == null) continue;

                if(config.status != WifiConfiguration.Status.ENABLED) {
                   continue;
                }

                // Check whether we should connect to the AP
                final List<ScanResult> wifiList = wifiMgr.getScanResults();

                if(wifiList != null){
                    for (ScanResult scanR: wifiList){

                        // Check scanR is NOT Null
                        if(scanR == null) continue;

                        // if BSSID is specified, check BSSID
                        if(config.BSSID != null){
                            if(scanR.BSSID != null
                                && !scanR.BSSID.isEmpty()
                                && !config.BSSID.isEmpty()){
                                if(scanR.BSSID.equals(config.BSSID)){
                                    flush(strScanEvent, strWiFiAvailability, "WIFI_CONNECTION_REQUESTED_TO_BSSID: [" + config.SSID + "-" + config.BSSID + "]");
                                }
                            }
                        }
                        // Else we check SSID
                        else{
                            if(scanR.SSID != null
                                && config.SSID != null
                                && !scanR.SSID.isEmpty()
                                && !config.SSID.isEmpty()){
                                String scanSSID = "\""+scanR.SSID + "\"";
                                if(scanSSID.equals(config.SSID)){
                                    flush(strScanEvent, strWiFiAvailability, "WIFI_CONNECTION_REQUESTED_TO_SSID: [" + scanR.SSID + "-" + scanR.BSSID + "]");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void collectMetrics(){
    }

    public void stop(Context ctx) {
        super.stop(ctx);
        ctx.unregisterReceiver(this);
    }
}
