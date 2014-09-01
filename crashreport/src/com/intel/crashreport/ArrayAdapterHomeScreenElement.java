/* Phone Doctor (CLOTA)
 *
 * intialize element layout
 *
 * Author:  Nicolae Natea <nicolaex.natea@intel.com>
 *
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
