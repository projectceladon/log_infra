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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;

public class GcmMessage {

	public static final String GCM_NONE_LABEL = "none";
	public static final String GCM_APP_LABEL = "app";
	public static final String GCM_URL_LABEL = "url";
	public static final String GCM_PHONE_DOCTOR_LABEL = "phone_doctor";

	public static final String GCM_EXTRA_TITLE = "notification_title";
	public static final String GCM_EXTRA_TEXT = "notification_text";
	public static final String GCM_EXTRA_TYPE = "notification_action_type";
	public static final String GCM_EXTRA_DATA = "notification_action_data";
	public static final String GCM_ROW_ID = "rowId";

	/*
	 * Phone Doctor specific extras
	 */
	public static final String GCM_ORIGIN = "origin";

	private final static SimpleDateFormat EVENT_DF = new SimpleDateFormat("yyyy-MM-dd/HH:mm:ss");

	private static EnumMap<GCM_ACTION,String> messageTypesList = new EnumMap<GCM_ACTION,String>(GCM_ACTION.class);
	static{
		messageTypesList.put(GCM_ACTION.GCM_NONE, GCM_NONE_LABEL);
		messageTypesList.put(GCM_ACTION.GCM_URL, GCM_URL_LABEL);
		messageTypesList.put(GCM_ACTION.GCM_APP, GCM_APP_LABEL);
		messageTypesList.put(GCM_ACTION.GCM_PHONE_DOCTOR, GCM_PHONE_DOCTOR_LABEL);
	}

	public static enum GCM_ACTION{
		GCM_NONE,
		GCM_URL,
		GCM_APP,
		GCM_PHONE_DOCTOR
	};
	private String title = "";
	private String text = "";
	private String data = "";
	private String icon = "";
	private int rowId;
	private GCM_ACTION type;
	private boolean cancelled;
	private Date date = null;

	public GcmMessage(int id,String sTitle, String sText, String sType, String sData, boolean bCancelled, Date dDate) {
		this(id,sTitle,sText,sType,bCancelled);

		if(type != GCM_ACTION.GCM_NONE)
			data = sData;
		date = dDate;

	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getDateAsString() {
		return EVENT_DF.format(date);
	}

	public GcmMessage(int id,String sTitle, String sText, String sType, String sData, boolean bCancelled) {
		this(id,sTitle,sText,sType,bCancelled);

		if(type != GCM_ACTION.GCM_NONE)
			data = sData;

	}

	public GcmMessage(int id,String sTitle, String sText, String sType,boolean bCancelled) {

		title = sTitle;
		text = sText;
		type = GCM_ACTION.GCM_NONE;
		for(GCM_ACTION actionType:GCM_ACTION.values()){
			if(sType.equals(messageTypesList.get(actionType))) {
				type = actionType;
				break;
			}
		}
		rowId = id;
		cancelled = bCancelled;

	}

	public GcmMessage() {
		title = "";
		text = "";
		data = "";
		icon = "";
		type = GCM_ACTION.GCM_NONE;
		rowId = -1;
		cancelled = false;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public GCM_ACTION getType() {
		return type;
	}

	public void setType(GCM_ACTION type) {
		this.type = type;
	}

	public int getRowId() {
		return rowId;
	}

	public void setRowId(int rowId) {
		this.rowId = rowId;
	}

	public static boolean typeExist(String sType) {
		return messageTypesList.containsValue(sType);
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean bCancelled) {
		cancelled = bCancelled;
	}

	@Override
	public String toString() {
		String className = this.getClass().getSimpleName();
		if(className == null) {
			className = "com.intel.crashreport.GcmMessage";
		}
		StringBuilder sb = new StringBuilder(className);
		sb.append("[id=");
		sb.append(this.getRowId());
		sb.append(",cancelled=");
		sb.append(this.isCancelled());
		sb.append(",title=");
		sb.append(this.getTitle());
		sb.append(",text=");
		sb.append(this.getText());
		sb.append(",type=");
		sb.append(this.getType());
		sb.append(",date=");
		sb.append(this.getDateAsString());
		sb.append("]");
		return sb.toString();
	}



}
