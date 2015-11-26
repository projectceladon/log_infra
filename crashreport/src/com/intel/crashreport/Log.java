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

public class Log {

	public static final String TAG = "PhoneDoctor";

	//getprop log.tag.CrashReport
	//setprop log.tag.CrashReport DEBUG
	//setprop log.tag.CrashReport WARN
	//System.setProperty("log.tag." + Log.TAG, "DEBUG");
	//import android.os.SystemProperties;
	//ifwi_version = SystemProperties.get("sys.ifwi.version", "default_value_if_property_not_found");

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
