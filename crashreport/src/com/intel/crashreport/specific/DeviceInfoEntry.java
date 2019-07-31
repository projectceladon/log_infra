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
