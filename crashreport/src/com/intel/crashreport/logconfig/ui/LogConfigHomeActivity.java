
package com.intel.crashreport.logconfig.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.intel.crashreport.R;
import com.intel.crashreport.logconfig.ConfigManager;
import com.intel.crashreport.logconfig.bean.ConfigStatus;
import com.intel.crashreport.logconfig.bean.ConfigStatus.ConfigState;

public class LogConfigHomeActivity extends Activity {

    private ArrayList<ConfigStatus> mListConfigStatus;
    private ConfigManager mConfigManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logconfighome);
        ListView listConfigs = (ListView) findViewById(R.id.listLogConfig);

        // Verify Configs consistency
        mConfigManager = ConfigManager.getInstance(getApplicationContext());
        mConfigManager.setActivity(this);
        ArrayList<String> configsName = mConfigManager.getConfigsName();
        mListConfigStatus = mConfigManager.getConfigStatusList();
        if (configsName.size() != mListConfigStatus.size())
            mListConfigStatus = mConfigManager.reloadConfigStatusList();

        // Setup Config list adapter
        LogConfigAdapter listConfigsName = new LogConfigAdapter(getApplicationContext());
        listConfigs.setAdapter(listConfigsName);
        setTitle(getString(R.string.app_name) + " " + getString(R.string.app_version));

        // Disable all configs button
        Button submitButton = (Button) findViewById(R.id.button_logconfig);
        submitButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                List<String> configNames = new ArrayList<String>();
                for (ConfigStatus config : mConfigManager.getConfigStatusList())
                    if (config.getState() == ConfigState.ON) {
                        config.setState(ConfigState.TO_OFF);
                        configNames.add(config.getName());
                    }
                mConfigManager.applyConfigs(configNames);
            }
        });
    }

    protected void onPause() {
        super.onPause();
        mConfigManager.saveConfigStatus();
    }

    public void updateData() {
        ListView listConfigs = (ListView) findViewById(R.id.listLogConfig);
        listConfigs.invalidateViews();
    }

}
