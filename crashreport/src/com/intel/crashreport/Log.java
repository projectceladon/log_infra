package com.intel.crashreport;

public class Log {

	public static final String TAG = "CrashReport";

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

	static public void e(String msg) {
		if (android.util.Log.isLoggable(TAG, android.util.Log.ERROR)) {
			android.util.Log.e(TAG, msg);
	    }
	}

	static public void i(String msg) {
		if (android.util.Log.isLoggable(TAG, android.util.Log.INFO)) {
			android.util.Log.i(TAG, msg);
	    }
	}

	static public void v(String msg) {
		if (android.util.Log.isLoggable(TAG, android.util.Log.VERBOSE)) {
			android.util.Log.v(TAG, msg);
	    }
	}

	static public void w(String msg) {
		if (android.util.Log.isLoggable(TAG, android.util.Log.WARN)) {
			android.util.Log.w(TAG, msg);
	    }
	}

}