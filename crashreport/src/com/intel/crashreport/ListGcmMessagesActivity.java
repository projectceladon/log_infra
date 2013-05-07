package com.intel.crashreport;

import java.util.ArrayList;

import com.intel.crashreport.GcmMessage.GCM_ACTION;
import com.intel.crashreport.specific.EventDB;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ListGcmMessagesActivity extends Activity {

	private CrashReport app;
	private GcmMessageViewAdapter messageAdapter;
	private ListView lvEvent;
	final Context context = this;
	private GcmMessage aMessage = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_gcm_messages);
		app = (CrashReport)getApplicationContext();

		setTitle(getString(R.string.app_name)+" "+getString(R.string.app_version));

		lvEvent = (ListView) findViewById(R.id.list_gcm_messages_all);
		messageAdapter = new GcmMessageViewAdapter(getApplicationContext());
		lvEvent.setAdapter(messageAdapter);
		lvEvent.setOnItemClickListener(listener);
		NotificationMgr nMgr = new NotificationMgr(context);
		nMgr.clearGcmNotification();
	}

	private OnItemClickListener listener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
			aMessage = (GcmMessage) lvEvent.getItemAtPosition(position);
			if(!aMessage.isCancelled()) {
				AlertDialog alert = new AlertDialog.Builder(context).create();
				alert.setMessage("Title : " + aMessage.getTitle() + "\n" +
						"Text : " +  aMessage.getText() + "\n" );
				if(GCM_ACTION.GCM_NONE != aMessage.getType()) {
					alert.setButton(DialogInterface.BUTTON_POSITIVE,context.getString(R.string.gcm_list_ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							switch(aMessage.getType()){

							case GCM_APP:
							case GCM_URL:
							default:
								break;

							}
							EventDB db = new EventDB(getApplicationContext());
							boolean resultDelete = false;
							try {
								db.open();
								resultDelete = db.updateGcmMessageToCancelled(aMessage.getRowId());
								db.close();
							}
							catch (SQLException e){
								Log.e("Exception occured while generating GCM messages list :" + e.getMessage());
								resultDelete = false;
							}
							if(resultDelete)
								updateList();

						}
					});
					alert.setButton(DialogInterface.BUTTON_NEGATIVE,context.getString(R.string.alert_dialog_cancel), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
						}
					});
				}
				else {
					EventDB db = new EventDB(getApplicationContext());
					boolean resultDelete = false;
					try {
						db.open();
						resultDelete = db.updateGcmMessageToCancelled(aMessage.getRowId());
						db.close();
					}
					catch (SQLException e){
						Log.e("Exception occured while generating GCM messages list :" + e.getMessage());
						resultDelete = false;
					}
					if(resultDelete)
						updateList();
				}
				alert.show();
			}
		}
	};

	private void updateList() {
		ArrayList<GcmMessage> listMessage = new ArrayList<GcmMessage>();
		EventDB db = new EventDB(getApplicationContext());
		try {
			db.open();
			Cursor cursor;
			cursor = db.fetchAllGcmMessages();
			if (cursor != null) {
				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					GcmMessage gMessage = db.fillGCMFromCursor(cursor);
					listMessage.add(gMessage);
					cursor.moveToNext();
				}
				cursor.close();
			}
			db.close();
		}
		catch (SQLException e){
			Log.e("Exception occured while generating GCM messages list :" + e.getMessage());
		}
		if (listMessage != null)
		{
			messageAdapter.setListGcmMessage(listMessage);
			messageAdapter.notifyDataSetChanged();
		}
	}

	protected void onResume() {
		super.onResume();
		updateList();
	}

	protected void onPause() {
		Log.d("ListGcmMessagesActivity: onPause");
		super.onPause();
	}
}
