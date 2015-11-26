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
