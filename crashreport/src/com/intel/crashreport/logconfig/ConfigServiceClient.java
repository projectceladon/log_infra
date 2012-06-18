
package com.intel.crashreport.logconfig;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.intel.crashreport.logconfig.bean.LogConfig;

public class ConfigServiceClient {

    private IConfigServiceClient mImplClient = null;
    private Context mContext = null;
    private static ConfigServiceClientHandler mHandler = null;
    private Messenger mMessenger = null;
    private Messenger mService = null;
    private Boolean mIsBound = false;
    private boolean mApply;

    private ArrayList<LogConfig> mLogConfigs;
    private int mConfigIndex;
    private ArrayList<String> appliedConfigs;

    static final int CLIENT_OFFSET = 100;
    static final int MSG_FETCH_DATA = 1 + CLIENT_OFFSET;
    static final int MSG_APPLY_NEXT_CONFIG = 2 + CLIENT_OFFSET;
    static final int MSG_FINISH_BOOT_CONFIG = 3 + CLIENT_OFFSET;
    static final int MSG_CLOSE = 4 + CLIENT_OFFSET;

    public ConfigServiceClient(IConfigServiceClient implClient) {
        mImplClient = implClient;
        mContext = implClient.getContext();
        mHandler = new ConfigServiceClientHandler(implClient.getLooper());
        mMessenger = new Messenger(mHandler);
    }

    public void applyConfigList(ArrayList<LogConfig> configs, boolean enabled) {
        mLogConfigs = configs;
        mConfigIndex = 0;
        mApply = enabled;
        appliedConfigs = new ArrayList<String>();
        if ((mLogConfigs != null) && (mLogConfigs.size() > mConfigIndex)) {
            doBindService();
        } else {
            mHandler.sendEmptyMessage(MSG_FINISH_BOOT_CONFIG);
        }
    }

    private final class ConfigServiceClientHandler extends Handler {

        public ConfigServiceClientHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_APPLY_NEXT_CONFIG:
                    if (!mLogConfigs.isEmpty()) {
                        Message msg2 = Message.obtain(null, ConfigService.MSG_APPLY_CONFIG);
                        msg2.obj = mLogConfigs.get(mConfigIndex);
                        msg2.replyTo = mMessenger;
                        try {
                            mService.send(msg2);
                        } catch (RemoteException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    break;
                case ConfigService.MSG_CONFIG_APPLIED:
                    appliedConfigs.add(mLogConfigs.get(mConfigIndex).getName());
                    mConfigIndex++;
                    if (mConfigIndex < mLogConfigs.size())
                        mHandler.sendEmptyMessage(MSG_APPLY_NEXT_CONFIG);
                    else
                        mHandler.sendEmptyMessage(MSG_FINISH_BOOT_CONFIG);
                    break;
                case ConfigService.MSG_CONFIG_APPLY_FAILED:
                    Log.e("LogConfig", "Applying config " + mLogConfigs.get(mConfigIndex).getName()
                            + " failed");
                    mHandler.sendEmptyMessage(MSG_FINISH_BOOT_CONFIG);
                    break;
                case MSG_FINISH_BOOT_CONFIG:
                    doUnbindService();
                    mImplClient.updateAppliedConfigs(appliedConfigs,mApply);
                    break;
                case MSG_CLOSE:
                    mImplClient.clientFinished();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Interact with <code>ConfigService</code>
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            mService = new Messenger(service);
            try {
                Message msg = Message.obtain(null,
                        ConfigService.MSG_REGISTER);
                msg.replyTo = mMessenger;
                mService.send(msg);

                mHandler.sendEmptyMessage(MSG_APPLY_NEXT_CONFIG);
            } catch (RemoteException e) {
                // TODO see what you see !!!
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            mHandler.sendEmptyMessage(MSG_CLOSE);
        }
    };

    void doBindService() {
        mContext.bindService(new Intent(mContext, ConfigService.class),
                mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            if (mService != null) {
                Message msg = Message.obtain(null,
                        ConfigService.MSG_UNREGISTER);
                msg.replyTo = mMessenger;
                try {
                    mService.send(msg);
                } catch (RemoteException e) {
                    // already disconnected
                }
            }
            mContext.unbindService(mConnection);
            mIsBound = false;
        }
    }

}
