package com.intel.phonemonitor;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class NetworkingMonitor extends Monitor {

    private static final boolean DBG  = true;
    private static final boolean VDBG = true;
    private static final String tag = "NetworkingMonitor";

    public static final String strStatUpdate                = "STATS_UPDATE";
    public static final String strTCPstats                  = "TCP";
    public static final String strUDPstats                  = "UDP";
    public static final String strWLANstats                 = "WLAN";
    public static final String strNetstats                  = "NetStat";
    public static final String strConnectivityChange        = "CONNECTIVITY_CHANGED";
    public static final String strConnectWiFiOn             = "WIFI_CONNECTIVITY_ON";
    public static final String strConnectWiFiOff            = "WIFI_CONNECTIVITY_OFF";
    public static final String strConnectMobileOn           = "MOBILE_CONNECTIVITY_ON";
    public static final String strConnectMobileOff          = "MOBILE_CONNECTIVITY_OFF";
    public static final String strWiFiNetworkStateChange    = "WIFI_NETWORK_STATE_CHANGED";

    /**
     * Constructor.
     * @param: None
     */
    public NetworkingMonitor() {
    }

    public void start(Context ctx, String out_file_name, boolean append){

        super.start(ctx, out_file_name, append);

        IntentFilter filter = new IntentFilter();

        // Set the filter
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        // Register the filter
        ctx.registerReceiver(this, filter);
    }

    public void stop(Context ctx) {
        super.stop(ctx);
        ctx.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        // Case Intent=NETWORK_STATE_CHANGED_ACTION for WiFi
        if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION )) {
            handleWiFiNetworkStateChanged(context, intent);

            // Case Intent=CONNECTIVITY_ACTION
        } else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION )) {
            handleConnectivityStateChanged(context, intent);
        }
    }

    public void handleConnectivityStateChanged(Context context, Intent intent){

        NetworkInfo networkInfo = (NetworkInfo)intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

        // Case the connection type is WiFi
        if(networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI){

            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

            if (noConnectivity) {

                // NOT_CONNECTED to WiFi;
                flush(strConnectivityChange, strConnectWiFiOff, networkInfo.toString());
            } else if (networkInfo.isConnected()){

                // CONNECTED to WiFi; Report stats
                flush(strConnectivityChange, strConnectWiFiOn, networkInfo.toString());
                collectMetrics();
            }
            // Case the connection type is Mobile
        } else if(networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

            if (noConnectivity) {

                // NOT connected to Mobile network
                flush(strConnectivityChange, strConnectMobileOff, networkInfo.toString());
            } else if (networkInfo.isConnected()){

                // Connected to Mobile network
                flush(strConnectivityChange, strConnectMobileOff, networkInfo.toString());
            }
        }
    }

    public void handleWiFiNetworkStateChanged(Context context, Intent intent) {
        String msg = "NO_STATE";
        String description = "NONE";

        NetworkInfo info = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

        // Check network detailed state
        NetworkInfo.DetailedState ds = info.getDetailedState();

        if(ds==NetworkInfo.DetailedState.AUTHENTICATING){
            msg = "AUTHENTICATING";
            description = getWiFiNetworkInfo(context);
        } else if(ds==NetworkInfo.DetailedState.BLOCKED){
            msg = "BLOCKED";
        } else if(ds==NetworkInfo.DetailedState.CONNECTED){
            msg = "CONNECTED";
            description = getWiFiNetworkInfo(context) + ", " + getWiFiDhcpInfo(context);
        } else if(ds==NetworkInfo.DetailedState.CONNECTING){
            msg = "CONNECTING";
            description = getWiFiNetworkInfo(context);
        } else if(ds==NetworkInfo.DetailedState.DISCONNECTED){
            msg = "DISCONNECTED";
            description = getWiFiNetworkInfo(context);
        } else if(ds==NetworkInfo.DetailedState.DISCONNECTING){
            msg = "DISCONNECTING";
            description = getWiFiNetworkInfo(context);
        } else if(ds==NetworkInfo.DetailedState.FAILED){
            msg = "FAILED";
        } else if(ds==NetworkInfo.DetailedState.IDLE){
            msg = "IDLE";
        } else if (ds==NetworkInfo.DetailedState.OBTAINING_IPADDR ){
            msg = "OBTAINING_IPADDR";
        } else if (ds==NetworkInfo.DetailedState.SCANNING ){
            msg = "SCANNING";
        } else if (ds==NetworkInfo.DetailedState.SUSPENDED ){
            msg = "SUSPENDED";
        } else if (ds==NetworkInfo.DetailedState.VERIFYING_POOR_LINK ){
            msg = "VERIFYING_POOR_LINK";
            description = getWiFiNetworkInfo(context);
        }

        flush(strWiFiNetworkStateChange, msg, description);
    }

    private String getWiFiNetworkInfo(Context context){

        String aAPInfo = " ";
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        // Check WiFi is enabled
        if (wifi.isWifiEnabled() == true ) {
            aAPInfo = wifi.getConnectionInfo().toString();
        }
        return aAPInfo;
    }

    public void collectMetrics(){

        // TCP stats
        flush(strStatUpdate, strTCPstats, getTcpStats());

        // UDP stats
        flush(strStatUpdate, strUDPstats, getUdpStats());

        // WLAN stats
        flush(strStatUpdate, strWLANstats, getWlanStats());

        // Netstats
        flush(strStatUpdate, strNetstats, getNetStats());
    }

    private String getTcpStats() {

        String aStat = "NONE";
        BufferedReader bufferedReader = null;

        try {

            //Construct the BufferedReader object
            bufferedReader = new BufferedReader(new FileReader("/proc/net/snmp"));
            aStat = grep("Tcp:", bufferedReader);


        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            //Close the BufferedReader
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return aStat;
    }

    private String getUdpStats() {

        String aStat = "NONE";
        BufferedReader bufferedReader = null;

        try {

            //Construct the BufferedReader object
            bufferedReader = new BufferedReader(new FileReader("/proc/net/snmp"));
            aStat = grep("Udp:", bufferedReader);


        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            //Close the BufferedReader
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return aStat;
    }

    private String getWlanStats() {

        String aStat = "NONE";
        BufferedReader bufferedReader = null;

        try {

            //Construct the BufferedReader object
            bufferedReader = new BufferedReader(new FileReader("/proc/net/dev"));
            aStat = grep("wlan0:", bufferedReader);


        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            //Close the BufferedReader
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return aStat;
    }

    private String getNetStats() {

        String aStat = "NONE";
        BufferedReader bufferedReader = null;

        try {

            //Construct the BufferedReader object
            bufferedReader = new BufferedReader(new FileReader("/proc/net/netstat"));
            aStat = grep("TcpExt:", bufferedReader);


        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            //Close the BufferedReader
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return aStat;
    }

    private String grep(String pattern, BufferedReader in) throws IOException {

        Pattern pat = Pattern.compile(pattern) ;
        String line = in.readLine() ;
        String output=" ";

        while (line != null) {
            Matcher m = pat.matcher(line) ;
            if (m.find()) {
                output = line;
            }
            line = in.readLine() ;
        }
        return output;
    }

    public String getWiFiDhcpInfo(Context context){

        String aDhcpInfo = " ";

        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        // Check WiFi is enabled
        if (wifi.isWifiEnabled() == true ) {
            aDhcpInfo = wifi.getDhcpInfo().toString();
        }
        return aDhcpInfo;
    }
}
