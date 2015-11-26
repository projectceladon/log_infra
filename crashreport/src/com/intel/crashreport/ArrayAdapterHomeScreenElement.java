/* INTEL CONFIDENTIAL
 * Copyright 2015 Intel Corporation
 *
 * The source code contained or described herein and all documents
 * related to the source code ("Material") are owned by Intel
 * Corporation or its suppliers or licensors. Title to the Material
 * remains with Intel Corporation or its suppliers and
 * licensors. The Material contains trade secrets and proprietary
 * and confidential information of Intel or its suppliers and
 * licensors. The Material is protected by worldwide copyright and
 * trade secret laws and treaty provisions. No part of the Material
 * may be used, copied, reproduced, modified, published, uploaded,
 * posted, transmitted, distributed, or disclosed in any way without
 * Intel's prior express written permission.
 *
 * No license under any patent, copyright, trade secret or other
 * intellectual property right is granted to or conferred upon you
 * by disclosure or delivery of the Materials, either expressly, by
 * implication, inducement, estoppel or otherwise. Any license under
 * such intellectual property rights must be express and approved by
 * Intel in writing.
 */

package com.intel.crashreport;

import android.app.Activity;
import android.content.Context;
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

		return convertView;
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
