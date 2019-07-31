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
