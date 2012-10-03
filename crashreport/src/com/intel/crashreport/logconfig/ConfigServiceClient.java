
package com.intel.crashreport.logconfig;

import java.util.List;

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

import com.intel.crashreport.logconfig.bean.ConfigStatus;

public class ConfigServiceClient {

    private IConfigServiceClient mImplClient = null;
    private Context mContext = null;
    private static ConfigServiceClientHandler mHandler = null;
    private Messenger mMessenger = null;
    private Messenger mService = null;
    private Boolean mIsBound = false;

    private List<ConfigStatus> mConfigs;
    private int mConfigIndex;

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

    public void applyConfigList(List<ConfigStatus> configs) {
        mConfigs = configs;
        mConfigIndex = 0;
        if ((mConfigs != null) && (mConfigs.size() > mConfigIndex)) {
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
                    if (!mConfigs.isEmpty()) {
                        Message msg2 = Message.obtain(null, ConfigService.MSG_APPLY_CONFIG);
                        msg2.obj = mConfigs.get(mConfigIndex);
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
                    mConfigs.get(mConfigIndex).updateStateAfterApply();
                    mConfigIndex++;
                    if (mConfigIndex < mConfigs.size())
                        mHandler.sendEmptyMessage(MSG_APPLY_NEXT_CONFIG);
                    else
                        mHandler.sendEmptyMessage(MSG_FINISH_BOOT_CONFIG);
                    break;
                case ConfigService.MSG_CONFIG_APPLY_FAILED:
                    ConfigStatus mConfig = mConfigs.get(mConfigIndex);
                    mConfig.updateStateAfterFail();
                    Log.w("LogConfig", "Applying config " + mConfig.getName() + " failed");
                    mHandler.sendEmptyMessage(MSG_FINISH_BOOT_CONFIG);
                    break;
                case MSG_FINISH_BOOT_CONFIG:
                    doUnbindService();
                    mImplClient.updateAppliedConfigs(mConfigs);
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
