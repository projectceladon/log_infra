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
