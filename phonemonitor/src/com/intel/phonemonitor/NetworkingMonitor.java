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
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.net.SocketException;

public class NetworkingMonitor extends Monitor {

    private static final boolean DBG  = true;
    private static final boolean VDBG = true;
    private static final String tag = "NetworkingMonitor";

    public static final String strStatUpdate                = "STATS_UPDATE";
    public static final String strTCPstats                  = "TCP";
    public static final String strUDPstats                  = "UDP";
    public static final String strWLANstats                 = "WLAN";
    public static final String strPhonestats                = "PHONE";
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

        if (networkInfo != null){
           flush(strConnectivityChange, networkInfo.getTypeName(), networkInfo.toString());
           collectMetrics();
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

    public synchronized void collectMetrics(){

        // TCP stats
        flush(strStatUpdate, strTCPstats, getTcpStats());

        // UDP stats
        flush(strStatUpdate, strUDPstats, getUdpStats());

        // WLAN stats
        flush(strStatUpdate, strWLANstats, getWlanStats());

        // Telephony interface stats
        flush(strStatUpdate, strPhonestats, getRmnetStats());

        // Netstats
        flush(strStatUpdate, strNetstats, getNetStats());

        // Network Interfaces
        flush(strStatUpdate, strNetstats, "ifconfg:" + getNetworkInterfaces());
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

    private String getRmnetStats() {

        String aStat = "NONE";
        BufferedReader bufferedReader = null;

        try {

        //Construct the BufferedReader object
            bufferedReader = new BufferedReader(new FileReader("/proc/net/dev"));
            aStat = grep("rmnet0:", bufferedReader);

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            //Close the BufferedReader
            try {
                if (bufferedReader != null) bufferedReader.close();
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

    public String getNetworkInterfaces() {
        String networkInterfaces = "None";
        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        if (interfaces != null){
            int i=0;
            while(interfaces.hasMoreElements()){

                NetworkInterface ifc=(NetworkInterface)interfaces.nextElement();

                boolean is_up = false;
                try{
                    is_up = ifc.isUp();
                } catch (SocketException e) {
                    continue;
                }

                // Check interface is up
                if(!is_up) continue;
                i++;
                if(i == 1){
                    networkInterfaces = ifc.toString();
                }
                else{
                    networkInterfaces += "," + ifc.toString();
                }
            }
        }
        return networkInterfaces;
    }
}
