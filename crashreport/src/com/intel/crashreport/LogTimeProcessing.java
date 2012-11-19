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
 * Author: Nicolas BENOIT <nicolasx.benoit@intel.com>
 */
package com.intel.crashreport;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LogTimeProcessing {

	private String logPath;

	public final static int DEFAULT_LOGS_VALUE = 5;
	private final static long COEFF_HOUR_2_MS =  1000 * 60 *60; /* coeff used to convert milliseconds to hour */
	private final static long THRESHOLD_VALUE = 30*24; /* represents one month in hour */

	public LogTimeProcessing(String aLogPath){
		logPath = aLogPath;
	}


	public long getDefaultLogHour() {
		return getLogHourByNumber(DEFAULT_LOGS_VALUE);
	}


	public long getLogHourByNumber(int iLog) {
		File searchFolder = new File(logPath );
		File[] files = searchFolder.listFiles();
		long tmpTime;
		long thresholdCalculated = THRESHOLD_VALUE * COEFF_HOUR_2_MS;;
		long logATime;
		long logBTime;
		long logcurTime = 0;

		List<String> listAplogs = new ArrayList<String>();
		List<Long> times = new ArrayList<Long>();
		for (int i = 1; i < iLog; i++) {
			listAplogs.add("aplog." + i);
		}
		//add current aplog to increase, log time precision
		listAplogs.add("aplog");
		int iNbLog = 0;
		if(files!=null) {
			for(File f: files) {
				if (listAplogs.contains(f.getName())){
					iNbLog++;
					times.add(f.lastModified());
				}
			}
			Collections.sort(times);
			for (int i = 1; i < times.size(); i++) {
				logATime = times.get(i-1);
				logBTime = times.get(i);
				tmpTime = Math.abs(logATime - logBTime);
				if (tmpTime < thresholdCalculated){
					logcurTime+=tmpTime;
				}
			}
			if (iNbLog > 1 ) {
				return  (logcurTime)/COEFF_HOUR_2_MS;
			}else {
				return -1;
			}
		}
		return -1;
	}


}
