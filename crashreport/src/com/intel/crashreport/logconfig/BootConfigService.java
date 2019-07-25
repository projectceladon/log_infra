/* Copyright (C) 2019 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.crashreport.logconfig;

import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import com.intel.crashreport.logconfig.bean.ConfigStatus;

/**
 * BootConfigService handles fetching and applying of persistent configurations.
 */
public class BootConfigService extends Service implements IConfigServiceClient {

    private ConfigServiceClient mClient;
    private Handler mHandler;
    private ConfigManager mConfigManager;

    private Runnable applyPersistentConfigs = new Runnable() {
        public void run() {
            mConfigManager = ConfigManager.getInstance(BootConfigService.this);
            List<ConfigStatus> mPersistConfigs = mConfigManager.getPersistentConfigList();
            mClient.applyConfigList(mPersistConfigs);
        }
    };

    /**
     * Entry point of the service.
     */
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LogConfig", "Logconfig Boot service start");
        mClient = new ConfigServiceClient(this);
        mHandler.post(applyPersistentConfigs);
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
	Looper threadLooper = t.getLooper();
	if(threadLooper != null) {
	        mHandler = new Handler(threadLooper);
	} else {
		mHandler = new Handler();
	}
    }

    public Looper getLooper() {
        return mHandler.getLooper();
    }

    // TODO to implement ?
    public void updateAppliedConfigs(List<ConfigStatus> configs) {
        if (mConfigManager != null)
            mConfigManager.updateAppliedConfigs(configs);
    }

    public void clientFinished() {
        stopSelf();
    }

    public Context getContext() {
        return BootConfigService.this;
    }

}
