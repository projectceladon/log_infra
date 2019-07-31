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
        if(listConfigs != null) {
                LogConfigAdapter listConfigsName = new LogConfigAdapter(getApplicationContext());
                listConfigs.setAdapter(listConfigsName);
        }
        setTitle("");

        // Disable all configs button
        Button submitButton = (Button) findViewById(R.id.button_logconfig);
        if(submitButton != null) {
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
    }

    protected void onPause() {
        super.onPause();
        mConfigManager.saveConfigStatus();
    }

    public void updateData() {
        final ListView listConfigs = (ListView) findViewById(R.id.listLogConfig);
        if(listConfigs != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listConfigs.invalidateViews();
                }
            });
        }
    }

}
