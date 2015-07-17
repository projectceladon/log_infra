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

import com.intel.crashreport.common.IEventLog;
import com.intel.crashreport.Log;

public class EventLog implements IEventLog {

	public void d(String msg) {
		Log.d(msg);
	}

	public void e(String msg) {
		Log.e(msg);
	}

	public void w(String msg) {
		Log.w(msg);
	}

	public void i(String msg) {
		Log.i(msg);
	}

	public void d(String msg, Throwable tr) {
		Log.d(msg, tr);
	}

	public void e(String msg, Throwable tr) {
		Log.e(msg, tr);
	}

	public void w(String msg, Throwable tr) {
		Log.w(msg, tr);
	}

	public void i(String msg, Throwable tr) {
		Log.i(msg, tr);
	}
}
