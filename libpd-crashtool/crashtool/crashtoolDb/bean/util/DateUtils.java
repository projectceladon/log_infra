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

package com.intel.crashtool.crashtoolDb.bean.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.apache.log4j.Logger;

public class DateUtils {

	//private final static Logger logger = Logger.getLogger(DateUtils.class);

	public static final String PATTERN_TIMESTAMP = "yyyy-MM-dd HH:mm:ss";
	public static final DateFormat DF_TIMESTAMP = new SimpleDateFormat( DateUtils.PATTERN_TIMESTAMP);

	public static final String PATTERN_DAY = "yyyy-MM-dd EEE";
	public static final DateFormat DF_DAY = new SimpleDateFormat( DateUtils.PATTERN_DAY, Locale.US);

	public static final String PATTERN_FOR_EXPORT = "yyyy-MM-dd_HH-mm-ss";
	public static final DateFormat DF_FOR_EXPORT = new SimpleDateFormat( DateUtils.PATTERN_FOR_EXPORT);

	public static final String PATTERN_DATE_BUILDID = "yyyyMMdd.HHmmss";
	public static final DateFormat DF_BUILDID = new SimpleDateFormat( DateUtils.PATTERN_DATE_BUILDID);

	public static final String PATTERN_DURATION_DAY = "dddd HH";



	/**
	 * convert a date to string with a pattern in parameter, null safe
	 * @param date
	 * @return
	 */
	private static String dateToString(Date date, DateFormat formatter) {

		String result = null;
		if (date != null) {
			formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
			result = formatter.format(date);
		}
		return result;
	}

	public static String dateToString(Date date) {
		return dateToString(date, DF_TIMESTAMP);
	}

	public static String dateToStringDay(Date date) {
		return dateToString(date, DF_DAY);
	}

	public static String stringToDateForExport(Date date) {
		return dateToString(date, DF_FOR_EXPORT);
	}

	/**convert a string to date, null safe
	 *
	 * @param date
	 * @return
	 */
	public static Date stringToDate(String date) {

		return stringToDate(date, DF_TIMESTAMP);
	}



	/**convert a string with a pattern in parameter to date
	 *
	 * @param date
	 * @return
	 */
	private static Date stringToDate(String date, DateFormat formatter) {

		Date result = null;
		try {
			if (date != null) {
				formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
				result = formatter.parse(date);
			}
		} catch (ParseException e) {
			//logger.error(e);
		}
		return result;
	}

	/**
	 * get a date from a text
	 * @param src
	 * @return
	 */
	public static Date getDateFromBuildId(String src) {

		DateFormat df = new SimpleDateFormat("yyyyMMdd.HHmmss");
      Pattern p = Pattern.compile("[0-9]{8}.[0-9]{6}");
      Matcher m = p.matcher(src);
		Date date = null;
		try {
			if (src != null) {
				if (m.find()) {
				String dateString = src.substring(src.length()-15, src.length());
				date = df.parse(dateString);
			}
			}
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	/**
	 * format a long to string %%0%dd format
	 * @param uptime
	 * @return
	 */
	public static String formatDuration(long uptime) {

		//return DurationFormatUtils.formatDuration(uptime * 1000, PATTERN_DURATION_DAY);

		String format = String.format("%%0%dd", 2);

		// String minutes = String.format(format, (elapsedTime % 3600) / 60);
		String hours = String.format(format, uptime / 3600);
		// String time = hours + " h " + minutes + " min";
		String time = hours + " h";
		return time;
	}
}
