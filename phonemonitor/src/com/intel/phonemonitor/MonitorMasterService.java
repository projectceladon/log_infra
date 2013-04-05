package com.intel.phonemonitor;

import java.io.File;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

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

    private static final String UPLOAD_ACTION            = "com.intel.phonemonitor.UPLOAD_METRICS_ACTION";
    private static final String COLLECT_ACTION           = "com.intel.phonemonitor.COLLECT_METRICS_ACTION";
    private static final String NEXT_MONITOR_ACTION      = "com.intel.phonemonitor.NEXT_MONITOR_ACTION";
    private static final String COLLECT_LIST_EXTRA       = "com.intel.phonemonitor.COLLECT_LIST_EXTRA";
    private static final String PD_UPLOAD_ACTION         = "intel.intent.action.phonedoctor.REPORT_STATS";
    private static final String PD_FILE_EXTRA            = "intel.intent.extra.phonedoctor.FILE";
    public  static final String EXTRA_ORIGINAL_ACTION    = "com.intel.phonemonitor.EXTRA_ORIGINAL_ACTION";

    private static final String STATDIR                  = "/logs/stats";
    private static final String TAG                      = "PhoneMonitorMaster";
    private static final String PD_PACKAGE               = "phonemonitor";
    private static final String ZIP_SUFFIX               = "_data.zip";

    private MasterReceiver mMasterReceiver;
    private MonitorList nextMonitorList;
    private ArrayList<LocalMonitor> fullMonitorList;
    private String cacheDir;
    private String zipFileName;
    private String triggerFileName;
    private String needUploadFileName;
    private long initialTime_us;
    private LinkedList<Intent> msgQueue;
    private IntentHandlingThread ht;

    @Override
    public IBinder onBind(Intent intent) {
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
        msgQueue = new LinkedList<Intent>();
        ht = new IntentHandlingThread();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        boolean softStart = true; // true if we are not restarted after being killed by LMK

        if (createMonitorList()) {
            if (intent == null) softStart = false; // Restarted by the system !

            initialTime_us = SystemClock.elapsedRealtime();

            IntentFilter filter = new IntentFilter();
            filter.addAction(UPLOAD_ACTION);
            filter.addAction(COLLECT_ACTION);
            filter.addAction(NEXT_MONITOR_ACTION);
            filter.addAction(Intent.ACTION_SHUTDOWN);

            ht.start();
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
        if (ht.isAlive()) ht.interrupt();
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
        // TODO: initialize all those values either from a HashMap belonging to a separate class
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
/*        fullMonitorList.add(new LocalMonitor("Thermal",
                                             0,
                                             new ThermalMonitor(),
                                             true,
                                             "thermalmonitor.txt"));
        fullMonitorList.add(new LocalMonitor("Telephony",
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

        Log.d(TAG, "Checking for next metrics collection");
        for (LocalMonitor m :fullMonitorList) {
            if (m.periodSec != 0) {
                long nextEventSecs = m.periodSec - timeSeconds % m.periodSec;
                if (nextEventSecs != 0) {
                    if (minNextSec == 0) minNextSec = nextEventSecs;
                    else if (nextEventSecs < minNextSec) minNextSec = nextEventSecs;
                }
            }
        }
        Log.d(TAG, "next metrics collection will occur in " + minNextSec + " seconds");

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
            BufferedWriter out = null;
            try {
                out = new BufferedWriter(new FileWriter(alarmFileName));
                out.write(String.valueOf(nextAlarmDateUs));
            } catch (IOException e) {
                Log.e(TAG, "Error writing to next alarm date file.");
            }
            finally {
                try {
                    if (out != null) out.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing the next alarm date file.");
                }
            }
        }
        else {
            BufferedReader in = null;
            try {
                in = new BufferedReader(new FileReader(alarmFileName));
                String s = in.readLine();
                nextAlarmDateUs = Long.parseLong(s);
                Log.d(TAG, "Regenerated next date from cache file");
            } catch (IOException e) {
                Log.d(TAG, "Error with next-alarm-date file. Falling down to default value.");
            } catch (java.lang.NumberFormatException e) {
                Log.d(TAG, "Error reading next alarm date from file. Falling down to default value.");
            }
            finally {
                try {
                    if (in != null) in.close();
                } catch (IOException e) {
                    Log.e(TAG , "Error closing next alarm date file");
                }
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

    private String copyMetricFile(String bundlePath) {
         File f = new File(STATDIR);

        if (!f.exists()) {
            Log.e(TAG, "Cannot find stat directory " + STATDIR + ". Trying to create it");
            if (!f.mkdirs()) {
                Log.e(TAG, "Cannot create stat directory " + STATDIR);
                return null;
            }
            f.setReadable(true, false);
            f.setWritable(true, false);
            f.setExecutable(true, false);
        }

        String finalFile = STATDIR + "/" + PD_PACKAGE + ZIP_SUFFIX;
        if (!Util.copyFile(bundlePath, finalFile)) {
            Log.e(TAG, "Error copying bundle to repository upload file");
            return null;
        }
        return finalFile;

   }

   private void uploadByIntent(String bundlePath, boolean blocking) {
        // Copy the file to a location where PhoneDoctor can see it
        String uploadFileName = copyMetricFile(bundlePath);
        if (uploadFileName == null) {
            Log.e(TAG, "Unable to copy metric file " + bundlePath + " to " + STATDIR + ".");
            return;
        }
        Intent i = new Intent(PD_UPLOAD_ACTION);
        i.putExtra(PD_FILE_EXTRA, uploadFileName);
        sendBroadcast(i);
        /* TODO: we cannot check for the event upload as of today, making
           blocking calls to this function impossible. Need to solve this ! */
    }

    private void uploadByFile(String bundlePath, boolean blocking) {
        // Copy the file to a location where PhoneDoctor can see it
        if (copyMetricFile(bundlePath) == null) {
            Log.e(TAG, "Unable to copy metric file " + bundlePath + " to " + STATDIR + ".");
            return;
        }
        // Now trigger the upload
        File f = new File(triggerFileName);
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
        ArrayList<String> inputGzFileNames = new ArrayList<String>();

        // No need to upload if we do not have anything to say
        if (isAllMonitorsEmpty()) return null;

        for (LocalMonitor m :fullMonitorList) {
            if (m.enabled) {
                String monitorName = cacheDir + "/" + m.metricFile;
                String gzipName = Util.gzipFile(monitorName);
                if (gzipName != null)
                    inputGzFileNames.add(gzipName);
            }
        }

        boolean zipDone = Util.zipFiles(zipFileName, inputGzFileNames);

        if (zipDone) {
            return zipFileName;
        }
        else {
            Log.d(TAG, "Cannot create zip");
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

        File f = new File(needUploadFileName);
        if (f.exists()) f.delete();
    }

    private void touchNeedUploadFile() {
        File f = new File(needUploadFileName);
        try {
            f.createNewFile();
        } catch (IOException e) {
            Log.e(TAG, "Unable to create upload file");
        }
    }

    private void launchMetricCollection(MonitorList m) {
        /* Each time we collect new metrics, mark that an
           upload is needed. This way, if we shutdown (even
           by long keypress) we will at least upload the
           metrics at the next reboot. */
        touchNeedUploadFile();
        for (LocalMonitor lm :m.list) {
            Log.d(TAG, "Launching metric collection for monitor " + lm.name);
            lm.monitor.collectMetrics();
        }
    }

    private LocalMonitor getMonitorByName(CharSequence s) {
        for (LocalMonitor lm :fullMonitorList) {
            if (lm.name.equals(s)) {
                return lm;
            }
        }
        return null;
    }

    public class MasterReceiver extends BroadcastReceiver {
        private final String TAG = MonitorMasterService.TAG;

        @Override
        public void onReceive(Context context, Intent intent) {
            String a = intent.getAction();
            final Intent i = intent;
            if (a == null) {
                Log.d(TAG, "Received null intent");
                return;
            }
            Log.d(TAG, "Received " + a);
            synchronized(msgQueue) {
                msgQueue.offer(intent);
                msgQueue.notify();
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

    private class IntentHandlingThread extends Thread {

        public IntentHandlingThread() {
            super(TAG);
        }

        public synchronized void receiverHandler(Intent intent) {
            String a = intent.getAction();
            if (a.equals(MonitorMasterService.UPLOAD_ACTION)) {
                Log.d(TAG, "Handling upload event");
                uploadToCrashtool(false);
                setUploadAlarm(true);
            }
            else if (a.equals(MonitorMasterService.NEXT_MONITOR_ACTION)) {
                Log.d(TAG, "Handling next monitor event");
                launchMetricCollection(nextMonitorList);
                nextMonitorList = setNextMetricAlarm();
            }
            else if (MonitorMasterService.COLLECT_ACTION.equals(a)) {
                Log.d(TAG, "Handling metric collection event");
                final CharSequence[] monitorListExtras = intent.getCharSequenceArrayExtra(MonitorMasterService.COLLECT_LIST_EXTRA);
                if (monitorListExtras != null) {
                    for (CharSequence s :monitorListExtras) {
                        LocalMonitor m = getMonitorByName(s);
                        if (m != null && m.enabled) {
                            m.monitor.collectMetrics();
                            m.monitor.forceFlush();
                        }
                    }
                }
            }
            else if (a.equals(Intent.ACTION_SHUTDOWN)) {
            /* Not redundant with the creation of upload file
               in launchMetricCollection() - some monitors never
               collect data directly but only listen to intents.
               If only such monitors are active, we need to mark an upload
               here. There is still a possible problem if only those were active
               *and* a shutdown occurs because of long keypress or battery
               removal... but in this case, there is really nothing we can do */
                touchNeedUploadFile();
                stopMonitors(); // Try and flush monitors on exit
            }
            else {
                Log.e(TAG, "Received unrecognized intent: "+ a);
            }
        }

        public void run() {
            Intent i;

            while (true) {
                try {
                    synchronized(msgQueue) {
                        while (msgQueue.size() == 0)
                            msgQueue.wait();
                        i = msgQueue.poll();
                    }
                    receiverHandler(i);
                }
                catch (InterruptedException e) {
                    Log.d(TAG, "Event handling loop interrupted.");
                    break;
                }
            }
        }
    }
}
