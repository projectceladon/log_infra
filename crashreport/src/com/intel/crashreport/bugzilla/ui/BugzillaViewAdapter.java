package com.intel.crashreport.bugzilla.ui;

import java.util.ArrayList;

import com.intel.crashreport.Log;
import com.intel.crashreport.R;
import com.intel.crashreport.bugzilla.BZ;

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

    static class ViewHolder {
        TextView summary;
        TextView description;
        TextView state;
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
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.bugzilla_view,null);
            holder = new ViewHolder();
            holder.summary = (TextView)convertView.findViewById(R.id.bugzilla_view_summary);
            holder.description = (TextView)convertView.findViewById(R.id.bugzilla_view_description);
            holder.state = (TextView)convertView.findViewById(R.id.bugzilla_view_state);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        if(null != listBz) {

            if(listBz.size() > position) {
                BZ bz = listBz.get(position);
                holder.summary.setText(bz.getSummary());
                String text = "";
                String description = bz.getDescription();
                description = description.replace("\\n", "\n");
                text += description + "\n";
                text += "Component : "+ bz.getComponent() + "\n";
                text += "Severity : " + bz.getSeverity() + "\n";
                text += "Bug type : " + bz.getType() + "\n";
                holder.description.setText(text);
                //holder.state.setTextColor(0xFF0000);
                switch(bz.getState()) {
                    case BZ.UPLOADED_STATE :
                        holder.state.setText("Uploaded");
                        holder.state.setTextColor(Color.GREEN);
                        break;
                    case BZ.PENDING_STATE :
                        holder.state.setText("Logs not uploaded");
                        holder.state.setTextColor(Color.YELLOW);
                        break;
                    default :
                        holder.state.setText("Not uploaded");
                        holder.state.setTextColor(Color.RED);
                        break;
                }
            }
        }
        Log.w(TAG+":getView "+position);
        return convertView;
    }


}
