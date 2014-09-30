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

package com.intel.crashreport.specific;

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

import com.intel.crashreport.ApplicationPreferences;
import com.intel.crashreport.Log;
import com.intel.crashreport.NotificationMgr;
import com.intel.crashreport.R;
import com.intel.crashreport.R.array;
import com.intel.crashreport.R.id;
import com.intel.crashreport.R.layout;
import com.intel.crashreport.R.string;
import com.intel.crashreport.specific.GcmMessage.GCM_ACTION;

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

                setTitle("");

		lvEvent = (ListView) findViewById(R.id.list_gcm_messages_all);
		messageAdapter = new GcmMessageViewAdapter(getApplicationContext());
		if(lvEvent != null && messageAdapter != null) {
			lvEvent.setAdapter(messageAdapter);
			lvEvent.setOnItemClickListener(listener);
		}
		GCMNotificationMgr.clearGcmNotification(this);

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
		// Check that the message is not null
		if(theMessage == null) {
			Log.e("[GCM] Got <null> message to display.");
			return;
		}

		// Create the dialog that will be used to display the message
		AlertDialog alert = this.buildMessageDialog(theMessage);

		// Retrieve the action
		GCM_ACTION action = theMessage.getType();

		// If there was nothing else to do than display the message,
		// we may consider if as read.
		if(GCM_ACTION.GCM_NONE == action) {
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

		// Show the dialog
		alert.show();
	}

	/**
	 * Returns a fully configured <code>AlertDialog</code> for the given
	 * <code>GcmMessage</code>.
	 *
	 * @param aMessage the message to display.
	 *
	 * @return a fully configured <code>AlertDialog</code> instance.
	 */
	private AlertDialog buildMessageDialog(final GcmMessage aMessage) {
		// Initialize the variables used for the dialog
		GCM_ACTION action = aMessage.getType();
		String data = aMessage.getData();
		String buttonLabel = "No action";
		String negativeButtonLabel = context.getString(R.string.alert_dialog_cancel);
		String textFooter = null;
		StringBuffer sb = new StringBuffer();
		// Create a simple dialog
		AlertDialog alert = new AlertDialog.Builder(context).create();
		// If the notification may contain some user interactions
		if(GCM_ACTION.GCM_NONE != action) {
			// Compute the labels and texts to display in the dialog
			if(GCM_ACTION.GCM_APP.equals(action)) {
				buttonLabel = "Start application";
				sb.append("[app: ");
				sb.append(data);
				sb.append("]");
				textFooter = sb.toString();
			} else if (GCM_ACTION.GCM_URL.equals(action)) {
				buttonLabel = "Open URL";
				sb.append("[url: ");
				sb.append(data);
				sb.append("]");
				textFooter = sb.toString();
			} else if (GCM_ACTION.GCM_PHONE_DOCTOR.equals(action)) {
				if(GcmMessage.GCM_KRATOS_START.equals(data)) {
					buttonLabel = "Start MPM";
				} else {
					buttonLabel = "Stop MPM";
				}
			}
			// Add a button that will trigger the action
			alert.setButton(DialogInterface.BUTTON_POSITIVE, buttonLabel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					if(GcmEvent.INSTANCE.takeGcmAction(
							aMessage.getRowId(),
							aMessage.getType(),
							aMessage.getData())) {
						refresh();
					}
				}
			});
		} else {
			negativeButtonLabel = "OK";
		}
		// Compute the text content
		alert.setTitle(aMessage.getTitle());
		sb = new StringBuffer("[");
		sb.append(aMessage.getDateAsString());
		sb.append("]\n\n");
		sb.append(aMessage.getText());
		if(textFooter != null) {
			sb.append("\n\n");
			sb.append(textFooter);
		}
		alert.setMessage(sb.toString());
		// Set the layout
		alert.getWindow().setLayout(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		// Add a button for 'no action'
		alert.setButton(DialogInterface.BUTTON_NEGATIVE, negativeButtonLabel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				GcmUtils gcmUtils = new GcmUtils(getApplicationContext());
				gcmUtils.markAsRead(aMessage.getRowId());
				refresh();
			}
		});
		// Return the dialog
		return alert;
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
		GCMNotificationMgr.clearGcmNotification(this);
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
				GCMNotificationMgr.clearGcmNotification(this);
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
				getActionBar().getThemedContext(),
				R.array.gcmFilter,
	            R.layout.spinner_dropdown_item);
		ActionBar actionBar = getActionBar();
		actionBar.setTitle("GCM");
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setListNavigationCallbacks(gcmFilterSpinner, gcmFilterListener);
		ApplicationPreferences prefs = new ApplicationPreferences(getApplicationContext());
		GcmFilter thefilter = getGcmFilter(prefs);
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
			setGcmFilter(selectedValue,prefs);
			filter = selectedValue;
			refresh();
		    return true;
		  }
	};
	
	public ListGcmMessagesActivity.GcmFilter getGcmFilter(ApplicationPreferences aPref) {
		ListGcmMessagesActivity.GcmFilter defaultFilter = ListGcmMessagesActivity.GcmFilter.NON_READ;
		String filterAsString = aPref.getGcmFilterAsStr(defaultFilter.toString()) ;
		ListGcmMessagesActivity.GcmFilter filter = ListGcmMessagesActivity.GcmFilter.valueOf(filterAsString);
		Log.d("[GCM] Returning GCM filter from preferences: " + filter);
		return filter;
	}

	public void setGcmFilter(ListGcmMessagesActivity.GcmFilter filter, ApplicationPreferences aPref) {
		Log.d("[GCM] Changing preference for GCM filter to: " + filter);
		aPref.setGcmFilterAsStr(filter.toString());
	}
}
