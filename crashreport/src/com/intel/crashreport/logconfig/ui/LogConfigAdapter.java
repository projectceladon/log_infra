
package com.intel.crashreport.logconfig.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.intel.crashreport.R;
import com.intel.crashreport.logconfig.ConfigManager;
import com.intel.crashreport.logconfig.bean.ConfigStatus;
import com.intel.crashreport.logconfig.bean.ConfigStatus.ConfigState;

public class LogConfigAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private ConfigManager mConfigManager;

    static class ViewHolder {
        Switch configEnabled;
        TextView configDescription;
    }

    public LogConfigAdapter(Context context) {
        mConfigManager = ConfigManager.getInstance(context);
        mInflater = LayoutInflater.from(context);
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
            holder.configDescription.setOnClickListener(configStatusItemListener);
            holder.configEnabled = (Switch) convertView.findViewById(R.id.switch_enabled);
            holder.configEnabled.setTag(config.getName());
            holder.configEnabled.setChecked(cState == ConfigState.ON || cState == ConfigState.TO_ON || cState == ConfigState.LOCKED_ON);
            holder.configEnabled.setOnCheckedChangeListener(configStatusSwitchListener);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            holder.configEnabled.setTag(config.getName());
            holder.configEnabled.setChecked(cState == ConfigState.ON || cState == ConfigState.TO_ON || cState == ConfigState.LOCKED_ON);
        }
        holder.configDescription.setText(config.getDescription());
        holder.configDescription.setTag(config.getName());
        holder.configEnabled.setEnabled(cState == ConfigState.ON || cState == ConfigState.OFF);
        return convertView;
    }

    OnClickListener configStatusItemListener = new OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(),
                    LogConfigDisplaySettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("com.intel.crashreport.logconfig.config", (String) v.getTag());
            v.getContext().startActivity(intent);
        }
    };

    OnCheckedChangeListener configStatusSwitchListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
            buttonView.setEnabled(false);
            ConfigStatus mConfigStatus =
                    mConfigManager.getConfigStatus((String) ((Switch) buttonView).getTag());
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
