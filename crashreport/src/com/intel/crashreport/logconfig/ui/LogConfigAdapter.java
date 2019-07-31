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

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.os.SystemProperties;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.intel.crashreport.R;
import com.intel.crashreport.logconfig.ConfigManager;
import com.intel.crashreport.logconfig.bean.ConfigStatus;
import com.intel.crashreport.logconfig.bean.ConfigStatus.ConfigState;

public class LogConfigAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private ConfigManager mConfigManager;
    private Context mCtx;

    static class ViewHolder {
        CheckBox configEnabled;
        TextView configDescription;
    }

    public LogConfigAdapter(Context context) {
        mConfigManager = ConfigManager.getInstance(context);
        mInflater = LayoutInflater.from(context);
        mCtx = context;
    }

    public int getCount() {
        return mConfigManager.getConfigStatusList().size();
    }

    public Object getItem(int position) {
        return getMyConfigStatus(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        /*Retrieve config state*/
        ConfigStatus config = getMyConfigStatus(position);
        ConfigState cState = config.getState();

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.logconfig_item, null);
            holder = new ViewHolder();
            holder.configDescription = (TextView) convertView
                    .findViewById(R.id.textView_description_logconfig);
            holder.configEnabled = (CheckBox) convertView.findViewById(R.id.switch_enabled);
            if(null != holder.configDescription && null != holder.configEnabled) {
                holder.configDescription.setOnClickListener(configStatusItemListener);
                holder.configEnabled.setTag(config.getName());
                holder.configEnabled.setChecked(cState == ConfigState.ON || cState == ConfigState.TO_ON || cState == ConfigState.LOCKED_ON);
                holder.configEnabled.setOnCheckedChangeListener(configStatusSwitchListener);
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            if(null != holder.configEnabled) {
                holder.configEnabled.setTag(config.getName());
                holder.configEnabled.setChecked(cState == ConfigState.ON || cState == ConfigState.TO_ON || cState == ConfigState.LOCKED_ON);
            }
        }
        if(null != holder.configDescription && null != holder.configEnabled) {
            holder.configDescription.setText(config.getDescription());
            holder.configDescription.setTag(config.getName());
            holder.configEnabled.setEnabled(cState == ConfigState.ON || cState == ConfigState.OFF);
        }
        return convertView;
    }

    OnClickListener configStatusItemListener = new OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(),
                    LogConfigDisplaySettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("com.intel.crashreport.logconfig.config", (String) v.getTag());
            v.getContext().startActivityAsUser(intent, UserHandle.CURRENT);
        }
    };

    OnCheckedChangeListener configStatusSwitchListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
            String logConfigProp = SystemProperties.get("intel.logconfig.available", "");
            if(!logConfigProp.equals("1")) {
                final Toast toast = Toast.makeText(mCtx,
                    "Property not available, don't allow to change.", Toast.LENGTH_SHORT);
                toast.show();
                buttonView.setChecked(!isChecked);
                return;
            }
            buttonView.setEnabled(false);
            ConfigStatus mConfigStatus =
                    mConfigManager.getConfigStatus((String) buttonView.getTag());
            if(null == mConfigStatus) {
                return; /* No configuration to apply */
            }
            ConfigState mState = mConfigStatus.getState();
            /*Compare switch button state to the associated config state
             * and update/apply setting if necessary*/
            if (mState == ConfigState.OFF && isChecked)
                mConfigStatus.setState(ConfigState.TO_ON);
            else if (mState == ConfigState.ON && !isChecked)
                mConfigStatus.setState(ConfigState.TO_OFF);
            else
                return; /*No config to apply*/
            List<String> mConfigNames = new ArrayList<String>();
            mConfigNames.add(mConfigStatus.getName());
            mConfigManager.applyConfigs(mConfigNames);
        }
    };

    private ConfigStatus getMyConfigStatus(int position) {
        return mConfigManager.getConfigStatusList().get(position);
    }
}
