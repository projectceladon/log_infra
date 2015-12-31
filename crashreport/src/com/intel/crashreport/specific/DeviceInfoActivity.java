/* INTEL CONFIDENTIAL
 * Copyright 2016 Intel Corporation
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

package com.intel.crashreport.specific;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.database.SQLException;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.*;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.intel.crashreport.database.EventDB;
import com.intel.crashreport.R;
import com.intel.crashreport.specific.ingredients.IngredientManager;
import com.intel.crashtoolserver.bean.Device;

import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

import org.json.JSONObject;
import org.json.JSONException;

public class DeviceInfoActivity extends Activity {
	private static final String newLine = "\n";
	private static final String separator = ": ";

	private static DeviceInfoActivity context = null;
	private static String glInfo = "";
	private static String[] glExtensions = null;
	private static List<DeviceInfoEntry> miscelaneous = Arrays.asList(
		new DeviceInfoEntry("ro.build.version.release", "Android version: $1"),
		new DeviceInfoEntry("ro.build.version.sdk", "API level: $1"),
		new DeviceInfoEntry("ro.hardware", "Hardware: $1"),
		new DeviceInfoEntry("ro.product.name", "Product name: $1"),
		new DeviceInfoEntry("ro.opengles.version", "OpenGL version: $1"),
		new DeviceInfoEntry(""),
		new DeviceInfoEntry("/proc/cpuinfo", "model name", "(.*):(.*)", "Processor: $2"),
		new DeviceInfoEntry("/proc/meminfo", "MemTotal", "(.*):(.*)", "Total memory: $2")
	);

	private String getBuildInformation() {
		Build build = new Build(getApplicationContext());
		build.fillBuildWithSystem();

		StringBuffer sb = new StringBuffer(512);
		Map<String, String>  items = new LinkedHashMap<String, String> ();

		items.put(getString(R.string.label_field_buildId), build.getBuildId());
		items.put(getString(R.string.label_field_fingerPrint), build.getFingerPrint());
		items.put(getString(R.string.label_field_kernelVersion), build.getKernelVersion());
		items.put(getString(R.string.label_field_userHostname),
			build.getBuildUserHostname());
		items.put(getString(R.string.label_field_os), build.getOs());

		for (String key:items.keySet())
			sb.append(newLine + key + separator + items.get(key));

		return sb.toString();
	}

	private String getIngredients() {
		StringBuffer  sb = new StringBuffer(512);
		JSONObject json;
		try {
			json = new JSONObject(Build.getIngredients());
		} catch (JSONException e) {
			return newLine + Build.getIngredients();
		}

		Iterator<String> iter = json.keys();
		while (iter.hasNext()) {
			Object value = null;
			String key = iter.next();
			try {
				value = json.get(key);
			} catch (JSONException e) {
				value = "Exception";
			}
			sb.append(newLine + key + separator + value);
		}

		return sb.toString();
	}

	private String getIngredientsUnique() {
		return newLine + IngredientManager.INSTANCE.getUniqueKeyList().toString();
	}

	private String getDeviceInformation() {
		Device dev;
		EventDB db = new EventDB(this);
		try {
			db.open();
			dev = db.fillDeviceInformation();
			db.close();
		}
		catch (SQLException e) {
			return newLine + "Error while retrieving device info";
		}

		StringBuffer sb = new StringBuffer(512);
		Map<String, String>  items = new LinkedHashMap<String, String> ();

		items.put(getString(R.string.label_field_deviceId), dev.getDeviceId());
		items.put(getString(R.string.label_field_imei), dev.getImei());
		items.put(getString(R.string.label_field_ssn), dev.getSsn());
		items.put(getString(R.string.label_field_gcmToken), dev.getGcmToken());
		items.put(getString(R.string.label_field_spid), dev.getSpid());

		for (String key:items.keySet()) {
			String value = items.get(key);
			if (value != null)
				sb.append(newLine + key + separator + value);
		}
		return sb.toString();
	}

	private String getDisplayInformation() {
		StringBuffer sb = new StringBuffer(512);
		Map<String, String>  items = new LinkedHashMap<String, String> ();

		final ActivityManager activityManager =
			(ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
		final ConfigurationInfo configurationInfo =
			activityManager.getDeviceConfigurationInfo();

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);

		items.put(getString(R.string.label_field_display_width), String.valueOf(size.x));
		items.put(getString(R.string.label_field_display_height), String.valueOf(size.y));
		items.put(getString(R.string.label_field_GL_support),
			configurationInfo.getGlEsVersion());

		for (String key:items.keySet())
			sb.append(newLine + key + separator + items.get(key));

		populateGlExtensions();

		return sb.toString() + newLine + glInfo;
	}

	private void populateGlExtensions() {
		final ExpandableListView listView = (ExpandableListView) findViewById(R.id.listGLExtensions);
		final ArrayList<String>  categories = new ArrayList<String>();
		final HashMap items = new HashMap<String, List<String>>();
		List<String> glExt = new ArrayList<String>();

		if (glExtensions != null)
			for(String s : glExtensions)
				glExt.add(s);

		categories.add(context.getString(R.string.label_field_GL_extensions));
		items.put(categories.get(0), glExt);

		ExpandableListAdapter elist = new ExpandableListAdapter(this, categories, items);
		listView.setAdapter(elist);
		listView.setOnGroupExpandListener(new OnGroupExpandListener() {
			@Override
			public void onGroupExpand(int groupPosition) {
				ListAdapter adapter = listView.getAdapter();
				listView.measure(
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

				int sum = listView.getMeasuredHeight();
				for (int i = 0; i < adapter.getCount(); i++) {
					View mView = adapter.getView(i, null, listView);
					mView.measure(
						MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
						MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
					sum += mView.getMeasuredHeight();
				}

				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)listView.getLayoutParams();
				params.height = sum;
				listView.setLayoutParams(params);
			}
		});
		listView.setOnGroupCollapseListener(new OnGroupCollapseListener() {

			@Override
			public void onGroupCollapse(int groupPosition) {
				listView.measure(
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)listView.getLayoutParams();
				params.height = listView.getMeasuredHeight();
				listView.setLayoutParams(params);
			}
		});
	}

	private String getMiscelaneousInformation() {
		StringBuffer sb = new StringBuffer(512);

		if (miscelaneous != null)
			for(final DeviceInfoEntry key:miscelaneous)
				sb.append(newLine + key);

		return sb.toString();
	}

	private void refreshDeviceInfo() {
		final TextView buildView = (TextView) findViewById(R.id.text_device_build_details);
		final TextView deviceView = (TextView) findViewById(R.id.text_device_hw_details);
		final TextView ingredientsView =
			(TextView) findViewById(R.id.text_device_ingredients_details);
		final TextView ingredientsUniqueView =
			(TextView) findViewById(R.id.text_device_uniques_details);
		final TextView displayView =
			(TextView) findViewById(R.id.text_device_display_details);
		final TextView miscelaneousView =
			(TextView) findViewById(R.id.text_device_miscelaneous_details);

		if (buildView != null)
			buildView.setText(getBuildInformation());
		if (deviceView != null)
			deviceView.setText(getDeviceInformation());
		if (ingredientsView != null)
			ingredientsView.setText(getIngredients());
		if (ingredientsUniqueView != null)
			ingredientsUniqueView.setText(getIngredientsUnique());
		if (displayView != null)
			displayView.setText(getDisplayInformation());
		if (miscelaneousView != null)
			miscelaneousView.setText(getMiscelaneousInformation());
	}

	public static class Surface extends GLSurfaceView  {
		private final SurfaceRenderer renderer;

		public Surface(Context context, AttributeSet attrs) {
			super(context, attrs);
			renderer = new SurfaceRenderer(context);
			setRenderer(renderer);
		}


		class SurfaceRenderer implements Renderer {
			public SurfaceRenderer (Context a) {
			}

			public void onSurfaceCreated(GL10 gl, EGLConfig config) {
				if (context == null)
					return;

				StringBuffer sb = new StringBuffer(512);
				Map<String, String> items = new LinkedHashMap<String, String> ();

				items.put(context.getString(R.string.label_field_GL_renderer),
					gl.glGetString(GL10.GL_RENDERER));
				items.put(context.getString(R.string.label_field_GL_vendor),
					gl.glGetString(GL10.GL_VENDOR));
				items.put(context.getString(R.string.label_field_GL_version),
					gl.glGetString(GL10.GL_VERSION));

				glExtensions = gl.glGetString(GL10.GL_EXTENSIONS).split(" ");
				for (String key:items.keySet())
					sb.append(newLine + key + separator + items.get(key));

				glInfo = sb.toString();

				context.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						context.refreshDeviceInfo();
						Surface glView = (Surface)context.findViewById(
							R.id.surfaceView);
						if (glView != null)
							glView.setVisibility(View.GONE);
					}
				});
			}

			public void onSurfaceChanged(GL10 gl, int width, int height) {
			}

			public void onDrawFrame(GL10 gl) {
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.device_info);
	}

	@Override
	public void onDestroy() {
		context = null;
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshDeviceInfo();
	}
}
