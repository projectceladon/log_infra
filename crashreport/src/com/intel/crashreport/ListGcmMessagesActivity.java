/* Phone Doctor (CLOTA)
 *
 * Copyright (C) Intel 2014
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Nicolas Benoit <nicolasx.benoit@intel.com>
 */

package com.intel.crashreport;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.intel.crashreport.GcmMessage.GCM_ACTION;
import com.intel.crashreport.specific.EventDB;
import com.intel.phonedoctor.utils.GcmUtils;

public class ListGcmMessagesActivity extends Activity {

	/**
	 * Simple enum representing the GCM filter.
	 *
	 * The values order must match the one that can be found
	 * in the array.xml resource.
	 */
	public static enum GcmFilter {
		NON_READ,
		NO_FILTER;
	}

	public static final String GCM_FILTER_PREFERENCE_EXTRA = "gcm_filter";

	private GcmMessageViewAdapter messageAdapter;
	private ListView lvEvent;
	final Context context = this;
	private GcmMessage aMessage = null;
	private GcmFilter filter = GcmFilter.NON_READ;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_gcm_messages);

		setTitle(getString(R.string.app_name)+" "+getString(R.string.app_version));

		lvEvent = (ListView) findViewById(R.id.list_gcm_messages_all);
		messageAdapter = new GcmMessageViewAdapter(getApplicationContext());
		if(lvEvent != null && messageAdapter != null) {
			lvEvent.setAdapter(messageAdapter);
			lvEvent.setOnItemClickListener(listener);
		}
		NotificationMgr.clearGcmNotification(this);

		Button markAllAsRead = (Button) findViewById(R.id.gcm_mark_all_as_read);
		if (markAllAsRead != null) {
			markAllAsRead.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					markAllAsRead();
					refresh();
				}
			});
		}
		refresh();
		buildActionBar();
	}

	private final OnItemClickListener listener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
			aMessage = (GcmMessage) lvEvent.getItemAtPosition(position);
			displayMessage(aMessage);
		}
	};

	/**
	 * Displays the given message in a dialog.
	 *
	 * @param theMessage the message to display.
	 */
	private void displayMessage(final GcmMessage theMessage) {
		if(theMessage == null) {
			Log.e("[GCM] Got <null> message to display.");
			return;
		}

		AlertDialog alert = new AlertDialog.Builder(context).create();
		alert.setTitle(theMessage.getTitle());
		StringBuffer sb = new StringBuffer("[");
		sb.append(theMessage.getDateAsString());
		sb.append("]\n\n");
		sb.append(theMessage.getText());
		alert.setMessage(sb.toString());
		alert.getWindow().setLayout(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		if(GCM_ACTION.GCM_NONE != theMessage.getType()) {
			alert.setButton(DialogInterface.BUTTON_POSITIVE,context.getString(R.string.gcm_list_ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					if(GcmEvent.INSTANCE.takeGcmAction(theMessage.getRowId(), theMessage.getType(), theMessage.getData()))
						refresh();
				}
			});
			alert.setButton(DialogInterface.BUTTON_NEGATIVE,context.getString(R.string.alert_dialog_cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
				}
			});
		} else {
			EventDB db = new EventDB(getApplicationContext());
			boolean resultDelete = false;
			try {
				db.open();
				resultDelete = db.updateGcmMessageToCancelled(theMessage.getRowId());
				db.close();
			}
			catch (SQLException e){
				Log.e("Exception occured while generating GCM messages list :" + e.getMessage());
				resultDelete = false;
			}
			if(resultDelete)
				refresh();
		}
		// Add some buttons
		alert.setButton(DialogInterface.BUTTON_NEUTRAL,"OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
			}
		});
		alert.show();
	}

	/**
	 * Marks all the GCM messages as read.
	 *
	 * @return the number of messages that have been updated.
	 */
	private int markAllAsRead() {
		GcmUtils gcmUtils = new GcmUtils(getApplicationContext());
		return gcmUtils.markAllAsRead();
	}

	/**
	 * Updates the message list.
	 */
	private void updateList(GcmUtils gcmUtils) {
		ArrayList<GcmMessage> listMessage = this.getGcmMessages(gcmUtils);
		if (listMessage != null)
		{
			messageAdapter.setListGcmMessage(listMessage);
			messageAdapter.notifyDataSetChanged();
		}
	}

	private void refresh() {
		GcmUtils gcmUtils = new GcmUtils(getApplicationContext());
		updateSummary(gcmUtils);
		updateList(gcmUtils);
	}

	/**
	 * Updates the text view used for summary informations.
	 * @return
	 */
	private void updateSummary(GcmUtils gcmUtils) {
		TextView tv = (TextView)findViewById(R.id.list_gcm_messages_title);
		if (tv == null) {
			return;
		}
		long unreadCount = gcmUtils.getUnreadMessageCount();
		long totalCount = gcmUtils.getMessageCount();
		StringBuilder sb = new StringBuilder("Unread: ");
		if(unreadCount >= 0) {
			sb.append(unreadCount);
		} else {
			sb.append("[NA]");
		}
		sb.append(" (total: ");
		if(totalCount >= 0) {
			sb.append(totalCount);
		} else {
			sb.append("[NA]");
		}
		sb.append(")");
		tv.setText(sb.toString());
	}

	private ArrayList<GcmMessage> getGcmMessages(GcmUtils gcmUtils) {
		ArrayList<GcmMessage> messageList = null;
		switch(this.filter) {
		case NO_FILTER:
			messageList = gcmUtils.getList();
			break;
		case NON_READ:
			messageList = gcmUtils.getNonRead();
			break;
		}
		return messageList;
	}

	@Override
	protected void onResume() {
		super.onResume();
		NotificationMgr.clearGcmNotification(this);
		refresh();
		Intent incomingIntent = getIntent();
		// Check whether we arrived here from a notification or not
		if(incomingIntent.hasExtra(GcmMessage.GCM_ORIGIN)) {
			int origin = incomingIntent.getIntExtra(GcmMessage.GCM_ORIGIN, -1);
			int rowId = incomingIntent.getIntExtra(GcmMessage.GCM_ROW_ID, -1);
			if(NotificationMgr.NOTIF_CRASHTOOL == origin && rowId != -1) {
				// In that case, display the latest message
				// which is actually at index 0
				GcmMessage lastMessage = (GcmMessage) lvEvent.getItemAtPosition(0);
				displayMessage(lastMessage);
				refresh();
				NotificationMgr.clearGcmNotification(this);
			}
		}
	}

	@Override
	protected void onPause() {
		Log.d("ListGcmMessagesActivity: onPause");
		super.onPause();
	}

	private void buildActionBar() {
		SpinnerAdapter gcmFilterSpinner = ArrayAdapter.createFromResource(
				this,
				R.array.gcmFilter,
	            android.R.layout.simple_spinner_dropdown_item);
		ActionBar actionBar = getActionBar();
		actionBar.setTitle("GCM");
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setListNavigationCallbacks(gcmFilterSpinner, gcmFilterListener);
		ApplicationPreferences prefs = new ApplicationPreferences(getApplicationContext());
		GcmFilter thefilter = prefs.getGcmFilter();
		this.filter = thefilter;
		actionBar.setSelectedNavigationItem(this.filter.compareTo(GcmFilter.NON_READ));
	}

	private final OnNavigationListener gcmFilterListener = new OnNavigationListener() {
		@Override
		  public boolean onNavigationItemSelected(int position, long itemId) {
			GcmFilter selectedValue = GcmFilter.NO_FILTER;
			if ((position >= 0) && (position < GcmFilter.values().length)) {
				selectedValue = GcmFilter.values()[position];
			}
			ApplicationPreferences prefs = new ApplicationPreferences(getApplicationContext());
			prefs.setGcmFilter(selectedValue);
			filter = selectedValue;
			refresh();
		    return true;
		  }
	};
}
