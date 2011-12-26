package com.intel.crashreport;

import android.app.Application;

public class CrashReport extends Application {

	private Boolean serviceStarted = false;
	private Boolean tryingToConnect = false;
	private Boolean activityBounded = false;
	private Boolean wifiOnly = false;

	public boolean isServiceStarted(){
		return serviceStarted;
	}
	public void setServiceStarted(Boolean s){
		serviceStarted = s;
	}

	public boolean isTryingToConnect(){
		return tryingToConnect;
	}
	public void setTryingToConnect(Boolean s){
		tryingToConnect = s;
	}

	public boolean isActivityBounded() {
		return activityBounded;
	}
	public void setActivityBounded(Boolean s) {
		activityBounded = s;
	}

	public boolean isWifiOnly() {
		return wifiOnly;
	}
	public void setWifiOnly(Boolean s) {
		wifiOnly = s;
	}
}
