package com.intel.phonemonitor;

import java.io.File;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;

public class MonitorMasterService extends Service {
    private static int uploadIntervalSeconds = 24 * 60 * 60 - 13;
    private static final String STATDIR = "/logs/stats";
    private static final String TAG = "PhoneMonitorMaster";
    private static final String PD_PACKAGE = "phonemonitor";
    private static final String UPLOAD_ACTION = "com.intel.phonemonitor.UPLOAD_METRICS";
    private static final String NEXT_MONITOR_ACTION = "com.intel.phonemonitor.NEXT_MONITOR";
    private static final String ZIP_SUFFIX = "_data.zip";
    public  static final String EXTRA_ORIGINAL_ACTION = "com.intel.phonemonitor.EXTRA_ORIGINAL_ACTION";
    private static Object mLock = new Object();

    private MasterReceiver mMasterReceiver;
    private MonitorList nextMonitorList;
    private ArrayList<LocalMonitor> fullMonitorList;
    private String cacheDir;
    private String zipFileName;
    private String triggerFileName;
    private String needUploadFileName;
    private long initialTime_us;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMasterReceiver = new MasterReceiver();
        cacheDir = getCacheDir().getAbsolutePath();
        zipFileName = cacheDir + "/" + PD_PACKAGE + ZIP_SUFFIX;
        needUploadFileName = cacheDir + "/.needupload";
        triggerFileName = STATDIR + "/" + PD_PACKAGE + "_trigger";
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        boolean softStart = true; // true if we are not restarted after being killed by LMK

        if (createMonitorList()) {
            if (intent == null) softStart = false; // Restarted by the system !

            initialTime_us = SystemClock.elapsedRealtime();

            IntentFilter filter = new IntentFilter();
            filter.addAction(UPLOAD_ACTION);
            filter.addAction(NEXT_MONITOR_ACTION);
            filter.addAction(Intent.ACTION_SHUTDOWN);
            registerReceiver(mMasterReceiver, filter);

            setUploadAlarm(softStart);
            cleanupCache(softStart);
            startMonitors(softStart);

            return START_STICKY;
        }
        this.stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopMonitors(); // Should flush all the monitor metrics
        uploadToCrashtool(false);
        cancelUploadAlarm();
        // If killed by LMK - the receiver will be automatically destroyed.
        unregisterReceiver(mMasterReceiver);
    }

    private boolean isAllMonitorsEmpty() {
        File f;

        for (LocalMonitor m :fullMonitorList) {
            if (m.enabled) {
                String fname = cacheDir + "/" + m.metricFile;
                f = new File(fname);
                if (f.length() > 0)
                    return false; // If f does not exists f.length() == 0
            }
        }
        return true;
   }

    private void cleanupCache(boolean cleanMetrics) {
        File f;

        if (cleanMetrics) { // First time we are started
            f = new File(needUploadFileName);
            if (f.exists()) {
                Log.d(TAG, "Detected a pending upload upon startup. Uploading.");
                uploadToCrashtool(true);
                f.delete();
            }

            Log.d(TAG, "Cleaning metrics");
            for (LocalMonitor m :fullMonitorList) {
                String fname = cacheDir + "/" + m.metricFile;
                f = new File(fname);
                if (f.exists()) {
                    f.delete();
                }
            }
        }

        f = new File(triggerFileName);
        if (f.exists()) {
            f.delete();
        }

        f = new File(zipFileName);
        if (f.exists()) {
            f.delete();
        }
    }

    private boolean createMonitorList() {
        // TODO: intialize all those values either from a HashMap belonging to a separate class
        // or (better) from a file  - which will anyway need a HashMap to match monitor name and class
        fullMonitorList = new ArrayList<LocalMonitor>();

        fullMonitorList.add(new LocalMonitor("Network",
                                             15 * 60 + 1,
                                             new NetworkingMonitor(),
                                             true,
                                             "networkmonitor.txt"));
        fullMonitorList.add(new LocalMonitor("Wifi",
                                             0,
                                             new WiFiMonitor(),
                                             true,
                                             "wifimonitor.txt"));
        fullMonitorList.add(new LocalMonitor("Power",
                                             60 * 60 - 3,
                                             new PowerMonitor(),
                                             true,
                                             "powermonitor.txt"));
/*        fullMonitorList.add(new LocalMonitor("Telephony",
                                             6 * 60,
                                             new TelephonyMonitor(),
                                             true,
                                             "telephonymonitor.txt"));
*/

        if (fullMonitorList.isEmpty()) {
            Log.e(TAG, "Monitor list is empty. Refusing to start.");
            return false;
        }
        return true;
    }

    private void startMonitors(boolean cleanMetrics) {
        // TODO: set first metrics upload alarm here
        Context ctx = getApplicationContext();

        for (LocalMonitor m :fullMonitorList) {
            if (m.enabled) {
                m.monitor.start(ctx, cacheDir + "/" + m.metricFile, !cleanMetrics);
                Log.d(TAG, "Monitor " + m.name + " started.");
            }
        }
        nextMonitorList = setNextMetricAlarm();
    }

    private void stopMonitors() {
        Context ctx = getApplicationContext();

        for (LocalMonitor m :fullMonitorList) {
            if (m.enabled) {
                m.monitor.stop(ctx);
                Log.d(TAG, "Monitor " + m.name + " stopped.");
            }
        }
    }

    /* Iterate through monitor list and find the closest ones in time */
    private MonitorList getNextMonitors(long timeSeconds) {
        MonitorList lm = new MonitorList(new ArrayList<LocalMonitor>(), 0);
        long minNextSec = 0;

        Log.d(TAG, "Checking for next stat gathering");
        for (LocalMonitor m :fullMonitorList) {
            if (m.periodSec != 0) {
                long nextEventSecs = m.periodSec - timeSeconds % m.periodSec;
                if (nextEventSecs != 0) {
                    if (minNextSec == 0) minNextSec = nextEventSecs;
                    else if (nextEventSecs < minNextSec) minNextSec = nextEventSecs;
                }
            }
        }
        Log.d(TAG, "next stat gathering will occur in " + minNextSec + " seconds");

        for (LocalMonitor m :fullMonitorList) {
            if (m.periodSec != 0) {
                if ((m.periodSec - timeSeconds % m.periodSec) == minNextSec) {
                    lm.list.add(m);
                    Log.d(TAG, "monitor " + m.name + " will fire");
                }
            }
        }
        lm.nextEventSec = minNextSec;
        return lm;
    }

    private MonitorList setNextMetricAlarm() {
        MonitorList lm = getNextMonitors((SystemClock.elapsedRealtime() - initialTime_us)/1000);
        PendingIntent pi = PendingIntent.getBroadcast(MonitorMasterService.this, 0, new Intent(NEXT_MONITOR_ACTION), 0);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(pi);
        // Use a NON WAKEUP alarm. We do not want the monitor to interfere with S3
        // This means we might "miss" some metric uploads if the system goes to S3 for a long time.
        // This is not an issue: if this is the case, the alarm will be delivered anyway when we
        // wakeup.
        am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + lm.nextEventSec*1000, pi);
        return lm;
    }

    private void setUploadAlarm(boolean resetDate) {
        PendingIntent pi = PendingIntent.getBroadcast(MonitorMasterService.this, 0, new Intent(UPLOAD_ACTION), 0);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(pi);

        long nextAlarmDateUs = SystemClock.elapsedRealtime() + uploadIntervalSeconds*1000;
        String alarmFileName = cacheDir + "/.nextupload";
        if (resetDate) {
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(alarmFileName));
                out.write(String.valueOf(nextAlarmDateUs));
                out.close();
            } catch (IOException e) {
                Log.d(TAG, "Error writing to next alarm date file.");
            }
        }
        else {
            try {
                BufferedReader in = new BufferedReader(new FileReader(alarmFileName));
                String s = in.readLine();
                nextAlarmDateUs = Long.parseLong(s);
                in.close();
                Log.d(TAG, "Regenerated next date from cache file");
            } catch (IOException e) {
                Log.d(TAG, "Error with next-alarm-date file. Falling down to default value.");
            } catch (java.lang.NumberFormatException e) {
                Log.d(TAG, "Error reading next alarm date from file. Falling down to default value.");
            }
        }
        Log.d(TAG, "Next upload will take place at " + nextAlarmDateUs/1000 + ". Current time: " + SystemClock.elapsedRealtime()/1000);
        // Use a NON WAKEUP alarm. We do not want the monitor to interfere with S3
        am.set(AlarmManager.ELAPSED_REALTIME, nextAlarmDateUs, pi);
    }

    private void cancelUploadAlarm() {
        PendingIntent pi = PendingIntent.getBroadcast(MonitorMasterService.this, 0, new Intent(UPLOAD_ACTION), 0);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(pi);
    }

    private void uploadByFile(String bundlePath, boolean blocking) {
        File f = new File(STATDIR);

        if (!f.exists()) {
            Log.e(TAG, "Cannot find stat directory " + STATDIR + ". Trying to create it");
            if (!f.mkdirs()) {
                Log.e(TAG, "Cannot create stat directory " + STATDIR);
                return;
            }
            f.setReadable(true, false);
            f.setWritable(true, false);
            f.setExecutable(true, false);
        }

        if (!Util.copyFile(bundlePath, STATDIR + "/" + PD_PACKAGE + ZIP_SUFFIX)) {
            Log.e(TAG, "Error copying bundle to repository upload file");
            return;
        }

        /* Now trigger the upload */
        f = new File(triggerFileName);
        if (!f.exists()) {
            try {
                f.createNewFile();
                f.setReadable(true, false);
                f.setWritable(true, false);
                f.setExecutable(true, false);
            } catch (IOException e) {
                Log.e(TAG, "Cannot create trigger file");
                return;
            }

            if (blocking) { // Wait until event consumed by PhoneDoctor
                int nIterations = 0;
                int maxWaitTimeMs = 500;
                int sleepStepMs = 100;
                int maxIterations = maxWaitTimeMs / sleepStepMs;
                while (f.exists() && nIterations++ < maxIterations) {
                    try {
                        Thread.sleep(sleepStepMs);
                    } catch (InterruptedException e) {}
                }
                if (nIterations >= maxIterations) {
                    Log.e(TAG, "Timeout while waiting for PhoneDoctor to consume data event");
                }
            }
        }
        else {
            Log.d(TAG, "trigger file already exists - looks like PD upload took some time");
        }
    }

    private String createFileBundle() {
        int nm = 0;
        boolean zipDone = false;

        // No need to upload if we do not have anything to say
        if (isAllMonitorsEmpty()) return null;

        for (LocalMonitor m :fullMonitorList) {
            if (m.enabled) nm++;
        }

        String[] inputFileNames = new String[nm];

        nm = 0;
        for (LocalMonitor m :fullMonitorList) {
            if (m.enabled) {
                inputFileNames[nm] = cacheDir + "/" + m.metricFile;
                nm++;
            }
        }

        synchronized(mLock) {
            zipDone = Util.zipFiles(zipFileName, inputFileNames);
        }

        if (zipDone) {
            return zipFileName;
        }
        else {
            return null;
        }
    }

    private void uploadToCrashtool(boolean blocking) {
        String bundlePath = createFileBundle();

        if (bundlePath == null) {
            Log.d(TAG, "Error creating file bundle or all monitors empty");
            return;
        }

        for (LocalMonitor m :fullMonitorList) {
            if (m.enabled) {
                m.monitor.resetMetrics();
            }
        }

        uploadByFile(bundlePath, blocking);
    }

    private void launchMetricCollection(MonitorList m) {
        for (LocalMonitor lm :m.list) {
             Log.d(TAG, "Launching metric collection for monitor " + lm.name);
             lm.monitor.collectMetrics();
        }
    }

    public class MasterReceiver extends BroadcastReceiver {
        private final String TAG = MonitorMasterService.TAG;

        @Override
        public void onReceive(Context context, Intent intent) {
            String a = intent.getAction();
            if (a == null) {
                Log.d(TAG, "Received null intent");
                return;
            }
            if (a.equals(MonitorMasterService.UPLOAD_ACTION)) {
                uploadToCrashtool(false);
                setUploadAlarm(true);
            }
            else if (a.equals(MonitorMasterService.NEXT_MONITOR_ACTION)) {
                Log.d(TAG, "Received next monitor event");
                // Even if metric collection is not supposed ot last for too long - do not stall
                // the monitoring process.
                new Thread(new Runnable() {
                    public void run() {
                        synchronized(mLock) { // Should not be necessary except if sth goes really wrong
                            launchMetricCollection(nextMonitorList);
                            nextMonitorList = setNextMetricAlarm();
                        }
                    }
                }).start();
            }
            else if (a.equals(Intent.ACTION_SHUTDOWN)) {
                File f = new File(needUploadFileName);
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    Log.e(TAG, "Unable to create upload file");
                }
            }
            else {
                Log.e(TAG, "Received unrecognized intent: "+ a);
            }
        }
    }

    private class LocalMonitor {
        public String name;
        public int periodSec;
        public Monitor monitor;
        public boolean enabled;
        public String metricFile;

        public LocalMonitor(String name, int periodSec, Monitor m, boolean e, String mFile) {
            this.name = name;
            this.periodSec = periodSec;
            this.monitor = m;
            this.enabled = e;
            this.metricFile = mFile;
        }
    }

    private class MonitorList {
        public ArrayList<LocalMonitor> list;
        public long nextEventSec;

        public MonitorList(ArrayList<LocalMonitor> list, long nextEventSec) {
            this.list = list;
            this.nextEventSec = nextEventSec;
        }
    }
}
