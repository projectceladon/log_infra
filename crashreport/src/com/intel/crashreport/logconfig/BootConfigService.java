/* INTEL CONFIDENTIAL
 * Copyright 2015 Intel Corporation
 *
 * The source code contained or described herein and all documents
 * related to the source code ("Material") are owned by Intel
 * Corporation or its suppliers or licensors. Title to the Material
 * remains with Intel Corporation or its suppliers and
 * licensors. The Material contains trade secrets and proprietary
 * and confidential information of Intel or its suppliers and
 * licensors. The Material is protected by worldwide copyright and
 * trade secret laws and treaty provisions. No part of the Material
 * may be used, copied, reproduced, modified, published, uploaded,
 * posted, transmitted, distributed, or disclosed in any way without
 * Intel's prior express written permission.
 *
 * No license under any patent, copyright, trade secret or other
 * intellectual property right is granted to or conferred upon you
 * by disclosure or delivery of the Materials, either expressly, by
 * implication, inducement, estoppel or otherwise. Any license under
 * such intellectual property rights must be express and approved by
 * Intel in writing.
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
