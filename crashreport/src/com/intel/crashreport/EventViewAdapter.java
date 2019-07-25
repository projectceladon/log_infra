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
					holder.state.setTextColor(mContext.getResources().getColor(R.color.blue));
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
