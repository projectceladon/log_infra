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

import android.os.SystemProperties;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

public class DeviceInfoEntry {
	private static final String LINE_START = "\n";

	String mValue = null;
	int type = 0;
	String key = null;
	String contains = null;
	String pattern = null;
	String format = null;

	public DeviceInfoEntry(int type, String key,
		String contains, String pattern, String format) {
		this.type = type;
		this.key = key;
		this.contains = contains;
		this.pattern = pattern;
		this.format = format;
	}

	public DeviceInfoEntry(String key) {
		this.type = 0;
		this.key = key;
	}

	public DeviceInfoEntry(String key,
		String contains, String pattern, String format) {
		this.type = 1;
		this.key = key;
		this.contains = contains;
		this.pattern = pattern;
		this.format = format;
	}

	public DeviceInfoEntry(String key, String format) {
		this.type = 2;
		this.key = key;
		this.pattern = "(.*)";
		this.format = format;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		if (mValue == null) {
			switch (type) {
				case 2:
					getPropertyType();
					break;
				case 1:
					getFileType();
					break;
				case 0:
					mValue = key;
					break;
				default:
			}
		}
		return mValue;
	}

	private void getPropertyType() {
		if (key == null)
			return;

		mValue = SystemProperties.get(key, "");
		if (mValue == null || pattern == null || format == null)
			return;

		try {
			mValue = mValue.replaceFirst(pattern, format);
		} catch (RuntimeException ex) {
			return;
		}
	}

	private void getFileType() {
		InputStream inputStream;
		StringBuffer sb = new StringBuffer();
		if (key == null || key.isEmpty())
			return;
		try {
			inputStream = new FileInputStream(key);
		}
		catch (FileNotFoundException e) {
			return;
		}
		BufferedReader input = new BufferedReader(
			new InputStreamReader(inputStream));

		try {
			String s = null;
			while ((s = input.readLine()) != null) {
				if (contains != null && !s.contains(contains))
					continue;

				try {
					if (pattern != null && format != null)
						s = s.replaceFirst(pattern, format);
				} catch (RuntimeException ex) {
					continue;
				}

				sb.append(LINE_START);
				sb.append(s);
			}
			input.close();
		} catch (IOException e) {
			mValue = sb.toString();
			if (input != null) {
				try {
					input.close();
				} catch (IOException e2) {
					return;
				}
			}
			return;
		}

		/* remove first new line*/
		if (sb.length() > 0)
			sb.deleteCharAt(0);

		mValue = sb.toString();
	}

	@Override
	public String toString() {
		return getValue();
	}
}
