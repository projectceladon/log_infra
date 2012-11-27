package com.intel.crashreport.bugzilla.ui;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.intel.crashreport.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ScreenshotAdapter extends BaseAdapter{
	private Context mContext;
	private LayoutInflater mInflater;
	private static String TAG = "ScreenshotAdapter";
	private HashMap<String,Boolean> screenshotsSelected;

	static class ViewHolder {
		ImageView screenshot;
	}

	public ScreenshotAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		screenshotsSelected = new HashMap<String,Boolean>();
	}

	public void refreshScreenshotsSelected(ArrayList<String> screenshotsFiles) {
		screenshotsSelected = new HashMap<String,Boolean>();
		String files[] = getScreenshots();
		for(String file:files) {
			screenshotsSelected.put(file, screenshotsFiles.contains(file));
		}

	}

	public void refreshScreenshotsSelected() {
		String files[] = getScreenshots();
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
			screenshots = pictures.list(new FilenameFilter(){

				public boolean accept(File dir, String filename) {
					if (filename.endsWith(".png")) {
						return true;
					}
					return false;
				}

			});
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
		if(listFileName != null && listFileName.size() > position) {
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
		holder.screenshot.setTag(filename);
		holder.screenshot.setImageDrawable(Drawable.createFromPath("/mnt/sdcard/Pictures/Screenshots/"+filename));
		if(screenshotsSelected.get(filename))
			convertView.setBackgroundColor(Color.parseColor("#52e0ed"));
		else convertView.setBackgroundColor(Color.parseColor("#00579c"));
		return convertView;
	}
}
