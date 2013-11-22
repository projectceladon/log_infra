/* Phone Doctor (CLOTA)
 *
 * Copyright (C) Intel 2012
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Charles-Edouard VIDOINE <charles.edouardx.vidoine@intel.com>
 */
package com.intel.crashreport.specific;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;

import android.content.Context;
import android.database.SQLException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemProperties;

import com.intel.crashreport.ApplicationPreferences;
import com.intel.crashreport.CrashReport;
import com.intel.crashreport.Log;
import com.intel.phonedoctor.Constants;

/**
  *
  * @brief : This class represents the PhoneDoctor Status. It's a field of the events table that represents
  * the state of Phone Doctor. It's composed by a 50 characters String which represents the informations below:
  * Check if an modem coredump is linked with the MPanic or not.
  * Check if the EventId in the database is the same that the EventId in the
  * crashfile.
  * Check if the dropbox is full or not.
  * Check if crashlogger daemon is still running or not.
  * Check if apklogfs is running or not.
  * Check if there is enough space available on the /logs partition.
  * Check if the PhoneDoctor database is available.
  * Check if a sdcard is present on the device and if it is available.
  * Test the value of the persist.core.enabled property.
  * Check if the history_event file is not corrupted.
  * Check if logs have to be upload or not.
  * Check if logs have to be upload only over a Wifi connection.
  * Number of uptime since the last SWUPDATE event.
  * Check if the device is connected to an Wifi network or not.
  * Check if the device is connected to a 3G network.
  *
  * The PDStatus class computes the PDStatus of an event.
  * @author Charles-Edouard VIDOINE <charles.edouardx.vidoine@intel.com>
  *
 **/
public enum PDStatus {

	INSTANCE;


	/* List of the PDStatus component */
	private static enum STATUS_LABEL{
		MPANIC (PDSTATUS_TIME.INSERTION_TIME,1,new PDStatusInterface(){
			/**
			 * @brief Check if a modem coredump is linked with the MPanic or not. Only available for a MPanic CRASH Event.
			 * @return String :  1 MPanic with modem coredump, M MPanic without modem coredump, x else.
			 **/
			@Override
			public String computeValue() {
				String result = "x";

				if(event.getEventName().equals("CRASH") && event.getType().equals("MPANIC") ) {
					if (event.getCrashDir() != null) {
						File crashDir = new File(event.getCrashDir());
						if(crashDir != null && crashDir.exists() && crashDir.isDirectory()){
							String lmcoredump[] = new String[0];
							lmcoredump = crashDir.list(new FilenameFilter(){

								@Override
								public boolean accept(File dir, String filename) {
									if (filename.startsWith("cd") && filename.endsWith(".tar.gz")) {
										return true;
									}
									return false;
								}

							});
							if(lmcoredump.length > 0)
								result = "1";
							else result = "M";
						}
					}
				}
				return result;
			}}),
		GOOD_EVENTID (PDSTATUS_TIME.BOTH_TIME, 1, new PDStatusInterface(){
			/**
			 * @brief Check if the EventId in the database is the same that the EventId in the crashfile.
			 * @return String : 1 same Ids, I different Ids, x else.
			 */
			@Override
			public String computeValue() {
				String result = "x";
				if(event.getEventName().equals("CRASH") && event.getCrashDir() != null) {
					try {
						CrashFile crashfile = new CrashFile(event.getCrashDir(), false);
						result = crashfile.getEventId().equals(event.getEventId())?"1":"I";
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}

				}
				return result;
			}}),
		FULL_DROPBOX (PDSTATUS_TIME.INSERTION_TIME, 1, new PDStatusInterface(){

			/**
			 * @brief Check if the dropbox is full or not with the help of the persist.sys.crashlogd.mode property value.
			 * @return String : F full dropbox, 0 else.
			 */
			@Override
			public String computeValue() {
				String result = "x";
				result = SystemProperties.get("persist.sys.crashlogd.mode","x");
				if(result.equals("nominal"))
					result = "0";
				else if (!result.equals("x"))
					result = "F";
				return result;
			}
		}),
		CRASHLOGD_RUN (PDSTATUS_TIME.UPLOAD_TIME, 1, new PDStatusInterface(){

			/**
			 * @brief Check if crashlogger daemon is still running or not with the help of init.svc.crashlogd value.
			 * @return String : 1 crashlogd is running, C else, x at the database insertion time.
			 */
			@Override
			public String computeValue() {
				String result;
				result = SystemProperties.get("init.svc.crashlogd","x");
				if(result.equals("running"))
					result = "1";
				else if (!result.equals("x"))
					result = "C";
				return result;
			}
		}),
		APKLOGFS_RUN (PDSTATUS_TIME.BOTH_TIME, 1, new PDStatusInterface() {
			/**
			 * @brief Check if apklogfs is running or not with the help of init.svc.apk_logfs value.
			 * @return String : 1 apklogfs is running, A else.
			 **/
			@Override
			public String computeValue() {
				String result;
				result = SystemProperties.get("init.svc.apk_logfs","x");
				if(result.equals("running"))
					result = "1";
				else if (!result.equals("x"))
					result = "A";
				return result;
			}
		}),
		LOGS_PARTITION_SIZE (PDSTATUS_TIME.INSERTION_TIME, 1, new PDStatusInterface(){
			/**
			 * @brief Check if there is enough space available on the /logs partition.
			 * @return String : 1 left more than 20Mb, L else.
			 */
			@Override
			public String computeValue() {
				String result = "x";
				File logsDir = new File("/logs");
				if(logsDir.exists() && logsDir.isDirectory()){
					if(logsDir.getUsableSpace() > Constants.LOGS_CRITICAL_SIZE)
						result = "1";
					else result = "L";
				}
				return result;
			}
		}),
		DB_OK (PDSTATUS_TIME.BOTH_TIME, 1, new PDStatusInterface(){
			/**
			 * @brief Check if the PhoneDoctor database is available.
			 * @return String : 1 database available, D else.
			 */
			@Override
			public String computeValue() {
				String result = "D";
				EventDB db = new EventDB(context);

				if( null != db) {
					try{
						db.open();
						db.close();
						result = "1";


					}catch(SQLException e){
						result = "D";
					}
				}
				return result;
			}
		}),
		SDCARD (PDSTATUS_TIME.INSERTION_TIME, 1, new PDStatusInterface(){
			/**
			 * @brief Check if a sdcard is present on the device and if it is available.
			 * @return String : 1 a sdcard is available, S else.
			 */
			@Override
			public String computeValue() {
				String result = "x";
				File sdcardDir = new File("/storage/sdcard_ext");
				if((null != sdcardDir) && sdcardDir.exists() && sdcardDir.isDirectory() && (sdcardDir.getTotalSpace() > 0))
					result = "1";
				else result = "S";
				return result;
			}
		}),
		CORE_ENABLED (PDSTATUS_TIME.INSERTION_TIME, 1, new PDStatusInterface(){
			/**
			 * @brief Test the value of the persist.core.enabled property.
			 * @return String : 1 or P (property set to 0), depends on the value of the property.
			 */
			@Override
			public String computeValue() {
				String result;
				result = SystemProperties.get("persist.core.enabled", "x");
				if(!result.equals("1"))
					result = "P";
				return result;
			}
		}),
		HISTORY_EVENT_CORRUPTED (PDSTATUS_TIME.INSERTION_TIME, 1, new PDStatusInterface(){
			/**
			 * @brief Check if the history_event file is not corrupted.( bad ended file, non-terminated line, data are missing).
			 * @return String : 1 history_event non corrupted, H else.
			 */
			@Override
			public String computeValue() {
				String result = "x";
				if(historyEventCorrupted)
					result = "H";
				else result = "1";
				return result;
			}
		}),
		UPLOAD_LOG_ON (PDSTATUS_TIME.UPLOAD_TIME, 1, new PDStatusInterface(){
			/**
			 * @brief Check if logs have to be upload or not. The test uses the preference linked with Upload logs checkbox on PhoneDoctor's UI.
			 * @return String : 1 if logs have to be upload, U else, x at the event database insertion time.
			 */
			@Override
			public String computeValue() {
				String result = "x";
				ApplicationPreferences prefs = new ApplicationPreferences(context);
				result = prefs.isCrashLogsUploadEnable()?"1":"U";
				return result;
			}
		}),
		BIG_DATA (PDSTATUS_TIME.UPLOAD_TIME, 1, new PDStatusInterface(){
			/**
			 * @brief Data are too big(>10M) or not to be uploaded without wifi.
			 * @return String : B if data are too big, 0 else,x at the event database insertion time
			 * or if there is no logs.
			 */
			@Override
			public String computeValue() {
				String result = "x";
				if(event.getCrashDir() != null){
					if(!event.getCrashDir().isEmpty() && (event.getLogsSize() >= Constants.WIFI_LOGS_SIZE))
						result = "B";
					else result = "0";
				}
				return result;
			}
		}),
		MTS_RUNNING (PDSTATUS_TIME.INSERTION_TIME, 1, new PDStatusInterface(){
			/**
			 * @brief MTS is running or not.
			 * @return String : T if MTS is not running, 1 else,x if MTS doesn't exists.
			 */
			@Override
			public String computeValue() {
				String result = "x";
				result = SystemProperties.get("init.svc.mtsp","x");
				if(result.equals("running"))
					result = "1";
				else if (!result.equals("x"))
					result = "T";
				else {
					result = SystemProperties.get("init.svc.mtso","x");
					if(result.equals("running"))
						result = "1";
					else if (!result.equals("x"))
						result = "T";
				}
				return result;
			}
		}),
		FW_VERSION (PDSTATUS_TIME.INSERTION_TIME, 1, new PDStatusInterface(){
			/**
			 * @brief Test if Firmware versions are good or not.
			 * @return String : V if one of the firmware version is wrong, 0 else.
			 */
			@Override
			public String computeValue() {
				String result = "x";
				CrashReport app = (CrashReport)context;
				result = app.getMyBuild().testVersion()?"0":"V";
				return result;
			}
		}),
		WIFI_ONLY (PDSTATUS_TIME.BOTH_TIME, 1, new PDStatusInterface(){
			/**
			 * @brief Check if logs have to be upload only over a Wifi connection.
			 * The test uses the preference linked with Wifi only checkbox on PhoneDoctor's UI.
			 * @return String : @ if logs have to be upload over Wifi, 0 else, x at the event database insertion time.
			 */
			@Override
			public String computeValue() {
				String result = "x";
				ApplicationPreferences prefs = new ApplicationPreferences(context);
				result = prefs.isWifiOnlyForEventData()?"@":"0";
				return result;
			}
		}),
		UPTIME (PDSTATUS_TIME.INSERTION_TIME, 4, new PDStatusInterface(){
			/**
			 * @brief Number of uptime since the last SWUPDATE event.
			 * @return String : 0 to 9999.
			 */
			@Override
			public String computeValue() {
				String result = "xxxx";
				EventDB db = new EventDB(context);

				if( null != db) {
					try{
						db.open();
						result = String.format("%04d", db.getUptimeNumber());
						db.close();

					}catch(SQLException e){
						result = "xxxx";
					}
				}
				return result;
			}
		}),
		VARIANT (PDSTATUS_TIME.INSERTION_TIME, 1, new PDStatusInterface() {
			/**
			 * @brief Returns an indicator for the modem variant.
			 * @return A character indicating the modem type:
			 * <ul>
			 * <li><code>x</code> if the modem value is a </li>
			 * <li><code>x</code> otherwise</li>
			 * </ul>
			 */
			@Override
			public String computeValue() {
				String result = "Z";
				String variant = event.getVariant();
				if(variant != null && variant.endsWith("7260")) {
					result = "Y";
				} else if(variant != null && variant.endsWith("7160")) {
					result = "1";
				}
				return result;
			}
		}),
		BYTE30 (PDSTATUS_TIME.BOTH_TIME, 1, new PDStatusInterface(){

			@Override
			public String computeValue() {
				return " ";
			}
		}),
		WIFI (PDSTATUS_TIME.BOTH_TIME, 3, new PDStatusInterface(){
			/**
			 * @brief Check if the device is connected to an Wifi network or not.
			 * @return String : W:1 if the device is connected to an Wifi network only at the database insertion time.
			 *         W:2 if the device is connected to an Wifi network only at the upload time.
			 *         W:3 if the device is connected to an Wifi network at both database insertion time and
			 *         upload time.
			 *         W:0 if the device has no Wifi network available at both database insertion time and
			 *         upload time.
			 */
			@Override
			public String computeValue() {
				String result = "W:";
				try {
					ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
					result = result.concat(networkInfo.isConnected()?"1":"0");
				}
				catch(NullPointerException e) {
					result += "x";
				}
				return result;
			}
		}),
		BYTE26 (PDSTATUS_TIME.BOTH_TIME, 1, new PDStatusInterface(){

			@Override
			public String computeValue() {
				return " ";
			}
		}),
		DATA (PDSTATUS_TIME.BOTH_TIME, 4, new PDStatusInterface(){
			/**
			 * @brief Check if the device is connected to a 3G network.
			 * @return String : 3G:1 if the device is connected to an 3G network only at the database insertion time.
			 *                  3G:2 if the device is connected to an 3G network only at the upload time.
			 *                  3G:3 if the device is connected to an 3G network at both database insertion time and
			 *                  upload time.
			 *                  3G:0 if the device has no 3G network available at both database insertion time and
			 *                  upload time.
			 */
			@Override
			public String computeValue() {
				String result = "3G:";
				try{
					ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
					result = result.concat(networkInfo.isConnected()?"1":"0");
				}
				catch(NullPointerException e){
					result = result.concat("x");
				}
				return result;
			}
		});

		/* The moment when the component must be computed*/
		private final PDSTATUS_TIME whenCompute;
		/* lenght of the component */
		private final int charactersNumber;

		private static Event event;
		private static Context context;
		private static boolean historyEventCorrupted;

		private PDStatusInterface computeValue;

		public PDSTATUS_TIME getWhenCompute() {
			return whenCompute;
		}

		public int getCharactersNumber() {
			return charactersNumber;
		}

		public String computeValue() {
			return computeValue.computeValue();
		}

		public static void setCurrentEvent(Event e) {
			event = e;
		}

		public static void setCurrentContext(Context c) {
			context = c;
		}

		public static void setHistoryEventCorrupted(boolean corrupted) {
			historyEventCorrupted = corrupted;
		}

		STATUS_LABEL(PDSTATUS_TIME when, int nb, PDStatusInterface function){
			whenCompute = when;
			charactersNumber = nb;
			computeValue = function;
		}


	};

	public static enum PDSTATUS_TIME{INSERTION_TIME,UPLOAD_TIME,BOTH_TIME};

	private String pdStatus = "";
	private Event event;
	private Context context;
	private PDSTATUS_TIME moment;
	private boolean historyEventCorrupted = false;

	private interface PDStatusInterface {
		public String computeValue();
	}

	/**
	 * @brief Compute the PDStatus field
	 * @param Event e : the event from which the PDStatus is computed
	 * @param PDSATUS_TIME m : moment when the PDStatus is computed
	 * @return String : the PDStatus
	 */
	public synchronized String computePDStatus(Event e, PDSTATUS_TIME m) {

		event = e;
		moment = m;

		int count = 0;
		String result;
		pdStatus = "";

		STATUS_LABEL.setCurrentEvent(e);

		for(STATUS_LABEL label:STATUS_LABEL.values()) {

			if(label.getWhenCompute() == moment || label.getWhenCompute() == PDSTATUS_TIME.BOTH_TIME) {
				if(m == PDSTATUS_TIME.UPLOAD_TIME && (label == STATUS_LABEL.WIFI || label == STATUS_LABEL.DATA)) {
					result = label.computeValue();
					if(event.getPdStatus().length() >= (count + label.getCharactersNumber())) {
						if(event.getPdStatus().substring(count, count+label.getCharactersNumber()).endsWith(":0")
								|| event.getPdStatus().substring(count, count+label.getCharactersNumber()).endsWith(":x")) {
							if(result.endsWith(":1")) result = result.replaceFirst(":1", ":2");
						}
						if(event.getPdStatus().substring(count, count+label.getCharactersNumber()).endsWith(":1")) {
							if(result.endsWith(":1")) result = result.replaceFirst(":1", ":3");
						}
					}
					pdStatus = pdStatus.concat(result);
				}
				else pdStatus = pdStatus.concat(label.computeValue());
			}
			// at insertion time for a component computed at upload || at upload time with component computed at insertion but with a bad previous value
			else if(event.getPdStatus().length() < (count + label.getCharactersNumber()) || PDSTATUS_TIME.UPLOAD_TIME == label.getWhenCompute())
				pdStatus = pdStatus.concat(fillUnknownValue(label.getCharactersNumber()));
			// during upload for a component computed at insertion with a correct previous value
			else pdStatus = pdStatus.concat(event.getPdStatus().substring(count, count+label.getCharactersNumber()));
			count += label.getCharactersNumber();

		}
		Log.d("PDStatus:computePDStatus: "+moment.name()+" event "+event.getEventId()+" "+pdStatus);
		return pdStatus;
	}

	public void setContext(Context ctx) {
		context = ctx;
		STATUS_LABEL.setCurrentContext(context);
	}

	public void setHistoryEventCorrupted(boolean corrupted) {
		historyEventCorrupted = corrupted;
		STATUS_LABEL.setHistoryEventCorrupted(historyEventCorrupted);
	}

	private String fillUnknownValue(int length) {
		String result = "";
		for(int i=0; i<length; i++)
			result = result.concat("x");
		return result;
	}

}
