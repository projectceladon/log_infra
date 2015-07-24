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

package com.intel.crashreport.common;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class Utils {
	private static final int COEF_S_TO_MS = 1000;
	public final static SimpleDateFormat PARSE_DF = new SimpleDateFormat("yyyy-MM-dd/HH:mm:ss");
	public static final int WIFI_LOGS_SIZE = 10 * 1024 * 1024;

	public static enum EVENT_FILTER{
		ALL,
		INFO,
		CRASH
	}

	public static List<String> parseUniqueKey(String aKey) {
		List<String> resultList = new ArrayList<String>();
		String filteredKey = aKey.replaceAll("\\[", "" );
		filteredKey = filteredKey.replaceAll("\\]", "" );
		String[] tmpList = filteredKey.split(", ");
		for (String retval:tmpList) {
			resultList.add(retval);
		}
		return resultList;
	}

	public static int convertUptime(String sUpTime){
		int iResultUpTime = -1;
		int iHour, iMinute, iSecond;
		String[] sDecodedUptime = sUpTime.split(":");
		if (sDecodedUptime.length == 3){
			try {
				iHour = Integer.parseInt(sDecodedUptime[0]);
				iMinute = Integer.parseInt(sDecodedUptime[1]);
				iSecond = Integer.parseInt(sDecodedUptime[2]);
				iResultUpTime = (iHour * 60 * 60) + (iMinute * 60) + iSecond;
			}
			catch (Exception e) {
				iResultUpTime= -1;
				System.err.println( "convertUptime  exception : " + e);
			}
		}

		return iResultUpTime;
	}

	/**
	 * Converts a date defined in seconds and under long format to
	 * a human readable date under string format.
	 * @param date date defined in seconds
	 * @return human readable date
	 */
	public static String convertDate( long date ) {
		Date convertedDate = new Date(date * COEF_S_TO_MS);
		PARSE_DF.setTimeZone(TimeZone.getTimeZone("GMT"));
		return PARSE_DF.format(convertedDate);
	}
}
