
package com.intel.crashreport.logconfig;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.intel.crashreport.logconfig.Storage.StorableConfigState;
import com.intel.crashreport.logconfig.bean.ConfigStatus;
import com.intel.crashreport.logconfig.bean.ConfigStatus.ConfigState;
import com.intel.crashreport.logconfig.bean.LogConfig;
import com.intel.crashreport.logconfig.ui.LogConfigHomeActivity;

public class ConfigManager implements IConfigServiceClient {

    private static ConfigManager mConfigManager;
    private Context mCtx;
    private ConfigLoader mConfigLoader;
    private Storage mStorage;
    private ArrayList<ConfigStatus> listConfigStatus;
    private ConfigServiceClient mClient = null;
    private LogConfigHomeActivity mActivity;

    private ConfigManager(Context c) {
        this.mCtx = c;
        this.mConfigLoader = ConfigLoader.getInstance(c);
        this.mStorage = new Storage(c);
    }

    public static synchronized ConfigManager getInstance(Context c) {
        if (mConfigManager == null)
            mConfigManager = new ConfigManager(c);
        return mConfigManager;
    }

    public ArrayList<ConfigStatus> getConfigStatusList() {
        if (listConfigStatus == null)
            listConfigStatus = loadBaseConfigStatusList();
        return listConfigStatus;
    }

    private ArrayList<ConfigStatus> loadBaseConfigStatusList() {
        List<String> mConfigNames = mConfigLoader.getList();
        List<String> mConfigAppliedNames = mStorage.getAppliedConfigs(StorableConfigState.ENABLED);
        ArrayList<ConfigStatus> mConfigStatusList = new ArrayList<ConfigStatus>();
        for (String configName : mConfigNames) {
            ConfigStatus cs = new ConfigStatus(configName);
            LogConfig mConfig = mConfigLoader.getConfig(configName);
            if (mConfig != null) {
                cs.setDescription(mConfig.getDescription());
                if (mConfig.isLockConfig()) {
                    cs.setState(ConfigState.LOCKED_ON);
                }
                else if (mConfigAppliedNames.contains(configName))
                    cs.setState(ConfigState.ON);
            }
            else
                cs.setDescription("! Bad file ! " + configName);
            mConfigStatusList.add(cs);
        }
        return mConfigStatusList;
    }

    /**
     * Save logconfigs state in sharedpreferences.
     * A logconfig is saved as enabled if its state is 'ON'
     * A logconfig is saved as disabled if its state is 'OFF'
     */
    public void saveConfigStatus() {
        ArrayList<String> mEnabledConfigAppliedNames = new ArrayList<String>();
        ArrayList<String> mDisabledConfigAppliedNames = new ArrayList<String>();
        for (ConfigStatus cs : getConfigStatusList()) {
            if (cs.getState() == ConfigState.ON)
                mEnabledConfigAppliedNames.add(cs.getName());
            else if (cs.getState() == ConfigState.OFF)
                mDisabledConfigAppliedNames.add(cs.getName());
        }
        mStorage.saveAppliedConfigs(mEnabledConfigAppliedNames, mDisabledConfigAppliedNames);
    }

    /**
     * Returns the persistent logconfigs status list. All logconfig of which
     * state has been previously saved in preferences are seen as 'persistent'.
     * Only 'LOCK' logconfig are not saved in persistent logconfigs.
     * @return the persistent logconfigs status list
     */
    public List<ConfigStatus> getPersistentConfigList() {
        Log.d("LogConfig", "Get persistent configs");
        List<ConfigStatus> mPersistConfigs = new ArrayList<ConfigStatus>();
        List<String> mEnabledPersistConfigNames = mStorage.getAppliedConfigs(StorableConfigState.ENABLED);
        List<String> mDisabledPersistConfigNames = mStorage.getAppliedConfigs(StorableConfigState.DISABLED);
        List<ConfigStatus> allStatus = getConfigStatusList();

        for(ConfigStatus cs:allStatus) {
            LogConfig mLog = mConfigLoader.getConfig(cs.getName());
            if (mLog != null) {
                cs.setLogConfig(mLog);
                Log.d("LogConfig", "Checking config : " + cs.getName());
                /*Retrieve logconfigs in the following order :
                 * - lock (new or not)
                 * - persistent enabled/disabled
                 * - default (new logconfig)*/
                if(mLog.isLockConfig()) {
                    Log.d("LogConfig", "Lock config, apply it");
                    mPersistConfigs.add(cs);
                }
                else if (mEnabledPersistConfigNames != null && mEnabledPersistConfigNames.contains(cs.getName())) {
                    Log.d("LogConfig", "Enabled persistent config, apply it");
                    cs.setState(ConfigState.TO_ON);
                    mPersistConfigs.add(cs); /*Apply ON*/
                }
                else if (mDisabledPersistConfigNames != null && mDisabledPersistConfigNames.contains(cs.getName())) {
                    Log.d("LogConfig", "Disabled persistent config"); /*don't apply OFF config*/
                }
                else if (mLog.isAppliedByDefault()) {
                    Log.d("LogConfig", "New Default config, add it to persist list and apply it");
                    cs.setState(ConfigState.TO_ON); /*Apply new default config*/
                    mPersistConfigs.add(cs);
                }
            }
        }
        return mPersistConfigs;
    }

    public ConfigStatus getConfigStatus(String configName) {
        for (ConfigStatus cs : getConfigStatusList()) {
            if (cs.getName().contentEquals(configName))
                return cs;
        }
        return null;
    }

    /**
     * Load LogConfig in ConfigStatus
     */
    public ConfigStatus loadConfigStatus(String configName) {
        ConfigStatus cs = getConfigStatus(configName);
        LogConfig mLogConfig = mConfigLoader.getConfig(configName);
        if (mLogConfig != null)
            cs.setLogConfig(mLogConfig);
        return cs;
    }

    /**
     * Apply a config list
     */
    public void applyConfigs(List<String> configNames) {
        if (configNames == null || configNames.isEmpty()) {
            Log.w("LogConfig", "No config to apply");
            return;
        }
        List<ConfigStatus> mConfigs = new ArrayList<ConfigStatus>();
        for (String configName : configNames) {
            ConfigStatus mConfigStatus = loadConfigStatus(configName);
            if (mConfigStatus != null)
                mConfigs.add(mConfigStatus);
        }
        if (mClient == null)
            mClient = new ConfigServiceClient(this);
        mClient.applyConfigList(mConfigs);
    }

    public void updateAppliedConfigs(List<ConfigStatus> configs) {
        saveConfigStatus();
        if (null != mActivity)
            mActivity.updateData();
    }

    public Context getContext() {
        return mCtx;
    }

    public Looper getLooper() {
        return mCtx.getMainLooper();
    }

    public void clientFinished() {
    }

    public ArrayList<String> getConfigsName() {
        return mConfigLoader.getList();
    }

    public ArrayList<String> getConfigsDescription() {
        ArrayList<String> descriptions = new ArrayList<String>();
        for (ConfigStatus cs : getConfigStatusList()) {
            descriptions.add(cs.getDescription());
        }
        return descriptions;
    }

    public ArrayList<ConfigStatus> reloadConfigStatusList() {
        listConfigStatus = loadBaseConfigStatusList();
        return listConfigStatus;
    }

    public void setActivity(LogConfigHomeActivity activity) {
        mActivity = activity;
    }

}
