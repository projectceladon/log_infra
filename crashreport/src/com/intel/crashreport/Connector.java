package com.intel.crashreport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.intel.crashreport.CrashReportService.ServiceMsg;

public class Connector {

	private static final String wifiSecurity = "WPA";
	private static final int WIFI_CONNECTION_TIME_OUT = 60000;
	private static final int WIFI_AVAILABLE_TIME_OUT = 10000;
	private static final int SERVER_CONNECTION_TIME_OUT = 60000;

	private CrashReport app;
	private Context mCtx;
	private Socket mSocket;
	private BufferedReader mInputStream = null;
	private PrintWriter mOutputStream = null;
	private ObjectOutputStream mObjectOutputStream;
	private Handler serviceHandler;
	private Timer mTimer;
	private Boolean scanInProgress = false;
	private Boolean wifiWaitForConnect = false;
	private List<ScanResult> mScanResults;
	private WifiManager wm;

	public Connector(Context context) {
		mCtx = context;
		this.app = (CrashReport)mCtx.getApplicationContext();
	}

	public Connector(Context context, Handler serviceHandler) {
		mCtx = context;
		this.serviceHandler = serviceHandler;
		this.app = (CrashReport)mCtx.getApplicationContext();
	}

	public boolean isTryingToConnect(){
		return app.isTryingToConnect();
	}

	public void setTryingToConnect(Boolean s){
		app.setTryingToConnect(s);
	}

	public void setupServerConnection() throws UnknownHostException, IOException, InterruptedIOException {
		String serverAddress = PreferenceManager.getDefaultSharedPreferences(mCtx).getString("serverAddressPref", "");
		String serverPortStr = PreferenceManager.getDefaultSharedPreferences(mCtx).getString("serverPortPref", "4001");
		int serverPort = Integer.parseInt(serverPortStr);
		mSocket = new Socket();
		mSocket.setSoTimeout(SERVER_CONNECTION_TIME_OUT);
		InetSocketAddress serverAddressPort = new InetSocketAddress(serverAddress, serverPort);
		mSocket.connect(serverAddressPort, SERVER_CONNECTION_TIME_OUT);
		mInputStream = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
		mOutputStream = new PrintWriter(mSocket.getOutputStream());
		mObjectOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
		if (mSocket == null || mInputStream == null || mOutputStream == null || !mInputStream.readLine().equals("ACK")) {
			throw new IOException();
		}
		Log.d("Connector: Connected to server");
	}

	public void closeServerConnection() throws IOException {
		if (mOutputStream != null) {
			mOutputStream.println("END");
			mOutputStream.flush();
		}
		if (mSocket != null)
			mSocket.close();
		Log.d("Connector: Disconnected from server");
	}

	public Boolean getDataConnectionAvailability() {
		ConnectivityManager cm = (ConnectivityManager)mCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm.getBackgroundDataSetting()) {
			NetworkInfo net = cm.getActiveNetworkInfo();
			if (net != null) {
				if (net.isConnectedOrConnecting())
					return true;
			}
		}
		return false;
	}

	public void getWifiAvailability() {
		wm = (WifiManager)mCtx.getSystemService(Context.WIFI_SERVICE);
		mTimer = new Timer();
		mTimer.schedule(wifiTimeOut, WIFI_AVAILABLE_TIME_OUT);
		registerReceiver();
		serviceHandler.post(checkWifiStateToAvailable);
	}

	public void getConnectionState() {
		Boolean connected = false;
		wm = (WifiManager)mCtx.getSystemService(Context.WIFI_SERVICE);
		String wifiSsid = getInternalWifiSsid();
		String wmSsid = "";
		if (wm.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
			WifiInfo wInfo = wm.getConnectionInfo();
			if (wInfo != null) {
				wmSsid = wInfo.getSSID();
				if ((wmSsid != null) && (wmSsid.equals(wifiSsid)))
					connected = true;
			}
		}
		if (connected)
			serviceHandler.sendEmptyMessage(ServiceMsg.wifiConnectedInternal);
		else
			serviceHandler.sendEmptyMessage(ServiceMsg.wifiNotConnected);
	}

	public void connect() {
		wm = (WifiManager)mCtx.getSystemService(Context.WIFI_SERVICE);
		mTimer = new Timer();
		mTimer.schedule(wifiTimeOut, WIFI_CONNECTION_TIME_OUT);
		setTryingToConnect(true);
		registerReceiver();
		serviceHandler.post(checkWifiStateToConnect);
	}

	public void disconnect() {
		wm = (WifiManager)mCtx.getSystemService(Context.WIFI_SERVICE);
		String wifiSsid = getInternalWifiSsid();
		String wmSsid = "";
		if (wm.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
			WifiInfo wInfo = wm.getConnectionInfo();
			if (wInfo != null) {
				wmSsid = wInfo.getSSID();
				if ((wmSsid != null) && (wmSsid.equals(wifiSsid))) {
					disableInternalWifi();
					wm.disconnect();
				}
			}
			wm.setWifiEnabled(false);
		}
		serviceHandler.sendEmptyMessage(ServiceMsg.wifiDisconnected);
	}

	public Boolean sendEvent(Event event) {
		return sendEventSocket(event);
	}

	private void disableInternalWifi() {
		WifiConfiguration wifiConf = getWifiConfigFromConfiguredNetworks(getInternalWifiSsid());
		if (wifiConf != null)
			wm.disableNetwork(wifiConf.networkId);
	}

	private TimerTask wifiTimeOut = new TimerTask() {
		public void run() {
			Log.d("Connector: WifiTimeOut");
			scanInProgress = false;
			mCtx.unregisterReceiver(wifiStateReceiver);
			disableInternalWifi();
			wm.setWifiEnabled(false);
			if (isTryingToConnect()) {
				setTryingToConnect(false);
				wifiWaitForConnect = false;
				serviceHandler.sendEmptyMessage(ServiceMsg.wifiConnectTimeOut);
			} else {
				serviceHandler.sendEmptyMessage(ServiceMsg.internalwifiNotAvailable);
			}
		}
	};

	private BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
				Log.d("Connector: Wifi state change");
				if (isTryingToConnect()) {
					serviceHandler.removeCallbacks(checkWifiStateToConnect);
					serviceHandler.post(checkWifiStateToConnect);
				} else {
					serviceHandler.post(checkWifiStateToAvailable);
				}
			} else if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
				if (scanInProgress) {
					Log.d("Connector: Scan results available");
					mScanResults = wm.getScanResults();
					if (isTryingToConnect()) {
						serviceHandler.removeCallbacks(checkInternalWifiAvailableConnectOrRetry);
						serviceHandler.post(checkInternalWifiAvailableConnectOrRetry);
					} else {
						serviceHandler.removeCallbacks(checkInternalWifiAvailability);
						serviceHandler.post(checkInternalWifiAvailability);
					}
					scanInProgress = false;
				}
			} else if (intent.getAction().equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
				if (wifiWaitForConnect) {
					Log.d("Connector: Supplicant state Changed");
					SupplicantState supState =  intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
					if (supState.equals(SupplicantState.COMPLETED)) {
						Log.d("Connector: Supplicant state OK :" + supState.toString());
						serviceHandler.removeCallbacks(checkWifiStateToConnect);
						serviceHandler.postDelayed(checkWifiStateToConnect, 500);
						wifiWaitForConnect = false;
					} else {
						Log.d("Connector: Supplicant state :" + supState.toString());
					}
				}
			}
			if (intent.getAction().equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
				Log.d("Connector: BD INT : SUPPLICANT_CONNECTION_CHANGE_ACTION : EXTRA_SUPPLICANT_CONNECTED:" +
						intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false));
			}
			if (intent.getAction().equals(WifiManager.NETWORK_IDS_CHANGED_ACTION)) {
				Log.d("Connector: BD INT : NETWORK_IDS_CHANGED_ACTION");
			}
			if (intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)) {
				Log.d("Connector: BD INT : RSSI_CHANGED_ACTION : " + intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI, 0));
			}
		}
	};

	private void registerReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
		filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		mCtx.registerReceiver(wifiStateReceiver, filter);
	}

	private WifiConfiguration createWifiConfiguration() {
		WifiConfiguration wfc = new WifiConfiguration();
		String wifiSsid = getInternalWifiSsid();
		String wifiPass = PreferenceManager.getDefaultSharedPreferences(mCtx).getString("wifiPassPref", "");

		wfc.SSID = "\"".concat(wifiSsid).concat("\"");
		wfc.status = WifiConfiguration.Status.ENABLED;
		wfc.priority = 40;

		if (wifiSecurity.equals("OPEN")) {
			wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			wfc.allowedAuthAlgorithms.clear();
			wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		} else if (wifiSecurity.equals("WEP")) {
			wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
			wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
			wfc.wepKeys[0] = "\"".concat(wifiPass).concat("\"");
			wfc.wepTxKeyIndex = 0;
		} else if (wifiSecurity.equals("WPA")) {
			wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			wfc.preSharedKey = "\"".concat(wifiPass).concat("\"");
		}

		return wfc;
	}

	private Runnable checkWifiStateToConnect = new Runnable() {
		public void run() {
			ConnectivityManager cm = (ConnectivityManager)mCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
			int wifiState = wm.getWifiState();
			switch (wifiState) {
			case WifiManager.WIFI_STATE_DISABLED: {
				Log.d("Connector:checkWifiState: WIFI_STATE_DISABLED");
				setTryingToConnect(true);
				wm.setWifiEnabled(true);

				break;
			}
			case WifiManager.WIFI_STATE_DISABLING: {
				Log.d("Connector:checkWifiState: WIFI_STATE_DISABLING");
				setTryingToConnect(true);
				break;
			}
			case WifiManager.WIFI_STATE_ENABLED: {
				Log.d("Connector:checkWifiState: WIFI_STATE_ENABLED");
				setTryingToConnect(true);
				NetworkInfo nInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				NetworkInfo.State nState = nInfo.getState();
				if (nState == NetworkInfo.State.CONNECTED) {
					Log.d("Connector:checkWifiState: WIFI NETWORK_STATE CONNECTED");
					setTryingToConnect(false);
					scanInProgress = false;
					wifiWaitForConnect = false;
					mCtx.unregisterReceiver(wifiStateReceiver);
					mTimer.cancel();
					serviceHandler.sendEmptyMessage(ServiceMsg.wifiConnectedInternal);
				} else if (nState == NetworkInfo.State.CONNECTING) {
					Log.d("Connector:checkWifiState: WIFI NETWORK_STATE CONNECTING");
					scanInProgress = true;
					serviceHandler.postDelayed(checkWifiStateToConnect, 10);
				} else if (nState == NetworkInfo.State.DISCONNECTED) {
					Log.d("Connector:checkWifiState: WIFI NETWORK_STATE DISCONNECTED");
					scanInProgress = true;
					wm.startScan();
				} else if (nState == NetworkInfo.State.DISCONNECTING) {
					Log.d("Connector:checkWifiState: WIFI NETWORK_STATE DISCONNECTING");
					scanInProgress = true;
					wm.startScan();
				} else if (nState == NetworkInfo.State.SUSPENDED) {
					Log.d("Connector:checkWifiState: WIFI NETWORK_STATE SUSPENDED");
					scanInProgress = true;
					wm.startScan();
				} else if (nState == NetworkInfo.State.UNKNOWN) {
					Log.d("Connector:checkWifiState: WIFI NETWORK_STATE UNKNOWN");
					scanInProgress = true;
					wm.startScan();
				}
				break;
			}
			case WifiManager.WIFI_STATE_ENABLING: {
				Log.d("Connector:checkWifiState: WIFI_STATE_ENABLING");
				setTryingToConnect(true);
				break;
			}
			case WifiManager.WIFI_STATE_UNKNOWN: {
				Log.d("Connector:checkWifiState: WIFI_STATE_UNKNOWN");
				setTryingToConnect(true);
				wm.setWifiEnabled(true);
				break;
			}
			default: {
				Log.w("Connector:checkWifiState: default");
				setTryingToConnect(true);
				wm.setWifiEnabled(true);
				break;
			}
			}
		}
	};

	private Runnable checkWifiStateToAvailable = new Runnable() {
		public void run() {
			int wifiState = wm.getWifiState();
			switch (wifiState) {
			case WifiManager.WIFI_STATE_DISABLED: {
				Log.d("Connector:checkWifiAvailability: WIFI_STATE_DISABLED");
				setTryingToConnect(false);
				wm.setWifiEnabled(true);
				break;
			}
			case WifiManager.WIFI_STATE_DISABLING: {
				Log.d("Connector:checkWifiAvailability: WIFI_STATE_DISABLING");
				break;
			}
			case WifiManager.WIFI_STATE_ENABLED: {
				Log.d("Connector:checkWifiAvailability: WIFI_STATE_ENABLED");
				setTryingToConnect(false);
				scanInProgress = true;
				wm.startScan();
				break;
			}
			case WifiManager.WIFI_STATE_ENABLING: {
				Log.d("Connector:checkWifiAvailability: WIFI_STATE_ENABLING");
				setTryingToConnect(false);
				break;
			}
			case WifiManager.WIFI_STATE_UNKNOWN: {
				Log.d("Connector:checkWifiAvailability: WIFI_STATE_UNKNOWN");
				setTryingToConnect(false);
				wm.setWifiEnabled(true);
				break;
			}
			default: {
				Log.w("Connector:checkWifiAvailability: default");
				setTryingToConnect(false);
				wm.setWifiEnabled(true);
				break;
			}
			}
		}
	};

	private Boolean checkWifiAvailable(String wifiSsid) {
		if ((mScanResults != null) && (!mScanResults.isEmpty()))
			for (ScanResult result : mScanResults) {
				if (result.SSID.equals(wifiSsid)) {
					return true;
				}
			}
		return false;
	}

	private Runnable checkInternalWifiAvailableConnectOrRetry = new Runnable() {
		Boolean wifiAvailable;
		String wifiSsid;
		public void run() {
			wifiSsid = getInternalWifiSsid();
			wifiAvailable = checkWifiAvailable(wifiSsid);
			if (wifiAvailable) {
				Log.d("Connector: " + wifiSsid + " available");
				serviceHandler.post(connectToNetwork);
			} else {
				Log.d("Connector: " + wifiSsid + " not available, retry scan");
				scanInProgress = true;
				wm.startScan();
			}
		}
	};

	private Runnable checkInternalWifiAvailability = new Runnable() {
		Boolean wifiAvailable;
		String wifiSsid;
		public void run() {
			wifiSsid = getInternalWifiSsid();
			wifiAvailable = checkWifiAvailable(wifiSsid);
			if (wifiAvailable) {
				Log.d("Connector: " + wifiSsid + " available");
				mCtx.unregisterReceiver(wifiStateReceiver);
				scanInProgress = false;
				mTimer.cancel();
				disableInternalWifi();
				wm.setWifiEnabled(false);
				serviceHandler.sendEmptyMessage(ServiceMsg.internalwifiAvailable);
			} else {
				Log.d("Connector: " + wifiSsid + " not available, retry scan");
				scanInProgress = true;
				wm.startScan();
			}
		}
	};

	private String getInternalWifiSsid() {
		return PreferenceManager.getDefaultSharedPreferences(mCtx).getString("wifiSsidPref", "");
	}

	private WifiConfiguration getWifiConfigFromConfiguredNetworks(String wifiSsid) {
		wifiSsid = "\"".concat(wifiSsid).concat("\"");
		List<WifiConfiguration> mWifiConfigs = wm.getConfiguredNetworks();
		for (WifiConfiguration config : mWifiConfigs) {
			if (config.SSID.equals(wifiSsid)) {
				return config;
			}
		}
		return null;
	}

	private Runnable connectToNetwork = new Runnable() {
		public void run() {
			WifiConfiguration validConf = null;

			Log.d("Connector:connectToNetwork");
			String wifiSsid = getInternalWifiSsid();
			validConf = getWifiConfigFromConfiguredNetworks(wifiSsid);
			if (validConf != null) {
				Log.d("Connector:connectToNetwork : Wifi already configured");
				if (wm.enableNetwork(validConf.networkId, true)) {
					Log.d("Connector:connectToNetwork : Network enabled");
					wifiWaitForConnect = true;
				}
			} else {
				Log.d("Connector:connectToNetwork : Configure wifi");
				WifiConfiguration wifiConfig = createWifiConfiguration();
				int networkId = wm.addNetwork(wifiConfig);
				wm.saveConfiguration();
				if (networkId != -1)
					if (wm.enableNetwork(networkId, true)) {
						Log.d("Connector:connectToNetwork : Network enabled");
						wifiWaitForConnect = true;
					}
			}
			if (!wifiWaitForConnect) {
				Log.d("Connector:connectToNetwork : Connection fail");
				serviceHandler.post(checkWifiStateToConnect);
			}
		}
	};

	private Boolean sendEventSocket(Event event) {
		SimpleDateFormat EVENT_DF = new SimpleDateFormat("yyyy-MM-dd/HH:mm:ss");
		SimpleDateFormat EVENT_DF_OLD = new SimpleDateFormat("yy-MM-dd-HH-mm-ss");
		Date date = null;
		try {
			date = EVENT_DF.parse(event.getDate());
		} catch (ParseException e) {
			try {
				date = EVENT_DF_OLD.parse(event.getDate());
			} catch (ParseException e1) {
				date = new Date();
			}
		}

		long uptime = 0;
		String uptimeSplited[] = event.getUptime().split(":");
		if (uptimeSplited.length == 3) {
			long hours = Long.parseLong(uptimeSplited[0]);
			long minutes = Long.parseLong(uptimeSplited[1]);
			long seconds = Long.parseLong(uptimeSplited[2]);
			uptime = seconds + (60 * minutes) + (3600 * hours);
		}

		com.intel.crashtoolserver.bean.Event sEvent = new com.intel.crashtoolserver.bean.Event(
				event.getEventId(),
				event.getEventName(),
				event.getType(),
				event.getData0(),
				event.getData1(),
				event.getData2(),
				event.getData3(),
				event.getData4(),
				event.getData5(),
				date,
				event.getBuildId(),
				event.getDeviceId(),
				event.getImei(),
				uptime);

		String serverMsg;
		try {
			mObjectOutputStream.writeObject(sEvent);
			serverMsg = mInputStream.readLine();
			if (checkAck(serverMsg))
				return true;
		} catch (IOException e) {
			return false;
		}

		return false;
	}

	private Boolean checkAck(String serverMsg) {
		if (serverMsg == null || !serverMsg.equals("ACK")) {
			return false;
		}
		return true;
	}

}