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

package com.intel.crashreport.bugzilla.ui.common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import com.intel.crashreport.Log;
import com.intel.crashreport.R;
import com.intel.crashreport.core.BZ;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BugzillaViewAdapter extends BaseAdapter{

    private Context mContext;
    private LayoutInflater mInflater;
    private static String TAG = "BugzillaViewAdapter";
    private ArrayList<BZ> listBz;

    private final static SimpleDateFormat EVENT_DF = new SimpleDateFormat("yyyy-MM-dd/HH:mm:ss");


    static class ViewHolder {
        TextView summary;
        TextView description;
        TextView state;
        TextView time;
    }

    public BugzillaViewAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public void setListBz(ArrayList<BZ> list) {
        listBz = list;
    }

    public int getCount() {
        if (null != listBz)
            return listBz.size();
        return 0;
    }

    public Object getItem(int position) {
        if (null != listBz) {
            if(listBz.size() > position)
                return listBz.get(position);
        }
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
	boolean isViewHolderValid = false;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.bugzilla_view,null);
            holder = new ViewHolder();
            holder.summary = (TextView)convertView.findViewById(R.id.bugzilla_view_summary);
            holder.description = (TextView)convertView.findViewById(R.id.bugzilla_view_description);
            holder.state = (TextView)convertView.findViewById(R.id.bugzilla_view_state);
            holder.time = (TextView)convertView.findViewById(R.id.bugzilla_view_time);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

	if(holder.summary != null && holder.description != null && holder.state != null && holder.time != null) {
		isViewHolderValid = true;
	}

        if(isViewHolderValid && listBz != null) {

            if(listBz.size() > position) {
                BZ bz = listBz.get(position);
                holder.summary.setText(bz.getSummary());
                String text = "";
                String description = bz.getDescription();
                description = description.replace("\\n", "\n");
                text += description + "\n";
                text += "Component : "+ bz.getComponent() + "\n";
                text += "Severity : " + bz.getSeverity() + "\n";
                text += "Bug type : " + bz.getType();
                holder.description.setText(text);
                //holder.state.setTextColor(0xFF0000);
                switch(bz.getState()) {
                    case BZ.UPLOADED_STATE :
                        holder.state.setText("Uploaded");
                        holder.state.setTextColor(mContext.getResources().getColor(R.color.green));
                        break;
                    case BZ.PENDING_STATE :
                        holder.state.setText("Logs not uploaded");
                        holder.state.setTextColor(mContext.getResources().getColor(R.color.yellow_dark));
                        break;
                    case BZ.INVALID_STATE :
                        holder.state.setText("Invalid");
                        holder.state.setTextColor(mContext.getResources().getColor(R.color.magenta));
                        break;
                    default :
                        holder.state.setText("Not uploaded");
                        holder.state.setTextColor(mContext.getResources().getColor(R.color.red));
                        break;
                }

                EVENT_DF.setTimeZone(TimeZone.getTimeZone("GMT"));
                text = "Bug created on : "+EVENT_DF.format(bz.getCreationDate());
                holder.time.setText(text);
            }
        }
        Log.w(TAG+":getView "+position);
        return convertView;
    }


}
