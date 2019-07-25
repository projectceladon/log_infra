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

package com.intel.crashreport.database;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import com.intel.crashreport.common.IEventLog;
import com.intel.crashreport.core.Logger;

public class Utils {
	private static final int COEF_S_TO_MS = 1000;
	public static final String[] INVALID_EVENTS = new String [] { "KDUMP" };
	private static final IEventLog log = Logger.getLog();

	public static int convertDateForDB(String sDate) {
		int iResult = -1;
		if (sDate != null) {
			try {
				com.intel.crashreport.common.Utils.PARSE_DF.setTimeZone(
					TimeZone.getTimeZone("GMT"));
				iResult = convertDateForDb(
					com.intel.crashreport.common.Utils.PARSE_DF.parse(sDate));
			} catch (ParseException e) {
				log.d("convertDateForDB: " + e);
			}
		}
		return iResult;
	}

	public static int convertDateForDb(Date date) {
		if (date==null) {
			return -1;
		}
		return (int)(date.getTime() / COEF_S_TO_MS);
	}

	public static Date convertDateForJava(int date) {
		long dateLong = date;
		dateLong = dateLong * COEF_S_TO_MS;
		return new Date(dateLong);
	}

	/**
	 * Returns if the event log is valid or not depending on the event type.
	 * This aims to never upload very large event logs whatever the
	 * available connection type is.
	 * @param eventType is the type of the event
	 * @return true if the event log is valid by default. False otherwise.
	 */
	public static boolean isEventLogsValid(String eventType) {
		return (!Arrays.asList(INVALID_EVENTS).contains(eventType));
	}
}
