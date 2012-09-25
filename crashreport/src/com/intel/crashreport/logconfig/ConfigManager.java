
package com.intel.crashreport.logconfig;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Looper;

import com.intel.crashreport.logconfig.bean.ConfigStatus;
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
        if (listConfigStatus == null) {
            listConfigStatus = loadBaseConfigStatusList();
        }
        return listConfigStatus;
    }

    private ArrayList<ConfigStatus> loadBaseConfigStatusList() {
        List<String> mConfigNames = mConfigLoader.getList();
        //List<String> mConfigPersistentNames = mStorage.getPersistentConfigs();
        List<String> mConfigAppliedNames = mStorage.getAppliedConfigs();
        ArrayList<ConfigStatus> mConfigStatusList = new ArrayList<ConfigStatus>();
        for (String configName : mConfigNames) {
            ConfigStatus cs = new ConfigStatus(configName);
            if (mConfigAppliedNames.contains(configName))
                cs.setApplied(true);
            /*if (mConfigPersistentNames.contains(configName))
                cs.setPersistent(true);*/
            cs.setDescription(mConfigLoader.getConfig(configName).getDescription());
            mConfigStatusList.add(cs);
        }
        return mConfigStatusList;
    }

    public void saveConfigStatus() {
        //ArrayList<String> mConfigPersistentNames = new ArrayList<String>();
        ArrayList<String> mConfigAppliedNames = new ArrayList<String>();
        for (ConfigStatus cs : getConfigStatusList()) {
            /*if (cs.isPersistent())
                mConfigPersistentNames.add(cs.getName());*/
            if (cs.isApplied())
                mConfigAppliedNames.add(cs.getName());
        }
        //mStorage.savePersistentConfigs(mConfigPersistentNames);
        mStorage.saveAppliedConfigs(mConfigAppliedNames);
    }

    public ArrayList<LogConfig> getPersistantConfigList() {
        ArrayList<String> mPersistConfigNames = mStorage.getAppliedConfigs();
        ArrayList<LogConfig> mPersistLogConfigs = new ArrayList<LogConfig>();
        for (String configName : mPersistConfigNames) {
            ConfigStatus mConfigStatus = loadConfigStatus(configName);
            if (mConfigStatus != null)
                mPersistLogConfigs.add(mConfigStatus.getLogConfig());
        }
        return mPersistLogConfigs;
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
        if (mLogConfig == null)
            return null;
        cs.setLogConfig(mLogConfig);
        return cs;
    }

    /**
     * Apply a config list
     */
    public void applyConfigs(ArrayList<String> configNames, boolean applied) {
        ArrayList<LogConfig> mLogConfigs = new ArrayList<LogConfig>();
        for (String configName : configNames) {
            ConfigStatus mConfigStatus = loadConfigStatus(configName);
            if (mConfigStatus != null) {
                LogConfig logconf = mConfigStatus.getLogConfig();
                logconf.setApplyValue(applied);
                mLogConfigs.add(logconf);
            }
        }
        if (mClient == null)
            mClient = new ConfigServiceClient(this);
        mClient.applyConfigList(mLogConfigs, applied);
    }

    public void updateAppliedConfigs(ArrayList<String> configs,boolean applied) {
        for (ConfigStatus cs : getConfigStatusList()) {
            if (configs.contains(cs.getName()))
                cs.setApplied(applied);
        }
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
        for(ConfigStatus cs: getConfigStatusList()) {
            descriptions.add(cs.getDescription());
        }
        return descriptions;
    }

    public ArrayList<ConfigStatus> reloadConfigStatusList() {
        listConfigStatus = loadBaseConfigStatusList();
        return listConfigStatus;
    }

    public void setActivity(LogConfigHomeActivity activity){
        mActivity = activity;
    }

}
