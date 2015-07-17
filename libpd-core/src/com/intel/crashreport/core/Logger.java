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

package com.intel.crashreport.core;

import com.intel.crashreport.common.IEventLog;

public class Logger implements IEventLog {
	private static IEventLog mLogger;

	private Logger() {
	}

	//@SuppressWarnings("unchecked")
	public static IEventLog getLog() {
		if (mLogger != null)
			return mLogger;

		try {
			Class c = Class.forName("com.intel.crashreport.specific.EventLog");
			mLogger = (IEventLog)c.newInstance();
		}
		catch (ClassNotFoundException e) {
			mLogger = new Logger();
		}
		catch (Exception e) {
			mLogger = new Logger();
		}

		return mLogger;
	}

	public void d(String msg){}
	public void e(String msg){}
	public void w(String msg){}
	public void i(String msg){}

	public void d(String msg, Throwable tr){}
	public void e(String msg, Throwable tr){}
	public void w(String msg, Throwable tr){}
	public void i(String msg, Throwable tr){}
}
