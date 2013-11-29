package com.intel.crashreport;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GcmMessageViewAdapter extends BaseAdapter{

    private Context mContext;
    private LayoutInflater mInflater;
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

    public int getCount() {
        if (null != listGcmMessages)
            return listGcmMessages.size();
        return 0;
    }

    public Object getItem(int position) {
        if (null != listGcmMessages) {
            if(listGcmMessages.size() > position)
                return listGcmMessages.get(position);
        }
        return null;
    }

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
                holder.date.setText(message.getDateAsString());
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

}
