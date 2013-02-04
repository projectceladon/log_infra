package com.intel.phonemonitor;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.PhoneStateListener;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import android.telephony.gsm.GsmCellLocation;
import com.android.internal.telephony.OemTelephonyConstants;
import android.telephony.CellLocation;
import android.os.Message;
import android.os.Handler;
import android.os.AsyncResult;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * @author mohamed.zied.ben.hamouda@intel.com
 * Telephony Monitor class
 *
 */

public class TelephonyMonitor extends Monitor {

    private static final boolean DBG  = false;
    private static final boolean VDBG = false;
    private static final String tag = "TelephonyMonitor";

    public static final String strNewCellEntred   = "MOBILE_NEW_CELL_ENTERED";
    public static final String strCellUnchanged   = "MOBILE_CELL_UNCHANGED";
    public static final String strStatUpdate      = "MOBILE_STAT_UPDATE";
    public static final String strModemState      = "MODEM_STATE";
    public static final String strModemStats      = "MODEM_GLOBAL_STATS";
    public static final String strCellConfig      = "MOBILE_CELL_CONFIG";

    public static String metric_name;
    public static String metric_value;
    public static String metric_desc;

    private myPSL mypsl = null;
    private final int global_stat_period = 100;
    private int global_stat_iter;

    /**
     * Constructor.
     * @param: None
     */
    public TelephonyMonitor(){
    }

    @Override
    public void start(Context ctx, String out_file_name, boolean append){
        super.start(ctx, out_file_name, append);

        // Start Phone listener
        mypsl = new myPSL(ctx);
        global_stat_iter = 0;
    }

    /**
     * Method handling coming Intents
     *
     */
    @Override
    public void onReceive(Context context, Intent intent) {
    }

    public void collectMetrics(){

        // Return modem sate metrics
        metric_name = strStatUpdate;
        mypsl.getModemState();

        // Return Modem global stats
        metric_name = strStatUpdate;
        global_stat_iter++;
        if(global_stat_iter == global_stat_period){
            mypsl.getModemGlobalStats();
            global_stat_iter=0;
        }
    }

    public void stop(Context ctx) {
        super.stop(ctx);
    }


    /**
     * Phone State Listener class
     *
     */
    private class myPSL extends PhoneStateListener {

        private Phone mPhone = null;
        private GsmCellLocation [] previous_locations;
        private GsmCellLocation cloc = new GsmCellLocation();
        private Context context;
        private final int EVENT_RIL_OEM_HOOK_RAW_COMPLETE = 1300;
        private final int EVENT_RIL_OEM_HOOK_STRINGS_COMPLETE = 1310;
        private final int EVENT_UNSOL_RIL_OEM_HOOK_RAW = 500;
        private Message msg;
        private String[] data;
        private boolean stats_ongoing;
        private boolean state_ongoing;
        private int current_page;
        private int  [] page_nr;

        //private String metric_name;
        //private String metric_value;
        //private String metric_desc;

        public myPSL (Context ctxt ) {
            super();
            stats_ongoing = false;
            state_ongoing = false;
            current_page = 0;
            page_nr = new int[4];
            page_nr[0] = 7;
            page_nr[1] = 9;
            page_nr[2] = 10;
            page_nr[3] = 11;

            mPhone = PhoneFactory.getDefaultPhone();
            context = ctxt;

            previous_locations = new GsmCellLocation[4];
            for (int i=0; i<previous_locations.length; i++) {
                previous_locations[i] = new GsmCellLocation();
            }
        }

        public void onCellLocationChanged(CellLocation location) {

            // Cell location changed"
            GsmCellLocation nloc = (GsmCellLocation) location;

            while (stats_ongoing == true) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e){
                }
            }
            state_ongoing = true;

            // Case new cell is entered
            if (isNewCellEntered(nloc) == true){
                metric_name = strNewCellEntred;
                getCellConfig();

                // Case Cell unchanged
            } else {
                // Get Modem (MM,RR/RRC) state. No need to get Cell Config again
                metric_name = strCellUnchanged;
                getModemState();
            }
        }

        /*
         * Detect that a new cell is entered
         */
        private boolean isNewCellEntered(GsmCellLocation nloc) {
            if (DBG) {Log.d(tag, "lac: " + nloc.getLac() + " - Cid: " + nloc.getCid());}
            if ((nloc.getLac() == cloc.getLac()) && (nloc.getCid() == cloc.getCid())) {
                if (DBG) {Log.d(tag, "We stayed on the same cell");}
                return false;
            } else {
                for (int i=0; i<previous_locations.length; i++) {
                    if ((nloc.getLac() == previous_locations[i].getLac()) &&
                            (nloc.getCid() == previous_locations[i].getCid())) {
                        if (DBG) {Log.d(tag, "This is one on the last cells we have been on");}
                        return false;
                    }
                }
            }
            // This is a new cell, update last cells saved.
            for (int i=previous_locations.length - 1; i > 0; i--) {
                previous_locations[i] = previous_locations[i-1];
            }
            previous_locations[0] = nloc;
            cloc = nloc;

            return true;
        }

        /*
         * Return cell configuration
         */
        public void getCellConfig() {

            while (stats_ongoing == true) {
                try {
                    Thread.sleep(5000);
                    if (DBG) {Log.d(tag, "waiting 5 sec in getCellConfig");}
                } catch (InterruptedException e){
                    if (DBG) {Log.d(tag, "InterruptedException caught, could not sleep");}
                }
            }
            state_ongoing = true;

            // Get the cell config from Modem: AT+CGED=0
            data = new String[1];
            data[0] = "166";
            msg = mHandler.obtainMessage(EVENT_RIL_OEM_HOOK_STRINGS_COMPLETE);

            metric_value = strCellConfig;
            metric_desc = "AT+CGED=0 --> ";
            mPhone.invokeOemRilRequestStrings(data, msg);
            if (DBG) {Log.d(tag, "getCellConfig - AT+CGED=0");}
        }

        /*
         * // Get Modem (MM,RR/RRC) state.
         */
        public void getModemState() {

            while (stats_ongoing == true) {
                try {
                    Thread.sleep(5000);
                    if (DBG) {Log.d(tag, "waiting 5 sec in getModemState");}
                } catch (InterruptedException e){
                    if (DBG) {Log.d(tag, "InterruptedException caught, could not sleep");}
                }
            }
            state_ongoing = true;

            // Get Modem state: AT+XCGEDPAGE=0,1
            data = new String[3];
            data[0] = "167";
            data[1] = "0"; //mode = 0;
            data[2] = "1"; //page_nr = 1;

            msg = mHandler.obtainMessage(EVENT_RIL_OEM_HOOK_STRINGS_COMPLETE);
            // mPhone = PhoneFactory.getDefaultPhone();

            if (DBG) {
                if (mPhone == null) { if (DBG) {Log.d(tag, "mPhone == null");}}
            }
            metric_value = strModemState;
            metric_desc = "AT+XCGEDPAGE=0,1 --> ";
            mPhone.invokeOemRilRequestStrings(data, msg);
            if (DBG) {Log.d(tag, "getModemState - AT+XCGEDPAGE=0,1");}
        }


        public void getModemGlobalStats() {
            // Get Modem stats
            data = new String[3];
            data[0] = "167";
            data[1] = "0";
            metric_desc = " ";

            switch (page_nr[current_page]) {
            case 7:
                stats_ongoing = true;
                data[2] = "7";
                metric_value = strModemStats;
                metric_desc = "AT+XCGEDPAGE=0,7 --> ";
                msg = mHandler.obtainMessage(EVENT_RIL_OEM_HOOK_STRINGS_COMPLETE);
                mPhone.invokeOemRilRequestStrings(data, msg);
                current_page++;
                if (DBG) {Log.d(tag, "getModemStats - AT+XCGEDPAGE=0,7");}
                break;
            case 9:
                data[2] = "9";
                metric_value = strModemStats;
                metric_desc = "AT+XCGEDPAGE=0,9 --> ";
                msg = mHandler.obtainMessage(EVENT_RIL_OEM_HOOK_STRINGS_COMPLETE);
                mPhone.invokeOemRilRequestStrings(data, msg);
                current_page++;
                if (DBG) {Log.d(tag, "getModemState - AT+XCGEDPAGE=0,9");}
                break;
            case 10:
                data[2] = "10";
                metric_value = strModemStats;
                metric_desc = "AT+XCGEDPAGE=0,10 --> ";
                msg = mHandler.obtainMessage(EVENT_RIL_OEM_HOOK_STRINGS_COMPLETE);
                mPhone.invokeOemRilRequestStrings(data, msg);
                current_page++;
                if (DBG) {Log.d(tag, "getModemState - AT+XCGEDPAGE=0,10");}
                break;
            case 11:
                data[2] = "11";
                metric_value = strModemStats;
                metric_desc = "AT+XCGEDPAGE=0,11 --> ";
                msg = mHandler.obtainMessage(EVENT_RIL_OEM_HOOK_STRINGS_COMPLETE);
                mPhone.invokeOemRilRequestStrings(data, msg);
                current_page = 0;
                if (DBG) {Log.d(tag, "getModemState - AT+XCGEDPAGE=0,11");}
                if (DBG) {Log.d(tag, "getModemState - Done getting stats");}
                break;
            }
        }

        private Handler mHandler = new Handler() {
            public void handleMessage(Message msg) {
                AsyncResult ar;
                switch (msg.what) {
                case EVENT_RIL_OEM_HOOK_RAW_COMPLETE:
                    ar = (AsyncResult) msg.obj;
                    logRilOemHookRawResponse(ar);
                    break;
                case EVENT_RIL_OEM_HOOK_STRINGS_COMPLETE:
                    ar = (AsyncResult) msg.obj;
                    logRilOemHookStringsResponse(ar);
                    break;
                case EVENT_UNSOL_RIL_OEM_HOOK_RAW:
                    break;
                }
            }
        };

        /*
         * Received oem hook raw response
         */
        private void logRilOemHookRawResponse(AsyncResult ar) {
            if (DBG) {Log.d(tag, "received oem hook raw response");}
            String str = new String("");
            if (ar.exception != null) {
                if (DBG) {Log.d(tag, "Exception:" + ar.exception);}
                str += "Exception:" + ar.exception;
            }
            if (ar.result != null) {
                byte[] oemResponse = (byte[])ar.result;
                int size = oemResponse.length;

                if (DBG) {Log.d(tag, "oemResponse length=[" + Integer.toString(size) + "]");}
                str += "oemResponse length=[" + Integer.toString(size) + "]";

                if (size > 0) {
                    for (int i=0; i<size; i++) {
                        byte myByte = oemResponse[i];
                        int myInt = (int)(myByte & 0x000000FF);
                        if (DBG) {Log.d(tag, "oemResponse[" + Integer.toString(i) + "]=[0x" + Integer.toString(myInt,16) + "]");}
                        str += "oemResponse[" + Integer.toString(i) + "]=[0x" + Integer.toString(myInt,16) + "]";
                    }
                }
            } else {
                if (DBG) {Log.d(tag, "received NULL oem hook raw response");}
                str += "received NULL oem hook raw response";
            }

            // Write results in the output file
            metric_desc += str;
            flush(metric_name, metric_value, metric_desc);
        }

        private void logRilOemHookStringsResponse(AsyncResult ar) {
            if (DBG) {Log.d(tag, "received oem hook strings response");}

            String str = new String("");
            if (ar.exception != null) {
                if (DBG) {Log.d(tag, "Exception:" + ar.exception);}
                str += "Exception:" + ar.exception;
            }
            if (ar.result != null)  {
                String[] oemResponse = (String[])ar.result;
                int size = oemResponse.length;

                if (DBG) {Log.d(tag, "oemResponse length=[" + Integer.toString(size) + "]");}
                str += "oemResponse length=[" + Integer.toString(size) + "]";

                if (size > 0) {
                    for (int i=0; i<size; i++) {
                        if (DBG) {Log.d(tag, "oemResponse[" + Integer.toString(i) + "]=[" + oemResponse[i] + "]");}
                        str += "oemResponse[" + Integer.toString(i) + "]=[" + oemResponse[i] + "]";
                    }
                }

                // Need to get other stats
                if (state_ongoing == true) {
                    // We received the answer for state request.
                    state_ongoing = false;
                    if (DBG) {Log.d(tag, "retrieved answer for modem state");}
                    metric_desc += str;
                    flush(metric_name, metric_value, metric_desc);

                    // Do additional handling here
                } else if (stats_ongoing == true) {

                    // page_nr[current_page]
                    int prev_page = (current_page -1);
                    if (prev_page < 0) {
                        prev_page = page_nr.length - 1;
                    }
                    if (DBG) {Log.d(tag, "retrieved answer for modem stats page_nr" + page_nr[prev_page]);}

                    // Write stats for crashtool
                    metric_desc += "page_nr " + page_nr[prev_page] + " ";
                    metric_desc += str;
                    flush(metric_name, metric_value, metric_desc);

                    if (page_nr[prev_page] == page_nr[page_nr.length -1]) {
                        stats_ongoing = false;

                    } else {
                        // Get next page
                        getModemGlobalStats();
                    }
                }
            } else {
                if (DBG) {Log.d(tag, "received NULL oem hook strings response");}
                str += "received NULL oem hook strings response";
            }
        }
    }
}
