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

package com.intel.crashreport.specific;

import android.app.Activity;
import android.database.SQLException;
import android.os.Bundle;
import android.widget.TextView;

import com.intel.crashreport.database.EventDB;
import com.intel.crashreport.R;
import com.intel.crashreport.specific.ingredients.IngredientManager;

import com.intel.crashtoolserver.bean.Device;
import com.intel.crashtoolserver.bean.TracmorDevice;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONException;

public class DeviceInfoActivity extends Activity {
	private static final String newLine = "\n";
	private static final String separator = ": ";

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

	private void refreshDeviceInfo() {
		final TextView buildView = (TextView) findViewById(R.id.text_device_build_details);
		final TextView deviceView = (TextView) findViewById(R.id.text_device_hw_details);
		final TextView ingredientsView =
			(TextView) findViewById(R.id.text_device_ingredients_details);
		final TextView ingredientsUniqueView =
			(TextView) findViewById(R.id.text_device_uniques_details);

		if (buildView != null)
			buildView.setText(getBuildInformation());
		if (deviceView != null)
			deviceView.setText(getDeviceInformation());
		if (ingredientsView != null)
			ingredientsView.setText(getIngredients());
		if (ingredientsUniqueView != null)
			ingredientsUniqueView.setText(getIngredientsUnique());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_info);
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshDeviceInfo();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
