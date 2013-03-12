package com.intel.phonemonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.PrintWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author mohamed.zied.ben.hamouda@intel.com
 * Pattern for a Monitor engine
 *
 */
public abstract class Monitor extends BroadcastReceiver {

    private static final boolean DBG  = true;
    private static final boolean VDBG = true;

    private static final int metricNameLength           = 32;
    private static final int metricValueLength          = 32;
    private static final int metricDescriptionLength    = 512;
    private static final String SeparatorTag            = "\t";

    private String mMonitorFileName;

    /** Max number of metric's Items to maintain in the memory*/
    private final int MAX_FLUSH_ITEMS_COUNT = 10;

    /** Current number of metric's Items to maintain in the memory*/
    private int nbr_metric_items_to_flush;

    /** Output file */
    protected PrintWriter myOutputFilePrintWriter = null;

    protected Object mLock = new Object();

    /**
     * Constructor.
     * @param: None
     */
    public Monitor(){
    }

    /**
     * Starts the monitoring engine.
     * @param fileName: the output file name
     */
    public void start(Context ctx, String fName, boolean append) {

        nbr_metric_items_to_flush = 0;
        mMonitorFileName = fName;

        // Open the output file
        try {
            myOutputFilePrintWriter = new PrintWriter(new FileWriter(mMonitorFileName, append));
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Stops the monitoring engine.
     * @param
     */
    public void stop(Context ctx) {
        synchronized(mLock) {
            myOutputFilePrintWriter.flush();
            myOutputFilePrintWriter.close();
            myOutputFilePrintWriter = null;
        }
    }

    /**
     * Remove metric files and recreate it
     * @param
     */
    public void resetMetrics() {
        synchronized(mLock) {
            nbr_metric_items_to_flush = 0;

            if (myOutputFilePrintWriter != null) { // Monitor already stopped ?
                myOutputFilePrintWriter.flush();
                myOutputFilePrintWriter.close();
                // Recreate the output file
                try {
                    myOutputFilePrintWriter = new PrintWriter(new FileWriter(mMonitorFileName));
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Main method that will collect metrics.
     * @param
     */
    public abstract void collectMetrics();

    /**
     * Flush the collected metrics in the output file.
     * @param metricName: metric name, metricValue: metric value, metricDesc: metric description
     */
    public void flush(String metricName, String metricValue, String metricDesc) {
        // Format output and write it in a CSV file
        String aOutput = getCurrentTimeStamp() + SeparatorTag
             + String.format("%1$-" + metricNameLength + "s", metricName) + SeparatorTag
             + String.format("%1$-" + metricValueLength + "s", metricValue) + SeparatorTag
             + String.format("%1$-" + metricDescriptionLength + "s", metricDesc);

        synchronized(mLock) {
            nbr_metric_items_to_flush++;

            if (myOutputFilePrintWriter != null) { // Did someone stop the monitor behind our back ?
                myOutputFilePrintWriter.println(aOutput);

                if (nbr_metric_items_to_flush == MAX_FLUSH_ITEMS_COUNT) {
                    nbr_metric_items_to_flush = 1;
                    myOutputFilePrintWriter.flush();
                }
            }
        }
    }

    public void forceFlush() {
        synchronized(mLock) {
            nbr_metric_items_to_flush = 1;
            myOutputFilePrintWriter.flush();
        }
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:ms");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public void onReceive(Context context, Intent intent) {
    }
}
