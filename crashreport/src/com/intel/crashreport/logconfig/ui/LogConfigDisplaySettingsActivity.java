
package com.intel.crashreport.logconfig.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;

import com.intel.crashreport.R;
import com.intel.crashreport.logconfig.ConfigManager;
import com.intel.crashreport.logconfig.bean.ConfigStatus;
import com.intel.crashreport.logconfig.bean.LogConfig;

public class LogConfigDisplaySettingsActivity extends Activity {

    private ConfigManager mConfigManager;
    private ArrayList<ConfigStatus> mListConfigStatus;
    private Context context;
    private static String TAG = "LogDisplaySettingsActivity";
    private int currentIndex;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logconfig_settings);
        context = getApplicationContext();
        ListView listView = (ListView) findViewById(R.id.list_settings);
        TextView titleConfig = (TextView) findViewById(R.id.title_config);

        mConfigManager = ConfigManager.getInstance(context);
        mListConfigStatus = mConfigManager.getConfigStatusList();
        String nameConfig = getIntent().getStringExtra("com.intel.crashreport.logconfig.config");
        ArrayList<String> configsNames = mConfigManager.getConfigsName();
        LogConfig config = null;
        if (!configsNames.isEmpty()) {
            for (int i = 0; i < configsNames.size(); i++) {
                if (nameConfig.equals(configsNames.get(i))) {
                    mListConfigStatus.set(i,
                            mConfigManager.loadConfigStatus(mListConfigStatus.get(i)
                                    .getName()));
                    config = mListConfigStatus.get(i).getLogConfig();
                    currentIndex = i;

                }
            }
        }
        titleConfig.setText(mListConfigStatus.get(currentIndex).getDescription() + " :");
        String listSettings[] = new String[0];
        if (!config.isEmpty()) {
            listSettings = config.getSettings();
        }
        ArrayAdapter<String> settingsAdapter = new ArrayAdapter<String>(this,
                R.layout.logconfig_setting_item, listSettings);
        listView.setAdapter(settingsAdapter);

        setTitle(getString(R.string.app_name) + " " + getString(R.string.app_version));

    }

    protected void onPause() {
        super.onPause();
        mConfigManager.saveConfigStatus();
    }
}
