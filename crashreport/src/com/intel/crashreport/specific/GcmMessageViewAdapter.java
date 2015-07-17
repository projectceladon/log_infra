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

package com.intel.crashreport.specific;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.intel.crashreport.core.GcmMessage;
import com.intel.crashreport.Log;
import com.intel.crashreport.R;
import com.intel.crashreport.R.drawable;
import com.intel.crashreport.R.id;
import com.intel.crashreport.R.layout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GcmMessageViewAdapter extends BaseAdapter{

    private final Context mContext;
    private final LayoutInflater mInflater;
    private static String TAG = "GcmMessageViewAdapter";
    private ArrayList<GcmMessage> listGcmMessages;


    static class ViewHolder {
        ImageView icon;
        TextView title;
        TextView text;
        TextView date;
    }

    public GcmMessageViewAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public void setListGcmMessage(ArrayList<GcmMessage> list) {
        listGcmMessages = list;
    }

    @Override
    public int getCount() {
        if (null != listGcmMessages)
            return listGcmMessages.size();
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (null != listGcmMessages) {
            if(listGcmMessages.size() > position)
                return listGcmMessages.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
	boolean isHolderValid = false;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.gcm_message_item,null);
            holder = new ViewHolder();
            holder.title = (TextView)convertView.findViewById(R.id.gcm_message_view_title);
            holder.text = (TextView)convertView.findViewById(R.id.gcm_message_view_text);
            holder.icon = (ImageView)convertView.findViewById(R.id.gcm_message_view_icon);
            holder.date = (TextView)convertView.findViewById(R.id.gcm_message_view_date);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        if(holder.title != null && holder.text != null && holder.icon != null && holder.date != null) {
            isHolderValid = true;
        }

        if(isHolderValid && null != listGcmMessages) {

            if(listGcmMessages.size() > position) {
                GcmMessage message = listGcmMessages.get(position);
                holder.title.setText(message.getTitle());
                holder.text.setText(message.getText());
                holder.date.setText(formatDate(message.getDate()));
                switch(message.getType()) {
                    case GCM_NONE :
                        holder.icon.setImageResource(R.drawable.ic_crashtool_notification);
                        break;
                    case GCM_APP :
                        holder.icon.setImageResource(R.drawable.icon_fota);
                        break;
                    case GCM_URL :
                        holder.icon.setImageResource(R.drawable.ic_crashtool_notification);
                        break;
                    case GCM_PHONE_DOCTOR :
                        holder.icon.setImageResource(R.drawable.icon);
                        break;
                    default :
                        holder.icon.setImageResource(R.drawable.ic_crashtool_notification);
                        break;
                }

                if(message.isCancelled())
                    convertView.setAlpha(0.5f);
                else convertView.setAlpha(1f);

            }
        }
        Log.d(TAG+":getView "+position);
        return convertView;
    }

    public CharSequence formatDate(Date aDate) {
        if(aDate == null) {
            return "";
        }
        return formatDate(aDate, mContext);
    }

    public static CharSequence formatDate(Date aDate, Context aContext) {
        if(aContext == null || aDate == null) {
            return "";
        }
        Calendar today = Calendar.getInstance();
        Calendar otherCalendar = Calendar.getInstance();
        String stringFormat = "yyyy-MM-dd";
        otherCalendar.setTime(aDate);
        int thisDay = today.get(Calendar.DAY_OF_YEAR);
        int day = otherCalendar.get(Calendar.DAY_OF_YEAR);
        if(thisDay == day) {
            stringFormat = "HH:mm";
        } else {
            int thisYear = today.get(Calendar.YEAR);
            int year = otherCalendar.get(Calendar.YEAR);
            if(thisYear == year) {
                stringFormat = "MMM dd";
            }
        }
        SimpleDateFormat df = new SimpleDateFormat(
                stringFormat);
        CharSequence date = df.format(aDate);
        return date;
    }
}
