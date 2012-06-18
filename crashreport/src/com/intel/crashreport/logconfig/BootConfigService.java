
package com.intel.crashreport.logconfig;

import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import com.intel.crashreport.logconfig.bean.LogConfig;

/**
 * BootConfigService handles fetching and applying of persistent configurations.
 */
public class BootConfigService extends Service implements IConfigServiceClient {

    private ConfigServiceClient mClient;
    private Handler mHandler;
    private ConfigManager mConfigManager;

    private Runnable applyPersistantConfigs = new Runnable() {
        public void run() {
            mConfigManager = ConfigManager.getInstance(BootConfigService.this);
            ArrayList<LogConfig> mPersistLogConfigs = mConfigManager.getPersistantConfigList();
            for (LogConfig conf:mPersistLogConfigs)
                conf.setApplyValue(true);
            mClient.applyConfigList(mPersistLogConfigs,true);
        }
    };

    /**
     * Entry point of the service.
     */
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LogConfig", "Logconfig Boot service start");
        mClient = new ConfigServiceClient(this);
        mHandler.post(applyPersistantConfigs);
        return START_NOT_STICKY;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Create a new thread which handles the service execution
     */
    public void onCreate() {
        HandlerThread t = new HandlerThread("ConfigServiceBootThread",
                Process.THREAD_PRIORITY_BACKGROUND);
        t.start();
        mHandler = new Handler(t.getLooper());
    }

    public Looper getLooper() {
        return mHandler.getLooper();
    }

    // TODO to implement ?
    public void updateAppliedConfigs(ArrayList<String> configs, boolean applied) {
        if (mConfigManager != null)
            mConfigManager.updateAppliedConfigs(configs,applied);
    }

    public void clientFinished() {
        stopSelf();
    }

    public Context getContext() {
        return BootConfigService.this;
    }

}
