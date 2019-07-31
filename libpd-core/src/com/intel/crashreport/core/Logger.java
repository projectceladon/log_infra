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
