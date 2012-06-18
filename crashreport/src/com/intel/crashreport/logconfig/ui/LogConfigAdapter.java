package com.intel.crashreport.logconfig.ui;

import java.util.ArrayList;

import com.intel.crashreport.R;
import com.intel.crashreport.logconfig.ConfigManager;
import com.intel.crashreport.logconfig.bean.ConfigStatus;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

public class LogConfigAdapter extends BaseAdapter{
    private Context mContext;
    private LayoutInflater mInflater;
    private static String TAG = "LogConfigAdapter";
    private ConfigManager mConfigManager;

    static class ViewHolder {
        Switch configEnabled;
        TextView configDescription;
    }

    public LogConfigAdapter(Context context){
        mContext = context;
        mConfigManager = ConfigManager.getInstance(context);
        mInflater = LayoutInflater.from(context);

    }

    public int getCount() {
        return mConfigManager.getConfigStatusList().size();
    }

    public Object getItem(int position) {
        return mConfigManager.getConfigStatusList().get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.logconfig_item,null);
            holder = new ViewHolder();
            holder.configDescription = (TextView)convertView.findViewById(R.id.textView_description_logconfig);
            holder.configDescription.setOnClickListener(new OnClickListener(){
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(),LogConfigDisplaySettingsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("com.intel.crashreport.logconfig.config",(String) v.getTag());
                    v.getContext().startActivity(intent);
                }

            });


            holder.configEnabled = (Switch)convertView.findViewById(R.id.switch_enabled);
            holder.configEnabled.setOnCheckedChangeListener(new OnCheckedChangeListener(){

                public void onCheckedChanged(CompoundButton buttonView,
                        boolean isChecked) {
                    ArrayList<String> configNames = new ArrayList<String>();
                    configNames.add((String) buttonView.getTag());
                    buttonView.setEnabled(false);
                    mConfigManager.applyConfigs(configNames,isChecked);
                }

            });
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }
        ArrayList<ConfigStatus> configsList = mConfigManager.getConfigStatusList();
        holder.configDescription.setText(configsList.get(position).getDescription());
        holder.configDescription.setTag(configsList.get(position).getName());
        holder.configEnabled.setTag(configsList.get(position).getName());
        holder.configEnabled.setChecked(configsList.get(position).isApplied());
        holder.configEnabled.setEnabled(true);
        return convertView;
    }

}
