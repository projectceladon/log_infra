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
