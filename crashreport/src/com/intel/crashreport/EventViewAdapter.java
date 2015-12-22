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

package com.intel.crashreport;

import java.util.ArrayList;

import com.intel.crashreport.specific.Event;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class EventViewAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	private static String TAG = "EventViewAdapter";
	private ArrayList<Event> listEvent;

	static class ViewHolder {
		TextView summary;
		TextView date;
		TextView state;
	}

	public EventViewAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
	}

	public void setListEvent(ArrayList<Event> list) {
		listEvent = list;
	}


	public Object getItem(int position) {
		if (null != listEvent) {
			if(listEvent.size() > position)
				return listEvent.get(position);
		}
		return null;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.event_view,null);
			holder = new ViewHolder();
			TextView tvSummary = (TextView)convertView.findViewById(R.id.event_view_summary);
			if(tvSummary == null) {
				tvSummary = new TextView(this.mContext);
			}
			holder.summary = tvSummary;
			TextView tvDate = (TextView)convertView.findViewById(R.id.event_view_date);
			if(tvDate == null) {
				tvDate = new TextView(this.mContext);
			}
			holder.date = tvDate;
			TextView tvState = (TextView)convertView.findViewById(R.id.event_view_state);
			if(tvState == null) {
				tvState = new TextView(this.mContext);
			}
			holder.state = tvState;
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder)convertView.getTag();
		}

		if(null != listEvent) {

			if(holder != null && listEvent.size() > position) {
				Event aEvent = listEvent.get(position);
				holder.summary.setText(aEvent.getType() + " (" + aEvent.getEventName() + ")");
				String sDate = aEvent.getDateAsString();
				holder.date.setText(sDate);

				if (!aEvent.isValid()){
					holder.state.setText("invalid");
					holder.state.setTextColor(Color.MAGENTA);
				}
				else if (aEvent.isDataReady()){
					if (aEvent.isUploaded()){
						if (aEvent.isLogUploaded() || aEvent.getCrashDir().isEmpty()){
							holder.state.setText("OK - uploaded") ;
							holder.state.setTextColor(mContext.getResources().getColor(R.color.green));
						}else{
							holder.state.setText( "log not uploaded" );
							holder.state.setTextColor(mContext.getResources().getColor(R.color.yellow_dark));
						}
					}else{
						holder.state.setText( "not uploaded") ;
						holder.state.setTextColor(mContext.getResources().getColor(R.color.red));
					}

				}
				else{
					holder.state.setText("not ready");
					holder.state.setTextColor(mContext.getResources().getColor(R.color.intel_blue));
				}

			}
		}
		Log.d(TAG+":getView "+position);
		return convertView;
	}



	public int getCount() {
		if (null != listEvent)
			return listEvent.size();
		return 0;

	}


}
