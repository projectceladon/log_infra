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

package com.intel.crashreport.specific;

import com.intel.crashreport.R;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class ExpandableListAdapter extends BaseExpandableListAdapter {
	private LayoutInflater inflater;
	private List<String> categories;
	private HashMap<String, List<String>> items;

	public ExpandableListAdapter(Context context, List<String> categories,
			HashMap<String, List<String>> items) {
		this.categories = categories;
		this.items = items;
		this.inflater = (LayoutInflater)context.getSystemService(
			Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public Object getChild(int groupPosition, int childPosititon) {
		return items.get(categories.get(groupPosition)).get(childPosititon);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, final int childPosition,
		boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null)
			convertView = inflater.inflate(R.layout.list_item, null);

		((TextView)convertView.findViewById(R.id.text_extend_item))
			.setText((String)getChild(groupPosition, childPosition));

		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return items.get(categories.get(groupPosition)).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return categories.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return categories.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
		View convertView, ViewGroup parent) {
		if (convertView == null)
			convertView = inflater.inflate(R.layout.list_group, null);

		((TextView)convertView.findViewById(R.id.text_extend_group))
			.setText((String)getGroup(groupPosition));

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}
}
