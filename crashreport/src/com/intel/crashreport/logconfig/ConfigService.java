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

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import com.intel.crashreport.logconfig.bean.ConfigStatus;
import com.intel.crashreport.logconfig.bean.LogSetting;

/**
 * <code>ConfigService</code> apply each passed <code>LogConfig</code>. Clients
 * use <code>send</code> method to send <code>Message</code> to the service.
 * <code>LogConfig</code> is passed as the <code>obj</code> attribute of the
 * <code>Message</code>.
 */
public class ConfigService extends Service {

    Messenger mMessenger;
    ArrayList<Messenger> mClients = new ArrayList<Messenger>();

    static final int MSG_REGISTER = 0;
    static final int MSG_APPLY_CONFIG = 1;
    static final int MSG_CONFIG_APPLIED = 2;
    static final int MSG_CONFIG_APPLY_FAILED = 3;
    static final int MSG_UNREGISTER = 4;

    private static LogConfigClient mLogConfigClient;
    private ApplicatorLogSettingAdapter mAdapter;

    /**
     * Use to interact with the service.
     *
     * @param msg Type <code>Message</code>, <code>what</code> attribute should
     *            be filled with a <code>ServiceMsg</code>. If it is a
     *            configuration to apply, <code>obj</code> attribute should be
     *            filled with it.
     * @see Message
     */
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER:
                    Log.i("LogConfig", "MSG_REGISTER");
                    mClients.add(msg.replyTo);
                    break;
                case MSG_APPLY_CONFIG:
                    Log.i("LogConfig", "MSG_APPLY_CONFIG");
                    ConfigStatus config = (ConfigStatus) msg.obj;
                    Message rMsg;
                    if(null != mMessenger) {
                        try {
                            applyConfig(config);
                            rMsg = Message.obtain(null, MSG_CONFIG_APPLIED);
                        } catch (IllegalStateException e1) {
                            // if service failed to apply config
                            rMsg = Message.obtain(null, MSG_CONFIG_APPLY_FAILED);
                            Log.i("LogConfig", "Exception : "+e1.getMessage());
                        }
                        rMsg.replyTo = mMessenger;
                    } else {
                        rMsg = Message.obtain(null, MSG_CONFIG_APPLY_FAILED);
                        Log.w("LogConfig", "Could not find a suitable messenger for message.");
                    }
                    Messenger mClient = msg.replyTo;
                    try {
                        mClient.send(rMsg);
                    } catch (RemoteException e) {
                        mClients.remove(mClient);
                    }
                    break;
                case MSG_UNREGISTER:
                    Log.i("LogConfig", "MSG_UNREGISTER");
                    mClients.remove(msg.replyTo);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void applyConfig(ConfigStatus config) throws IllegalStateException {
        Log.i("LogConfig", "applying config: " + config.getName());
        List<LogSetting> mSettings = config.getSettingsToApply();
        if (mSettings != null) {
            if (mLogConfigClient == null)
                mLogConfigClient = LogConfigClient.getInstance(getApplicationContext());
            if (mAdapter == null)
                mAdapter = new ApplicatorLogSettingAdapter(mLogConfigClient);

            for (LogSetting s : mSettings)
                mAdapter.apply(s);
        }
    }

    public void onCreate() {
        HandlerThread thread = new HandlerThread("ConfigServiceThread",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        Looper threadLooper = thread.getLooper();
        if(null != threadLooper) {
            mMessenger = new Messenger(new ServiceHandler(threadLooper));
        }
    }

    /**
     * Called when service is started. If it comes from
     * <code>Intent.ACTION_BOOT_COMPLETED</code>,
     * <code>BootConfigService is called</code>.
     */
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void onDestroy() {
        if (mLogConfigClient != null) {
            mLogConfigClient.close();
            mLogConfigClient = null;
        }
        mAdapter = null;
    }

    public IBinder onBind(Intent intent) {
        if(null == mMessenger) {
            return null;
        }
        return mMessenger.getBinder();
    }
}
