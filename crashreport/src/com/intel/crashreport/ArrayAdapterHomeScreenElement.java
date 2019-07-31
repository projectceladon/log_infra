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

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ImageView;
import java.util.List;
import java.util.Comparator;
import java.util.Collections;

public class ArrayAdapterHomeScreenElement extends ArrayAdapter<HomeScreenElement> {

	private Context context;
	private int resourceId;
        private List<HomeScreenElement> data1;

	public ArrayAdapterHomeScreenElement(Context context, int resourceId, List<HomeScreenElement> data1) {
		super(context, resourceId, data1);

		this.resourceId = resourceId;
		this.context = context;
		this.data1 = data1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if(convertView==null)
			convertView = ((Activity) context).getLayoutInflater().inflate(resourceId, parent, false);

		TextView tv = (TextView) convertView.findViewById(R.id.textViewEntry);
		tv.setText(data1.get(position).getElementName());
		tv.setTag(data1.get(position).getElementID());

		ImageView iv = (ImageView) convertView.findViewById(R.id.CrashReport_imageViewEntry1);
		iv.setImageResource(data1.get(position).getElementImageResource());
		if (!data1.get(position).isEnabled()) {
			final int disabledColor = context.getResources().getColor(R.color.disabled_icon);
			iv.setColorFilter(disabledColor, Mode.SRC_ATOP);
			tv.setTextColor(disabledColor);
			convertView.setEnabled(false);
		}

		return convertView;
	}

	public boolean isEnabled(int position) {
		return data1.get(position).isEnabled();
	}

	public void add(HomeScreenElement album) {
		data1.add(album);

		Collections.sort(data1, new Comparator<HomeScreenElement>() {
			@Override public int compare(HomeScreenElement e1, HomeScreenElement e2) {
				return e1.getElementPosition() - e2.getElementPosition();
			}
		});

		notifyDataSetChanged();
	}

	public HomeScreenElement getItem(int position) {
		return data1.get(position);
	}
}
