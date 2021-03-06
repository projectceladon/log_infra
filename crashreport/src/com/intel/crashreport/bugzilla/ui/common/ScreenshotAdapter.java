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

package com.intel.crashreport.bugzilla.ui.common;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Iterator;

import com.intel.crashreport.R;
import com.intel.phonedoctor.utils.FileOps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ScreenshotAdapter extends BaseAdapter{
	private Context mContext;
	private LayoutInflater mInflater;
	private static String TAG = "ScreenshotAdapter";
	private LinkedHashMap<String,Boolean> screenshotsSelected;

	static class ViewHolder {
		ImageView screenshot;
	}

	public ScreenshotAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		screenshotsSelected = new LinkedHashMap<String,Boolean>();
	}

	public void refreshScreenshotsSelected(ArrayList<String> screenshotsFiles) {
		screenshotsSelected = new LinkedHashMap<String,Boolean>();
		String files[] = getScreenshots();
		if(files == null) {
			return;
		}
		for(String file:files) {
			screenshotsSelected.put(file, screenshotsFiles.contains(file));
		}

	}

	public void refreshScreenshotsSelected() {
		String files[] = getScreenshots();
		if(files == null) {
			return;
		}
		for(String file:files) {
			if(!screenshotsSelected.containsKey(file))
				screenshotsSelected.put(file, false);
		}
	}

	public int getCount() {
		if(null != screenshotsSelected)
			return screenshotsSelected.size();
		return 0;
	}

	public int getItemPosition(String filename){
		Iterator<String> itFilename = screenshotsSelected.keySet().iterator();
		if (null != itFilename) {
			int i=0;
			while(itFilename.hasNext()){
				if(filename.equals(itFilename.next()))
					return i;
				i++;
			}
		}

		return -1;
	}

	public String[] getScreenshots() {
		File pictures = new File("/mnt/sdcard/Pictures/Screenshots");
		String[] screenshots = new String[0];
		if (pictures.exists() && pictures.isDirectory()) {
			File[] files = pictures.listFiles(new FilenameFilter(){

					public boolean accept(File dir, String filename) {
						if (filename.endsWith(".png")) {
							return true;
						}
						return false;
					}
				});
			if ( files != null ){
				Arrays.sort(files, new Comparator<File>(){
						public int compare(File f1, File f2){
							return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
						}
					});
				screenshots = new String[files.length];
				for (int i = 0; i < files.length; i++){
					screenshots[i] = files[i].getName();
				}
			}
		}
		return screenshots;
	}

	public ArrayList<String> getScreenshotsSelected() {
		ArrayList<String> screens = new ArrayList<String>();
		for(String screenshot:screenshotsSelected.keySet()) {
			if(screenshotsSelected.get(screenshot))
				screens.add(screenshot);
		}
		return screens;
	}

	public Object getItem(int position) {
		ArrayList<String> listFileName = new ArrayList<String>(screenshotsSelected.keySet());
		if(listFileName.size() > position) {
			return listFileName.get(position);
		}
		return "";
	}

	public long getItemId(int position) {
		return position;
	}

	public void updateItem(View v){
		ViewHolder child = (ViewHolder)v.getTag();
		String filename = (String)child.screenshot.getTag();
		screenshotsSelected.put(filename, !screenshotsSelected.get(filename));
		if(screenshotsSelected.get(filename))
			v.setBackgroundColor(Color.parseColor("#52e0ed"));
		else v.setBackgroundColor(Color.parseColor("#00579c"));
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.screenshot_item,null);
			holder = new ViewHolder();
			holder.screenshot = (ImageView)convertView.findViewById(R.id.screenshot_view);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder)convertView.getTag();
		}
		String filename = (String)getItem(position);
		if(holder != null && holder.screenshot != null) {
			ViewGroup.LayoutParams viewParams = convertView.findViewById(R.id.screenshot_view).getLayoutParams();
			Bitmap b;
			if (viewParams != null)
				b = FileOps.loadScaledImageFromFile("/mnt/sdcard/Pictures/Screenshots/"+filename,
							viewParams.width, viewParams.height);
			else
				b = null;

			if (b == null)
				b = Bitmap.createBitmap(1, 1, Config.ARGB_8888);

			holder.screenshot.setTag(filename);
			holder.screenshot.setImageBitmap(b);
		}
		if(screenshotsSelected.get(filename))
			convertView.setBackgroundColor(Color.parseColor("#52e0ed"));
		else convertView.setBackgroundColor(Color.parseColor("#00579c"));
		return convertView;
	}
}
