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
			holder.summary = (TextView)convertView.findViewById(R.id.event_view_summary);
			holder.date = (TextView)convertView.findViewById(R.id.event_view_date);
			holder.state = (TextView)convertView.findViewById(R.id.event_view_state);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder)convertView.getTag();
		}

		if(null != listEvent) {

			if(listEvent.size() > position) {
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
						if (aEvent.isLogUploaded() || aEvent.getCrashDir().equals("")){
							holder.state.setText("OK - uploaded") ;
							holder.state.setTextColor(Color.GREEN);
						}else{
							holder.state.setText( "log not uploaded" );
							holder.state.setTextColor(Color.YELLOW);
						}
					}else{
						holder.state.setText( "not uploaded") ;
						holder.state.setTextColor(Color.RED);
					}

				}
				else{
					holder.state.setText("not ready");
					holder.state.setTextColor(Color.CYAN);
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
