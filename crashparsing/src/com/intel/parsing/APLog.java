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

package com.intel.parsing;

public class APLog {

	public static final String TAG = "crashparsing";

	//getprop log.tag.crashparsing
	//setprop log.tag.crashparsing DEBUG
	//setprop log.tag.crashparsing WARN
	//System.setProperty("log.tag." + Log.TAG, "DEBUG");


	static public void d(String msg) {
		if (android.util.Log.isLoggable(TAG, android.util.Log.DEBUG)) {
			android.util.Log.d(TAG, msg);
		}
	}

	static public void d(String msg, Throwable tr) {
		if (android.util.Log.isLoggable(TAG, android.util.Log.DEBUG)) {
			android.util.Log.d(TAG, msg, tr);
		}
	}

	static public void e(String msg) {
		if (android.util.Log.isLoggable(TAG, android.util.Log.ERROR)) {
			android.util.Log.e(TAG, msg);
		}
	}

	static public void e(String msg, Throwable tr) {
		if (android.util.Log.isLoggable(TAG, android.util.Log.ERROR)) {
			android.util.Log.e(TAG, msg, tr);
		}
	}

	static public void i(String msg) {
		if (android.util.Log.isLoggable(TAG, android.util.Log.INFO)) {
			android.util.Log.i(TAG, msg);
		}
	}

	static public void i(String msg, Throwable tr) {
		if (android.util.Log.isLoggable(TAG, android.util.Log.INFO)) {
			android.util.Log.i(TAG, msg, tr);
		}
	}

	static public void v(String msg) {
		if (android.util.Log.isLoggable(TAG, android.util.Log.VERBOSE)) {
			android.util.Log.v(TAG, msg);
		}
	}

	static public void v(String msg, Throwable tr) {
		if (android.util.Log.isLoggable(TAG, android.util.Log.VERBOSE)) {
			android.util.Log.v(TAG, msg, tr);
		}
	}

	static public void w(String msg) {
		if (android.util.Log.isLoggable(TAG, android.util.Log.WARN)) {
			android.util.Log.w(TAG, msg);
		}
	}

	static public void w(String msg, Throwable tr) {
		if (android.util.Log.isLoggable(TAG, android.util.Log.WARN)) {
			android.util.Log.w(TAG, msg, tr);
		}
	}

	public static String getStackTraceString(Throwable tr) {
		return android.util.Log.getStackTraceString(tr);
	}

}
