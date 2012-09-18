package com.intel.crashreport.bugzilla.ui;

import java.io.File;
import java.io.FilenameFilter;

import com.intel.crashreport.Log;
import com.intel.crashreport.R;

import android.content.Context;
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

	static class ViewHolder {
		ImageView screenshot;
	}

	public ScreenshotAdapter(Context context){
		mContext = context;
		mInflater = LayoutInflater.from(context);

	}

	public int getCount() {
		String[] screenshots = getScreenshots();
		if(null != screenshots)
			return screenshots.length;
		return 0;
	}

	public int getItemPosition(String filename){
		String[] screenshots = getScreenshots();
		if (null != screenshots) {
			for (int i=0;i<screenshots.length;i++) {
				if (screenshots[i].equals(filename)) {
					return i;
				}
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

	public Object getItem(int position) {
		String[] screenshotsFiles = getScreenshots();
		if (null != screenshotsFiles){

			if (screenshotsFiles.length > position)
				return screenshotsFiles[position];
		}
		return "";
	}

	public long getItemId(int position) {
		return position;
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
		String[] screenshotsFiles = getScreenshots();
		holder.screenshot.setImageDrawable(Drawable.createFromPath("/mnt/sdcard/Pictures/Screenshots/"+screenshotsFiles[position]));
		return convertView;
	}
}
