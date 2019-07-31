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
