package com.intel.phonemonitor;

import java.io.PrintWriter;

import android.content.Context;

import android.os.Parcel;
import android.os.ServiceManager;
import android.os.RemoteException;
import android.os.BatteryManager;
import android.os.BatteryStats;

import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.app.IBatteryStats;

public class PowerMonitor extends Monitor {
    private BatteryStatsImpl mStats;
    private static final String TAG = "PowerMonitor";
    IBatteryStats mBatteryInfo;

    public PowerMonitor() {
    }

    public void start(Context context, String metricFileName, boolean append) {
        super.start(context, metricFileName, append);
        mBatteryInfo = IBatteryStats.Stub.asInterface(ServiceManager.getService("batteryinfo"));
    }

    public void stop(Context context) {
        super.stop(context);
    }

    /* We won't reinvent the wheel here, since Android is providing a very good power stat
       gathering API. */
    // Below code is shamelessly copied from Settings app
    private void refreshStats() {
        try {
            byte[] data = mBatteryInfo.getStatistics();
            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(data, 0, data.length);
            parcel.setDataPosition(0);
            mStats = com.android.internal.os.BatteryStatsImpl.CREATOR.createFromParcel(parcel);
            mStats.distributeWorkLocked(BatteryStats.STATS_SINCE_UNPLUGGED);
        } catch (RemoteException e) {
            android.util.Log.e(TAG, "RemoteException:", e);
        }
    }

    /* Since BatteryStatsImpl provides a dumper taking directly a PrintWriter as input
       we do not reuse the parent class flush method, but directly tap into the parent
       class PrintWriter. Yes, this is ugly. No we do not care. */
    public void collectMetrics() {
        PrintWriter pw = myOutputFilePrintWriter;
        refreshStats();
        String midPmuStates = Util.stringFromFile("/sys/kernel/debug/mid_pmu_states");
        String midPmuStats = Util.stringFromFile("/sys/kernel/debug/pmu_stats_log");
        synchronized(mLock) {
            flush("======================================", "", "");
            flush("BATTERY_STATS", "", "");
            flush("MID_PMU_STATES", "", "");
            flush("Content", "", midPmuStates);
            flush("MID_PMU_STATS", "", "");
            flush("Content", "", midPmuStats);
            mStats.dumpLocked(pw);
            myOutputFilePrintWriter.flush();
      }
    }
}
